package com.eightackerbird.intellij.plugins.monorepo.structurefolding

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class ProjectStructure(structureFilePath: Path) {
    private var locationPath: Path = structureFilePath

    private var projectGroups: List<ProjectGroup> = loadProjectGroups()

    val value: String
        get() = projectGroups.joinToString("\n") { group ->
            "${group.name}\n${group.projects.joinToString("\n") { project -> "+---$project" }}"
        }

    fun updateLocation(location: String) {
        val path = Path.of(location)
        val groups = loadProjectGroups(path)
        this.locationPath = path
        this.projectGroups = groups
    }

    fun updateStructure(content: String) {
        val groups = parseProjectGroups(content)
        this.locationPath.writeText(content, Charsets.UTF_8)
        this.projectGroups = groups
    }

    private fun loadProjectGroups(path: Path = locationPath): List<ProjectGroup> {
        println("Loading project structure from $path")
        val content = if(path.exists()) path.readText(Charsets.UTF_8) else return emptyList()
        return parseProjectGroups(content)
    }

    private fun parseProjectGroups(content: String): List<ProjectGroup> {
        val lines = content.split("\n")
            .filter { it.isNotBlank() }
            .map { it.trim() }
        var projectGroup = ProjectGroup.EMPTY
        val result = lines.fold(mutableListOf<ProjectGroup>()) { acc, line ->
            if (line.startsWith("+---")) {
                val projectName = line.substring(4)
                projectGroup = projectGroup.copy(projects = projectGroup.projects + projectName)
            } else {
                projectGroup.ifNotEmpty { acc.add(it) }
                projectGroup = ProjectGroup(name = line)
            }
            acc
        }
        projectGroup.ifNotEmpty { result.add(it) }
        return result
    }

    fun allGroups(): List<ProjectGroup> = projectGroups
}