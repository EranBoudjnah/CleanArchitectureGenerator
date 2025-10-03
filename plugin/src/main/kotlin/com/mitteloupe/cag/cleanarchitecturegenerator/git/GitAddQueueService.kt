package com.mitteloupe.cag.cleanarchitecturegenerator.git

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.mitteloupe.cag.cleanarchitecturegenerator.settings.AppSettingsService
import com.mitteloupe.cag.git.Git
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

@Service(Service.Level.PROJECT)
class GitAddQueueService(private val project: Project) {
    private val queue = ConcurrentSkipListSet<String>()
    private val git: Git by lazy {
        Git(gitBinaryPath = AppSettingsService.getInstance().gitPath)
    }

    fun enqueue(file: File) {
        val path = file.absolutePath
        queue.add(path)
    }

    fun flush() {
        val basePath = project.basePath ?: return
        val projectRoot = File(basePath)
        if (!projectRoot.exists()) {
            return
        }

        val items = queue.toSet()
        if (items.isEmpty()) {
            return
        }

        if (!git.isAvailable(projectRoot)) return

        git.stage(projectRoot, items.map(::File))
        queue.removeAll(items)
    }
}
