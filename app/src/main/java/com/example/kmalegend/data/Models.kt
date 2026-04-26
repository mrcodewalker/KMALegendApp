package com.example.kmalegend.data

data class StudentInfo(
    val display_name: String = "",
    val student_code: String = "",
    val gender: String = "",
    val birthday: String = "",
    val birth_place: String = "",
    val id_card: String = "",
    val bank_account: String = "",
    val enroll_semester: String = "",
    val phone: String = "",
    val email: String = ""
)

data class CourseSchedule(
    val course_name: String = "",
    val course_code: String = "",
    val study_days: String = "",
    val lessons: String = "",
    val study_location: String = "",
    val teacher: String = ""
)

data class ScoreDTO(
    val scoreText: String = "",
    val scoreFirst: Double = 0.0,
    val scoreSecond: Double = 0.0,
    val scoreFinal: Double = 0.0,
    val scoreOverall: Double = 0.0,
    val subjectName: String = "",
    val subjectCredit: Int = 0
)

data class VirtualScore(
    val scoreText: String = "",
    val scoreFirst: Double = 0.0,
    val scoreSecond: Double = 0.0,
    val scoreFinal: Double = 0.0,
    val scoreOverall: Double = 0.0,
    val subjectName: String = "",
    val subjectCredit: Int = 0,
    val isSelected: Boolean = true
)

data class StudentBasicInfo(
    val studentCode: String = "",
    val studentName: String = "",
    val studentClass: String = ""
)

// Request cho /score-batch/create-or-update
data class ScoreBatchRequest(
    val studentInfo: StudentBasicInfo,
    val scores: List<VirtualScore>,
    val lastUpdated: String
)

// Response từ /score-batch/get-by-encrypted
data class ScoreBatchItem(
    val itemId: Long = 0,
    val scoreText: String = "",
    val scoreFirst: Double = 0.0,
    val scoreSecond: Double = 0.0,
    val scoreFinal: Double = 0.0,
    val scoreOverall: Double = 0.0,
    val subjectName: String = "",
    val subjectCredit: Int = 0,
    val isSelected: Boolean = true
)

data class ScoreBatch(
    val batchId: Long = 0,
    val studentCode: String = "",
    val studentName: String = "",
    val studentClass: String = "",
    val lastUpdated: String = "",
    val scoreItems: List<ScoreBatchItem> = emptyList()
)

data class ScoreBatchResponse(
    val student_info: StudentInfo? = null,
    val score_batch: ScoreBatch? = null
)

fun ScoreBatchItem.toVirtualScore() = VirtualScore(
    scoreText = scoreText,
    scoreFirst = scoreFirst,
    scoreSecond = scoreSecond,
    scoreFinal = scoreFinal,
    scoreOverall = scoreOverall,
    subjectName = subjectName,
    subjectCredit = subjectCredit,
    isSelected = isSelected
)

data class ScholarshipStudent(
    val studentCode: String = "",
    val studentName: String = "",
    val studentClass: String = "",
    val ranking: Int = 0,
    val gpa: Double = 0.0,
    val asiaGpa: Double = 0.0
)

data class CourseDetails(
    val study_days: String = "",
    val teacher: String = "",
    val course_code: String = "",
    val course_name: String = "",
    val study_location: String = "",
    val lessons: String = ""
)

data class VirtualCalendarItem(
    val course: String = "",
    val course_name: String = "",
    val details: CourseDetails = CourseDetails(),
    val base_time: String = ""
)

data class EncryptedPayload(
    val encryptedKey: String,
    val encryptedData: String,
    val iv: String
)

// API Response wrappers
data class LoginResponse(
    val message: String = "",
    val code: String = "",
    val data: LoginData? = null
)

data class LoginData(
    val student_info: StudentInfo? = null,
    val student_schedule: List<CourseSchedule>? = null
)

data class VirtualCalendarResponse(
    val message: String = "",
    val code: String = "",
    val data: VirtualCalendarData? = null
)

data class VirtualCalendarData(
    val student_info: StudentInfo? = null,
    val virtual_calendar: List<VirtualCalendarItem>? = null
)

data class ScoresResponse(
    val studentDTO: StudentBasicInfo? = null,
    val scoreDTOS: List<ScoreDTO>? = null
)

// Lesson time mapping
data class LessonTime(val start: String, val end: String)

val LESSON_TIME_MAP: Map<Set<Int>, LessonTime> = mapOf(
    setOf(1, 2, 3) to LessonTime("07:00", "09:25"),
    setOf(4, 5, 6) to LessonTime("09:35", "12:00"),
    setOf(7, 8, 9) to LessonTime("12:30", "14:55"),
    setOf(10, 11, 12) to LessonTime("15:05", "17:30"),
    setOf(13, 14, 15, 16) to LessonTime("18:00", "20:30")
)

fun getLessonTime(lessons: String): LessonTime {
    val nums = lessons.split(",").mapNotNull { it.trim().toIntOrNull() }
    if (nums.isEmpty()) return LessonTime("07:00", "09:25")
    val min = nums.min()
    return when {
        min <= 3 -> LessonTime("07:00", "09:25")
        min <= 6 -> LessonTime("09:35", "12:00")
        min <= 9 -> LessonTime("12:30", "14:55")
        min <= 12 -> LessonTime("15:05", "17:30")
        else -> LessonTime("18:00", "20:30")
    }
}

// Grade conversion
data class GradeInfo(val min: Double, val max: Double, val grade4: Double, val letter: String, val label: String)

val GRADE_TABLE = listOf(
    GradeInfo(9.0, 10.0, 4.0, "A+", "Xuất sắc"),
    GradeInfo(8.5, 8.9, 3.8, "A", "Giỏi"),
    GradeInfo(7.8, 8.4, 3.5, "B+", "Khá"),
    GradeInfo(7.0, 7.7, 3.0, "B", "Khá"),
    GradeInfo(6.3, 6.9, 2.4, "C+", "Trung bình"),
    GradeInfo(5.5, 6.2, 2.0, "C", "Trung bình"),
    GradeInfo(4.8, 5.4, 1.5, "D+", "Trung bình yếu"),
    GradeInfo(4.0, 4.7, 1.0, "D", "Trung bình yếu"),
    GradeInfo(0.0, 3.9, 0.0, "F", "Kém")
)

fun scoreToLetter(score: Double): String {
    return GRADE_TABLE.firstOrNull { score >= it.min && score <= it.max }?.letter ?: "F"
}

fun scoreToGrade4(score: Double): Double {
    return GRADE_TABLE.firstOrNull { score >= it.min && score <= it.max }?.grade4 ?: 0.0
}

fun calculateScoreOverall(tp1: Double, tp2: Double, ck: Double): Double {
    return (tp1 * 0.7 + tp2 * 0.3) * 0.3 + ck * 0.7
}

fun isFailedSubject(scoreFinal: Double, scoreOverall: Double): Boolean {
    return scoreFinal < 2.0 || scoreOverall < 4.0
}

val EXCLUDED_GPA_SUBJECTS = listOf("giáo dục thể chất", "thực hành vật lý")

fun isExcludedFromGPA(subjectName: String): Boolean {
    val lower = subjectName.lowercase()
    return EXCLUDED_GPA_SUBJECTS.any { lower.contains(it) }
}

fun calculateCPA(scores: List<ScoreDTO>): Double {
    val valid = scores.filter { !isExcludedFromGPA(it.subjectName) && !isFailedSubject(it.scoreFinal, it.scoreOverall) }
    val totalCredits = valid.sumOf { it.subjectCredit }
    if (totalCredits == 0) return 0.0
    val weightedSum = valid.sumOf { scoreToGrade4(it.scoreOverall) * it.subjectCredit }
    return weightedSum / totalCredits
}

// Program info
data class ProgramInfo(val code: String, val name: String, val totalCredits: Int)

val PROGRAMS = listOf(
    ProgramInfo("CT", "Công nghệ thông tin", 176),
    ProgramInfo("AT", "An toàn thông tin", 153),
    ProgramInfo("DT", "Điện tử viễn thông", 169)
)
