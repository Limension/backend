package net.blophy.workspace.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll

object Projects : IntIdTable("projects") {
    val name = text("name")
    val public = bool("public").default(false)
    val maintainer = integer("maintainer")
    val admins = array<Int>("admins")
    val description = text("description")
    val members = array<Int>("members")
    val guests = array<Int>("guests")
    val blackList = array<Int>("blacklist")
}

class ProjectsEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProjectsEntity>(Projects)

    var name by Projects.name
    var public by Projects.public
    var maintainer by Projects.maintainer
    var admins by Projects.admins
    var description by Projects.description
    var members by Projects.members
    var guests by Projects.guests
    var blackList by Projects.blackList
}

@Serializable
data class Project(
    val id: Int,
    val name: String,
    val public: Boolean,
    val maintainer: Int,
    val admins: List<Int>,
    val description: String,
    val members: List<Int>,
    val guests: List<Int>,
    val blackList: List<Int>,
)

@Serializable
data class NewProject(
    val name: String,
    val maintainer: Int,
    val public: Boolean = false,
    val description: String = "New Project",
)

fun ResultRow.toProject() = Project(
    id = this[Projects.id].value,
    name = this[Projects.name],
    public = this[Projects.public],
    maintainer = this[Projects.maintainer],
    admins = this[Projects.admins],
    description = this[Projects.description],
    members = this[Projects.members],
    guests = this[Projects.guests],
    blackList = this[Projects.blackList]
)

fun Project.getPartitions(): List<Partition> =
    Partitions(id.toString()).selectAll().map { it.toPartition(id.toString()) }

object ProjectsService {

    fun new(data: NewProject) {
        ProjectsEntity.new {
            name = data.name
            description = data.description
            public = data.public
            maintainer = data.maintainer
            admins = emptyList()
            members = emptyList()
            guests = emptyList()
            blackList = emptyList()
        }
    }

    fun delete(id: Int) {
        ProjectsEntity[id].delete()
    }

    fun edit(data: Project) {
        ProjectsEntity.findByIdAndUpdate(data.id) {
            it.name = data.name
            it.public = data.public
            it.maintainer = data.maintainer
            it.members = data.members
            it.admins = data.admins
            it.description = data.description
            it.blackList = data.blackList
            it.guests = data.guests
        }
    }
}
