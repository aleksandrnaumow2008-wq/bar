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
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bar.trainer.data.*
import com.bar.trainer.ui.components.FrostedCard
import com.bar.trainer.ui.components.NeonBadge
import com.bar.trainer.ui.theme.*

enum class AdminTab(val title: String) {
    STAFF("Штат и Результаты"),
    LECTURES("Публикация лекций"),
    ANALYTICS("Аналитика ошибок"),
    RECIPES("Управление картой")
}

@Composable
fun AdminDashboardScreen(
    users: List<UserProfile>,
    allCocktails: List<Cocktail>,
    lectures: List<Lecture>,
    onAddCocktail: (Cocktail) -> Unit,
    onDeleteCocktail: (Int) -> Unit,
    onAddLecture: (Lecture) -> Unit,
    onDeleteLecture: (String) -> Unit,
    onToggleVerifyUser: (String) -> Unit,
    onDeleteUser: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedAdminTab by remember { mutableStateOf(AdminTab.STAFF) }
    var searchQuery by remember { mutableStateOf("") }
    var inspectedBartender by remember { mutableStateOf<UserProfile?>(null) }
    var showAddCocktailDialog by remember { mutableStateOf(false) }
    var showAddLectureDialog by remember { mutableStateOf(false) }

    val bartenders = remember(users) {
        users.filter { it.role == UserRole.BARTENDER }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Admin Top Banner
        FrostedCard(glowColor = ElectricMagenta, cornerRadius = 18.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ElectricMagenta.copy(alpha = 0.2f))
                            .border(1.5.dp, ElectricMagenta, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 24.sp)
                    }

                    Column {
                        Text("ПАНЕЛЬ УПРАВЛЯЮЩЕГО", style = MaterialTheme.typography.labelMedium, color = ElectricMagenta)
                        Text("Контроль смены и аттестаций", style = MaterialTheme.typography.titleMedium)
                    }
                }

                IconButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Выйти", tint = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Tabs for Admin
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AdminTab.entries) { tab ->
                val selected = selectedAdminTab == tab
                FilterChip(
                    selected = selected,
                    onClick = { selectedAdminTab = tab },
                    label = { Text(tab.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricMagenta.copy(alpha = 0.25f),
                        selectedLabelColor = ElectricMagenta
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedAdminTab) {
            AdminTab.STAFF -> {
                val totalPassed = bartenders.count { it.bestExamScore >= 85 }
                val avgScore = if (bartenders.isNotEmpty()) bartenders.map { it.bestExamScore }.average().toInt() else 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FrostedCard(glowColor = NeonCyan, modifier = Modifier.weight(1f)) {
                        Text("ВСЕГО БАРМЕНОВ", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                        Text("${bartenders.size}", style = MaterialTheme.typography.headlineLarge)
                    }

                    FrostedCard(glowColor = EmeraldSuccess, modifier = Modifier.weight(1f)) {
                        Text("ДОПУЩЕНО К СМЕНЕ", style = MaterialTheme.typography.labelMedium, color = EmeraldSuccess)
                        Text("$totalPassed / ${bartenders.size}", style = MaterialTheme.typography.headlineLarge)
                    }

                    FrostedCard(glowColor = AmberGold, modifier = Modifier.weight(1f)) {
                        Text("СРЕДНИЙ БАЛЛ", style = MaterialTheme.typography.labelMedium, color = AmberGold)
                        Text("$avgScore%", style = MaterialTheme.typography.headlineLarge)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск бармена по имени...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = NeonCyan) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val filteredStaff = bartenders.filter {
                    searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredStaff, key = { it.id }) { user ->
                        val isPassed = user.bestExamScore >= 85
                        FrostedCard(
                            glowColor = if (isPassed) EmeraldSuccess else CrimsonError,
                            onClick = { inspectedBartender = user }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(user.avatarEmoji, fontSize = 28.sp)
                                    Column {
                                        Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("Логин: ${user.username} • Пароль: ${user.password}", fontSize = 11.sp, color = TextSecondary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            NeonBadge(
                                                text = if (isPassed) "ДОПУЩЕН" else "ПЕРЕСДАЧА",
                                                accentColor = if (isPassed) EmeraldSuccess else CrimsonError
                                            )
                                            NeonBadge(
                                                text = "ТЕХКАРТЫ ${user.masteredIds.size}/${allCocktails.size}",
                                                accentColor = NeonCyan
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${user.bestExamScore}%", style = MaterialTheme.typography.titleLarge, color = if (isPassed) EmeraldSuccess else CrimsonError)
                                    Text("Запара: ${user.bestTimeAttackScore} оч.", style = MaterialTheme.typography.bodyMedium, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            AdminTab.LECTURES -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Button(
                        onClick = { showAddLectureDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = ObsidianBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Опубликовать новую лекцию", color = ObsidianBg, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Опубликованные лекции (${lectures.size})", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(lectures, key = { it.id }) { lecture ->
                            FrostedCard(glowColor = AmberGold, cornerRadius = 14.dp) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        NeonBadge(text = lecture.category, accentColor = AmberGold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(lecture.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("Автор: ${lecture.authorName}", fontSize = 12.sp, color = TextSecondary)
                                    }

                                    IconButton(onClick = { onDeleteLecture(lecture.id) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Удалить", tint = CrimsonError)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AdminTab.ANALYTICS -> {
                val allMistakes = bartenders.flatMap { it.recentMistakes }
                val mistakeCountByCocktail = allMistakes.groupBy { it.cocktailName }
                    .mapValues { it.value.size }
                    .toList()
                    .sortedByDescending { it.second }

                Column(modifier = Modifier.fillMaxSize()) {
                    Text("Частые проблемы у команды", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (allMistakes.isEmpty()) {
                        FrostedCard {
                            Text("Ошибок не зафиксировано! Команда работает отлично.", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(mistakeCountByCocktail) { (cocktailName, count) ->
                                FrostedCard(glowColor = CrimsonError, cornerRadius = 14.dp) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(cocktailName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        NeonBadge(text = "$count ОШИБОК", accentColor = CrimsonError)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AdminTab.RECIPES -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Button(
                        onClick = { showAddCocktailDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = ObsidianBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Добавить новую техкарту в меню", color = ObsidianBg, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Текущее меню бара (${allCocktails.size})", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(allCocktails, key = { it.id }) { cocktail ->
                            FrostedCard(
                                glowColor = if (cocktail.isCustom) ElectricMagenta else FrostedGlassBorder,
                                cornerRadius = 14.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(cocktail.name, fontWeight = FontWeight.Bold)
                                            if (cocktail.isCustom) {
                                                NeonBadge(text = "АВТОРСКИЙ НОВЫЙ", accentColor = ElectricMagenta)
                                            }
                                        }
                                        Text("${cocktail.glassware.title} • ${cocktail.method.title}", fontSize = 12.sp, color = TextSecondary)
                                    }

                                    if (cocktail.isCustom) {
                                        IconButton(onClick = { onDeleteCocktail(cocktail.id) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Удалить", tint = CrimsonError)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Inspect Bartender Dialog
    inspectedBartender?.let { user ->
        AlertDialog(
            onDismissRequest = { inspectedBartender = null },
            containerColor = DeepSlateSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(user.avatarEmoji, fontSize = 26.sp)
                    Text("Личное дело: ${user.name}")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Логин: ${user.username} | Пароль: ${user.password}", fontWeight = FontWeight.Bold, color = NeonCyan)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NeonBadge(
                            text = if (user.bestExamScore >= 85) "ДОПУЩЕН" else "НЕ ДОПУЩЕН",
                            accentColor = if (user.bestExamScore >= 85) EmeraldSuccess else CrimsonError
                        )
                        NeonBadge(text = "Аттестация: ${user.bestExamScore}%", accentColor = AmberGold)
                    }

                    Text("Освоено техкарт: ${user.masteredIds.size} из ${allCocktails.size}", color = TextPrimary, fontSize = 14.sp)
                    Text("Рекорд в режиме «Запара»: ${user.bestTimeAttackScore} очков", color = TextPrimary, fontSize = 14.sp)

                    HorizontalDivider(color = FrostedGlassBorder)

                    Text("Журнал ошибок:", fontWeight = FontWeight.Bold, color = CrimsonError)
                    if (user.recentMistakes.isEmpty()) {
                        Text("Ошибок не зарегистрировано", fontSize = 12.sp, color = TextSecondary)
                    } else {
                        user.recentMistakes.take(5).forEach { m ->
                            Text("• ${m.cocktailName}: ${m.errorDescription}", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { inspectedBartender = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Закрыть", color = ObsidianBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDeleteUser(user.id)
                        inspectedBartender = null
                    }
                ) {
                    Text("Удалить пользователя", color = CrimsonError)
                }
            }
        )
    }

    // Add Lecture Dialog
    if (showAddLectureDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Технологии") }
        var content by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddLectureDialog = false },
            containerColor = DeepSlateSurface,
            title = { Text("Публикация обучающей лекции") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Тема лекции") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Категория (например, Технологии, Стандарты)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Текст лекции / Инструкция") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            onAddLecture(
                                Lecture(
                                    id = System.currentTimeMillis().toString(),
                                    title = title.trim(),
                                    category = category.ifBlank { "Обучение" },
                                    content = content.trim(),
                                    authorName = "Александр (Шеф)"
                                )
                            )
                            showAddLectureDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                ) {
                    Text("Опубликовать", color = ObsidianBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLectureDialog = false }) {
                    Text("Отмена", color = TextSecondary)
                }
            }
        )
    }

    // Add Custom Cocktail Dialog
    if (showAddCocktailDialog) {
        var name by remember { mutableStateOf("") }
        var isSignature by remember { mutableStateOf(true) }
        var selectedGlass by remember { mutableStateOf(Glassware.WINE) }
        var selectedMethod by remember { mutableStateOf(PrepMethod.SHAKE) }
        var garnish by remember { mutableStateOf("Без украшения") }
        var ingName by remember { mutableStateOf("") }
        var ingAmount by remember { mutableStateOf("50 мл") }
        val ingredientsList = remember { mutableStateListOf<IngredientItem>() }

        AlertDialog(
            onDismissRequest = { showAddCocktailDialog = false },
            containerColor = DeepSlateSurface,
            title = { Text("Новая техкарта") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название коктейля") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = garnish,
                        onValueChange = { garnish = it },
                        label = { Text("Гарниш / Украшение") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Ингредиенты (${ingredientsList.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = ingName,
                            onValueChange = { ingName = it },
                            label = { Text("Название") },
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = ingAmount,
                            onValueChange = { ingAmount = it },
                            label = { Text("Объем") },
                            modifier = Modifier.weight(0.8f)
                        )
                        IconButton(
                            onClick = {
                                if (ingName.isNotBlank()) {
                                    ingredientsList.add(
                                        IngredientItem(ingName, ingAmount, IngredientCategory.BASE_SPIRIT, 0xFF00F5D4)
                                    )
                                    ingName = ""
                                }
                            }
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Добавить", tint = NeonCyan)
                        }
                    }

                    ingredientsList.forEach { ing ->
                        Text("• ${ing.name} — ${ing.amount}", fontSize = 12.sp, color = NeonCyan)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && ingredientsList.isNotEmpty()) {
                            val newCocktail = Cocktail(
                                id = (System.currentTimeMillis() % 100000).toInt(),
                                name = name.trim(),
                                isSignature = isSignature,
                                glassware = selectedGlass,
                                method = selectedMethod,
                                garnish = garnish.ifBlank { "Без украшения" },
                                ingredients = ingredientsList.toList(),
                                isCustom = true
                            )
                            onAddCocktail(newCocktail)
                            showAddCocktailDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricMagenta)
                ) {
                    Text("Сохранить техкарту", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCocktailDialog = false }) {
                    Text("Отмена", color = TextSecondary)
                }
            }
        )
    }
}
