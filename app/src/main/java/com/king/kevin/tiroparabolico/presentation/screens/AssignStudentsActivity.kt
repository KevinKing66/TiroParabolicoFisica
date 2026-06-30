package com.king.kevin.tiroparabolico.presentation.screens

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.king.kevin.tiroparabolico.PhysicsLabApplication
import com.king.kevin.tiroparabolico.databinding.ActivityAssignmentBinding
import com.king.kevin.tiroparabolico.presentation.viewmodel.AssignmentViewModel
import kotlinx.coroutines.launch

class AssignStudentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAssignmentBinding
    private lateinit var viewModel: AssignmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val app = application as PhysicsLabApplication
        viewModel = app.createAssignmentViewModel()
        
        observeState()
        setupActions()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.assignmentPanel.visibility = if (state.canManage) View.VISIBLE else View.GONE
                
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
        binding.btnAssign.setOnClickListener {
            val course = binding.etCourseCode.text.toString()
            val student = binding.etStudentCode.text.toString()
            viewModel.assign(course, student)
        }
    }
}
