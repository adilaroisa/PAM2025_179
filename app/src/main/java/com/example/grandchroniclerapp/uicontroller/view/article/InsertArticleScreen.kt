package com.example.grandchroniclerapp.uicontroller.view.article

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import coil.compose.rememberAsyncImagePainter
import com.example.grandchroniclerapp.ui.theme.PastelBluePrimary
import com.example.grandchroniclerapp.ui.theme.PastelPinkSecondary
import com.example.grandchroniclerapp.ui.theme.SoftError
import com.example.grandchroniclerapp.viewmodel.article.ImageUploadState
import com.example.grandchroniclerapp.viewmodel.article.InsertViewModel
import com.example.grandchroniclerapp.viewmodel.article.UploadUiState
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsertArticleScreen(
    navigateBack: () -> Unit,
    viewModel: InsertViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDraftConfirmDialog by remember { mutableStateOf(false) }
    var showPublishConfirmDialog by remember { mutableStateOf(false) }
    var imageToDelete by remember { mutableStateOf<ImageUploadState?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val multipleImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        viewModel.addImages(uris)
    }

    fun onBackAttempt() {
        if (viewModel.hasUnsavedChanges()) showDiscardDialog = true else navigateBack()
    }
    BackHandler { onBackAttempt() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is UploadUiState.Success) {
            delay(1500)
            viewModel.resetState()
            navigateBack()
        }
    }

    // --- DIALOGS ---
    if (imageToDelete != null) {
        AlertDialog(
            onDismissRequest = { imageToDelete = null },
            title = { Text("Hapus Gambar?", color = SoftError) },
            text = { Text("Gambar ini akan dihapus dari daftar upload.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.removeImage(imageToDelete!!); imageToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftError)
                ) { Text("Hapus") }
            },
            dismissButton = {
                OutlinedButton(onClick = { imageToDelete = null }) { Text("Batal") }
            }
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Keluar?", color = SoftError) },
            text = { Text("Tulisan belum disimpan.") },
            confirmButton = {
                Button(
                    onClick = { showDiscardDialog = false; navigateBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftError)
                ) { Text("Keluar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDiscardDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showDraftConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDraftConfirmDialog = false },
            title = { Text("Simpan Draf?") },
            text = { Text("Simpan perubahan sebagai draf.") },
            confirmButton = {
                Button(onClick = { showDraftConfirmDialog = false; viewModel.submitArticle(context, "Draft") }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showDraftConfirmDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showPublishConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPublishConfirmDialog = false },
            title = { Text("Terbitkan?") },
            text = { Text("Artikel dapat diakses oleh publik.") },
            confirmButton = {
                Button(onClick = { showPublishConfirmDialog = false; viewModel.submitArticle(context, "Published") }) { Text("Terbit") }
            },
            dismissButton = {
                TextButton(onClick = { showPublishConfirmDialog = false }) { Text("Batal") }
            }
        )
    }

    // MAIN LAYOUT
    Box(modifier = Modifier.fillMaxSize().background(PastelBluePrimary)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(80.dp))

            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    // --- HEADER FOTO ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Foto Artikel", fontWeight = FontWeight.Bold, color = PastelBluePrimary, style = MaterialTheme.typography.titleMedium)

                        // TOMBOL TAMBAH GAMBAR
                        TextButton(onClick = { multipleImagePicker.launch("image/*") }) {
                            Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Tambah")
                        }
                    }
                    Text("Tambahkan foto pendukung dan berikan keterangan.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(Modifier.height(12.dp))

                    // LIST FOTO YANG DIPILIH
                    if (viewModel.imageList.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)
                        ) {
                            itemsIndexed(viewModel.imageList) { index, item ->
                                Card(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .wrapContentHeight(),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column {
                                        // AREA GAMBAR
                                        Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
                                            Image(
                                                painter = rememberAsyncImagePainter(item.uri),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )

                                            // Tombol Hapus
                                            IconButton(
                                                onClick = { imageToDelete = item },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(28.dp)
                                                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
                                            ) {
                                                Icon(Icons.Default.Delete, null, tint = SoftError, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        // AREA CAPTION
                                        OutlinedTextField(
                                            value = item.caption,
                                            onValueChange = { viewModel.updateCaption(index, it) },
                                            placeholder = { Text("Tulis keterangan foto...", fontSize = 12.sp, color = Color.Gray) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 4.dp, vertical = 8.dp),
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                            minLines = 2,
                                            maxLines = 3,
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                                                focusedBorderColor = PastelBluePrimary.copy(alpha = 0.5f),
                                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                                focusedContainerColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // INPUT JUDUL
                    OutlinedTextField(
                        value = viewModel.title, onValueChange = { viewModel.updateTitle(it) },
                        label = { Text("Judul Artikel") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PastelBluePrimary, unfocusedContainerColor = Color(0xFFFAFAFA)),
                        isError = viewModel.title.isBlank() && uiState is UploadUiState.Error
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // KATEGORI
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = viewModel.selectedCategory?.category_name ?: "Pilih Kategori",
                            onValueChange = {}, readOnly = true, label = { Text("Kategori") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PastelBluePrimary, unfocusedContainerColor = Color(0xFFFAFAFA)),
                            isError = viewModel.selectedCategory == null && uiState is UploadUiState.Error
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                            viewModel.categories.forEach { category -> DropdownMenuItem(text = { Text(category.category_name) }, onClick = { viewModel.updateCategory(category); expanded = false }) }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // INPUT TAGS
                    OutlinedTextField(
                        value = viewModel.tags, onValueChange = { viewModel.updateTags(it) },
                        label = { Text("Tags / Hashtag (Opsional)") }, placeholder = { Text("#Sejarah #Budaya") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PastelBluePrimary, unfocusedContainerColor = Color(0xFFFAFAFA))
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // ISI ARTIKEL
                    OutlinedTextField(
                        value = viewModel.content, onValueChange = { viewModel.updateContent(it) },
                        label = { Text("Tulis kisah sejarah di sini...") },
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PastelBluePrimary, unfocusedContainerColor = Color(0xFFFAFAFA)),
                        isError = viewModel.content.isBlank() && uiState is UploadUiState.Error
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // TOMBOL AKSI
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { if (viewModel.title.isNotBlank()) showDraftConfirmDialog = true else viewModel.submitArticle(context, "Draft") },
                            modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PastelBluePrimary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PastelBluePrimary),
                            enabled = uiState !is UploadUiState.Loading
                        ) { Text("Simpan Draf") }

                        Button(
                            onClick = { if (viewModel.title.isNotBlank() && viewModel.selectedCategory != null && viewModel.content.isNotBlank()) showPublishConfirmDialog = true else viewModel.submitArticle(context, "Published") },
                            modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PastelBluePrimary),
                            enabled = uiState !is UploadUiState.Loading
                        ) { if(uiState is UploadUiState.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Terbit") }
                    }
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }

        // HEADER
        Row(modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onBackAttempt() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Text("Tulis Artikel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(start = 16.dp))
        }

        // SNACKBAR
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isSuccess = data.visuals.message.contains("Berhasil", true) || data.visuals.message.contains("Disimpan", true) || data.visuals.message.contains("Terbit", true)
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