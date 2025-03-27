package net.blophy.workspace.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.selectAll

class Partitions(projectId: String) : IntIdTable(projectId) {
    val name = text("name")
    val description = text("description")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

@Serializable
data class Partition(
    val id: Int,
    val name: String,
    val description: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

fun Partition.getMissions(): List<Mission> =
    Missions.selectAll().where { Missions.partition eq id }.map { it.toMission() }

fun ResultRow.toPartition(id: String) = Partition(
    id = this[Partitions(id).id].value,
    name = this[Partitions(id).name],
    description = this[Partitions(id).description],
    createdAt = this[Partitions(id).createdAt],
    updatedAt = this[Partitions(id).updatedAt]
)
