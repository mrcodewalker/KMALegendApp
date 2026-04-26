package com.example.kmalegend.ui.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kmalegend.ui.common.*
import com.example.kmalegend.ui.theme.*

@Composable
fun FeedbackScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    Scaffold(topBar = { KmaTopBar(title = "Phản hồi", navController = navController, showBack = true) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header card
                Card(shape = RoundedCornerShape(16.dp), elevation = 0.dp, backgroundColor = KmaRedSurface) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Feedback, contentDescription = null, tint = KmaRed, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Gửi phản hồi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.subtitle1, color = KmaRed)
                            Text("Ý kiến của bạn giúp chúng mình cải thiện ứng dụng", style = MaterialTheme.typography.caption, color = KmaRed.copy(alpha = 0.7f))
                        }
                    }
                }

                // Form card
                Card(shape = RoundedCornerShape(16.dp), elevation = 0.dp, backgroundColor = White) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            label = { Text("Họ tên") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = if (name.isNotEmpty()) KmaRed else OnSurfaceMedium) },
                            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = KmaRed, cursorColor = KmaRed)
                        )
                        OutlinedTextField(
                            value = email, onValueChange = { email = it },
                            label = { Text("Email (tùy chọn)") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = if (email.isNotEmpty()) KmaRed else OnSurfaceMedium) },
                            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = KmaRed, cursorColor = KmaRed)
                        )
                        OutlinedTextField(
                            value = message, onValueChange = { message = it },
                            label = { Text("Nội dung phản hồi") },
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            maxLines = 6, shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = KmaRed, cursorColor = KmaRed)
                        )
                        Button(
                            onClick = { if (name.isNotBlank() && message.isNotBlank()) { showSuccess = true; name = ""; email = ""; message = "" } },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = KmaRed),
                            elevation = ButtonDefaults.elevation(0.dp),
                            enabled = name.isNotBlank() && message.isNotBlank()
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = White)
                            Spacer(Modifier.width(8.dp))
                            Text("Gửi phản hồi", color = White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (showSuccess) {
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    ToastMessage("Cảm ơn bạn đã gửi phản hồi!", ToastType.SUCCESS) { showSuccess = false }
                }
            }
        }
    }
}
