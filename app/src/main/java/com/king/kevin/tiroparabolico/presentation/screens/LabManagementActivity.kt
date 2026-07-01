package com.king.kevin.tiroparabolico.presentation.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.king.kevin.tiroparabolico.PhysicsLabApplication
import com.king.kevin.tiroparabolico.databinding.ActivityLabManagementBinding
import com.king.kevin.tiroparabolico.databinding.ItemLabManageBinding
import com.king.kevin.tiroparabolico.databinding.ItemManageQuestionBinding
import com.king.kevin.tiroparabolico.databinding.ItemManageSectionBinding
import com.king.kevin.tiroparabolico.domain.model.Course
import com.king.kevin.tiroparabolico.domain.model.Lab
import com.king.kevin.tiroparabolico.domain.model.Question
import com.king.kevin.tiroparabolico.domain.model.QuestionSection
import com.king.kevin.tiroparabolico.domain.model.QuestionType
import com.king.kevin.tiroparabolico.presentation.viewmodel.LabViewModel
import kotlinx.coroutines.launch
import java.util.UUID

class LabManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLabManagementBinding
    private lateinit var viewModel: LabViewModel
    private val sections = mutableListOf<MutableQuestionSection>()
    private lateinit var sectionAdapter: ManageSectionAdapter
    
    private val labsListAdapter = LabManageAdapter(
        onViewResponses = { openResponses(it) },
        onEdit = { populateForm(it) },
        onDelete = { viewModel.deleteLab(it.code) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sectionAdapter = ManageSectionAdapter(sections) { index ->
            sections.removeAt(index)
            sectionAdapter.notifyItemRemoved(index)
            sectionAdapter.notifyItemRangeChanged(index, sections.size)
        }
        
        val app = application as PhysicsLabApplication
        viewModel = app.createLabViewModel()
        
        setupRecyclerViews()
        observeState()
        setupActions()
    }

    private fun setupRecyclerViews() {
        binding.rvManageSections.layoutManager = LinearLayoutManager(this)
        binding.rvManageSections.adapter = sectionAdapter
        
        binding.rvLabsList.layoutManager = LinearLayoutManager(this)
        binding.rvLabsList.adapter = labsListAdapter
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.btnAddLab.isEnabled = !state.isLoading
                
                setupCourseSpinner(state.availableCourses)
                labsListAdapter.submitList(state.labs)

                // Handle edit mode UI
                if (state.labToEdit != null) {
                    binding.etLabCode.isEnabled = false
                    binding.btnAddLab.text = "Actualizar Laboratorio"
                    binding.btnCancelLabEdit.visibility = View.VISIBLE
                } else {
                    binding.etLabCode.isEnabled = true
                    binding.btnAddLab.text = "Guardar Laboratorio"
                    binding.btnCancelLabEdit.visibility = View.GONE
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

    private fun setupCourseSpinner(courses: List<Course>) {
        if (binding.spinnerCourses.adapter == null && courses.isNotEmpty()) {
            val labels = courses.map { "${it.code} - ${it.name}" }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCourses.adapter = adapter
        }
    }

    private fun setupActions() {
        binding.btnAddSection.setOnClickListener {
            sections.add(MutableQuestionSection(UUID.randomUUID().toString(), ""))
            sectionAdapter.notifyItemInserted(sections.size - 1)
        }

        binding.btnAddLab.setOnClickListener {
            val code = binding.etLabCode.text.toString()
            val name = binding.etLabName.text.toString()
            val desc = binding.etLabDescription.text.toString()
            val exec = binding.etLabExercise.text.toString()
            
            val selectedPos = binding.spinnerCourses.selectedItemPosition
            if (selectedPos < 0) {
                Snackbar.make(binding.root, "Selecciona un curso primero", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val courseCode = viewModel.uiState.value.availableCourses[selectedPos].code
            
            // Filtramos secciones y preguntas vacías para garantizar la calidad de los datos
            val domainSections = sections.mapNotNull { ms ->
                val validQuestions = ms.questions.filter { it.text.isNotBlank() }
                if (validQuestions.isNotEmpty()) {
                    QuestionSection(
                        id = ms.id,
                        title = ms.title.ifBlank { "Sin título" },
                        questions = validQuestions.map { mq ->
                            Question(mq.id, mq.text, QuestionType.TEXT)
                        }
                    )
                } else null
            }

            if (domainSections.isEmpty()) {
                Snackbar.make(binding.root, "El laboratorio debe tener al menos una sección con preguntas válidas", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            viewModel.createLab(code, name, desc, exec, courseCode, domainSections)
        }

        binding.btnCancelLabEdit.setOnClickListener {
            viewModel.cancelEdit()
            clearInputs()
        }
    }

    private fun populateForm(lab: Lab) {
        viewModel.selectLabForEdit(lab)
        binding.etLabCode.setText(lab.code)
        binding.etLabName.setText(lab.name)
        binding.etLabDescription.setText(lab.description)
        binding.etLabExercise.setText(lab.exercise)
        
        // Populate sections
        sections.clear()
        lab.sections.forEach { ds ->
            val ms = MutableQuestionSection(ds.id, ds.title)
            ds.questions.forEach { dq ->
                ms.questions.add(MutableQuestion(dq.id, dq.text))
            }
            sections.add(ms)
        }
        sectionAdapter.notifyDataSetChanged()
        
        // Select correct course in spinner
        val courseIndex = viewModel.uiState.value.availableCourses.indexOfFirst { it.code == lab.courseCode }
        if (courseIndex >= 0) binding.spinnerCourses.setSelection(courseIndex)
        
        binding.labPanel.requestFocus()
    }

    private fun openResponses(lab: Lab) {
        val intent = Intent(this, LabResponsesActivity::class.java).apply {
            putExtra("LAB_CODE", lab.code)
            putExtra("LAB_NAME", lab.name)
            putExtra("COURSE_CODE", lab.courseCode)
        }
        startActivity(intent)
    }

    private fun clearInputs() {
        binding.etLabCode.text?.clear()
        binding.etLabName.text?.clear()
        binding.etLabDescription.text?.clear()
        binding.etLabExercise.text?.clear()
        sections.clear()
        sectionAdapter.notifyDataSetChanged()
    }

    private data class MutableQuestionSection(val id: String, var title: String, val questions: MutableList<MutableQuestion> = mutableListOf())
    private data class MutableQuestion(val id: String, var text: String)

    private class ManageSectionAdapter(
        private val sections: MutableList<MutableQuestionSection>,
        private val onRemove: (Int) -> Unit
    ) : RecyclerView.Adapter<ManageSectionAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemManageSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(sections[position])
        }

        override fun getItemCount() = sections.size

        inner class ViewHolder(val binding: ItemManageSectionBinding) : RecyclerView.ViewHolder(binding.root) {
            private var currentWatcher: android.text.TextWatcher? = null

            fun bind(section: MutableQuestionSection) {
                binding.etSectionTitle.removeTextChangedListener(currentWatcher)
                binding.etSectionTitle.setText(section.title)
                
                currentWatcher = object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        section.title = s?.toString() ?: ""
                    }
                    override fun afterTextChanged(s: android.text.Editable?) {}
                }
                binding.etSectionTitle.addTextChangedListener(currentWatcher)

                val qAdapter = ManageQuestionAdapter(section.questions) { qIndex ->
                    section.questions.removeAt(qIndex)
                    // Nested notification
                }
                binding.rvManageQuestions.layoutManager = LinearLayoutManager(binding.root.context)
                binding.rvManageQuestions.adapter = qAdapter

                binding.btnAddQuestion.setOnClickListener {
                    section.questions.add(MutableQuestion(UUID.randomUUID().toString(), ""))
                    qAdapter.notifyItemInserted(section.questions.size - 1)
                }

                binding.btnRemoveSection.setOnClickListener {
                    onRemove(bindingAdapterPosition)
                }
            }
        }
    }

    private class ManageQuestionAdapter(
        private val questions: MutableList<MutableQuestion>,
        private val onRemove: (Int) -> Unit
    ) : RecyclerView.Adapter<ManageQuestionAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemManageQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(questions[position])
        }

        override fun getItemCount() = questions.size

        inner class ViewHolder(val binding: ItemManageQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
            private var currentWatcher: android.text.TextWatcher? = null

            fun bind(question: MutableQuestion) {
                binding.etQuestionText.removeTextChangedListener(currentWatcher)
                binding.etQuestionText.setText(question.text)

                currentWatcher = object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        question.text = s?.toString() ?: ""
                    }
                    override fun afterTextChanged(s: android.text.Editable?) {}
                }
                binding.etQuestionText.addTextChangedListener(currentWatcher)
                
                binding.btnRemoveQuestion.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onRemove(pos)
                        notifyItemRemoved(pos)
                        notifyItemRangeChanged(pos, questions.size)
                    }
                }
            }
        }
    }

    private class LabManageAdapter(
        val onViewResponses: (Lab) -> Unit,
        val onEdit: (Lab) -> Unit,
        val onDelete: (Lab) -> Unit
    ) : RecyclerView.Adapter<LabManageAdapter.ViewHolder>() {
        
        private var list = emptyList<Lab>()
        fun submitList(newList: List<Lab>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemLabManageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.binding.tvManageLabName.text = item.name
            holder.binding.tvManageLabInfo.text = "Código: ${item.code} | Secciones: ${item.sections.size}"
            holder.binding.btnViewResponses.setOnClickListener { onViewResponses(item) }
            holder.binding.btnEditLab.setOnClickListener { onEdit(item) }
            holder.binding.btnDeleteLab.setOnClickListener { onDelete(item) }
        }

        override fun getItemCount() = list.size
        class ViewHolder(val binding: ItemLabManageBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
