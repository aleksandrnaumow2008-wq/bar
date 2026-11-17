package com.bar.trainer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bar.trainer.data.UserProfile
import com.bar.trainer.data.UserRole
import com.bar.trainer.ui.components.FrostedCard
import com.bar.trainer.ui.theme.*

@Composable
fun AuthScreen(
    users: List<UserProfile>,
    activeUserId: String?,
    onSelectUser: (String) -> Unit,
    onRegisterUser: (name: String, username: String, pass: String, role: UserRole, avatar: String) -> Unit,
    onContinue: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }

    // Direct Login Form State
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Registration Form State
    var regName by remember { mutableStateOf("") }
    var regUsername by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf(UserRole.BARTENDER) }
    var regAvatar by remember { mutableStateOf("🍸") }
    var regError by remember { mutableStateOf<String?>(null) }

    val emojis = listOf("🍸", "🍹", "🥂", "🥃", "🍾", "👑", "🔥", "⚡", "🍋", "🍒")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ObsidianBg, DeepSlateSurface, Color(0xFF0D111A))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(2.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isRegisterMode) "👤" else "🍸", fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isRegisterMode) "РЕГИСТРАЦИЯ СОТРУДНИКА" else "BAR ATTESTATION TRAINER",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan,
                    letterSpacing = 2.sp
                )

                Text(
                    text = if (isRegisterMode) "Создание личного аккаунта" else "Вход по логину и паролю",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }

            if (!isRegisterMode) {
                // LOGIN FORM ONLY (NO QUICK SWITCH)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    FrostedCard(glowColor = NeonCyan, cornerRadius = 18.dp) {
                        Text("Авторизация сотрудника", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = {
                                loginUsername = it
                                loginError = null
                            },
                            label = { Text("Логин") },
                            placeholder = { Text("admin / bartender", color = TextSecondary) },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = NeonCyan) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = {
                                loginPassword = it
                                loginError = null
                            },
                            label = { Text("Пароль") },
                            placeholder = { Text("admin123 / bar123", color = TextSecondary) },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = NeonCyan) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        loginError?.let { err ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(err, color = CrimsonError, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val found = users.find {
                                    it.username.equals(loginUsername.trim(), ignoreCase = true) &&
                                            (it.password == loginPassword || it.pin == loginPassword)
                                }
                                if (found != null) {
                                    onSelectUser(found.id)
                                    onContinue()
                                } else {
                                    loginError = "Неверный логин или пароль"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("ВОЙТИ В СИСТЕМУ", color = ObsidianBg, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = {
                            isRegisterMode = true
                            regError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Зарегистрировать нового сотрудника")
                    }
                }
            } else {
                // REGISTRATION FORM
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    FrostedCard(glowColor = NeonCyan, cornerRadius = 18.dp) {
                        Text("Анкета нового сотрудника", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text("Имя и Фамилия") },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = NeonCyan) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("Логин для входа") },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = NeonCyan) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Пароль") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = NeonCyan) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Выберите роль в баре:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UserRole.entries.forEach { role ->
                                val selected = regRole == role
                                FilterChip(
                                    selected = selected,
                                    onClick = { regRole = role },
                                    label = { Text(role.title) },
                                    leadingIcon = {
                                        if (role == UserRole.ADMIN) Icon(Icons.Rounded.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(role.badgeColorHex).copy(alpha = 0.25f),
                                        selectedLabelColor = Color(role.badgeColorHex)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Аватар / Бейдж:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(emojis) { emoji ->
                                val isSelected = regAvatar == emoji
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) NeonCyan.copy(alpha = 0.25f) else FrostedGlassFill)
                                        .border(
                                            1.5.dp,
                                            if (isSelected) NeonCyan else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { regAvatar = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 20.sp)
                                }
                            }
                        }

                        regError?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(err, color = CrimsonError, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { isRegisterMode = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Назад к входу", color = TextSecondary)
                        }

                        Button(
                            onClick = {
                                if (regName.isBlank()) {
                                    regError = "Введите имя сотрудника"
                                } else if (regUsername.isBlank()) {
                                    regError = "Введите логин"
                                } else if (regPassword.isBlank()) {
                                    regError = "Введите пароль"
                                } else {
                                    onRegisterUser(
                                        regName.trim(),
                                        regUsername.trim().lowercase(),
                                        regPassword.trim(),
                                        regRole,
                                        regAvatar
                                    )
                                    isRegisterMode = false
                                    onContinue()
                                }
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("Зарегистрировать", color = ObsidianBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
