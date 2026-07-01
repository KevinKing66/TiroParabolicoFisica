package com.king.kevin.tiroparabolico.presentation.screens

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.king.kevin.tiroparabolico.MainActivity
import com.king.kevin.tiroparabolico.PhysicsLabApplication
import com.king.kevin.tiroparabolico.databinding.ActivityMenuBinding
import com.king.kevin.tiroparabolico.presentation.viewmodel.MenuViewModel
import kotlinx.coroutines.launch

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private lateinit var viewModel: MenuViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as PhysicsLabApplication
        viewModel = app.createMenuViewModel()

        setupActions()
        observeUiState()
    }

    private fun setupActions() {
        binding.btnSimulator.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.btnManageCourses.setOnClickListener {
            startActivity(Intent(this, CourseManagementActivity::class.java))
        }

        binding.btnAssignStudents.setOnClickListener {
            startActivity(Intent(this, AssignStudentsActivity::class.java))
        }

        binding.btnManageLabs.setOnClickListener {
            startActivity(Intent(this, LabManagementActivity::class.java))
        }

        binding.btnManageInstitutions.setOnClickListener {
            startActivity(Intent(this, InstitutionManagementActivity::class.java))
        }

        binding.btnAdminCreateUser.setOnClickListener {
            startActivity(Intent(this, AdminUserCreationActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(this, AuthActivity::class.java))
            finishAffinity()
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val session = state.session ?: return@collect
                    binding.tvWelcome.text = "Hola, ${session.fullName}"
                    
                    val role = session.role.lowercase()
                    val isTeacher = role == "teacher" || role == "admin"
                    val isAdmin = role == "admin"

                    binding.teacherMenu.visibility = if (isTeacher) View.VISIBLE else View.GONE
                    binding.adminOnlyMenu.visibility = if (isAdmin) View.VISIBLE else View.GONE
                    binding.studentMenu.visibility = if (role == "student") View.VISIBLE else View.GONE
                }
            }
        }
    }
}
