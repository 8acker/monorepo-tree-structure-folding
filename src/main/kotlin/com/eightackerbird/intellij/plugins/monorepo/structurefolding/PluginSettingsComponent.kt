package com.eightackerbird.intellij.plugins.monorepo.structurefolding

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField


class PluginSettingsComponent(
    val panel: JPanel = JPanel(BorderLayout()),
    private val location: JTextField = JBTextField("", 58),
    private val structure: JTextArea = JBTextArea(100, 30)
) {
    init {
        structure.setLineWrap(true)
        structure.setWrapStyleWord(false)

        val panelComponent = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                "File location",
                location
            )
            .addLabeledComponent(
                "Project structure",
                JBScrollPane(structure)
            ).panel
        this.panel.add(panelComponent, BorderLayout.NORTH)
    }

    var projectStructureLocation: String
        get() = location.getText()
        set(text) {
            this@PluginSettingsComponent.location.setText(text)
        }

    var projectStructure: String
        get() = structure.getText()
        set(text) {
            structure.setText(text)
        }
}