package com.example.projeto_programaomobile_parte1.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ListaCompra(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    var titulo: String = "",
    var imagemPath: String? = null, // Caminho local da imagem no device
    val criadaEm: Timestamp = Timestamp.now(),
    var atualizadaEm: Timestamp = Timestamp.now()
) {
    // Construtor sem argumentos necessário para Firestore
    constructor() : this("", "", "", null, Timestamp.now(), Timestamp.now())

    // Converter para Map para salvar no Firestore
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "titulo" to titulo,
        "imagemPath" to imagemPath,
        "criadaEm" to criadaEm,
        "atualizadaEm" to Timestamp.now()
    )
}
