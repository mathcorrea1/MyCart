package com.example.projeto_programaomobile_parte1.data.repo.api

import android.net.Uri
import com.example.projeto_programaomobile_parte1.data.model.Categoria
import com.example.projeto_programaomobile_parte1.data.model.Item
import com.example.projeto_programaomobile_parte1.data.model.ListaCompra
import com.example.projeto_programaomobile_parte1.data.model.Unidade
import com.example.projeto_programaomobile_parte1.util.Result
import kotlinx.coroutines.flow.Flow

interface IListRepository {
    fun observarListas(userId: String): Flow<Result<List<ListaCompra>>>
    suspend fun obterListas(userId: String): Result<List<ListaCompra>>
    suspend fun adicionarLista(userId: String, titulo: String, imagemUri: Uri?): Result<ListaCompra>
    suspend fun editarLista(userId: String, id: String, novoTitulo: String, novaImagem: Uri?): Result<ListaCompra>
    suspend fun excluirLista(userId: String, id: String): Result<Unit>

    fun observarItens(listaId: String): Flow<Result<List<Item>>>
    suspend fun adicionarItem(
        listaId: String,
        nome: String,
        quantidade: Double,
        unidade: Unidade,
        categoria: Categoria
    ): Result<Item>
    suspend fun editarItem(item: Item): Result<Item>
    suspend fun removerItem(listaId: String, itemId: String): Result<Unit>
    suspend fun alternarComprado(listaId: String, itemId: String, comprado: Boolean): Result<Unit>
}
