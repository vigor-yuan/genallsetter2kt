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
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
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
        val generateDTO = BuilderGenerateDTO(
            project,
            expression,
            psiClass.methods.filterNot { it.parameterList.isEmpty }.toList(),
            splitText
        )
        genCodeAndImports(generateDTO).also {
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
        generateDTO.methods.forEach { psiMethod ->
            code.append(generateDTO.splitText).append('.').append(psiMethod.name).append("(")
            genCodeAndImportsFromMethod(psiMethod, getters.flatMap { it.getAllGetMethods() }).also {
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
        val file = e.getData(PlatformDataKeys.PSI_FILE) as? PsiJavaFile
        if (project == null || editor == null || file == null) {
            return false
        }
        return PsiElementUtils.getElement(editor, file)?.let {
            PsiTreeUtil.getParentOfType(it, PsiMethodCallExpression::class.java)
        }?.resolveMethod()?.takeIf {
            PsiClassUtils.isValidBuilderMethod(it)
        }?.let { PsiTypesUtil.getPsiClass(it.returnType) }?.let { it.name?.endsWith("Builder") } ?: false
    }

    abstract fun genCodeAndImportsFromMethod(method: PsiMethod, getters: List<ExtMethod>): CodeAndImports
}

class GenerateAllBuilderNoDefaultValue : BaseGenerateAllBuilder(GenCodeType.NONE) {
    override fun genCodeAndImportsFromMethod(method: PsiMethod, getters: List<ExtMethod>): CodeAndImports {
        return CodeAndImports()
    }

}

class GenerateAllBuilderWithDefaultValue : BaseGenerateAllBuilder(GenCodeType.DEFAULT) {
    override fun genCodeAndImportsFromMethod(method: PsiMethod, getters: List<ExtMethod>): CodeAndImports {
        val imports = mutableSetOf<String>()
        val code = StringBuilder()
        val parameters = method.parameterList.parameters
        parameters.forEachIndexed { i, psiParameter ->
            //解析参数类名称
            val parameter = PsiToolUtils.extraParameterFromFullyQualifiedName(psiParameter.type.canonicalText)
            //内置了默认值，如果不匹配的话则生成默认值和import语句
            val valueAndImport = CodeUtils.getDefaultValueAndDefaultImport(parameter.packagePath, parameter.className)
            code.append(valueAndImport.first).append(if (i != parameters.size - 1) "," else "")
            imports.add(valueAndImport.second)
        }
        return CodeAndImports(code.toString(), imports)
    }
}

class GenerateAllBuilderWithGetter : BaseGenerateAllBuilder(GenCodeType.GETTER) {
    override fun genCodeAndImportsFromMethod(method: PsiMethod, getters: List<ExtMethod>): CodeAndImports {
        return getters.firstOrNull {
            it.fieldName.equals(method.name, true) && it.psiType == method.parameterList.parameters.first()!!.type
        }?.let {
            CodeAndImports("${it.caller}.get${it.fieldName}()")
        } ?: CodeAndImports()
    }

}