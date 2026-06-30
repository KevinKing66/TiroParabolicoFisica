package com.king.kevin.tiroparabolico.presentation.screens

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.king.kevin.tiroparabolico.PhysicsLabApplication
import com.king.kevin.tiroparabolico.databinding.ActivityCourseManagementBinding
import com.king.kevin.tiroparabolico.presentation.viewmodel.CourseViewModel
import kotlinx.coroutines.launch

class CourseManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCourseManagementBinding
    private lateinit var viewModel: CourseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Assuming DI through Application as seen in MainActivity
        val app = application as PhysicsLabApplication
        viewModel = app.createCourseViewModel()

        observeState()
        setupActions()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                // UX: Hide management panel if not allowed
                binding.createCoursePanel.visibility = if (state.canManage) View.VISIBLE else View.GONE
                
                state.errorMessage?.let { 
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    viewModel.clearMessages()
                }
                
                state.successMessage?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    viewModel.clearMessages()
                }
            }
        }
    }

    private fun setupActions() {
        binding.btnCreateCourse.setOnClickListener {
            val code = binding.etCourseCode.text.toString()
            val name = binding.etCourseName.text.toString()
            val inst = binding.etInstitution.text.toString()
            
            if (code.isBlank() || name.isBlank()) {
                binding.etCourseCode.error = "Obligatorio"
                return@setOnClickListener
            }
            
            viewModel.saveCourse(code, name, inst)
        }
    }
}
