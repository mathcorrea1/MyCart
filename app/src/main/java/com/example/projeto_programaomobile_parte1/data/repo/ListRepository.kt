package com.example.projeto_programaomobile_parte1.data.repo

import android.content.Context
import android.net.Uri
import com.example.projeto_programaomobile_parte1.data.model.Categoria
import com.example.projeto_programaomobile_parte1.data.model.Item
import com.example.projeto_programaomobile_parte1.data.model.ListaCompra
import com.example.projeto_programaomobile_parte1.data.model.Unidade
import com.example.projeto_programaomobile_parte1.data.repo.api.IListRepository
import com.example.projeto_programaomobile_parte1.util.ImageManager
import com.example.projeto_programaomobile_parte1.util.Result
import com.example.projeto_programaomobile_parte1.util.toFirestoreErrorMessage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Repository para gerenciar listas de compras com Firebase Firestore
 * Usa listeners em tempo real para sincronização automática
 */
class ListRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : IListRepository {
    private val listasCollection = firestore.collection("listas")
    private val imageManager = ImageManager(context)

    /**
     * Observa listas do usuário em tempo real (Flow)
     */
    override fun observarListas(userId: String): Flow<Result<List<ListaCompra>>> = callbackFlow {
        trySend(Result.Loading)

        val listener = listasCollection
            .whereEqualTo("userId", userId)
            .orderBy("titulo", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, error.toFirestoreErrorMessage()))
                    return@addSnapshotListener
                }

                val listas = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        ListaCompra(
                            id = doc.id,
                            userId = data["userId"] as? String ?: "",
                            titulo = data["titulo"] as? String ?: "",
                            imagemPath = data["imagemPath"] as? String?,
                            criadaEm = data["criadaEm"] as? Timestamp ?: Timestamp.now(),
                            atualizadaEm = data["atualizadaEm"] as? Timestamp ?: Timestamp.now()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(Result.Success(listas))
            }

        awaitClose { listener.remove() }
    }

    /**
     * Busca todas as listas do usuário (uma vez)
     */
    override suspend fun obterListas(userId: String): Result<List<ListaCompra>> {
        return try {
            val snapshot = listasCollection
                .whereEqualTo("userId", userId)
                .orderBy("titulo", Query.Direction.ASCENDING)
                .get()
                .await()

            val listas = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    ListaCompra(
                        id = doc.id,
                        userId = data["userId"] as? String ?: "",
                        titulo = data["titulo"] as? String ?: "",
                        imagemPath = data["imagemPath"] as? String?,
                        criadaEm = data["criadaEm"] as? Timestamp ?: Timestamp.now(),
                        atualizadaEm = data["atualizadaEm"] as? Timestamp ?: Timestamp.now()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.Success(listas)
        } catch (e: Exception) {
            Result.Error(e, e.toFirestoreErrorMessage())
        }
    }

    /**
     * Adiciona nova lista no Firestore
     */
    override suspend fun adicionarLista(
        userId: String,
        titulo: String,
        imagemUri: Uri?
    ): Result<ListaCompra> {
        return try {
            val listaId = UUID.randomUUID().toString()

            // Salvar imagem localmente se foi fornecida
            val imagemPath = imagemUri?.let {
                imageManager.salvarImagem(it, userId, listaId)
            }

            val lista = ListaCompra(
                id = listaId,
                userId = userId,
                titulo = titulo,
                imagemPath = imagemPath,
                criadaEm = Timestamp.now(),
                atualizadaEm = Timestamp.now()
            )

            listasCollection.document(listaId)
                .set(lista.toMap())
                .await()

            Result.Success(lista)
        } catch (e: Exception) {
            Result.Error(e, e.toFirestoreErrorMessage())
        }
    }

    /**
     * Edita uma lista existente
     */
    override suspend fun editarLista(
        userId: String,
        id: String,
        novoTitulo: String,
        novaImagemUri: Uri?
    ): Result<ListaCompra> {
        return try {
            // Buscar lista atual
            val snapshot = listasCollection.document(id).get().await()
            val data = snapshot.data
                ?: return Result.Error(Exception("Lista não encontrada"))

            val listaAtual = ListaCompra(
                id = snapshot.id,
                userId = data["userId"] as? String ?: "",
                titulo = data["titulo"] as? String ?: "",
                imagemPath = data["imagemPath"] as? String?,
                criadaEm = data["criadaEm"] as? Timestamp ?: Timestamp.now(),
                atualizadaEm = data["atualizadaEm"] as? Timestamp ?: Timestamp.now()
            )

            // Atualizar imagem se foi fornecida
            val imagemPath = if (novaImagemUri != null) {
                // Excluir imagem antiga se existir
                listaAtual.imagemPath?.let { imageManager.excluirImagem(it) }
                // Salvar nova imagem
                imageManager.salvarImagem(novaImagemUri, userId, id)
            } else {
                listaAtual.imagemPath
            }

            val updates = mapOf(
                "titulo" to novoTitulo,
                "imagemPath" to imagemPath,
                "atualizadaEm" to Timestamp.now()
            )

            listasCollection.document(id).update(updates).await()

            val listaAtualizada = listaAtual.copy(
                titulo = novoTitulo,
                imagemPath = imagemPath,
                atualizadaEm = Timestamp.now()
            )

            Result.Success(listaAtualizada)
        } catch (e: Exception) {
            Result.Error(e, e.toFirestoreErrorMessage())
        }
    }

    /**
     * Exclui uma lista e todos os seus itens
     */
    override suspend fun excluirLista(userId: String, id: String): Result<Unit> {
        return try {
            // 1. Buscar a lista para pegar o imagemPath
            val snapshot = listasCollection.document(id).get().await()
            val data = snapshot.data

            // 2. Excluir imagem local se existir
            val imagemPath = data?.get("imagemPath") as? String
            imagemPath?.let { imageManager.excluirImagem(it) }

            // 3. Excluir todos os itens da lista (subcoleção)
            val itensSnapshot = listasCollection.document(id)
                .collection("itens")
                .get()
                .await()

            itensSnapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            // 4. Excluir a lista
            listasCollection.document(id).delete().await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.toFirestoreErrorMessage())
        }
    }

    // ==================== MÉTODOS PARA ITENS ====================

    /**
     * Observa itens de uma lista em tempo real
     */
    override fun observarItens(listaId: String): Flow<Result<List<Item>>> = callbackFlow {
        trySend(Result.Loading)

        val listener = listasCollection.document(listaId)
            .collection("itens")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error, error.toFirestoreErrorMessage()))
                    return@addSnapshotListener
                }

                val itens = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        Item(
                            id = doc.id,
                            listaId = data["listaId"] as? String ?: "",
                            nome = data["nome"] as? String ?: "",
                            quantidade = (data["quantidade"] as? Number)?.toDouble() ?: 0.0,
                            unidade = Unidade.valueOf(data["unidade"] as? String ?: "UN"),
                            categoria = Categoria.valueOf(data["categoria"] as? String ?: "OUTROS"),
                            comprado = data["comprado"] as? Boolean ?: false,
                            criadoEm = data["criadoEm"] as? Timestamp ?: Timestamp.now(),
                            atualizadoEm = data["atualizadoEm"] as? Timestamp ?: Timestamp.now()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(Result.Success(itens))
            }

        awaitClose { listener.remove() }
    }

    /**
     * Adiciona novo item à lista
     */
    override suspend fun adicionarItem(
        listaId: String,
        nome: String,
        quantidade: Double,
        unidade: Unidade,
        categoria: Categoria
    ): Result<Item> {
        return try {
            val itemId = UUID.randomUUID().toString()
            val item = Item(
                id = itemId,
                listaId = listaId,
                nome = nome,
                quantidade = quantidade,
                unidade = unidade,
                categoria = categoria,
                comprado = false,
                criadoEm = Timestamp.now(),
                atualizadoEm = Timestamp.now()
            )

            listasCollection.document(listaId)
                .collection("itens")
                .document(itemId)
                .set(item.toMap())
                .await()

            Result.Success(item)
        } catch (e: Exception) {
            Result.Error(e, e.toFirestoreErrorMessage())
        }
    }

    /**
     * Edita um item existente
     */
    override suspend fun editarItem(item: Item): Result<Item> {
        return try {
            listasCollection.document(item.listaId)
                .collection("itens")
                .document(item.id)
                .set(item.toMap())
                .await()

            Result.Success(item)
        } catch (e: Exception) {
            Result.Error(e, e.toFirestoreErrorMessage())
        }
    }

    /**
     * Remove um item
     */
    override suspend fun removerItem(listaId: String, itemId: String): Result<Unit> {
        return try {
            listasCollection.document(listaId)
                .collection("itens")
                .document(itemId)
                .delete()
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.toFirestoreErrorMessage())
        }
    }

    /**
     * Marca/desmarca item como comprado
     */
    override suspend fun alternarComprado(listaId: String, itemId: String, comprado: Boolean): Result<Unit> {
        return try {
            listasCollection.document(listaId)
                .collection("itens")
                .document(itemId)
                .update(mapOf(
                    "comprado" to comprado,
                    "atualizadoEm" to Timestamp.now()
                ))
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.toFirestoreErrorMessage())
        }
    }
}
