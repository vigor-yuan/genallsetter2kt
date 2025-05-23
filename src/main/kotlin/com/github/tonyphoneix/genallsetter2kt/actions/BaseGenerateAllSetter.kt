package com.github.tonyphoneix.genallsetter2kt.actions

import com.github.tonyphoneix.genallsetter2kt.entity.CodeAndImports
import com.github.tonyphoneix.genallsetter2kt.entity.ExtMethod
import com.github.tonyphoneix.genallsetter2kt.entity.GenCodeType
import com.github.tonyphoneix.genallsetter2kt.entity.SetGenerateDTO
import com.github.tonyphoneix.genallsetter2kt.ui.GenerateSetterFromParametersDialog
import com.github.tonyphoneix.genallsetter2kt.utils.CodeUtils
import com.github.tonyphoneix.genallsetter2kt.utils.PsiClassUtils
import com.github.tonyphoneix.genallsetter2kt.utils.PsiDocumentUtils
import com.github.tonyphoneix.genallsetter2kt.utils.PsiElementUtils
import com.github.tonyphoneix.genallsetter2kt.write
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.psi.util.elementType
import utils.PsiToolUtils

/**
 * set method generator
 */
abstract class BaseGenerateAllSetter(codeType: GenCodeType) : BaseGenerate(codeType) {

    override fun update(e: AnActionEvent) {
        if (ApplicationManager.getApplication().isDispatchThread) {
            ApplicationManager.getApplication().executeOnPooledThread {
                ApplicationManager.getApplication().runReadAction {
                    e.presentation.isVisible = available(e)
                }
            }
        } else {
            e.presentation.isVisible = available(e)
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (!available(e)) return
        val project = e.project!!
        val editor = e.getData(PlatformDataKeys.EDITOR)!!
        val file = e.getData(PlatformDataKeys.PSI_FILE)!! as PsiJavaFile
        val document = editor.document

        val element = PsiElementUtils.getElement(editor, file)
        val localVar = PsiTreeUtil.getParentOfType(element, PsiLocalVariable::class.java)
        val parameter = PsiTreeUtil.getParentOfType(element, PsiParameter::class.java)

        data class GenerateContext(
            val variable: PsiElement,
            val type: PsiType,
            val variableName: String,
            val insertOffset: Int,
            val splitText: String
        )

        val context = when {
            localVar != null -> {
                val offset = localVar.parent.textOffset + localVar.parent.textLength
                val split = PsiDocumentUtils.calculateSplitText(document, localVar.parent.textOffset)
                GenerateContext(localVar, localVar.type, localVar.name, offset, split)
            }

            parameter != null -> {
                // Find the method containing this parameter
                val method = PsiTreeUtil.getParentOfType(parameter, PsiMethod::class.java)
                if (method?.body == null) return

                // Get the method body and find the last statement
                val methodBody = method.body!!
                val statements = methodBody.statements

                // Calculate the insert offset:
                // If there are statements, insert after the last one
                // Otherwise, insert after the opening brace
                if (statements.isNotEmpty()) {
                    val lastStatement = statements.last()
                    val offset = lastStatement.textOffset + lastStatement.textLength
                    // Calculate split text based on the method body's indentation
                    val methodIndent = PsiDocumentUtils.calculateSplitText(document, lastStatement.textOffset)
                    GenerateContext(parameter, parameter.type, parameter.name, offset, methodIndent)
                } else {
                    val offset = methodBody.lBrace?.textOffset?.plus(1) ?: 0
                    // Calculate split text based on the method body's indentation
                    val methodIndent = PsiDocumentUtils.calculateSplitText(document, methodBody.textOffset)
                    // Add 4 spaces to the method indentation
                    val statementIndent = methodIndent + "    "
                    GenerateContext(parameter, parameter.type, parameter.name, offset, statementIndent)
                }
            }

            else -> return
        }

        val allSetMethods = PsiClassUtils.extractSetMethods(PsiTypesUtil.getPsiClass(context.type)!!)

        genCodeAndImports(
            SetGenerateDTO(
                project, context.variable, allSetMethods, context.splitText, context.variableName
            )
        ).also {
            document.write(project) { insertString(context.insertOffset, it.code) }
            PsiToolUtils.addImportToFile(project, file, document, it.imports)
        }
    }

    /**
     * Determine whether the menu can be displayed
     * 1. Get the element at the cursor
     * 2. Determine whether it is a local variable && whether the parent class is a declaration statement
     * 3. Get the class file according to the type and check whether the class contains the set method
     */
    private fun available(e: AnActionEvent): Boolean {
        val editor = e.getData(PlatformDataKeys.EDITOR)
        val file = e.getData(PlatformDataKeys.PSI_FILE) as? PsiJavaFile
        if (editor == null || file == null) {
            return false
        }
        val element = PsiElementUtils.getElement(editor, file)
        // Check for local variable
        val localVar = PsiTreeUtil.getParentOfType(element, PsiLocalVariable::class.java)
        if (localVar != null && localVar.parent is PsiDeclarationStatement) {
            return PsiTypesUtil.getPsiClass(localVar.type)?.let { PsiClassUtils.checkClassHasValidSetMethod(it) }
                ?: false
        }
        // Check for method parameter
        val parameter = PsiTreeUtil.getParentOfType(element, PsiParameter::class.java)
        if (parameter != null) {
            return PsiTypesUtil.getPsiClass(parameter.type)?.let { PsiClassUtils.checkClassHasValidSetMethod(it) }
                ?: false
        }
        return false
    }

    /**
     * 生成赋值代码和import语句
     *
     * @param setGenerateDTO
     * @return
     */
    private fun genCodeAndImports(setGenerateDTO: SetGenerateDTO): CodeAndImports {
        val code = StringBuilder()
        //收集import语句
        val imports = mutableSetOf<String>()
        val getters = if (GenCodeType.GENERATE_ALL_GET_METHOD == this.codeType) {
            val parameters = searchParameters(setGenerateDTO.selectedElement)
            GenerateSetterFromParametersDialog(setGenerateDTO.project, parameters).run {
                if (parameters.isEmpty() || !showAndGet()) {
                    return CodeAndImports()
                }
                choices
            }
        } else emptyList()
        setGenerateDTO.methods.forEach { m ->
            //生成set赋值代码
            code.append(setGenerateDTO.splitText).append(setGenerateDTO.variable).append('.').append(m.name).append("(")
            //解析方法的参数列表
            genCodeAndImportsFromMethod(m, getters.flatMap { it.getAllGetMethods() }).also {
                //添加代码
                if (it.code.isNotBlank()) code.append(it.code)
                //添加imports
                imports.addAll(it.imports.filter { i -> CodeUtils.isNeedToDeclareClasses(i) })
            }
            code.append(");")
        }
        return CodeAndImports(code.toString(), imports)
    }

    abstract fun genCodeAndImportsFromMethod(method: PsiMethod, getters: List<ExtMethod>): CodeAndImports
}

class GenerateAllSetterNoDefaultValue : BaseGenerateAllSetter(GenCodeType.NONE) {

    override fun genCodeAndImportsFromMethod(
        method: PsiMethod, getters: List<ExtMethod>
    ): CodeAndImports {
        return CodeAndImports()
    }
}

class GenerateAllSetterWithDefaultValue : BaseGenerateAllSetter(GenCodeType.DEFAULT) {

    override fun genCodeAndImportsFromMethod(
        method: PsiMethod, getters: List<ExtMethod>
    ): CodeAndImports {
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

class GenerateAllSetterWithGetter : BaseGenerateAllSetter(GenCodeType.GENERATE_ALL_GET_METHOD) {

    override fun genCodeAndImportsFromMethod(
        method: PsiMethod, getters: List<ExtMethod>
    ): CodeAndImports {
        val extSetMethod = ExtMethod.extSetMethod(psiMethod = method) ?: return CodeAndImports()
        return getters.firstOrNull {
            it.fieldName == extSetMethod.fieldName && it.psiType == extSetMethod.psiType
        }?.let {
            CodeAndImports("${it.caller}.get${it.fieldName}()")
        } ?: CodeAndImports()
    }
}