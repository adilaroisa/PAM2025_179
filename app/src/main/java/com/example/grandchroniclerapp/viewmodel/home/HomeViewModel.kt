package com.example.grandchroniclerapp.viewmodel.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.model.Article
import com.example.grandchroniclerapp.repository.ArticleRepository
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Loading : HomeUiState
    object Success : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(private val repository: ArticleRepository) : ViewModel() {

    // --- KONFIGURASI PAGINATION ---
    companion object {
        const val PAGE_SIZE = 20
    }

    var homeUiState: HomeUiState by mutableStateOf(HomeUiState.Loading)
        private set

    var articles = mutableStateListOf<Article>()
        private set

    private var currentPage = 1
    var canLoadMore = true
        private set
    var isLoadingMore = false
        private set

    init {
        loadArticles(reset = true)
    }

    fun loadArticles(reset: Boolean = false) {
        if (reset) {
            currentPage = 1
            canLoadMore = true
            homeUiState = HomeUiState.Loading
        }

        if (!canLoadMore || (isLoadingMore && !reset)) return

        isLoadingMore = true

        viewModelScope.launch {
            try {
                // Panggil API
                val response = repository.getArticles(page = currentPage)

                if (response.status) {
                    val newData = response.data ?: emptyList()

                    if (reset) {
                        articles.clear()
                        articles.addAll(newData)
                        homeUiState = HomeUiState.Success
                    } else {
                        articles.addAll(newData)
                    }

                    // --- LOGIKA PAGINATION ---
                    if (newData.size < PAGE_SIZE) {
                        canLoadMore = false
                    } else {
                        currentPage++
                    }
                } else {
                    if (reset) homeUiState = HomeUiState.Error(response.message ?: "Gagal memuat data")
                }
            } catch (e: Exception) {
                if (reset) homeUiState = HomeUiState.Error("Error: ${e.message}")
            } finally {
                isLoadingMore = false
            }
        }
    }
}