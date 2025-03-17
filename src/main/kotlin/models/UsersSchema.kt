package net.blophy.workspace.models

import dev.adamko.kxstsgen.KxsTsGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import net.blophy.workspace.Config
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.io.File

object Users : IntIdTable() {
    val name = text("name")
    val password = text("password")
    val email = text("email")
    val verified = bool("verified").default(false)
    val extraEmails = array<String>("extra_emails").default(listOf())
    val group = array<Int>("group").default(listOf(0))
    val totp = text("totp").nullable()
    val webauthn = text("webauthn").nullable()
}

@Serializable
data class User(
    val id: Int,
    val username: String,
    val password: String,
    val email: String,
    val verified: Boolean,
    val extraEmail: List<String>,
    val group: List<Int>,
    val totp: String?,
    val webauthn: String?
)

fun ResultRow.toUser() = User(
    id = this[Users.id].value,
    username = this[Users.name],
    password = this[Users.password],
    email = this[Users.email],
    verified = this[Users.verified],
    extraEmail = this[Users.extraEmails],
    group = this[Users.group],
    totp = this[Users.totp],
    webauthn = this[Users.webauthn],
)

class UserService {

    init {
        /*transaction {
            arrayOf<Table>(Users)
        }*/
        File("${Config.tsTypeGeneratePath}/user.ts").writeText(KxsTsGenerator().generate(User.serializer()))
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
