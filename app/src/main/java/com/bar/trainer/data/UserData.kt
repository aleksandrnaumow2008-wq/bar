package com.bar.trainer.data

enum class UserRole(val title: String, val badgeColorHex: Long) {
    BARTENDER("Бармен", 0xFF00F5D4),
    ADMIN("Управляющий", 0xFFFF2A85)
}

data class ExamHistoryEntry(
    val id: String,
    val timestamp: Long,
    val scorePercent: Int,
    val passed: Boolean,
    val correctCount: Int,
    val totalQuestions: Int,
    val timeSpentSeconds: Int,
    val mistakesSummary: List<String>
)

data class BartenderMistake(
    val cocktailName: String,
    val errorDescription: String,
    val timestamp: Long
)

data class UserProfile(
    val id: String,
    val username: String,
    val name: String,
    val pin: String,
    val password: String = "123456",
    val role: UserRole,
    val avatarEmoji: String,
    val isVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val masteredIds: Set<Int> = emptySet(),
    val bestExamScore: Int = 0,
    val bestTimeAttackScore: Int = 0,
    val totalPassedExams: Int = 0,
    val totalExamsTaken: Int = 0,
    val examHistory: List<ExamHistoryEntry> = emptyList(),
    val recentMistakes: List<BartenderMistake> = emptyList()
)

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean
)

data class Lecture(
    val id: String,
    val title: String,
    val category: String,
    val content: String,
    val authorName: String,
    val createdAt: Long = System.currentTimeMillis()
)
