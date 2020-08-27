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

/**
 * @Author bruce.ge
 * @Date 2017/1/30
 * @Description
 */
object PsiClassUtils {
    private fun isNotSystemClass(psiClass: PsiClass): Boolean {
        return psiClass.qualifiedName?.let { !it.startsWith("java.") } ?: false
    }

    private fun isValidSetMethod(m: PsiMethod): Boolean {
        return m.hasModifierProperty(PsiModifier.PUBLIC)
                && !m.hasModifierProperty(PsiModifier.STATIC)
                && m.name.startsWith("set")
    }

    //    fun isValidGetMethod(m: PsiMethod): Boolean {
//        return m.hasModifierProperty(PsiModifier.PUBLIC) && !m.hasModifierProperty(PsiModifier.STATIC) &&
//                (m.getName().startsWith("get") || m.getName().startsWith("is"))
//    }
//
    fun isValidBuilderMethod(m: PsiMethod): Boolean {
        return m.hasModifierProperty(PsiModifier.PUBLIC) && m.hasModifierProperty(PsiModifier.STATIC) && "builder" == m.name
    }

    private fun addSetMethodToList(psiClass: PsiClass): List<PsiMethod> {
        return psiClass.methods.filter { isValidSetMethod(it) }
    }

//    fun addGettMethodToList(psiClass: PsiClass): List<PsiMethod> {
//        return Arrays.stream(psiClass.getMethods())
//                .filter({ obj: PsiClassUtils?, m: PsiMethod -> isValidGetMethod(m) })
//                .collect(Collectors.toList())
//    }

    fun extractSetMethods(psiClass: PsiClass): List<PsiMethod> {
        val methodList = mutableListOf<PsiMethod>()
        if (isNotSystemClass(psiClass)) {
            methodList.addAll(addSetMethodToList(psiClass))
            psiClass.superClass?.let { extractSetMethods(it) }
        }
        return methodList
    }

//    fun extractGetMethod(psiClass: PsiClass): List<PsiMethod> {
//        var psiClass: PsiClass = psiClass
//        val methodList: List<PsiMethod> = ArrayList<PsiMethod>()
//        while (isNotSystemClass(psiClass)) {
//            addGettMethodToList(psiClass)
//            psiClass = psiClass.getSuperClass()
//        }
//        return methodList
//    }

    /**
     * 检查文件中是否存在set方法
     */
    fun checkClassHasValidSetMethod(psiClass: PsiClass): Boolean {
        return if (isNotSystemClass(psiClass) && psiClass.methods.any { isValidSetMethod(it) }) true
        else {
            psiClass.superClass?.let { checkClassHasValidSetMethod(it) } ?: false
        }
    }

//    fun checkClasHasValidGetMethod(psiClass: PsiClass?): Boolean {
//        var psiClass: PsiClass = psiClass ?: return false
//        while (isNotSystemClass(psiClass)) {
//            for (m in psiClass.getMethods()) {
//                if (isValidGetMethod(m)) {
//                    return true
//                }
//            }
//            psiClass = psiClass.getSuperClass()
//        }
//        return false
//    }
}