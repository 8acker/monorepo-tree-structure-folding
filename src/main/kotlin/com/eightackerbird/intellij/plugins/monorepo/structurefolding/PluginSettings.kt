package com.eightackerbird.intellij.plugins.monorepo.structurefolding

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Nls
import java.io.IOException
import java.nio.file.InvalidPathException
import javax.swing.JComponent

class PluginSettings(private val project: Project) : Configurable {
    private lateinit var component: PluginSettingsComponent
    private val state: PluginSettingsState = PluginSettingsState.getInstance(project)

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "Monorepo Tree Structure Folding"
    }

    override fun createComponent(): JComponent {
        component = PluginSettingsComponent()
        return component.panel
    }

    override fun isModified(): Boolean = differs()

    override fun apply() {
        try {
            when {
                locationDiffers() -> state.updateLocation(component.projectStructureLocation)
                structureDiffers() -> state.updateStructure(component.projectStructure)
                else -> return
            }
        } catch (exception: InvalidPathException) {
            throw ConfigurationException("Invalid project structure file location: ${exception.message}")
        } catch (exception: IOException) {
            throw ConfigurationException("Could not load or save the project structure: ${exception.message}")
        }
        applyChanges()
        refreshView()
    }

    private fun refreshView() {
        ProjectView.getInstance(project).refresh()
    }

    private fun differs(): Boolean = locationDiffers() || structureDiffers()

    private fun locationDiffers(): Boolean = component.projectStructureLocation != state.projectStructureLocation

    private fun structureDiffers(): Boolean = component.projectStructure != state.projectStructure

    private fun applyChanges() {
        component.projectStructureLocation = state.projectStructureLocation
        component.projectStructure = state.projectStructure
    }

    override fun reset() = applyChanges()

    override fun disposeUIResources() {
    }
}