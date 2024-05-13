package com.vaibhavwani.githubapp.data.model

data class GithubRepo(
    val name: String? = null,
    val url: String? = null,
    val owner: GithubUser? = null,
) {
    override fun toString(): String {
        return "$name - $url"
    }
}


data class GithubUser(
    val id: String? = null,
    val login: String? = null,
)