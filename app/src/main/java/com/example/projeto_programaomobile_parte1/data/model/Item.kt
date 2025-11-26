package com.example.projeto_programaomobile_parte1.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.UUID

data class Item(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val listaId: String = "",
    var nome: String = "",
    var quantidade: Double = 0.0,
    var unidade: Unidade = Unidade.UN,
    var categoria: Categoria = Categoria.OUTROS,
    var comprado: Boolean = false,
    val criadoEm: Timestamp = Timestamp.now(),
    var atualizadoEm: Timestamp = Timestamp.now()
) {
    // Construtor sem argumentos necessário para Firestore
    constructor() : this(
        id = "",
        listaId = "",
        nome = "",
        quantidade = 0.0,
        unidade = Unidade.UN,
        categoria = Categoria.OUTROS,
        comprado = false,
        criadoEm = Timestamp.now(),
        atualizadoEm = Timestamp.now()
    )

    // Converter para Map para salvar no Firestore
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "listaId" to listaId,
        "nome" to nome,
        "quantidade" to quantidade,
        "unidade" to unidade.name,
        "categoria" to categoria.name,
        "comprado" to comprado,
        "criadoEm" to criadoEm,
        "atualizadoEm" to atualizadoEm
    )
}
