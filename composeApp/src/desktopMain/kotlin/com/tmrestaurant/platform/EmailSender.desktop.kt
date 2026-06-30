package com.tmrestaurant.platform

actual fun sendEmail(
    config: SmtpConfig,
    fromName: String,
    fromAddr: String,
    to: String,
    subject: String,
    body: String,
    attachments: List<EmailAttachment>
): EmailResult {
    println("--- EMAIL (desktop stub) ---")
    println("To: $to")
    println("Subject: $subject")
    println("Body: $body")
    println("Attachments: ${attachments.joinToString { it.fileName }}")
    println("---")
    return EmailResult(true)
}
