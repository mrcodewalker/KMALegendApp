package com.example.kmalegend.ui.donate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kmalegend.ui.common.KmaTopBar
import com.example.kmalegend.ui.theme.*

@Composable
fun DonateScreen(navController: NavController) {
    Scaffold(topBar = { KmaTopBar(title = "Ủng hộ", navController = navController, showBack = true) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero
            Card(shape = RoundedCornerShape(20.dp), elevation = 0.dp, backgroundColor = KmaRedSurface) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = KmaRed, modifier = Modifier.size(52.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Ủng hộ KMA Legend", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = KmaRed)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Nếu ứng dụng hữu ích với bạn, hãy ủng hộ chúng mình để tiếp tục phát triển nhé!",
                        textAlign = TextAlign.Center, color = KmaRed.copy(alpha = 0.75f), fontSize = 13.sp, lineHeight = 20.sp
                    )
                }
            }

            // Bank info
            Card(shape = RoundedCornerShape(16.dp), elevation = 0.dp, backgroundColor = White) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = KmaRed, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Thông tin chuyển khoản", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.subtitle1)
                    }
                    Divider(color = SurfaceVariant)
                    BankRow("Ngân hàng", "MB Bank")
                    BankRow("Số tài khoản", "0123456789")
                    BankRow("Chủ tài khoản", "NGUYEN VAN A")
                    BankRow("Nội dung", "Ung ho KMA Legend")
                }
            }

            // QR placeholder
            Card(shape = RoundedCornerShape(16.dp), elevation = 0.dp, backgroundColor = White) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Quét mã QR", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.subtitle2)
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.size(160.dp).background(SurfaceVariant, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(72.dp), tint = OnSurfaceMedium)
                            Text("QR Code", style = MaterialTheme.typography.caption, color = OnSurfaceMedium)
                        }
                    }
                }
            }

            // Footer
            Text("Mr.CodeWalker × Hải Code Dạo", style = MaterialTheme.typography.caption, color = OnSurfaceMedium)
            Text("Cảm ơn bạn rất nhiều ❤️", fontWeight = FontWeight.Medium, color = KmaRed, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BankRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = OnSurfaceMedium, modifier = Modifier.width(120.dp), style = MaterialTheme.typography.caption)
        Text(value, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.body2, color = OnSurfaceHigh)
    }
}
