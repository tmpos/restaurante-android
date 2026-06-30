package com.tmrestaurant.platform

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/**
 * Estado del lector de tarjetas magneticas.
 */
enum class CardReaderState {
    IDLE,
    WAITING,
    READING,
    DETECTED,
    VALID,
    INVALID
}

/**
 * Informacion de diagnostico de una tecla capturada.
 */
data class KeyDiagnostic(
    val key: String,
    val keyCode: Long,
    val nativeKeyCode: Int,
    val scanCode: Int,
    val isShiftPressed: Boolean,
    val isCtrlPressed: Boolean,
    val isAltPressed: Boolean,
    val timeSincePrevMs: Long,
    val charAdded: Char?
)

/**
 * Resultado del parseo de datos de tarjeta magnetica.
 */
data class MagstripeParseResult(
    val raw: String,
    val normalized: String,
    val masked: String,
    val pan: String?,
    val maskedPan: String?,
    val brand: String?,
    val track1: String?,
    val track2: String?,
    val track3: String?,
    val hasValidStructure: Boolean,
    val warnings: List<String>
)

/**
 * Helper reutilizable para capturar lectura de tarjetas magneticas
 * desde un lector USB HID que funciona como teclado.
 *
 * Problema principal que resuelve: Los lectores MSR envian scancodes US-QWERTY,
 * pero si el SO tiene layout espanol/latino, los caracteres especiales llegan
 * corruptos (ej: ; -> ñ, / -> -, ? -> _, % -> 5, etc).
 *
 * Solucion: Interceptar KeyEvent a nivel de key code (no character),
 * y re-mapear usando tabla US layout fija.
 */
class CardReaderHelper {

    // --- Estado ---
    var state: CardReaderState = CardReaderState.IDLE
        private set

    // --- Buffer de lectura ---
    private val buffer = StringBuilder()
    private val timestamps = mutableListOf<Long>()
    private val diagnostics = mutableListOf<KeyDiagnostic>()

    // --- Configuracion ---
    /** Tiempo maximo entre teclas para considerar que es lectura automatica (ms) */
    var maxInterKeyDelayMs: Long = 80L

    /** Tiempo de inactividad para limpiar el buffer (ms) */
    var inactivityTimeoutMs: Long = 3000L

    /** Minimo de caracteres para considerar lectura valida */
    var minCardLength: Int = 5

    // --- Callbacks ---
    var onStateChanged: ((CardReaderState) -> Unit)? = null
    var onBufferChanged: ((String) -> Unit)? = null
    var onDiagnosticAdded: ((KeyDiagnostic) -> Unit)? = null
    var onReadComplete: ((MagstripeParseResult) -> Unit)? = null

    // --- Timestamp tracking ---
    private var lastKeyTime: Long = 0L

    /**
     * Inicia el modo de prueba del lector. Limpia todo estado previo.
     */
    fun startCardReaderTest() {
        buffer.clear()
        timestamps.clear()
        diagnostics.clear()
        lastKeyTime = 0L
        state = CardReaderState.WAITING
        onStateChanged?.invoke(state)
        onBufferChanged?.invoke("")
    }

    /**
     * Detiene el modo de prueba.
     */
    fun stopCardReaderTest() {
        state = CardReaderState.IDLE
        onStateChanged?.invoke(state)
    }

    /**
     * Limpia todos los datos capturados.
     */
    fun clearCardReaderTest() {
        buffer.clear()
        timestamps.clear()
        diagnostics.clear()
        lastKeyTime = 0L
        state = if (state != CardReaderState.IDLE) CardReaderState.WAITING else CardReaderState.IDLE
        onStateChanged?.invoke(state)
        onBufferChanged?.invoke("")
    }

    /**
     * Obtener datos crudos actuales del buffer.
     */
    fun getRawData(): String = buffer.toString()

    /**
     * Obtener lista de diagnosticos capturados.
     */
    fun getDiagnostics(): List<KeyDiagnostic> = diagnostics.toList()

    /**
     * Obtener cantidad de caracteres en el buffer.
     */
    fun getCharCount(): Int = buffer.length

    /**
     * Maneja un KeyEvent de Compose. Retorna true si el evento fue consumido.
     *
     * IMPORTANTE: Esta funcion usa event.key (Key enum basado en keyCode virtual)
     * y NO el caracter que reporta el SO, para evitar problemas de layout.
     */
    fun handleCardKeyEvent(event: KeyEvent): Boolean {
        if (state != CardReaderState.WAITING && state != CardReaderState.READING) {
            return false
        }

        if (event.type != KeyEventType.KeyDown) {
            return true // Consumir KeyUp tambien para no propagar
        }

        val now = System.currentTimeMillis()
        val timeSincePrev = if (lastKeyTime > 0) now - lastKeyTime else 0L

        // Si paso mucho tiempo desde la ultima tecla y hay datos, es probable
        // que sea escritura manual. Limpiar buffer si el timeout expiro.
        if (lastKeyTime > 0 && timeSincePrev > inactivityTimeoutMs && buffer.isNotEmpty()) {
            buffer.clear()
            timestamps.clear()
            state = CardReaderState.WAITING
            onStateChanged?.invoke(state)
            onBufferChanged?.invoke("")
        }

        // Extraer informacion nativa del evento
        val nativeKeyCode = extractNativeKeyCode(event)
        val scanCode = extractScanCode(event)

        // Mapear la tecla usando US layout basado en Key enum (NO en el caracter del SO)
        val ch = mapKeyToUsChar(event.key, event.isShiftPressed)

        if (ch != null) {
            if (ch == '\n') {
                // Enter = fin de lectura
                if (buffer.length >= minCardLength) {
                    val rawData = buffer.toString()
                    val result = parseMagstripeData(rawData)
                    state = if (result.hasValidStructure) CardReaderState.VALID else CardReaderState.INVALID
                    onStateChanged?.invoke(state)
                    onReadComplete?.invoke(result)
                }
                // Registrar diagnostico del Enter
                val diag = KeyDiagnostic(
                    key = "Enter",
                    keyCode = event.key.keyCode,
                    nativeKeyCode = nativeKeyCode,
                    scanCode = scanCode,
                    isShiftPressed = event.isShiftPressed,
                    isCtrlPressed = event.isCtrlPressed,
                    isAltPressed = event.isAltPressed,
                    timeSincePrevMs = timeSincePrev,
                    charAdded = null
                )
                diagnostics.add(diag)
                onDiagnosticAdded?.invoke(diag)
                lastKeyTime = now
                return true
            }

            // Agregar caracter al buffer
            appendKeyToBuffer(ch, now)

            // Cambiar a estado READING si es la primera tecla o hay datos
            if (state == CardReaderState.WAITING) {
                state = CardReaderState.READING
                onStateChanged?.invoke(state)
            }

            // Registrar diagnostico
            val diag = KeyDiagnostic(
                key = event.key.toString(),
                keyCode = event.key.keyCode,
                nativeKeyCode = nativeKeyCode,
                scanCode = scanCode,
                isShiftPressed = event.isShiftPressed,
                isCtrlPressed = event.isCtrlPressed,
                isAltPressed = event.isAltPressed,
                timeSincePrevMs = timeSincePrev,
                charAdded = ch
            )
            diagnostics.add(diag)
            onDiagnosticAdded?.invoke(diag)
            lastKeyTime = now

            onBufferChanged?.invoke(buffer.toString())
            return true
        }

        // Tecla no reconocida - registrar diagnostico igualmente
        val diag = KeyDiagnostic(
            key = event.key.toString(),
            keyCode = event.key.keyCode,
            nativeKeyCode = nativeKeyCode,
            scanCode = scanCode,
            isShiftPressed = event.isShiftPressed,
            isCtrlPressed = event.isCtrlPressed,
            isAltPressed = event.isAltPressed,
            timeSincePrevMs = timeSincePrev,
            charAdded = null
        )
        diagnostics.add(diag)
        onDiagnosticAdded?.invoke(diag)
        lastKeyTime = now

        return true // Consumir todo mientras estamos capturando
    }

    private fun appendKeyToBuffer(ch: Char, timestamp: Long) {
        buffer.append(ch)
        timestamps.add(timestamp)
    }

    /**
     * Determina si la secuencia de timestamps indica un swipe automatico de tarjeta
     * (las teclas llegan muy rapido, < maxInterKeyDelayMs entre cada una).
     */
    fun isProbablyCardSwipe(): Boolean {
        if (timestamps.size < minCardLength) return false
        var fastCount = 0
        for (i in 1 until timestamps.size) {
            val delta = timestamps[i] - timestamps[i - 1]
            if (delta < maxInterKeyDelayMs) fastCount++
        }
        // Al menos 80% de las teclas deben llegar rapido
        return fastCount.toFloat() / (timestamps.size - 1) > 0.8f
    }

    // ==================== MAPEO US LAYOUT ====================

    /**
     * Mapea Key enum de Compose a caracter US-QWERTY.
     *
     * Esto es INDEPENDIENTE del layout del SO porque Key enum se basa en
     * virtual key codes, no en el caracter traducido. El lector MSR siempre
     * envia scancodes US, asi que este mapeo produce los caracteres correctos
     * sin importar si el SO tiene layout espanol.
     */
    private fun mapKeyToUsChar(key: Key, shifted: Boolean): Char? {
        return when (key) {
            // Digitos / simbolos con shift (US layout)
            Key.Zero -> if (shifted) ')' else '0'
            Key.One -> if (shifted) '!' else '1'
            Key.Two -> if (shifted) '@' else '2'
            Key.Three -> if (shifted) '#' else '3'
            Key.Four -> if (shifted) '$' else '4'
            Key.Five -> if (shifted) '%' else '5'
            Key.Six -> if (shifted) '^' else '6'
            Key.Seven -> if (shifted) '&' else '7'
            Key.Eight -> if (shifted) '*' else '8'
            Key.Nine -> if (shifted) '(' else '9'
            // Letras
            Key.A -> if (shifted) 'A' else 'a'
            Key.B -> if (shifted) 'B' else 'b'
            Key.C -> if (shifted) 'C' else 'c'
            Key.D -> if (shifted) 'D' else 'd'
            Key.E -> if (shifted) 'E' else 'e'
            Key.F -> if (shifted) 'F' else 'f'
            Key.G -> if (shifted) 'G' else 'g'
            Key.H -> if (shifted) 'H' else 'h'
            Key.I -> if (shifted) 'I' else 'i'
            Key.J -> if (shifted) 'J' else 'j'
            Key.K -> if (shifted) 'K' else 'k'
            Key.L -> if (shifted) 'L' else 'l'
            Key.M -> if (shifted) 'M' else 'm'
            Key.N -> if (shifted) 'N' else 'n'
            Key.O -> if (shifted) 'O' else 'o'
            Key.P -> if (shifted) 'P' else 'p'
            Key.Q -> if (shifted) 'Q' else 'q'
            Key.R -> if (shifted) 'R' else 'r'
            Key.S -> if (shifted) 'S' else 's'
            Key.T -> if (shifted) 'T' else 't'
            Key.U -> if (shifted) 'U' else 'u'
            Key.V -> if (shifted) 'V' else 'v'
            Key.W -> if (shifted) 'W' else 'w'
            Key.X -> if (shifted) 'X' else 'x'
            Key.Y -> if (shifted) 'Y' else 'y'
            Key.Z -> if (shifted) 'Z' else 'z'
            // Simbolos especiales - CRITICOS para tracks de tarjetas
            Key.Semicolon -> if (shifted) ':' else ';'    // Track 2 start
            Key.Equals -> if (shifted) '+' else '='        // Field separator
            Key.Minus -> if (shifted) '_' else '-'
            Key.Slash -> if (shifted) '?' else '/'         // Track end marker
            Key.Backslash -> if (shifted) '|' else '\\'
            Key.LeftBracket -> if (shifted) '{' else '['
            Key.RightBracket -> if (shifted) '}' else ']'
            Key.Comma -> if (shifted) '<' else ','
            Key.Period -> if (shifted) '>' else '.'
            Key.Apostrophe -> if (shifted) '"' else '\''
            Key.Grave -> if (shifted) '~' else '`'
            Key.Spacebar -> ' '
            Key.Enter -> '\n'
            Key.NumPadEnter -> '\n'
            // Numpad
            Key.NumPad0 -> '0'
            Key.NumPad1 -> '1'
            Key.NumPad2 -> '2'
            Key.NumPad3 -> '3'
            Key.NumPad4 -> '4'
            Key.NumPad5 -> '5'
            Key.NumPad6 -> '6'
            Key.NumPad7 -> '7'
            Key.NumPad8 -> '8'
            Key.NumPad9 -> '9'
            Key.NumPadDot -> '.'
            Key.NumPadComma -> ','
            Key.NumPadSubtract -> '-'
            Key.NumPadAdd -> '+'
            Key.NumPadMultiply -> '*'
            Key.NumPadDivide -> '/'
            Key.NumPadEquals -> '='
            else -> null
        }
    }

    // ==================== PARSEO DE TARJETA ====================

    /**
     * Normaliza la entrada cruda de magstripe.
     * Limpia caracteres de control y whitespace innecesario.
     */
    fun normalizeMagstripeInput(rawData: String): String {
        return rawData
            .replace("\r", "")
            .replace("\n", "")
            .replace("\t", "")
            .trim()
    }

    /**
     * Parsea los datos crudos de tarjeta magnetica y extrae tracks.
     * Track 1: Inicia con % (o %B), termina con ?
     * Track 2: Inicia con ;, termina con ?
     * Track 3: Si hay segundo ; despues del primer ?, inicia con ; y termina con ?
     */
    fun parseMagstripeData(rawData: String): MagstripeParseResult {
        val normalized = normalizeMagstripeInput(decodeHexIfNeeded(rawData))
        val warnings = mutableListOf<String>()

        // Extraer Track 1: %...?
        val track1 = extractTrack(normalized, '%', '?')
        // Extraer Track 2: ;...? (despues de track 1 si existe)
        val t2Start = if (track1 != null) {
            val t1End = normalized.indexOf('?')
            if (t1End >= 0) normalized.indexOf(';', t1End) else normalized.indexOf(';')
        } else {
            normalized.indexOf(';')
        }
        val track2 = if (t2Start >= 0) {
            val t2End = normalized.indexOf('?', t2Start)
            if (t2End >= 0) normalized.substring(t2Start, t2End + 1) else null
        } else null

        // Extraer Track 3: segundo ;...? despues de track 2
        val track3 = if (track2 != null && t2Start >= 0) {
            val t2End = normalized.indexOf('?', t2Start)
            if (t2End >= 0) {
                val t3Start = normalized.indexOf(';', t2End)
                if (t3Start >= 0) {
                    val t3End = normalized.indexOf('?', t3Start)
                    if (t3End >= 0) normalized.substring(t3Start, t3End + 1) else null
                } else null
            } else null
        } else null

        // Validacion de estructura
        val hasPercent = normalized.contains('%')
        val hasSemicolon = normalized.contains(';')
        val hasQuestion = normalized.contains('?')
        val hasEquals = normalized.contains('=')
        val hasValidStart = hasPercent || hasSemicolon
        val hasValidEnd = hasQuestion
        val hasValidStructure = hasValidStart && hasValidEnd && normalized.length >= minCardLength

        if (!hasValidStart) warnings.add("No se detecto inicio de track (% o ;)")
        if (!hasValidEnd) warnings.add("No se detecto fin de track (?)")
        if (!hasEquals && track2 != null) warnings.add("Track 2 sin separador (=)")
        if (normalized.length < minCardLength) warnings.add("Lectura muy corta (${normalized.length} chars)")

        // Verificar si parece problema de layout
        val suspiciousChars = normalized.count { it in "\\|{}~`" }
        if (suspiciousChars > 3 && !hasValidStart) {
            warnings.add("Posible problema de layout de teclado - caracteres sospechosos detectados")
        }

        // Enmascarar datos sensibles
        val masked = maskSensitiveData(normalized)
        val pan = extractPan(track1, track2, normalized)
        val maskedPan = pan?.let { maskPan(it) }
        val brand = pan?.let { detectBrand(it) }

        return MagstripeParseResult(
            raw = normalized,
            normalized = normalized,
            masked = masked,
            pan = pan,
            maskedPan = maskedPan,
            brand = brand,
            track1 = track1,
            track2 = track2,
            track3 = track3,
            hasValidStructure = hasValidStructure,
            warnings = warnings
        )
    }

    private fun extractTrack(data: String, startChar: Char, endChar: Char): String? {
        val start = data.indexOf(startChar)
        if (start < 0) return null
        val end = data.indexOf(endChar, start)
        if (end < 0) return null
        return data.substring(start, end + 1)
    }

    private fun decodeHexIfNeeded(data: String): String {
        val compact = data.replace(" ", "").replace("\n", "").replace("\r", "").replace("\t", "")
        if (compact.length < 16 || compact.length % 2 != 0 || !compact.matches(Regex("[0-9A-Fa-f]+"))) {
            return data
        }
        return try {
            buildString {
                var i = 0
                while (i + 1 < compact.length) {
                    append(compact.substring(i, i + 2).toInt(16).toChar())
                    i += 2
                }
            }
        } catch (_: Exception) {
            data
        }
    }

    private fun extractPan(track1: String?, track2: String?, normalized: String): String? {
        track2?.let {
            Regex(";?(\\d{13,19})=").find(it)?.groupValues?.getOrNull(1)?.let { pan -> return pan }
        }
        track1?.let {
            Regex("%?B?(\\d{13,19})\\^").find(it)?.groupValues?.getOrNull(1)?.let { pan -> return pan }
        }
        return Regex("\\d{13,19}").find(normalized)?.value
    }

    private fun maskPan(pan: String): String =
        if (pan.length > 8) pan.take(4) + "*".repeat(pan.length - 8) + pan.takeLast(4) else "*".repeat(pan.length)

    private fun detectBrand(pan: String): String {
        return when {
            pan.startsWith("4") -> "Visa"
            pan.take(2).toIntOrNull() in 51..55 -> "Mastercard"
            pan.take(4).toIntOrNull() in 2221..2720 -> "Mastercard"
            pan.startsWith("34") || pan.startsWith("37") -> "American Express"
            pan.startsWith("6011") || pan.startsWith("65") -> "Discover"
            else -> "Desconocida"
        }
    }

    /**
     * Enmascara numeros de tarjeta para seguridad.
     * Mantiene los primeros 4 y ultimos 4 digitos, enmascara el resto.
     */
    private fun maskSensitiveData(data: String): String {
        // Buscar secuencias de 12+ digitos consecutivos (posible PAN)
        val panRegex = Regex("(\\d{12,19})")
        return panRegex.replace(data) { match ->
            val pan = match.value
            if (pan.length > 8) {
                pan.take(4) + "*".repeat(pan.length - 8) + pan.takeLast(4)
            } else {
                "*".repeat(pan.length)
            }
        }
    }

    // ==================== PLATFORM-SPECIFIC HELPERS ====================

    /**
     * Obtiene el objeto nativo del KeyEvent via Java reflection.
     * En Desktop (JVM) es java.awt.event.KeyEvent, en Android es android.view.KeyEvent.
     * La propiedad "nativeKeyEvent" existe en cada plataforma pero no en commonMain.
     */
    private fun getNativeEvent(event: KeyEvent): Any? {
        return try {
            val getter = event.javaClass.getMethod("getNativeKeyEvent")
            getter.invoke(event)
        } catch (_: Exception) {
            try {
                // Fallback: intentar con campo directo
                val field = event.javaClass.getDeclaredField("nativeKeyEvent")
                field.isAccessible = true
                field.get(event)
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Extrae nativeKeyCode del evento.
     */
    private fun extractNativeKeyCode(event: KeyEvent): Int {
        return try {
            val native = getNativeEvent(event) ?: return -1
            val method = native.javaClass.getMethod("getKeyCode")
            method.invoke(native) as? Int ?: -1
        } catch (_: Exception) {
            -1
        }
    }

    /**
     * Extrae scanCode del evento nativo.
     */
    private fun extractScanCode(event: KeyEvent): Int {
        return try {
            val native = getNativeEvent(event) ?: return -1
            // Android: getScanCode(), Desktop: getExtendedKeyCode()
            val method = native.javaClass.methods.firstOrNull {
                it.name == "getScanCode" || it.name == "getExtendedKeyCode"
            }
            method?.invoke(native) as? Int ?: -1
        } catch (_: Exception) {
            -1
        }
    }
}
