package com.mitteloupe.cag.core.kotlinpackage

import java.io.File

fun buildPackageDirectory(
    root: File,
    packageSegments: List<String>
): File = packageSegments.fold(root) { parent, segment -> File(parent, segment) }
