package net.blophy.workspace.manager

import io.ktor.util.logging.*
import net.blophy.workspace.Settings
import net.blophy.workspace.models.useCache
import java.util.*
import java.util.concurrent.TimeUnit
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

val logger = KtorSimpleLogger("net.blophy.forum.utils.ev")

class EmailService(
    private val smtpHost: String,
    private val smtpPort: String,
    private val username: String,
    private val password: String
) {

    private fun createSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.ssl.enable", "true")
            put("mail.smtp.host", smtpHost)
            put("mail.smtp.port", smtpPort)
        }

        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })
    }

    fun sendEmail(to: String, subject: String, body: String): Boolean {
        try {
            val session = createSession()
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                addRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                setSubject(subject)
                setText(body)
            }

            Transport.send(message)
            logger.info("Email sent successfully to $to")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            logger.error("Failed to send email to $to")
            return false
        }
    }
}


object CodeMan {

    suspend fun generateCode(email: String): String {
        val code = UUID.randomUUID().toString().take(6) // 生成6位随机验证码
        storeCode(email, code)
        return code
    }

    private suspend fun storeCode(email: String, code: String) {
        useCache {
            // 将验证码存储在Redis中，设置过期时间为10分钟
            it.set(email, code, TimeUnit.MINUTES.toSeconds(10).toULong())
            // 60秒内不得重复请求验证码
            it.set("look:${email}", "1", TimeUnit.MINUTES.toSeconds(1).toULong())
        }
    }

    suspend fun verifyCode(email: String, code: String): Boolean {
        return useCache {
            val storedCode = it.get(email)
            return@useCache if (storedCode != null && storedCode == code.trim()) {
                // 验证成功后删除验证码
                it.del(email)
                true
            } else false
        }
    }

    fun genMessage(username: String, code: String) =
        "Hi $username,\nAn action performed on your account requires verification.\n\nYour verification code is: $code\nYou can enter the code with or without spaces.\n\nIf you did not request this, please REPLY IMMEDIATELY as your account may be in danger.\n\n--\nLime Network | https://workspace.blophy.net"

    fun genRegisterMessage(username: String, code: String) =
        "Hi $username,\nYou are requesting verification of an email address.\n\nYour verification code is: $code\nYou can enter the code with or without spaces.\n\nIf you did not request this, please REPLY IMMEDIATELY as your account may be in danger.\n\n--\nLime Network | https://workspace.blophy.net"
}

object VerificationCodeManager {
    private val service = if (Settings.smtpHost != null) EmailService(
        Settings.smtpHost, Settings.smtpPort.toString(), Settings.smtpUsername, Settings.smtpPassword
    ) else null

    suspend fun genCode(username: String, email: String): Int {
        if (useCache { it.exists("look:$email") }) return -1
        val code = CodeMan.generateCode(email)
        val message = CodeMan.genMessage(username, code)
        if (service != null) {
            logger.debug("sending verification code to $email: $code")
            return if (service.sendEmail(email, "Lime Network Verification Code", message)) 0 else 1
        } else {
            logger.debug("verification code to $email: $code")
            return 0
        }
    }

    suspend fun registerCode(username: String, email: String): Int {
        if (useCache { it.exists("look:$email") || it.exists(email) }) return -1
        val code = CodeMan.generateCode(email)
        val message = CodeMan.genRegisterMessage(username, code)
        if (service != null) {
            logger.debug("sending registration code to $email: $code")
            return if (service.sendEmail(email, "Lime Network Account", message)) 0 else 1
        } else {
            logger.debug("registration code to $email: $code")
            return 0
        }
    }

    suspend fun verifyCode(email: String, code: String, type: String): Boolean {
        if (CodeMan.verifyCode(email, code.trim().lowercase())) {
            useCache {
                // 验证后120秒内有效
                it.set("$type:$email", "1", TimeUnit.MINUTES.toSeconds(2).toULong())
                return@useCache true
            }
        }
        return false
    }
}
