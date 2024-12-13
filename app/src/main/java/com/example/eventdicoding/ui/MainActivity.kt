package com.example.eventdicoding.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.eventdicoding.R
import com.example.eventdicoding.databinding.ActivityMainBinding
import com.example.eventdicoding.ui.home.EventFragment
import com.example.eventdicoding.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSearchBar()

        supportActionBar?.hide()

        loadFragment(HomeFragment())

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_upcoming -> {
                    loadFragment(EventFragment.newInstance(1))
                    true
                }
                R.id.navigation_finished -> {
                    loadFragment(EventFragment.newInstance(0))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setupSearchBar() {
        binding.searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    searchInCurrentFragment(query)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.searchView.setupWithSearchBar(binding.searchBar)

        binding.searchView.editText.setOnEditorActionListener { _, _, _ ->
            val query = binding.searchView.text.toString().trim()
            if (query.isNotEmpty()) {
                binding.searchBar.setText(query)
                binding.searchView.hide()

                searchInCurrentFragment(query)

                binding.searchBar.setText("")
                binding.searchView.editText.setText("")
            }
            false
        }
    }

    private fun searchInCurrentFragment(query: String) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        when (currentFragment) {
            is HomeFragment -> {
                currentFragment.search(query)
            }
            is EventFragment -> {
                currentFragment.search(query)
            }
            else -> {
                Toast.makeText(this, "No search functionality in the current screen", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
