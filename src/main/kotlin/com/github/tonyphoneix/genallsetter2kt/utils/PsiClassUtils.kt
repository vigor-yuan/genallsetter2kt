package com.github.tonyphoneix.genallsetter2kt.utils

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier

object PsiClassUtils {

    private fun isNotSystemClass(psiClass: PsiClass): Boolean {
        return psiClass.qualifiedName?.let { !it.startsWith("java.") } ?: false
    }

    private fun isValidSetMethod(m: PsiMethod): Boolean {
        return m.hasModifierProperty(PsiModifier.PUBLIC)
                && !m.hasModifierProperty(PsiModifier.STATIC)
                && m.name.startsWith("set")
    }

    fun getFieldNameFromSetMethod(m: PsiMethod): String? {
        return if (isValidSetMethod(m)) m.name.substringAfter("set") else null
    }

    fun getFieldNameFromGetMethod(m: PsiMethod): String? {
        return if (isValidGetMethod(m)) {
            m.name.substringAfter("get", "").ifBlank { m.name.substringAfter("is") }
        } else null
    }


    fun isValidGetMethod(m: PsiMethod): Boolean {
        return m.hasModifierProperty(PsiModifier.PUBLIC) && !m.hasModifierProperty(PsiModifier.STATIC) &&
                (m.name.startsWith("get") || m.name.startsWith("is"))
    }

    fun isValidBuilderMethod(m: PsiMethod): Boolean {
        return m.hasModifierProperty(PsiModifier.PUBLIC) && m.hasModifierProperty(PsiModifier.STATIC) &&
                "builder" == m.name
    }

    private fun addSetMethodToList(psiClass: PsiClass): List<PsiMethod> {
        return psiClass.methods.filter { isValidSetMethod(it) }
    }

    private fun addGetMethodToList(psiClass: PsiClass): List<PsiMethod> {
        return psiClass.methods.filter { m: PsiMethod -> isValidGetMethod(m) }
    }

    fun extractSetMethods(psiClass: PsiClass): List<PsiMethod> {
        val methodList = mutableListOf<PsiMethod>()
        if (isNotSystemClass(psiClass)) {
            methodList.addAll(addSetMethodToList(psiClass))
            psiClass.superClass?.let {
                methodList.addAll(extractSetMethods(it))
            }
        }
        return methodList
    }

    fun extractGetMethod(psiClass: PsiClass): List<PsiMethod> {
        val methodList = mutableListOf<PsiMethod>()
        if (isNotSystemClass(psiClass)) {
            methodList.addAll(addGetMethodToList(psiClass))
            psiClass.superClass?.let {
                methodList.addAll(extractGetMethod(it))
            }
        }
        return methodList
    }

    /**
     * Check if the set method exists in Psi Class
     */
    fun checkClassHasValidSetMethod(psiClass: PsiClass): Boolean {
        return if (isNotSystemClass(psiClass) && psiClass.methods.any { isValidSetMethod(it) }) true
        else psiClass.superClass?.let { checkClassHasValidSetMethod(it) } ?: false

    }

    /**
     * Check if there is a get method in Psi Class
     */
    fun checkClassHasValidGetMethod(psiClass: PsiClass): Boolean {
        return if (isNotSystemClass(psiClass) && psiClass.methods.any { isValidGetMethod(it) }) true
        else psiClass.superClass?.let { checkClassHasValidGetMethod(it) } ?: false
    }
}