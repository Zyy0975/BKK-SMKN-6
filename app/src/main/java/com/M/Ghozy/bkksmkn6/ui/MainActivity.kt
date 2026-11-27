package com.M.Ghozy.bkksmkn6.ui

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.M.Ghozy.bkksmkn6.R
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootMain = findViewById<View>(R.id.rootMain)
        val appBar = findViewById<AppBarLayout>(R.id.appBar)
        val pager = findViewById<ViewPager2>(R.id.pager)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        ViewCompat.setOnApplyWindowInsetsListener(rootMain) { _, insets ->
            val atas = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
            ).top
            appBar.updatePadding(top = atas)
            insets
        }

        val menuHalaman = listOf(R.id.nav_jobs, R.id.nav_news, R.id.nav_companies)

        pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = menuHalaman.size
            override fun createFragment(position: Int): Fragment = when (position) {
                0 -> JobsFragment()
                1 -> NewsFragment()
                else -> CompaniesFragment()
            }
        }

        pager.isUserInputEnabled = false
        pager.offscreenPageLimit = menuHalaman.size

        bottomNav.setOnItemSelectedListener { item ->
            val index = menuHalaman.indexOf(item.itemId)
            if (index < 0) return@setOnItemSelectedListener false
            pager.setCurrentItem(index, false)
            appBar.setExpanded(true, true)
            true
        }
    }
}