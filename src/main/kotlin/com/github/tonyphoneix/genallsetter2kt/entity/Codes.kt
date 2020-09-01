package com.github.tonyphoneix.genallsetter2kt.entity

import com.github.tonyphoneix.genallsetter2kt.utils.PsiClassUtils
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType


data class CodeAndImports(val code: String = "", val imports: Set<String> = emptySet())

data class SetGenerateDTO(val project: Project,
                          val selectedElement: PsiElement,
                          val methods: List<PsiMethod>,
                          val splitText: String,
                          val variable: String = "")

data class BuilderGenerateDTO(val project: Project,
                              val selectedElement: PsiElement,
                              val fields: List<PsiField>,
                              val splitText: String)

/**
 *  拆解method
 */
data class ExtMethod(val caller: String, val fieldName: String, val psiType: PsiType) {
    companion object {

        fun extGetMethod(caller: String, psiMethod: PsiMethod): ExtMethod? {
            //字段名称
            val fieldName = PsiClassUtils.getFieldNameFromGetMethod(psiMethod)
            //字段类型
            val fieldType = psiMethod.returnType!!
            return fieldName?.let { ExtMethod(caller, fieldName, fieldType) }
        }

        fun extSetMethod(caller: String = "", psiMethod: PsiMethod): ExtMethod? {
            //字段名称
            val fieldName = PsiClassUtils.getFieldNameFromSetMethod(psiMethod)
            //字段类型
            val fieldType = psiMethod.parameterList.parameters.first().type
            return fieldName?.let { ExtMethod(caller, fieldName, fieldType) }
        }
    }
}

enum class GenCodeType {
    NONE, DEFAULT, GETTER
}