package com.eightackerbird.intellij.plugins.monorepo.structurefolding

import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.psi.PsiDirectory

class CustomTreeStructureProvider : TreeStructureProvider {

    override fun modify(
        node: AbstractTreeNode<*>,
        children: MutableCollection<AbstractTreeNode<*>>,
        viewSettings: ViewSettings?
    ): MutableCollection<AbstractTreeNode<*>> {
        if (!isWorkspaceRoot(node)) {
            return children
        }
        val project = node.project ?: return children

        val unmatchedOther = mutableListOf<AbstractTreeNode<*>>()
        val groupNodes = PluginSettingsState.getInstance(project).allGroups()
            .map { CustomGroupNode(it) }

        for (child in children) {
            val name = childName(child)
            val groupNode = if (name != null) groupNodes.find { it.group.includes(name) } else null
            if (groupNode != null) {
                groupNode.addChild(child)
            } else {
                unmatchedOther.add(child)
            }
        }

        var rank = 1
        val grouped = groupNodes
            .filter { it.isNotEmpty() }
            .map {
                VirtualDirectoryNode(node.project, it.group.name, it.getChildren(), rank++, viewSettings)
            }
        val nodeForOtherElements = VirtualDirectoryNode(node.project, "~", unmatchedOther, rank, viewSettings)

        return (grouped + nodeForOtherElements).toMutableList()
    }

    private fun isWorkspaceRoot(node: AbstractTreeNode<*>): Boolean {
        val directory = (node as? PsiDirectoryNode)?.value ?: return false
        val basePath = node.project?.basePath ?: return false

        return directory.virtualFile.path == basePath
    }

    private fun childName(node: AbstractTreeNode<*>): String? {
        if (node is PsiDirectoryNode && node.value is PsiDirectory) {
            return node.value.name
        }
        if (node is PsiFileNode) {
            return node.virtualFile?.name
        }
        if (node is ProjectViewNode<*>) {
            return node.virtualFile?.name ?: node.name
        }
        return null
    }
}

private class CustomGroupNode(val group: ProjectGroup) {

    private val foundChildren: MutableList<AbstractTreeNode<*>> = mutableListOf()

    fun addChild(child: AbstractTreeNode<*>) {
        foundChildren.add(child)
    }

    fun getChildren(): List<AbstractTreeNode<*>> {
        return foundChildren
    }

    fun isNotEmpty(): Boolean {
        return foundChildren.isNotEmpty()
    }
}