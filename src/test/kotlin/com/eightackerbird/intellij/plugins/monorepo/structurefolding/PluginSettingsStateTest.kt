package com.eightackerbird.intellij.plugins.monorepo.structurefolding

import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.lang.reflect.Proxy
import kotlin.io.path.createDirectory
import kotlin.io.path.readText
import kotlin.io.path.writeText

class PluginSettingsStateTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `workspace files and updates are isolated between projects`() {
        val firstRoot = temporaryFolder.newFolder("first-monorepo").toPath()
        val secondRoot = temporaryFolder.newFolder("second-monorepo").toPath()
        val firstFile = firstRoot.resolve("project-structure.txt")
        val secondFile = secondRoot.resolve("project-structure.txt")
        firstFile.writeText("Frontend\n+---web")
        secondFile.writeText("Backend\n+---api")

        val first = PluginSettingsState(project(firstRoot.toString()))
        val second = PluginSettingsState(project(secondRoot.toString()))

        assertEquals(firstFile.toString(), first.projectStructureLocation)
        assertEquals(secondFile.toString(), second.projectStructureLocation)
        assertEquals(listOf(ProjectGroup("Frontend", listOf("web"))), first.allGroups())
        assertEquals(listOf(ProjectGroup("Backend", listOf("api"))), second.allGroups())

        first.updateStructure("Tools\n+---cli")

        assertEquals("Tools\n+---cli", firstFile.readText())
        assertEquals("Backend\n+---api", secondFile.readText())
        assertEquals(listOf(ProjectGroup("Backend", listOf("api"))), second.allGroups())
    }

    @Test
    fun `saved custom location loads without reading the default file`() {
        val root = temporaryFolder.newFolder("monorepo").toPath()
        root.resolve("project-structure.txt").createDirectory()
        val customFile = root.resolve("custom-structure.txt")
        customFile.writeText("Services\n+---worker")

        val original = PluginSettingsState(project(root.toString()))
        original.loadState(PluginSettingsState.Settings(customFile.toString()))
        val savedState = XmlSerializer.serialize(original.getState())
        val restored = PluginSettingsState(project(root.toString()))
        restored.loadState(XmlSerializer.deserialize(savedState, PluginSettingsState.Settings::class.java))

        assertEquals(customFile.toString(), restored.projectStructureLocation)
        assertEquals(listOf(ProjectGroup("Services", listOf("worker"))), restored.allGroups())
    }

    @Test
    fun `loading empty settings resets an initialized structure to the workspace default`() {
        val root = temporaryFolder.newFolder("monorepo").toPath()
        val defaultFile = root.resolve("project-structure.txt")
        val customFile = root.resolve("custom-structure.txt")
        defaultFile.writeText("Default\n+---app")
        customFile.writeText("Custom\n+---tools")
        val settings = PluginSettingsState(project(root.toString()))
        settings.allGroups()
        settings.updateLocation(customFile.toString())
        assertEquals(listOf(ProjectGroup("Custom", listOf("tools"))), settings.allGroups())

        settings.loadState(PluginSettingsState.Settings())

        assertEquals(defaultFile.toString(), settings.projectStructureLocation)
        assertEquals(listOf(ProjectGroup("Default", listOf("app"))), settings.allGroups())
    }

    @Test
    fun `missing workspace root does not fall back to the home directory`() {
        val settings = PluginSettingsState(project(null))

        assertThrows(IllegalStateException::class.java) {
            settings.projectStructureLocation
        }
    }

    private fun project(basePath: String?): Project =
        Proxy.newProxyInstance(Project::class.java.classLoader, arrayOf(Project::class.java)) { _, method, _ ->
            when (method.name) {
                "getBasePath" -> basePath
                else -> error("Unexpected project method: ${method.name}")
            }
        } as Project
}
