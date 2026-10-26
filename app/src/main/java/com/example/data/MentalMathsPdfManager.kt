package com.example.data

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class MathPdfItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val localFilePath: String,
    val pageCount: Int,
    val isCustom: Boolean,
    val colorHex: Long
)

class MentalMathsPdfManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("mental_maths_prefs", Context.MODE_PRIVATE)
    private val pdfDir = File(context.filesDir, "mental_maths_pdfs").apply {
        if (!exists()) mkdirs()
    }

    private val defaultPdfs = listOf(
        DefaultPdfMeta(
            id = "default_addition",
            title = "கூட்டல் மனக்கணக்கு (500)",
            subtitle = "தொடக்கநிலை எளிய கூட்டல் பயிற்சிகள்",
            assetName = "pdf/addition_500.pdf",
            colorHex = 0xFF7C3AED
        ),
        DefaultPdfMeta(
            id = "default_subtraction",
            title = "கழித்தல் மனக்கணக்கு (500)",
            subtitle = "தொடக்கநிலை எளிய கழித்தல் பயிற்சிகள்",
            assetName = "pdf/subtraction_500.pdf",
            colorHex = 0xFF2196F3
        ),
        DefaultPdfMeta(
            id = "default_multiplication",
            title = "பெருக்கல் மனக்கணக்கு (500)",
            subtitle = "தொடக்கநிலை எளிய பெருக்கல் பயிற்சிகள்",
            assetName = "pdf/multiplication_500.pdf",
            colorHex = 0xFF20C997
        ),
        DefaultPdfMeta(
            id = "default_division",
            title = "வகுத்தல் மனக்கணக்கு (500)",
            subtitle = "தொடக்கநிலை எளிய வகுத்தல் பயிற்சிகள்",
            assetName = "pdf/division_500.pdf",
            colorHex = 0xFFFFA726
        )
    )

    private data class DefaultPdfMeta(
        val id: String,
        val title: String,
        val subtitle: String,
        val assetName: String,
        val colorHex: Long
    )

    fun getDeletedIds(): Set<String> {
        return prefs.getStringSet("deleted_pdf_ids", emptySet()) ?: emptySet()
    }

    fun getAllPdfs(): List<MathPdfItem> {
        val deletedIds = getDeletedIds()
        val list = mutableListOf<MathPdfItem>()

        // 1. Load default PDFs (if not deleted by user)
        for (meta in defaultPdfs) {
            if (!deletedIds.contains(meta.id)) {
                val targetFile = File(pdfDir, "${meta.id}.pdf")
                if (!targetFile.exists()) {
                    copyAssetToFile(meta.assetName, targetFile)
                }
                if (targetFile.exists() && targetFile.length() > 0) {
                    val pages = getPdfPageCount(targetFile)
                    list.add(
                        MathPdfItem(
                            id = meta.id,
                            title = meta.title,
                            subtitle = meta.subtitle,
                            localFilePath = targetFile.absolutePath,
                            pageCount = pages,
                            isCustom = false,
                            colorHex = meta.colorHex
                        )
                    )
                }
            }
        }

        // 2. Load custom user-added PDFs
        val customJson = prefs.getString("custom_math_pdfs", "[]") ?: "[]"
        try {
            val jsonArray = JSONArray(customJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                if (!deletedIds.contains(id)) {
                    val path = obj.getString("path")
                    val file = File(path)
                    if (file.exists() && file.length() > 0) {
                        list.add(
                            MathPdfItem(
                                id = id,
                                title = obj.getString("title"),
                                subtitle = obj.optString("subtitle", "பயனர் சேர்த்த புத்தகம்"),
                                localFilePath = file.absolutePath,
                                pageCount = obj.optInt("pageCount", getPdfPageCount(file)),
                                isCustom = true,
                                colorHex = obj.optLong("colorHex", 0xFFEC4899)
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}

        return list
    }

    fun addCustomPdf(uri: Uri, customTitle: String? = null): MathPdfItem? {
        return try {
            val id = "custom_${System.currentTimeMillis()}"
            val fileName = getFileNameFromUri(uri) ?: "$id.pdf"
            val displayTitle = customTitle?.takeIf { it.isNotBlank() }
                ?: fileName.removeSuffix(".pdf").replace("_", " ")

            val targetFile = File(pdfDir, "$id.pdf")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (!targetFile.exists() || targetFile.length() == 0L) return null

            val pageCount = getPdfPageCount(targetFile)
            val colorHex = listOf(0xFFEC4899, 0xFF8B5CF6, 0xFF10B981, 0xFFF59E0B).random()

            // Save to SharedPreferences
            val customJson = prefs.getString("custom_math_pdfs", "[]") ?: "[]"
            val jsonArray = JSONArray(customJson)
            val newObj = JSONObject().apply {
                put("id", id)
                put("title", displayTitle)
                put("subtitle", "$pageCount பக்கங்கள் • மனக்கணக்கு பயிற்சி")
                put("path", targetFile.absolutePath)
                put("pageCount", pageCount)
                put("colorHex", colorHex)
            }
            jsonArray.put(newObj)
            prefs.edit().putString("custom_math_pdfs", jsonArray.toString()).apply()

            MathPdfItem(
                id = id,
                title = displayTitle,
                subtitle = "$pageCount பக்கங்கள் • மனக்கணக்கு பயிற்சி",
                localFilePath = targetFile.absolutePath,
                pageCount = pageCount,
                isCustom = true,
                colorHex = colorHex
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deletePdf(id: String): Boolean {
        return try {
            // 1. Mark in deleted_ids set (so it NEVER returns)
            val deleted = getDeletedIds().toMutableSet()
            deleted.add(id)
            prefs.edit().putStringSet("deleted_pdf_ids", deleted).apply()

            // 2. Delete the physical file from internal storage
            val file = File(pdfDir, "$id.pdf")
            if (file.exists()) {
                file.delete()
            }

            // 3. Clean up custom list if applicable
            val customJson = prefs.getString("custom_math_pdfs", "[]") ?: "[]"
            val jsonArray = JSONArray(customJson)
            val newArray = JSONArray()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.getString("id") != id) {
                    newArray.put(obj)
                }
            }
            prefs.edit().putString("custom_math_pdfs", newArray.toString()).apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun copyAssetToFile(assetPath: String, outFile: File) {
        try {
            context.assets.open(assetPath).use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getPdfPageCount(file: File): Int {
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        return try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            renderer.pageCount
        } catch (_: Exception) {
            1
        } finally {
            renderer?.close()
            pfd?.close()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }
}
