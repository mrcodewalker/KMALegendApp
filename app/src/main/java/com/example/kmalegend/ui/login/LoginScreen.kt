package com.example.kmalegend.ui.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kmalegend.ui.navigation.Routes
import com.example.kmalegend.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

// ─── Floating bubble data ────────────────────────────────────────────────────
private data class Bubble(val x: Float, val y: Float, val size: Float, val speed: Float, val alpha: Float)

@Composable
private fun FloatingBubbles(modifier: Modifier = Modifier) {
    val bubbles = remember {
        listOf(
            Bubble(0.08f, 0.9f, 14f, 0.6f, 0.25f),
            Bubble(0.18f, 0.75f, 22f, 0.45f, 0.18f),
            Bubble(0.35f, 0.85f, 10f, 0.8f, 0.3f),
            Bubble(0.55f, 0.92f, 18f, 0.55f, 0.2f),
            Bubble(0.70f, 0.78f, 28f, 0.38f, 0.15f),
            Bubble(0.85f, 0.88f, 12f, 0.7f, 0.28f),
            Bubble(0.92f, 0.65f, 20f, 0.5f, 0.22f),
            Bubble(0.25f, 0.6f, 8f, 0.9f, 0.2f),
            Bubble(0.62f, 0.55f, 16f, 0.65f, 0.15f),
        )
    }

    val infiniteTransition = rememberInfiniteTransition()
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(8000, easing = LinearEasing))
    )

    Box(modifier = modifier) {
        bubbles.forEachIndexed { i, bubble ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -1f,
                animationSpec = infiniteRepeatable(
                    animation = tween((4000 / bubble.speed).toInt(), easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
            val wobble = sin((time * 2 * Math.PI + i * 1.2f).toFloat()) * 0.02f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val cx = (bubble.x + wobble) * size.width
                        val cy = size.height * (1f + offsetY * bubble.speed * 0.4f)
                        drawCircle(
                            color = Color.White.copy(alpha = bubble.alpha),
                            radius = bubble.size.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                    }
            )
        }
    }
}

// ─── Shimmer overlay ─────────────────────────────────────────────────────────
@Composable
private fun ShimmerLogo(size: Dp = 88.dp) {
    val shimmer = rememberInfiniteTransition()
    val shimmerX by shimmer.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing))
    )

    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                // Base translucent circle
                drawCircle(color = Color.White.copy(alpha = 0.18f))
                // Shimmer sweep
                val gradient = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    start = Offset(shimmerX * this.size.width - this.size.width, 0f),
                    end = Offset(shimmerX * this.size.width, this.size.height)
                )
                drawCircle(brush = gradient)
            }
            .clip(RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.School,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
    }
}

// ─── Animated gradient background ───────────────────────────────────────────
@Composable
private fun AnimatedGradientBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val topColor = lerp(KmaRed, Color(0xFFDD1111), shift)
    val midColor = lerp(KmaRedDark, Color(0xFF880000), shift)
    val botColor = lerp(Color(0xFF660000), Color(0xFF440000), shift)

    Box(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(topColor, midColor, botColor))
        )
    )
}

private fun lerp(a: Color, b: Color, t: Float) = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = 1f
)

// ─── Main Login Screen ───────────────────────────────────────────────────────
@Composable
fun LoginScreen(navController: NavController, vm: LoginViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // ── Entrance animation states ───────────────────────────────────────────
    val logoScale = remember { Animatable(0.3f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleSlide = remember { Animatable(30f) }
    val titleAlpha = remember { Animatable(0f) }
    val cardSlide = remember { Animatable(80f) }
    val cardAlpha = remember { Animatable(0f) }
    val footerAlpha = remember { Animatable(0f) }

    // ── Button press animation ─────────────────────────────────────────────
    val buttonScale = remember { Animatable(1f) }

    // ── Success animation ──────────────────────────────────────────────────
    var showSuccess by remember { mutableStateOf(false) }
    val successAlpha = remember { Animatable(0f) }
    val successScale = remember { Animatable(0.5f) }

    // ── Staggered entrance ─────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        delay(100)
        // Logo pops in with spring bounce
        launch {
            logoAlpha.animateTo(1f, tween(400))
            logoScale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
            )
        }
        delay(200)
        // Title slides up
        launch {
            titleAlpha.animateTo(1f, tween(500))
            titleSlide.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
        delay(350)
        // Card slides in from bottom
        launch {
            cardAlpha.animateTo(1f, tween(600))
            cardSlide.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
        }
        delay(800)
        // Footer fades in
        footerAlpha.animateTo(1f, tween(600))
    }

    // ── Navigate on success ────────────────────────────────────────────────
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            showSuccess = true
            launch {
                successAlpha.animateTo(1f, tween(300))
                successScale.animateTo(1.2f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                successScale.animateTo(1f, tween(200))
            }
            delay(900)
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.LOGIN) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated gradient bg
        AnimatedGradientBackground(modifier = Modifier.fillMaxSize())

        // Floating bubbles layer
        FloatingBubbles(modifier = Modifier.fillMaxSize())

        // Radial glow in top center
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2f, 0f),
                            radius = size.width * 0.7f
                        ),
                        radius = size.width * 0.7f,
                        center = Offset(size.width / 2f, 0f)
                    )
                }
        )

        // Decorative circles (bottom right)
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.06f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // ── Logo with shimmer & bounce ────────────────────────────────
            Box(
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = logoScale.value,
                        scaleY = logoScale.value,
                        alpha = logoAlpha.value
                    )
            ) {
                ShimmerLogo(size = 90.dp)
            }

            Spacer(Modifier.height(20.dp))

            // ── Title slides up ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .graphicsLayer(
                        translationY = titleSlide.value,
                        alpha = titleAlpha.value
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "KMA Legend",
                    style = MaterialTheme.typography.h5,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                // Glowing subtitle line
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color.White.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Học viện Kỹ thuật Mật mã",
                    style = MaterialTheme.typography.body2,
                    color = Color.White.copy(alpha = 0.78f),
                    letterSpacing = 0.3.sp
                )
            }

            Spacer(Modifier.height(44.dp))

            // ── Login card slides from bottom ──────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        translationY = cardSlide.value,
                        alpha = cardAlpha.value
                    ),
                shape = RoundedCornerShape(28.dp),
                elevation = 24.dp,
                backgroundColor = Color.White
            ) {
                Column(modifier = Modifier.padding(28.dp)) {

                    // Card header with gradient underline
                    Text(
                        "Đăng nhập",
                        style = MaterialTheme.typography.h6,
                        color = OnSurfaceHigh,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(3.dp)
                            .background(
                                Brush.horizontalGradient(listOf(KmaRed, KmaRedLight)),
                                RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Nhập thông tin tài khoản KMA của bạn",
                        style = MaterialTheme.typography.caption,
                        color = OnSurfaceMedium
                    )
                    Spacer(Modifier.height(24.dp))

                    // ── Username field ─────────────────────────────────────
                    val usernameFocused = username.isNotEmpty()
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Mã sinh viên") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (usernameFocused) KmaRed else OnSurfaceMedium,
                                modifier = Modifier
                                    .scale(if (usernameFocused) 1.1f else 1f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = KmaRed,
                            focusedLabelColor = KmaRed,
                            cursorColor = KmaRed,
                            unfocusedBorderColor = Outline
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Password field ─────────────────────────────────────
                    val passwordFocused = password.isNotEmpty()
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mật khẩu") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (passwordFocused) KmaRed else OnSurfaceMedium,
                                modifier = Modifier
                                    .scale(if (passwordFocused) 1.1f else 1f)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
@OptIn(ExperimentalAnimationApi::class)
                                AnimatedContent(
                                    targetState = passwordVisible,
                                    transitionSpec = {
                                        (scaleIn(initialScale = 0.6f) + fadeIn()) with
                                                (scaleOut(targetScale = 0.6f) + fadeOut())
                                    }
                                ) { visible ->
                                    Icon(
                                        if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = OnSurfaceMedium
                                    )
                                }
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = KmaRed,
                            focusedLabelColor = KmaRed,
                            cursorColor = KmaRed,
                            unfocusedBorderColor = Outline
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            vm.login(username, password)
                        })
                    )

                    // ── Error with animated slide + shake ─────────────────
                    AnimatedVisibility(
                        visible = uiState.error != null,
                        enter = slideInVertically(tween(350)) { it } + fadeIn(tween(350)),
                        exit = slideOutVertically(tween(300)) { it } + fadeOut(tween(300))
                    ) {
                        uiState.error?.let { err ->
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ErrorSurface)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Error.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = Error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    err,
                                    color = Error,
                                    style = MaterialTheme.typography.caption,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { vm.clearError() },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Error,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── Login button with scale press effect ───────────────
                    val enabled = !uiState.isLoading && username.isNotBlank() && password.isNotBlank()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(buttonScale.value)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    // Press micro-animation
                                    buttonScale.animateTo(0.95f, tween(80))
                                    buttonScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                    focusManager.clearFocus()
                                    vm.login(username, password)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = enabled,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Transparent,
                                disabledBackgroundColor = Outline
                            ),
                            contentPadding = PaddingValues(0.dp),
                            elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (enabled)
                                            Brush.horizontalGradient(listOf(KmaRed, Color(0xFFFF3333)))
                                        else
                                            Brush.horizontalGradient(listOf(Outline, Outline)),
                                        RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
@OptIn(ExperimentalAnimationApi::class)
                                AnimatedContent(
                                    targetState = uiState.isLoading,
                                    transitionSpec = {
                                        (fadeIn(tween(200)) + scaleIn(initialScale = 0.85f)) with
                                                (fadeOut(tween(200)) + scaleOut(targetScale = 0.85f))
                                    }
                                ) { isLoading ->
                                    if (isLoading) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.5.dp
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                "Đang đăng nhập...",
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 15.sp
                                            )
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Login,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "Đăng nhập",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // ── Divider + hint ─────────────────────────────────────
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Outline))
                        Text(
                            "  Tài khoản portal KMA  ",
                            style = MaterialTheme.typography.caption,
                            color = OnSurfaceMedium
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Outline))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Footer ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier.alpha(footerAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(4.dp).background(Color.White.copy(alpha = 0.3f), CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Mr.CodeWalker × Hải Code Dạo",
                        style = MaterialTheme.typography.caption,
                        color = Color.White.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.size(4.dp).background(Color.White.copy(alpha = 0.3f), CircleShape))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "v1.0 • KMA Legend",
                    style = MaterialTheme.typography.overline,
                    color = Color.White.copy(alpha = 0.35f),
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(32.dp))
        }

        // ── Success overlay ────────────────────────────────────────────────
        AnimatedVisibility(
            visible = showSuccess,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(successScale.value)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }
    }
}
