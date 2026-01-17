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

// Wrapper Classes
data class ExistingImageState(
    val url: String,
    var caption: String
)

data class NewImageState(
    val uri: Uri,
    var caption: String = ""
)

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
    var tags by mutableStateOf("")
    var selectedCategory: Category? by mutableStateOf(null)
    var categories: List<Category> by mutableStateOf(emptyList())

    var oldImages = mutableStateListOf<ExistingImageState>()
        private set
    private var deletedImageUrls = mutableListOf<String>()

    var newImages = mutableStateListOf<NewImageState>()
        private set

    // --- State Awal (Untuk Cek Perubahan) ---
    private var initialTitle = ""
    private var initialContent = ""
    private var initialTags = ""
    private var initialCategoryId = 0
    private var initialOldCaptions: List<String> = emptyList()

    val totalImagesCount: Int get() = oldImages.size + newImages.size

    init { fetchCategories() }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val res = repository.getCategories()
                if (res.status) {
                    categories = res.data.filter { it.category_id != 7 }
                }
            } catch (e: Exception) { }
        }
    }

    fun loadArticleData(articleId: Int) {
        if (title.isNotEmpty() || initialTitle.isNotEmpty()) return

        viewModelScope.launch {
            try {
                val res = repository.getArticleDetail(articleId)
                if (res.status && res.data != null) {
                    val article = res.data!!
                    title = article.title
                    content = article.content ?: ""
                    tags = article.tags ?: ""

                    selectedCategory = categories.find { it.category_id == article.category_id }
                        ?: categories.find { it.category_name == article.category_name }

                    oldImages.clear()
                    val tempCaptions = mutableListOf<String>()
                    article.images.forEachIndexed { index, url ->
                        val cap = if (index < article.captions.size) article.captions[index] else ""
                        oldImages.add(ExistingImageState(url, cap))
                        tempCaptions.add(cap)
                    }

                    deletedImageUrls.clear()
                    newImages.clear()

                    // Simpan Snapshot Awal
                    initialTitle = article.title
                    initialContent = article.content ?: ""
                    initialTags = article.tags ?: ""
                    initialCategoryId = article.category_id
                    initialOldCaptions = tempCaptions.toList()
                }
            } catch (e: Exception) {
                uiState = UploadUiState.Error("Gagal load data")
                _snackbarEvent.send("Gagal memuat: ${e.message}")
            }
        }
    }

    // --- Manipulasi Gambar ---
    fun deleteOldImage(item: ExistingImageState) {
        oldImages.remove(item)
        deletedImageUrls.add(item.url)
    }
    fun updateOldCaption(index: Int, text: String) {
        if (index in oldImages.indices) oldImages[index] = oldImages[index].copy(caption = text)
    }
    fun updateImages(uris: List<Uri>) {
        uris.forEach { newImages.add(NewImageState(it)) }
    }
    fun removeNewImage(item: NewImageState) { newImages.remove(item) }
    fun updateNewCaption(index: Int, text: String) {
        if (index in newImages.indices) newImages[index] = newImages[index].copy(caption = text)
    }

    fun updateTitle(t: String) { title = t }
    fun updateContent(c: String) { content = c }
    fun updateTags(t: String) { tags = t }
    fun updateCategory(c: Category) { selectedCategory = c }

    // --- DIRTY CHECK ---
    fun hasChanges(): Boolean {
        if (title.trim() != initialTitle.trim()) return true
        if (content.trim() != initialContent.trim()) return true
        if (tags.trim() != initialTags.trim()) return true
        if ((selectedCategory?.category_id ?: 0) != initialCategoryId) return true
        if (newImages.isNotEmpty()) return true
        if (deletedImageUrls.isNotEmpty()) return true

        if (oldImages.size == initialOldCaptions.size) {
            oldImages.forEachIndexed { index, img ->
                if (img.caption != initialOldCaptions[index]) return true
            }
        } else {
            return true
        }
        return false
    }

    // --- SUBMIT UPDATE ---
    fun submitUpdate(context: Context, articleId: Int, status: String) {
        if (title.isBlank()) {
            viewModelScope.launch { _snackbarEvent.send("Judul tidak boleh kosong!") }
            return
        }

        viewModelScope.launch {
            uiState = UploadUiState.Loading
            try {
                val gson = Gson()
                val deletedJson = if (deletedImageUrls.isNotEmpty()) gson.toJson(deletedImageUrls) else null
                val allCaptions = mutableListOf<String>()
                oldImages.forEach { allCaptions.add(it.caption) }
                newImages.forEach { allCaptions.add(it.caption) }

                val contentToSend = if (content.isBlank()) null else content
                val catIdToSend = selectedCategory?.category_id?.toString()

                val res = repository.updateArticle(
                    articleId = articleId,
                    title = title,
                    content = contentToSend,
                    categoryId = catIdToSend,
                    status = status,
                    tags = tags,
                    captions = allCaptions,
                    newImageUris = newImages.map { it.uri },
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