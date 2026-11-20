package com.M.Ghozy.bkksmkn6.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.M.Ghozy.bkksmkn6.R
import com.M.Ghozy.bkksmkn6.data.ApiClient
import com.M.Ghozy.bkksmkn6.data.Berita
import com.M.Ghozy.bkksmkn6.data.Lowongan
import com.M.Ghozy.bkksmkn6.data.Perusahaan
import com.M.Ghozy.bkksmkn6.util.ApiError
import com.M.Ghozy.bkksmkn6.util.loadImage
import com.M.Ghozy.bkksmkn6.util.setRichText
import com.M.Ghozy.bkksmkn6.util.setTextOrHide
import com.M.Ghozy.bkksmkn6.util.tanggalIndo
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.crypto.AEADBadTagException

class DetailActivity: AppCompatActivity(){
    private lateinit var rootDetail: View
    private lateinit var toolbar: MaterialToolbar
    private lateinit var scroll: NestedScrollView
    private lateinit var ivHero: ImageView
    private lateinit var ivLogo: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvMeta: TextView
    private lateinit var sections: LinearLayout
    private lateinit var tvJobsHeader: TextView
    private lateinit var rvJobs: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var errorView: LinearLayout
    private lateinit var tvError: TextView
    private lateinit var btnRetry: Button
    private lateinit var applyBar: LinearLayout
    private lateinit var btnAplly: Button

    private var shareText: String? = null
    private var applyUrl: String? = null

    private val type by lazy {intent.getStringExtra(EXTRA_TYPE) ?: TYPE_JOB}
    private val itemId by lazy { intent.getIntExtra(EXTRA_ID, 0) }

    private val jobsAdapter = LowonganAdapter{ open(this, TYPE_JOB, it.id) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        initViews()

        ViewCompat.setOnApplyWindowInsetsListener(rootDetail){ view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.updatePadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(
            when(type){
            TYPE_JOB -> R.string.detail_lowongan
            TYPE_NEWS -> R.string.detail_berita
            else -> R.string.detail_perusahaan
            }
        )

        rvJobs.layoutManager = LinearLayoutManager(this)
        rvJobs.adapter = jobsAdapter
        btnRetry.setOnClickListener { load() }
        btnAplly.setOnClickListener { bukaTautan(applyUrl) }
        load()
    }

    private fun initViews(){
        rootDetail = findViewById(R.id.rootDetail)
        toolbar = findViewById(R.id.toolbar)
        scroll = findViewById(R.id.scroll)
        ivHero = findViewById(R.id.ivHero)
        ivLogo = findViewById(R.id.ivLogo)
        tvTitle = findViewById(R.id.tvTitle)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        tvMeta = findViewById(R.id.tvMeta)
        sections = findViewById(R.id.sections)
        tvJobsHeader = findViewById(R.id.tvJobsHeader)
        rvJobs = findViewById(R.id.rvJobs)
        progress = findViewById(R.id.progress)
        errorView = findViewById(R.id.errorView)
        tvError = findViewById(R.id.tvError)
        btnRetry = findViewById(R.id.btnRetry)
        applyBar = findViewById(R.id.applyBar)
        btnAplly = findViewById(R.id.btnApply)
    }

    private fun load(){
        progress.isVisible = true
        errorView.isVisible = false
        scroll.isVisible = false
        applyBar.isVisible = false
        sections.removeAllViews()

        lifecycleScope.launch {
            try{
                when(type){
                    TYPE_JOB -> showJob(ApiClient.api.job(itemId).data)
                    TYPE_NEWS -> showNews(ApiClient.api.newsDetail(itemId).data)
                    else -> showCompany(ApiClient.api.company(itemId).data)
                }
                scroll.isVisible = true
            } catch (e: CancellationException){
                throw e
            }catch (e: Exception){
                tvError.text = ApiError.message(e)
                errorView.isVisible = true
            }finally {
                progress.isVisible = false
            }
        }
    }

    private fun showJob(l: Lowongan){
        ivHero.isVisible = !l.imageUrl.isNullOrBlank()
        if (ivHero.isVisible) ivHero.loadImage(l.imageUrl)
        ivLogo.isVisible = true
        ivLogo.loadImage(l.company?.name)

        val baris = listOfNotNull(
            listOfNotNull(l.type,l.location,l.field).filter { it.isNotBlank() }
                .joinToString ("•").ifBlank { null },
            l.salaryText?.takeIf { it.isNotBlank() }?.let{"Gaji: $it"},
            l.deadline.tanggalIndo()?.let { "Batas lamaran: $it" },
            if (l.isOpen == false) "Lowongan ini sudah ditutup" else null
        )
        tvMeta.setTextOrHide(baris.joinToString ("\n"))

        addSection("Deskripsi Pekerjaan", l.descriptionHtml, l.descriptionText)
        addSection("Persyaratan",l.requirementsHtml, l.requirementsText)
        addSection("Cara Melamar",l.howToApplyHtml,l.howToApplyText)
        setShare(l.title,l.url)

        applyUrl = l.url
        applyBar.isVisible = l.isOpen != false && !l.url.isNullOrBlank()
    }

    private fun bukaTautan(url: String?){
        if (url.isNullOrBlank()) return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }catch (e: ActivityNotFoundException){
            Toast.makeText(this,R.string.link_gagal,Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNews(n: Berita){
        ivHero.isVisible = !n.thumbUrl.isNullOrBlank()
        if (ivHero.isVisible) ivHero.loadImage(n.thumbUrl)
        tvTitle.text = n.title
        tvSubtitle.setTextOrHide(n.publishedAt.tanggalIndo())
        tvMeta.isVisible = false
        addSection(null,n.bodyHtml,n.bodyText?:n.summary)
        setShare(n.title,n.url)
    }

    private fun showCompany(p: Perusahaan){
        ivLogo.isVisible = true
        ivLogo.loadImage(p.logoUrl)
        tvTitle.text = p.name
        tvSubtitle.setTextOrHide(p.city)
        tvMeta.isVisible = false
        addSection("Tentang Perusahaan", p.descriptionHtml, p.descriptionText)

        val lowongan = p.jobs.orEmpty()
        tvJobsHeader.isVisible = lowongan.isNotEmpty()
        jobsAdapter.submitList(lowongan)
        setShare(p.name,p.url)
    }

    private fun addSection(judul: String?, html: String?, teks: String?){
        if (html.isNullOrBlank() && teks.isNullOrBlank()) return
        val item = layoutInflater.inflate(R.layout.item_section, sections, false)
        item.findViewById<TextView>(R.id.tvHeading).setTextOrHide(judul)
        item.findViewById<TextView>(R.id.tvBody).setRichText(html, teks)
        sections.addView(item)
    }

    private fun setShare(judul: String?, url: String?) {
        shareText = listOfNotNull(judul, url).joinToString("\n").ifBlank { null }
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail,menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_share)?.isVisible = shareText != null
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId){
        R.id.action_share -> {
            val kirim = Intent(Intent.ACTION_SEND).apply{
                this.type = "text/plain"
                putExtra(Intent.EXTRA_TEXT,shareText)
            }
            startActivity(Intent.createChooser(kirim,getString(R.string.bagikan)))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object{
        const val TYPE_JOB = "job"
        const val TYPE_NEWS = "news"
        const val TYPE_COMPANY = "company"
        private const val EXTRA_TYPE = "type"
        private const val EXTRA_ID = "id"

        fun open(context: Context,type: String,id: Int){
            context.startActivity(
                Intent(context, DetailActivity::class.java)
                    .putExtra(EXTRA_TYPE, type)
                    .putExtra(EXTRA_ID, id)
            )
        }
    }
}
