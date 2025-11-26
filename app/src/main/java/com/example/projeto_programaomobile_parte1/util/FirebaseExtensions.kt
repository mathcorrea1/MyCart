package com.example.projeto_programaomobile_parte1.util

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Extensões úteis para trabalhar com Firebase de forma mais idiomática com Coroutines
 */

/**
 * Converte um Task do Firebase em suspend function
 * Permite usar await() para aguardar o resultado
 */
suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
}

/**
 * Tratamento de erros do Firebase Authentication
 * Retorna mensagens amigáveis em português
 */
fun Exception.toAuthErrorMessage(): String {
    return when {
        message?.contains("The email address is badly formatted") == true ->
            "Email inválido"
        message?.contains("The password is invalid") == true ->
            "Senha incorreta"
        message?.contains("There is no user record") == true ->
            "Usuário não encontrado"
        message?.contains("The email address is already in use") == true ->
            "Este email já está cadastrado"
        message?.contains("The password must be 6 characters") == true ->
            "A senha deve ter pelo menos 6 caracteres"
        message?.contains("A network error") == true ->
            "Erro de conexão. Verifique sua internet"
        message?.contains("too many requests") == true ->
            "Muitas tentativas. Tente novamente mais tarde"
        else -> message ?: "Erro desconhecido"
    }
}

/**
 * Tratamento de erros do Firestore
 * Retorna mensagens amigáveis em português
 */
fun Exception.toFirestoreErrorMessage(): String {
    return when {
        message?.contains("PERMISSION_DENIED") == true ->
            "Permissão negada. Faça login novamente"
        message?.contains("NOT_FOUND") == true ->
            "Documento não encontrado"
        message?.contains("ALREADY_EXISTS") == true ->
            "Este item já existe"
        message?.contains("network error") == true ->
            "Erro de conexão. Verifique sua internet"
        message?.contains("UNAVAILABLE") == true ->
            "Serviço temporariamente indisponível"
        else -> message ?: "Erro ao acessar dados"
    }
}

