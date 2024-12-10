package com.github.tonyphoneix.genallsetter2kt.actions

import com.github.tonyphoneix.genallsetter2kt.entity.CodeAndImports
import com.github.tonyphoneix.genallsetter2kt.entity.SetGenerateDTO
import com.github.tonyphoneix.genallsetter2kt.utils.PsiClassUtils
import com.github.tonyphoneix.genallsetter2kt.utils.PsiDocumentUtils
import com.github.tonyphoneix.genallsetter2kt.utils.PsiElementUtils
import com.github.tonyphoneix.genallsetter2kt.write
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import utils.PsiToolUtils

/**
 * set method generator
 */
class InvokeAllGetter : AnAction() {

    data class GenerateContext(
        val variable: PsiElement,
        val type: PsiType,
        val variableName: String,
        val insertOffset: Int,
        val splitText: String
    )

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

        val allGetMethods = PsiClassUtils.extractGetMethod(PsiTypesUtil.getPsiClass(context.type)!!)
        genCodeAndImports(SetGenerateDTO(project, context.variable, allGetMethods, context.splitText, context.variableName)).also {
            document.write(project) { insertString(context.insertOffset, it.code) }
            PsiToolUtils.addImportToFile(project, file, document, it.imports)
        }
    }

    /**
     * Determine whether the menu can be displayed
     * 1. Get the element at the cursor
     * 2. Determine whether it is a local variable && whether the parent class is a declaration statement
     * 3. Get the class file according to the type and check whether the class contains the GET method
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
            return PsiTypesUtil.getPsiClass(localVar.type)?.let { PsiClassUtils.checkClassHasValidGetMethod(it) } ?: false
        }
        
        // Check for method parameter
        val parameter = PsiTreeUtil.getParentOfType(element, PsiParameter::class.java)
        if (parameter != null) {
            return PsiTypesUtil.getPsiClass(parameter.type)?.let { PsiClassUtils.checkClassHasValidGetMethod(it) } ?: false
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
        setGenerateDTO.methods.forEach { m ->
            //生成all get methods
            code.append(setGenerateDTO.splitText).append(setGenerateDTO.variable).append('.').append(m.name)
                .append("();")
        }
        return CodeAndImports(code.toString(), emptySet())
    }
}