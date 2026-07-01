package com.king.kevin.tiroparabolico.presentation.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.king.kevin.tiroparabolico.MainActivity
import com.king.kevin.tiroparabolico.PhysicsLabApplication
import com.king.kevin.tiroparabolico.databinding.ActivityLabDetailBinding
import com.king.kevin.tiroparabolico.databinding.ItemLabQuestionBinding
import com.king.kevin.tiroparabolico.databinding.ItemLabSectionBinding
import com.king.kevin.tiroparabolico.domain.model.AcademicResponse
import com.king.kevin.tiroparabolico.domain.model.Lab
import com.king.kevin.tiroparabolico.domain.model.Question
import com.king.kevin.tiroparabolico.domain.model.QuestionSection
import com.king.kevin.tiroparabolico.presentation.viewmodel.LabViewModel
import kotlinx.coroutines.launch

class LabDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLabDetailBinding
    private lateinit var viewModel: LabViewModel
    private var currentLab: Lab? = null
    
    private val sectionAdapter = SectionAdapter { section, answers ->
        submitAnswers(section, answers)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as PhysicsLabApplication
        viewModel = app.createLabViewModel()

        setupRecyclerView()
        setupActions()
        observeState()

        val labCode = intent.getStringExtra("LAB_CODE") ?: ""
        val courseCode = intent.getStringExtra("COURSE_CODE") ?: ""
        
        loadLab(labCode, courseCode)
        
        val session = app.authRepository.getCurrentSession()
        if (session != null) {
            viewModel.observeStudentResponsesForLab(session.code, labCode)
        }
    }

    private fun setupRecyclerView() {
        binding.rvSections.layoutManager = LinearLayoutManager(this)
        binding.rvSections.adapter = sectionAdapter
    }

    private fun setupActions() {
        binding.btnOpenSimulator.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                sectionAdapter.updateStudentResponses(state.labResponses)
                
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

    private fun loadLab(labCode: String, courseCode: String) {
        lifecycleScope.launch {
            val app = application as PhysicsLabApplication
            app.labRepository.getLabsByCourse(courseCode).onSuccess { labs ->
                val lab = labs.find { it.code == labCode }
                if (lab != null) {
                    renderLab(lab)
                }
            }
        }
    }

    private fun renderLab(lab: Lab) {
        currentLab = lab
        binding.tvLabTitle.text = lab.name
        binding.tvLabDescription.text = lab.description
        binding.tvLabExercise.text = lab.exercise
        sectionAdapter.submitList(lab.sections)
    }

    private fun submitAnswers(section: QuestionSection, answers: Map<String, String>) {
        if (answers.values.all { it.isBlank() }) {
            Snackbar.make(binding.root, "Responde al menos una pregunta", Snackbar.LENGTH_SHORT).show()
            return
        }
        val labId = currentLab?.code ?: return
        viewModel.submitSectionAnswers(labId, section.id, answers)
    }

    private class SectionAdapter(val onSubmit: (QuestionSection, Map<String, String>) -> Unit) : 
        RecyclerView.Adapter<SectionAdapter.ViewHolder>() {
        
        private var sections: List<QuestionSection> = emptyList()
        private var studentResponses: List<AcademicResponse> = emptyList()

        fun submitList(newList: List<QuestionSection>) {
            sections = newList
            notifyDataSetChanged()
        }

        fun updateStudentResponses(responses: List<AcademicResponse>) {
            studentResponses = responses
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemLabSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val section = sections[position]
            val existingResponse = studentResponses.find { it.sectionId == section.id }
            holder.bind(section, existingResponse)
        }

        override fun getItemCount() = sections.size

        inner class ViewHolder(private val binding: ItemLabSectionBinding) : RecyclerView.ViewHolder(binding.root) {
            private val questionAdapter = QuestionAdapter()

            init {
                binding.rvQuestions.layoutManager = LinearLayoutManager(binding.root.context)
                binding.rvQuestions.adapter = questionAdapter
            }

            fun bind(section: QuestionSection, response: AcademicResponse?) {
                binding.tvSectionTitle.text = section.title
                questionAdapter.submitList(section.questions, response?.answers ?: emptyMap())
                
                if (response != null) {
                    binding.btnSubmitSection.text = "Sección Completada"
                    binding.btnSubmitSection.isEnabled = false
                } else {
                    binding.btnSubmitSection.text = "Responder Sección"
                    binding.btnSubmitSection.isEnabled = true
                    binding.btnSubmitSection.setOnClickListener {
                        onSubmit(section, questionAdapter.getAnswers())
                    }
                }
            }
        }
    }

    private class QuestionAdapter : RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {
        private var questions: List<Question> = emptyList()
        private var existingAnswers: Map<String, String> = emptyMap()
        private val currentAnswers = mutableMapOf<String, String>()

        fun submitList(newList: List<Question>, answers: Map<String, String>) {
            questions = newList
            existingAnswers = answers
            notifyDataSetChanged()
        }

        fun getAnswers(): Map<String, String> = currentAnswers

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemLabQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val q = questions[position]
            holder.bind(q, existingAnswers[q.id])
        }

        override fun getItemCount() = questions.size

        inner class ViewHolder(private val binding: ItemLabQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(question: Question, existingAnswer: String?) {
                binding.tvQuestionText.text = question.text
                binding.etAnswer.setText(existingAnswer ?: "")
                binding.etAnswer.isEnabled = existingAnswer == null
                
                binding.etAnswer.addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        currentAnswers[question.id] = s?.toString() ?: ""
                    }
                    override fun afterTextChanged(s: android.text.Editable?) {}
                })
            }
        }
    }
}
