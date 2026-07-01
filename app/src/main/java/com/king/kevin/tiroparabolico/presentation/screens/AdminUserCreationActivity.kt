package com.king.kevin.tiroparabolico.presentation.screens

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.king.kevin.tiroparabolico.PhysicsLabApplication
import com.king.kevin.tiroparabolico.databinding.ActivityAdminUserCreationBinding
import com.king.kevin.tiroparabolico.domain.model.Institution
import com.king.kevin.tiroparabolico.presentation.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

class AdminUserCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminUserCreationBinding
    private lateinit var viewModel: AdminViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUserCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as PhysicsLabApplication
        viewModel = app.createAdminViewModel()

        setupSpinners()
        setupActions()
        observeState()
    }

    private fun setupSpinners() {
        val roles = listOf("Student", "Teacher", "Admin")
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUserRole.adapter = roleAdapter
    }

    private fun setupActions() {
        binding.btnCreateUser.setOnClickListener {
            val name = binding.etUserFullName.text.toString()
            val email = binding.etUserEmail.text.toString()
            val pass = binding.etUserPass.text.toString()
            val role = binding.spinnerUserRole.selectedItem.toString()
            val instPos = binding.spinnerUserInst.selectedItemPosition
            
            if (name.isBlank() || email.isBlank() || pass.isBlank() || instPos < 0) {
                Snackbar.make(binding.root, "Complete todos los campos", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val institution = viewModel.uiState.value.institutions[instPos].name
            viewModel.createUser(name, email, pass, institution, role)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.adminUserProgress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.btnCreateUser.isEnabled = !state.isLoading
                
                // UX: Only populate spinner once or when list size actually changes
                if (state.institutions.isNotEmpty() && binding.spinnerUserInst.adapter == null) {
                    updateInstitutionSpinner(state.institutions)
                }
                
                state.errorMessage?.let { 
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    viewModel.clearMessages()
                }
                state.successMessage?.let { 
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    clearInputs()
                    viewModel.clearMessages()
                }
            }
        }
    }

    private fun updateInstitutionSpinner(institutions: List<Institution>) {
        val names = institutions.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUserInst.adapter = adapter
    }

    private fun clearInputs() {
        binding.etUserFullName.text?.clear()
        binding.etUserEmail.text?.clear()
        binding.etUserPass.text?.clear()
    }
}
