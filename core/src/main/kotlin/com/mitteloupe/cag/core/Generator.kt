package com.mitteloupe.cag.core

interface Generator {
    fun generateFeature(request: GenerateFeatureRequest): String
}

class DefaultGenerator : Generator {
    override fun generateFeature(request: GenerateFeatureRequest): String {
        return "Success!"
    }
}
