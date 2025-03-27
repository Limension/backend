package net.blophy.workspace.models

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.blophy.workspace.Config
import net.blophy.workspace.manager.logger
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

fun configureDatabases() {
    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://${Config.dbUrl}/${Config.dbName}"
        username = Config.dbUsername
        password = Config.dbPassword
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
    })

    val db = Database.Companion.connect(dataSource)
    TransactionManager.Companion.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    TransactionManager.Companion.defaultDatabase = db
}

val partitionsSource = HikariDataSource(HikariConfig().apply {
    jdbcUrl = Config.partitionsDbUrl
    username = Config.partitionsDbUsername
    password = Config.partitionsDbPassword
    driverClassName = "org.postgresql.Driver"
    maximumPoolSize = 10
})

val partitionsDb = Database.Companion.connect(partitionsSource)

private object PgTables : IntIdTable("pg_tables") {
    val pgTableName = varchar("tablename", 255)
    val pgSchemaName = varchar("schemaname", 255).default("public")
}

fun createMissingTablesForPartitionDb() {
    // 查询 public 模式下的所有表名
    val existRankTableNames = transaction(partitionsDb) {
        PgTables
            .select(PgTables.pgTableName)
            .where { PgTables.pgSchemaName eq "public" }
            .map { it[PgTables.pgTableName].toInt() }
            .toSet()
    }

    transaction {
        return@transaction Projects.selectAll()
            .map { it[Projects.id].value }
            .toList()
    }.filterNot { it in existRankTableNames }
        .forEach {
            logger.info("Creating missing tables $it...")
            transaction(partitionsDb) {
                SchemaUtils.create(Partitions(it.toString()))
            }
        }
}