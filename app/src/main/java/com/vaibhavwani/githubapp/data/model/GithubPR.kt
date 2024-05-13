package com.vaibhavwani.githubapp.data.model

import com.google.gson.annotations.SerializedName

data class GithubPR(
    val id: String? = null,
    val title: String? = null,
    val number: String? = null,
    @SerializedName("comments_url") val commentsUrl: String? = null,
    val user: GithubUser? = null,
) {
    override fun toString(): String {
        return "$title - $id"
    }
}
