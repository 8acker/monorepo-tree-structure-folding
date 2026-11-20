package com.eightackerbird.intellij.plugins.monorepo.structurefolding

data class ProjectGroup(val name: String, val projects: List<String> = emptyList()) {
    private val isNotEmpty: Boolean = name != ""

    fun includes(directory: String): Boolean = projects.contains(directory)

    fun ifNotEmpty(action: (ProjectGroup) -> Unit) {
        if (isNotEmpty) {
            action(this)
        }
    }

    companion object {
        val EMPTY = ProjectGroup(name = "")
    }
}