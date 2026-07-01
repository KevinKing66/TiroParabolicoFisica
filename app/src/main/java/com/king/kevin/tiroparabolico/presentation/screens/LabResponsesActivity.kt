package com.king.kevin.tiroparabolico.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.kevin.tiroparabolico.PhysicsLabApplication
import com.king.kevin.tiroparabolico.core.extensions.toDisplay
import com.king.kevin.tiroparabolico.databinding.ActivityLabResponsesBinding
import com.king.kevin.tiroparabolico.databinding.ItemLabResponseBinding
import com.king.kevin.tiroparabolico.domain.model.AcademicResponse
import com.king.kevin.tiroparabolico.domain.model.Lab
import com.king.kevin.tiroparabolico.presentation.viewmodel.LabViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LabResponsesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLabResponsesBinding
    private lateinit var viewModel: LabViewModel
    private val adapter = ResponseAdapter()
    private var currentLab: Lab? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabResponsesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as PhysicsLabApplication
        viewModel = app.createLabViewModel()

        val labCode = intent.getStringExtra("LAB_CODE") ?: ""
        val labName = intent.getStringExtra("LAB_NAME") ?: "Laboratorio"
        val courseCode = intent.getStringExtra("COURSE_CODE") ?: ""
        
        binding.tvLabResponsesTitle.text = "Respuestas: $labName"

        setupRecyclerView()
        observeState()
        
        loadLabDetails(labCode, courseCode)
        viewModel.observeLabResponses(labCode)
    }

    private fun loadLabDetails(labCode: String, courseCode: String) {
        lifecycleScope.launch {
            val app = application as PhysicsLabApplication
            app.labRepository.getLabsByCourse(courseCode).onSuccess { labs ->
                currentLab = labs.find { it.code == labCode }
                currentLab?.let { adapter.setLabDefinition(it) }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvLabResponses.layoutManager = LinearLayoutManager(this)
        binding.rvLabResponses.adapter = adapter
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                adapter.submitList(state.labResponses)
            }
        }
    }

    private class ResponseAdapter : RecyclerView.Adapter<ResponseAdapter.ViewHolder>() {
        private var list = emptyList<AcademicResponse>()
        private var labDefinition: Lab? = null
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun submitList(newList: List<AcademicResponse>) {
            list = newList
            notifyDataSetChanged()
        }

        fun setLabDefinition(lab: Lab) {
            labDefinition = lab
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemLabResponseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.binding.tvStudentUserCode.text = "Estudiante: ${item.userCode}"
            holder.binding.tvDate.text = dateFormat.format(Date(item.createdAtMillis))
            
            // Map IDs to texts using labDefinition
            val displayAnswers = item.answers.entries.map { (qId, answer) ->
                val questionText = findQuestionText(item.sectionId, qId)
                "P: $questionText\nR: $answer"
            }.joinToString("\n\n")
            
            holder.binding.tvAnswers.text = displayAnswers
        }

        private fun findQuestionText(sectionId: String, questionId: String): String {
            val section = labDefinition?.sections?.find { it.id == sectionId }
            val question = section?.questions?.find { it.id == questionId }
            return question?.text ?: "Pregunta no encontrada ($questionId)"
        }

        override fun getItemCount() = list.size
        class ViewHolder(val binding: ItemLabResponseBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
