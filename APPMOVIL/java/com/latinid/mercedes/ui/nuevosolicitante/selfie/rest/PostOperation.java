package com.latinid.mercedes.ui.nuevosolicitante.selfie.rest;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by stoth on 1/25/2017.
 *
 * A utility class for HTTP network operations
 */

public class PostOperation {

    private static final String POST_OPERATION_TAG = "post_operation_tag";
    public  static final String POST_FAILED_KEY = "Failed:";

    private static OkHttpClient sOkHttpClient = null;
    private String mUrlBase, mUploadData, mOperation;
    private final char[]  responseBuffer;

    /**
     * Constructor for Synchronous use
     *
     */
    public PostOperation(String url, String upload, String operation) {
        mUrlBase = url;
        mUploadData = upload;
        mOperation = operation;
        responseBuffer = new char[204800];


        if (mUrlBase.contains("https")) {
            createSecureUnsafeHttpClient();
        }
        else {
            createHttpClient();  // could be a no-op, we only want one for duration of activity
        }
    }

    private synchronized static void createHttpClient() {
        if (sOkHttpClient == null) {
            sOkHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
    }

    private synchronized static void createSecureUnsafeHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            sOkHttpClient = builder.build();
        } catch (Exception e) {
            sOkHttpClient = null;
            throw new RuntimeException(e);
        }
    }


    public synchronized static void shutdownHttpClient() {
        if (sOkHttpClient != null) {
            sOkHttpClient.dispatcher().executorService().shutdown();
            sOkHttpClient.connectionPool().evictAll();
            try {
                sOkHttpClient.cache().close();
            } catch (IOException e) {
                Log.d(POST_OPERATION_TAG, "Cache close failed: " + e.getMessage());
            }
        }
    }

    /**
     * doPostJson
     * connects, writes, and waits for response from server
     *
     * this method is package private as to be called via
     * 'new PostOperation(mUrlBase, mRequestMethod, mUploadData).doPostJson()'
     * in a Synchronous environment.
     */
    public String doPostJson() throws java.net.MalformedURLException {

        String urlRequest = mUrlBase + "/" + mOperation;

        Headers headers = new Headers.Builder()
                .add("Content-Type", "application/json")
                .add("Accept-Language", "en-us")
                .add("Accept", "*/*")
                .add("Accept-Encoding", "gzip, deflate")
                .add("X-Requested-With", "XMLHttpRequest")
                .build();

        RequestBody requestBody = RequestBody
                .create(MediaType.parse("application/json; charset=utf-8"), mUploadData);

        Request request = new Request.Builder()
                .url(urlRequest)
                .headers(headers)
                .post(requestBody)
                .build();

        String response = executeRequest(request);
        return response;
    }

    String doGetResponse() throws java.net.MalformedURLException {
        String urlRequest = mUrlBase + "/" + mOperation;
        Log.d(POST_OPERATION_TAG, "Request: " + urlRequest);

        Request request = new Request.Builder()
                .url(urlRequest)
                .build();  // no post means 'get'

        String response = executeRequest(request);
        return response;
    }

    private String executeRequest(Request request) {
        try {
            Response response = sOkHttpClient.newCall(request).execute();
            ResponseBody body = response.body();
            String result;
            if (body == null) {
                throw new IOException("Unexpected code response body is null");
            }
            result = streamToString(body.byteStream());
            Log.w(POST_OPERATION_TAG, "Response Body: " + result);

            if (!response.isSuccessful()) {
                throw new IOException(result);
            }

            return result;

        } catch (IOException exception) {
            if(exception.getMessage() == null) {
                return "Failed";
            }
            String reason = exception.getMessage();
            if (exception instanceof SocketTimeoutException) {
                Log.w(POST_OPERATION_TAG, "Connection Timed Out: " + exception);
                reason = "Connection Timed Out ";
            }
            //write and return error string
            String reply = String.format("%s%s", POST_FAILED_KEY, reason);
            Log.d(POST_OPERATION_TAG, "RESULT: " + reply);
            Log.d(POST_OPERATION_TAG, "IOException: " + exception.getMessage());
            return reply;
        }
    }

    private String streamToString(final InputStream is) {
        final StringBuilder out = new StringBuilder();

        try (Reader in = new InputStreamReader(is, "UTF-8")) {
            while (true) {
                int rsz = in.read(responseBuffer, 0, responseBuffer.length);
                if (rsz < 0) break;
                out.append(responseBuffer, 0, rsz);
            }
        } catch (UnsupportedEncodingException ex)  {
            Log.e(POST_OPERATION_TAG, "Encoding failure reading http response: " + ex);
        } catch (IOException ioe) {
            Log.e(POST_OPERATION_TAG, "IO Exception reading http response: " + ioe);
        }

        return out.toString();
    }

}
