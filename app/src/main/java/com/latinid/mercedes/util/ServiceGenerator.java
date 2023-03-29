package com.latinid.mercedes.util;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class ServiceGenerator {

    public static final String API_BASE_URL = "https://mbfs.latinid.com.mx:9582/Gateway/api/recuperacion/";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL);

    public static <S> S createService(Class<S> serviceClass){
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }

}
