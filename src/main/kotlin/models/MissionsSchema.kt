package net.blophy.workspace.models

import dev.adamko.kxstsgen.KxsTsGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import net.blophy.workspace.Config
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.io.File

object Missions : IntIdTable("projects") {
    val name = text("name")
    val description = text("description")
    val present = integer("present")
    val assignees = array<Int>("assignees")
    val contributors = array<Int>("contributors")
    val urgency = integer("urgency")
    val status = integer("status")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    val partition = integer("partition")
}

class MissionsEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MissionsEntity>(Missions)

    var name by Missions.name
    var description by Missions.description
    var present by Missions.present
    var assignees by Missions.assignees
    var contributors by Missions.contributors
    var urgency by Missions.urgency
    var status by Missions.status
    var createdAt by Missions.createdAt
    var updatedAt by Missions.updatedAt
    var partition by Missions.partition
}

@Serializable
data class Mission(
    val id: Int,
    val name: String,
    val description: String,
    val present: Int,
    val assignees: List<Int>,
    val contributors: List<Int>,
    val urgency: Int,
    val status: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val partition: Int,
)

fun ResultRow.toMission() = Mission(
    id = this[Missions.id].value,
    name = this[Missions.name],
    description = this[Missions.description],
    present = this[Missions.present],
    assignees = this[Missions.assignees],
    contributors = this[Missions.contributors],
    urgency = this[Missions.urgency],
    status = this[Missions.status],
    createdAt = this[Missions.createdAt],
    updatedAt = this[Missions.updatedAt],
    partition = this[Missions.partition],
)

class MissionsService {
    init {
        //arrayOf<Table>(Missions)
        File("${Config.tsTypeGeneratePath}/mission.ts").writeText(KxsTsGenerator().generate(Mission.serializer()))
    }

    suspend fun new() = {
        MissionsEntity.new { }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

}