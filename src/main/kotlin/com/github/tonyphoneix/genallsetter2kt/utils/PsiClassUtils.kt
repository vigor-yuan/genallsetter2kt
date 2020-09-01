/*
 *  Copyright (c) 2017-2019, bruce.ge.
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License
 *    as published by the Free Software Foundation; version 2 of
 *    the License.
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *    You should have received a copy of the GNU General Public License
 *    along with this program;
 */
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
     * 检查PsiClass中是否存在set方法
     */
    fun checkClassHasValidSetMethod(psiClass: PsiClass): Boolean {
        return if (isNotSystemClass(psiClass) && psiClass.methods.any { isValidSetMethod(it) }) true
        else psiClass.superClass?.let { checkClassHasValidSetMethod(it) } ?: false

    }

    /**
     * 检查PsiClass中是否存在get方法
     */
    fun checkClassHasValidGetMethod(psiClass: PsiClass): Boolean {
        return if (isNotSystemClass(psiClass) && psiClass.methods.any { isValidGetMethod(it) }) true
        else psiClass.superClass?.let { checkClassHasValidGetMethod(it) } ?: false
    }
}