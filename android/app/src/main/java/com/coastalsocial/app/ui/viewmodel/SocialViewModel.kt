package com.coastalsocial.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coastalsocial.app.data.model.*
import com.coastalsocial.app.data.repository.FriendRepository
import com.coastalsocial.app.data.repository.MessageRepository
import com.coastalsocial.app.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessagesUiState(
    val isLoading: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val error: String? = null
)

data class ChatUiState(
    val isLoading: Boolean = false,
    val messages: List<Message> = emptyList(),
    val isSending: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true
)

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null
)

data class FriendsUiState(
    val isLoading: Boolean = false,
    val friends: List<Friend> = emptyList(),
    val requests: List<Friend> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    fun loadConversations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            messageRepository.getConversations()
                .onSuccess { conversations ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        conversations = conversations
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
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentUserId: Int = 0

    fun loadMessages(userId: Int) {
        currentUserId = userId
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            messageRepository.getMessages(userId)
                .onSuccess { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        messages = data.messages,
                        page = data.page + 1,
                        hasMore = data.hasMore
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

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)

            messageRepository.sendMessage(currentUserId, content)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSending = false)
                    loadMessages(currentUserId) // Reload to get new message
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = exception.message
                    )
                }
        }
    }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            notificationRepository.getNotifications()
                .onSuccess { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notifications = data.notifications,
                        unreadCount = data.unreadCount
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

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
            loadNotifications()
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
            loadNotifications()
        }
    }
}

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendRepository: FriendRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
        loadRequests()
    }

    fun loadFriends() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            friendRepository.getFriends()
                .onSuccess { friends ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        friends = friends
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

    fun loadRequests() {
        viewModelScope.launch {
            friendRepository.getFriendRequests()
                .onSuccess { requests ->
                    _uiState.value = _uiState.value.copy(requests = requests)
                }
        }
    }

    fun acceptRequest(userId: Int) {
        viewModelScope.launch {
            friendRepository.acceptFriendRequest(userId)
                .onSuccess {
                    loadFriends()
                    loadRequests()
                }
        }
    }

    fun declineRequest(userId: Int) {
        viewModelScope.launch {
            friendRepository.declineFriendRequest(userId)
                .onSuccess {
                    loadRequests()
                }
        }
    }

    fun removeFriend(userId: Int) {
        viewModelScope.launch {
            friendRepository.removeFriend(userId)
                .onSuccess {
                    loadFriends()
                }
        }
    }
}
