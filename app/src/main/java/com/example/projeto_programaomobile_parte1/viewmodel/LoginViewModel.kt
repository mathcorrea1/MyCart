package com.example.projeto_programaomobile_parte1.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_programaomobile_parte1.di.ServiceLocator
import com.example.projeto_programaomobile_parte1.domain.validarEmail
import com.example.projeto_programaomobile_parte1.util.Result
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val auth = ServiceLocator.authRepository()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _erro = MutableLiveData<String?>()
    val erro: LiveData<String?> = _erro

    private val _sucesso = MutableLiveData<Boolean>()
    val sucesso: LiveData<Boolean> = _sucesso

    private val _mensagem = MutableLiveData<String?>()
    val mensagem: LiveData<String?> = _mensagem

    fun efetuarLogin(email: String, senha: String) {
        _erro.value = null

        // Validações locais
        if (!validarEmail(email)) {
            _erro.value = "E-mail inválido"
            return
        }
        if (senha.isBlank()) {
            _erro.value = "Senha obrigatória"
            return
        }

        // Chamar Firebase de forma assíncrona
        _loading.value = true
        viewModelScope.launch {
            when (val result = auth.efetuarLogin(email, senha)) {
                is Result.Success -> {
                    _loading.value = false
                    _sucesso.value = true
                }
                is Result.Error -> {
                    _loading.value = false
                    _erro.value = result.message ?: "Erro ao fazer login"
                }
                is Result.Loading -> {
                    // Já está em loading
                }
            }
        }
    }

    fun recuperarSenha(email: String) {
        _mensagem.value = null
        _erro.value = null

        if (!validarEmail(email)) {
            _erro.value = "E-mail inválido"
            return
        }

        _loading.value = true
        viewModelScope.launch {
            when (val result = auth.enviarEmailRecuperacaoSenha(email)) {
                is Result.Success -> {
                    _loading.value = false
                    _mensagem.value = "Email de recuperação enviado! Verifique sua caixa de entrada."
                }
                is Result.Error -> {
                    _loading.value = false
                    _erro.value = result.message ?: "Erro ao enviar email"
                }
                is Result.Loading -> {
                    // Já está em loading
                }
            }
        }
    }
}
