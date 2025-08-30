package com.mitteloupe.cag.core.content

fun buildDataSourceImplementationKotlinFile(
    packageName: String,
    dataSourceName: String
): String =
    """package $packageName

class ${dataSourceName}Impl : $dataSourceName {
}
"""
