package com.bar.trainer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bar_attestation_v3_prefs")

data class AppStateData(
    val users: List<UserProfile> = emptyList(),
    val activeUserId: String? = null,
    val customCocktails: List<Cocktail> = emptyList(),
    val lectures: List<Lecture> = emptyList()
) {
    val activeUser: UserProfile?
        get() = users.find { it.id == activeUserId } ?: users.firstOrNull()

    val allCocktails: List<Cocktail>
        get() = CocktailsDatabase.defaultCocktails + customCocktails
}

class ProgressRepository(private val context: Context) {

    private companion object {
        val USERS_JSON_KEY = stringPreferencesKey("users_data_v3_json")
        val ACTIVE_USER_ID_KEY = stringPreferencesKey("active_user_id_v3")
        val CUSTOM_COCKTAILS_JSON_KEY = stringPreferencesKey("custom_cocktails_v3_json")
        val LECTURES_JSON_KEY = stringPreferencesKey("lectures_v3_json")
    }

    private val defaultUsers = listOf(
        UserProfile(
            id = "admin_chef",
            username = "admin",
            name = "Александр (Шеф)",
            pin = "1111",
            password = "admin123",
            role = UserRole.ADMIN,
            avatarEmoji = "👑",
            isVerified = true
        ),
        UserProfile(
            id = "bartender_ivan",
            username = "bartender",
            name = "Иван (Бармен)",
            pin = "0000",
            password = "bar123",
            role = UserRole.BARTENDER,
            avatarEmoji = "🍸",
            isVerified = true,
            masteredIds = setOf(1, 3, 9, 11, 14),
            bestExamScore = 88,
            bestTimeAttackScore = 280,
            totalPassedExams = 4,
            totalExamsTaken = 5,
            examHistory = listOf(
                ExamHistoryEntry(
                    id = "e1",
                    timestamp = System.currentTimeMillis() - 86400000L * 2,
                    scorePercent = 88,
                    passed = true,
                    correctCount = 13,
                    totalQuestions = 15,
                    timeSpentSeconds = 110,
                    mistakesSummary = listOf("Ошибся в бокале 'Космополитен'", "Путаница в граммовке 'Мохито'")
                )
            ),
            recentMistakes = listOf(
                BartenderMistake("Космополитен", "Нужен 'Коктейльная рюмка', выбран 'Рокс'", System.currentTimeMillis() - 86400000L * 2),
                BartenderMistake("Мохито", "Неверное количество мяты (15 гр)", System.currentTimeMillis() - 86400000L * 2)
            )
        )
    )

    private val defaultLectures = listOf(
        Lecture(
            id = "lec_1",
            title = "Основы методов приготовления (Build, Shake, Stir)",
            category = "Технологии",
            content = """
                1. BUILD (Билд):
                Коктейль собирается прямо в бокале подачи со льдом. Порядок: сначала крепкий алкоголь, затем сиропы/кислота, в конце содовая/игристое. Перемешивать аккуратно барной ложкой.

                2. SHAKE (Шейк):
                Используется для напитков с соками, сливками, сиропами или яичным белком. Шейкер заполняется льдом на 2/3. Интенсивный взбив 10-15 секунд до образования инея на металле.

                3. STIR (Стир):
                Метод для прозрачных крепких коктейлей (Негрони, Манхэттен). Смешивание со льдом в смесительном стакане барной ложкой в течение 20 секунд для охлаждения без лишнего разбавления.

                4. MUDDLE / FRAPPE (Мадлинг):
                Разминание ингредиентов (мята, лайм, ягоды) мадлером прямо в бокале перед добавлением дробленого льда (краш).
            """.trimIndent(),
            authorName = "Александр (Шеф)"
        ),
        Lecture(
            id = "lec_2",
            title = "Стандарты сервировки и правила работы за стойкой",
            category = "Стандарты",
            content = """
                • Всегда проверяйте чистоту и отсутствие сколов на стекле.
                • Бокалы наполняются льдом доверху (за исключением шотов и подач безо льда).
                • Гарниш (цедра, фрукты, мята) наносится непосредственно перед отдачей гостю.
                • При отдаче авторских напитков кратко презентуйте вкус и идею коктейля.
                • Держите рабочую станцию (спидрейл, мадлеры, джиггеры) в идеальной чистоте после каждого заказа.
            """.trimIndent(),
            authorName = "Александр (Шеф)"
        )
    )

    val appStateFlow: Flow<AppStateData> = context.dataStore.data.map { prefs ->
        val usersJsonStr = prefs[USERS_JSON_KEY]
        val activeUserId = prefs[ACTIVE_USER_ID_KEY] ?: "bartender_ivan"
        val customJsonStr = prefs[CUSTOM_COCKTAILS_JSON_KEY]
        val lecturesJsonStr = prefs[LECTURES_JSON_KEY]

        val users = if (usersJsonStr.isNullOrEmpty()) {
            defaultUsers
        } else {
            parseUsers(usersJsonStr)
        }

        val customCocktails = if (customJsonStr.isNullOrEmpty()) {
            emptyList()
        } else {
            parseCocktails(customJsonStr)
        }

        val lectures = if (lecturesJsonStr.isNullOrEmpty()) {
            defaultLectures
        } else {
            parseLectures(lecturesJsonStr)
        }

        AppStateData(
            users = users,
            activeUserId = activeUserId,
            customCocktails = customCocktails,
            lectures = lectures
        )
    }

    suspend fun registerUser(name: String, username: String, pass: String, role: UserRole, avatarEmoji: String): UserProfile {
        val newUser = UserProfile(
            id = UUID.randomUUID().toString(),
            username = username.ifBlank { name.lowercase().replace(" ", "_") },
            name = name,
            pin = "0000",
            password = pass.ifBlank { "123456" },
            role = role,
            avatarEmoji = avatarEmoji,
            isVerified = true
        )

        context.dataStore.edit { prefs ->
            val currentUsers = parseUsers(prefs[USERS_JSON_KEY] ?: "").toMutableList()
            if (currentUsers.isEmpty()) {
                currentUsers.addAll(defaultUsers)
            }
            currentUsers.add(newUser)
            prefs[USERS_JSON_KEY] = serializeUsers(currentUsers)
            prefs[ACTIVE_USER_ID_KEY] = newUser.id
        }

        return newUser
    }

    suspend fun setActiveUser(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[ACTIVE_USER_ID_KEY] = userId
        }
    }

    suspend fun toggleVerifyUser(userId: String) {
        updateUser(userId) { user ->
            user.copy(isVerified = !user.isVerified)
        }
    }

    suspend fun deleteUser(userId: String) {
        context.dataStore.edit { prefs ->
            val current = parseUsers(prefs[USERS_JSON_KEY] ?: "").filter { it.id != userId }
            prefs[USERS_JSON_KEY] = serializeUsers(current)
        }
    }

    suspend fun addLecture(lecture: Lecture) {
        context.dataStore.edit { prefs ->
            val current = parseLectures(prefs[LECTURES_JSON_KEY] ?: "").toMutableList()
            if (current.isEmpty()) current.addAll(defaultLectures)
            current.add(lecture)
            prefs[LECTURES_JSON_KEY] = serializeLectures(current)
        }
    }

    suspend fun deleteLecture(lectureId: String) {
        context.dataStore.edit { prefs ->
            val current = parseLectures(prefs[LECTURES_JSON_KEY] ?: "").filter { it.id != lectureId }
            prefs[LECTURES_JSON_KEY] = serializeLectures(current)
        }
    }

    suspend fun markCocktailMastered(userId: String, cocktailId: Int) {
        updateUser(userId) { user ->
            val updatedMastered = user.masteredIds + cocktailId
            user.copy(masteredIds = updatedMastered)
        }
    }

    suspend fun recordTimeAttackResult(userId: String, score: Int) {
        updateUser(userId) { user ->
            val newBest = maxOf(user.bestTimeAttackScore, score)
            user.copy(bestTimeAttackScore = newBest)
        }
    }

    suspend fun recordExamResult(
        userId: String,
        scorePercent: Int,
        passed: Boolean,
        correctCount: Int,
        totalQuestions: Int,
        timeSpentSeconds: Int,
        mistakes: List<BartenderMistake>
    ) {
        updateUser(userId) { user ->
            val newBest = maxOf(user.bestExamScore, scorePercent)
            val newTotalPassed = if (passed) user.totalPassedExams + 1 else user.totalPassedExams
            val newTotalExams = user.totalExamsTaken + 1

            val newEntry = ExamHistoryEntry(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                scorePercent = scorePercent,
                passed = passed,
                correctCount = correctCount,
                totalQuestions = totalQuestions,
                timeSpentSeconds = timeSpentSeconds,
                mistakesSummary = mistakes.map { "${it.cocktailName}: ${it.errorDescription}" }
            )

            val updatedHistory = listOf(newEntry) + user.examHistory
            val updatedMistakes = (mistakes + user.recentMistakes).take(25)

            user.copy(
                bestExamScore = newBest,
                totalPassedExams = newTotalPassed,
                totalExamsTaken = newTotalExams,
                examHistory = updatedHistory,
                recentMistakes = updatedMistakes
            )
        }
    }

    suspend fun recordSingleMistake(userId: String, cocktailName: String, description: String) {
        updateUser(userId) { user ->
            val mistake = BartenderMistake(cocktailName, description, System.currentTimeMillis())
            val updatedMistakes = (listOf(mistake) + user.recentMistakes).take(25)
            user.copy(recentMistakes = updatedMistakes)
        }
    }

    suspend fun addCustomCocktail(cocktail: Cocktail) {
        context.dataStore.edit { prefs ->
            val current = parseCocktails(prefs[CUSTOM_COCKTAILS_JSON_KEY] ?: "").toMutableList()
            current.add(cocktail)
            prefs[CUSTOM_COCKTAILS_JSON_KEY] = serializeCocktails(current)
        }
    }

    suspend fun deleteCustomCocktail(cocktailId: Int) {
        context.dataStore.edit { prefs ->
            val current = parseCocktails(prefs[CUSTOM_COCKTAILS_JSON_KEY] ?: "").filter { it.id != cocktailId }
            prefs[CUSTOM_COCKTAILS_JSON_KEY] = serializeCocktails(current)
        }
    }

    private suspend fun updateUser(userId: String, transform: (UserProfile) -> UserProfile) {
        context.dataStore.edit { prefs ->
            var currentUsers = parseUsers(prefs[USERS_JSON_KEY] ?: "")
            if (currentUsers.isEmpty()) {
                currentUsers = defaultUsers
            }

            val updatedList = currentUsers.map { user ->
                if (user.id == userId) transform(user) else user
            }

            prefs[USERS_JSON_KEY] = serializeUsers(updatedList)
        }
    }

    // JSON Helpers for Users
    private fun serializeUsers(users: List<UserProfile>): String {
        val arr = JSONArray()
        users.forEach { user ->
            val obj = JSONObject().apply {
                put("id", user.id)
                put("username", user.username)
                put("name", user.name)
                put("pin", user.pin)
                put("password", user.password)
                put("role", user.role.name)
                put("avatarEmoji", user.avatarEmoji)
                put("isVerified", user.isVerified)
                put("createdAt", user.createdAt)
                put("masteredIds", JSONArray(user.masteredIds))
                put("bestExamScore", user.bestExamScore)
                put("bestTimeAttackScore", user.bestTimeAttackScore)
                put("totalPassedExams", user.totalPassedExams)
                put("totalExamsTaken", user.totalExamsTaken)

                val historyArr = JSONArray()
                user.examHistory.forEach { h ->
                    val hObj = JSONObject().apply {
                        put("id", h.id)
                        put("timestamp", h.timestamp)
                        put("scorePercent", h.scorePercent)
                        put("passed", h.passed)
                        put("correctCount", h.correctCount)
                        put("totalQuestions", h.totalQuestions)
                        put("timeSpentSeconds", h.timeSpentSeconds)
                        put("mistakesSummary", JSONArray(h.mistakesSummary))
                    }
                    historyArr.put(hObj)
                }
                put("examHistory", historyArr)

                val mistakeArr = JSONArray()
                user.recentMistakes.forEach { m ->
                    val mObj = JSONObject().apply {
                        put("cocktailName", m.cocktailName)
                        put("errorDescription", m.errorDescription)
                        put("timestamp", m.timestamp)
                    }
                    mistakeArr.put(mObj)
                }
                put("recentMistakes", mistakeArr)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseUsers(json: String): List<UserProfile> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<UserProfile>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val masteredSet = mutableSetOf<Int>()
                val masteredArr = obj.optJSONArray("masteredIds")
                if (masteredArr != null) {
                    for (j in 0 until masteredArr.length()) {
                        masteredSet.add(masteredArr.getInt(j))
                    }
                }

                val historyList = mutableListOf<ExamHistoryEntry>()
                val historyArr = obj.optJSONArray("examHistory")
                if (historyArr != null) {
                    for (j in 0 until historyArr.length()) {
                        val hObj = historyArr.getJSONObject(j)
                        val mSumm = mutableListOf<String>()
                        val mArr = hObj.optJSONArray("mistakesSummary")
                        if (mArr != null) {
                            for (k in 0 until mArr.length()) {
                                mSumm.add(mArr.getString(k))
                            }
                        }
                        historyList.add(
                            ExamHistoryEntry(
                                id = hObj.optString("id", UUID.randomUUID().toString()),
                                timestamp = hObj.optLong("timestamp", System.currentTimeMillis()),
                                scorePercent = hObj.optInt("scorePercent", 0),
                                passed = hObj.optBoolean("passed", false),
                                correctCount = hObj.optInt("correctCount", 0),
                                totalQuestions = hObj.optInt("totalQuestions", 15),
                                timeSpentSeconds = hObj.optInt("timeSpentSeconds", 0),
                                mistakesSummary = mSumm
                            )
                        )
                    }
                }

                val mistakesList = mutableListOf<BartenderMistake>()
                val mistakeArr = obj.optJSONArray("recentMistakes")
                if (mistakeArr != null) {
                    for (j in 0 until mistakeArr.length()) {
                        val mObj = mistakeArr.getJSONObject(j)
                        mistakesList.add(
                            BartenderMistake(
                                cocktailName = mObj.optString("cocktailName", ""),
                                errorDescription = mObj.optString("errorDescription", ""),
                                timestamp = mObj.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }

                list.add(
                    UserProfile(
                        id = obj.getString("id"),
                        username = obj.optString("username", ""),
                        name = obj.getString("name"),
                        pin = obj.optString("pin", "0000"),
                        password = obj.optString("password", "123456"),
                        role = try { UserRole.valueOf(obj.getString("role")) } catch (e: Exception) { UserRole.BARTENDER },
                        avatarEmoji = obj.optString("avatarEmoji", "🍸"),
                        isVerified = obj.optBoolean("isVerified", true),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        masteredIds = masteredSet,
                        bestExamScore = obj.optInt("bestExamScore", 0),
                        bestTimeAttackScore = obj.optInt("bestTimeAttackScore", 0),
                        totalPassedExams = obj.optInt("totalPassedExams", 0),
                        totalExamsTaken = obj.optInt("totalExamsTaken", 0),
                        examHistory = historyList,
                        recentMistakes = mistakesList
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // JSON Helpers for Custom Cocktails
    private fun serializeCocktails(cocktails: List<Cocktail>): String {
        val arr = JSONArray()
        cocktails.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("isSignature", c.isSignature)
                put("glassware", c.glassware.name)
                put("method", c.method.name)
                put("garnish", c.garnish)
                put("isCustom", true)

                val ingArr = JSONArray()
                c.ingredients.forEach { ing ->
                    val ingObj = JSONObject().apply {
                        put("name", ing.name)
                        put("amount", ing.amount)
                        put("category", ing.category.name)
                        put("colorHex", ing.colorHex)
                    }
                    ingArr.put(ingObj)
                }
                put("ingredients", ingArr)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseCocktails(json: String): List<Cocktail> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<Cocktail>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val ingList = mutableListOf<IngredientItem>()
                val ingArr = obj.optJSONArray("ingredients")
                if (ingArr != null) {
                    for (j in 0 until ingArr.length()) {
                        val ingObj = ingArr.getJSONObject(j)
                        ingList.add(
                            IngredientItem(
                                name = ingObj.getString("name"),
                                amount = ingObj.getString("amount"),
                                category = try { IngredientCategory.valueOf(ingObj.getString("category")) } catch (e: Exception) { IngredientCategory.BASE_SPIRIT },
                                colorHex = ingObj.optLong("colorHex", 0xFF00F5D4)
                            )
                        )
                    }
                }

                list.add(
                    Cocktail(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        isSignature = obj.optBoolean("isSignature", false),
                        glassware = try { Glassware.valueOf(obj.getString("glassware")) } catch (e: Exception) { Glassware.ROCKS },
                        method = try { PrepMethod.valueOf(obj.getString("method")) } catch (e: Exception) { PrepMethod.SHAKE },
                        garnish = obj.optString("garnish", "Без украшения"),
                        ingredients = ingList,
                        isCustom = true
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // JSON Helpers for Lectures
    private fun serializeLectures(lectures: List<Lecture>): String {
        val arr = JSONArray()
        lectures.forEach { l ->
            val obj = JSONObject().apply {
                put("id", l.id)
                put("title", l.title)
                put("category", l.category)
                put("content", l.content)
                put("authorName", l.authorName)
                put("createdAt", l.createdAt)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseLectures(json: String): List<Lecture> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<Lecture>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Lecture(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        category = obj.optString("category", "Обучение"),
                        content = obj.getString("content"),
                        authorName = obj.optString("authorName", "Шеф-бармен"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
