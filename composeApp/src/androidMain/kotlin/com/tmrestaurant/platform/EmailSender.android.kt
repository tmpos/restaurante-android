package com.tmrestaurant.platform

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

private fun isBase64(s: String): Boolean =
    s.matches(Regex("^[A-Za-z0-9+/]*={0,2}$")) && s.length % 4 == 0

actual fun sendEmail(
    config: SmtpConfig,
    fromName: String,
    fromAddr: String,
    to: String,
    subject: String,
    body: String,
    attachments: List<EmailAttachment>
): EmailResult {
    return try {
        val socket = if (config.useSsl) {
            SSLSocketFactory.getDefault().createSocket(config.host, config.port) as SSLSocket
        } else {
            java.net.Socket(config.host, config.port)
        }
        socket.soTimeout = 15000
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val writer: Writer = OutputStreamWriter(socket.getOutputStream())

        fun read(): String = reader.readLine() ?: ""
        fun write(cmd: String) { writer.write("$cmd\r\n"); writer.flush() }

        // Read greeting
        var resp = read()

        // EHLO
        write("EHLO TM-POS")
        resp = read()
        var supportsStartTls = false
        while (resp.startsWith("250-")) {
            if (resp.uppercase().contains("STARTTLS")) supportsStartTls = true
            resp = read()
        }

        // STARTTLS (if not already SSL and server supports it)
        if (!config.useSsl && supportsStartTls) {
            write("STARTTLS")
            resp = read()
            if (!resp.startsWith("220")) {
                reader.close(); writer.close(); socket.close()
                return EmailResult(false, "Error STARTTLS: $resp")
            }
            val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
            val sslSocket = factory.createSocket(socket, config.host, config.port, true) as SSLSocket
            sslSocket.soTimeout = 15000
            sslSocket.startHandshake()
            val newReader = BufferedReader(InputStreamReader(sslSocket.getInputStream()))
            val newWriter: Writer = OutputStreamWriter(sslSocket.getOutputStream())
            newWriter.write("EHLO TM-POS\r\n"); newWriter.flush()
            var r = newReader.readLine() ?: ""
            while (r.startsWith("250-")) r = newReader.readLine() ?: ""
        }

        // AUTH LOGIN
        write("AUTH LOGIN")
        resp = read()

        // Username (always needs base64 encoding)
        write(android.util.Base64.encodeToString(config.username.toByteArray(), android.util.Base64.NO_WRAP))
        resp = read()

        // Password: auto-detect if already base64
        val passwordB64 = if (isBase64(config.password)) {
            config.password
        } else {
            android.util.Base64.encodeToString(config.password.toByteArray(), android.util.Base64.NO_WRAP)
        }
        write(passwordB64)
        resp = read()
        if (!resp.startsWith("235")) {
            reader.close(); writer.close(); socket.close()
            return EmailResult(false, "Autenticacion SMTP fallo: $resp")
        }

        // MAIL FROM (must match authenticated user)
        write("MAIL FROM:<${fromAddr}>")
        resp = read()
        if (!resp.startsWith("250")) {
            reader.close(); writer.close(); socket.close()
            return EmailResult(false, "Error MAIL FROM: $resp")
        }

        // RCPT TO
        write("RCPT TO:<${to}>")
        resp = read()
        if (!resp.startsWith("250")) {
            reader.close(); writer.close(); socket.close()
            return EmailResult(false, "Error RCPT TO: $resp")
        }

        // DATA
        write("DATA")
        resp = read()
        if (!resp.startsWith("354")) {
            reader.close(); writer.close(); socket.close()
            return EmailResult(false, "Error DATA: $resp")
        }

        // Email content
        val message = if (attachments.isEmpty()) {
            buildString {
                appendLine("From: $fromName <$fromAddr>")
                appendLine("To: <${to}>")
                appendLine("Subject: $subject")
                appendLine("MIME-Version: 1.0")
                appendLine("Content-Type: text/html; charset=UTF-8")
                appendLine()
                appendLine(body)
            }
        } else {
            val boundary = "TM-POS-${System.currentTimeMillis()}"
            buildString {
                appendLine("From: $fromName <$fromAddr>")
                appendLine("To: <${to}>")
                appendLine("Subject: $subject")
                appendLine("MIME-Version: 1.0")
                appendLine("Content-Type: multipart/mixed; boundary=\"$boundary\"")
                appendLine()
                appendLine("--$boundary")
                appendLine("Content-Type: text/html; charset=UTF-8")
                appendLine("Content-Transfer-Encoding: 8bit")
                appendLine()
                appendLine(body)
                attachments.forEach { attachment ->
                    appendLine("--$boundary")
                    appendLine("Content-Type: ${attachment.mimeType}; name=\"${attachment.fileName}\"")
                    appendLine("Content-Transfer-Encoding: base64")
                    appendLine("Content-Disposition: attachment; filename=\"${attachment.fileName}\"")
                    appendLine()
                    appendLine(android.util.Base64.encodeToString(attachment.bytes, android.util.Base64.NO_WRAP))
                }
                appendLine("--$boundary--")
            }
        }
        write(message)
        write(".")

        resp = read()

        // QUIT
        write("QUIT")
        reader.close()
        writer.close()
        socket.close()

        if (resp.startsWith("250") || resp.startsWith("2")) {
            EmailResult(true)
        } else {
            EmailResult(false, "Error al enviar mensaje: $resp")
        }
    } catch (e: Exception) {
        EmailResult(false, e.message ?: "Error SMTP")
    }
}
