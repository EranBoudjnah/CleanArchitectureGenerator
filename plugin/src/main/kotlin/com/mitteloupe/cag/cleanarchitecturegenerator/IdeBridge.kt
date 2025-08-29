package com.mitteloupe.cag.cleanarchitecturegenerator

import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.mitteloupe.cag.core.ERROR_PREFIX
import java.io.File

class IdeBridge {
    fun refreshIde(projectRootDirectory: File) {
        val virtualRoot = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(projectRootDirectory)
        if (virtualRoot == null) {
            VirtualFileManager.getInstance().asyncRefresh(null)
        } else {
            VfsUtil.markDirtyAndRefresh(true, true, true, virtualRoot)
        }
    }

    fun synchronizeGradle(
        project: Project?,
        result: String,
        projectRootDir: File
    ) {
        if (project != null && !result.startsWith(ERROR_PREFIX)) {
            ExternalSystemUtil.refreshProject(
                projectRootDir.absolutePath,
                ImportSpecBuilder(project, ProjectSystemId("GRADLE")).build()
            )
        }
    }
}
