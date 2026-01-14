package com.example.grandchroniclerapp.viewmodel.article

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.model.Category
import com.example.grandchroniclerapp.repository.ArticleRepository
import com.google.gson.Gson
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class EditArticleViewModel(
    private val repository: ArticleRepository
) : ViewModel() {

    var uiState: UploadUiState by mutableStateOf(UploadUiState.Idle)
        private set

    private val _snackbarEvent = Channel<String>()
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    // Form Data
    var title by mutableStateOf("")
    var content by mutableStateOf("")
    var selectedCategory: Category? by mutableStateOf(null)
    var categories: List<Category> by mutableStateOf(emptyList())

    // Gambar
    var oldImageUrls = mutableStateListOf<String>()
        private set
    private var deletedImageUrls = mutableListOf<String>()
    var newImageUris = mutableStateListOf<Uri>()
        private set

    private var initialTitle = ""
    private var initialContent = ""
    private var initialCategoryId = 0

    val totalImagesCount: Int get() = oldImageUrls.size + newImageUris.size

    init { fetchCategories() }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val res = repository.getCategories()
                if (res.status) categories = res.data
            } catch (e: Exception) { }
        }
    }

    fun loadArticleData(articleId: Int) {
        viewModelScope.launch {
            try {
                val res = repository.getArticleDetail(articleId)
                if (res.status && res.data != null) {
                    val article = res.data!!
                    title = article.title
                    content = article.content
                    selectedCategory = categories.find { it.category_id == article.category_id }
                        ?: categories.find { it.category_name == article.category_name }

                    oldImageUrls.clear()
                    oldImageUrls.addAll(article.images)
                    deletedImageUrls.clear()
                    newImageUris.clear()

                    initialTitle = article.title
                    initialContent = article.content
                    initialCategoryId = article.category_id
                }
            } catch (e: Exception) {
                uiState = UploadUiState.Error("Gagal load data")
                _snackbarEvent.send("Gagal memuat: ${e.message}")
            }
        }
    }

    fun deleteOldImage(url: String) { oldImageUrls.remove(url); deletedImageUrls.add(url) }
    fun updateImages(uris: List<Uri>) { newImageUris.addAll(uris) }
    fun removeNewImage(uri: Uri) { newImageUris.remove(uri) }
    fun updateTitle(t: String) { title = t }
    fun updateContent(c: String) { content = c }
    fun updateCategory(c: Category) { selectedCategory = c }

    fun hasChanges(): Boolean {
        val currentCatId = selectedCategory?.category_id ?: 0
        return title != initialTitle || content != initialContent || currentCatId != initialCategoryId || newImageUris.isNotEmpty() || deletedImageUrls.isNotEmpty()
    }

    // --- SUBMIT UPDATE PINTAR ---
    fun submitUpdate(context: Context, articleId: Int, status: String) {
        // 1. VALIDASI
        var errorMessage: String? = null

        if (title.isBlank()) {
            errorMessage = "Judul tidak boleh kosong!"
        } else if (status == "Published") {
            if (selectedCategory == null) errorMessage = "Pilih kategori untuk terbit!"
            else if (content.isBlank()) errorMessage = "Isi artikel tidak boleh kosong!"
        }

        if (errorMessage != null) {
            uiState = UploadUiState.Error("Validasi Gagal")
            viewModelScope.launch { _snackbarEvent.send(errorMessage!!) }
            return
        }

        // 2. PROSES UPDATE
        viewModelScope.launch {
            uiState = UploadUiState.Loading
            try {
                val gson = Gson()
                val deletedJson = if (deletedImageUrls.isNotEmpty()) gson.toJson(deletedImageUrls) else null

                // Handle Data Kosong (Kirim NULL agar aman)
                val catIdToSend = selectedCategory?.category_id?.toString()
                val contentToSend = if (content.isBlank()) null else content

                val res = repository.updateArticle(
                    articleId = articleId,
                    title = title,
                    content = contentToSend,
                    categoryId = catIdToSend,
                    status = status,
                    newImageUris = newImageUris,
                    deletedImagesJson = deletedJson,
                    context = context
                )

                if (res.status) {
                    uiState = UploadUiState.Success
                    _snackbarEvent.send("Berhasil Diupdate!")
                } else {
                    uiState = UploadUiState.Error(res.message ?: "Gagal")
                    _snackbarEvent.send(res.message ?: "Gagal update")
                }
            } catch (e: Exception) {
                uiState = UploadUiState.Error("Error")
                _snackbarEvent.send("Terjadi kesalahan: ${e.message}")
            }
        }
    }
}