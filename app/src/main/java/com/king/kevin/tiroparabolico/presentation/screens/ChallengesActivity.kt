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
import com.king.kevin.tiroparabolico.databinding.ActivityChallengesBinding
import com.king.kevin.tiroparabolico.domain.model.AcademicResponse
import com.king.kevin.tiroparabolico.domain.model.AcademicType
import com.king.kevin.tiroparabolico.presentation.viewmodel.AcademicViewModel
import kotlinx.coroutines.launch

class ChallengesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChallengesBinding
    private lateinit var viewModel: AcademicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChallengesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appContainer = (application as PhysicsLabApplication).appContainer
        viewModel = appContainer.createAcademicViewModel()

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
                "c1" to binding.editC1.text.toString().trim(),
                "c2" to binding.editC2.text.toString().trim(),
                "c3" to binding.editC3.text.toString().trim()
            )

            if (answers.values.any { it.isEmpty() }) {
                Toast.makeText(this, "Por favor, completa todos los retos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val response = AcademicResponse(
                type = AcademicType.CHALLENGES,
                answers = answers
            )
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
                            Toast.makeText(this@ChallengesActivity, "Retos guardados correctamente", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@ChallengesActivity, "Error al guardar: ${it.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                        viewModel.resetSaveState()
                    }
                }
            }
        }
    }
}
