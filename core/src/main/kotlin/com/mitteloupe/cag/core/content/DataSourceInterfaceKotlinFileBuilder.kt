package com.mitteloupe.cag.core.content

fun buildDataSourceInterfaceKotlinFile(
    packageName: String,
    dataSourceName: String
): String =
    """package $packageName

interface $dataSourceName {
}
"""
