package com.vaibhavwani.githubapp.data

import com.vaibhavwani.githubapp.data.model.GithubAccessToken
import com.vaibhavwani.githubapp.data.model.GithubComment
import com.vaibhavwani.githubapp.data.model.GithubPR
import com.vaibhavwani.githubapp.data.model.GithubRepo
import io.reactivex.rxjava3.core.Single
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface GithubApi {

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("https://github.com/login/oauth/access_token")
    fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
    ): Single<GithubAccessToken>

    @GET("user/repos")
    fun getAllRepos(): Single<List<GithubRepo>>

    @GET("/repos/{owner}/{repo}/pulls")
    fun getPRs(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
    ): Single<List<GithubPR>>

    @GET("/repos/{owner}/{repo}/issues/{pr}/comments")
    fun getComments(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pr") pr: String,
    ): Single<List<GithubComment>>

    @POST("/repos/{owner}/{repo}/issues/{pr}/comments")
    fun postComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pr") pr: String,
        @Body comment: GithubComment,
    ): Single<ResponseBody>
}