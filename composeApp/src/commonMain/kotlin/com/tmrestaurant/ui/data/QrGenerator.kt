package com.tmrestaurant.ui.data

object QrGenerator {
    private val gfExp = IntArray(512).also { a ->
        var v = 1
        for (i in 0..<255) { a[i] = v; a[i + 255] = v; v = v shl 1; if (v >= 256) v = v xor 0x11D }
    }
    private val gfLog = IntArray(256).also { a -> for (i in 0..<255) a[gfExp[i]] = i }

    private fun gfMul(x: Int, y: Int): Int {
        if (x == 0 || y == 0) return 0
        return gfExp[gfLog[x] + gfLog[y]]
    }

    private fun rsEncode(data: IntArray, eccCount: Int): IntArray {
        var gen = intArrayOf(1)
        for (i in 0..<eccCount) {
            val alpha = gfExp[i]
            val newGen = IntArray(gen.size + 1)
            for (j in gen.indices) {
                newGen[j] = newGen[j] xor gfMul(gen[j], alpha)
                newGen[j + 1] = newGen[j + 1] xor gen[j]
            }
            gen = newGen
        }
        val msg = data + IntArray(eccCount)
        for (i in data.indices) {
            if (msg[i] != 0) {
                val coef = msg[i]
                for (j in gen.indices) msg[i + j] = msg[i + j] xor gfMul(coef, gen[j])
            }
        }
        return msg.copyOfRange(data.size, data.size + eccCount)
    }

    // Version table: size, data codewords (M-ECC), ECC codewords
    private data class QrVer(val version: Int, val size: Int, val dataCW: Int, val eccCW: Int)
    private val versions = listOf(
        QrVer(3, 29, 44, 26),
        QrVer(4, 33, 64, 36),
        QrVer(5, 37, 108, 44)
    )

    fun generate(text: String, preferVersion: Int = 5): Array<BooleanArray> {
        val bytes = text.encodeToByteArray().map { it.toInt() and 0xFF }
        val ver = versions.last { it.dataCW >= bytes.size + 2 }
        val s = ver.size
        val dataCW = ver.dataCW
        val eccCW = ver.eccCW
        val matrix = Array(s) { BooleanArray(s) }
        val charBits = if (ver.version <= 9) 8 else 16

        val dataBits = mutableListOf<Int>()
        dataBits.addAll(byteBits(0x04, 4))
        dataBits.addAll(byteBits(bytes.size, charBits))
        for (b in bytes) dataBits.addAll(byteBits(b, 8))
        var termLen = minOf(4, dataCW * 8 - dataBits.size)
        repeat(termLen) { dataBits.add(0) }
        while (dataBits.size % 8 != 0) dataBits.add(0)
        val padBytes = listOf(0xEC, 0x11)
        var pi = 0
        while (dataBits.size < dataCW * 8) {
            val pb = padBytes[pi % 2]; pi++
            for (i in 7 downTo 0) dataBits.add((pb shr i) and 1)
        }
        val cw = IntArray(dataCW) { i ->
            (0..7).sumOf { b -> dataBits[i * 8 + b] shl (7 - b) }
        }
        val ecc = rsEncode(cw, eccCW)
        val allCW = cw + ecc

        placeFunctionPatterns(matrix, s)
        placeData(matrix, allCW, s)
        applyBestMask(matrix, s)
        placeFormatInfo(matrix, s)
        return matrix
    }

    private fun byteBits(value: Int, bits: Int): List<Int> =
        (bits - 1 downTo 0).map { (value shr it) and 1 }

    private fun placeFunctionPatterns(m: Array<BooleanArray>, s: Int) {
        for ((fx, fy) in listOf(0 to 0, s - 7 to 0, 0 to s - 7)) {
            for (r in -1..7) for (c in -1..7) {
                val x = fx + c; val y = fy + r
                if (x in 0..<s && y in 0..<s) m[y][x] = true
            }
        }
        for (i in 8..<s - 8) { m[6][i] = i % 2 == 0; m[i][6] = i % 2 == 0 }
        m[s - 8][8] = true
    }

    private fun placeData(m: Array<BooleanArray>, data: IntArray, s: Int) {
        var bit = 0; var dirUp = true; var col = s - 1
        while (col > 0) {
            if (col == 6) col--
            for (row in if (dirUp) (0..<s) else (s - 1 downTo 0)) {
                for (cx in 0..1) {
                    val x = col - cx
                    if (x < 0 || isReserved(m, row, x, s)) continue
                    val b = if (bit / 8 < data.size) (data[bit / 8] shr (7 - (bit % 8))) and 1 else 0
                    m[row][x] = b == 1; bit++
                }
            }
            dirUp = !dirUp; col -= 2
        }
    }

    private fun isReserved(m: Array<BooleanArray>, r: Int, c: Int, s: Int): Boolean {
        for ((fx, fy) in listOf(0 to 0, s - 7 to 0, 0 to s - 7))
            if (r in fy - 1..fy + 7 && c in fx - 1..fx + 7) return true
        if (r == 6 || c == 6) return true
        if (r < 9 && c == 8) return true
        if (r == 8 && c < 8) return true
        if (r == 8 && c >= s - 8) return true
        if (r >= s - 8 && c == 8) return true
        if (r == s - 8 && c == 8) return true
        return false
    }

    private val MASKS = listOf<(Int, Int) -> Boolean>(
        { r, c -> (r + c) % 2 == 0 }, { r, c -> r % 2 == 0 }, { r, c -> c % 3 == 0 },
        { r, c -> (r + c) % 3 == 0 }, { r, c -> (r / 2 + c / 3) % 2 == 0 },
        { r, c -> (r * c) % 2 + (r * c) % 3 == 0 },
        { r, c -> ((r * c) % 2 + (r * c) % 3) % 2 == 0 },
        { r, c -> ((r + c) % 2 + (r * c) % 3) % 2 == 0 }
    )

    private fun applyBestMask(m: Array<BooleanArray>, s: Int) {
        var best = Int.MAX_VALUE; var bestIdx = 0
        for (idx in 0..7) {
            val test = m.map { it.copyOf() }.toTypedArray()
            applyMask(test, idx, s)
            val score = evaluate(test, s)
            if (score < best) { best = score; bestIdx = idx }
        }
        applyMask(m, bestIdx, s); _selectedMask = bestIdx
    }

    private var _selectedMask = 0

    private fun applyMask(m: Array<BooleanArray>, idx: Int, s: Int) {
        val fn = MASKS[idx]
        for (r in 0..<s) for (c in 0..<s)
            if (!isReserved(m, r, c, s) && fn(r, c)) m[r][c] = !m[r][c]
    }

    private fun evaluate(m: Array<BooleanArray>, s: Int): Int {
        var score = 0
        for (row in 0..<s) { var run = 1; for (c in 1..<s) { if (m[row][c] == m[row][c - 1]) run++ else { if (run >= 5) score += run - 2; run = 1 } }; if (run >= 5) score += run - 2 }
        for (col in 0..<s) { var run = 1; for (r in 1..<s) { if (m[r][col] == m[r - 1][col]) run++ else { if (run >= 5) score += run - 2; run = 1 } }; if (run >= 5) score += run - 2 }
        for (r in 0..<s - 1) for (c in 0..<s - 1) { val v = m[r][c]; if (v == m[r][c + 1] && v == m[r + 1][c] && v == m[r + 1][c + 1]) score += 3 }
        val f1 = booleanArrayOf(true, false, true, true, true, false, true, false, false, false, false)
        val f2 = booleanArrayOf(false, false, false, false, true, false, true, true, true, false, true)
        for (r in 0..<s) for (c in 0..<s - 11) { var a = true; var b = true; for (k in 0..10) { if (m[r][c + k] != f1[k]) a = false; if (m[r][c + k] != f2[k]) b = false }; if (a || b) score += 40 }
        for (c in 0..<s) for (r in 0..<s - 11) { var a = true; var b = true; for (k in 0..10) { if (m[r + k][c] != f1[k]) a = false; if (m[r + k][c] != f2[k]) b = false }; if (a || b) score += 40 }
        val dark = m.sumOf { r -> r.count { it } }
        val rt = dark * 100 / (s * s)
        score += minOf(abs(rt / 5 * 5 - 50) * 2, abs((rt / 5 + 1) * 5 - 50) * 2)
        return score
    }

    private fun abs(x: Int) = if (x < 0) -x else x

    private fun placeFormatInfo(m: Array<BooleanArray>, s: Int) {
        val fmt = intArrayOf(
            0x5412, 0x5125, 0x5E7C, 0x5B4B, 0x45F9, 0x40CE, 0x4F97, 0x4AA0,
            0x77C4, 0x72F3, 0x7DAA, 0x789D, 0x662F, 0x6318, 0x6C41, 0x6976,
            0x1689, 0x13BE, 0x1CE7, 0x19D0, 0x0762, 0x0255, 0x0D0C, 0x083B,
            0x355F, 0x3068, 0x3F31, 0x3A06, 0x24B4, 0x2183, 0x2EDA, 0x2BED
        )[0b00 shl 3 or _selectedMask]
        val p1 = listOf(
            intArrayOf(0, 8), intArrayOf(1, 8), intArrayOf(2, 8), intArrayOf(3, 8), intArrayOf(4, 8), intArrayOf(5, 8),
            intArrayOf(7, 8), intArrayOf(8, 8), intArrayOf(8, 7), intArrayOf(8, 5), intArrayOf(8, 4),
            intArrayOf(8, 3), intArrayOf(8, 2), intArrayOf(8, 1), intArrayOf(8, 0)
        )
        val p2 = listOf(
            intArrayOf(s - 1, 8), intArrayOf(s - 2, 8), intArrayOf(s - 3, 8), intArrayOf(s - 4, 8), intArrayOf(s - 5, 8), intArrayOf(s - 6, 8), intArrayOf(s - 7, 8),
            intArrayOf(8, s - 8), intArrayOf(8, s - 7), intArrayOf(8, s - 6), intArrayOf(8, s - 5), intArrayOf(8, s - 4), intArrayOf(8, s - 3), intArrayOf(8, s - 2), intArrayOf(8, s - 1)
        )
        for (i in 0..14) { val b = (fmt shr (14 - i)) and 1; m[p1[i][0]][p1[i][1]] = b == 1; m[p2[i][0]][p2[i][1]] = b == 1 }
    }

    fun dgiiUrl(rnc: String, ncf: String, total: Double, codigoSeguridad: String = ""): String {
        val base = "https://fc.dgii.gov.do/eCF/ConsultaTimbreFC"
        val params = mutableListOf(
            "RNCEmisor=$rnc",
            "ENCF=$ncf",
            "MontoTotal=${"%.2f".format(total)}"
        )
        if (codigoSeguridad.isNotBlank()) {
            params.add("CodigoSeguridad=$codigoSeguridad")
        }
        return "$base?${params.joinToString("&")}"
    }

    fun toAscii(matrix: Array<BooleanArray>): String =
        matrix.joinToString("\n") { row -> row.joinToString("") { if (it) "#" else " " } }
}
