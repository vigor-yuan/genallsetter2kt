package com.github.tonyphoneix.genallsetter2kt

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project

fun Document.write(project: Project, block: Document.() -> Unit) {
    WriteCommandAction.runWriteCommandAction(project) { block() }
}

