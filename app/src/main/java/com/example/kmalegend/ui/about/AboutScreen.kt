package com.example.kmalegend.ui.about

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kmalegend.ui.common.KmaTopBar
import com.example.kmalegend.ui.theme.*

// ─── Data models ──────────────────────────────────────────────────────────────
data class SubjectItem(val name: String, val credits: Int, val type: SubjectType = SubjectType.CORE)
enum class SubjectType { GENERAL, CORE, SPECIALIZED, THESIS }

data class SemesterData(val semester: String, val subjects: List<SubjectItem>)

data class CareerPath(val title: String, val icon: ImageVector, val desc: String)
data class SkillTag(val label: String, val color: Color)

data class ProgramData(
    val code: String,
    val name: String,
    val shortDesc: String,
    val degree: String,
    val duration: String,
    val totalCredits: Int,
    val accentColor: Color,
    val accentLight: Color,
    val icon: ImageVector,
    val highlights: List<Pair<String, String>>,   // label to value
    val overview: String,
    val majors: List<String>,
    val careers: List<CareerPath>,
    val skills: List<SkillTag>,
    val semesters: List<SemesterData>
)

// ─── Program data ─────────────────────────────────────────────────────────────
val CT_PROGRAM = ProgramData(
    code = "CT", name = "Công nghệ thông tin",
    shortDesc = "Kỹ thuật phần mềm nhúng & di động",
    degree = "Cử nhân / Kỹ sư", duration = "4 – 5 năm", totalCredits = 176,
    accentColor = Color(0xFF1565C0), accentLight = Color(0xFFE8F0FE),
    icon = Icons.Default.Computer,
    highlights = listOf("176 TC" to "Tổng tín chỉ", "10 HK" to "Học kỳ", "4–5 năm" to "Thời gian", "2 bằng" to "Cử nhân/KS"),
    overview = "Ngành Công nghệ thông tin tại KMA đào tạo kỹ sư phần mềm chuyên sâu về hệ thống nhúng và phần mềm di động — hai lĩnh vực cốt lõi của cuộc cách mạng công nghiệp 4.0. Sinh viên được trang bị nền tảng toán học vững chắc, kỹ năng lập trình đa nền tảng (Linux, Android, ARM), thiết kế hệ thống nhúng thời gian thực và phát triển ứng dụng di động hiện đại. Chương trình kết hợp lý thuyết chuyên sâu với thực hành dự án thực tế, giúp sinh viên sẵn sàng làm việc tại các tập đoàn công nghệ lớn trong và ngoài nước.",
    majors = listOf("Kỹ thuật phần mềm nhúng", "Phần mềm di động"),
    careers = listOf(
        CareerPath("Kỹ sư nhúng", Icons.Default.Memory, "Phát triển firmware, driver, RTOS cho thiết bị IoT, ô tô, y tế"),
        CareerPath("Lập trình Android", Icons.Default.PhoneAndroid, "Xây dựng ứng dụng di động native và cross-platform"),
        CareerPath("Kỹ sư ARM/Linux", Icons.Default.DeveloperBoard, "Tối ưu hệ thống Linux nhúng trên nền tảng ARM"),
        CareerPath("Kỹ sư IoT", Icons.Default.Wifi, "Thiết kế hệ thống kết nối vạn vật thông minh"),
        CareerPath("Nghiên cứu & Giảng dạy", Icons.Default.School, "Học sau đại học, nghiên cứu AI/ML trên nền nhúng")
    ),
    skills = listOf(
        SkillTag("C/C++", Color(0xFF1565C0)), SkillTag("Linux Kernel", Color(0xFF2E7D32)),
        SkillTag("Android", Color(0xFF00695C)), SkillTag("ARM Assembly", Color(0xFF6A1B9A)),
        SkillTag("RTOS", Color(0xFFE65100)), SkillTag("IoT", Color(0xFF0277BD)),
        SkillTag("Python", Color(0xFF1565C0)), SkillTag("Git", Color(0xFFC62828)),
        SkillTag("Docker", Color(0xFF0277BD)), SkillTag("SQL", Color(0xFF558B2F))
    ),
    semesters = listOf(
        SemesterData("HK1", listOf(SubjectItem("Giải tích 1", 3, SubjectType.GENERAL), SubjectItem("Đại số tuyến tính", 3, SubjectType.GENERAL), SubjectItem("Tin học đại cương", 2, SubjectType.GENERAL), SubjectItem("Triết học Mác – Lê nin", 3, SubjectType.GENERAL), SubjectItem("Giáo dục quốc phòng an ninh", 8, SubjectType.GENERAL), SubjectItem("Giáo dục thể chất 1", 1, SubjectType.GENERAL))),
        SemesterData("HK2", listOf(SubjectItem("Vật lý đại cương 1", 3, SubjectType.GENERAL), SubjectItem("Giải tích 2", 3, SubjectType.GENERAL), SubjectItem("Lập trình căn bản", 3, SubjectType.CORE), SubjectItem("Kinh tế chính trị Mác – Lênin", 2, SubjectType.GENERAL), SubjectItem("Lịch sử Đảng CSVN", 2, SubjectType.GENERAL), SubjectItem("Giáo dục thể chất 2", 1, SubjectType.GENERAL), SubjectItem("Kỹ năng mềm", 2, SubjectType.GENERAL))),
        SemesterData("HK3", listOf(SubjectItem("Vật lý đại cương 2", 3, SubjectType.GENERAL), SubjectItem("Thực hành vật lý", 2, SubjectType.GENERAL), SubjectItem("Tiếng Anh 1", 3, SubjectType.GENERAL), SubjectItem("Xác suất thống kê", 2, SubjectType.CORE), SubjectItem("Phương pháp tính", 2, SubjectType.CORE), SubjectItem("Mạng máy tính", 3, SubjectType.CORE), SubjectItem("Tư tưởng Hồ Chí Minh", 2, SubjectType.GENERAL), SubjectItem("Giáo dục thể chất 3", 1, SubjectType.GENERAL))),
        SemesterData("HK4", listOf(SubjectItem("Tiếng Anh 2", 3, SubjectType.GENERAL), SubjectItem("Toán rời rạc", 2, SubjectType.CORE), SubjectItem("Quản trị mạng", 2, SubjectType.CORE), SubjectItem("Otomat & ngôn ngữ hình thức", 2, SubjectType.CORE), SubjectItem("Chương trình dịch", 2, SubjectType.CORE), SubjectItem("Lý thuyết CSDL", 2, SubjectType.CORE), SubjectItem("Điện tử tương tự & số", 3, SubjectType.CORE), SubjectItem("Giáo dục thể chất 4", 1, SubjectType.GENERAL), SubjectItem("CNXHKH", 2, SubjectType.GENERAL))),
        SemesterData("HK5", listOf(SubjectItem("Tiếng Anh 3", 4, SubjectType.GENERAL), SubjectItem("Lập trình hướng đối tượng", 2, SubjectType.CORE), SubjectItem("Phát triển phần mềm ứng dụng", 2, SubjectType.CORE), SubjectItem("Cấu trúc dữ liệu & giải thuật", 2, SubjectType.CORE), SubjectItem("Lý thuyết độ phức tạp", 2, SubjectType.CORE), SubjectItem("Hệ quản trị CSDL", 2, SubjectType.CORE), SubjectItem("Kỹ thuật vi xử lý", 2, SubjectType.CORE), SubjectItem("Cơ sở lý thuyết truyền tin", 2, SubjectType.CORE), SubjectItem("Giáo dục thể chất 5", 1, SubjectType.GENERAL))),
        SemesterData("HK6", listOf(SubjectItem("Tiếng Anh chuyên ngành", 4, SubjectType.GENERAL), SubjectItem("Kiến trúc máy tính", 2, SubjectType.CORE), SubjectItem("Nguyên lý hệ điều hành", 2, SubjectType.CORE), SubjectItem("Phát triển ứng dụng web", 2, SubjectType.CORE), SubjectItem("Công nghệ phần mềm", 2, SubjectType.CORE), SubjectItem("Phân tích thiết kế HTTT", 2, SubjectType.CORE), SubjectItem("Xử lý tín hiệu số", 2, SubjectType.CORE), SubjectItem("Kỹ thuật truyền số liệu", 2, SubjectType.CORE), SubjectItem("Hệ thống viễn thông", 2, SubjectType.CORE), SubjectItem("Hệ thống thông tin di động", 2, SubjectType.CORE))),
        SemesterData("HK7", listOf(SubjectItem("Thiết kế hệ thống nhúng", 3, SubjectType.SPECIALIZED), SubjectItem("Công nghệ phần mềm nhúng", 2, SubjectType.SPECIALIZED), SubjectItem("Hệ điều hành nhúng TGTT", 3, SubjectType.SPECIALIZED), SubjectItem("Kiểm thử phần mềm nhúng", 2, SubjectType.SPECIALIZED), SubjectItem("Cơ sở ATBMTT", 3, SubjectType.SPECIALIZED), SubjectItem("Linux & phần mềm nguồn mở", 2, SubjectType.SPECIALIZED), SubjectItem("Lập trình hợp ngữ", 3, SubjectType.SPECIALIZED), SubjectItem("Quản trị dự án phần mềm", 2, SubjectType.SPECIALIZED), SubjectItem("Thực tập cơ sở", 3, SubjectType.SPECIALIZED))),
        SemesterData("HK8", listOf(SubjectItem("Lập trình nhân Linux", 4, SubjectType.SPECIALIZED), SubjectItem("Lập trình driver", 4, SubjectType.SPECIALIZED), SubjectItem("Lập trình ARM cơ bản", 3, SubjectType.SPECIALIZED), SubjectItem("Lập trình hệ thống nhúng Linux", 3, SubjectType.SPECIALIZED), SubjectItem("Lập trình Android cơ bản", 3, SubjectType.SPECIALIZED), SubjectItem("Phát triển PM trong thẻ thông minh", 3, SubjectType.SPECIALIZED))),
        SemesterData("HK9", listOf(SubjectItem("Lập trình ARM nâng cao", 3, SubjectType.SPECIALIZED), SubjectItem("Thị giác máy tính trên nền nhúng", 3, SubjectType.SPECIALIZED), SubjectItem("ATBM trong hệ thống nhúng", 3, SubjectType.SPECIALIZED), SubjectItem("Tối ưu phần mềm nhúng", 3, SubjectType.SPECIALIZED), SubjectItem("Lập trình Android nâng cao", 3, SubjectType.SPECIALIZED), SubjectItem("Phát triển game trên Android", 3, SubjectType.SPECIALIZED), SubjectItem("ATBM trong phát triển PM di động", 3, SubjectType.SPECIALIZED), SubjectItem("Tối ưu phần mềm di động", 3, SubjectType.SPECIALIZED))),
        SemesterData("HK10", listOf(SubjectItem("Thực tập tốt nghiệp", 3, SubjectType.THESIS), SubjectItem("Đồ án tốt nghiệp", 8, SubjectType.THESIS)))
    )
)

val AT_PROGRAM = ProgramData(
    code = "AT", name = "An toàn thông tin",
    shortDesc = "Bảo mật hệ thống & mạng máy tính",
    degree = "Cử nhân / Kỹ sư", duration = "4 – 4,5 năm", totalCredits = 153,
    accentColor = Color(0xFFCC0000), accentLight = Color(0xFFFFF0F0),
    icon = Icons.Default.Security,
    highlights = listOf("153 TC" to "Tổng tín chỉ", "9 HK" to "Học kỳ", "4–4.5 năm" to "Thời gian", "3 chuyên ngành" to "Định hướng"),
    overview = "Ngành An toàn thông tin tại KMA là một trong những chương trình đào tạo bảo mật hàng đầu Việt Nam, gắn liền với sứ mệnh bảo vệ an ninh mạng quốc gia. Sinh viên được đào tạo chuyên sâu về mật mã học, phân tích mã độc, kiểm thử xâm nhập, điều tra số và quản lý an toàn hệ thống. Chương trình có 3 định hướng chuyên ngành rõ ràng, giúp sinh viên lựa chọn con đường phù hợp: bảo mật hệ thống, kỹ nghệ an toàn mạng hoặc phát triển phần mềm an toàn.",
    majors = listOf("An toàn hệ thống thông tin", "Kỹ nghệ an toàn mạng", "Công nghệ phần mềm an toàn"),
    careers = listOf(
        CareerPath("Chuyên gia Pentest", Icons.Default.BugReport, "Kiểm thử xâm nhập, đánh giá lỗ hổng bảo mật hệ thống"),
        CareerPath("SOC Analyst", Icons.Default.Shield, "Giám sát, phát hiện và ứng phó sự cố an toàn mạng"),
        CareerPath("Malware Analyst", Icons.Default.FindInPage, "Phân tích mã độc, reverse engineering, threat intelligence"),
        CareerPath("Digital Forensics", Icons.Default.Search, "Điều tra số, thu thập bằng chứng điện tử"),
        CareerPath("Security Engineer", Icons.Default.Lock, "Thiết kế kiến trúc bảo mật cho hệ thống doanh nghiệp")
    ),
    skills = listOf(
        SkillTag("Mật mã học", Color(0xFFCC0000)), SkillTag("Pentest", Color(0xFF6A1B9A)),
        SkillTag("Wireshark", Color(0xFF1565C0)), SkillTag("Metasploit", Color(0xFF2E7D32)),
        SkillTag("Forensics", Color(0xFFE65100)), SkillTag("Malware Analysis", Color(0xFFCC0000)),
        SkillTag("Python", Color(0xFF1565C0)), SkillTag("Linux", Color(0xFF2E7D32)),
        SkillTag("Network Security", Color(0xFF0277BD)), SkillTag("ISO 27001", Color(0xFF558B2F))
    ),
    semesters = listOf(
        SemesterData("HK1", listOf(SubjectItem("Giáo dục quốc phòng an ninh", 8, SubjectType.GENERAL), SubjectItem("Tin học đại cương", 2, SubjectType.GENERAL), SubjectItem("Triết học Mác – Lênin", 3, SubjectType.GENERAL), SubjectItem("Giải tích 1", 3, SubjectType.GENERAL), SubjectItem("Đại số tuyến tính", 3, SubjectType.GENERAL), SubjectItem("Giáo dục thể chất 1", 1, SubjectType.GENERAL), SubjectItem("Pháp luật đại cương", 2, SubjectType.GENERAL))),
        SemesterData("HK2", listOf(SubjectItem("Giải tích 2", 3, SubjectType.GENERAL), SubjectItem("Vật lý đại cương 1", 3, SubjectType.GENERAL), SubjectItem("Kinh tế chính trị Mác – Lênin", 2, SubjectType.GENERAL), SubjectItem("CNXHKH", 2, SubjectType.GENERAL), SubjectItem("Giáo dục thể chất 2", 1, SubjectType.GENERAL), SubjectItem("Tư tưởng Hồ Chí Minh", 2, SubjectType.GENERAL), SubjectItem("Lập trình căn bản", 3, SubjectType.CORE), SubjectItem("Kỹ năng mềm", 2, SubjectType.GENERAL))),
        SemesterData("HK3", listOf(SubjectItem("Vật lý đại cương 2", 3, SubjectType.GENERAL), SubjectItem("Toán xác suất thống kê", 2, SubjectType.CORE), SubjectItem("Toán chuyên đề", 3, SubjectType.CORE), SubjectItem("Tiếng Anh 1", 3, SubjectType.GENERAL), SubjectItem("Cấu trúc dữ liệu & giải thuật", 2, SubjectType.CORE), SubjectItem("Mạng máy tính", 3, SubjectType.CORE), SubjectItem("Toán rời rạc", 2, SubjectType.CORE), SubjectItem("Phương pháp tính", 2, SubjectType.CORE), SubjectItem("Giáo dục thể chất 3", 1, SubjectType.GENERAL), SubjectItem("Lịch sử Đảng CSVN", 2, SubjectType.GENERAL))),
        SemesterData("HK4", listOf(SubjectItem("Tiếng Anh 2", 3, SubjectType.GENERAL), SubjectItem("Kỹ thuật truyền số liệu", 2, SubjectType.CORE), SubjectItem("Cơ sở lý thuyết truyền tin", 2, SubjectType.CORE), SubjectItem("Lý thuyết CSDL", 2, SubjectType.CORE), SubjectItem("Hệ quản trị CSDL", 2, SubjectType.CORE), SubjectItem("Quản trị mạng", 2, SubjectType.CORE), SubjectItem("Kiến trúc máy tính & hợp ngữ", 3, SubjectType.CORE))),
        SemesterData("HK5", listOf(SubjectItem("Tiếng Anh 3", 4, SubjectType.GENERAL), SubjectItem("Lập trình hướng đối tượng", 2, SubjectType.CORE), SubjectItem("Phân tích thiết kế HTTT", 2, SubjectType.CORE), SubjectItem("Nguyên lý hệ điều hành", 2, SubjectType.CORE), SubjectItem("Linux & phần mềm nguồn mở", 2, SubjectType.CORE), SubjectItem("Thuật toán trong ATTT", 2, SubjectType.SPECIALIZED), SubjectItem("Nhập môn mật mã học", 3, SubjectType.SPECIALIZED))),
        SemesterData("HK6", listOf(SubjectItem("Tiếng Anh chuyên ngành", 4, SubjectType.GENERAL), SubjectItem("Chuyên đề cơ sở", 2, SubjectType.CORE), SubjectItem("Cơ sở an toàn thông tin", 3, SubjectType.SPECIALIZED), SubjectItem("An toàn mạng máy tính", 3, SubjectType.SPECIALIZED), SubjectItem("Kỹ thuật lập trình", 2, SubjectType.CORE), SubjectItem("An toàn CSDL", 2, SubjectType.SPECIALIZED), SubjectItem("Giao thức an toàn mạng", 2, SubjectType.SPECIALIZED))),
        SemesterData("HK7", listOf(SubjectItem("Công nghệ web an toàn", 3, SubjectType.SPECIALIZED), SubjectItem("Quản trị an toàn hệ thống", 3, SubjectType.SPECIALIZED), SubjectItem("AT mạng không dây & di động", 2, SubjectType.SPECIALIZED), SubjectItem("Phân tích thiết kế AT mạng", 2, SubjectType.SPECIALIZED), SubjectItem("Mã độc", 3, SubjectType.SPECIALIZED), SubjectItem("Chuyên đề AT hệ thống TT", 2, SubjectType.SPECIALIZED))),
        SemesterData("HK8", listOf(SubjectItem("Giám sát & ứng phó sự cố", 2, SubjectType.SPECIALIZED), SubjectItem("Kiểm thử & đánh giá AT HTTT", 3, SubjectType.SPECIALIZED), SubjectItem("Quản lý an toàn thông tin", 2, SubjectType.SPECIALIZED), SubjectItem("Điều tra số", 3, SubjectType.SPECIALIZED))),
        SemesterData("HK9", listOf(SubjectItem("Thực tập tốt nghiệp", 3, SubjectType.THESIS), SubjectItem("Đồ án tốt nghiệp", 8, SubjectType.THESIS)))
    )
)

val DT_PROGRAM = ProgramData(
    code = "DT", name = "Điện tử viễn thông",
    shortDesc = "Hệ thống nhúng & điều khiển tự động",
    degree = "Cử nhân / Kỹ sư", duration = "4 – 4,5 năm", totalCredits = 169,
    accentColor = Color(0xFF2E7D32), accentLight = Color(0xFFE8F5E9),
    icon = Icons.Default.DeveloperBoard,
    highlights = listOf("169 TC" to "Tổng tín chỉ", "9 HK" to "Học kỳ", "4–4.5 năm" to "Thời gian", "1 chuyên ngành" to "Định hướng"),
    overview = "Ngành Điện tử viễn thông tại KMA đào tạo kỹ sư chuyên sâu về hệ thống nhúng, điều khiển tự động và IoT — nền tảng của các hệ thống thông minh hiện đại. Sinh viên được học từ nền tảng điện tử analog/digital, vi xử lý, đến thiết kế VLSI, FPGA và phát triển ứng dụng IoT. Chương trình chú trọng thực hành với nhiều đồ án kỹ thuật, giúp sinh viên có khả năng thiết kế và triển khai các hệ thống điện tử thông minh trong công nghiệp, y tế và quốc phòng.",
    majors = listOf("Hệ thống nhúng và điều khiển tự động"),
    careers = listOf(
        CareerPath("Kỹ sư hệ thống nhúng", Icons.Default.Memory, "Thiết kế phần cứng và firmware cho thiết bị thông minh"),
        CareerPath("Kỹ sư IoT", Icons.Default.Wifi, "Phát triển hệ thống kết nối vạn vật, smart home, smart city"),
        CareerPath("Kỹ sư tự động hóa", Icons.Default.Settings, "Lập trình PLC, SCADA, hệ thống điều khiển công nghiệp"),
        CareerPath("Kỹ sư VLSI/FPGA", Icons.Default.DeveloperBoard, "Thiết kế vi mạch, FPGA cho các ứng dụng chuyên dụng"),
        CareerPath("Kỹ sư viễn thông", Icons.Default.CellTower, "Thiết kế và vận hành hệ thống truyền thông không dây")
    ),
    skills = listOf(
        SkillTag("C/C++ nhúng", Color(0xFF2E7D32)), SkillTag("VHDL/Verilog", Color(0xFF6A1B9A)),
        SkillTag("PLC/SCADA", Color(0xFFE65100)), SkillTag("RTOS", Color(0xFF1565C0)),
        SkillTag("IoT/MQTT", Color(0xFF0277BD)), SkillTag("ARM Cortex", Color(0xFFCC0000)),
        SkillTag("FPGA", Color(0xFF558B2F)), SkillTag("Altium Designer", Color(0xFF2E7D32)),
        SkillTag("MATLAB", Color(0xFFE65100)), SkillTag("Python", Color(0xFF1565C0))
    ),
    semesters = listOf(
        SemesterData("HK1", listOf(SubjectItem("Giáo dục quốc phòng an ninh", 8, SubjectType.GENERAL), SubjectItem("Triết học Mác – Lênin", 3, SubjectType.GENERAL), SubjectItem("Toán cao cấp 1", 4, SubjectType.GENERAL), SubjectItem("Vật lý đại cương 1", 3, SubjectType.GENERAL), SubjectItem("Tin học đại cương", 2, SubjectType.GENERAL), SubjectItem("Giáo dục thể chất 1", 1, SubjectType.GENERAL))),
        SemesterData("HK2", listOf(SubjectItem("Toán cao cấp 2", 3, SubjectType.GENERAL), SubjectItem("Vật lý đại cương 2", 3, SubjectType.GENERAL), SubjectItem("Lập trình căn bản", 3, SubjectType.CORE), SubjectItem("Kinh tế chính trị Mác – Lênin", 2, SubjectType.GENERAL), SubjectItem("Lịch sử Đảng CSVN", 2, SubjectType.GENERAL), SubjectItem("Giáo dục thể chất 2", 1, SubjectType.GENERAL), SubjectItem("CNXHKH", 2, SubjectType.GENERAL))),
        SemesterData("HK3", listOf(SubjectItem("Toán cao cấp 3", 3, SubjectType.GENERAL), SubjectItem("Xác suất thống kê", 3, SubjectType.CORE), SubjectItem("Thực hành vật lý", 2, SubjectType.GENERAL), SubjectItem("Tiếng Anh 1", 3, SubjectType.GENERAL), SubjectItem("Tư tưởng Hồ Chí Minh", 2, SubjectType.GENERAL), SubjectItem("Công nghệ mạng máy tính", 2, SubjectType.CORE), SubjectItem("Kỹ thuật lập trình", 2, SubjectType.CORE), SubjectItem("Giáo dục thể chất 3", 1, SubjectType.GENERAL), SubjectItem("Kỹ năng mềm", 2, SubjectType.GENERAL))),
        SemesterData("HK4", listOf(SubjectItem("Tiếng Anh 2", 3, SubjectType.GENERAL), SubjectItem("Toán rời rạc", 2, SubjectType.CORE), SubjectItem("Tín hiệu và hệ thống", 2, SubjectType.CORE), SubjectItem("Kỹ thuật điện", 2, SubjectType.CORE), SubjectItem("Linh kiện điện tử", 3, SubjectType.CORE), SubjectItem("Lý thuyết mạch", 2, SubjectType.CORE), SubjectItem("Điện tử công suất", 2, SubjectType.CORE), SubjectItem("Điện tử tương tự", 3, SubjectType.CORE), SubjectItem("Giáo dục thể chất 4", 1, SubjectType.GENERAL))),
        SemesterData("HK5", listOf(SubjectItem("Tiếng Anh 3", 3, SubjectType.GENERAL), SubjectItem("Thông tin số", 2, SubjectType.CORE), SubjectItem("Kỹ thuật đo lường điện tử", 3, SubjectType.CORE), SubjectItem("Kỹ thuật vi xử lý", 2, SubjectType.CORE), SubjectItem("Điện tử tương số", 3, SubjectType.CORE), SubjectItem("Thiết kế mạch điện tử", 2, SubjectType.CORE), SubjectItem("Thực tập cơ sở 1", 2, SubjectType.CORE), SubjectItem("Cơ sở điều khiển tự động", 2, SubjectType.CORE), SubjectItem("Giáo dục thể chất 5", 1, SubjectType.GENERAL))),
        SemesterData("HK6", listOf(SubjectItem("Tiếng Anh chuyên ngành", 3, SubjectType.GENERAL), SubjectItem("Cơ sở lý thuyết truyền tin", 2, SubjectType.CORE), SubjectItem("Kỹ thuật truyền số liệu", 2, SubjectType.CORE), SubjectItem("Hệ thống viễn thông", 2, SubjectType.CORE), SubjectItem("Thiết kế hệ thống số", 3, SubjectType.SPECIALIZED), SubjectItem("Kiến trúc máy tính", 2, SubjectType.CORE), SubjectItem("Điện tử công nghiệp", 2, SubjectType.SPECIALIZED), SubjectItem("Đồ án 1", 2, SubjectType.THESIS))),
        SemesterData("HK7", listOf(SubjectItem("Thiết bị ngoại vi & ghép nối", 2, SubjectType.SPECIALIZED), SubjectItem("Xử lý tín hiệu số", 3, SubjectType.SPECIALIZED), SubjectItem("Hệ điều hành nhúng TGTT", 3, SubjectType.SPECIALIZED), SubjectItem("Mật mã lý thuyết", 2, SubjectType.SPECIALIZED), SubjectItem("Hệ thống nhúng", 3, SubjectType.SPECIALIZED), SubjectItem("Cơ sở thiết kế VLSI", 3, SubjectType.SPECIALIZED), SubjectItem("Thực tập cơ sở 2", 2, SubjectType.CORE), SubjectItem("Đồ án 2", 2, SubjectType.THESIS))),
        SemesterData("HK8", listOf(SubjectItem("Thiết kế hệ thống nhúng", 3, SubjectType.SPECIALIZED), SubjectItem("Phát triển ứng dụng IoT", 3, SubjectType.SPECIALIZED), SubjectItem("Thiết kế PLC", 3, SubjectType.SPECIALIZED), SubjectItem("Thực tập cơ sở 3", 2, SubjectType.CORE), SubjectItem("Đồ án 3", 2, SubjectType.THESIS))),
        SemesterData("HK9", listOf(SubjectItem("Thực tập tốt nghiệp", 3, SubjectType.THESIS), SubjectItem("Đồ án tốt nghiệp", 8, SubjectType.THESIS)))
    )
)

// ─── Main screen ──────────────────────────────────────────────────────────────
@Composable
fun AboutScreen(navController: NavController, initialProgramCode: String = "CT") {
    val programs = listOf(CT_PROGRAM, AT_PROGRAM, DT_PROGRAM)
    var selectedIdx by remember {
        mutableStateOf(programs.indexOfFirst { it.code == initialProgramCode }.coerceAtLeast(0))
    }
    val prog = programs[selectedIdx]
    var tab by remember(selectedIdx) { mutableStateOf(0) }

    Scaffold(
        topBar = { KmaTopBar(title = "Chương trình học", navController = navController, showBack = true) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Program selector ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(prog.accentColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                programs.forEachIndexed { i, p ->
                    val selected = i == selectedIdx
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedIdx = i },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) White else White.copy(alpha = 0.2f),
                        elevation = if (selected) 2.dp else 0.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(p.icon, contentDescription = null,
                                tint = if (selected) p.accentColor else White,
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(2.dp))
                            Text(p.code, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = if (selected) p.accentColor else White)
                        }
                    }
                }
            }

            // ── Content tabs ─────────────────────────────────────────────
            TabRow(
                selectedTabIndex = tab,
                backgroundColor = White,
                contentColor = prog.accentColor,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tab]),
                        color = prog.accentColor, height = 3.dp
                    )
                }
            ) {
                listOf("Tổng quan", "Chương trình học").forEachIndexed { i, title ->
                    Tab(selected = tab == i, onClick = { tab = i },
                        text = { Text(title, fontSize = 13.sp, fontWeight = if (tab == i) FontWeight.Bold else FontWeight.Normal) })
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                when (tab) {
                    0 -> overviewItems(prog)
                    1 -> curriculumItems(prog)
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.overviewItems(prog: ProgramData) {
    // Hero banner
    item {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(prog.accentColor, prog.accentColor.copy(alpha = 0.75f))))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(52.dp)
                            .background(White.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(prog.icon, contentDescription = null, tint = White, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(prog.name, color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(prog.shortDesc, color = White.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    prog.highlights.forEach { (value, label) ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(value, color = White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(label, color = White.copy(alpha = 0.8f), fontSize = 9.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }

    // Overview text
    item {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader("Giới thiệu ngành", prog.accentColor)
            Text(prog.overview, fontSize = 13.sp, color = OnSurfaceHigh, lineHeight = 20.sp)
        }
    }

    // Majors
    if (prog.majors.isNotEmpty()) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader("Chuyên ngành / Định hướng", prog.accentColor)
                prog.majors.forEachIndexed { i, major ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(prog.accentLight, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(28.dp)
                                .background(prog.accentColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${i + 1}", color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(major, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OnSurfaceHigh)
                    }
                }
            }
        }
    }

    // Career paths
    item {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader("Cơ hội nghề nghiệp", prog.accentColor)
            prog.careers.forEach { career ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = 0.dp,
                    backgroundColor = prog.accentLight
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp)
                                .background(prog.accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(career.icon, contentDescription = null, tint = prog.accentColor, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(career.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = OnSurfaceHigh)
                            Text(career.desc, fontSize = 11.sp, color = OnSurfaceMedium, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }
    }

    // Skills
    item {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader("Kỹ năng & Công nghệ", prog.accentColor)
            // Manual wrap: chunk skills into rows of 3
            prog.skills.chunked(3).forEach { rowSkills ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowSkills.forEach { skill ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            color = skill.color.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, skill.color.copy(alpha = 0.4f))
                        ) {
                            Text(
                                skill.label,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 11.sp, color = skill.color, fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center, maxLines = 1
                            )
                        }
                    }
                    // Fill empty slots in last row
                    repeat(3 - rowSkills.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.curriculumItems(prog: ProgramData) {
    val totalCredits = prog.semesters.sumOf { s -> s.subjects.sumOf { it.credits } }

    // Credit breakdown header
    item {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader("Phân bổ tín chỉ", prog.accentColor)
            val typeCredits = SubjectType.values().associateWith { type ->
                prog.semesters.sumOf { s -> s.subjects.filter { it.type == type }.sumOf { it.credits } }
            }
            val typeLabels = mapOf(
                SubjectType.GENERAL    to ("Đại cương" to Color(0xFF78909C)),
                SubjectType.CORE       to ("Cơ sở ngành" to Color(0xFF1565C0)),
                SubjectType.SPECIALIZED to ("Chuyên ngành" to prog.accentColor),
                SubjectType.THESIS     to ("Đồ án/TN" to Color(0xFFE65100))
            )
            typeLabels.forEach { (type, pair) ->
                val (label, color) = pair
                val credits = typeCredits[type] ?: 0
                if (credits == 0) return@forEach
                val fraction = credits.toFloat() / totalCredits
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, fontSize = 12.sp, color = OnSurfaceMedium, modifier = Modifier.width(110.dp))
                    Box(modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp)).background(color.copy(alpha = 0.15f))) {
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction).background(color, RoundedCornerShape(5.dp)))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("$credits TC", fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
                }
            }
        }
    }

    // Semester cards
    items(prog.semesters) { sem ->
        SemesterCard(sem, prog.accentColor)
        Spacer(Modifier.height(8.dp))
    }
    item { Spacer(Modifier.height(8.dp)) }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(4.dp).height(18.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurfaceHigh)
    }
}

@Composable
private fun SemesterCard(sem: SemesterData, accentColor: Color) {
    var expanded by remember { mutableStateOf(false) }
    val totalTC = sem.subjects.sumOf { it.credits }
    val typeColor = mapOf(
        SubjectType.GENERAL     to Color(0xFF78909C),
        SubjectType.CORE        to Color(0xFF1565C0),
        SubjectType.SPECIALIZED to accentColor,
        SubjectType.THESIS      to Color(0xFFE65100)
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp)
                        .background(accentColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(sem.semester, color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("${sem.subjects.size} môn học", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("$totalTC tín chỉ", fontSize = 12.sp, color = OnSurfaceMedium)
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null, tint = OnSurfaceMedium
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(200)),
                exit = shrinkVertically(tween(200))
            ) {
                Column {
                    Divider(color = Outline.copy(alpha = 0.5f))
                    sem.subjects.forEach { subject ->
                        val color = typeColor[subject.type] ?: accentColor
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
                            Spacer(Modifier.width(10.dp))
                            Text(subject.name, modifier = Modifier.weight(1f), fontSize = 13.sp, color = OnSurfaceHigh)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = color.copy(alpha = 0.12f)
                            ) {
                                Text("${subject.credits}TC", modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
                            }
                        }
                        Divider(color = Outline.copy(alpha = 0.3f), modifier = Modifier.padding(start = 30.dp))
                    }
                    // Type legend
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val usedTypes = sem.subjects.map { it.type }.toSet()
                        mapOf(SubjectType.GENERAL to "Đại cương", SubjectType.CORE to "Cơ sở",
                            SubjectType.SPECIALIZED to "Chuyên ngành", SubjectType.THESIS to "Đồ án")
                            .filter { it.key in usedTypes }
                            .forEach { (type, label) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(7.dp).background(typeColor[type] ?: accentColor, CircleShape))
                                    Spacer(Modifier.width(3.dp))
                                    Text(label, fontSize = 10.sp, color = OnSurfaceMedium)
                                }
                            }
                    }
                }
            }
        }
    }
}
