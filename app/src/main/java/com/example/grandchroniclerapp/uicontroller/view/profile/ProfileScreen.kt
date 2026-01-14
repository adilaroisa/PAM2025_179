package com.example.grandchroniclerapp.uicontroller.view.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.grandchroniclerapp.model.Article
import com.example.grandchroniclerapp.ui.theme.PastelBluePrimary
import com.example.grandchroniclerapp.ui.theme.PastelPinkSecondary
import com.example.grandchroniclerapp.ui.theme.SoftError
import com.example.grandchroniclerapp.viewmodel.profile.ProfileUiState
import com.example.grandchroniclerapp.viewmodel.profile.ProfileViewModel
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onEditArticle: (Int) -> Unit,
    onAddArticle: () -> Unit,
    onAboutClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    LaunchedEffect(Unit) { viewModel.getProfile() }
    val uiState by viewModel.uiState.collectAsState()
    val deleteMessage by viewModel.deleteMessage.collectAsState(initial = null)

    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var articleToDeleteId by remember { mutableStateOf<Int?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Terbit", "Draf")

    // Trigger Snackbar saat ada pesan delete
    LaunchedEffect(deleteMessage) {
        deleteMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    // --- DIALOGS ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Keluar?") }, text = { Text("Yakin ingin logout?") },
            confirmButton = { Button(onClick = { showLogoutDialog = false; viewModel.logout(); onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Keluar") } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Batal") } }
        )
    }
    if (showDeleteDialog && articleToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Artikel?") }, text = { Text("Artikel ini akan dihapus permanen.") },
            confirmButton = { Button(onClick = { viewModel.deleteArticle(articleToDeleteId!!); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SoftError)) { Text("Hapus") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") } }
        )
    }

    // --- UI UTAMA ---
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(PastelBluePrimary, Color.White)))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(80.dp))
            Surface(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), color = Color.White) {
                when (uiState) {
                    is ProfileUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is ProfileUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Button(onClick = { viewModel.getProfile() }) { Text("Coba Lagi") } }
                    is ProfileUiState.Success -> {
                        val user = (uiState as ProfileUiState.Success).user
                        val articles = (uiState as ProfileUiState.Success).articles.filter {
                            if (selectedTabIndex == 0) it.status == "Published" else it.status == "Draft"
                        }

                        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                            // --- INFO USER ---
                            item {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
                                    // FOTO
                                    Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.LightGray)) {
                                        val photoUrl = if (!user.profile_photo.isNullOrEmpty()) "http://10.0.2.2:3000/uploads/${user.profile_photo}" else null
                                        if (photoUrl != null) AsyncImage(model = photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        else Icon(Icons.Default.Person, null, modifier = Modifier.fillMaxSize().padding(20.dp), tint = Color.White)
                                    }
                                    Spacer(Modifier.height(12.dp))

                                    // NAMA
                                    Text(user.full_name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                                    // EMAIL
                                    Text(user.email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                                    Spacer(Modifier.height(8.dp))

                                    // TAG STATUS/ROLE
                                    Surface(
                                        color = PastelBluePrimary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, PastelBluePrimary.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = user.role ?: "Penulis",
                                            color = PastelBluePrimary,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }

                                    // BIO
                                    if (!user.bio.isNullOrBlank()) {
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            text = "\"${user.bio}\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontStyle = FontStyle.Italic,
                                            color = Color.DarkGray,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 32.dp)
                                        )
                                    }

                                    Spacer(Modifier.height(16.dp))
                                    OutlinedButton(onClick = onEditProfile, border = BorderStroke(1.dp, PastelBluePrimary)) { Text("Edit Profil", color = PastelBluePrimary) }
                                }
                            }

                            // TABS
                            stickyHeader {
                                TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.White) {
                                    tabs.forEachIndexed { index, title -> Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title) }) }
                                }
                            }

                            // LIST ITEMS
                            if (articles.isEmpty()) item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Belum ada artikel", color = Color.Gray) } }
                            else items(articles) { article ->
                                MyArticleItem(article, { onEditArticle(article.article_id) }, { articleToDeleteId = article.article_id; showDeleteDialog = true })
                            }
                        }
                    }
                }
            }
        }

        // HEADER ATAS
        Row(Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = onAboutClick) { Icon(Icons.Default.Info, "About", tint = Color.White) }
            Text("Profil Saya", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showLogoutDialog = true }) { Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = Color.White) }
        }

        // FAB
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
            FloatingActionButton(onClick = onAddArticle, containerColor = PastelBluePrimary, contentColor = Color.White, shape = CircleShape) { Icon(Icons.Default.Add, "Add") }
        }

        // --- SNACKBAR CUSTOM (PERBAIKAN DI SINI) ---
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isSuccess = data.visuals.message.contains("Berhasil", true) || data.visuals.message.contains("Sukses", true)
                val bgColor = if (isSuccess) Brush.horizontalGradient(listOf(PastelBluePrimary, PastelPinkSecondary)) else Brush.linearGradient(listOf(SoftError, SoftError))
                val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Close

                Box(modifier = Modifier.padding(16.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, null, tint = Color.White)
                        Spacer(Modifier.width(12.dp))
                        Text(data.visuals.message, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MyArticleItem(article: Article, onEdit: () -> Unit, onDelete: () -> Unit) {
    val rawImg = if (article.images.isNotEmpty()) article.images[0] else null
    val thumbUrl = if (rawImg != null && !rawImg.startsWith("http")) "http://10.0.2.2:3000/uploads/$rawImg" else rawImg

    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (thumbUrl != null) AsyncImage(model = thumbUrl, contentDescription = null, modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            else Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray))

            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (article.status == "Draft") "DRAF" else article.category_name ?: "Tanpa Kategori", style = MaterialTheme.typography.labelSmall, color = if (article.status == "Draft") Color(0xFFE65100) else Color.Gray)
            }
            // TOMBOL EDIT & DELETE
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = PastelBluePrimary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = SoftError) }
        }
    }
}