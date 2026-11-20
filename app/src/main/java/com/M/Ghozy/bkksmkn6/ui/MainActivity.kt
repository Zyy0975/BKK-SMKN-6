package com.M.Ghozy.bkksmkn6.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.M.Ghozy.bkksmkn6.R
import com.M.Ghozy.bkksmkn6.data.ApiClient
import com.M.Ghozy.bkksmkn6.util.ApiError
import kotlinx.coroutines.launch

// MainActivity SEMENTARA, hanya untuk menguji API (akan diganti di Langkah 15)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            try {
                val hasil = ApiClient.api.jobs(perPage = 5)
                Log.d("BKK", "Total lowongan: ${hasil.meta?.total}")
                hasil.data.orEmpty().forEach {
                    Log.d("BKK", "${it.title} - ${it.company?.name}")
                }
            } catch (e: Exception) {
                Log.e("BKK", ApiError.message(e))
            }
        }
    }
}