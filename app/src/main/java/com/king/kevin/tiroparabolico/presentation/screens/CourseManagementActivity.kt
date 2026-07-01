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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.kevin.tiroparabolico.databinding.ItemCourseBinding
import com.king.kevin.tiroparabolico.domain.model.Course

class CourseManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCourseManagementBinding
    private lateinit var viewModel: CourseViewModel
    private val courseAdapter = CourseAdapter(
        onEdit = { viewModel.selectCourseForEdit(it) },
        onDelete = { viewModel.deleteCourse(it.code) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as PhysicsLabApplication
        viewModel = app.createCourseViewModel()

        setupRecyclerView()
        observeState()
        setupActions()
    }

    private fun setupRecyclerView() {
        binding.rvCourses.layoutManager = LinearLayoutManager(this)
        binding.rvCourses.adapter = courseAdapter
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.btnCreateCourse.isEnabled = !state.isLoading
                
                // Actualizar lista
                courseAdapter.submitList(state.courses)

                // UX: Hide management panel if not allowed
                binding.createCoursePanel.visibility = if (state.canManage) View.VISIBLE else View.GONE
                
                // Manejar modo edición
                if (state.courseToEdit != null) {
                    binding.etCourseCode.setText(state.courseToEdit.code)
                    binding.etCourseCode.isEnabled = false // No permitir cambiar el ID (Código) en edición
                    binding.etCourseName.setText(state.courseToEdit.name)
                    binding.btnCreateCourse.text = "Actualizar Curso"
                    binding.btnCancelEdit.visibility = View.VISIBLE
                } else {
                    binding.etCourseCode.isEnabled = true
                    binding.btnCreateCourse.text = "Guardar Curso"
                    binding.btnCancelEdit.visibility = View.GONE
                }

                state.errorMessage?.let { 
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    viewModel.clearMessages()
                }
                
                state.successMessage?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    if (state.courseToEdit == null) {
                        binding.etCourseCode.text?.clear()
                        binding.etCourseName.text?.clear()
                    }
                    viewModel.clearMessages()
                }
            }
        }
    }

    private fun setupActions() {
        binding.btnCreateCourse.setOnClickListener {
            val code = binding.etCourseCode.text.toString()
            val name = binding.etCourseName.text.toString()
            
            if (code.isBlank() || name.isBlank()) {
                binding.etCourseCode.error = "Obligatorio"
                return@setOnClickListener
            }
            
            viewModel.saveCourse(code, name)
        }

        binding.btnCancelEdit.setOnClickListener {
            binding.etCourseCode.text?.clear()
            binding.etCourseName.text?.clear()
            viewModel.cancelEdit()
        }
    }

    private class CourseAdapter(
        val onEdit: (Course) -> Unit,
        val onDelete: (Course) -> Unit
    ) : RecyclerView.Adapter<CourseAdapter.ViewHolder>() {
        
        private var list = emptyList<Course>()
        fun submitList(newList: List<Course>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.binding.tvCourseName.text = item.name
            holder.binding.tvCourseCode.text = "Código: ${item.code}"
            holder.binding.tvInstitution.text = item.institution
            holder.binding.btnEditCourse.setOnClickListener { onEdit(item) }
            holder.binding.btnDeleteCourse.setOnClickListener { onDelete(item) }
        }

        override fun getItemCount() = list.size
        class ViewHolder(val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
