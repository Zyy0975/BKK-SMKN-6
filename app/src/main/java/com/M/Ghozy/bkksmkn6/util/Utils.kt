package com.M.Ghozy.bkksmkn6.util

import android.text.method.LinkMovementMethod
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import coil.load
import com.M.Ghozy.bkksmkn6.R
import java.io.IOException
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import retrofit2.HttpException

object ApiError {
    fun message(e: Throwable): String = when (e) {
        is HttpException -> when (e.code()) {
            401 -> "Kunci API Tidak Valid Atau Sudah Dinonaktifkan."
            404 -> "Data Tidak Ditemukan."
            429 -> "Terlalu Banyak Permintaan, Tunggu Sebentar Lalu Coba Lagi."
            else -> "Terjadi Kesalahan Di Server (kode ${e.code()})"
        }

        is IOException -> "Tidak ada koneksi interent."
        else -> "Terjadi kesalahan: ${e.message}"
    }
}

    private val localeId = Locale("id", "ID")

/** "2026-10-14T07:44:00+07:00" atau "2026-11-04" -> "14 Okt 2026" */
    fun String?.tanggalIndo(): String? {
        if (this.isNullOrBlank()) return null
        val fmt = DateTimeFormatter.ofPattern("d MMM yyyy", localeId)
        return try {
            OffsetDateTime.parse(this).format(fmt)
        } catch (e: Exception) {
            this
        }
    }

fun ImageView.loadImage(url: String?) {
    if (url.isNullOrBlank()) {
        setImageResource(R.drawable.bg_placeholder)
    } else {
        load(url) {
            crossfade(true)
            placeholder(R.drawable.bg_placeholder)
            error(R.drawable.bg_placeholder)
        }
    }
}

fun TextView.setTextOrHide(value: String?) {
    if (value.isNullOrBlank()) {
        visibility = android.view.View.GONE
    } else {
        visibility = android.view.View.VISIBLE
        text = value
    }
}

fun TextView.setRichText(html: String?, plain: String?) {
    when {
        !html.isNullOrBlank() -> {
            visibility = android.view.View.VISIBLE
            text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT).trimEnd()
            movementMethod = LinkMovementMethod.getInstance()
        }

        !plain.isNullOrBlank() -> {
            visibility = android.view.View.VISIBLE
            text = plain.trim()
        }

        else -> visibility = android.view.View.GONE
    }
}