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
        // Logic to submit answers for a section
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
        
        // We need a way to load a single lab
        loadLab(labCode, courseCode)
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
                // Handle errors
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
        // This logic should be in ViewModel/Repository
        // For now, let's assume ViewModel can provide labs by course
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
        // Here we would call the ViewModel to save answers
        Snackbar.make(binding.root, "Respuestas de ${section.title} enviadas", Snackbar.LENGTH_SHORT).show()
    }

    private class SectionAdapter(val onSubmit: (QuestionSection, Map<String, String>) -> Unit) : 
        RecyclerView.Adapter<SectionAdapter.ViewHolder>() {
        
        private var sections: List<QuestionSection> = emptyList()

        fun submitList(newList: List<QuestionSection>) {
            sections = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemLabSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(sections[position])
        }

        override fun getItemCount() = sections.size

        inner class ViewHolder(private val binding: ItemLabSectionBinding) : RecyclerView.ViewHolder(binding.root) {
            private val questionAdapter = QuestionAdapter()

            init {
                binding.rvQuestions.layoutManager = LinearLayoutManager(binding.root.context)
                binding.rvQuestions.adapter = questionAdapter
            }

            fun bind(section: QuestionSection) {
                binding.tvSectionTitle.text = section.title
                questionAdapter.submitList(section.questions)
                
                binding.btnSubmitSection.setOnClickListener {
                    onSubmit(section, questionAdapter.getAnswers())
                }
            }
        }
    }

    private class QuestionAdapter : RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {
        private var questions: List<Question> = emptyList()
        private val answers = mutableMapOf<String, String>()

        fun submitList(newList: List<Question>) {
            questions = newList
            notifyDataSetChanged()
        }

        fun getAnswers(): Map<String, String> = answers

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemLabQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(questions[position])
        }

        override fun getItemCount() = questions.size

        inner class ViewHolder(private val binding: ItemLabQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(question: Question) {
                binding.tvQuestionText.text = question.text
                binding.etAnswer.addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        answers[question.id] = s?.toString() ?: ""
                    }
                    override fun afterTextChanged(s: android.text.Editable?) {}
                })
            }
        }
    }
}
