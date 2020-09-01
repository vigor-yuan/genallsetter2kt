package com.github.tonyphoneix.genallsetter2kt.ui

import com.github.tonyphoneix.genallsetter2kt.entity.ParameterValue
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
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
        return panel {
            noteRow("Check the parameters that need to be generated for \n the get method, multiple choices are available")
            this@GenerateSetterFromParametersDialog.parameters.forEach {
                row {
                    checkBox("${it.caller} : ${it.type.presentableText}") { _, component ->
                        if (component.isSelected) choices.add(it) else choices.remove(it)
                    }
                }
            }
        }
    }
}