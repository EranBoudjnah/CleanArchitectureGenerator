package com.mitteloupe.cag.core

import java.io.File

interface ProjectModel {
    fun selectedModuleRootDir(): File?

    fun allModuleRootDirs(): List<File>
}
