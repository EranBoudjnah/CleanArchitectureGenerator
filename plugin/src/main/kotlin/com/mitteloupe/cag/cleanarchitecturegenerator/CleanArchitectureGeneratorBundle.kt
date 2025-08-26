package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE = "messages.CleanArchitectureGeneratorBundle"

object CleanArchitectureGeneratorBundle : DynamicBundle(BUNDLE) {
    @JvmStatic
    fun message(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any
    ): String = getMessage(key, *params)

    @JvmStatic
    fun messagePointer(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any
    ) = getLazyMessage(key, *params)
}
