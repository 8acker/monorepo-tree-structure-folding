package com.eightackerbird.intellij.plugins.monorepo.structurefolding

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class VirtualDirectoryNode(
    project: Project?,
    private val name: String,
    private val children: List<AbstractTreeNode<*>>,
    private val rank: Int,
    viewSettings: ViewSettings?
) : ProjectViewNode<String>(project, name, viewSettings) {

    override fun getChildren(): Collection<AbstractTreeNode<*>> = children
    override fun update(presentation: PresentationData) {
        presentation.presentableText = name
        presentation.setIcon(AllIcons.Nodes.CopyOfFolder)
    }
    override fun getName(): String = name
    override fun isAlwaysLeaf(): Boolean = false
    override fun canNavigate(): Boolean = true
    override fun canNavigateToSource(): Boolean = true
    override fun getVirtualFile(): VirtualFile? = children.filterIsInstance<PsiDirectoryNode>().firstNotNullOfOrNull { it.virtualFile }
    override fun contains(file: VirtualFile): Boolean = children.any { it is ProjectViewNode<*> && it.contains(file) }
    override fun canRepresent(element: Any?): Boolean = children.any { it is PsiDirectoryNode && it.canRepresent(element) }
    override fun getTypeSortWeight(sortByType: Boolean): Int = rank
    override fun equals(other: Any?): Boolean {
        return other is VirtualDirectoryNode &&
                other.name == name &&
                other.project == project
    }
    override fun hashCode(): Int = name.hashCode()
}