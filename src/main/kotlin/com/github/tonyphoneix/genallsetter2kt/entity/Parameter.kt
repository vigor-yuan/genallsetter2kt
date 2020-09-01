package com.github.tonyphoneix.genallsetter2kt.entity

import com.github.tonyphoneix.genallsetter2kt.utils.PsiClassUtils
import com.intellij.psi.PsiTypeElement
import com.intellij.psi.util.PsiTypesUtil


/**
 * Disassembly of method parameters
 */
data class Parameter(var packagePath: String = "", var className: String = "", var genericParameters: List<GenericParameter> = emptyList())

/**
 * Package name disassembly
 */
data class GenericParameter(var realPackage: String = "", var realName: String = "")

/**
 * Parameter name, parameter class type
 */
data class ParameterValue(val caller: String, val typeElement: PsiTypeElement) {

    fun isHasValidGetMethod(): Boolean {
        val psiClass = PsiTypesUtil.getPsiClass(typeElement.type) ?: return false
        return PsiClassUtils.checkClassHasValidGetMethod(psiClass)
    }

    fun getAllGetMethods(): List<ExtMethod> {
        val psiClass = PsiTypesUtil.getPsiClass(typeElement.type)!!
        return PsiClassUtils.extractGetMethod(psiClass).mapNotNull { ExtMethod.extGetMethod(caller, it) }
    }
}