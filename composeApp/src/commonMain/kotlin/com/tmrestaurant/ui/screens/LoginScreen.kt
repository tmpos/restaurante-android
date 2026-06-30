package com.tmrestaurant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmrestaurant.platform.formatDateTime
import com.tmrestaurant.ui.data.AccessControl
import com.tmrestaurant.ui.data.TurnoManager
import com.tmrestaurant.ui.data.User
import com.tmrestaurant.ui.data.UserRole
import com.tmrestaurant.ui.data.Usuario
import com.tmrestaurant.ui.data.UsuariosManager
import com.tmrestaurant.ui.screens.login.BrandPanel
import com.tmrestaurant.ui.screens.login.CredentialsLoginPanel
import com.tmrestaurant.ui.screens.login.LoginTabs
import com.tmrestaurant.ui.screens.login.PinLoginPanel
import com.tmrestaurant.ui.theme.AppColors

private const val PinLength = 4

@Composable
fun LoginScreen(
    companyName: String,
    companyLogo: ByteArray?,
    onLoginComplete: () -> Unit
) {
    var loginUser by remember { mutableStateOf<User?>(null) }
    var showTurnoInput by remember { mutableStateOf(false) }
    var initialAmountText by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showTurnoActivoModal by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var forceCredentialUser by remember { mutableStateOf<Usuario?>(null) }

    var credUsername by remember { mutableStateOf("") }
    var credPassword by remember { mutableStateOf("") }
    var credError by remember { mutableStateOf<String?>(null) }

    fun continueLogin(usuario: Usuario) {
        val user = User(id = usuario.id, name = usuario.name, role = usuario.role, clave = "")
        loginUser = user
        val userHasTurno = TurnoManager.activeTurnoForUser(user.id)
        val requiresTurno = AccessControl.canOperateCash(user) && user.role != UserRole.ADMIN
        if (userHasTurno != null && requiresTurno) {
            showTurnoActivoModal = true
        } else {
            TurnoManager.login(user)
            if (requiresTurno) showTurnoInput = true else onLoginComplete()
        }
    }

    if (forceCredentialUser != null) {
        ForceCredentialsModal(
            usuario = forceCredentialUser!!,
            onDismiss = {
                forceCredentialUser = null
                credPassword = ""
                credError = null
                pin = ""
            },
            onSaved = { updated ->
                forceCredentialUser = null
                continueLogin(updated)
            }
        )
    } else if (showTurnoActivoModal && loginUser != null) {
        val userTurno = TurnoManager.activeTurnoForUser(loginUser!!.id) ?: return
        TurnoActivoModal(
            turno = userTurno,
            onCerrar = {
                TurnoManager.closeTurnoForUser(loginUser!!.id)
                TurnoManager.login(loginUser!!)
                showTurnoActivoModal = false
                pin = ""
                if (AccessControl.canOperateCash(loginUser!!) && loginUser!!.role != UserRole.ADMIN) showTurnoInput = true else onLoginComplete()
            },
            onContinuar = {
                TurnoManager.login(loginUser!!)
                showTurnoActivoModal = false
                onLoginComplete()
            }
        )
    } else if (showTurnoInput && loginUser != null) {
        TurnoInitialModal(
            userName = loginUser!!.name,
            amountText = initialAmountText,
            onAmountChange = { initialAmountText = it },
            onConfirm = {
                val amount = initialAmountText.toDoubleOrNull() ?: 0.0
                TurnoManager.openTurno(amount)
                onLoginComplete()
            },
            onCancel = {
                showTurnoInput = false
                loginUser = null
                pin = ""
                pinError = false
            }
        )
    } else {
        BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
            val isLandscape = maxWidth > maxHeight
            val outerPadding = if (isLandscape) 24.dp else 16.dp
            val formHorizontalPadding = if (isLandscape) 48.dp else 20.dp
            val formCardModifier = if (isLandscape) Modifier.fillMaxSize() else Modifier.fillMaxWidth()

            @Composable
            fun LoginFormContent(modifier: Modifier = Modifier) {
                Column(
                    modifier.widthIn(max = 400.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text("Bienvenido", color = Color(0xFF0F172A), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Inicia sesion para continuar", color = Color(0xFF64748B), fontSize = 14.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(28.dp))
                    LoginTabs(
                        selectedTab = selectedTab,
                        onTabSelected = {
                            selectedTab = it
                            pin = ""
                            pinError = false
                            credError = null
                        }
                    )
                    Spacer(Modifier.height(32.dp))
                    when (selectedTab) {
                        0 -> CredentialsLoginPanel(
                            username = credUsername,
                            password = credPassword,
                            onUsernameChange = { credUsername = it; credError = null },
                            onPasswordChange = { credPassword = it; credError = null },
                            onLogin = {
                                val candidate = UsuariosManager.findByLogin(credUsername)
                                if (candidate != null && UsuariosManager.isLocked(candidate)) {
                                    val remainingMin = (UsuariosManager.lockRemainingMs(candidate) / 60000L).coerceAtLeast(1L)
                                    credError = "Usuario bloqueado. Intente en $remainingMin min."
                                    return@CredentialsLoginPanel
                                }
                                val user = UsuariosManager.validateCredentials(credUsername.trim(), credPassword)
                                if (user != null) {
                                    credError = null
                                    if (user.mustChangeCredentials) forceCredentialUser = user else continueLogin(user)
                                } else {
                                    credError = "Credenciales invalidas"
                                }
                            }
                        )
                        1 -> PinLoginPanel(
                            pin = pin,
                            pinError = pinError,
                            onDigit = { digit ->
                                if (pin.length < PinLength) {
                                    pin += digit
                                    pinError = false
                                    credError = null
                                }
                            },
                            onDelete = {
                                if (pin.isNotEmpty()) {
                                    pin = pin.dropLast(1)
                                    pinError = false
                                }
                            },
                            onClear = {
                                pin = ""
                                pinError = false
                            }
                        )
                    }
                    if (!credError.isNullOrBlank() && selectedTab == 0) {
                        Spacer(Modifier.height(12.dp))
                        Text(credError!!, color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            if (isLandscape) {
                Row(Modifier.fillMaxSize().padding(outerPadding)) {
                    Box(Modifier.weight(0.38f)) {
                        BrandPanel(companyName = companyName, companyLogo = companyLogo)
                    }
                    Spacer(Modifier.width(24.dp))
                    Box(
                        Modifier.weight(0.62f).then(formCardModifier).clip(RoundedCornerShape(28.dp)).background(Color.White).padding(horizontal = formHorizontalPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        LoginFormContent()
                    }
                }
            } else {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(outerPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BrandPanel(
                        companyName = companyName,
                        companyLogo = companyLogo,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 260.dp).clip(RoundedCornerShape(28.dp))
                    )
                    Box(
                        Modifier.fillMaxWidth().then(formCardModifier).clip(RoundedCornerShape(28.dp)).background(Color.White).padding(horizontal = formHorizontalPadding, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoginFormContent()
                    }
                }
            }
        }

        LaunchedEffect(pin) {
            if (pin.length == PinLength) {
                val candidate = UsuariosManager.findByPin(pin)
                if (candidate != null && UsuariosManager.isLocked(candidate)) {
                    pinError = true
                    credError = "PIN bloqueado temporalmente"
                    pin = ""
                    return@LaunchedEffect
                }
                val user = UsuariosManager.validatePin(pin)
                if (user != null) {
                    if (user.mustChangeCredentials) forceCredentialUser = user else continueLogin(user)
                } else {
                    UsuariosManager.registerFailedPin(pin)
                    pinError = true
                    pin = ""
                }
            }
        }
    }
}

@Composable
private fun ForceCredentialsModal(
    usuario: Usuario,
    onDismiss: () -> Unit,
    onSaved: (Usuario) -> Unit
) {
    var name by remember(usuario.id) { mutableStateOf(usuario.name) }
    var pin by remember(usuario.id) { mutableStateOf("") }
    var password by remember(usuario.id) { mutableStateOf("") }
    var confirmPassword by remember(usuario.id) { mutableStateOf("") }
    var error by remember(usuario.id) { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(420.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Cambie sus credenciales", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
            Text("Este usuario usa credenciales iniciales y debe actualizarlas antes de entrar.", color = AppColors.TextSecondary, fontSize = 13.sp)
            ForceField("Nombre", name) { name = it; error = "" }
            ForceField("PIN (4 digitos)", pin) { pin = it.filter(Char::isDigit).take(4); error = "" }
            ForceField("Nueva contrasena", password) { password = it; error = "" }
            ForceField("Confirmar contrasena", confirmPassword) { confirmPassword = it; error = "" }
            if (error.isNotBlank()) {
                Text(error, color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) { Text("Cancelar", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold) }
                Box(
                    Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Primary).clickable {
                        error = when {
                            name.isBlank() -> "El nombre es obligatorio"
                            pin.length != 4 -> "El PIN debe tener 4 digitos"
                            password.length < 4 -> "La contrasena debe tener al menos 4 caracteres"
                            password != confirmPassword -> "Las contrasenas no coinciden"
                            UsuariosManager.isPinInUse(pin, usuario.id) -> "Ese PIN ya esta en uso"
                            else -> ""
                        }
                        if (error.isBlank()) {
                            UsuariosManager.forceCredentialsUpdated(usuario.id, name, pin, password)
                            onSaved(UsuariosManager.usuarios.first { it.id == usuario.id })
                        }
                    },
                    contentAlignment = Alignment.Center
                ) { Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun ForceField(label: String, value: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = AppColors.TextSecondary, fontSize = 12.sp)
        Box(
            Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Background)
                .border(1.dp, AppColors.Border, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onChange,
                textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp),
                modifier = Modifier.fillMaxSize(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun TurnoActivoModal(
    turno: com.tmrestaurant.ui.data.Turno,
    onCerrar: () -> Unit,
    onContinuar: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(400.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(56.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFFFEF3C7)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Lock, null, tint = Color(0xFFD97706), modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Turno Activo", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
            Text("Ya hay un turno abierto", color = AppColors.TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Background).padding(14.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth()) { Text("Cajero:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text(turno.userName, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    Row(Modifier.fillMaxWidth()) { Text("Inicio:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text(formatDateTime(turno.startTime), color = AppColors.TextPrimary, fontSize = 13.sp) }
                    Row(Modifier.fillMaxWidth()) { Text("Inicial:", color = AppColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text("RD\$ ${"%,.2f".format(turno.initialAmount)}", color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).clickable(onClick = onCerrar),
                    contentAlignment = Alignment.Center
                ) { Text("Cerrar Turno", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                Box(
                    Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Primary)
                        .clickable(onClick = onContinuar),
                    contentAlignment = Alignment.Center
                ) { Text("Continuar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun TurnoInitialModal(
    userName: String,
    amountText: String,
    onAmountChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(400.dp).clip(RoundedCornerShape(20.dp)).background(AppColors.Surface).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(56.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFFFEF3C7)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Lock, null, tint = Color(0xFFD97706), modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Apertura de Turno", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
            Text("$userName, ingresa el monto inicial en efectivo", color = AppColors.TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (amountText.isEmpty()) {
                    Text("Monto inicial RD\$", color = AppColors.Gray, fontSize = 14.sp)
                }
                BasicTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) onAmountChange(it) },
                    textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxSize(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Background)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)).clickable(onClick = onCancel),
                    contentAlignment = Alignment.Center
                ) { Text("Cancelar", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                Box(
                    Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.Primary)
                        .clickable(enabled = amountText.isNotBlank()) { onConfirm() },
                    contentAlignment = Alignment.Center
                ) { Text("Iniciar Turno", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
