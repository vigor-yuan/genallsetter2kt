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
import com.intellij.psi.PsiDeclarationStatement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import utils.PsiToolUtils

/**
 * set method generator
 */
abstract class BaseGenerateAllSetter(codeType: GenCodeType) : BaseGenerate(codeType) {

    override fun update(e: AnActionEvent) {
        //Judgment of the visibility of the menu bar
        e.presentation.isVisible = available(e)
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (!available(e)) return
        //Because available has been pre-empted, it is treated as non-empty
        val project = e.project!!
        val editor = e.getData(PlatformDataKeys.EDITOR)!!
        val file = e.getData(PlatformDataKeys.PSI_FILE)!! as PsiJavaFile
        val document = editor.document
        //Find declared variable
        val variable = PsiElementUtils.getElement(editor, file)
            .let { PsiTreeUtil.getParentOfType(it, PsiLocalVariable::class.java) }!!
        //Get the variable type PsiClass, and then get all the set methods of PsiClass
        val allSetMethods = PsiClassUtils.extractSetMethods(PsiTypesUtil.getPsiClass(variable.type)!!)
        //Separate text, used at the beginning of each line
        val splitText = PsiDocumentUtils.calculateSplitText(document, variable.parent.textOffset)
        //Generate call code and import statement code
        genCodeAndImports(SetGenerateDTO(project, variable, allSetMethods, splitText, variable.name)).also {
            //Insert calling code safely
            document.write(project) { insertString(variable.parent.textOffset + variable.parent.textLength, it.code) }
            //Insert the import statement separately
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
        return PsiElementUtils.getElement(editor, file)?.let {
            PsiTreeUtil.getParentOfType(it, PsiLocalVariable::class.java)
        }?.takeIf { it.parent is PsiDeclarationStatement }?.let {
            PsiTypesUtil.getPsiClass(it.type)
        }?.let { PsiClassUtils.checkClassHasValidSetMethod(it) } ?: false
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
        val getters = if (GenCodeType.GETTER == this.codeType) {
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
        method: PsiMethod,
        getters: List<ExtMethod>
    ): CodeAndImports {
        return CodeAndImports()
    }
}

class GenerateAllSetterWithDefaultValue : BaseGenerateAllSetter(GenCodeType.DEFAULT) {

    override fun genCodeAndImportsFromMethod(
        method: PsiMethod,
        getters: List<ExtMethod>
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

class GenerateAllSetterWithGetter : BaseGenerateAllSetter(GenCodeType.GETTER) {

    override fun genCodeAndImportsFromMethod(
        method: PsiMethod,
        getters: List<ExtMethod>
    ): CodeAndImports {
        val extSetMethod = ExtMethod.extSetMethod(psiMethod = method) ?: return CodeAndImports()
        return getters.firstOrNull {
            it.fieldName == extSetMethod.fieldName && it.psiType == extSetMethod.psiType
        }?.let {
            CodeAndImports("${it.caller}.get${it.fieldName}()")
        } ?: CodeAndImports()
    }
}