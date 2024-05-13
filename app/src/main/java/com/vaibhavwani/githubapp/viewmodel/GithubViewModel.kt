package com.vaibhavwani.githubapp.viewmodel

import androidx.lifecycle.ViewModel
import com.vaibhavwani.githubapp.data.GithubService
import com.vaibhavwani.githubapp.data.model.GithubAccessToken
import com.vaibhavwani.githubapp.data.model.GithubComment
import com.vaibhavwani.githubapp.data.model.GithubPR
import com.vaibhavwani.githubapp.data.model.GithubRepo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.ResponseBody

class GithubViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    var token: String? = null
    var owner: String? = null
    var selectedRepo: String? = null
    var selectedPR: String? = null

    val tokenFlow = MutableStateFlow<String?>(null)
    val repoFlow = MutableStateFlow<List<GithubRepo>?>(null)
    val prFlow = MutableStateFlow<List<GithubPR>?>(null)
    val commentFlow = MutableStateFlow<List<GithubComment>?>(null)
    val postCommentFlow = MutableStateFlow<Boolean>(false)

    fun getToken(
        code: String,
        clientId: String,
        clientSecret: String,
    ) {
        compositeDisposable.add(
            GithubService.getUnauthorizedApi().getAccessToken(clientId, clientSecret, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<GithubAccessToken>() {
                    override fun onSuccess(t: GithubAccessToken) {
                        tokenFlow.value = t.accessToken
                    }

                    override fun onError(e: Throwable) {
                        tokenFlow.value = "Error"
                    }
                }
                )
        )
    }

    fun getAllRepos() {
        token?.let {
            compositeDisposable.add(
                GithubService.getAuthorizedApi(it).getAllRepos()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<List<GithubRepo>>() {
                        override fun onSuccess(t: List<GithubRepo>) {
                            repoFlow.value = t
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                    )
            )
        }
    }

    fun getAllPRs(
        owner: String,
        repo: String,
    ) {
        this.owner = owner
        this.selectedRepo = repo
        token?.let {
            compositeDisposable.add(
                GithubService.getAuthorizedApi(it).getPRs(owner, repo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<List<GithubPR>>() {
                        override fun onSuccess(t: List<GithubPR>) {
                            prFlow.value = t
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                    )
            )
        }
    }

    fun getComments(
        pr: String,
    ) {
        selectedPR = pr
        token?.let {
            if (owner != null && selectedRepo != null) {
                compositeDisposable.add(
                    GithubService.getAuthorizedApi(it).getComments(owner!!, selectedRepo!!, pr)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<List<GithubComment>>() {
                            override fun onSuccess(t: List<GithubComment>) {
                                commentFlow.value = t
                            }

                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                            }
                        }
                        )
                )
            }
        }
    }

    fun postComment(
        comment: GithubComment
    ) {
        token?.let {
            if (owner != null && selectedRepo != null && selectedPR != null) {
                compositeDisposable.add(
                    GithubService.getAuthorizedApi(it).postComment(owner!!, selectedRepo!!, selectedPR!!, comment)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<ResponseBody>() {
                            override fun onSuccess(t: ResponseBody) {
                                postCommentFlow.value = true
                            }

                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                            }
                        }
                        )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}

sealed class UiState {
    object Loading : UiState()
    object Error : UiState()
    class Success(data: String) : UiState()
}