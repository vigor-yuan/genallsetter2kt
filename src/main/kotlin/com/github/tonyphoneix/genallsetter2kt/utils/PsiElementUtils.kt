package com.github.tonyphoneix.genallsetter2kt.utils

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

object PsiElementUtils {
    /**
     * 获取光标处的元素
     *
     * @param editor
     * @param file
     * @return
     */
    fun getElement(editor: Editor, file: PsiFile): PsiElement? {
        val caretModel = editor.caretModel
        val position = caretModel.offset
        return file.findElementAt(position)
    }
}