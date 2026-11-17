package com.bar.trainer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bar.trainer.data.Cocktail
import com.bar.trainer.ui.components.CocktailGlassVisualizer
import com.bar.trainer.ui.components.FrostedCard
import com.bar.trainer.ui.components.NeonBadge
import com.bar.trainer.ui.theme.*
import kotlinx.coroutines.delay

data class TimeAttackQuestion(
    val cocktail: Cocktail,
    val questionPrompt: String,
    val options: List<String>,
    val correctAnswer: String
)

@Composable
fun TimeAttackScreen(
    allCocktails: List<Cocktail>,
    bestScore: Int,
    onFinishRound: (score: Int) -> Unit
) {
    var isActive by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var secondsLeft by remember { mutableStateOf(60) }
    var currentQuestion by remember { mutableStateOf<TimeAttackQuestion?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isFeedbackPositive by remember { mutableStateOf(true) }

    fun generateQuestion(): TimeAttackQuestion {
        val target = allCocktails.random()
        return when ((0..2).random()) {
            0 -> {
                val wrong = allCocktails.filter { it.glassware != target.glassware }
                    .map { it.glassware.title }
                    .distinct()
                    .shuffled()
                    .take(3)
                TimeAttackQuestion(
                    cocktail = target,
                    questionPrompt = "Бокал под «${target.name}»?",
                    options = (wrong + target.glassware.title).shuffled(),
                    correctAnswer = target.glassware.title
                )
            }
            1 -> {
                val wrong = allCocktails.filter { it.method != target.method }
                    .map { it.method.title }
                    .distinct()
                    .shuffled()
                    .take(3)
                TimeAttackQuestion(
                    cocktail = target,
                    questionPrompt = "Метод отдачи «${target.name}»?",
                    options = (wrong + target.method.title).shuffled(),
                    correctAnswer = target.method.title
                )
            }
            else -> {
                val wrongGarnishes = allCocktails.map { it.garnish }
                    .filter { it != target.garnish }
                    .distinct()
                    .shuffled()
                    .take(3)
                TimeAttackQuestion(
                    cocktail = target,
                    questionPrompt = "Украшение (гарниш) «${target.name}»?",
                    options = (wrongGarnishes + target.garnish).shuffled(),
                    correctAnswer = target.garnish
                )
            }
        }
    }

    LaunchedEffect(isActive, isFinished) {
        if (isActive && !isFinished) {
            while (secondsLeft > 0) {
                delay(1000L)
                secondsLeft -= 1
            }
            isActive = false
            isFinished = true
            onFinishRound(score)
        }
    }

    fun startNewRound() {
        score = 0
        streak = 0
        secondsLeft = 60
        isFinished = false
        isActive = true
        currentQuestion = generateQuestion()
    }

    fun handleAnswer(answer: String) {
        val q = currentQuestion ?: return
        if (answer == q.correctAnswer) {
            streak += 1
            val gained = 100 * streak
            score += gained
            feedbackMessage = "+$gained! СТРИК x$streak 🔥"
            isFeedbackPositive = true
        } else {
            streak = 0
            secondsLeft = maxOf(0, secondsLeft - 4)
            feedbackMessage = "ОШИБКА! -4 сек ⚠️ (Правильно: ${q.correctAnswer})"
            isFeedbackPositive = false
        }
        currentQuestion = generateQuestion()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isActive && !isFinished) {
            FrostedCard(glowColor = ElectricMagenta, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.Bolt, contentDescription = null, tint = ElectricMagenta, modifier = Modifier.size(32.dp))
                    Column {
                        Text("РЕЖИМ «ЗАПАРА» (СПОДРАН)", style = MaterialTheme.typography.labelMedium, color = ElectricMagenta)
                        Text("Скоростной бармен", style = MaterialTheme.typography.headlineLarge)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "• 60 секунд на непрерывную отдачу коктейлей\n" +
                            "• За каждую правильную отдачу растет СТРИК МУЛЬТИПЛИКАТОР (x1, x2, x3...)\n" +
                            "• Ошибка сбрасывает стрик и отнимает 4 секунды времени!\n" +
                            "• Твой личный рекорд: $bestScore очков",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { startNewRound() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricMagenta)
                ) {
                    Text("ВСТАТЬ НА ЗАПАРУ (СТАРТ)", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        } else if (isActive && currentQuestion != null) {
            val q = currentQuestion!!

            // Top Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonBadge(text = "ОЧКИ: $score", accentColor = NeonCyan)
                NeonBadge(text = "СТРИК: x$streak 🔥", accentColor = ElectricMagenta)
                NeonBadge(text = "⏱️ $secondsLeft с", accentColor = if (secondsLeft < 15) CrimsonError else AmberGold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cocktail Card
            FrostedCard(glowColor = NeonCyan, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ЗАКАЗ ГОСТЯ", color = ElectricMagenta, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(q.cocktail.name, style = MaterialTheme.typography.headlineLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(q.questionPrompt, style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                    }
                    CocktailGlassVisualizer(
                        ingredients = q.cocktail.ingredients,
                        modifier = Modifier.size(width = 60.dp, height = 80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            feedbackMessage?.let { msg ->
                Text(
                    text = msg,
                    color = if (isFeedbackPositive) EmeraldSuccess else CrimsonError,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Option Choices
            q.options.forEach { option ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FrostedGlassFill)
                        .border(1.5.dp, FrostedGlassBorder, RoundedCornerShape(14.dp))
                        .clickable { handleAnswer(option) }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = option,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        } else if (isFinished) {
            val isNewRecord = score > bestScore

            FrostedCard(
                glowColor = if (isNewRecord) EmeraldSuccess else AmberGold,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.Star, contentDescription = null, tint = AmberGold, modifier = Modifier.size(36.dp))
                    Column {
                        Text(
                            text = if (isNewRecord) "🎉 НОВЫЙ РЕКОРД СМЕНЫ!" else "СМЕНА ЗАВЕРШЕНА",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isNewRecord) EmeraldSuccess else AmberGold
                        )
                        Text(
                            text = "$score очков",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isNewRecord) {
                        "Отличный темп на баре! Прошлая планка превзойдена."
                    } else {
                        "Предыдущий рекорд: $bestScore. Продолжай тренироваться!"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { startNewRound() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("ПОВТОРНО НА ЗАПАРУ", color = ObsidianBg, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
