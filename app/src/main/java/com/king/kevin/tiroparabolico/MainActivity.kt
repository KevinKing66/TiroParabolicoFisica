package com.king.kevin.tiroparabolico

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.king.kevin.tiroparabolico.core.extensions.toDisplay
import com.king.kevin.tiroparabolico.core.extensions.toNullableDouble
import com.king.kevin.tiroparabolico.databinding.ActivityMainBinding
import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment
import com.king.kevin.tiroparabolico.presentation.screens.AnalysisActivity
import com.king.kevin.tiroparabolico.presentation.screens.ChallengesActivity
import com.king.kevin.tiroparabolico.presentation.screens.ExperimentHistoryAdapter
import com.king.kevin.tiroparabolico.presentation.state.ExperimentUiState
import com.king.kevin.tiroparabolico.presentation.viewmodel.ExperimentViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ExperimentViewModel
    private val historyAdapter = ExperimentHistoryAdapter { viewModel.selectExperiment(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicializar ViewModel usando el contenedor de dependencias
        val appContainer = (application as PhysicsLabApplication).appContainer
        viewModel = appContainer.createExperimentViewModel()
        
        setupInsets()
        setupHistory()
        setupActions()
        observeUiState()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupHistory() {
        binding.historyRecycler.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = historyAdapter
        }
    }

    private fun setupActions() {
        binding.simulateButton.setOnClickListener {
            clearInputErrors()
            val velocity = binding.velocityInput.text?.toString().orEmpty().toNullableDouble()
            val angle = binding.angleInput.text?.toString().orEmpty().toNullableDouble()
            val gravityText = binding.gravityInput.text?.toString().orEmpty()
            val gravity = gravityText.toNullableDouble()
            markMissingFields(velocity, angle)
            if (gravityText.isNotBlank() && gravity == null) {
                binding.gravityInputLayout.error = "Ingresa un numero valido"
                return@setOnClickListener
            }
            viewModel.simulate(velocity, angle, gravity)
        }
        binding.saveButton.setOnClickListener { viewModel.saveCurrentExperiment() }
        
        binding.btnAnalysis.setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
        }
        
        binding.btnChallenges.setOnClickListener {
            startActivity(Intent(this, ChallengesActivity::class.java))
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: ExperimentUiState) {
        binding.saveButton.isEnabled = !state.isSaving
        renderExperiment(state.currentExperiment)
        historyAdapter.submitList(state.history)
        binding.historyEmptyText.visibility = if (state.history.isEmpty()) View.VISIBLE else View.GONE
        state.errorMessage?.let { showMessage(it) }
        state.successMessage?.let { showMessage(it) }
        if (state.errorMessage != null || state.successMessage != null) {
            viewModel.clearMessages()
        }
    }

    private fun renderExperiment(experiment: ProjectileExperiment?) {
        if (experiment == null) return

        binding.resultsText.text = buildString {
            appendLine("Vx = ${experiment.horizontalVelocity.toDisplay()} m/s")
            appendLine("Vy = ${experiment.verticalVelocity.toDisplay()} m/s")
            appendLine("tiempo (t) = ${experiment.flightTime.toDisplay()} s")
            appendLine("Altura máxima (h) = ${experiment.maxHeight.toDisplay()} m")
            appendLine("Alcance horizontal (R) = ${experiment.horizontalRange.toDisplay()} m")
            append("g = ${experiment.gravity.toDisplay()} m/s2")
        }
        binding.trajectoryView.submitTrajectory(experiment.trajectory)
    }

    private fun markMissingFields(velocity: Double?, angle: Double?) {
        if (velocity == null) {
            binding.velocityInputLayout.error = "Campo obligatorio"
        }
        if (angle == null) {
            binding.angleInputLayout.error = "Campo obligatorio"
        }
    }

    private fun clearInputErrors() {
        binding.velocityInputLayout.error = null
        binding.angleInputLayout.error = null
        binding.gravityInputLayout.error = null
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
