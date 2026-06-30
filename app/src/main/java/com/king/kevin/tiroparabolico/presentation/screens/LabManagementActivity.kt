package com.king.kevin.tiroparabolico.presentation.screens

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.king.kevin.tiroparabolico.PhysicsLabApplication
import com.king.kevin.tiroparabolico.databinding.ActivityLabManagementBinding
import com.king.kevin.tiroparabolico.presentation.viewmodel.LabViewModel
import kotlinx.coroutines.launch

class LabManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLabManagementBinding
    private lateinit var viewModel: LabViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val app = application as PhysicsLabApplication
        viewModel = app.createLabViewModel()
        
        observeState()
        setupActions()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Basic check for lab visibility if needed
                state.errorMessage?.let { 
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                }
                state.successMessage?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupActions() {
        binding.btnAddLab.setOnClickListener {
            val code = binding.etLabCode.text.toString()
            val name = binding.etLabName.text.toString()
            // In a real app, we'd get the courseCode from intent extras or a selection
            val courseCode = intent.getStringExtra("COURSE_CODE") ?: return@setOnClickListener
            
            viewModel.createLab(code, name, courseCode, emptyList())
        }
    }
}
