package com.example.projeto_programaomobile_parte1.data.repo.api

import com.example.projeto_programaomobile_parte1.data.model.Usuario
import com.example.projeto_programaomobile_parte1.util.Result

interface IAuthRepository {
    suspend fun cadastrar(nome: String, email: String, senha: String): Result<Usuario>
    suspend fun efetuarLogin(email: String, senha: String): Result<Usuario>
    suspend fun enviarEmailRecuperacaoSenha(email: String): Result<Unit>
    fun efetuarLogout()
    fun usuarioAtual(): Usuario?
    fun isLoggedIn(): Boolean
    fun getCurrentUserId(): String?
}
