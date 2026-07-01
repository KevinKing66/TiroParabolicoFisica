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
import com.king.kevin.tiroparabolico.presentation.viewmodel.LabViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LabResponsesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLabResponsesBinding
    private lateinit var viewModel: LabViewModel
    private val adapter = ResponseAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabResponsesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as PhysicsLabApplication
        viewModel = app.createLabViewModel()

        val labCode = intent.getStringExtra("LAB_CODE") ?: ""
        val labName = intent.getStringExtra("LAB_NAME") ?: "Laboratorio"
        binding.tvLabResponsesTitle.text = "Respuestas: $labName"

        setupRecyclerView()
        observeState()
        
        viewModel.observeLabResponses(labCode)
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
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun submitList(newList: List<AcademicResponse>) {
            list = newList
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
            
            holder.binding.tvAnswers.text = item.answers.entries.joinToString("\n") { 
                "${it.key}: ${it.value}" 
            }
        }

        override fun getItemCount() = list.size
        class ViewHolder(val binding: ItemLabResponseBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
