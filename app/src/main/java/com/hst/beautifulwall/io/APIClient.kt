package com.hst.beautifulwall.io

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class APIClient {
    companion object {
        private var retrofit : Retrofit? = null

        fun getRetrofit() : Retrofit? {
            val httpLoggingInterceptor =  HttpLoggingInterceptor();
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
            val client = OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build()
            retrofit = Retrofit.Builder().baseUrl("http://103.226.250.94:3002/")
                .addConverterFactory(GsonConverterFactory.create()).client(client).build()
            return retrofit;
        }
    }
}