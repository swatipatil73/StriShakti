package com.collage.empowermentstrishakti


import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.collage.empowermentstrishakti.Adapter.AllAdsAdapter
import com.collage.empowermentstrishakti.Common.SessionManager
import com.collage.empowermentstrishakti.Factory.AdsViewModelFactory
import com.collage.empowermentstrishakti.R
import com.collage.empowermentstrishakti.data.model.Ads.PostItem
import com.collage.empowermentstrishakti.data.model.Ads.Resource
import com.collage.empowermentstrishakti.data.model.post.AdPost
import com.collage.empowermentstrishakti.data.repository.AdsRepository
import com.collage.empowermentstrishakti.ui.RegisterViewModel.AdsViewModel

class AllAdsActivity : AppCompatActivity() {

    private lateinit var rvAds: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: AllAdsAdapter

    private val viewModel: AdsViewModel by lazy {
        val factory = AdsViewModelFactory(AdsRepository())
        ViewModelProvider(this, factory).get(AdsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_all_ads)

        // handle insets (optional)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvAds = findViewById(R.id.rvAds)
        progressBar = findViewById(R.id.progressBar)

        setupRecycler()
        setupObservers()

        val token = getAuthToken(this)
        if (!token.isNullOrBlank()) {
            viewModel.loadAds(token)
        } else {
            Toast.makeText(this, "Token missing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecycler() {
        adapter = AllAdsAdapter { item: AdPost ->
            //Toast.makeText(this, "Clicked: ${item.postId}", Toast.LENGTH_SHORT).show()
            val dialog = ImageDialog.newInstance(item.postImageUrl)
            dialog.show(supportFragmentManager, "image_dialog")
        }

        rvAds.layoutManager = LinearLayoutManager(this)
        rvAds.adapter = adapter
    }


    private fun setupObservers() {
        viewModel.ads.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    val list = resource.data?.postData ?: emptyList()
                    adapter.submitList(list)
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.message ?: "Error loading ads", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Get auth token from SessionManager and ensure it has "Bearer " prefix if needed.
     * If your backend already stores "Bearer <token>", this will return it unchanged.
     */
    private fun getAuthToken(context: Context): String? {
        val session = SessionManager(context)
        val raw = session.getToken() ?: return null
        return if (raw.startsWith("Bearer ", ignoreCase = true)) raw else "Bearer $raw"
    }
}
