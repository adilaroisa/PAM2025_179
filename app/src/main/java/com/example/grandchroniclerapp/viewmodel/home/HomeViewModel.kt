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
    // PENTING: Angka ini HARUS SAMA dengan 'limit' di server.js
    // Ubah jadi 3 saat testing, ubah balik jadi 20 saat production/sidang
    companion object {
        const val PAGE_SIZE = 3
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
            // Jangan set Loading full screen jika hanya refresh (opsional, tapi lebih bagus UX-nya)
            homeUiState = HomeUiState.Loading
        }

        // Cegah load jika data habis atau sedang loading (kecuali reset paksa)
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
                        // Tambahkan data baru ke list yang sudah ada
                        articles.addAll(newData)
                    }

                    // --- LOGIKA UTAMA PERBAIKAN ---
                    // Cek apakah data yang diterima kurang dari PAGE_SIZE?
                    // Jika iya, berarti itu halaman terakhir.
                    if (newData.size < PAGE_SIZE) {
                        canLoadMore = false
                    } else {
                        // Jika pas sesuai limit, mungkin masih ada halaman berikutnya
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