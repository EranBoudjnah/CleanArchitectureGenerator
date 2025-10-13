package com.mitteloupe.cag.cleanarchitecturegenerator.form

import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class OnChangeDocumentListener(
    private val onChange: () -> Unit
) : DocumentListener {
    override fun insertUpdate(e: DocumentEvent) = onChange()

    override fun removeUpdate(e: DocumentEvent) = onChange()

    override fun changedUpdate(e: DocumentEvent) = onChange()
}
