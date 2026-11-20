package com.eightackerbird.intellij.plugins.monorepo.structurefolding

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import kotlin.io.path.Path

@State(name = "PluginSettingsState", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class PluginSettingsState(private val project: Project) : PersistentStateComponent<PluginSettingsState.Settings> {
    data class Settings(var projectStructureLocation: String = "")

    private var settings = Settings()
    private val structure = lazy { ProjectStructure(Path(projectStructureLocation)) }

    val projectStructureLocation: String
        get() = resolveLocation(settings.projectStructureLocation)

    val projectStructure: String
        get() = structure.value.value

    fun updateLocation(location: String) {
        structure.value.updateLocation(location)
        settings.projectStructureLocation = location
    }

    fun updateStructure(content: String) {
        structure.value.updateStructure(content)
    }

    fun allGroups(): List<ProjectGroup> = structure.value.allGroups()

    override fun getState(): Settings = settings

    override fun loadState(state: Settings) {
        if (structure.isInitialized()) {
            structure.value.updateLocation(resolveLocation(state.projectStructureLocation))
        }
        settings = state
    }

    private fun resolveLocation(location: String): String {
        if (location.isNotEmpty()) {
            return location
        }
        val basePath = checkNotNull(project.basePath) { "Cannot determine the workspace root for the project" }
        return Path(basePath).resolve("project-structure.txt").toString()
    }

    companion object {
        fun getInstance(project: Project): PluginSettingsState = project.getService(PluginSettingsState::class.java)
    }
}