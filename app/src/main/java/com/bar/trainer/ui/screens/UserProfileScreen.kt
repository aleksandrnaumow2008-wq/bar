package com.bar.trainer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bar.trainer.data.AchievementBadge
import com.bar.trainer.data.UserProfile
import com.bar.trainer.ui.components.FrostedCard
import com.bar.trainer.ui.components.NeonBadge
import com.bar.trainer.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserProfileScreen(
    user: UserProfile,
    totalCocktailsCount: Int,
    onLogout: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    val achievements = listOf(
        AchievementBadge(
            id = "first_steps",
            title = "Первые шаги",
            description = "Освоено 3+ техкарт",
            iconEmoji = "🌱",
            isUnlocked = user.masteredIds.size >= 3
        ),
        AchievementBadge(
            id = "menu_master",
            title = "Мастер Карты",
            description = "Освоены все техкарты",
            iconEmoji = "👑",
            isUnlocked = user.masteredIds.size >= totalCocktailsCount && totalCocktailsCount > 0
        ),
        AchievementBadge(
            id = "speedrunner",
            title = "Спидраннер",
            description = "300+ очков в режиме «Запара»",
            iconEmoji = "⚡",
            isUnlocked = user.bestTimeAttackScore >= 300
        ),
        AchievementBadge(
            id = "perfect_exam",
            title = "Отличник",
            description = "100% результат на аттестации",
            iconEmoji = "🎯",
            isUnlocked = user.bestExamScore >= 100
        ),
        AchievementBadge(
            id = "bar_pro",
            title = "Профи Смены",
            description = "Сдано 3+ аттестации",
            iconEmoji = "🛡️",
            isUnlocked = user.totalPassedExams >= 3
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card
        item {
            FrostedCard(glowColor = NeonCyan, cornerRadius = 20.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .border(2.dp, NeonCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.avatarEmoji, fontSize = 32.sp)
                        }

                        Column {
                            Text(user.name, style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                NeonBadge(
                                    text = user.role.title,
                                    accentColor = Color(user.role.badgeColorHex)
                                )
                                NeonBadge(
                                    text = if (user.bestExamScore >= 85) "ДОПУЩЕН К БАРУ" else "ТРЕБУЕТСЯ АТТЕСТАЦИЯ",
                                    accentColor = if (user.bestExamScore >= 85) EmeraldSuccess else CrimsonError
                                )
                            }
                        }
                    }

                    IconButton(onClick = onLogout) {
                        Icon(Icons.Rounded.Logout, contentDescription = "Выйти", tint = TextSecondary)
                    }
                }
            }
        }

        // Stats Matrix
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FrostedCard(glowColor = AmberGold, modifier = Modifier.weight(1f)) {
                    Text("РЕКОРД АТТЕСТАЦИИ", style = MaterialTheme.typography.labelMedium, color = AmberGold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${user.bestExamScore}%", style = MaterialTheme.typography.headlineLarge)
                    Text("Сдано: ${user.totalPassedExams}/${user.totalExamsTaken}", style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp)
                }

                FrostedCard(glowColor = ElectricMagenta, modifier = Modifier.weight(1f)) {
                    Text("РЕКОРД ЗАПАРЫ", style = MaterialTheme.typography.labelMedium, color = ElectricMagenta)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${user.bestTimeAttackScore} оч.", style = MaterialTheme.typography.headlineLarge)
                    Text("Скоростной режим", style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp)
                }
            }
        }

        // Mastered Tech Cards Progress
        item {
            FrostedCard(glowColor = NeonCyan) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ОСВОЕНИЕ ТЕХКАРТ БАРА", style = MaterialTheme.typography.titleMedium)
                    NeonBadge(text = "${user.masteredIds.size} / $totalCocktailsCount", accentColor = NeonCyan)
                }
                Spacer(modifier = Modifier.height(8.dp))
                val progress = if (totalCocktailsCount > 0) user.masteredIds.size.toFloat() / totalCocktailsCount else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = NeonCyan,
                    trackColor = FrostedGlassFill
                )
            }
        }

        // Achievements Section
        item {
            Text("Достижения и Бейджи", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(achievements) { badge ->
                    FrostedCard(
                        glowColor = if (badge.isUnlocked) AmberGold else Color.Transparent,
                        cornerRadius = 16.dp,
                        modifier = Modifier.width(150.dp)
                    ) {
                        Text(badge.iconEmoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(badge.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (badge.isUnlocked) TextPrimary else TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(badge.description, fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        NeonBadge(
                            text = if (badge.isUnlocked) "ОТКРЫТО" else "ЗАКРЫТО",
                            accentColor = if (badge.isUnlocked) EmeraldSuccess else TextSecondary
                        )
                    }
                }
            }
        }

        // Exam History Log
        item {
            Text("История Аттестаций", style = MaterialTheme.typography.titleMedium)
        }

        if (user.examHistory.isEmpty()) {
            item {
                Text("Пока нет зафиксированных попыток аттестации.", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            items(user.examHistory) { entry ->
                FrostedCard(glowColor = if (entry.passed) EmeraldSuccess else CrimsonError, cornerRadius = 14.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (entry.passed) "СДАНО — ${entry.scorePercent}%" else "ПЕРЕСДАЧА — ${entry.scorePercent}%",
                                fontWeight = FontWeight.Bold,
                                color = if (entry.passed) EmeraldSuccess else CrimsonError
                            )
                            Text(dateFormat.format(Date(entry.timestamp)), style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp)
                        }
                        NeonBadge(
                            text = "${entry.correctCount}/${entry.totalQuestions} Вопросов",
                            accentColor = if (entry.passed) EmeraldSuccess else CrimsonError
                        )
                    }

                    if (entry.mistakesSummary.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = FrostedGlassBorder)
                        Spacer(modifier = Modifier.height(6.dp))
                        entry.mistakesSummary.forEach { m ->
                            Text("• $m", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Recent Mistakes
        if (user.recentMistakes.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Журнал Замеченных Ошибок", style = MaterialTheme.typography.titleMedium, color = CrimsonError)
            }

            items(user.recentMistakes.take(8)) { m ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FrostedGlassFill)
                        .border(1.dp, CrimsonError.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(m.cocktailName, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(m.errorDescription, fontSize = 13.sp, color = CrimsonError)
                    }
                    Text(dateFormat.format(Date(m.timestamp)), fontSize = 10.sp, color = TextSecondary)
                }
            }
        }
    }
}
