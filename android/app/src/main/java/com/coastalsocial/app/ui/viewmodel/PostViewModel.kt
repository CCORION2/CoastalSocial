package com.coastalsocial.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coastalsocial.app.data.model.Comment
import com.coastalsocial.app.data.model.Post
import com.coastalsocial.app.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val isRefreshing: Boolean = false
)

data class PostDetailUiState(
    val isLoading: Boolean = false,
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val error: String? = null,
    val isCommenting: Boolean = false
)

data class CreatePostUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed(refresh: Boolean = false) {
        if (_uiState.value.isLoading) return
        
        viewModelScope.launch {
            val page = if (refresh) 1 else _uiState.value.page
            _uiState.value = _uiState.value.copy(
                isLoading = !refresh,
                isRefreshing = refresh,
                error = null
            )

            postRepository.getFeed(page)
                .onSuccess { data ->
                    val newPosts = if (refresh) data.posts else _uiState.value.posts + data.posts
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        posts = newPosts,
                        page = data.page + 1,
                        hasMore = data.hasMore
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = exception.message
                    )
                }
        }
    }

    fun loadMore() {
        if (_uiState.value.hasMore && !_uiState.value.isLoading) {
            loadFeed()
        }
    }

    fun refresh() {
        loadFeed(refresh = true)
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            postRepository.toggleLike(post.id)
                .onSuccess { liked ->
                    val updatedPosts = _uiState.value.posts.map {
                        if (it.id == post.id) {
                            it.copy(
                                isLiked = liked,
                                likesCount = if (liked) it.likesCount + 1 else it.likesCount - 1
                            )
                        } else it
                    }
                    _uiState.value = _uiState.value.copy(posts = updatedPosts)
                }
        }
    }

    fun toggleSave(post: Post) {
        viewModelScope.launch {
            postRepository.toggleSave(post.id)
                .onSuccess { saved ->
                    val updatedPosts = _uiState.value.posts.map {
                        if (it.id == post.id) it.copy(isSaved = saved) else it
                    }
                    _uiState.value = _uiState.value.copy(posts = updatedPosts)
                }
        }
    }
}

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    fun loadPost(postId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            postRepository.getPost(postId)
                .onSuccess { post ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        post = post
                    )
                    loadComments(post.id)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    private fun loadComments(postId: Int) {
        viewModelScope.launch {
            postRepository.getComments(postId)
                .onSuccess { comments ->
                    _uiState.value = _uiState.value.copy(comments = comments)
                }
        }
    }

    fun addComment(content: String) {
        val post = _uiState.value.post ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCommenting = true)

            postRepository.addComment(post.id, content)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isCommenting = false)
                    loadComments(post.id)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isCommenting = false,
                        error = "Kommentar fehlgeschlagen"
                    )
                }
        }
    }

    fun toggleLike() {
        val post = _uiState.value.post ?: return
        
        viewModelScope.launch {
            postRepository.toggleLike(post.id)
                .onSuccess { liked ->
                    _uiState.value = _uiState.value.copy(
                        post = post.copy(
                            isLiked = liked,
                            likesCount = if (liked) post.likesCount + 1 else post.likesCount - 1
                        )
                    )
                }
        }
    }
}

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    fun createPost(content: String?, imageUri: Uri?, privacy: String = "public") {
        if (content.isNullOrBlank() && imageUri == null) {
            _uiState.value = _uiState.value.copy(error = "Inhalt oder Bild erforderlich")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            postRepository.createPost(content, imageUri, privacy)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
