package com.github.tonyphoneix.genallsetter2kt.utils

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange

object PsiDocumentUtils {
    /**
     * return the string from the statementoffset position to the previous text
     * as the beginning of the next inserted text
     *
     * @param document
     * @param statementOffset
     * @return
     */
    fun calculateSplitText(document: Document, statementOffset: Int): String {
        var splitText = ""
        var cur = statementOffset
        var text = document.getText(TextRange(cur - 1, cur))
        while (text == " " || text == "\t") {
            splitText = text + splitText
            cur--
            if (cur < 1) {
                break
            }
            text = document.getText(TextRange(cur - 1, cur))
        }
        return "\n" + splitText;
    }
}