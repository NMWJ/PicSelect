package com.rx.net;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class MyRetrofit {


    private static OkHttpClient client;

    private MyRetrofit(){

    }

    public static void init(){
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10,TimeUnit.SECONDS)
                .build();

    }

    public static OkHttpClient getClient(){
        return client;
    }



}
