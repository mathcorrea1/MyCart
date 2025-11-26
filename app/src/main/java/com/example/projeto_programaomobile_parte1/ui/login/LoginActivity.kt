package com.example.projeto_programaomobile_parte1.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.projeto_programaomobile_parte1.databinding.ActivityLoginBinding
import com.example.projeto_programaomobile_parte1.di.ServiceLocator
import com.example.projeto_programaomobile_parte1.ui.listas.ListsActivity
import com.example.projeto_programaomobile_parte1.ui.register.RegisterActivity
import com.example.projeto_programaomobile_parte1.viewmodel.LoginViewModel
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var vm: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar se usuário já está logado
        val auth = ServiceLocator.authRepository()
        if (auth.isLoggedIn()) {
            irParaListas()
            return
        }

        vm = ViewModelProvider(this)[LoginViewModel::class.java]

        binding.btnEntrar.setOnClickListener {
            val email = binding.inputEmail.editText?.text?.toString()?.trim().orEmpty()
            val senha = binding.inputSenha.editText?.text?.toString()?.trim().orEmpty()
            vm.efetuarLogin(email, senha)
        }

        binding.linkCadastrar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Adicionar link para recuperação de senha
        binding.linkRecuperarSenha.setOnClickListener {
            mostrarDialogRecuperarSenha()
        }

        // Observar estados
        vm.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnEntrar.isEnabled = !isLoading
            binding.linkCadastrar.isEnabled = !isLoading
        }

        vm.erro.observe(this) { msg ->
            msg?.let {
                binding.inputEmail.error = null
                binding.inputSenha.error = null
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }

        vm.mensagem.observe(this) { msg ->
            msg?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        }

        vm.sucesso.observe(this) { ok ->
            if (ok == true) {
                irParaListas()
            }
        }
    }

    private fun mostrarDialogRecuperarSenha() {
        val dialog = RecoveryPasswordDialog(this) { email ->
            vm.recuperarSenha(email)
        }
        dialog.show()
    }

    private fun irParaListas() {
        val intent = Intent(this, ListsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}

private fun String?.orElse(alt: String) = this ?: alt
