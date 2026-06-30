package com.tmrestaurant.cloud

data class AlanubeResult(
    val success: Boolean = false,
    val message: String = "",
    val statusCode: Int = 0,
    val responseBody: String = "",
    val documentId: String? = null,
    val legalStatus: String? = null
)

object AlanubeService {
    private val client = CloudHttpClient()
    private var config = AlanubeConfig()

    fun init(cfg: AlanubeConfig) {
        config = cfg
    }

    fun getBaseUrl(): String = config.environment.baseUrl

    private fun companyUrl(): String =
        if (config.companyId.isNotBlank()) "${getBaseUrl()}/company/${config.companyId}" else "${getBaseUrl()}/company"

    private fun urlWithCompany(path: String): String =
        if (config.companyId.isNotBlank()) "$path?idCompany=${config.companyId}" else path

    private fun companyObj(): String =
        if (config.companyId.isNotBlank()) "\"company\":{\"id\":\"${config.companyId}\"}," else ""

    suspend fun testConnection(): AlanubeStatus {
        if (config.jwtToken.isBlank()) return AlanubeStatus(false, error = "JWT token requerido")
        return runCatching {
            val res = client.get(companyUrl(), config.jwtToken)
            if (!res.ok) {
                return@runCatching AlanubeStatus(
                    false,
                    error = when (res.code) {
                        401, 403 -> "Token invalido. Verifica tu JWT"
                        else -> "Error ${res.code}: ${res.body.take(200)}"
                    }
                )
            }
            val name = extractJsonValue(res.body, "name") ?: extractJsonValue(res.body, "companyName")
            AlanubeStatus(connected = true, companyName = name)
        }.getOrElse { AlanubeStatus(false, error = it.message ?: "Error de red") }
    }

    suspend fun getCompanyInfo(): AlanubeResult {
        if (config.jwtToken.isBlank()) return AlanubeResult(false, "JWT token requerido")
        return runCatching {
            val res = client.get(companyUrl(), config.jwtToken)
            AlanubeResult(
                success = res.ok,
                message = if (res.ok) "Informacion obtenida" else "Error ${res.code}",
                statusCode = res.code,
                responseBody = formatJson(res.body)
            )
        }.getOrElse { AlanubeResult(false, it.message ?: "Error de red") }
    }

    suspend fun emitInvoiceConsumption(
        encf: String,
        total: Double,
        paymentType: Int = 1,
        incomeType: Int = 1,
        items: List<Pair<String, Double>> = emptyList()
    ): AlanubeResult {
        if (config.jwtToken.isBlank()) return AlanubeResult(false, "JWT token requerido")

        val senderObj = buildJsonObject(
            "rnc" to "\"${config.rnc}\"",
            "companyName" to "\"${config.companyName}\"",
            "address" to "\"${config.address}\"",
            "stampDate" to "\"${config.stampDate}\""
        )

        val idDocObj = buildJsonObject(
            "encf" to "\"$encf\"",
            "incomeType" to "$incomeType",
            "paymentType" to "$paymentType"
        )

        val totalsObj = buildJsonObject(
            "totalAmount" to "$total"
        )

        val itemsArray = if (items.isEmpty()) {
            """[{"lineNumber":1,"itemName":"Venta POS","billingIndicator":1,"goodServiceIndicator":1,"quantityItem":1,"unitOfMeasure":1,"unitPriceItem":$total,"itemAmount":$total}]"""
        } else {
            items.mapIndexed { i, (desc, price) ->
                """{"lineNumber":${i+1},"itemName":"${desc.replace("\"","\\\"")}","billingIndicator":1,"goodServiceIndicator":1,"quantityItem":1,"unitOfMeasure":1,"unitPriceItem":$price,"itemAmount":$price}"""
            }.joinToString(",", "[", "]")
        }

        val body = """{${companyObj()}"idDoc":$idDocObj,"sender":$senderObj,"totals":$totalsObj,"itemDetails":$itemsArray}"""

        return runCatching {
            val res = client.post("${getBaseUrl()}/invoices", config.jwtToken, body)
            val docId = extractJsonValue(res.body, "id")
            val legalStatus = extractJsonValue(res.body, "legalStatus")
            val result = AlanubeResult(
                success = res.ok,
                message = if (res.ok) "Factura emitida exitosamente" else "Error ${res.code}",
                statusCode = res.code,
                responseBody = formatJson(res.body),
                documentId = docId,
                legalStatus = legalStatus
            )
            saveEmission("32", encf, total, result)
            result
        }.getOrElse { AlanubeResult(false, it.message ?: "Error de red") }
    }

    private fun saveEmission(docType: String, encf: String, total: Double, result: AlanubeResult) {
        val now = com.tmrestaurant.platform.localDateTimeLabel()
        AlanubeEmissionStore.add(
            AlanubeEmissionRecord(
                timestamp = now, docType = docType, encf = encf, total = total.toString(),
                documentId = result.documentId ?: "", legalStatus = result.legalStatus ?: "",
                success = result.success, responseBody = result.responseBody.take(500)
            )
        )
    }

    suspend fun emitCreditFiscalInvoice(
        encf: String,
        total: Double,
        buyerRnc: String,
        buyerName: String,
        paymentType: Int = 1,
        incomeType: Int = 1,
        items: List<Pair<String, Double>> = emptyList()
    ): AlanubeResult {
        if (config.jwtToken.isBlank()) return AlanubeResult(false, "JWT token requerido")

        val senderObj = buildJsonObject(
            "rnc" to "\"${config.rnc}\"",
            "companyName" to "\"${config.companyName}\"",
            "address" to "\"${config.address}\"",
            "stampDate" to "\"${config.stampDate}\""
        )

        val buyerObj = buildJsonObject(
            "rnc" to "\"${buyerRnc}\"",
            "companyName" to "\"${buyerName}\""
        )

        val idDocObj = buildJsonObject(
            "encf" to "\"$encf\"",
            "incomeType" to "$incomeType",
            "paymentType" to "$paymentType"
        )

        val totalsObj = buildJsonObject(
            "totalAmount" to "$total"
        )

        val itemsArray = if (items.isEmpty()) {
            """[{"lineNumber":1,"itemName":"Venta POS","billingIndicator":1,"goodServiceIndicator":1,"quantityItem":1,"unitOfMeasure":1,"unitPriceItem":$total,"itemAmount":$total}]"""
        } else {
            items.mapIndexed { i, (desc, price) ->
                """{"lineNumber":${i+1},"itemName":"${desc.replace("\"","\\\"")}","billingIndicator":1,"goodServiceIndicator":1,"quantityItem":1,"unitOfMeasure":1,"unitPriceItem":$price,"itemAmount":$price}"""
            }.joinToString(",", "[", "]")
        }

        val body = """{${companyObj()}"idDoc":$idDocObj,"sender":$senderObj,"buyer":$buyerObj,"totals":$totalsObj,"itemDetails":$itemsArray}"""

        return runCatching {
            val res = client.post("${getBaseUrl()}/fiscal-invoices", config.jwtToken, body)
            val docId = extractJsonValue(res.body, "id")
            val legalStatus = extractJsonValue(res.body, "legalStatus")
            val result = AlanubeResult(
                success = res.ok,
                message = if (res.ok) "Factura emitida exitosamente" else "Error ${res.code}",
                statusCode = res.code,
                responseBody = formatJson(res.body),
                documentId = docId,
                legalStatus = legalStatus
            )
            saveEmission("31", encf, total, result)
            result
        }.getOrElse { AlanubeResult(false, it.message ?: "Error de red") }
    }

    suspend fun checkDocumentStatus(documentId: String): AlanubeResult {
        if (config.jwtToken.isBlank()) return AlanubeResult(false, "JWT token requerido")
        return runCatching {
            val res = client.get(urlWithCompany("${getBaseUrl()}/invoices/$documentId"), config.jwtToken)
            val legalStatus = extractJsonValue(res.body, "legalStatus")
            AlanubeResult(
                success = res.ok,
                message = if (res.ok) "Estado: ${legalStatus ?: "desconocido"}" else "Error ${res.code}",
                statusCode = res.code,
                responseBody = formatJson(res.body),
                documentId = documentId,
                legalStatus = legalStatus
            )
        }.getOrElse { AlanubeResult(false, it.message ?: "Error de red") }
    }

    suspend fun checkFiscalInvoiceStatus(documentId: String): AlanubeResult {
        if (config.jwtToken.isBlank()) return AlanubeResult(false, "JWT token requerido")
        return runCatching {
            val res = client.get(urlWithCompany("${getBaseUrl()}/fiscal-invoices/$documentId"), config.jwtToken)
            val legalStatus = extractJsonValue(res.body, "legalStatus")
            AlanubeResult(
                success = res.ok,
                message = if (res.ok) "Estado: ${legalStatus ?: "desconocido"}" else "Error ${res.code}",
                statusCode = res.code,
                responseBody = formatJson(res.body),
                documentId = documentId,
                legalStatus = legalStatus
            )
        }.getOrElse { AlanubeResult(false, it.message ?: "Error de red") }
    }

    suspend fun emitGubernamentalInvoice(
        encf: String,
        total: Double,
        buyerRnc: String,
        buyerName: String,
        incomeType: Int = 1,
        paymentType: Int = 1
    ): AlanubeResult {
        if (config.jwtToken.isBlank()) return AlanubeResult(false, "JWT token requerido")

        val senderObj = buildJsonObject(
            "rnc" to "\"${config.rnc}\"",
            "companyName" to "\"${config.companyName}\"",
            "address" to "\"${config.address}\"",
            "stampDate" to "\"${config.stampDate}\""
        )

        val buyerObj = buildJsonObject(
            "rnc" to "\"${buyerRnc}\"",
            "companyName" to "\"${buyerName}\""
        )

        val idDocObj = buildJsonObject(
            "encf" to "\"$encf\"",
            "incomeType" to "$incomeType",
            "paymentType" to "$paymentType"
        )

        val totalsObj = buildJsonObject("totalAmount" to "$total")

        val itemsArray = """[{"lineNumber":1,"itemName":"Venta","billingIndicator":1,"goodServiceIndicator":1,"quantityItem":1,"unitOfMeasure":1,"unitPriceItem":$total,"itemAmount":$total}]"""

        val body = """{${companyObj()}"idDoc":$idDocObj,"sender":$senderObj,"buyer":$buyerObj,"totals":$totalsObj,"itemDetails":$itemsArray}"""

        return runCatching {
            val res = client.post("${getBaseUrl()}/gubernamental-invoices", config.jwtToken, body)
            val docId = extractJsonValue(res.body, "id")
            val legalStatus = extractJsonValue(res.body, "legalStatus")
            val result = AlanubeResult(
                success = res.ok,
                message = if (res.ok) "Factura gubernamental emitida" else "Error ${res.code}",
                statusCode = res.code,
                responseBody = formatJson(res.body),
                documentId = docId,
                legalStatus = legalStatus
            )
            saveEmission("45", encf, total, result)
            result
        }.getOrElse { AlanubeResult(false, it.message ?: "Error de red") }
    }

    suspend fun emitCreditNote(
        encf: String,
        total: Double,
        referenceEncF: String,
        buyerRnc: String,
        buyerName: String
    ): AlanubeResult {
        if (config.jwtToken.isBlank()) return AlanubeResult(false, "JWT token requerido")

        val senderObj = buildJsonObject(
            "rnc" to "\"${config.rnc}\"",
            "companyName" to "\"${config.companyName}\"",
            "address" to "\"${config.address}\"",
            "stampDate" to "\"${config.stampDate}\""
        )

        val buyerObj = buildJsonObject(
            "rnc" to "\"${buyerRnc}\"",
            "companyName" to "\"${buyerName}\""
        )

        val idDocObj = buildJsonObject(
            "encf" to "\"$encf\"",
            "incomeType" to "1",
            "paymentType" to "1"
        )

        val totalsObj = buildJsonObject("totalAmount" to "$total")

        val itemsArray = """[{"lineNumber":1,"itemName":"NC","billingIndicator":1,"goodServiceIndicator":1,"quantityItem":1,"unitOfMeasure":1,"unitPriceItem":$total,"itemAmount":$total}]"""

        val body = """{${companyObj()}"idDoc":$idDocObj,"sender":$senderObj,"buyer":$buyerObj,"totals":$totalsObj,"itemDetails":$itemsArray}"""

        return runCatching {
            val res = client.post("${getBaseUrl()}/credit-notes", config.jwtToken, body)
            val docId = extractJsonValue(res.body, "id")
            val legalStatus = extractJsonValue(res.body, "legalStatus")
            val result = AlanubeResult(
                success = res.ok,
                message = if (res.ok) "Nota de credito emitida" else "Error ${res.code}",
                statusCode = res.code,
                responseBody = formatJson(res.body),
                documentId = docId,
                legalStatus = legalStatus
            )
            saveEmission("34", encf, total, result)
            result
        }.getOrElse { AlanubeResult(false, it.message ?: "Error de red") }
    }

    suspend fun emitDebitNote(
        encf: String,
        total: Double,
        referenceEncF: String,
        buyerRnc: String,
        buyerName: String
    ): AlanubeResult {
        if (config.jwtToken.isBlank()) return AlanubeResult(false, "JWT token requerido")

        val senderObj = buildJsonObject(
            "rnc" to "\"${config.rnc}\"",
            "companyName" to "\"${config.companyName}\"",
            "address" to "\"${config.address}\"",
            "stampDate" to "\"${config.stampDate}\""
        )

        val buyerObj = buildJsonObject(
            "rnc" to "\"${buyerRnc}\"",
            "companyName" to "\"${buyerName}\""
        )

        val idDocObj = buildJsonObject(
            "encf" to "\"$encf\"",
            "incomeType" to "1",
            "paymentType" to "1"
        )

        val totalsObj = buildJsonObject("totalAmount" to "$total")

        val itemsArray = """[{"lineNumber":1,"itemName":"ND","billingIndicator":1,"goodServiceIndicator":1,"quantityItem":1,"unitOfMeasure":1,"unitPriceItem":$total,"itemAmount":$total}]"""

        val body = """{${companyObj()}"idDoc":$idDocObj,"sender":$senderObj,"buyer":$buyerObj,"totals":$totalsObj,"itemDetails":$itemsArray}"""

        return runCatching {
            val res = client.post("${getBaseUrl()}/debit-notes", config.jwtToken, body)
            val docId = extractJsonValue(res.body, "id")
            val legalStatus = extractJsonValue(res.body, "legalStatus")
            val result = AlanubeResult(
                success = res.ok,
                message = if (res.ok) "Nota de debito emitida" else "Error ${res.code}",
                statusCode = res.code,
                responseBody = formatJson(res.body),
                documentId = docId,
                legalStatus = legalStatus
            )
            saveEmission("33", encf, total, result)
            result
        }.getOrElse { AlanubeResult(false, it.message ?: "Error de red") }
    }

    suspend fun checkDGIIHealth(): AlanubeResult {
        if (config.jwtToken.isBlank()) return AlanubeResult(false, "JWT token requerido")
        return runCatching {
            val res = client.get(urlWithCompany("${getBaseUrl()}/dgii/status"), config.jwtToken)
            AlanubeResult(
                success = res.ok,
                message = if (res.ok) "DGII operativa" else "DGII no disponible",
                statusCode = res.code,
                responseBody = formatJson(res.body)
            )
        }.getOrElse { AlanubeResult(false, it.message ?: "Error de red") }
    }

    private fun extractJsonValue(json: String, key: String): String? {
        val regex = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        val m = regex.find(json) ?: return null
        return m.groupValues[1]
    }

    private fun formatJson(json: String): String {
        return try {
            var indent = 0
            val sb = StringBuilder()
            var inStr = false
            for (ch in json) {
                if (ch == '"' && (sb.isEmpty() || sb.last() != '\\')) inStr = !inStr
                if (!inStr) when (ch) {
                    '{', '[' -> { sb.append(ch); sb.append('\n'); indent++; sb.append("  ".repeat(indent)) }
                    '}', ']' -> { sb.append('\n'); indent--; sb.append("  ".repeat(indent)); sb.append(ch) }
                    ',' -> { sb.append(ch); sb.append('\n'); sb.append("  ".repeat(indent)) }
                    ':' -> sb.append(": ")
                    else -> sb.append(ch)
                } else sb.append(ch)
            }
            sb.toString()
        } catch (_: Exception) { json }
    }
}
