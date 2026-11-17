package com.bar.trainer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bar.trainer.data.Lecture
import com.bar.trainer.ui.components.FrostedCard
import com.bar.trainer.ui.components.NeonBadge
import com.bar.trainer.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LecturesScreen(lectures: List<Lecture>) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLecture by remember { mutableStateOf<Lecture?>(null) }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    val filteredLectures = remember(lectures, searchQuery) {
        lectures.filter { l ->
            searchQuery.isBlank() ||
                    l.title.contains(searchQuery, ignoreCase = true) ||
                    l.content.contains(searchQuery, ignoreCase = true) ||
                    l.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Banner Header
        FrostedCard(glowColor = NeonCyan, cornerRadius = 18.dp) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Rounded.Book, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(32.dp))
                Column {
                    Text("БАЗА ЗНАНИЙ И ЛЕКЦИИ", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                    Text("Обучающие материалы бара", style = MaterialTheme.typography.headlineLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск по лекциям и темам...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = NeonCyan) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredLectures.isEmpty()) {
            FrostedCard(modifier = Modifier.fillMaxWidth()) {
                Text("Лекций по вашему запросу не найдено.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredLectures, key = { it.id }) { lecture ->
                    FrostedCard(
                        glowColor = AmberGold,
                        cornerRadius = 16.dp,
                        onClick = { selectedLecture = lecture }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                NeonBadge(text = lecture.category, accentColor = AmberGold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = lecture.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Автор: ${lecture.authorName} • ${dateFormat.format(Date(lecture.createdAt))}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Lecture Reader Dialog
    selectedLecture?.let { lecture ->
        AlertDialog(
            onDismissRequest = { selectedLecture = null },
            containerColor = DeepSlateSurface,
            title = {
                Column {
                    NeonBadge(text = lecture.category, accentColor = AmberGold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(lecture.title, style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Автор: ${lecture.authorName} (${dateFormat.format(Date(lecture.createdAt))})",
                        fontSize = 12.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider(color = FrostedGlassBorder)
                    Text(
                        text = lecture.content,
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedLecture = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Закрыть лекцию", color = ObsidianBg, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
