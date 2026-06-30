package com.tmrestaurant.platform

data class SmtpConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val useSsl: Boolean = true
)

data class EmailResult(
    val success: Boolean,
    val error: String = ""
)

data class EmailAttachment(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray
)

expect fun sendEmail(
    config: SmtpConfig,
    fromName: String,
    fromAddr: String,
    to: String,
    subject: String,
    body: String,
    attachments: List<EmailAttachment> = emptyList()
): EmailResult
