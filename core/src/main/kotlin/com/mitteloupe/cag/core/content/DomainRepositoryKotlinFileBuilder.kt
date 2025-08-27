package com.mitteloupe.cag.core.content

fun buildDomainRepositoryKotlinFile(featurePackageName: String): String =
    """package $featurePackageName.domain.repository

interface PerformExampleRepository {
    fun perform(input: Unit): Unit
}
"""
