package com.github.tonyphoneix.genallsetter2kt.ui

import com.github.tonyphoneix.genallsetter2kt.entity.ParameterValue
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import wu.seal.jsontokotlin.ui.*
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
        if (parameters.size == 1) choices.add(parameters.first())
    }

    override fun createCenterPanel(): JComponent? {
        return verticalLinearLayout {
            label("<html>Check the parameters that need to be generated for <br>" +
                    " the get method, multiple choices are available</html>").putAlignLeft()
            val ps = this@GenerateSetterFromParametersDialog.parameters
            ps.forEach {
                checkBox("${it.caller} : ${it.type.presentableText}", ps.size == 1) { isSelected ->
                    if (isSelected) choices.add(it) else choices.remove(it)
                }.putAlignLeft()
            }
            line()()
            horizontalLinearLayout {
                label("Like this plugin? Please star ")()
                link("here", "https://github.com/TonyPhoneix/genallsetter2kt")()
            }.putAlignLeft()
        }

    }
}