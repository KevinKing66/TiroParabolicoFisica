package com.king.kevin.tiroparabolico.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.king.kevin.tiroparabolico.PhysicsLabApplication
import com.king.kevin.tiroparabolico.databinding.ActivityInstitutionManagementBinding
import com.king.kevin.tiroparabolico.databinding.ItemInstitutionBinding
import com.king.kevin.tiroparabolico.domain.model.Institution
import com.king.kevin.tiroparabolico.presentation.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

class InstitutionManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInstitutionManagementBinding
    private lateinit var viewModel: AdminViewModel
    private val adapter = InstitutionAdapter { viewModel.deleteInstitution(it.id) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstitutionManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as PhysicsLabApplication
        viewModel = app.createAdminViewModel()

        setupRecyclerView()
        setupActions()
        observeState()
    }

    private fun setupRecyclerView() {
        binding.rvInstitutions.layoutManager = LinearLayoutManager(this)
        binding.rvInstitutions.adapter = adapter
    }

    private fun setupActions() {
        binding.btnSaveInst.setOnClickListener {
            val name = binding.etInstName.text.toString()
            val addr = binding.etInstAddress.text.toString()
            if (name.isBlank()) {
                binding.etInstName.error = "Obligatorio"
                return@setOnClickListener
            }
            viewModel.saveInstitution(name, addr)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                adapter.submitList(state.institutions)
                binding.btnSaveInst.isEnabled = !state.isLoading
                
                state.errorMessage?.let { 
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    viewModel.clearMessages()
                }
                state.successMessage?.let { 
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    binding.etInstName.text?.clear()
                    binding.etInstAddress.text?.clear()
                    viewModel.clearMessages()
                }
            }
        }
    }

    private class InstitutionAdapter(val onDelete: (Institution) -> Unit) : RecyclerView.Adapter<InstitutionAdapter.ViewHolder>() {
        private var list = emptyList<Institution>()
        fun submitList(newList: List<Institution>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemInstitutionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.binding.tvName.text = item.name
            holder.binding.tvAddress.text = item.address
            holder.binding.btnDelete.setOnClickListener { onDelete(item) }
        }

        override fun getItemCount() = list.size
        class ViewHolder(val binding: ItemInstitutionBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
