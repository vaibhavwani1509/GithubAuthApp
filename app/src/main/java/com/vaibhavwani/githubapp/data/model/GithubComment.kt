package com.vaibhavwani.githubapp.data.model

import com.google.gson.annotations.SerializedName

data class GithubComment(
    val id: String? = null,
    val body: String? = null,
) {
    override fun toString(): String {
        return "$body - $id"
    }
}
