package com.vaibhavwani.githubapp.data

import com.vaibhavwani.githubapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object GithubService {
    const val BASE_URL = "https://api.github.com"

    fun getUnauthorizedApi() =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
            .create<GithubApi>()


    fun getAuthorizedApi(token: String): GithubApi {
        val okHttpClient = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            okHttpClient.addInterceptor(logging)
        }

        okHttpClient.addInterceptor { chain ->
            val request = chain.request()
            val newRequest = request.newBuilder()
                .addHeader("Authorization", "token ${token}")
                .build()
            chain.proceed(newRequest)
        }

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
            .create<GithubApi>()
    }
}