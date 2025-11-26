package com.example.projeto_programaomobile_parte1.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Usuario(
    @DocumentId
    val uid: String = "",
    val nome: String = "",
    val email: String = "",
    val criadoEm: Timestamp = Timestamp.now()
) {
    // Construtor sem argumentos necessário para Firestore
    constructor() : this("", "", "", Timestamp.now())

    // Converter para Map para salvar no Firestore
    fun toMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "nome" to nome,
        "email" to email,
        "criadoEm" to criadoEm
    )
}
