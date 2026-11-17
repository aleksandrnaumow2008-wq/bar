package com.bar.trainer.data

enum class Glassware(val title: String) {
    WINE("Винка"),
    ROCKS("Рокс"),
    THIN_ROCKS("Тонкий рокс"),
    GOBLET("Кубок"),
    SHOT_60("Шот 60 мл"),
    COCKTAIL_GLASS("Коктейльная рюмка"),
    HIGHBALL("Хайбол"),
    CHALICE("Шале")
}

enum class PrepMethod(val title: String) {
    BUILD("Билд"),
    SHAKE("Шейк"),
    STIR("Стир"),
    LAYERING("Лейеринг"),
    MUDDLE_FRAPPE("Мадлинг / Фраппе")
}

enum class IngredientCategory(val title: String) {
    BASE_SPIRIT("Алкоголь и Настойки"),
    LIQUEUR_VERMOUTH("Ликеры и Вермуты"),
    SYRUP_ACID("Сиропы и Кислота"),
    JUICE_MIXER("Соки, Содовая и Прочее")
}

data class IngredientItem(
    val name: String,
    val amount: String,
    val category: IngredientCategory,
    val colorHex: Long
)

data class Cocktail(
    val id: Int,
    val name: String,
    val isSignature: Boolean,
    val glassware: Glassware,
    val method: PrepMethod,
    val garnish: String,
    val ingredients: List<IngredientItem>,
    val isCustom: Boolean = false
)

object CocktailsDatabase {
    val defaultCocktails = listOf(
        Cocktail(
            id = 1, name = "Аура Неона", isSignature = true,
            glassware = Glassware.HIGHBALL, method = PrepMethod.BUILD, garnish = "Цедра лайма",
            ingredients = listOf(
                IngredientItem("Водка хаус", "40 мл", IngredientCategory.BASE_SPIRIT, 0x22FFFFFF),
                IngredientItem("Сироп блю кюрасао", "15 мл", IngredientCategory.SYRUP_ACID, 0xFF0288D1),
                IngredientItem("Кислота", "15 мл", IngredientCategory.SYRUP_ACID, 0x44FFF59D),
                IngredientItem("Сок маракуйя", "50 мл", IngredientCategory.JUICE_MIXER, 0xFFFFA000),
                IngredientItem("Содовая", "100 мл", IngredientCategory.JUICE_MIXER, 0x33FFFFFF)
            )
        ),
        Cocktail(
            id = 2, name = "Бархатный Шелкопряд", isSignature = true,
            glassware = Glassware.COCKTAIL_GLASS, method = PrepMethod.SHAKE, garnish = "Коктейльная вишня",
            ingredients = listOf(
                IngredientItem("Ром хаус", "40 мл", IngredientCategory.BASE_SPIRIT, 0xFFD4AC0D),
                IngredientItem("Ликер маракуйя", "20 мл", IngredientCategory.LIQUEUR_VERMOUTH, 0xFFFFB74D),
                IngredientItem("Сироп ваниль", "10 мл", IngredientCategory.SYRUP_ACID, 0xFFFFE082),
                IngredientItem("Сироп персик", "15 мл", IngredientCategory.SYRUP_ACID, 0xFFFFCC80),
                IngredientItem("Сливки", "30 мл", IngredientCategory.JUICE_MIXER, 0xFFFFFDE7)
            )
        ),
        Cocktail(
            id = 3, name = "Флирт", isSignature = true,
            glassware = Glassware.WINE, method = PrepMethod.BUILD, garnish = "Губы карамель кц",
            ingredients = listOf(
                IngredientItem("Пич ликер", "50 мл", IngredientCategory.LIQUEUR_VERMOUTH, 0xFFFFB74D),
                IngredientItem("Сироп орхидея", "25 мл", IngredientCategory.SYRUP_ACID, 0xFFCE93D8),
                IngredientItem("Содовая", "30 мл", IngredientCategory.JUICE_MIXER, 0x33FFFFFF),
                IngredientItem("Игристое хаус", "100 мл", IngredientCategory.BASE_SPIRIT, 0xFFF9E79F)
            )
        ),
        Cocktail(
            id = 4, name = "Игривость", isSignature = true,
            glassware = Glassware.WINE, method = PrepMethod.BUILD, garnish = "Мармелад кц",
            ingredients = listOf(
                IngredientItem("Настойка матрешки", "50 мл", IngredientCategory.BASE_SPIRIT, 0xFFE57373),
                IngredientItem("Сироп баблгам", "20 мл", IngredientCategory.SYRUP_ACID, 0xFFFF80AB),
                IngredientItem("Кислота", "10 мл", IngredientCategory.SYRUP_ACID, 0x44FFF59D),
                IngredientItem("Содовая", "30 мл", IngredientCategory.JUICE_MIXER, 0x33FFFFFF),
                IngredientItem("Игристое хаус", "75 мл", IngredientCategory.BASE_SPIRIT, 0xFFF9E79F)
            )
        ),
        Cocktail(
            id = 5, name = "Эйфория", isSignature = true,
            glassware = Glassware.GOBLET, method = PrepMethod.SHAKE, garnish = "Круста из шипучки",
            ingredients = listOf(
                IngredientItem("Водка хаус", "30 мл", IngredientCategory.BASE_SPIRIT, 0x22FFFFFF),
                IngredientItem("Пич ликер", "30 мл", IngredientCategory.LIQUEUR_VERMOUTH, 0xFFFFB74D),
                IngredientItem("Ликер декасис", "30 мл", IngredientCategory.LIQUEUR_VERMOUTH, 0xFF4A148C),
                IngredientItem("Кислота", "10 мл", IngredientCategory.SYRUP_ACID, 0x44FFF59D),
                IngredientItem("Сахарный сироп", "15 мл", IngredientCategory.SYRUP_ACID, 0x44FFFFFF)
            )
        ),
        Cocktail(
            id = 6, name = "Магический Сапфир", isSignature = true,
            glassware = Glassware.HIGHBALL, method = PrepMethod.BUILD, garnish = "Долька лимона",
            ingredients = listOf(
                IngredientItem("Джин хаус", "50 мл", IngredientCategory.BASE_SPIRIT, 0x22FFFFFF),
                IngredientItem("Ликер блю кюрасао", "15 мл", IngredientCategory.LIQUEUR_VERMOUTH, 0xFF0288D1),
                IngredientItem("Кислота", "15 мл", IngredientCategory.SYRUP_ACID, 0x44FFF59D),
                IngredientItem("Тоник", "120 мл", IngredientCategory.JUICE_MIXER, 0x33E0F7FA)
            )
        ),
        Cocktail(
            id = 7, name = "Момент", isSignature = true,
            glassware = Glassware.GOBLET, method = PrepMethod.SHAKE, garnish = "Бабочка изомальт",
            ingredients = listOf(
                IngredientItem("Джин хаус", "50 мл", IngredientCategory.BASE_SPIRIT, 0x22FFFFFF),
                IngredientItem("Сироп табак", "15 мл", IngredientCategory.SYRUP_ACID, 0xFF6D4C41),
                IngredientItem("Сироп ваниль", "5 мл", IngredientCategory.SYRUP_ACID, 0xFFFFE082),
                IngredientItem("Сироп бузина", "10 мл", IngredientCategory.SYRUP_ACID, 0xFFF0F4C3),
                IngredientItem("Гренадин", "5 мл", IngredientCategory.SYRUP_ACID, 0xFFD32F2F),
                IngredientItem("Кислота", "20 мл", IngredientCategory.SYRUP_ACID, 0x44FFF59D),
                IngredientItem("Тоник", "30 мл", IngredientCategory.JUICE_MIXER, 0x33E0F7FA)
            )
        ),
        Cocktail(
            id = 8, name = "Наслаждение", isSignature = true,
            glassware = Glassware.THIN_ROCKS, method = PrepMethod.SHAKE, garnish = "Чипс ананаса",
            ingredients = listOf(
                IngredientItem("Настойка маракуйя", "50 мл", IngredientCategory.BASE_SPIRIT, 0xFFFFA000),
                IngredientItem("Джин хаус", "40 мл", IngredientCategory.BASE_SPIRIT, 0x22FFFFFF),
                IngredientItem("Сироп гуава", "25 мл", IngredientCategory.SYRUP_ACID, 0xFFF48FB1),
                IngredientItem("Кислота", "25 мл", IngredientCategory.SYRUP_ACID, 0x44FFF59D)
            )
        ),
        Cocktail(
            id = 9, name = "Апероль Шприц", isSignature = false,
            glassware = Glassware.WINE, method = PrepMethod.BUILD, garnish = "Долька апельсина",
            ingredients = listOf(
                IngredientItem("Ликер Апероль", "50 мл", IngredientCategory.LIQUEUR_VERMOUTH, 0xFFFF5722),
                IngredientItem("Содовая", "30 мл", IngredientCategory.JUICE_MIXER, 0x33FFFFFF),
                IngredientItem("Игристое хаус", "100 мл", IngredientCategory.BASE_SPIRIT, 0xFFF9E79F)
            )
        ),
        Cocktail(
            id = 10, name = "Маргарита", isSignature = false,
            glassware = Glassware.CHALICE, method = PrepMethod.SHAKE, garnish = "Круста соли, долька лайма",
            ingredients = listOf(
                IngredientItem("Текила хаус", "50 мл", IngredientCategory.BASE_SPIRIT, 0x22FFFFFF),
                IngredientItem("Трипл сек", "20 мл", IngredientCategory.LIQUEUR_VERMOUTH, 0x33FFFFFF),
                IngredientItem("Сок лайма", "25 мл", IngredientCategory.JUICE_MIXER, 0xFF7CB342),
                IngredientItem("Сахарный сироп", "10 мл", IngredientCategory.SYRUP_ACID, 0x44FFFFFF)
            )
        ),
        Cocktail(
            id = 11, name = "Негрони", isSignature = false,
            glassware = Glassware.ROCKS, method = PrepMethod.STIR, garnish = "Цедра апельсина",
            ingredients = listOf(
                IngredientItem("Джин хаус", "30 мл", IngredientCategory.BASE_SPIRIT, 0x22FFFFFF),
                IngredientItem("Кампари", "30 мл", IngredientCategory.LIQUEUR_VERMOUTH, 0xFFB71C1C),
                IngredientItem("Мартини Россо", "30 мл", IngredientCategory.LIQUEUR_VERMOUTH, 0xFF4A0E0E)
            )
        ),
        Cocktail(
            id = 12, name = "Космополитен", isSignature = false,
            glassware = Glassware.COCKTAIL_GLASS, method = PrepMethod.SHAKE, garnish = "Цедра лимона",
            ingredients = listOf(
                IngredientItem("Водка хаус", "40 мл", IngredientCategory.BASE_SPIRIT, 0x22FFFFFF),
                IngredientItem("Трипл сек", "15 мл", IngredientCategory.LIQUEUR_VERMOUTH, 0x33FFFFFF),
                IngredientItem("Кислота", "15 мл", IngredientCategory.SYRUP_ACID, 0x44FFF59D),
                IngredientItem("Сахарный сироп", "15 мл", IngredientCategory.SYRUP_ACID, 0x44FFFFFF),
                IngredientItem("Морс сок", "50 мл", IngredientCategory.JUICE_MIXER, 0xFFC62828)
            )
        ),
        Cocktail(
            id = 13, name = "Мохито", isSignature = false,
            glassware = Glassware.HIGHBALL, method = PrepMethod.MUDDLE_FRAPPE, garnish = "Веточка мяты",
            ingredients = listOf(
                IngredientItem("Ром хаус", "40 мл", IngredientCategory.BASE_SPIRIT, 0x22FFFFFF),
                IngredientItem("Сахарный сироп", "20 мл", IngredientCategory.SYRUP_ACID, 0x44FFFFFF),
                IngredientItem("Мята", "15 гр", IngredientCategory.JUICE_MIXER, 0xFF2E7D32),
                IngredientItem("Лайм", "3 дольки", IngredientCategory.JUICE_MIXER, 0xFF7CB342),
                IngredientItem("Содовая", "100 мл", IngredientCategory.JUICE_MIXER, 0x33FFFFFF)
            )
        ),
        Cocktail(
            id = 14, name = "Пина Колада", isSignature = false,
            glassware = Glassware.HIGHBALL, method = PrepMethod.SHAKE, garnish = "Коктейльная вишня",
            ingredients = listOf(
                IngredientItem("Ром хаус", "50 мл", IngredientCategory.BASE_SPIRIT, 0x22FFFFFF),
                IngredientItem("Сливки", "50 мл", IngredientCategory.JUICE_MIXER, 0xFFFFFDE7),
                IngredientItem("Сироп кокос", "20 мл", IngredientCategory.SYRUP_ACID, 0xFFFFFFFF),
                IngredientItem("Сок ананас", "120 мл", IngredientCategory.JUICE_MIXER, 0xFFFFEE58)
            )
        ),
        Cocktail(
            id = 15, name = "Виски Сауэр", isSignature = false,
            glassware = Glassware.ROCKS, method = PrepMethod.SHAKE, garnish = "Долька лимона",
            ingredients = listOf(
                IngredientItem("Виски хаус", "50 мл", IngredientCategory.BASE_SPIRIT, 0xFFB76E00),
                IngredientItem("Кислота", "25 мл", IngredientCategory.SYRUP_ACID, 0x44FFF59D),
                IngredientItem("Сахарный сироп", "20 мл", IngredientCategory.SYRUP_ACID, 0x44FFFFFF),
                IngredientItem("Белок", "20 мл", IngredientCategory.JUICE_MIXER, 0xFFFFFDE7)
            )
        ),
        Cocktail(
            id = 16, name = "Эспрессо Мартини", isSignature = false,
            glassware = Glassware.COCKTAIL_GLASS, method = PrepMethod.SHAKE, garnish = "Зерна кофе",
            ingredients = listOf(
                IngredientItem("Водка хаус", "40 мл", IngredientCategory.BASE_SPIRIT, 0x22FFFFFF),
                IngredientItem("Кофейный ликер", "20 мл", IngredientCategory.LIQUEUR_VERMOUTH, 0xFF3E2723),
                IngredientItem("Кофе эспрессо", "30 мл", IngredientCategory.JUICE_MIXER, 0xFF4E342E),
                IngredientItem("Сахарный сироп", "10 мл", IngredientCategory.SYRUP_ACID, 0x44FFFFFF)
            )
        )
    )

    val allCocktails: List<Cocktail> get() = defaultCocktails
}
