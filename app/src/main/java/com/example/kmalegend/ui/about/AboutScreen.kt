package com.example.kmalegend.ui.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

data class SubjectItem(val name: String, val credits: Int)
data class SemesterData(val semester: String, val subjects: List<SubjectItem>)
data class ProgramData(
    val code: String,
    val name: String,
    val degree: String,
    val duration: String,
    val totalCredits: Int,
    val majors: List<String>,
    val semesters: List<SemesterData>
)

val CT_PROGRAM = ProgramData(
    code = "CT", name = "Công nghệ thông tin",
    degree = "Cử nhân và Kỹ sư", duration = "4 hoặc 5 năm", totalCredits = 176,
    majors = listOf("Kỹ thuật phần mềm nhúng và phần mềm di động"),
    semesters = listOf(
        SemesterData("HK1", listOf(SubjectItem("Giải tích 1", 3), SubjectItem("Đại số tuyến tính", 3), SubjectItem("Tin học đại cương", 2), SubjectItem("Triết học Mác – Lê nin", 3), SubjectItem("Giáo dục quốc phòng an ninh", 8), SubjectItem("Giáo dục thể chất 1", 1))),
        SemesterData("HK2", listOf(SubjectItem("Vật lý đại cương 1", 3), SubjectItem("Giải tích 2", 3), SubjectItem("Lập trình căn bản", 3), SubjectItem("Kinh tế chính trị Mác – Lênin", 2), SubjectItem("Lịch sử Đảng Cộng sản Việt Nam", 2), SubjectItem("Giáo dục thể chất 2", 1), SubjectItem("Kỹ năng mềm", 2))),
        SemesterData("HK3", listOf(SubjectItem("Vật lý đại cương 2", 3), SubjectItem("Thực hành vật lý đại cương 1 & 2", 2), SubjectItem("Tiếng Anh 1", 3), SubjectItem("Xác suất thống kê", 2), SubjectItem("Phương pháp tính", 2), SubjectItem("Mạng máy tính", 3), SubjectItem("Tư tưởng Hồ Chí Minh", 2), SubjectItem("Giáo dục thể chất 3", 1))),
        SemesterData("HK4", listOf(SubjectItem("Tiếng Anh 2", 3), SubjectItem("Toán rời rạc", 2), SubjectItem("Quản trị mạng máy tính", 2), SubjectItem("Otomat và ngôn ngữ hình thức", 2), SubjectItem("Chương trình dịch", 2), SubjectItem("Lý thuyết cơ sở dữ liệu", 2), SubjectItem("Điện tử tương tự và điện tử số", 3), SubjectItem("Giáo dục thể chất 4", 1), SubjectItem("Chủ Nghĩa xã hội Khoa học", 2))),
        SemesterData("HK5", listOf(SubjectItem("Tiếng Anh 3", 4), SubjectItem("Lập trình hướng đối tượng", 2), SubjectItem("Phát triển phần mềm ứng dụng", 2), SubjectItem("Cấu trúc dữ liệu và giải thuật", 2), SubjectItem("Lý thuyết độ phức tạp tính toán", 2), SubjectItem("Hệ quản trị cơ sở dữ liệu", 2), SubjectItem("Kỹ thuật vi xử lý", 2), SubjectItem("Cơ sở lý thuyết truyền tin", 2), SubjectItem("Giáo dục thể chất 5", 1))),
        SemesterData("HK6", listOf(SubjectItem("Tiếng Anh chuyên ngành", 4), SubjectItem("Kiến trúc máy tính", 2), SubjectItem("Nguyên lý hệ điều hành", 2), SubjectItem("Phát triển ứng dụng web", 2), SubjectItem("Công nghệ phần mềm", 2), SubjectItem("Phân tích, thiết kế hệ thống thông tin", 2), SubjectItem("Xử lý tín hiệu số", 2), SubjectItem("Kỹ thuật truyền số liệu", 2), SubjectItem("Hệ thống viễn thông", 2), SubjectItem("Hệ thống thông tin di động", 2))),
        SemesterData("HK7", listOf(SubjectItem("Thiết kế hệ thống nhúng", 3), SubjectItem("Công nghệ phần mềm nhúng", 2), SubjectItem("Hệ điều hành nhúng thời gian thực", 3), SubjectItem("Kiểm thử phần mềm nhúng", 2), SubjectItem("Cơ sở an toàn và bảo mật thông tin", 3), SubjectItem("Linux và phần mềm nguồn mở", 2), SubjectItem("Lập trình hợp ngữ", 3), SubjectItem("Quản trị dự án phần mềm", 2), SubjectItem("Thực tập cơ sở chuyên ngành", 3))),
        SemesterData("HK8", listOf(SubjectItem("Lập trình nhân Linux", 4), SubjectItem("Lập trình driver", 4), SubjectItem("Lập trình ARM cơ bản", 3), SubjectItem("Lập trình hệ thống nhúng Linux", 3), SubjectItem("Lập trình Android cơ bản", 3), SubjectItem("Phát triển phần mềm trong thẻ thông minh", 3))),
        SemesterData("HK9", listOf(SubjectItem("Lập trình ARM nâng cao", 3), SubjectItem("Thị giác máy tính trên nền nhúng", 3), SubjectItem("An toàn và bảo mật trong hệ thống nhúng", 3), SubjectItem("Tối ưu phần mềm nhúng", 3), SubjectItem("Lập trình Android nâng cao", 3), SubjectItem("Phát triển game trên Android", 3), SubjectItem("An toàn và bảo mật trong phát triển phần mềm di động", 3), SubjectItem("Tối ưu phần mềm di động", 3))),
        SemesterData("HK10", listOf(SubjectItem("Thực tập tốt nghiệp", 3), SubjectItem("Đồ án tốt nghiệp", 8)))
    )
)

val AT_PROGRAM = ProgramData(
    code = "AT", name = "An toàn thông tin",
    degree = "Cử nhân và Kỹ sư", duration = "4 hoặc 4,5 năm", totalCredits = 153,
    majors = listOf("An toàn hệ thống thông tin", "Kỹ nghệ an toàn mạng", "Công nghệ phần mềm an toàn"),
    semesters = listOf(
        SemesterData("HK1", listOf(SubjectItem("Giáo dục quốc phòng an ninh", 8), SubjectItem("Tin học đại cương", 2), SubjectItem("Triết học Mác – Lênin", 3), SubjectItem("Giải tích 1", 3), SubjectItem("Đại số tuyến tính", 3), SubjectItem("Giáo dục thể chất 1", 1), SubjectItem("Pháp luật đại cương", 2))),
        SemesterData("HK2", listOf(SubjectItem("Giải tích 2", 3), SubjectItem("Vật lý đại cương 1", 3), SubjectItem("Kinh tế chính trị Mác – Lênin", 2), SubjectItem("Chủ Nghĩa xã hội Khoa học", 2), SubjectItem("Giáo dục thể chất 2", 1), SubjectItem("Tư tưởng Hồ Chí Minh", 2), SubjectItem("Lập trình căn bản", 3), SubjectItem("Kỹ năng mềm", 2))),
        SemesterData("HK3", listOf(SubjectItem("Vật lý đại cương 2", 3), SubjectItem("Toán xác suất thống kê", 2), SubjectItem("Toán chuyên đề", 3), SubjectItem("Tiếng Anh 1", 3), SubjectItem("Cấu trúc dữ liệu và giải thuật", 2), SubjectItem("Mạng máy tính", 3), SubjectItem("Toán rời rạc", 2), SubjectItem("Phương pháp tính", 2), SubjectItem("Giáo dục thể chất 3", 1), SubjectItem("Lịch sử Đảng Cộng sản Việt Nam", 2))),
        SemesterData("HK4", listOf(SubjectItem("Tiếng Anh 2", 3), SubjectItem("Kỹ thuật truyền số liệu", 2), SubjectItem("Cơ sở lý thuyết truyền tin", 2), SubjectItem("Lý thuyết cơ sở dữ liệu", 2), SubjectItem("Hệ quản trị cơ sở dữ liệu", 2), SubjectItem("Quản trị mạng máy tính", 2), SubjectItem("Kiến trúc máy tính và hợp ngữ", 3))),
        SemesterData("HK5", listOf(SubjectItem("Tiếng Anh 3", 4), SubjectItem("Lập trình hướng đối tượng", 2), SubjectItem("Phân tích, thiết kế hệ thống thông tin", 2), SubjectItem("Nguyên lý hệ điều hành", 2), SubjectItem("Linux và phần mềm nguồn mở", 2), SubjectItem("Thuật toán trong an toàn thông tin", 2), SubjectItem("Nhập môn mật mã học", 3))),
        SemesterData("HK6", listOf(SubjectItem("Tiếng Anh chuyên ngành", 4), SubjectItem("Chuyên đề cơ sở", 2), SubjectItem("Cơ sở an toàn thông tin", 3), SubjectItem("An toàn mạng máy tính", 3), SubjectItem("Kỹ thuật lập trình", 2), SubjectItem("An toàn cơ sở dữ liệu", 2), SubjectItem("Giao thức an toàn mạng", 2))),
        SemesterData("HK7", listOf(SubjectItem("Công nghệ web an toàn", 3), SubjectItem("Quản trị an toàn hệ thống", 3), SubjectItem("An toàn mạng không dây và di động", 2), SubjectItem("Phân tích thiết kế an toàn mạng máy tính", 2), SubjectItem("Mã độc", 3), SubjectItem("Chuyên đề An toàn hệ thống thông tin", 2))),
        SemesterData("HK8", listOf(SubjectItem("Giám sát và ứng phó sự cố an toàn mạng", 2), SubjectItem("Kiểm thử và đánh giá an toàn hệ thống thông tin", 3), SubjectItem("Quản lý an toàn thông tin", 2), SubjectItem("Điều tra số", 3))),
        SemesterData("HK9", listOf(SubjectItem("Thực tập tốt nghiệp", 3), SubjectItem("Đồ án tốt nghiệp", 8)))
    )
)

val DT_PROGRAM = ProgramData(
    code = "DT", name = "Điện tử viễn thông",
    degree = "Cử nhân và Kỹ sư", duration = "4 hoặc 4,5 năm", totalCredits = 169,
    majors = listOf("Hệ thống nhúng và điều khiển tự động"),
    semesters = listOf(
        SemesterData("HK1", listOf(SubjectItem("Giáo dục quốc phòng an ninh", 8), SubjectItem("Triết học Mác – Lênin", 3), SubjectItem("Toán cao cấp 1", 4), SubjectItem("Vật lý đại cương 1", 3), SubjectItem("Tin học đại cương", 2), SubjectItem("Giáo dục thể chất 1", 1))),
        SemesterData("HK2", listOf(SubjectItem("Toán cao cấp 2", 3), SubjectItem("Vật lý đại cương 2", 3), SubjectItem("Lập trình căn bản", 3), SubjectItem("Kinh tế chính trị Mác – Lênin", 2), SubjectItem("Lịch sử Đảng Cộng sản Việt Nam", 2), SubjectItem("Giáo dục thể chất 2", 1), SubjectItem("Chủ Nghĩa xã hội Khoa học", 2))),
        SemesterData("HK3", listOf(SubjectItem("Toán cao cấp 3", 3), SubjectItem("Xác suất thống kê", 3), SubjectItem("Thực hành vật lý đại cương 1&2", 2), SubjectItem("Tiếng Anh 1", 3), SubjectItem("Tư tưởng Hồ Chí Minh", 2), SubjectItem("Công nghệ mạng máy tính", 2), SubjectItem("Kỹ thuật lập trình", 2), SubjectItem("Giáo dục thể chất 3", 1), SubjectItem("Kỹ năng mềm", 2))),
        SemesterData("HK4", listOf(SubjectItem("Tiếng Anh 2", 3), SubjectItem("Toán rời rạc", 2), SubjectItem("Tín hiệu và hệ thống", 2), SubjectItem("Kỹ thuật điện", 2), SubjectItem("Linh kiện điện tử", 3), SubjectItem("Lý thuyết mạch", 2), SubjectItem("Điện tử công suất", 2), SubjectItem("Điện tử tương tự", 3), SubjectItem("Giáo dục thể chất 4", 1))),
        SemesterData("HK5", listOf(SubjectItem("Tiếng Anh 3", 3), SubjectItem("Thông tin số", 2), SubjectItem("Kỹ thuật đo lường điện tử", 3), SubjectItem("Kỹ thuật vi xử lý", 2), SubjectItem("Điện tử tương số", 3), SubjectItem("Thiết kế mạch điện tử sử dụng máy tính", 2), SubjectItem("Thực tập cơ sở 1", 2), SubjectItem("Cơ sở điều khiển tự động", 2), SubjectItem("Giáo dục thể chất 5", 1))),
        SemesterData("HK6", listOf(SubjectItem("Tiếng Anh chuyên ngành", 3), SubjectItem("Cơ sở lý thuyết truyền tin", 2), SubjectItem("Kỹ thuật truyền số liệu", 2), SubjectItem("Hệ thống viễn thông", 2), SubjectItem("Thiết kế hệ thống số", 3), SubjectItem("Kiến trúc máy tính", 2), SubjectItem("Điện tử công nghiệp", 2), SubjectItem("Đồ án 1", 2))),
        SemesterData("HK7", listOf(SubjectItem("Thiết bị ngoại vi và kỹ thuật ghép nối", 2), SubjectItem("Xử lý tín hiệu số", 3), SubjectItem("Hệ điều hành nhúng thời gian thực", 3), SubjectItem("Mật mã lý thuyết", 2), SubjectItem("Hệ thống nhúng", 3), SubjectItem("Cơ sở thiết kế VLSI", 3), SubjectItem("Thực tập cơ sở 2", 2), SubjectItem("Đồ án 2", 2))),
        SemesterData("HK8", listOf(SubjectItem("Thiết kế hệ thống nhúng", 3), SubjectItem("Phát triển ứng dụng IoT", 3), SubjectItem("Thiết kế PLC", 3), SubjectItem("Thực tập cơ sở 3", 2), SubjectItem("Đồ án 3", 2))),
        SemesterData("HK9", listOf(SubjectItem("Thực tập tốt nghiệp", 3), SubjectItem("Đồ án tốt nghiệp", 8)))
    )
)

@Composable
fun AboutScreen(navController: NavController) {
    val programs = listOf(CT_PROGRAM, AT_PROGRAM, DT_PROGRAM)
    var selectedProgramIndex by remember { mutableStateOf(0) }
    val selectedProgram = programs[selectedProgramIndex]
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Tổng quan", "Chương trình học")

    Scaffold(
        topBar = { KmaTopBar(title = "Chương trình học", navController = navController, showBack = true) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Program selector tabs
            ScrollableTabRow(
                selectedTabIndex = selectedProgramIndex,
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = White
            ) {
                programs.forEachIndexed { index, program ->
                    Tab(
                        selected = selectedProgramIndex == index,
                        onClick = { selectedProgramIndex = index; selectedTab = 0 },
                        text = { Text(program.code, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Content tabs
            TabRow(
                selectedTabIndex = selectedTab,
                backgroundColor = MaterialTheme.colors.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 13.sp) }
                    )
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                when (selectedTab) {
                    0 -> {
                        item {
                            Card(elevation = 4.dp, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(selectedProgram.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colors.primary)
                                    Divider()
                                    InfoRow(Icons.Default.School, "Bằng cấp", selectedProgram.degree)
                                    InfoRow(Icons.Default.Schedule, "Thời gian", selectedProgram.duration)
                                    InfoRow(Icons.Default.MenuBook, "Tổng tín chỉ", "${selectedProgram.totalCredits} TC")
                                    if (selectedProgram.majors.isNotEmpty()) {
                                        Divider()
                                        Text("Chuyên ngành:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        selectedProgram.majors.forEach { major ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Circle, contentDescription = null, modifier = Modifier.size(8.dp), tint = KmaRed)
                                                Spacer(Modifier.width(8.dp))
                                                Text(major, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        items(selectedProgram.semesters.size) { semIndex ->
                            val sem = selectedProgram.semesters[semIndex]
                            SemesterCard(sem)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = KmaRed, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("$label: ", fontWeight = FontWeight.Medium, fontSize = 13.sp)
        Text(value, fontSize = 13.sp)
    }
}

@Composable
private fun SemesterCard(sem: SemesterData) {
    var expanded by remember { mutableStateOf(false) }
    val totalCredits = sem.subjects.sumOf { it.credits }
    Card(elevation = 2.dp, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(sem.semester, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.primary, fontSize = 15.sp)
                Spacer(Modifier.weight(1f))
                Text("$totalCredits TC", fontSize = 12.sp, color = OnSurfaceMedium)
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            if (expanded) {
                Divider()
                sem.subjects.forEach { subject ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(subject.name, modifier = Modifier.weight(1f), fontSize = 13.sp)
                        Text("${subject.credits} TC", fontSize = 12.sp, color = KmaRed, fontWeight = FontWeight.Medium)
                    }
                    Divider(color = Outline)
                }
            }
        }
    }
}
