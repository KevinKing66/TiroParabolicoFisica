package com.king.kevin.tiroparabolico.presentation.screens

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
import com.google.android.material.snackbar.Snackbar
import com.king.kevin.tiroparabolico.MainActivity
import com.king.kevin.tiroparabolico.PhysicsLabApplication
import com.king.kevin.tiroparabolico.databinding.ActivityAuthBinding
import com.king.kevin.tiroparabolico.core.extensions.setupSystemInsets
import com.king.kevin.tiroparabolico.presentation.state.AuthMode
import com.king.kevin.tiroparabolico.presentation.state.AuthUiState
import com.king.kevin.tiroparabolico.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var viewModel: AuthViewModel
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupSystemInsets(binding.authRoot)
        
        val app = application as PhysicsLabApplication
        viewModel = app.createAuthViewModel()
        
        setupActions()
        observeUiState()
    }

    private fun setupActions() {
        binding.authModeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = if (checkedId == binding.loginModeButton.id) AuthMode.LOGIN else AuthMode.REGISTER
            viewModel.setMode(mode)
        }

        binding.authActionButton.setOnClickListener {
            clearInputErrors()
            val state = viewModel.uiState.value
            if (state.mode == AuthMode.LOGIN) {
                validateLoginFields()
                viewModel.login(
                    email = binding.emailInput.text?.toString().orEmpty(),
                    password = binding.passwordInput.text?.toString().orEmpty()
                )
            } else {
                validateRegisterFields()
                viewModel.register(
                    fullName = binding.fullNameInput.text?.toString().orEmpty(),
                    email = binding.emailInput.text?.toString().orEmpty(),
                    password = binding.passwordInput.text?.toString().orEmpty(),
                    institutionName = binding.institutionInput.text?.toString().orEmpty(),
                    courseCode = binding.courseCodeInput.text?.toString().orEmpty()
                )
            }
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: AuthUiState) {
        val isRegister = state.mode == AuthMode.REGISTER
        binding.registerFieldsGroup.visibility = if (isRegister) View.VISIBLE else View.GONE
        binding.registerExtraFieldsGroup.visibility = if (isRegister) View.VISIBLE else View.GONE
        binding.authActionButton.text = if (isRegister) "Crear cuenta" else "Ingresar"
        binding.authActionButton.isEnabled = !state.isLoading
        binding.authProgress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.loginModeButton.isChecked = state.mode == AuthMode.LOGIN
        binding.registerModeButton.isChecked = state.mode == AuthMode.REGISTER

        state.errorMessage?.let { showMessage(it) }
        state.successMessage?.let { showMessage(it) }
        if (state.errorMessage != null || state.successMessage != null) {
            viewModel.clearMessages()
        }
        if (state.session != null && !hasNavigated) {
            hasNavigated = true
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
    }

    private fun validateLoginFields() {
        if (binding.emailInput.text.isNullOrBlank()) binding.emailInputLayout.error = "Campo obligatorio"
        if (binding.passwordInput.text.isNullOrBlank()) binding.passwordInputLayout.error = "Campo obligatorio"
    }

    private fun validateRegisterFields() {
        validateLoginFields()
        if (binding.fullNameInput.text.isNullOrBlank()) binding.fullNameInputLayout.error = "Campo obligatorio"
        if (binding.institutionInput.text.isNullOrBlank()) binding.institutionInputLayout.error = "Campo obligatorio"
    }

    private fun clearInputErrors() {
        binding.fullNameInputLayout.error = null
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
        binding.institutionInputLayout.error = null
        binding.courseCodeInputLayout.error = null
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
