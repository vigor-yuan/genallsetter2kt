package com.github.tonyphoneix.genallsetter2kt.actions

import com.github.tonyphoneix.genallsetter2kt.utils.CodeUtils
import com.github.tonyphoneix.genallsetter2kt.utils.PsiClassUtils
import com.github.tonyphoneix.genallsetter2kt.utils.PsiDocumentUtils
import com.github.tonyphoneix.genallsetter2kt.utils.PsiElementUtils
import com.github.tonyphoneix.genallsetter2kt.write
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.PsiDeclarationStatement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import utils.PsiToolUtils

/**
 * set方法生成器
 */
abstract class BaseGenerateAllSetter : AnAction() {

    override fun update(e: AnActionEvent) {
        //菜单栏可见性的判断
        e.presentation.isVisible = available(e)
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (!available(e)) return
        //因为available已经前置判空了，所以按照非空处理
        val project = e.project!!
        val editor = e.getData(PlatformDataKeys.EDITOR)!!
        val file = e.getData(PlatformDataKeys.PSI_FILE)!! as PsiJavaFile
        val document = editor.document
        //找到声明的变量
        val variable = PsiElementUtils.getElement(editor, file)
                .let { PsiTreeUtil.getParentOfType(it, PsiLocalVariable::class.java) }!!
        //获取变量的类型PsiClass, 再获取PsiClass所有的set方法
        val allSetMethods = PsiClassUtils.extractSetMethods(PsiTypesUtil.getPsiClass(variable.type)!!)
        //分隔文本，用于每一行的起始位置
        val splitText = PsiDocumentUtils.calculateSplitText(document, variable.parent.textOffset)
        //生成调用代码和import语句代码
        generateCodeAndImports(allSetMethods, variable.name, splitText).run {
            //安全的插入调用代码
            document.write(project) { insertString(variable.parent.textOffset + variable.parent.textLength, first) }
            //单独再插入import语句
            PsiToolUtils.addImportToFile(project, file, document, second)
        }
    }

    /**
     * 判定是否可以展示菜单
     * 1、拿到光标处元素
     * 2、判断是否是本地变量 && 父类是否为 声明语句
     * 3、根据类型拿到class文件，检查class是否含有set方法
     */
    private fun available(e: AnActionEvent): Boolean {
        val editor = e.getData(PlatformDataKeys.EDITOR)
        val file = e.getData(PlatformDataKeys.PSI_FILE) as? PsiJavaFile
        if (editor == null || file == null) {
            return false
        }
        return PsiElementUtils.getElement(editor, file)?.let {
            PsiTreeUtil.getParentOfType(it, PsiLocalVariable::class.java)
        }?.takeIf { it.parent is PsiDeclarationStatement }?.let {
            PsiTypesUtil.getPsiClass(it.type)
        }?.let { PsiClassUtils.checkClassHasValidSetMethod(it) } ?: false
    }

    /**
     * 生成赋值代码和import语句
     *
     * @param allSetMethods 所有set方法
     * @param variableText  变量名称
     * @param splitText     分割文本
     * @return
     */
    private fun generateCodeAndImports(allSetMethods: List<PsiMethod>,
                                       variableText: String,
                                       splitText: String): Pair<String, Set<String>> {
        val code = StringBuilder()
        //收集import语句
        val imports = mutableSetOf<String>()
        allSetMethods.forEach { m ->
            //生成set赋值代码
            code.append(splitText).append(variableText).append('.').append(m.name).append("(")
            //如果勾选生成默认值
            if (hasDefaultValue()) {
                //解析方法的参数列表
                val parameters = m.parameterList.parameters
                parameters.mapIndexed { i, p ->
                    generateParamaterCode(i, parameters.size, p.type.canonicalText).also {
                        //添加代码
                        code.append(it.first)
                    }.takeIf { CodeUtils.isNeedToDeclareClasses(it.second) }?.also {
                        //添加imports
                        imports.add(it.second)
                    }
                }
            }
            code.append(");")
        }
        return Pair(code.toString(), imports)
    }

    /**
     * 生成code 和 import
     */
    private fun generateParamaterCode(index: Int, size: Int, paramaterClassName: String): Pair<String, String> {
        //解析参数类名称
        val p = PsiToolUtils.extraParmaterFromFullyQualifiedName(paramaterClassName)
        //内置了默认值，如果不匹配的话则生成默认值和import语句
        val valueAndImport = CodeUtils.getDefaultValueAndDefaultImport(p.packagePath, p.className)
        val code = StringBuilder(valueAndImport.first).append(if (index != size - 1) "," else "")
        return Pair(code.toString(), valueAndImport.second)
    }

    /**
     * 是否需要生成默认值
     *
     * @return
     */
    protected abstract fun hasDefaultValue(): Boolean

}

class GenerateAllSetterNoDefaultValue : BaseGenerateAllSetter() {
    override fun hasDefaultValue(): Boolean {
        return false
    }
}

class GenerateAllSetterWithDefaultValue : BaseGenerateAllSetter() {
    override fun hasDefaultValue(): Boolean {
        return true
    }
}