package com.example.grandchroniclerapp.viewmodel.article

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.model.Article
import com.example.grandchroniclerapp.repository.ArticleRepository
import com.example.grandchroniclerapp.uicontroller.navigation.DestinasiDetail
import kotlinx.coroutines.launch
import java.io.IOException

// Definisi State UI
sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val article: Article) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class DetailArticleViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ArticleRepository
) : ViewModel() {

    var detailUiState: DetailUiState by mutableStateOf(DetailUiState.Loading)
        private set

    // Mengambil ID artikel dari argumen navigasi
    private val articleId: Int = checkNotNull(savedStateHandle[DestinasiDetail.articleIdArg])

    init {
        getArticleDetail()
    }

    fun getArticleDetail() {
        viewModelScope.launch {
            detailUiState = DetailUiState.Loading
            try {
                // Panggil repository
                val response = repository.getArticleDetail(articleId)

                if (response.status && response.data != null) {
                    detailUiState = DetailUiState.Success(response.data)
                } else {
                    detailUiState = DetailUiState.Error(response.message ?: "Gagal memuat data artikel")
                }
            } catch (e: IOException) {
                detailUiState = DetailUiState.Error("Tidak ada koneksi internet.")
            } catch (e: Exception) {
                detailUiState = DetailUiState.Error("Terjadi kesalahan: ${e.localizedMessage}")
            }
        }
    }
}