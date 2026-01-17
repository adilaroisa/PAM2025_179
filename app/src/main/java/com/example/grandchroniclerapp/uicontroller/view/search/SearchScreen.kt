package com.example.grandchroniclerapp.uicontroller.view.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.grandchroniclerapp.model.Article
import com.example.grandchroniclerapp.ui.theme.BlackText
import com.example.grandchroniclerapp.ui.theme.PastelBluePrimary
import com.example.grandchroniclerapp.viewmodel.provider.PenyediaViewModel
import com.example.grandchroniclerapp.viewmodel.search.SearchUiState
import com.example.grandchroniclerapp.viewmodel.search.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String? = null,
    onDetailClick: (Int) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = PenyediaViewModel.Factory)
) {
    val focusManager = LocalFocusManager.current
    val uiState = viewModel.searchUiState

    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank()) {
            viewModel.updateQuery(initialQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        // --- SEARCH BAR ---
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.updateQuery(it) },
            label = {
                if (viewModel.selectedCategory != null)
                    Text("Kategori: ${viewModel.selectedCategory!!.category_name}")
                else
                    Text("Cari Artikel Sejarah...")
            },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = PastelBluePrimary) },
            trailingIcon = {
                if (viewModel.searchQuery.isNotEmpty() || viewModel.selectedCategory != null) {
                    IconButton(onClick = {
                        viewModel.clearSearch()
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PastelBluePrimary,
                unfocusedBorderColor = Color.LightGray
            ),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        Spacer(Modifier.height(16.dp))

        // --- CONTENT ---
        when (uiState) {
            is SearchUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PastelBluePrimary)
                }
            }

            // TAMPILAN PILIH KATEGORI (AWAL)
            is SearchUiState.Idle -> {
                Text("Jelajahi Kategori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BlackText)
                Spacer(Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.categories.filter { it.category_id != 7 }) { cat ->
                        Card(
                            onClick = { viewModel.selectCategory(cat) },
                            modifier = Modifier.height(60.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (viewModel.selectedCategory == cat) PastelBluePrimary else Color(0xFFFFE0E0)
                            )
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    cat.category_name,
                                    fontWeight = FontWeight.Bold,
                                    color = if (viewModel.selectedCategory == cat) Color.White else Color(0xFF4A4A4A)
                                )
                            }
                        }
                    }
                }
            }

            // HASIL PENCARIAN
            is SearchUiState.Success -> {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    val headerText = if (viewModel.selectedCategory != null) "Kategori: ${viewModel.selectedCategory!!.category_name}" else "Hasil Pencarian:"
                    Text(text = headerText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (uiState.articles.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(50.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Tidak ada artikel ditemukan.", color = Color.Gray)
                        }
                    }
                } else {
                    // Paksa Grid mulai dari Kiri (LTR)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalItemSpacing = 16.dp,
                            contentPadding = PaddingValues(bottom = 100.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.articles, key = { it.article_id }) { article ->
                                val isPinterestStyle = article.category_id == 2

                                SearchStaggeredCard(
                                    article = article,
                                    isPinterestStyle = isPinterestStyle,
                                    searchQuery = if(viewModel.selectedCategory == null) viewModel.searchQuery else "",
                                    onClick = { onDetailClick(article.article_id) }
                                )
                            }
                        }
                    }
                }
            }

            is SearchUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.message, color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun SearchStaggeredCard(
    article: Article,
    isPinterestStyle: Boolean,
    searchQuery: String,
    onClick: () -> Unit
) {
    val thumbnailImage = article.images.firstOrNull() ?: article.image

    val fullContent = article.content ?: ""

    // --- LOGIKA KONTEN DITAMPILKAN & POTONGANNYA ---
    val (displayContent, shouldShowContent) = remember(fullContent, searchQuery) {
        if (searchQuery.isBlank()) {
            // UPDATED: Saat Kategori (query kosong), TIDAK perlu tampilkan isi konten.
            // Biar seragam dengan Home Screen yang cuma Gambar & Judul.
            "" to false
        } else {
            // Saat Search Teks: Cek apakah ada match di body
            val matchIndex = fullContent.indexOf(searchQuery, ignoreCase = true)
            if (matchIndex != -1) {
                // ADA MATCH: Potong teks biar match-nya kelihatan di awal
                val startIndex = (matchIndex - 30).coerceAtLeast(0)
                val snippet = fullContent.substring(startIndex)

                val finalSnippet = if (startIndex > 0) "...$snippet" else snippet
                finalSnippet to true
            } else {
                "" to false
            }
        }
    }

    val sizeModifier = if (isPinterestStyle) {
        Modifier.fillMaxWidth().wrapContentHeight()
    } else {
        Modifier.fillMaxWidth().wrapContentHeight()
    }

    val badgeColor = getCategoryColor(article.category_id)

    Card(
        modifier = sizeModifier.clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // --- GAMBAR ---
            if (thumbnailImage != null) {
                val imgUrl = if (thumbnailImage.startsWith("http")) thumbnailImage else "http://10.0.2.2:3000/uploads/$thumbnailImage"

                val imageModifier = if (isPinterestStyle) {
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .heightIn(min = 120.dp)
                } else {
                    Modifier.fillMaxWidth().height(140.dp)
                }

                val contentScale = if (isPinterestStyle) ContentScale.FillWidth else ContentScale.Crop

                val request = ImageRequest.Builder(LocalContext.current)
                    .data(imgUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()

                AsyncImage(
                    model = request,
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = imageModifier.background(Color(0xFFF5F5F5))
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color(0xFFEEEEEE)), contentAlignment = Alignment.Center) {
                    Text("No Image", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {

                // --- KATEGORI ---
                Surface(color = badgeColor, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        text = article.category_name ?: "Umum",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color(0xFF4A4A4A)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- JUDUL ---
                val highlightedTitle = highlightText(text = article.title, query = searchQuery, isTitle = true)
                Text(
                    text = highlightedTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BlackText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // --- ISI KONTEN  ---
                if (shouldShowContent) {
                    Spacer(modifier = Modifier.height(4.dp))

                    val highlightedContent = highlightText(text = displayContent, query = searchQuery, isTitle = false)
                    Text(
                        text = highlightedContent,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- PENULIS ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = article.author_name ?: "Sejarawan",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// --- HELPER FUNGSI HIGHLIGHT ---
@Composable
fun highlightText(text: String, query: String, isTitle: Boolean): AnnotatedString {
    return remember(text, query) {
        if (query.isBlank()) {
            buildAnnotatedString { append(text) }
        } else {
            buildAnnotatedString {
                val lowerText = text.lowercase()
                val lowerQuery = query.lowercase()
                var startIndex = 0

                while (true) {
                    val index = lowerText.indexOf(lowerQuery, startIndex)
                    if (index == -1) {
                        append(text.substring(startIndex))
                        break
                    }

                    append(text.substring(startIndex, index))

                    withStyle(SpanStyle(background = Color(0xFFFFF176), fontWeight = if (isTitle) FontWeight.ExtraBold else FontWeight.Bold)) {
                        append(text.substring(index, index + query.length))
                    }

                    startIndex = index + query.length
                }
            }
        }
    }
}

fun getCategoryColor(categoryId: Int): Color {
    return when (categoryId) {
        1 -> Color(0xFFE3F2FD)
        2 -> Color(0xFFFFF9C4)
        3 -> Color(0xFFE8F5E9)
        4 -> Color(0xFFFCE4EC)
        5 -> Color(0xFFF3E5F5)
        6 -> Color(0xFFFFF3E0)
        else -> Color(0xFFF5F5F5)
    }
}