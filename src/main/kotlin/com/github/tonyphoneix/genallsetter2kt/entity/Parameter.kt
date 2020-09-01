package com.github.tonyphoneix.genallsetter2kt.entity

import com.github.tonyphoneix.genallsetter2kt.utils.PsiClassUtils
import com.intellij.psi.PsiType
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
data class ParameterValue(val caller: String, val type: PsiType) {

    fun isHasValidGetMethod(): Boolean {
        val psiClass = PsiTypesUtil.getPsiClass(type) ?: return false
        return PsiClassUtils.checkClassHasValidGetMethod(psiClass)
    }

    fun getAllGetMethods(): List<ExtMethod> {
        val psiClass = PsiTypesUtil.getPsiClass(type)!!
        return PsiClassUtils.extractGetMethod(psiClass).mapNotNull { ExtMethod.extGetMethod(caller, it) }
    }
}