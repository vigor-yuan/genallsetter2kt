package com.github.tonyphoneix.genallsetter2kt.actions

import com.github.tonyphoneix.genallsetter2kt.entity.GenCodeType
import com.github.tonyphoneix.genallsetter2kt.entity.ParameterValue
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

abstract class BaseGenerate(val codeType: GenCodeType) : AnAction() {

    /**
     * Search the parameters from the outer method element down until it is equal to the specified element
     */
    protected fun searchParameters(element: PsiElement): List<ParameterValue> {
        val psiMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)
            ?: return emptyList()
        val parameters = mutableListOf<ParameterValue>()
        //1. Search for local variables 2. Search for parameters
        psiMethod.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitElement(e: PsiElement) {
                if (e == element) {
                    stopWalking()
                    return
                }
                super.visitElement(e)
            }

            override fun visitLocalVariable(variable: PsiLocalVariable) {
                if (variable == element) {
                    stopWalking()
                    return
                }
                variable.also { parameters.add(ParameterValue(it.name, it.type)) }
                super.visitLocalVariable(variable)
            }

            override fun visitParameterList(list: PsiParameterList) {
                if (list == element) {
                    stopWalking()
                    return
                }
                list.parameters.forEach { parameters.add(ParameterValue(it.name, it.type)) }
                super.visitParameterList(list)
            }
        })
        //Check if there is a get method, and reverse the order of addition
        return parameters.filter { it.isHasValidGetMethod() }.reversed()
    }
}