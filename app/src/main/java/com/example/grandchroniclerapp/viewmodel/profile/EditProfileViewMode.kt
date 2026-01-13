package com.example.grandchroniclerapp.viewmodel.profile

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.data.UserPreferences
import com.example.grandchroniclerapp.model.UpdateUserRequest
import com.example.grandchroniclerapp.repository.ArticleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface EditProfileUiState {
    object Idle : EditProfileUiState
    object Loading : EditProfileUiState
    object Success : EditProfileUiState
    object DeleteSuccess : EditProfileUiState
    data class Error(val message: String) : EditProfileUiState
}

class EditProfileViewModel(
    private val repository: ArticleRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    var uiState: EditProfileUiState by mutableStateOf(EditProfileUiState.Idle)
        private set

    // Data Form
    var fullName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var bio by mutableStateOf("")
    var role by mutableStateOf("Penulis") // State untuk Role (SRS 1.a)

    // Data Foto
    var currentPhotoUrl by mutableStateOf("") // Nama file dari DB
    var selectedImageUri by mutableStateOf<Uri?>(null) // Uri lokal dari galeri

    private var initialFullName = ""
    private var initialEmail = ""
    private var initialBio = ""

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val loggedInId = userPreferences.getUserId.first()
                if (loggedInId != -1) {
                    val response = repository.getUserDetail(loggedInId)
                    if (response.status && response.data != null) {
                        fullName = response.data.full_name
                        email = response.data.email
                        bio = response.data.bio ?: ""
                        role = response.data.role ?: "Penulis"
                        currentPhotoUrl = response.data.profile_photo ?: ""

                        initialFullName = fullName
                        initialEmail = email
                        initialBio = bio
                    }
                }
            } catch (e: Exception) { /* Silent fail */ }
        }
    }

    fun hasChanges(): Boolean {
        return fullName != initialFullName ||
                email != initialEmail ||
                bio != initialBio ||
                password.isNotEmpty() ||
                selectedImageUri != null
    }

    // Fungsi konversi Uri ke Base64 (Untuk upload foto)
    private fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) Base64.encodeToString(bytes, Base64.NO_WRAP) else null
        } catch (e: Exception) {
            null
        }
    }

    fun submitUpdate(context: Context) {
        if (fullName.isBlank() || email.isBlank()) {
            uiState = EditProfileUiState.Error("Nama dan Email tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            uiState = EditProfileUiState.Loading
            try {
                val loggedInId = userPreferences.getUserId.first()
                if (loggedInId != -1) {

                    // Konversi foto ke base64 jika user memilih foto baru
                    val base64Image = selectedImageUri?.let { uriToBase64(context, it) }

                    val request = UpdateUserRequest(
                        full_name = fullName,
                        email = email,
                        password = if (password.isEmpty()) null else password,
                        bio = bio,
                        profile_photo = base64Image
                    )

                    val response = repository.updateUser(loggedInId, request)
                    if (response.status) {
                        uiState = EditProfileUiState.Success
                    } else {
                        uiState = EditProfileUiState.Error(response.message ?: "Gagal update")
                    }
                }
            } catch (e: IOException) {
                uiState = EditProfileUiState.Error("Koneksi bermasalah")
            } catch (e: Exception) {
                uiState = EditProfileUiState.Error("Gagal update: ${e.message}")
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            uiState = EditProfileUiState.Loading
            try {
                val loggedInId = userPreferences.getUserId.first()
                if (loggedInId != -1) {
                    val response = repository.deleteUser(loggedInId)
                    if (response.status) {
                        userPreferences.saveUserId(-1)
                        uiState = EditProfileUiState.DeleteSuccess
                    } else {
                        uiState = EditProfileUiState.Error(response.message ?: "Gagal")
                    }
                }
            } catch (e: Exception) {
                uiState = EditProfileUiState.Error("Terjadi kesalahan")
            }
        }
    }
}