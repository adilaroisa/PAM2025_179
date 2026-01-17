package com.example.grandchroniclerapp.viewmodel.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grandchroniclerapp.data.UserPreferences
import com.example.grandchroniclerapp.repository.ArticleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

    var fullName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var bio by mutableStateOf("")
    var role by mutableStateOf("Penulis")
    var currentPhotoUrl by mutableStateOf("")
    var selectedImageUri by mutableStateOf<Uri?>(null)

    // State Awal
    private var initialFullName = ""
    private var initialEmail = ""
    private var initialBio = ""

    init { loadCurrentUser() }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val id = userPreferences.getUserId.first()
                if (id != -1) {
                    val res = repository.getUserDetail(id)
                    if (res.status && res.data != null) {
                        fullName = res.data.full_name
                        email = res.data.email
                        bio = res.data.bio ?: ""
                        role = res.data.role ?: "Penulis"
                        currentPhotoUrl = res.data.profile_photo ?: ""

                        // Simpan Snapshot
                        initialFullName = fullName
                        initialEmail = email
                        initialBio = bio
                    }
                }
            } catch (e: Exception) { }
        }
    }

    // --- DIRTY CHECK ---
    fun hasChanges(): Boolean {
        if (fullName.trim() != initialFullName.trim()) return true
        if (email.trim() != initialEmail.trim()) return true
        if (bio.trim() != initialBio.trim()) return true
        if (password.isNotEmpty()) return true
        if (selectedImageUri != null) return true
        return false
    }

    fun submitUpdate(context: Context) {
        if (fullName.isBlank() || email.isBlank()) {
            uiState = EditProfileUiState.Error("Isi Nama & Email")
            return
        }
        viewModelScope.launch {
            uiState = EditProfileUiState.Loading
            try {
                val id = userPreferences.getUserId.first()
                if (id != -1) {
                    val res = repository.updateUser(id, fullName, email, bio, password, selectedImageUri, context)
                    if (res.status) uiState = EditProfileUiState.Success
                    else uiState = EditProfileUiState.Error(res.message ?: "Gagal")
                }
            } catch (e: Exception) {
                uiState = EditProfileUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            uiState = EditProfileUiState.Loading
            try {
                val id = userPreferences.getUserId.first()
                if (id != -1) {
                    val res = repository.deleteUser(id)
                    if (res.status) {
                        userPreferences.saveUserId(-1)
                        uiState = EditProfileUiState.DeleteSuccess
                    } else uiState = EditProfileUiState.Error(res.message ?: "Gagal")
                }
            } catch (e: Exception) { uiState = EditProfileUiState.Error("Error") }
        }
    }
}