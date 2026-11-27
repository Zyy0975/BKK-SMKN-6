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
    open val lokasiFilter: List<String> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val searchLayout = view.findViewById<TextInputLayout>(R.id.searchLayout)
        val etSearch = view.findViewById<TextInputEditText>(R.id.etSearch)
        val filterScroll = view.findViewById<HorizontalScrollView>(R.id.filterScroll)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroup)
        val locationScroll = view.findViewById<HorizontalScrollView>(R.id.locationScroll)
        val chipGroupLocation = view.findViewById<ChipGroup>(R.id.chipGroupLocation)
        val swipe = view.findViewById<SwipeRefreshLayout>(R.id.swipe)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        val stateView = view.findViewById<View>(R.id.stateView)
        val tvState = view.findViewById<TextView>(R.id.tvState)
        val btnRetry = view.findViewById<Button>(R.id.btnRetry)
        val progressMore = view.findViewById<LinearProgressIndicator>(R.id.progressMore)

        val adapter = createAdapter()

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        searchLayout.hint = getString(searchHint)
        etSearch.setText(vm.query)
        etSearch.setOnEditorActionListener {v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                vm.query = v.text?.toString()
                vm.refresh(clear = true)
                val imm = requireContext(). getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken,0)
                true
            }else false
        }

        etSearch.doAfterTextChanged {text ->
            if (text.isNullOrEmpty() && !vm.query.isNullOrEmpty()){
                vm.query = null
                vm.refresh(clear = true)
            }
        }

        if(tipeFilter.isNotEmpty()){
            filterScroll.isVisible = true
            tipeFilter.forEachIndexed { index, nama ->
                val chip = layoutInflater.inflate(R.layout.item_chip,chipGroup,false) as Chip
                chip.id = View.generateViewId()
                chip.text = nama
                chipGroup.addView(chip)
                val aktif = if (vm.type == null) index == 0 else vm.type == nama
                if (aktif) chipGroup.check(chip.id)
            }

            chipGroup.setOnCheckedStateChangeListener { group, ids ->
                val id = ids.firstOrNull() ?: return@setOnCheckedStateChangeListener
                val nama = group.findViewById<Chip>(id).text.toString()
                val baru = if (nama == tipeFilter.first()) null else nama
                if(baru != vm.type){
                    vm.type = baru
                    vm.refresh(clear = true)
                }
            }
        }
        val jobsVm = vm as? JobsViewModel
        if (jobsVm != null && lokasiFilter.isNotEmpty()){
            locationScroll.isVisible = true
            lokasiFilter.forEachIndexed { index, lokasi ->
                val chip = layoutInflater.inflate(R.layout.item_chip,chipGroupLocation,false) as Chip
                chip.id = View.generateViewId()
                chip.text = lokasi
                chipGroupLocation.addView(chip)
                val aktif = if (jobsVm.location == null) index == 0 else jobsVm.location == lokasi
                if (aktif) chipGroupLocation.check(chip.id)
            }

            chipGroupLocation.setOnCheckedStateChangeListener { group,ids->
                val id = ids.firstOrNull()?: return@setOnCheckedStateChangeListener
                val namaLokasi = group.findViewById<Chip>(id).text.toString()
                val baru = if (namaLokasi == lokasiFilter.first()) null else namaLokasi
                if (baru != jobsVm.location){
                    jobsVm.updateFilters(location = baru)
                }
            }
        }

        swipe.setColorSchemeColors(R.color.bkk_blue,R.color.bkk_orange)
        swipe.setOnRefreshListener { vm.refresh() }
        swipe.setOnChildScrollUpCallback { _, _ -> recycler.canScrollVertically(-1) }
        btnRetry.setOnClickListener { vm.refresh() }

        recycler.addOnScrollListener(object  : RecyclerView.OnScrollListener(){
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int){
                if (dy <= 0) return
                val lm = rv.layoutManager as LinearLayoutManager
                if (lm.findLastVisibleItemPosition() >= lm.itemCount - 3) vm.loadMore()
            }
        })

        fun render(){
            val kosong = vm.items.value.isNullOrEmpty()
            val memuat = vm.loading.value == true
            val error = vm.error.value
            stateView.isVisible = kosong && !memuat
            tvState.text = error ?: getString(R.string.data_kosong)
            btnRetry.isVisible = error != null
        }

        vm.loadInitial()
        vm.items.observe(viewLifecycleOwner){
            adapter.submitList(it)
            render()
        }
        vm.loading.observe(viewLifecycleOwner){
            swipe.isRefreshing = it
            render()
        }
        vm.loadingMore.observe(viewLifecycleOwner){ progressMore.isVisible = it }
        vm.error.observe(viewLifecycleOwner){ pesan ->
            render()
            if (pesan != null && !vm.items.value.isNullOrEmpty()){
                Toast.makeText(requireContext(), pesan, Toast.LENGTH_SHORT).show()
                vm.clearError()
            }
        }
    }
}

class JobsFragment : ListFragment<Lowongan>(){
    override val vm by viewModels<JobsViewModel>()
    override val searchHint = R.string.cari_lowongan
    override val tipeFilter = listOf("Semua", "Penuh Waktu", "Kontrak", "Magang", "Paruh Waktu")
    override val lokasiFilter = listOf("Semua Lokasi", "Surakarta", "Karanganyar","Kartasura", "Sukoharjo")
    override fun createAdapter() = LowonganAdapter{
        DetailActivity.open(requireContext(), DetailActivity.TYPE_JOB,it.id)
    }
}

class NewsFragment : ListFragment<Berita>(){
    override val vm by viewModels<NewsViewModel>()
    override val searchHint = R.string.cari_berita
    override fun createAdapter() = BeritaAdapter{
        DetailActivity.open(requireContext(), DetailActivity.TYPE_NEWS,it.id)
    }
}

class CompaniesFragment : ListFragment<Perusahaan>(){
    override val vm by viewModels<CompaniesViewModel>()
    override val searchHint = R.string.cari_perusahaan
    override fun createAdapter() = PerusahaanAdapter{
        DetailActivity.open(requireContext(), DetailActivity.TYPE_COMPANY,it.id)
    }
}