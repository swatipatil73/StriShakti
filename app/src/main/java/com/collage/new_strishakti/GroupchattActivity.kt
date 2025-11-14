package com.collage.new_strishakti

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.collage.new_strishakti.databinding.ActivityGroupchattBinding

class GroupchattActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupchattBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_groupchatt)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val groupId = intent.getStringExtra("GROUP_ID") ?: ""
        val groupTitle = intent.getStringExtra("TITLE") ?: "Group Chat"

        // Setup toolbar
        binding.toolbar.title = groupTitle
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Just a placeholder toast
        Toast.makeText(this, "Opened Group: $groupTitle ($groupId)", Toast.LENGTH_SHORT).show()

        // TODO: Add RecyclerView for messages, send button, etc.
    }
}