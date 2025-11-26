package com.example.projeto_programaomobile_parte1.ui.register

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.projeto_programaomobile_parte1.databinding.ActivityRegisterBinding
import com.example.projeto_programaomobile_parte1.viewmodel.RegisterViewModel
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var vm: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vm = ViewModelProvider(this)[RegisterViewModel::class.java]

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnCriarConta.setOnClickListener {
            val nome = binding.inputNome.editText?.text?.toString()?.trim().orEmpty()
            val email = binding.inputEmail.editText?.text?.toString()?.trim().orEmpty()
            val senha = binding.inputSenha.editText?.text?.toString()?.trim().orEmpty()
            val confirmar = binding.inputConfirmarSenha.editText?.text?.toString()?.trim().orEmpty()
            vm.cadastrar(nome, email, senha, confirmar)
        }

        // Observar loading state
        vm.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnCriarConta.isEnabled = !isLoading
        }

        vm.erro.observe(this) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }

        vm.mensagem.observe(this) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
