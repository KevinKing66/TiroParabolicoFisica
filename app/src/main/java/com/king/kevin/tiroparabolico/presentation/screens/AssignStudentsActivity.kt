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

import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.kevin.tiroparabolico.domain.model.Course
import com.king.kevin.tiroparabolico.domain.model.UserMinimal
import com.king.kevin.tiroparabolico.databinding.ItemStudentSelectableBinding

class AssignStudentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAssignmentBinding
    private lateinit var viewModel: AssignmentViewModel
    private val studentAdapter = SelectableStudentAdapter { viewModel.toggleStudentSelection(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val app = application as PhysicsLabApplication
        viewModel = app.createAssignmentViewModel()
        
        setupRecyclerView()
        observeState()
        setupActions()
    }

    private fun setupRecyclerView() {
        binding.rvStudentSearch.apply {
            layoutManager = LinearLayoutManager(this@AssignStudentsActivity)
            adapter = studentAdapter
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.assignmentPanel.visibility = if (state.canManage) View.VISIBLE else View.GONE
                
                // Actualizar Spinner de cursos
                setupCourseSpinner(state.courses)
                
                // Actualizar lista de búsqueda
                studentAdapter.submitList(state.studentsFound, state.selectedStudents)
                
                binding.tvSelectedCount.text = "Seleccionados: ${state.selectedStudents.size}"
                
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

    private fun setupCourseSpinner(courses: List<Course>) {
        val courseLabels = courses.map { "${it.code} - ${it.name}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courseLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCourses.adapter = adapter
    }

    private fun setupActions() {
        binding.etSearchStudent.addTextChangedListener { text ->
            viewModel.searchStudents(text?.toString() ?: "")
        }

        binding.btnAssign.setOnClickListener {
            val selectedPosition = binding.spinnerCourses.selectedItemPosition
            if (selectedPosition >= 0) {
                val courseCode = viewModel.uiState.value.courses[selectedPosition].code
                viewModel.assignMultiple(courseCode)
            } else {
                Snackbar.make(binding.root, "Seleccione un curso primero", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private class SelectableStudentAdapter(val onToggle: (UserMinimal) -> Unit) : 
        RecyclerView.Adapter<SelectableStudentAdapter.ViewHolder>() {
        
        private var items: List<UserMinimal> = emptyList()
        private var selected: List<UserMinimal> = emptyList()

        fun submitList(newList: List<UserMinimal>, newSelected: List<UserMinimal>) {
            items = newList
            selected = newSelected
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemStudentSelectableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val student = items[position]
            holder.bind(student, selected.any { it.code == student.code })
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(private val binding: ItemStudentSelectableBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(student: UserMinimal, isSelected: Boolean) {
                binding.tvStudentName.text = student.fullName
                binding.tvStudentCode.text = student.code
                binding.cbSelected.setOnCheckedChangeListener(null)
                binding.cbSelected.isChecked = isSelected
                binding.cbSelected.setOnCheckedChangeListener { _, _ -> onToggle(student) }
                binding.root.setOnClickListener { onToggle(student) }
            }
        }
    }
}
