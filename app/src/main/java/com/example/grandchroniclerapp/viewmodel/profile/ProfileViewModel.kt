package com.example.grandchroniclerapp.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.data.UserPreferences
import com.example.grandchroniclerapp.model.Article
import com.example.grandchroniclerapp.model.User
import com.example.grandchroniclerapp.repository.ArticleRepository
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(val user: User, val articles: List<Article>) : ProfileUiState
    object Error : ProfileUiState
}

class ProfileViewModel(
    private val repository: ArticleRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _deleteMessage = Channel<String>()
    val deleteMessage = _deleteMessage.receiveAsFlow()

    init {
        getProfile()
    }

    fun getProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val userId = userPreferences.getUserId.first()
                if (userId != -1) {
                    val userRes = repository.getUserDetail(userId)
                    val articleRes = repository.getMyArticles(userId)

                    if (userRes.status && userRes.data != null) {
                        val articles = if (articleRes.status && articleRes.data != null) articleRes.data else emptyList()
                        _uiState.value = ProfileUiState.Success(userRes.data, articles)
                    } else {
                        _uiState.value = ProfileUiState.Error
                    }
                } else {
                    _uiState.value = ProfileUiState.Error
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error
            }
        }
    }

    fun deleteArticle(articleId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.deleteArticle(articleId)
                if (res.status) {
                    _deleteMessage.send("Artikel berhasil dihapus")
                    getProfile() // Refresh data
                } else {
                    _deleteMessage.send(res.message ?: "Gagal menghapus")
                }
            } catch (e: Exception) {
                _deleteMessage.send("Error: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.saveUserId(-1)
        }
    }
}