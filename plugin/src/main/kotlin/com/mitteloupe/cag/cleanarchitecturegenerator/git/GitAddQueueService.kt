package com.mitteloupe.cag.cleanarchitecturegenerator.git

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.io.File
import java.util.concurrent.ConcurrentSkipListSet

@Service(Service.Level.PROJECT)
class GitAddQueueService(private val project: Project) {
    private val queue = ConcurrentSkipListSet<String>()
    private val gitStager = GitStager()

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

        gitStager.stage(projectRoot, items.map(::File))
        queue.removeAll(items)
    }
}
