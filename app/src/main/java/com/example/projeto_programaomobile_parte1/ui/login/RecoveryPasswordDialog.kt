package com.example.projeto_programaomobile_parte1.ui.login

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.example.projeto_programaomobile_parte1.databinding.DialogRecoveryPasswordBinding
import com.example.projeto_programaomobile_parte1.domain.validarEmail
import com.google.android.material.snackbar.Snackbar

/**
 * Dialog para recuperação de senha
 */
class RecoveryPasswordDialog(
    context: Context,
    private val onEnviar: (String) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogRecoveryPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogRecoveryPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCancelar.setOnClickListener {
            dismiss()
        }

        binding.btnEnviar.setOnClickListener {
            val email = binding.tilEmail.editText?.text?.toString()?.trim().orEmpty()

            if (!validarEmail(email)) {
                binding.tilEmail.error = "E-mail inválido"
                return@setOnClickListener
            }

            binding.tilEmail.error = null
            onEnviar(email)
            dismiss()
        }
    }
}

