package com.mitteloupe.cag.cli

import com.mitteloupe.cag.core.DefaultGenerator

fun main(args: Array<String>) {
    val featureName = args.firstOrNull() ?: "SampleFeature"
    val generator = DefaultGenerator()
    val result = generator.generateFeature(featureName)
    println(result)
}
