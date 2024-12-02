package com.zenmo.zummon.companysurvey

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class File (
    val blobName: String,
    val originalName: String,
    val contentType: String?,
    val size: Int,
)

@JsExport
fun formatByteSize(size: Int) = when {
    size < 1024 -> "$size B"
    size < 1024 * 1024 -> "${size / 1024} KiB"
    size < 1024 * 1024 * 1024 -> "${size / 1024 / 1024} MiB"
    else -> "${size / 1024 / 1024 / 1024} GiB"
}
