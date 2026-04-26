package com.example.kmalegend.ui.qa

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kmalegend.ui.common.KmaTopBar
import com.example.kmalegend.ui.theme.*

private val QA_LIST = listOf(
    "Làm thế nào để xem lịch học?" to "Đăng nhập bằng mã sinh viên và mật khẩu, sau đó vào mục 'Lịch học'. Lịch sẽ hiển thị theo tháng với màu sắc riêng cho từng môn.",
    "Bảng điểm ảo là gì?" to "Bảng điểm ảo cho phép bạn mô phỏng và chỉnh sửa điểm số để tính CPA dự kiến. Bạn có thể thêm, xóa, sửa điểm và xem CPA thay đổi như thế nào.",
    "Lịch ảo dùng để làm gì?" to "Lịch ảo giúp bạn lên kế hoạch đăng ký học phần cho kỳ tới. Chọn các lớp học và hệ thống tự động phát hiện trùng lịch.",
    "Dữ liệu có được bảo mật không?" to "Có. Tất cả dữ liệu gửi lên server đều được mã hóa bằng RSA + AES hybrid. Mật khẩu không được lưu trữ dưới dạng plain text.",
    "Tại sao không thấy bảng điểm ảo?" to "Bảng điểm ảo là màn hình độc lập, truy cập từ menu hoặc trang chủ. Nó tự động fetch điểm bằng mã sinh viên đã đăng nhập.",
    "Làm sao để tính CPA mục tiêu?" to "Vào màn hình Tra cứu điểm, sau khi tra cứu, nhấn 'CPA mục tiêu'. Nhập tổng tín chỉ chương trình và CPA mong muốn.",
    "Học bổng được tính như thế nào?" to "Học bổng xếp hạng dựa trên GPA 4.0 trong từng khóa. Top 3 được huy chương vàng/bạc/đồng, top 10 được đánh dấu xanh.",
    "Có thể xuất lịch học sang Google Calendar không?" to "Tính năng xuất ICS đang được phát triển. Hiện tại bạn có thể xem lịch trực tiếp trong ứng dụng.",
    "Quên mật khẩu phải làm gì?" to "Mật khẩu đăng nhập là mật khẩu tài khoản KMA. Nếu quên, liên hệ phòng Đào tạo của trường để được hỗ trợ.",
    "Ứng dụng có miễn phí không?" to "Có, KMA Legend hoàn toàn miễn phí. Nếu thấy hữu ích, bạn có thể ủng hộ nhóm phát triển qua mục 'Ủng hộ'."
)

@Composable
fun QAScreen(navController: NavController) {
    Scaffold(topBar = { KmaTopBar(title = "Hỏi đáp", navController = navController, showBack = true) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Card(shape = RoundedCornerShape(16.dp), elevation = 0.dp, backgroundColor = KmaRedSurface) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = KmaRed, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Câu hỏi thường gặp", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.subtitle1, color = KmaRed)
                            Text("${QA_LIST.size} câu hỏi", style = MaterialTheme.typography.caption, color = KmaRed.copy(alpha = 0.7f))
                        }
                    }
                }
            }
            items(QA_LIST) { (q, a) -> QACard(question = q, answer = a) }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun QACard(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(14.dp), elevation = 0.dp, backgroundColor = White, modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(32.dp).background(KmaRedSurface, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = KmaRed, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(question, fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.weight(1f), color = OnSurfaceHigh)
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null, tint = OnSurfaceMedium
                )
            }
            AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column {
                    Divider(color = SurfaceVariant)
                    Row(modifier = Modifier.fillMaxWidth().background(SurfaceVariant).padding(16.dp)) {
                        Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(KmaRed, RoundedCornerShape(2.dp)))
                        Spacer(Modifier.width(12.dp))
                        Text(answer, fontSize = 13.sp, color = OnSurfaceMedium, lineHeight = 20.sp)
                    }
                }
            }
        }
    }
}
