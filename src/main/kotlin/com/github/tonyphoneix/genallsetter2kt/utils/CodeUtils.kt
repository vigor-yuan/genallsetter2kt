package com.github.tonyphoneix.genallsetter2kt.utils

object CodeUtils {

    private val staticTypes = listOf("boolean", "byte", "int", "short", "long", "float", "double", "char")

    private val defaultValueMap = mapOf(
            Pair("boolean", "false"),
            Pair("java.lang.Boolean", "false"),
            Pair("int", "0"),
            Pair("byte", "(byte)0"),
            Pair("java.lang.Byte", "(byte)0"),
            Pair("java.lang.Integer", "0"),
            Pair("java.lang.String", "\"\""),
            Pair("java.math.BigDecimal", "new BigDecimal(\"0\")"),
            Pair("java.lang.Long", "0L"),
            Pair("long", "0L"),
            Pair("short", "(short)0"),
            Pair("java.lang.Short", "(short)0"),
            Pair("java.util.Date", "new Date()"),
            Pair("float", "0.0F"),
            Pair("java.lang.Float", "0.0F"),
            Pair("double", "0.0D"),
            Pair("java.lang.Double", "0.0D"),
            Pair("java.lang.Character", "\'\'"),
            Pair("char", "\'\'"),
            Pair("java.time.LocalDateTime", "LocalDateTime.now()"),
            Pair("java.time.LocalDate", "LocalDate.now()"),
            Pair("java.time.OffsetDateTime", "OffsetDateTime.now()"),
            Pair("java.util.Optional", "Optional.empty()"),
            Pair("java.util.List", "new ArrayList()"),
            Pair("java.util.ArrayList", "new ArrayList()"),
            Pair("java.util.Collection", "new ArrayList()"),
            Pair("java.util.Set", "new HashSet()"),
            Pair("java.util.HashSet", "new HashSet()"),
            Pair("java.util.Map", "new HashMap()"),
            Pair("java.util.HashMap", "new HashMap()"))

    private val defaultImports: Map<String, String> = mapOf(
            Pair("java.util.List", "java.util.ArrayList"),
            Pair("java.util.Set", "java.util.HashSet"),
            Pair("java.util.Map", "java.util.HashMap"))

    /**
     * Get the default implementation and package through the class declaration path
     *
     * @param packagePath
     * @return
     */
    fun getDefaultValueAndDefaultImport(packagePath: String, className: String): Pair<String, String> {
        val value = defaultValueMap[packagePath]
        if (packagePath.isBlank() || value.isNullOrBlank()) return Pair("new $className()", packagePath)
        val defaultImport = defaultImports[packagePath] ?: packagePath
        return Pair(value, defaultImport)
    }

    fun isNeedToDeclareClasses(packagePath: String): Boolean {
        return !(packagePath.startsWith("java.lang") || staticTypes.contains(packagePath))
    }
}