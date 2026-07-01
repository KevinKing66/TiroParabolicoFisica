package com.king.kevin.tiroparabolico.presentation.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.kevin.tiroparabolico.MainActivity
import com.king.kevin.tiroparabolico.PhysicsLabApplication
import com.king.kevin.tiroparabolico.databinding.ActivityMenuBinding
import com.king.kevin.tiroparabolico.databinding.ItemLabStudentBinding
import com.king.kevin.tiroparabolico.domain.model.Lab
import com.king.kevin.tiroparabolico.presentation.viewmodel.MenuViewModel
import kotlinx.coroutines.launch

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private lateinit var viewModel: MenuViewModel
    private val labAdapter = LabStudentAdapter { openLabDetail(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as PhysicsLabApplication
        viewModel = app.createMenuViewModel()

        setupRecyclerView()
        setupActions()
        observeUiState()
    }

    private fun setupRecyclerView() {
        binding.rvStudentLabs.apply {
            layoutManager = LinearLayoutManager(this@MenuActivity)
            adapter = labAdapter
        }
    }

    private fun openLabDetail(lab: Lab) {
        val intent = Intent(this, LabDetailActivity::class.java).apply {
            putExtra("LAB_CODE", lab.code)
            putExtra("COURSE_CODE", lab.courseCode)
        }
        startActivity(intent)
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

                    labAdapter.submitList(state.studentLabs)
                }
            }
        }
    }

    private class LabStudentAdapter(val onDoLab: (Lab) -> Unit) : 
        RecyclerView.Adapter<LabStudentAdapter.ViewHolder>() {
        
        private var list = emptyList<Lab>()
        fun submitList(newList: List<Lab>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemLabStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.binding.tvLabName.text = item.name
            holder.binding.tvLabCode.text = "ID: ${item.code}"
            holder.binding.btnDoLab.setOnClickListener { onDoLab(item) }
            holder.binding.root.setOnClickListener { onDoLab(item) }
        }

        override fun getItemCount() = list.size
        class ViewHolder(val binding: ItemLabStudentBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
