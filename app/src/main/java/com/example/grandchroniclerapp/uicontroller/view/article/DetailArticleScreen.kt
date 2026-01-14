package com.example.grandchroniclerapp.uicontroller.view.article

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.grandchroniclerapp.ui.theme.PastelBluePrimary
import com.example.grandchroniclerapp.viewmodel.article.DetailArticleViewModel
import com.example.grandchroniclerapp.viewmodel.article.DetailUiState
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailArticleScreen(
    navigateBack: () -> Unit,
    onTagClick: (String) -> Unit,
    viewModel: DetailArticleViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    val uiState = viewModel.detailUiState
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detail Artikel") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (uiState) {
            is DetailUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PastelBluePrimary)
                }
            }
            is DetailUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(text = uiState.message, color = Color.Red)
                }
            }
            is DetailUiState.Success -> {
                val article = uiState.article

                var showCaption by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // --- 1. HEADER GAMBAR SLIDER ---
                    if (article.images.isNotEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { article.images.size })

                        val isTokohDunia = article.category_id == 2
                        val headerHeight = if (isTokohDunia) 500.dp else 300.dp

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(headerHeight)
                                .background(Color.LightGray)
                        ) {
                            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                                val rawUrl = article.images[page]
                                val fullUrl = if (rawUrl.startsWith("http")) rawUrl else "http://10.0.2.2:3000/uploads/$rawUrl"

                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(fullUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { showCaption = !showCaption },
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // --- LOGIKA TOMBOL INFO (i) atau caption ---
                            val currentPageCaption = if (pagerState.currentPage < article.captions.size) article.captions[pagerState.currentPage] else ""

                            if (currentPageCaption.isNotBlank()) {
                                IconButton(
                                    onClick = { showCaption = !showCaption },
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                        .size(32.dp)
                                ) {
                                    val iconTint = if (showCaption) Color(0xFFFFD700) else Color.White

                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = iconTint,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            // Navigasi Slider
                            if (article.images.size > 1) {
                                if (pagerState.currentPage > 0) {
                                    IconButton(
                                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                                        modifier = Modifier.align(Alignment.CenterStart).padding(8.dp).background(Color.Black.copy(0.3f), CircleShape)
                                    ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color.White) }
                                }
                                if (pagerState.currentPage < article.images.size - 1) {
                                    IconButton(
                                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                                        modifier = Modifier.align(Alignment.CenterEnd).padding(8.dp).background(Color.Black.copy(0.3f), CircleShape)
                                    ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White) }
                                }
                                // Indikator Halaman
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp)
                                        .background(Color.Black.copy(0.6f), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("${pagerState.currentPage + 1}/${article.images.size}", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // --- CAPTION DI BAWAH FOTO ---
                        val captionToShow = if (pagerState.currentPage < article.captions.size) article.captions[pagerState.currentPage] else ""

                        // Tampilkan hanya jika ada isinya
                        if (captionToShow.isNotBlank()) {
                            AnimatedVisibility(
                                visible = showCaption,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF5F7FA)) // Abu sangat muda
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = captionToShow,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                        color = Color.Black
                                    )
                                }
                            }
                        } else {
                            // Opsional: Jika user geser ke foto tanpa caption, otomatis tutup toggle-nya agar rapi
                            LaunchedEffect(pagerState.currentPage) {
                                showCaption = false
                            }
                        }

                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color(0xFFEEEEEE)), contentAlignment = Alignment.Center) {
                            Text("Tidak ada gambar", color = Color.Gray)
                        }
                    }

                    // --- 2. KONTEN UTAMA ---
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(article.category_name ?: "Tanpa Kategori", color = PastelBluePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))

                        Text(article.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Text(article.author_name ?: "Unknown", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(Modifier.width(16.dp))
                            Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Text(article.published_at?.take(10) ?: "Draft", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(Modifier.width(16.dp))
                            Icon(Icons.Default.Visibility, null, Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(Modifier.width(4.dp))
                            Text("${article.views_count} x Dilihat", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }

                        if (!article.tags.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            val tagList = article.tags.split(" ", "\n").filter { it.isNotBlank() }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(tagList) { tag ->
                                    SuggestionChip(
                                        onClick = { onTagClick(tag) },
                                        label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = PastelBluePrimary.copy(alpha = 0.1f),
                                            labelColor = PastelBluePrimary
                                        ),
                                        border = BorderStroke(1.dp, PastelBluePrimary.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(100)
                                    )
                                }
                            }
                        }

                        Divider(Modifier.padding(vertical = 20.dp))
                        Text(article.content, style = MaterialTheme.typography.bodyLarge, lineHeight = 28.sp)
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                }
            }
        }
    }
}