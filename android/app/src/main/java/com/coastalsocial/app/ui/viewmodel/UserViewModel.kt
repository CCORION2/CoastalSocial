package com.coastalsocial.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coastalsocial.app.data.model.Post
import com.coastalsocial.app.data.model.User
import com.coastalsocial.app.data.repository.FriendRepository
import com.coastalsocial.app.data.repository.PostRepository
import com.coastalsocial.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true
)

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isSaved: Boolean = false
)

data class SearchUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val query: String = "",
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val friendRepository: FriendRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(username: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            userRepository.getUserProfile(username)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user
                    )
                    loadUserPosts(username)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    private fun loadUserPosts(username: String, page: Int = 1) {
        viewModelScope.launch {
            postRepository.getUserPosts(username, page)
                .onSuccess { data ->
                    val newPosts = if (page == 1) data.posts else _uiState.value.posts + data.posts
                    _uiState.value = _uiState.value.copy(
                        posts = newPosts,
                        page = data.page + 1,
                        hasMore = data.hasMore
                    )
                }
        }
    }

    fun sendFriendRequest() {
        val user = _uiState.value.user ?: return
        
        viewModelScope.launch {
            friendRepository.sendFriendRequest(user.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        user = user.copy(friendshipStatus = "pending")
                    )
                }
        }
    }

    fun removeFriend() {
        val user = _uiState.value.user ?: return
        
        viewModelScope.launch {
            friendRepository.removeFriend(user.id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        user = user.copy(friendshipStatus = null)
                    )
                }
        }
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
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun loadCurrentUser(username: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            userRepository.getUserProfile(username)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Profil laden fehlgeschlagen"
                    )
                }
        }
    }

    fun updateProfile(
        fullName: String,
        bio: String,
        location: String,
        website: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            userRepository.updateProfile(
                fullName = fullName.ifBlank { null },
                bio = bio.ifBlank { null },
                location = location.ifBlank { null },
                website = website.ifBlank { null }
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        isSaved = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = exception.message
                    )
                }
        }
    }

    fun uploadProfilePicture(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            
            userRepository.uploadProfilePicture(uri)
                .onSuccess { url ->
                    val user = _uiState.value.user
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        user = user?.copy(profilePicture = url)
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Upload fehlgeschlagen"
                    )
                }
        }
    }
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun search(query: String) {
        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(users = emptyList(), query = query)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, query = query)

            userRepository.searchUsers(query)
                .onSuccess { users ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        users = users
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Suche fehlgeschlagen"
                    )
                }
        }
    }

    fun clearSearch() {
        _uiState.value = SearchUiState()
    }
}
