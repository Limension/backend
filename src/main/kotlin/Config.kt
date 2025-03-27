package net.blophy.workspace

import dev.adamko.kxstsgen.KxsTsGenerator
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking
import kotlinx.io.IOException
import net.blophy.workspace.manager.logger
import net.blophy.workspace.models.*
import java.io.File

object Config {
    // 通过延迟加载确保单例初始化安全
    lateinit var environment: ApplicationEnvironment

    fun getConfig(env: String, default: String, key: String? = null) =
        System.getenv(env)?.toString()
            ?: key?.let { environment.config.propertyOrNull(it)?.toString() }
            ?: default

    // Token 配置
    val tokenExpireAt: Long by lazy {
        getConfig("TOKEN_EXPIRE_AT", (6_048_000_000L).toString(), "limension.security.token_expire").toLong()
    }

    val enableRegistration: Boolean by lazy {
        getConfig("ENABLE_REGISTRATION", true.toString(), "limension.security.enable_registration").toBoolean()
    }

    // 数据库配置
    val dbUrl: String by lazy {
        getConfig("DB_URL", "localhost:5432", "limension.database.url")
    }

    val dbName: String by lazy {
        getConfig("DB_NAME", "limension", "limension.database.name")
    }

    val dbUsername: String by lazy {
        getConfig("DB_USER", "limension", "limension.database.user")
    }

    val dbPassword: String by lazy {
        getConfig("DB_PASSWORD", "limension")
    }

    val partitionsDbUrl: String by lazy {
        getConfig("PRDB_URL", "localhost:5432", "limension.database.partition.url")
    }

    val partitionsDbName: String by lazy {
        getConfig("PRDB_NAME", "limension_partitions", "limension.database.partition.name")
    }

    val partitionsDbUsername: String by lazy {
        getConfig("PRDB_USER", "limension", "limension.database.partition.user")
    }

    val partitionsDbPassword: String by lazy {
        getConfig("PRDB_PASSWORD", "localhost:5432")
    }

    // Redis 配置
    val redis: String by lazy {
        getConfig("REDIS_URL", "localhost:6379", "limension.redis")
    }

    // 邮件配置
    val smtpHost: String? by lazy {
        System.getenv("SMTP_HOST") ?: environment.config.propertyOrNull("limension.ext.smtp.host")?.getString()
    }

    val smtpPort: String? by lazy {
        System.getenv("SMTP_PORT") ?: environment.config.propertyOrNull("limension.ext.smtp.port")?.getString()
    }

    val smtpUsername: String? by lazy {
        System.getenv("SMTP_USERNAME") ?: environment.config.propertyOrNull("limension.ext.smtp.username")?.getString()
    }

    val smtpPassword: String? by lazy {
        System.getenv("SMTP_PASSWORD")
    }

    val tsTypeGeneratePath: String by lazy {
        getConfig("TS_GENERATE_PATH", "generated", "limension.ext.dev.type_generating.path")
    }

    val commonEmailService: Set<String> = setOf(
        // 腾讯
        "qq.com",
        "vip.qq.com",
        "foxmail.com",
        // 网易
        "163.com",
        "126.com",
        "yeah.net",
        "vip.163.com",
        "vip.126.com",
        "188.com",
        // 阿里
        "aliyun.com",
        // 新浪
        "sina.com",
        "sina.cn",
        "vip.sina.com",
        "vip.sina.cn",
        // 运营商
        "189.com", // 电信
        "139.com", // 移动
        // 国外
        "outlook.com",
        "gmail.com",
        "yandex.com",
        "yahoo.com",
        "myyahoo.com",
        // 友链(?
        "zyghit.cn",
        "milthm.cn",
        "morizero.com"
    )
    val trustedEmailService: Set<String> = setOf("blophy.net")

    val blockedEmailService: Set<String> by lazy {
        try {
            runBlocking {
                HttpClient().use { client ->
                    client.get("https://raw.githubusercontent.com/disposable/disposable-email-domains/master/domains_strict.txt")
                        .bodyAsText().lines().toSet()
                }
            }
        } catch (e: IOException) {
            logger.warn("Failed to load domains: $e")
            emptySet()
        }
    }

    init {
        // 生成TypeScript类型绑定
        File("${tsTypeGeneratePath}/partition.ts").writeText(KxsTsGenerator().generate(Partition.serializer()))
        File("${tsTypeGeneratePath}/project.ts").writeText(KxsTsGenerator().generate(Project.serializer()))
        File("${tsTypeGeneratePath}/new-project.ts").writeText(KxsTsGenerator().generate(NewProject.serializer()))
        File("${tsTypeGeneratePath}/user.ts").writeText(KxsTsGenerator().generate(User.serializer()))
        File("${tsTypeGeneratePath}/mission.ts").writeText(KxsTsGenerator().generate(Mission.serializer()))
    }
}
