package com.mitteloupe.cag.cleanarchitecturegenerator.form

import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

class PredicateDocumentFilter(
    private val predicate: (character: Char) -> Boolean
) : DocumentFilter() {
    override fun insertString(
        fb: FilterBypass,
        offset: Int,
        string: String?,
        attr: AttributeSet?
    ) {
        if (string == null) return
        val filtered = string.filter { predicate(it) }
        if (filtered.isNotEmpty()) {
            super.insertString(fb, offset, filtered, attr)
        }
    }

    override fun replace(
        fb: FilterBypass,
        offset: Int,
        length: Int,
        text: String?,
        attrs: AttributeSet?
    ) {
        val filtered = text?.filter { predicate(it) } ?: return
        super.replace(fb, offset, length, filtered, attrs)
    }
}
