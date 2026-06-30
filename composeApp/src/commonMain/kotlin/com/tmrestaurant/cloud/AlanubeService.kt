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

    private fun urlWithCompany(path: String): String =
        if (config.companyId.isNotBlank()) "$path?idCompany=${config.companyId}" else path

    suspend fun testConnection(): AlanubeStatus {
        if (config.jwtToken.isBlank()) return AlanubeStatus(false, error = "JWT token requerido")
        return runCatching {
            val res = client.get(urlWithCompany("${getBaseUrl()}/company"), config.jwtToken)
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
            val res = client.get(urlWithCompany("${getBaseUrl()}/company"), config.jwtToken)
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
            """[{"itemSequence":1,"itemDescription":"Venta POS","quantity":1,"unitOfMeasure":1,"unitPrice":$total,"totalAmount":$total}]"""
        } else {
            items.mapIndexed { i, (desc, price) ->
                """{"itemSequence":${i+1},"itemDescription":"${desc.replace("\"","\\\"")}","quantity":1,"unitOfMeasure":1,"unitPrice":$price,"totalAmount":$price}"""
            }.joinToString(",", "[", "]")
        }

        val body = buildJsonObject(
            "idDoc" to idDocObj,
            "sender" to senderObj,
            "totals" to totalsObj,
            "itemDetails" to itemsArray
        )

        return runCatching {
            val res = client.post(urlWithCompany("${getBaseUrl()}/invoices"), config.jwtToken, body)
            val docId = extractJsonValue(res.body, "id")
            val legalStatus = extractJsonValue(res.body, "legalStatus")
            AlanubeResult(
                success = res.ok,
                message = if (res.ok) "Factura emitida exitosamente" else "Error ${res.code}",
                statusCode = res.code,
                responseBody = formatJson(res.body),
                documentId = docId,
                legalStatus = legalStatus
            )
        }.getOrElse { AlanubeResult(false, it.message ?: "Error de red") }
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
            """[{"itemSequence":1,"itemDescription":"Venta POS","quantity":1,"unitOfMeasure":1,"unitPrice":$total,"totalAmount":$total}]"""
        } else {
            items.mapIndexed { i, (desc, price) ->
                """{"itemSequence":${i+1},"itemDescription":"${desc.replace("\"","\\\"")}","quantity":1,"unitOfMeasure":1,"unitPrice":$price,"totalAmount":$price}"""
            }.joinToString(",", "[", "]")
        }

        val body = buildJsonObject(
            "idDoc" to idDocObj,
            "sender" to senderObj,
            "buyer" to buyerObj,
            "totals" to totalsObj,
            "itemDetails" to itemsArray
        )

        return runCatching {
            val res = client.post(urlWithCompany("${getBaseUrl()}/fiscal-invoices"), config.jwtToken, body)
            val docId = extractJsonValue(res.body, "id")
            val legalStatus = extractJsonValue(res.body, "legalStatus")
            AlanubeResult(
                success = res.ok,
                message = if (res.ok) "Factura emitida exitosamente" else "Error ${res.code}",
                statusCode = res.code,
                responseBody = formatJson(res.body),
                documentId = docId,
                legalStatus = legalStatus
            )
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

        val itemsArray = """[{"itemSequence":1,"itemDescription":"Venta","quantity":1,"unitOfMeasure":1,"unitPrice":$total,"totalAmount":$total}]"""

        val body = buildJsonObject(
            "idDoc" to idDocObj,
            "sender" to senderObj,
            "buyer" to buyerObj,
            "totals" to totalsObj,
            "itemDetails" to itemsArray
        )

        return runCatching {
            val res = client.post(urlWithCompany("${getBaseUrl()}/gubernamental-invoices"), config.jwtToken, body)
            AlanubeResult(
                success = res.ok,
                message = if (res.ok) "Factura gubernamental emitida" else "Error ${res.code}",
                statusCode = res.code,
                responseBody = formatJson(res.body),
                documentId = extractJsonValue(res.body, "id"),
                legalStatus = extractJsonValue(res.body, "legalStatus")
            )
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

        val itemsArray = """[{"itemSequence":1,"itemDescription":"NC","quantity":1,"unitOfMeasure":1,"unitPrice":$total,"totalAmount":$total}]"""

        val body = buildJsonObject(
            "idDoc" to idDocObj,
            "sender" to senderObj,
            "buyer" to buyerObj,
            "totals" to totalsObj,
            "itemDetails" to itemsArray
        )

        return runCatching {
            val res = client.post(urlWithCompany("${getBaseUrl()}/credit-notes"), config.jwtToken, body)
            AlanubeResult(
                success = res.ok,
                message = if (res.ok) "Nota de credito emitida" else "Error ${res.code}",
                statusCode = res.code,
                responseBody = formatJson(res.body),
                documentId = extractJsonValue(res.body, "id"),
                legalStatus = extractJsonValue(res.body, "legalStatus")
            )
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

        val itemsArray = """[{"itemSequence":1,"itemDescription":"ND","quantity":1,"unitOfMeasure":1,"unitPrice":$total,"totalAmount":$total}]"""

        val body = buildJsonObject(
            "idDoc" to idDocObj,
            "sender" to senderObj,
            "buyer" to buyerObj,
            "totals" to totalsObj,
            "itemDetails" to itemsArray
        )

        return runCatching {
            val res = client.post(urlWithCompany("${getBaseUrl()}/debit-notes"), config.jwtToken, body)
            AlanubeResult(
                success = res.ok,
                message = if (res.ok) "Nota de debito emitida" else "Error ${res.code}",
                statusCode = res.code,
                responseBody = formatJson(res.body),
                documentId = extractJsonValue(res.body, "id"),
                legalStatus = extractJsonValue(res.body, "legalStatus")
            )
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
