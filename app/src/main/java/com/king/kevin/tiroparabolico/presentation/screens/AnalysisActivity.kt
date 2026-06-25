package com.king.kevin.tiroparabolico.presentation.screens

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.king.kevin.tiroparabolico.PhysicsLabApplication
import com.king.kevin.tiroparabolico.databinding.ActivityAnalysisBinding
import com.king.kevin.tiroparabolico.domain.model.AcademicResponse
import com.king.kevin.tiroparabolico.domain.model.AcademicType
import com.king.kevin.tiroparabolico.presentation.viewmodel.AcademicViewModel
import kotlinx.coroutines.launch

class AnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalysisBinding
    private lateinit var viewModel: AcademicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as PhysicsLabApplication
        viewModel = app.createAcademicViewModel()

        setupInsets()
        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val answers = mapOf(
                "q1" to binding.editQ1.text.toString().trim(),
                "q2" to binding.editQ2.text.toString().trim(),
                "q3" to binding.editQ3.text.toString().trim(),
                "q4" to binding.editQ4.text.toString().trim(),
                "q5" to binding.editQ5.text.toString().trim()
            )

            if (answers.values.any { it.isEmpty() }) {
                Toast.makeText(this, "Por favor, completa todas las preguntas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val response = AcademicResponse(
                type = AcademicType.ANALYSIS,
                answers = answers
            )
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false
            viewModel.saveResponse(response)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { result ->
                    result?.let {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSave.isEnabled = true
                        
                        if (it.isSuccess) {
                            Toast.makeText(this@AnalysisActivity, "Respuestas guardadas correctamente", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@AnalysisActivity, "Error al guardar: ${it.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                        viewModel.resetSaveState()
                    }
                }
            }
        }
    }
}
