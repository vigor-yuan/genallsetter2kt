package com.github.tonyphoneix.genallsetter2kt.entity


/**
 * 方法参数的拆解
 */
data class Parameter(var packagePath: String = "", var className: String = "", var genericParameters: List<GenericParameter> = emptyList())

/**
 * 包名拆解
 */
data class GenericParameter(var realPackage: String = "", var realName: String = "")