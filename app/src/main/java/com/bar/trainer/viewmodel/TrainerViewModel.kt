package com.bar.trainer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bar.trainer.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    CARDS("Техкарты"),
    BUILDER("Сборка"),
    SPEED("Запара"),
    EXAM("Аттестация"),
    LECTURES("Лекции"),
    PROFILE("Профиль"),
    ADMIN_PANEL("Управление")
}

enum class CocktailFilter(val title: String) {
    ALL("Все"),
    SIGNATURE("Авторские"),
    CLASSIC("Классика")
}

data class BuilderSessionState(
    val targetCocktail: Cocktail = CocktailsDatabase.defaultCocktails.first(),
    val selectedGlassware: Glassware? = null,
    val selectedMethod: PrepMethod? = null,
    val selectedGarnish: String? = null,
    val selectedIngredients: Set<IngredientItem> = emptySet(),
    val isChecked: Boolean = false,
    val isSuccess: Boolean = false,
    val feedbackErrors: List<String> = emptyList(),
    val pendingIngredientForAmount: String? = null,
    val pendingIngredientCategory: IngredientCategory = IngredientCategory.BASE_SPIRIT
)

data class ExamQuestion(
    val cocktail: Cocktail,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
)

data class ExamSessionState(
    val isActive: Boolean = false,
    val isFinished: Boolean = false,
    val questions: List<ExamQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val correctCount: Int = 0,
    val secondsRemaining: Int = 180,
    val lastAnswerCorrect: Boolean? = null,
    val recordedMistakes: List<BartenderMistake> = emptyList()
)

class TrainerViewModel(private val repository: ProgressRepository) : ViewModel() {

    val appState: StateFlow<AppStateData> = repository.appStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppStateData())

    val activeUser: StateFlow<UserProfile?> = appState.map { it.activeUser }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allCocktails: StateFlow<List<Cocktail>> = appState.map { it.allCocktails }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CocktailsDatabase.defaultCocktails)

    val lectures: StateFlow<List<Lecture>> = appState.map { it.lectures }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(AppTab.CARDS)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _filter = MutableStateFlow(CocktailFilter.ALL)
    val filter: StateFlow<CocktailFilter> = _filter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredCocktails: StateFlow<List<Cocktail>> = combine(
        allCocktails,
        _filter,
        _searchQuery
    ) { cocktailsList, currentFilter, query ->
        cocktailsList.filter { cocktail ->
            val matchesFilter = when (currentFilter) {
                CocktailFilter.ALL -> true
                CocktailFilter.SIGNATURE -> cocktail.isSignature
                CocktailFilter.CLASSIC -> !cocktail.isSignature
            }
            val matchesQuery = query.isBlank() ||
                    cocktail.name.contains(query, ignoreCase = true) ||
                    cocktail.ingredients.any { it.name.contains(query, ignoreCase = true) }
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CocktailsDatabase.defaultCocktails)

    private val _builderState = MutableStateFlow(BuilderSessionState())
    val builderState: StateFlow<BuilderSessionState> = _builderState.asStateFlow()

    private val _examState = MutableStateFlow(ExamSessionState())
    val examState: StateFlow<ExamSessionState> = _examState.asStateFlow()

    private var timerJob: Job? = null

    val allUniqueGarnishes: List<String>
        get() = allCocktails.value.map { it.garnish }.distinct().sorted()

    val allUniqueIngredientsCatalog: List<Pair<String, IngredientCategory>>
        get() = allCocktails.value.flatMap { c -> c.ingredients.map { Pair(it.name, it.category) } }
            .distinctBy { it.first }
            .sortedBy { it.first }

    init {
        startNewBuilderRound()
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setFilter(newFilter: CocktailFilter) {
        _filter.value = newFilter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun registerUser(name: String, username: String, pass: String, role: UserRole, avatarEmoji: String) {
        viewModelScope.launch {
            val created = repository.registerUser(name, username, pass, role, avatarEmoji)
            repository.setActiveUser(created.id)
            if (role == UserRole.ADMIN) {
                _currentTab.value = AppTab.ADMIN_PANEL
            } else {
                _currentTab.value = AppTab.CARDS
            }
        }
    }

    fun setActiveUser(userId: String) {
        viewModelScope.launch {
            repository.setActiveUser(userId)
            val selected = appState.value.users.find { it.id == userId }
            if (selected?.role == UserRole.ADMIN) {
                _currentTab.value = AppTab.ADMIN_PANEL
            } else {
                _currentTab.value = AppTab.CARDS
            }
        }
    }

    fun toggleVerifyUser(userId: String) {
        viewModelScope.launch {
            repository.toggleVerifyUser(userId)
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
        }
    }

    fun addLecture(lecture: Lecture) {
        viewModelScope.launch {
            repository.addLecture(lecture)
        }
    }

    fun deleteLecture(lectureId: String) {
        viewModelScope.launch {
            repository.deleteLecture(lectureId)
        }
    }

    fun recordTimeAttackResult(score: Int) {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            repository.recordTimeAttackResult(user.id, score)
        }
    }

    fun addCustomCocktail(cocktail: Cocktail) {
        viewModelScope.launch {
            repository.addCustomCocktail(cocktail)
        }
    }

    fun deleteCustomCocktail(cocktailId: Int) {
        viewModelScope.launch {
            repository.deleteCustomCocktail(cocktailId)
        }
    }

    fun startNewBuilderRound(specificCocktail: Cocktail? = null) {
        val available = allCocktails.value.ifEmpty { CocktailsDatabase.defaultCocktails }
        val nextCocktail = specificCocktail ?: available.random()
        _builderState.value = BuilderSessionState(targetCocktail = nextCocktail)
    }

    fun selectBuilderGlassware(glassware: Glassware) {
        _builderState.update { it.copy(selectedGlassware = glassware, isChecked = false) }
    }

    fun selectBuilderMethod(method: PrepMethod) {
        _builderState.update { it.copy(selectedMethod = method, isChecked = false) }
    }

    fun selectBuilderGarnish(garnish: String) {
        _builderState.update { it.copy(selectedGarnish = garnish, isChecked = false) }
    }

    fun openIngredientAmountDialog(ingredientName: String, category: IngredientCategory) {
        _builderState.update {
            it.copy(
                pendingIngredientForAmount = ingredientName,
                pendingIngredientCategory = category
            )
        }
    }

    fun dismissIngredientAmountDialog() {
        _builderState.update { it.copy(pendingIngredientForAmount = null) }
    }

    fun confirmBuilderIngredientWithAmount(amount: String) {
        val state = _builderState.value
        val name = state.pendingIngredientForAmount ?: return
        val category = state.pendingIngredientCategory

        val existingItem = state.selectedIngredients.find { it.name == name }
        val updated = state.selectedIngredients.toMutableSet()
        if (existingItem != null) {
            updated.remove(existingItem)
        }

        val newItem = IngredientItem(
            name = name,
            amount = amount.ifBlank { "50 мл" },
            category = category,
            colorHex = 0xFF00F5D4
        )
        updated.add(newItem)

        _builderState.update {
            it.copy(
                selectedIngredients = updated,
                pendingIngredientForAmount = null,
                isChecked = false
            )
        }
    }

    fun removeBuilderIngredient(name: String) {
        _builderState.update { state ->
            val updated = state.selectedIngredients.filter { it.name != name }.toSet()
            state.copy(selectedIngredients = updated, isChecked = false)
        }
    }

    fun verifyBuilderCocktail() {
        val state = _builderState.value
        val target = state.targetCocktail
        val errors = mutableListOf<String>()

        if (state.selectedGlassware != target.glassware) {
            errors.add("Бокал: нужен «${target.glassware.title}»")
        }
        if (state.selectedMethod != target.method) {
            errors.add("Метод: нужен «${target.method.title}»")
        }
        if (state.selectedGarnish != target.garnish) {
            errors.add("Гарниш: нужен «${target.garnish}»")
        }

        val targetSet = target.ingredients.map { "${it.name}_${it.amount}" }.toSet()
        val selectedSet = state.selectedIngredients.map { "${it.name}_${it.amount}" }.toSet()

        val missing = target.ingredients.filter { !selectedSet.contains("${it.name}_${it.amount}") }
        val extra = state.selectedIngredients.filter { !targetSet.contains("${it.name}_${it.amount}") }

        if (missing.isNotEmpty()) {
            errors.add("Не хватает или неверная граммовка: ${missing.joinToString { "${it.name} (${it.amount})" }}")
        }
        if (extra.isNotEmpty()) {
            errors.add("Лишнее: ${extra.joinToString { "${it.name} (${it.amount})" }}")
        }

        val passed = errors.isEmpty()
        _builderState.update {
            it.copy(
                isChecked = true,
                isSuccess = passed,
                feedbackErrors = errors
            )
        }

        val userId = activeUser.value?.id
        if (userId != null) {
            viewModelScope.launch {
                if (passed) {
                    repository.markCocktailMastered(userId, target.id)
                } else {
                    repository.recordSingleMistake(userId, target.name, errors.firstOrNull() ?: "Ошибка в сборке")
                }
            }
        }
    }

    fun startExam() {
        val available = allCocktails.value.ifEmpty { CocktailsDatabase.defaultCocktails }
        val generatedQuestions = available.shuffled().take(15).map { cocktail ->
            when ((0..3).random()) {
                0 -> {
                    val wrong = Glassware.entries.filter { it != cocktail.glassware }.shuffled().take(3).map { it.title }
                    ExamQuestion(
                        cocktail = cocktail,
                        questionText = "В какой бокал отдается коктейль «${cocktail.name}»?",
                        options = (wrong + cocktail.glassware.title).shuffled(),
                        correctAnswer = cocktail.glassware.title
                    )
                }
                1 -> {
                    val wrong = PrepMethod.entries.filter { it != cocktail.method }.shuffled().take(3).map { it.title }
                    ExamQuestion(
                        cocktail = cocktail,
                        questionText = "Какой метод приготовления у коктейля «${cocktail.name}»?",
                        options = (wrong + cocktail.method.title).shuffled(),
                        correctAnswer = cocktail.method.title
                    )
                }
                2 -> {
                    val wrong = allUniqueGarnishes.filter { it != cocktail.garnish }.shuffled().take(3)
                    ExamQuestion(
                        cocktail = cocktail,
                        questionText = "Какое украшение (гарниш) идет в «${cocktail.name}»?",
                        options = (wrong + cocktail.garnish).shuffled(),
                        correctAnswer = cocktail.garnish
                    )
                }
                else -> {
                    val targetIng = cocktail.ingredients.random()
                    val correctStr = "${targetIng.name} — ${targetIng.amount}"
                    val wrong = allUniqueIngredientsCatalog
                        .map { it.first }
                        .filter { it != targetIng.name }
                        .shuffled()
                        .take(3)
                        .map { "$it — ${targetIng.amount}" }
                    ExamQuestion(
                        cocktail = cocktail,
                        questionText = "Какая точная граммовка входит в состав «${cocktail.name}»?",
                        options = (wrong + correctStr).shuffled(),
                        correctAnswer = correctStr
                    )
                }
            }
        }

        _examState.value = ExamSessionState(
            isActive = true,
            isFinished = false,
            questions = generatedQuestions,
            currentIndex = 0,
            correctCount = 0,
            secondsRemaining = 180,
            recordedMistakes = emptyList()
        )

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_examState.value.secondsRemaining > 0 && _examState.value.isActive && !_examState.value.isFinished) {
                delay(1000L)
                _examState.update { it.copy(secondsRemaining = it.secondsRemaining - 1) }
            }
            if (_examState.value.isActive && !_examState.value.isFinished) {
                finishExam()
            }
        }
    }

    fun submitExamAnswer(selectedOption: String) {
        val state = _examState.value
        if (!state.isActive || state.isFinished || state.lastAnswerCorrect != null) return

        val currentQ = state.questions[state.currentIndex]
        val isCorrect = selectedOption == currentQ.correctAnswer

        val newMistakes = state.recordedMistakes.toMutableList()
        if (!isCorrect) {
            newMistakes.add(
                BartenderMistake(
                    cocktailName = currentQ.cocktail.name,
                    errorDescription = "Ответил «$selectedOption» вместо «${currentQ.correctAnswer}»",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        viewModelScope.launch {
            val newCorrect = if (isCorrect) state.correctCount + 1 else state.correctCount
            _examState.update {
                it.copy(
                    correctCount = newCorrect,
                    lastAnswerCorrect = isCorrect,
                    recordedMistakes = newMistakes
                )
            }
            delay(650L)

            if (state.currentIndex + 1 < state.questions.size) {
                _examState.update {
                    it.copy(
                        currentIndex = it.currentIndex + 1,
                        lastAnswerCorrect = null
                    )
                }
            } else {
                finishExam()
            }
        }
    }

    private fun finishExam() {
        timerJob?.cancel()
        val state = _examState.value
        val total = state.questions.size.coerceAtLeast(1)
        val percent = (state.correctCount * 100) / total
        val passed = percent >= 85
        val timeSpent = 180 - state.secondsRemaining

        _examState.update {
            it.copy(
                isActive = false,
                isFinished = true,
                lastAnswerCorrect = null
            )
        }

        val userId = activeUser.value?.id ?: return
        viewModelScope.launch {
            repository.recordExamResult(
                userId = userId,
                scorePercent = percent,
                passed = passed,
                correctCount = state.correctCount,
                totalQuestions = total,
                timeSpentSeconds = timeSpent,
                mistakes = state.recordedMistakes
            )
        }
    }
}

class TrainerViewModelFactory(private val repository: ProgressRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TrainerViewModel(repository) as T
    }
}
