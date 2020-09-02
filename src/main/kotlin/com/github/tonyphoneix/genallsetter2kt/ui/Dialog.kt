package com.github.tonyphoneix.genallsetter2kt.ui

import com.github.tonyphoneix.genallsetter2kt.entity.ParameterValue
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import wu.seal.jsontokotlin.ui.checkBox
import wu.seal.jsontokotlin.ui.label
import wu.seal.jsontokotlin.ui.verticalLinearLayout
import javax.swing.JComponent


class GenerateSetterFromParametersDialog(project: Project,
                                         private val parameters: List<ParameterValue>) : DialogWrapper(project) {

    /**
     * Selected result
     */
    val choices = mutableListOf<ParameterValue>()

    init {
        super.init()
        setOKButtonText("Generate")
        title = "Generate all set methods from parameters";
    }

    override fun createCenterPanel(): JComponent? {
        return verticalLinearLayout {
            fixedSpace(5)
            label("<html>Check the parameters that need to be generated for <br>" +
                    " the get method, multiple choices are available</html>")()
            val ps = this@GenerateSetterFromParametersDialog.parameters
            ps.forEach {
                checkBox("${it.caller} : ${it.type.presentableText}", ps.size == 1) { isSelected ->
                    if (isSelected) choices.add(it) else choices.remove(it)
                }()
            }
            if (ps.size == 1) choices.add(ps.first())
        }
    }
}