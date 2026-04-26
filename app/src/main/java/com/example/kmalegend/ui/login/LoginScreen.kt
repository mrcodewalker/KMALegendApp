package com.example.kmalegend.ui.login

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kmalegend.ui.navigation.Routes
import com.example.kmalegend.ui.theme.*

@Composable
fun LoginScreen(navController: NavController, vm: LoginViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) navController.navigate(Routes.HOME) {
            popUpTo(Routes.LOGIN) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(KmaRed, KmaRedDark, Color(0xFF660000))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            // Logo section
            Box(
                modifier = Modifier.size(88.dp)
                    .background(White.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = White, modifier = Modifier.size(48.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text("KMA Legend", style = MaterialTheme.typography.h5, color = White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Học viện Kỹ thuật Mật mã",
                style = MaterialTheme.typography.body2,
                color = White.copy(alpha = 0.75f)
            )

            Spacer(Modifier.height(48.dp))

            // Login card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = 0.dp,
                backgroundColor = White
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Text("Đăng nhập", style = MaterialTheme.typography.h6, color = OnSurfaceHigh)
                    Text("Nhập thông tin tài khoản KMA của bạn", style = MaterialTheme.typography.caption, color = OnSurfaceMedium)
                    Spacer(Modifier.height(24.dp))

                    // Username field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Mã sinh viên") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = if (username.isNotEmpty()) KmaRed else OnSurfaceMedium)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = KmaRed,
                            focusedLabelColor = KmaRed,
                            cursorColor = KmaRed
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(Modifier.height(16.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mật khẩu") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = if (password.isNotEmpty()) KmaRed else OnSurfaceMedium)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = OnSurfaceMedium
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = KmaRed,
                            focusedLabelColor = KmaRed,
                            cursorColor = KmaRed
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); vm.login(username, password) })
                    )

                    // Error message
                    AnimatedVisibility(visible = uiState.error != null) {
                        uiState.error?.let { err ->
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ErrorSurface)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Error, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(err, color = Error, style = MaterialTheme.typography.caption, modifier = Modifier.weight(1f))
                                IconButton(onClick = { vm.clearError() }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Error, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Login button
                    Button(
                        onClick = { focusManager.clearFocus(); vm.login(username, password) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isLoading && username.isNotBlank() && password.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = KmaRed,
                            disabledBackgroundColor = Outline
                        ),
                        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 4.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Đang đăng nhập...", color = White, fontWeight = FontWeight.SemiBold)
                        } else {
                            Text("Đăng nhập", color = White, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text(
                "Mr.CodeWalker × Hải Code Dạo",
                style = MaterialTheme.typography.caption,
                color = White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
