package com.king.kevin.tiroparabolico.presentation.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.king.kevin.tiroparabolico.PhysicsLabApplication
import com.king.kevin.tiroparabolico.databinding.ActivityAssignmentBinding
import com.king.kevin.tiroparabolico.databinding.ItemStudentSelectableBinding
import com.king.kevin.tiroparabolico.domain.model.Course
import com.king.kevin.tiroparabolico.domain.model.UserMinimal
import com.king.kevin.tiroparabolico.presentation.viewmodel.AssignmentViewModel
import kotlinx.coroutines.launch

class AssignStudentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAssignmentBinding
    private lateinit var viewModel: AssignmentViewModel
    
    private val searchAdapter by lazy {
        SelectableStudentAdapter(
            onToggle = { viewModel.toggleStudentSelection(it) },
            onRemove = { /* Not used here */ }
        )
    }
    
    private val enrolledAdapter by lazy {
        SelectableStudentAdapter(
            onToggle = { /* Not used here */ },
            onRemove = { viewModel.removeFromCourse(it.code) }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val app = application as PhysicsLabApplication
        viewModel = app.createAssignmentViewModel()
        
        setupRecyclerViews()
        observeState()
        setupActions()
    }

    private fun setupRecyclerViews() {
        binding.rvStudentSearch.apply {
            layoutManager = LinearLayoutManager(this@AssignStudentsActivity)
            adapter = searchAdapter
        }
        binding.rvEnrolledStudents.apply {
            layoutManager = LinearLayoutManager(this@AssignStudentsActivity)
            adapter = enrolledAdapter
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.assignmentPanel.visibility = if (state.canManage) View.VISIBLE else View.GONE
                
                setupCourseSpinner(state.courses)
                binding.tvNoCourses.visibility = if (state.courses.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE
                
                searchAdapter.submitList(
                    newList = state.studentsFound, 
                    newSelected = state.selectedStudents,
                    enrolledCodes = state.enrolledStudents.map { it.code }
                )

                enrolledAdapter.submitList(
                    newList = state.enrolledStudents,
                    newSelected = emptyList(),
                    enrolledCodes = state.enrolledStudents.map { it.code },
                    isReadOnly = true
                )
                
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
        
        // Obtenemos los datos actuales del adapter para comparar
        val currentAdapter = binding.spinnerCourses.adapter as? ArrayAdapter<String>
        val currentData = if (currentAdapter != null) {
            (0 until currentAdapter.count).map { currentAdapter.getItem(it)!! }
        } else null

        // Solo actualizamos si los datos son diferentes para evitar loops de selección
        if (currentData != courseLabels) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courseLabels)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCourses.adapter = adapter

            binding.spinnerCourses.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position >= 0 && position < courses.size) {
                        val courseCode = courses[position].code
                        if (courseCode != viewModel.uiState.value.selectedCourseCode) {
                            viewModel.selectCourse(courseCode)
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            
            // Si ya había un curso seleccionado en el ViewModel, intentamos mantener la selección visual
            val selectedCode = viewModel.uiState.value.selectedCourseCode
            if (selectedCode != null) {
                val index = courses.indexOfFirst { it.code == selectedCode }
                if (index >= 0) binding.spinnerCourses.setSelection(index)
            }
        }
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

    private class SelectableStudentAdapter(
        val onToggle: (UserMinimal) -> Unit,
        val onRemove: (UserMinimal) -> Unit
    ) : 
        RecyclerView.Adapter<SelectableStudentAdapter.ViewHolder>() {
        
        private var items: List<UserMinimal> = emptyList()
        private var selected: List<UserMinimal> = emptyList()
        private var enrolled: List<String> = emptyList()
        private var isReadOnly: Boolean = false

        fun submitList(
            newList: List<UserMinimal>, 
            newSelected: List<UserMinimal>, 
            enrolledCodes: List<String>,
            isReadOnly: Boolean = false
        ) {
            items = newList
            selected = newSelected
            enrolled = enrolledCodes
            this.isReadOnly = isReadOnly
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemStudentSelectableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val student = items[position]
            holder.bind(
                student = student, 
                isSelected = selected.any { it.code == student.code },
                isAlreadyEnrolled = enrolled.contains(student.code)
            )
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(private val binding: ItemStudentSelectableBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(student: UserMinimal, isSelected: Boolean, isAlreadyEnrolled: Boolean) {
                binding.tvStudentName.text = student.fullName
                binding.tvStudentCode.text = student.code
                
                binding.tvEnrollmentStatus.visibility = if (isAlreadyEnrolled && !isReadOnly) View.VISIBLE else View.GONE
                
                if (isReadOnly) {
                    // Item in enrolled list
                    binding.cbSelected.visibility = View.GONE
                    binding.btnRemoveStudent.visibility = View.VISIBLE
                    binding.btnRemoveStudent.setOnClickListener { onRemove(student) }
                    binding.root.setOnClickListener(null)
                } else if (isAlreadyEnrolled) {
                    // Item in search results, already enrolled
                    binding.cbSelected.visibility = View.GONE
                    binding.btnRemoveStudent.visibility = View.GONE
                    binding.root.setOnClickListener(null)
                } else {
                    // Item in search results, not enrolled
                    binding.cbSelected.visibility = View.VISIBLE
                    binding.btnRemoveStudent.visibility = View.GONE
                    binding.cbSelected.setOnCheckedChangeListener(null)
                    binding.cbSelected.isChecked = isSelected
                    binding.cbSelected.setOnCheckedChangeListener { _, _ -> onToggle(student) }
                    binding.root.setOnClickListener { onToggle(student) }
                }
            }
        }
    }
}
