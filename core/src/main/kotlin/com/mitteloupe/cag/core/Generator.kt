package com.mitteloupe.cag.core

interface Generator {
    fun generateFeature(featureName: String): String
}

class DefaultGenerator : Generator {
    override fun generateFeature(featureName: String): String {
        return "Generated feature: $featureName"
    }
}
