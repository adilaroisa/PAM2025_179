package com.example.grandchroniclerapp.viewmodel.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.model.Article
import com.example.grandchroniclerapp.model.Category
import com.example.grandchroniclerapp.repository.ArticleRepository
import kotlinx.coroutines.launch
import java.io.IOException

// UI State Definition
sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val articles: List<Article>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(private val repository: ArticleRepository) : ViewModel() {

    var searchUiState: SearchUiState by mutableStateOf(SearchUiState.Idle)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var categories: List<Category> by mutableStateOf(emptyList())
        private set

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = repository.getCategories()
                if (response.status) {
                    // Filter kategori agar ID 7 (Tanpa Kategori) tidak masuk list
                    categories = response.data.filter { it.category_id != 7 }
                }
            } catch (e: Exception) {
                // Kategori gagal dimuat, abaikan saja agar UI tidak crash
            }
        }
    }

    fun updateQuery(newQuery: String) {
        searchQuery = newQuery

        if (newQuery.isBlank()) {
            searchUiState = SearchUiState.Idle
        } else {
            performSearch(newQuery)
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            searchUiState = SearchUiState.Loading
            try {
                val response = repository.getArticles(query)
                if (response.status) {
                    searchUiState = SearchUiState.Success(response.data ?: emptyList())
                } else {
                    searchUiState = SearchUiState.Error(response.message ?: "Gagal mencari artikel")
                }
            } catch (e: IOException) {
                searchUiState = SearchUiState.Error("Tidak ada koneksi internet")
            } catch (e: Exception) {
                searchUiState = SearchUiState.Error("Terjadi kesalahan: ${e.message ?: "Kesalahan tidak diketahui"}")
            }
        }
    }
}