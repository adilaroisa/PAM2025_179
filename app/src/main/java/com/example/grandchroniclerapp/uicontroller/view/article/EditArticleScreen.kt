package com.example.grandchroniclerapp.uicontroller.view.article

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.grandchroniclerapp.ui.theme.PastelBluePrimary
import com.example.grandchroniclerapp.ui.theme.PastelPinkSecondary
import com.example.grandchroniclerapp.ui.theme.SoftError
import com.example.grandchroniclerapp.viewmodel.article.EditArticleViewModel
import com.example.grandchroniclerapp.viewmodel.article.ExistingImageState
import com.example.grandchroniclerapp.viewmodel.article.NewImageState
import com.example.grandchroniclerapp.viewmodel.article.UploadUiState
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditArticleScreen(
    articleId: Int,
    navigateBack: () -> Unit,
    viewModel: EditArticleViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    LaunchedEffect(articleId) { viewModel.loadArticleData(articleId) }

    val uiState = viewModel.uiState
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDraftConfirmDialog by remember { mutableStateOf(false) }
    var showPublishConfirmDialog by remember { mutableStateOf(false) }
    var oldImageToDelete by remember { mutableStateOf<ExistingImageState?>(null) }
    var newImageToDelete by remember { mutableStateOf<NewImageState?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val multipleImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { viewModel.updateImages(it) }

    fun onBackAttempt() { if (viewModel.hasChanges()) showDiscardDialog = true else navigateBack() }
    BackHandler { onBackAttempt() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message -> snackbarHostState.showSnackbar(message) }
    }

    LaunchedEffect(uiState) {
        if (uiState is UploadUiState.Success) {
            delay(1000)
            navigateBack()
        }
    }

    // --- VALIDASI ---
    if (oldImageToDelete != null) {
        AlertDialog(onDismissRequest = { oldImageToDelete = null }, title = { Text("Hapus Gambar?") }, confirmButton = { Button(onClick = { viewModel.deleteOldImage(oldImageToDelete!!); oldImageToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Hapus") } }, dismissButton = { OutlinedButton(onClick = { oldImageToDelete = null }) { Text("Batal") } })
    }
    if (newImageToDelete != null) {
        AlertDialog(onDismissRequest = { newImageToDelete = null }, title = { Text("Hapus Upload?") }, confirmButton = { Button(onClick = { viewModel.removeNewImage(newImageToDelete!!); newImageToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Hapus") } }, dismissButton = { OutlinedButton(onClick = { newImageToDelete = null }) { Text("Batal") } })
    }
    if (showDiscardDialog) {
        AlertDialog(onDismissRequest = { showDiscardDialog = false }, title = { Text("Keluar?", color = SoftError) }, text = { Text("Perubahan belum disimpan.") }, confirmButton = { Button(onClick = { showDiscardDialog = false; navigateBack() }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Keluar") } }, dismissButton = { OutlinedButton(onClick = { showDiscardDialog = false }) { Text("Lanjut") } })
    }
    if (showPublishConfirmDialog) {
        AlertDialog(onDismissRequest = { showPublishConfirmDialog = false }, title = { Text("Update Artikel?") }, text = { Text("Artikel akan diperbarui sesuai perubahan terbaru.") }, confirmButton = { Button(onClick = { showPublishConfirmDialog = false; viewModel.submitUpdate(context, articleId, "Published") }) { Text("Update") } }, dismissButton = { TextButton(onClick = { showPublishConfirmDialog = false }) { Text("Batal") } })
    }
    if (showDraftConfirmDialog) {
        AlertDialog(onDismissRequest = { showDraftConfirmDialog = false }, title = { Text("Simpan Draf?") }, text = { Text("Simpan perubahan sebagai draf.") }, confirmButton = { Button(onClick = { showDraftConfirmDialog = false; viewModel.submitUpdate(context, articleId, "Draft") }) { Text("Simpan") } }, dismissButton = { TextButton(onClick = { showDraftConfirmDialog = false }) { Text("Batal") } })
    }

    // MAIN UI
    Box(modifier = Modifier.fillMaxSize().background(PastelBluePrimary)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(80.dp))
            Surface(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), color = Color.White) {
                Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(scrollState)) {

                    // HEADER BAGIAN FOTO
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Kelola Foto", fontWeight = FontWeight.Bold, color = PastelBluePrimary, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { multipleImagePicker.launch("image/*") }) {
                            Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Tambah Baru")
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    // 1. GAMBAR LAMA
                    if (viewModel.oldImages.isNotEmpty()) {
                        Text("Foto Saat Ini:", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(viewModel.oldImages.size) { index ->
                                val item = viewModel.oldImages[index]
                                val finalUrl = if (item.url.startsWith("http")) item.url else "http://10.0.2.2:3000/uploads/${item.url}"

                                Card(
                                    modifier = Modifier.width(200.dp).wrapContentHeight(),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column {
                                        Box(modifier = Modifier.height(130.dp).fillMaxWidth()) {
                                            AsyncImage(model = finalUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                            IconButton(
                                                onClick = { oldImageToDelete = item },
                                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.White.copy(0.7f), CircleShape).size(28.dp)
                                            ) { Icon(Icons.Default.Delete, null, tint = SoftError, modifier = Modifier.size(16.dp)) }
                                        }
                                        OutlinedTextField(
                                            value = item.caption,
                                            onValueChange = { viewModel.updateOldCaption(index, it) },
                                            placeholder = { Text("Caption...") },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                            minLines = 2, maxLines = 3,
                                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = PastelBluePrimary.copy(0.5f), unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    } else {
                        Text("- Tidak ada foto lama -", fontSize = 12.sp, color = Color.LightGray, modifier = Modifier.padding(bottom = 16.dp))
                    }

                    // 2. GAMBAR BARU
                    if (viewModel.newImages.isNotEmpty()) {
                        Text("Upload Baru:", fontSize = 12.sp, color = PastelBluePrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(viewModel.newImages.size) { index ->
                                val item = viewModel.newImages[index]
                                Card(
                                    modifier = Modifier.width(200.dp).wrapContentHeight(),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)) // Sedikit biru biar beda
                                ) {
                                    Column {
                                        Box(modifier = Modifier.height(130.dp).fillMaxWidth()) {
                                            AsyncImage(model = item.uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                            IconButton(
                                                onClick = { newImageToDelete = item },
                                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.White.copy(0.7f), CircleShape).size(28.dp)
                                            ) { Icon(Icons.Default.Close, null, tint = SoftError, modifier = Modifier.size(16.dp)) }
                                        }
                                        OutlinedTextField(
                                            value = item.caption,
                                            onValueChange = { viewModel.updateNewCaption(index, it) },
                                            placeholder = { Text("Caption baru...") },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                            minLines = 2, maxLines = 3,
                                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = PastelBluePrimary.copy(0.5f), unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    // FORM INPUT DATA
                    OutlinedTextField(
                        value = viewModel.title, onValueChange = { viewModel.updateTitle(it) },
                        label = { Text("Judul") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PastelBluePrimary, unfocusedContainerColor = Color(0xFFFAFAFA)),
                        isError = viewModel.title.isBlank() && uiState is UploadUiState.Error
                    )
                    Spacer(Modifier.height(16.dp))

                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = viewModel.selectedCategory?.category_name ?: "Pilih Kategori",
                            onValueChange = {}, readOnly = true, label = { Text("Kategori") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PastelBluePrimary, unfocusedContainerColor = Color(0xFFFAFAFA)),
                            isError = viewModel.selectedCategory == null && uiState is UploadUiState.Error
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                            viewModel.categories.forEach { category -> DropdownMenuItem(text = { Text(category.category_name) }, onClick = { viewModel.updateCategory(category); expanded = false }) }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.tags, onValueChange = { viewModel.updateTags(it) },
                        label = { Text("Tags") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PastelBluePrimary, unfocusedContainerColor = Color(0xFFFAFAFA))
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.content, onValueChange = { viewModel.updateContent(it) },
                        label = { Text("Isi Artikel") }, modifier = Modifier.fillMaxWidth().height(300.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PastelBluePrimary, unfocusedContainerColor = Color(0xFFFAFAFA)),
                        isError = viewModel.content.isBlank() && uiState is UploadUiState.Error
                    )
                    Spacer(Modifier.height(24.dp))

                    // TOMBOL AKSI
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { if (viewModel.title.isNotBlank()) showDraftConfirmDialog = true else viewModel.submitUpdate(context, articleId, "Draft") },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PastelBluePrimary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelBluePrimary),
                            enabled = uiState !is UploadUiState.Loading
                        ) { Text("Simpan Draf") }

                        Button(
                            onClick = { if (viewModel.title.isNotBlank() && viewModel.content.isNotBlank() && viewModel.selectedCategory != null) showPublishConfirmDialog = true else viewModel.submitUpdate(context, articleId, "Published") },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PastelBluePrimary),
                            enabled = uiState !is UploadUiState.Loading
                        ) { if (uiState is UploadUiState.Loading) CircularProgressIndicator(color = Color.White) else Text("Update") }
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }

        // HEADER & SNACKBAR
        Row(Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onBackAttempt() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Text("Edit Artikel", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isSuccess = data.visuals.message.contains("Berhasil", true) || data.visuals.message.contains("Update", true)
                val bgColor = if (isSuccess) Brush.horizontalGradient(listOf(PastelBluePrimary, PastelPinkSecondary)) else Brush.linearGradient(listOf(SoftError, SoftError))
                Box(modifier = Modifier.padding(16.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Close, null, tint = Color.White)
                        Spacer(Modifier.width(12.dp))
                        Text(data.visuals.message, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}