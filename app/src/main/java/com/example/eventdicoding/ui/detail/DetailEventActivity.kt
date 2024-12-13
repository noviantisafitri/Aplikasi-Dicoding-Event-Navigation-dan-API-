package com.example.eventdicoding.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.example.eventdicoding.databinding.ActivityDetailBinding
import com.example.eventdicoding.databinding.LayoutErrorBinding
import androidx.core.text.HtmlCompat

class DetailEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val detailEventViewModel: DetailEventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val upArrow = resources.getDrawable(androidx.appcompat.R.drawable.abc_ic_ab_back_material, null)
        val wrappedDrawable = DrawableCompat.wrap(upArrow)
        DrawableCompat.setTint(wrappedDrawable, resources.getColor(android.R.color.white))
        supportActionBar?.setHomeAsUpIndicator(wrappedDrawable)
        binding.toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val eventId = intent.getIntExtra("id", 0)
        if (eventId == 0) {
            Toast.makeText(this, "Event ID tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        detailEventViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        detailEventViewModel.eventDetail.observe(this) { detailEventResponse ->
            detailEventResponse?.event?.let { event ->
                binding.tvEventName.text = event.name ?: "Nama"
                supportActionBar?.title = event.name ?: "Event Detail"
                binding.tvDescription.text = HtmlCompat.fromHtml(event.description ?: "No Description", HtmlCompat.FROM_HTML_MODE_LEGACY)
                binding.tvOwnerName.text = event.ownerName ?: "Owner"
                binding.tvBeginTime.text = event.beginTime ?: "Waktu"
                val availableQuota = event.quota?.minus(event.registrants ?: 0)
                binding.tvQuota.text = if (availableQuota != null && availableQuota > 0) {
                    "Sisa Kuota: $availableQuota"
                } else {
                    "Kuota Penuh"
                }

                Glide.with(this).load(event.mediaCover).into(binding.ivEventImage)

                if (!event.link.isNullOrEmpty()) {
                    binding.btnOpenLink.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.link))
                        startActivity(intent)
                    }
                } else {
                    binding.btnOpenLink.isEnabled = false
                }

                binding.includeErrorLayout.root.visibility = View.GONE
                binding.contentLayout.visibility = View.VISIBLE
            } ?: run {
                showError("Failed to load event details.")
            }
        }

        detailEventViewModel.errorMessage.observe(this) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                showError("No internet connection")
            }
        }

        val errorBinding = LayoutErrorBinding.bind(binding.includeErrorLayout.root)
        errorBinding.btnRetry.setOnClickListener {
            errorBinding.errorLayout.visibility = View.GONE
            loadData(eventId)
        }

        loadData(eventId)
    }

    private fun loadData(eventId: Int) {
        detailEventViewModel.getDetailEvent(eventId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.contentLayout.visibility = View.GONE
            binding.includeErrorLayout.root.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.contentLayout.visibility = View.GONE
        binding.includeErrorLayout.root.visibility = View.VISIBLE
        val errorBinding = LayoutErrorBinding.bind(binding.includeErrorLayout.root)
        errorBinding.tvErrorMessage.text = message
    }
}
