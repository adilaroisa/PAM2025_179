package com.example.grandchroniclerapp.uicontroller.view.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(
    navigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Syarat & Ketentuan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Selamat Datang di The Grand Chronicler",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Terakhir Diperbarui: 14 Januari 2026", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(16.dp))

            TermsSection(
                title = "1. Penerimaan Syarat",
                content = "Dengan mendaftar dan menggunakan aplikasi The Grand Chronicler, Anda setuju untuk mematuhi syarat dan ketentuan ini sepenuhnya."
            )

            TermsSection(
                title = "2. Kewajiban Pengguna",
                content = "Sebagai pengguna platform ini, Anda setuju untuk:\n" +
                        "• Tidak mengunggah konten yang melanggar hak cipta (Copyright) milik orang lain.\n" +
                        "• Tidak mempublikasikan konten yang mengandung unsur SARA (Suku, Agama, Ras, dan Antargolongan), ujaran kebencian, atau konten ilegal lainnya.\n" +
                        "• Bertanggung jawab penuh atas keaslian dan validitas artikel sejarah yang Anda tulis."
            )

            TermsSection(
                title = "3. Hak Kekayaan Intelektual",
                content = "Konten yang Anda unggah tetap menjadi milik Anda, namun Anda memberikan lisensi kepada The Grand Chronicler untuk menampilkan dan mendistribusikan konten tersebut di dalam platform ini."
            )

            TermsSection(
                title = "4. Penghentian Akun",
                content = "Kami berhak menghapus konten atau menangguhkan akun Anda tanpa pemberitahuan sebelumnya jika ditemukan pelanggaran terhadap syarat ketentuan ini, terutama terkait plagiarisme dan konten SARA."
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun TermsSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = content, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
    }
}