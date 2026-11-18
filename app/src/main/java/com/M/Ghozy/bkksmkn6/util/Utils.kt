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
            else -> "Terjadi Kesalahan Di Server (kode ${e.message}"
        }
    }

    private val localeId = Locale("id", "ID")
    fun String?.tanggalIndo(): String? {
        if (this.isNullOrBlank()) return null
        val fmt = DateTimeFormatter.ofPattern("d MMM yyyy", localeId)
        return try {
            OffsetDateTime.parse(this).format(fmt)
        } catch (e: Exception) {
            this
        }
    }
}

fun ImageView.loadImage(url: String?){
    if (url.isNullOrBlank()){

        }
}
