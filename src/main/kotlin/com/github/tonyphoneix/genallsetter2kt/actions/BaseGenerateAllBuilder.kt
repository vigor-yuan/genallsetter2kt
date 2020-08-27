package com.github.tonyphoneix.genallsetter2kt.actions

import com.github.tonyphoneix.genallsetter2kt.utils.CodeUtils
import com.github.tonyphoneix.genallsetter2kt.utils.PsiClassUtils
import com.github.tonyphoneix.genallsetter2kt.utils.PsiDocumentUtils
import com.github.tonyphoneix.genallsetter2kt.utils.PsiElementUtils
import com.github.tonyphoneix.genallsetter2kt.write
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.PsiField
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import utils.PsiToolUtils

abstract class BaseGenerateAllBuilder : AnAction() {

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
        generateCode(psiClass.fields, splitText).run {
            document.write(project) { insertString(expression.textOffset + expression.textLength, first) }
            PsiToolUtils.addImportToFile(project, file, document, second)
        }
    }

    /**
     * @param fields
     * @param splitText
     */
    private fun generateCode(fields: Array<PsiField>, splitText: String): Pair<String, Set<String>> {
        val code = StringBuilder()
        val importPackages = mutableSetOf<String>()
        fields.forEach { psiField ->
            code.append(splitText).append('.').append(psiField.name).append("(")
            if (hasDefaultValue()) {
                val parameter = PsiToolUtils.extraParmaterFromFullyQualifiedName(psiField.type.canonicalText)
                CodeUtils.getDefaultValueAndDefaultImport(parameter.packagePath, parameter.className)
                        .also { code.append(it.first) }
                        .takeIf { CodeUtils.isNeedToDeclareClasses(it.second) }
                        ?.also { importPackages.add(it.second) }
            }
            code.append(")")
        }
        code.append(splitText).append(".build()")
        return Pair(code.toString(), importPackages)
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

    protected abstract fun hasDefaultValue(): Boolean
}

class GenerateAllBuilderWithDefaultValue : BaseGenerateAllBuilder() {
    override fun hasDefaultValue(): Boolean {
        return true
    }
}

class GenerateAllBuilderNoDefaultValue : BaseGenerateAllBuilder() {
    override fun hasDefaultValue(): Boolean {
        return false
    }
}