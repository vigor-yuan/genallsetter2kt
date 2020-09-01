package com.github.tonyphoneix.genallsetter2kt.actions

import com.github.tonyphoneix.genallsetter2kt.entity.BuilderGenerateDTO
import com.github.tonyphoneix.genallsetter2kt.entity.CodeAndImports
import com.github.tonyphoneix.genallsetter2kt.entity.ExtMethod
import com.github.tonyphoneix.genallsetter2kt.entity.GenCodeType
import com.github.tonyphoneix.genallsetter2kt.ui.GenerateSetterFromParametersDialog
import com.github.tonyphoneix.genallsetter2kt.utils.CodeUtils
import com.github.tonyphoneix.genallsetter2kt.utils.PsiClassUtils
import com.github.tonyphoneix.genallsetter2kt.utils.PsiDocumentUtils
import com.github.tonyphoneix.genallsetter2kt.utils.PsiElementUtils
import com.github.tonyphoneix.genallsetter2kt.write
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.PsiField
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import utils.PsiToolUtils

abstract class BaseGenerateAllBuilder(codeType: GenCodeType) : BaseGenerate(codeType) {

    override fun actionPerformed(e: AnActionEvent) {
        if (!available(e)) return
        val project = e.project!!
        val editor = e.getData(PlatformDataKeys.EDITOR)!!
        val file = e.getData(PlatformDataKeys.PSI_FILE)!! as PsiJavaFile
        val expression = PsiElementUtils.getElement(editor, file)!!.let {
            PsiTreeUtil.getParentOfType(it, PsiMethodCallExpression::class.java)
        }!!
        val psiMethod = expression.resolveMethod()!!
        val psiClass = PsiTypesUtil.getPsiClass(psiMethod.returnType)!!
        val document = editor.document
        //split text
        val splitText = PsiDocumentUtils.calculateSplitText(document, expression.textOffset) + "\t\t"
        //Generate code
        genCodeAndImports(BuilderGenerateDTO(project, expression, psiClass.fields.toList(), splitText)).also {
            document.write(project) { insertString(expression.textOffset + expression.textLength, it.code) }
            PsiToolUtils.addImportToFile(project, file, document, it.imports)
        }
    }

    /**
     * generate code
     */
    private fun genCodeAndImports(generateDTO: BuilderGenerateDTO): CodeAndImports {
        val code = StringBuilder()
        val imports = mutableSetOf<String>()
        val getters = if (GenCodeType.GETTER == this.codeType) {
            val parameters = searchParameters(generateDTO.selectedElement)
            GenerateSetterFromParametersDialog(generateDTO.project, parameters).run {
                if (parameters.isEmpty() || !showAndGet()) {
                    return CodeAndImports()
                }
                choices
            }
        } else emptyList()
        generateDTO.fields.forEach { psiField ->
            code.append(generateDTO.splitText).append('.').append(psiField.name).append("(")
            genCodeAndImportsFromField(psiField, getters.flatMap { it.getAllGetMethods() }).also {
                //添加代码
                if (it.code.isNotBlank()) code.append(it.code)
                //添加imports
                imports.addAll(it.imports.filter { i -> CodeUtils.isNeedToDeclareClasses(i) })
            }
            code.append(")")
        }
        code.append(generateDTO.splitText).append(".build()")
        return CodeAndImports(code.toString(), imports)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = available(e)
    }

    private fun available(e: AnActionEvent): Boolean {
        val project = e.project
        val editor = e.getData(PlatformDataKeys.EDITOR)
        val file = e.getData(PlatformDataKeys.PSI_FILE) as PsiJavaFile?
        if (project == null || editor == null || file == null) {
            return false
        }
        return PsiElementUtils.getElement(editor, file)?.let {
            PsiTreeUtil.getParentOfType(it, PsiMethodCallExpression::class.java)
        }?.resolveMethod()?.takeIf {
            PsiClassUtils.isValidBuilderMethod(it)
        }?.let { PsiTypesUtil.getPsiClass(it.returnType) }?.let { it.name?.endsWith("Builder") } ?: false
    }

    abstract fun genCodeAndImportsFromField(field: PsiField, getters: List<ExtMethod>): CodeAndImports
}

class GenerateAllBuilderNoDefaultValue : BaseGenerateAllBuilder(GenCodeType.NONE) {
    override fun genCodeAndImportsFromField(field: PsiField, getters: List<ExtMethod>): CodeAndImports {
        return CodeAndImports()
    }

}

class GenerateAllBuilderWithDefaultValue : BaseGenerateAllBuilder(GenCodeType.DEFAULT) {
    override fun genCodeAndImportsFromField(field: PsiField, getters: List<ExtMethod>): CodeAndImports {
        val parameter = PsiToolUtils.extraParameterFromFullyQualifiedName(field.type.canonicalText)
        return CodeUtils.getDefaultValueAndDefaultImport(parameter.packagePath, parameter.className).let {
            CodeAndImports(it.first, setOf(it.second))
        }
    }
}

class GenerateAllBuilderWithGetter : BaseGenerateAllBuilder(GenCodeType.GETTER) {
    override fun genCodeAndImportsFromField(field: PsiField, getters: List<ExtMethod>): CodeAndImports {
        return getters.firstOrNull {
            it.fieldName.equals(field.name, true) && it.psiType == field.type
        }?.let {
            CodeAndImports("${it.caller}.get${it.fieldName}()")
        } ?: CodeAndImports()
    }

}