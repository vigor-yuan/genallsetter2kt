package com.github.tonyphoneix.genallsetter2kt.utils

object CodeUtils {

    private val staticTypes = listOf("boolean", "byte", "int", "short", "long", "float", "double", "char")

    private val defaultValueMap = mapOf(
        "boolean" to "false",
        "java.lang.Boolean" to "false",
        "int" to "0",
        "byte" to "(byte)0",
        "java.lang.Byte" to "(byte)0",
        "java.lang.Integer" to "0",
        "java.lang.String" to "\"\"",
        "java.math.BigDecimal" to "new BigDecimal(\"0\")",
        "java.lang.Long" to "0L",
        "long" to "0L",
        "short" to "(short)0",
        "java.lang.Short" to "(short)0",
        "java.util.Date" to "new Date()",
        "float" to "0.0F",
        "java.lang.Float" to "0.0F",
        "double" to "0.0D",
        "java.lang.Double" to "0.0D",
        "java.lang.Character" to "\'\'",
        "char" to "\'\'",
        "java.time.LocalDateTime" to "LocalDateTime.now()",
        "java.time.LocalDate" to "LocalDate.now()",
        "java.time.OffsetDateTime" to "OffsetDateTime.now()",
        "java.util.Optional" to "Optional.empty()",
        "java.util.List" to "new ArrayList()",
        "java.util.ArrayList" to "new ArrayList()",
        "java.util.Collection" to "new ArrayList()",
        "java.util.Set" to "new HashSet()",
        "java.util.HashSet" to "new HashSet()",
        "java.util.Map" to "new HashMap()",
        "java.util.HashMap" to "new HashMap()"
    )

    private val defaultImports: Map<String, String> = mapOf(
        "java.util.List" to "java.util.ArrayList",
        "java.util.Set" to "java.util.HashSet",
        "java.util.Map" to "java.util.HashMap"
    )

    /**
     * Get the default implementation and package through the class declaration path
     *
     * @param packagePath
     * @param className
     * @return
     */
    fun getDefaultValueAndDefaultImport(packagePath: String, className: String): Pair<String, String> {
        val value = defaultValueMap[packagePath]
        if (packagePath.isBlank() || value.isNullOrBlank()) return "new $className()" to packagePath
        val defaultImport = defaultImports[packagePath] ?: packagePath
        return value to defaultImport
    }

    fun isNeedToDeclareClasses(packagePath: String): Boolean {
        return !(packagePath.isBlank() || packagePath.startsWith("java.lang") || staticTypes.contains(packagePath))
    }
}