package com.bar.trainer.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bar.trainer.data.*
import com.bar.trainer.ui.components.CocktailGlassVisualizer
import com.bar.trainer.ui.components.FrostedCard
import com.bar.trainer.ui.components.NeonBadge
import com.bar.trainer.ui.theme.*
import com.bar.trainer.viewmodel.*
import java.util.Locale

@Composable
fun MainScreen(viewModel: TrainerViewModel) {
    val appState by viewModel.appState.collectAsState()
    val activeUser by viewModel.activeUser.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val allCocktails by viewModel.allCocktails.collectAsState()
    val lectures by viewModel.lectures.collectAsState()

    var showAuthScreen by remember { mutableStateOf(false) }

    if (showAuthScreen || (activeUser == null)) {
        AuthScreen(
            users = appState.users,
            activeUserId = appState.activeUserId,
            onSelectUser = { userId ->
                viewModel.setActiveUser(userId)
            },
            onRegisterUser = { name, username, pass, role, avatar ->
                viewModel.registerUser(name, username, pass, role, avatar)
            },
            onContinue = {
                showAuthScreen = false
            }
        )
        return
    }

    val user = activeUser!!

    Scaffold(
        containerColor = ObsidianBg,
        topBar = {
            BarTopHeader(
                user = user,
                totalCocktailsCount = allCocktails.size
            )
        },
        bottomBar = {
            BarBottomNav(
                currentRole = user.role,
                currentTab = currentTab,
                onTabSelected = viewModel::selectTab
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1A00F5D4), ObsidianBg),
                        radius = 900f
                    )
                )
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    AppTab.CARDS -> TechCardsScreen(viewModel, user)
                    AppTab.BUILDER -> CocktailBuilderScreen(viewModel)
                    AppTab.SPEED -> TimeAttackScreen(
                        allCocktails = allCocktails,
                        bestScore = user.bestTimeAttackScore,
                        onFinishRound = { score -> viewModel.recordTimeAttackResult(score) }
                    )
                    AppTab.EXAM -> ExamScreen(viewModel)
                    AppTab.LECTURES -> LecturesScreen(lectures = lectures)
                    AppTab.PROFILE -> UserProfileScreen(
                        user = user,
                        totalCocktailsCount = allCocktails.size,
                        onLogout = { showAuthScreen = true }
                    )
                    AppTab.ADMIN_PANEL -> AdminDashboardScreen(
                        users = appState.users,
                        allCocktails = allCocktails,
                        lectures = lectures,
                        onAddCocktail = { viewModel.addCustomCocktail(it) },
                        onDeleteCocktail = { viewModel.deleteCustomCocktail(it) },
                        onAddLecture = { viewModel.addLecture(it) },
                        onDeleteLecture = { viewModel.deleteLecture(it) },
                        onToggleVerifyUser = { viewModel.toggleVerifyUser(it) },
                        onDeleteUser = { viewModel.deleteUser(it) },
                        onLogout = { showAuthScreen = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun BarTopHeader(
    user: UserProfile,
    totalCocktailsCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(user.role.badgeColorHex).copy(alpha = 0.2f))
                    .border(1.5.dp, Color(user.role.badgeColorHex), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(user.avatarEmoji, fontSize = 18.sp)
            }

            Column {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "BAR • ${user.role.title.uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(user.role.badgeColorHex),
                    fontSize = 10.sp
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (user.role == UserRole.BARTENDER) {
                NeonBadge(
                    text = "${user.masteredIds.size}/$totalCocktailsCount КАРТ",
                    accentColor = EmeraldSuccess
                )
                NeonBadge(
                    text = "РЕКОРД ${user.bestExamScore}%",
                    accentColor = AmberGold
                )
            } else {
                NeonBadge(
                    text = "ШЕФ-БАР",
                    accentColor = ElectricMagenta
                )
            }
        }
    }
}

@Composable
private fun BarBottomNav(
    currentRole: UserRole,
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = DeepSlateSurface.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        if (currentRole == UserRole.ADMIN) {
            NavigationBarItem(
                selected = currentTab == AppTab.ADMIN_PANEL,
                onClick = { onTabSelected(AppTab.ADMIN_PANEL) },
                icon = { Icon(Icons.Rounded.Shield, contentDescription = null) },
                label = { Text("Панель", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ObsidianBg,
                    selectedTextColor = ElectricMagenta,
                    indicatorColor = ElectricMagenta,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }

        NavigationBarItem(
            selected = currentTab == AppTab.CARDS,
            onClick = { onTabSelected(AppTab.CARDS) },
            icon = { Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null) },
            label = { Text("Карты", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ObsidianBg,
                selectedTextColor = NeonCyan,
                indicatorColor = NeonCyan,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        NavigationBarItem(
            selected = currentTab == AppTab.BUILDER,
            onClick = { onTabSelected(AppTab.BUILDER) },
            icon = { Icon(Icons.Rounded.LocalBar, contentDescription = null) },
            label = { Text("Сборка", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ObsidianBg,
                selectedTextColor = ElectricMagenta,
                indicatorColor = ElectricMagenta,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        NavigationBarItem(
            selected = currentTab == AppTab.LECTURES,
            onClick = { onTabSelected(AppTab.LECTURES) },
            icon = { Icon(Icons.Rounded.Book, contentDescription = null) },
            label = { Text("Лекции", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ObsidianBg,
                selectedTextColor = AmberGold,
                indicatorColor = AmberGold,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            )
        )

        if (currentRole == UserRole.BARTENDER) {
            NavigationBarItem(
                selected = currentTab == AppTab.SPEED,
                onClick = { onTabSelected(AppTab.SPEED) },
                icon = { Icon(Icons.Rounded.Bolt, contentDescription = null) },
                label = { Text("Запара", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ObsidianBg,
                    selectedTextColor = AmberGold,
                    indicatorColor = AmberGold,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )

            NavigationBarItem(
                selected = currentTab == AppTab.EXAM,
                onClick = { onTabSelected(AppTab.EXAM) },
                icon = { Icon(Icons.Rounded.Timer, contentDescription = null) },
                label = { Text("Экзамен", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ObsidianBg,
                    selectedTextColor = EmeraldSuccess,
                    indicatorColor = EmeraldSuccess,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )

            NavigationBarItem(
                selected = currentTab == AppTab.PROFILE,
                onClick = { onTabSelected(AppTab.PROFILE) },
                icon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                label = { Text("Профиль", fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ObsidianBg,
                    selectedTextColor = NeonCyan,
                    indicatorColor = NeonCyan,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}

@Composable
private fun TechCardsScreen(viewModel: TrainerViewModel, user: UserProfile) {
    val cocktails by viewModel.filteredCocktails.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedCocktail by remember { mutableStateOf<Cocktail?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::setSearchQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Поиск по названию или ингредиенту...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = NeonCyan) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = FrostedGlassBorder,
                focusedContainerColor = FrostedGlassFill,
                unfocusedContainerColor = FrostedGlassFill
            )
        )

        LazyRow(
            modifier = Modifier.padding(vertical = 10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CocktailFilter.entries) { filter ->
                FilterChip(
                    selected = currentFilter == filter,
                    onClick = { viewModel.setFilter(filter) },
                    label = { Text(filter.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                        selectedLabelColor = NeonCyan
                    )
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cocktails, key = { it.id }) { cocktail ->
                val isMastered = user.masteredIds.contains(cocktail.id)
                FrostedCard(
                    glowColor = if (cocktail.isSignature) ElectricMagenta else NeonCyan,
                    onClick = { selectedCocktail = cocktail }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = cocktail.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isMastered) {
                                    Icon(
                                        imageVector = Icons.Rounded.Verified,
                                        contentDescription = null,
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                NeonBadge(
                                    text = if (cocktail.isSignature) "АВТОРСКИЙ" else "КЛАССИКА",
                                    accentColor = if (cocktail.isSignature) ElectricMagenta else NeonCyan
                                )
                                NeonBadge(text = cocktail.method.title, accentColor = AmberGold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${cocktail.glassware.title} • Гарниш: ${cocktail.garnish}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        CocktailGlassVisualizer(
                            ingredients = cocktail.ingredients,
                            modifier = Modifier.size(width = 64.dp, height = 88.dp)
                        )
                    }
                }
            }
        }
    }

    selectedCocktail?.let { cocktail ->
        AlertDialog(
            onDismissRequest = { selectedCocktail = null },
            containerColor = DeepSlateSurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(cocktail.name, style = MaterialTheme.typography.titleLarge)
                    NeonBadge(
                        text = cocktail.method.title,
                        accentColor = AmberGold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Подача: ${cocktail.glassware.title}", color = NeonCyan, fontWeight = FontWeight.SemiBold)
                            Text("Украшение: ${cocktail.garnish}", color = TextSecondary)
                        }
                        CocktailGlassVisualizer(ingredients = cocktail.ingredients)
                    }
                    HorizontalDivider(color = FrostedGlassBorder)
                    cocktail.ingredients.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(item.colorHex).copy(alpha = 0.9f))
                                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(item.name, color = TextPrimary)
                            }
                            Text(
                                text = item.amount,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.startNewBuilderRound(cocktail)
                        selectedCocktail = null
                        viewModel.selectTab(AppTab.BUILDER)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricMagenta)
                ) {
                    Text("Отработать сборку", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCocktail = null }) {
                    Text("Закрыть", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun CocktailBuilderScreen(viewModel: TrainerViewModel) {
    val state by viewModel.builderState.collectAsState()
    var selectedCategory by remember { mutableStateOf(IngredientCategory.BASE_SPIRIT) }

    // Preset Amount Dialog options
    val presetAmounts = listOf("10 мл", "15 мл", "20 мл", "25 мл", "30 мл", "40 мл", "50 мл", "75 мл", "100 мл", "120 мл", "150 мл", "1 шт", "3 дольки")
    var customAmountText by remember { mutableStateOf("50 мл") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FrostedCard(glowColor = ElectricMagenta) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ЗАКАЗ ГОСТЯ", style = MaterialTheme.typography.labelMedium, color = ElectricMagenta)
                        Text(state.targetCocktail.name, style = MaterialTheme.typography.headlineLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Собери точный состав, метод, бокал и гарниш",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    CocktailGlassVisualizer(ingredients = state.selectedIngredients.toList())
                }
            }
        }

        item {
            Text("1. Бокал подачи", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Glassware.entries) { glass ->
                    val selected = state.selectedGlassware == glass
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectBuilderGlassware(glass) },
                        label = { Text(glass.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                            selectedLabelColor = NeonCyan
                        )
                    )
                }
            }
        }

        item {
            Text("2. Метод приготовления", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PrepMethod.entries) { method ->
                    val selected = state.selectedMethod == method
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectBuilderMethod(method) },
                        label = { Text(method.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberGold.copy(alpha = 0.25f),
                            selectedLabelColor = AmberGold
                        )
                    )
                }
            }
        }

        item {
            Text("3. Украшение (Гарниш)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.allUniqueGarnishes) { garnish ->
                    val selected = state.selectedGarnish == garnish
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectBuilderGarnish(garnish) },
                        label = { Text(garnish) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricMagenta.copy(alpha = 0.25f),
                            selectedLabelColor = ElectricMagenta
                        )
                    )
                }
            }
        }

        item {
            Text("4. Ингредиенты (Нажми на название для выбора граммовки)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(IngredientCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.title) }
                    )
                }
            }
        }

        // Ingredient catalog buttons (WITHOUT PREFILLED AMOUNTS!)
        val categoryItems = viewModel.allUniqueIngredientsCatalog.filter { it.second == selectedCategory }
        items(categoryItems) { (ingName, cat) ->
            val addedItem = state.selectedIngredients.find { it.name == ingName }
            val isSelected = addedItem != null

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) NeonCyan.copy(alpha = 0.16f) else FrostedGlassFill)
                    .border(
                        1.dp,
                        if (isSelected) NeonCyan else FrostedGlassBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        customAmountText = addedItem?.amount ?: "50 мл"
                        viewModel.openIngredientAmountDialog(ingName, cat)
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ingName,
                    color = TextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.titleMedium
                )

                if (isSelected) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NeonBadge(text = addedItem.amount, accentColor = NeonCyan)
                        IconButton(
                            onClick = { viewModel.removeBuilderIngredient(ingName) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Удалить", tint = CrimsonError)
                        }
                    }
                } else {
                    Text("+ Выбрать объем", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        if (state.isChecked) {
            item {
                FrostedCard(glowColor = if (state.isSuccess) EmeraldSuccess else CrimsonError) {
                    if (state.isSuccess) {
                        Text(
                            text = "ИДЕАЛЬНАЯ ПОДАЧА! Техкарта сдана на 100%.",
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Ошибки в отдаче:",
                            color = CrimsonError,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        state.feedbackErrors.forEach { err ->
                            Text("• $err", color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.startNewBuilderRound() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Следующий", color = TextPrimary)
                }
                Button(
                    onClick = { viewModel.verifyBuilderCocktail() },
                    modifier = Modifier.weight(1.4f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("ОТДАТЬ ЗАКАЗ", color = ObsidianBg, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }

    // Ingredient Amount Popup Dialog
    state.pendingIngredientForAmount?.let { ingName ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissIngredientAmountDialog() },
            containerColor = DeepSlateSurface,
            title = {
                Text("Укажите объем: $ingName", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Выберите стандартную дозировку или введите вручную:", fontSize = 13.sp, color = TextSecondary)

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presetAmounts) { amount ->
                            val isSelected = customAmountText == amount
                            FilterChip(
                                selected = isSelected,
                                onClick = { customAmountText = amount },
                                label = { Text(amount) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan.copy(alpha = 0.3f),
                                    selectedLabelColor = NeonCyan
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = { customAmountText = it },
                        label = { Text("Количество / Объем") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmBuilderIngredientWithAmount(customAmountText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Добавить в коктейль", color = ObsidianBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissIngredientAmountDialog() }) {
                    Text("Отмена", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ExamScreen(viewModel: TrainerViewModel) {
    val state by viewModel.examState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            !state.isActive && !state.isFinished -> {
                FrostedCard(glowColor = AmberGold, modifier = Modifier.fillMaxWidth()) {
                    Text("АТТЕСТАЦИЯ БАРМЕНА", style = MaterialTheme.typography.labelMedium, color = AmberGold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Блиц-экзамен на знание карты", style = MaterialTheme.typography.headlineLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• 15 случайных вопросов по авторским и классическим коктейлям\n" +
                                "• 3 минуты на всю смену\n" +
                                "• Проходной балл для допуска за стойку: 85%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.startExam() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                    ) {
                        Text("НАЧАТЬ АТТЕСТАЦИЮ", color = ObsidianBg, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            state.isActive -> {
                val question = state.questions[state.currentIndex]
                val minutes = state.secondsRemaining / 60
                val seconds = state.secondsRemaining % 60

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeonBadge(
                        text = "ВОПРОС ${state.currentIndex + 1} / ${state.questions.size}",
                        accentColor = NeonCyan
                    )
                    NeonBadge(
                        text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                        accentColor = if (state.secondsRemaining < 30) CrimsonError else AmberGold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                FrostedCard(glowColor = NeonCyan, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = question.questionText,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                question.options.forEach { option ->
                    val borderColor = when {
                        state.lastAnswerCorrect != null && option == question.correctAnswer -> EmeraldSuccess
                        state.lastAnswerCorrect == false -> CrimsonError.copy(alpha = 0.5f)
                        else -> FrostedGlassBorder
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(FrostedGlassFill)
                            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                            .clickable { viewModel.submitExamAnswer(option) }
                            .padding(16.dp)
                    ) {
                        Text(option, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            else -> {
                val percent = (state.correctCount * 100) / state.questions.size.coerceAtLeast(1)
                val passed = percent >= 85

                FrostedCard(
                    glowColor = if (passed) EmeraldSuccess else CrimsonError,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (passed) "АТТЕСТАЦИЯ СДАНА" else "ОТПРАВЛЕН НА ПЕРЕСДАЧУ",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (passed) EmeraldSuccess else CrimsonError
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Результат: $percent% (${state.correctCount} из ${state.questions.size})",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (passed) {
                            "Отличная скорость и точность! Допуск к бару подтвержден."
                        } else {
                            "Для допуска нужно минимум 85%. Повтори техкарты в режиме сборки."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.startExam() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("ПРОЙТИ ЕЩЕ РАЗ", color = ObsidianBg, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
