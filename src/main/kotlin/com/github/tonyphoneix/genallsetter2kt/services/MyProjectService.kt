package com.github.tonyphoneix.genallsetter2kt.services

import com.intellij.openapi.project.Project
import com.github.tonyphoneix.genallsetter2kt.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
