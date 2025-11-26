package com.example.projeto_programaomobile_parte1.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_programaomobile_parte1.di.ServiceLocator
import com.example.projeto_programaomobile_parte1.domain.validarEmail
import com.example.projeto_programaomobile_parte1.util.Result
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val auth = ServiceLocator.authRepository()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _mensagem = MutableLiveData<String?>()
    val mensagem: LiveData<String?> = _mensagem

    private val _erro = MutableLiveData<String?>()
    val erro: LiveData<String?> = _erro

    fun cadastrar(nome: String, email: String, senha: String, confirmar: String) {
        _erro.value = null

        // Validações locais
        if (nome.isBlank()) {
            _erro.value = "Nome obrigatório"
            return
        }
        if (!validarEmail(email)) {
            _erro.value = "E-mail inválido"
            return
        }
        if (senha.isBlank() || confirmar.isBlank()) {
            _erro.value = "Senha obrigatória"
            return
        }
        if (senha != confirmar) {
            _erro.value = "As senhas não conferem"
            return
        }
        if (senha.length < 6) {
            _erro.value = "A senha deve ter pelo menos 6 caracteres"
            return
        }

        // Chamar Firebase de forma assíncrona
        _loading.value = true
        viewModelScope.launch {
            when (val result = auth.cadastrar(nome, email, senha)) {
                is Result.Success -> {
                    _loading.value = false
                    _mensagem.value = "Conta criada com sucesso! Faça login."
                }
                is Result.Error -> {
                    _loading.value = false
                    _erro.value = result.message ?: "Erro ao criar conta"
                }
                is Result.Loading -> {
                    // Já está em loading
                }
            }
        }
    }
}
