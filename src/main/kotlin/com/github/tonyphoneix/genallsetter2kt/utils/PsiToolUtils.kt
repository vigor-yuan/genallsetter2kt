package utils

import com.github.tonyphoneix.genallsetter2kt.entity.GenericParameter
import com.github.tonyphoneix.genallsetter2kt.entity.Parameter
import com.github.tonyphoneix.genallsetter2kt.write
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiJavaFile

object PsiToolUtils {

    /**
     * 通过参数的全限定名称来分解出需要的路径和类名
     *
     * @param fullyQualifiedName
     * @return
     */
    fun extraParmaterFromFullyQualifiedName(fullyQualifiedName: String): Parameter {
        val u = fullyQualifiedName.indexOf("<")
        return if (fullyQualifiedName.indexOf("<") == -1) {
            Parameter(fullyQualifiedName, extractShortName(fullyQualifiedName))
        } else {
            val packagePath = fullyQualifiedName.substring(0, u)
            val genericParamaters = fullyQualifiedName.substring(u + 1, fullyQualifiedName.length - 1)
                    .split(',').map { GenericParameter(extractShortName(it), it) }
            Parameter(packagePath, extractShortName(packagePath), genericParamaters)
        }
    }

    /**
     * 把imports 插入文件头中
     *
     * @param file
     * @param document
     * @param newImportList
     */
    fun addImportToFile(project: Project, file: PsiJavaFile, document: Document, newImportList: Set<String>) {
        if (newImportList.isEmpty()) return
        val newImportText = StringBuilder()
        for (newImport in newImportList) {
            newImportText.append("\nimport ").append(newImport).append(";")
        }
        val start = file.packageStatement?.let { it.textLength + it.textOffset } ?: 0
        if (newImportText.isNotBlank()) {
            document.write(project) { insertString(start, newImportText) }
        }
    }

    private fun extractShortName(fullName: String): String {
        return fullName.substring(fullName.lastIndexOf(".") + 1)
    }
}