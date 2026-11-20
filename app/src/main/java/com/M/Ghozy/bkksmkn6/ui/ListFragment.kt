package com.M.Ghozy.bkksmkn6.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.HorizontalScrollView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.M.Ghozy.bkksmkn6.R
import com.M.Ghozy.bkksmkn6.data.Berita
import com.M.Ghozy.bkksmkn6.data.Lowongan
import com.M.Ghozy.bkksmkn6.data.Perusahaan
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

abstract class ListFragment<T>: Fragment(R.layout.fragment_list){
    abstract val vm: PagedViewModel<T>
    abstract fun createAdapter(): ListAdapter<T, *>

    @get:StringRes
    abstract val searchHint: Int
    open val tipeFilter: List<String> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val searchLayout = view.findViewById<TextInputLayout>(R.id.searchLayout)
        val etSearch = view.findViewById<TextInputLayout>(R.id.etSearch)
    }
}
