import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection.TRANSACTION_SERIALIZABLE
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.blophy.workspace.Config


fun configureDatabases() {
    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://${Config.dbUrl}/${Config.dbName}"
        username = Config.dbUsername
        password = Config.dbPassword
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
    })

    val db = Database.connect(dataSource)
    TransactionManager.manager.defaultIsolationLevel = TRANSACTION_SERIALIZABLE
    TransactionManager.defaultDatabase = db
}
