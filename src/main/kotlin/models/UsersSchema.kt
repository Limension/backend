package net.blophy.workspace.models

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

object Users : IntIdTable("users") {
    val name = text("name")
    val password = text("password")
    val email = text("email")
    val verified = bool("verified").default(false)
    val extraEmails = array<String>("extra_emails").default(listOf())
    val group = array<Int>("group").default(listOf(0))
    val totp = text("totp").nullable()
    val webauthn = text("webauthn").nullable()
}

class UsersEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UsersEntity>(Users)

    var name by Users.name
    var password by Users.password
    var email by Users.email
    var verified by Users.verified
    var extraEmails by Users.extraEmails
    var group by Users.group
    var totp by Users.totp
    var webauthn by Users.webauthn
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

@Serializable
data class NewUser(
    val username: String,
    val email: String,
    val password: String
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

object UserService {

    fun new(init: NewUser) {
        UsersEntity.new {
            name = init.username
            password = init.password
            email = init.email
            extraEmails = emptyList()
            verified = false
            group = listOf(0)
            totp = null
            webauthn = null
        }
    }

    fun findUserByUsername(username: String) =
        UsersEntity.find { Users.name eq username }.singleOrNull()

    fun findUserByEmail(email: String) =
        UsersEntity.find { Users.email eq email }.singleOrNull()

    fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(12))

    fun checkPassword(userId: Int, passwd: String) =
        UsersEntity[userId].password.let { BCrypt.checkpw(passwd, it) }

    suspend fun ApplicationCall.changePassword(userId: Int, oldPass: String, newPass: String) {
        val isPasswordValid = checkPassword(userId, oldPass)

        if (!isPasswordValid) {
            this.respond(HttpStatusCode.Unauthorized)
            return
        }

        val hashedPassword = hashPassword(newPass)
        transaction {
            UsersEntity.findByIdAndUpdate(userId) {
                it.password = hashedPassword
            }
        }

        this.respond(HttpStatusCode.OK)
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
