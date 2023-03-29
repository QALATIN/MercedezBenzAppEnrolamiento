package com.latinid.mercedes.util;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class GetPost {

    public static JSONObject crearGet(String url, Context context) throws ExecutionException, InterruptedException {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null, future, future);
        req.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(req);
        return future.get();
    }

    public static JSONObject crearGetHeaders(String url, HashMap<String, String> headers, Context context) throws ExecutionException, InterruptedException {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null, future, future){
            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(req);
        return future.get();
    }

    public static JSONObject crearPost(String url, JSONObject jsonObject, Context context) throws ExecutionException, InterruptedException {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, jsonObject, future, future);
        req.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(req);
        return future.get();
    }

    public static JSONObject crearPostHeaders(String url, JSONObject jsonObject, HashMap<String, String> headers, Context context) throws ExecutionException, InterruptedException {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, jsonObject, future, future){
            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(req);
        return future.get();
    }
}
