package com.example.projeto_programaomobile_parte1.util

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Classe para gerenciar armazenamento local de imagens
 * As imagens são salvas em: /data/data/{package}/files/listas/{userId}/{listaId}/imagem.jpg
 */
class ImageManager(private val context: Context) {

    private val baseDir = File(context.filesDir, "listas")

    init {
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
    }

    /**
     * Salva uma imagem no storage local
     * @param uri URI da imagem selecionada (galeria ou câmera)
     * @param userId ID do usuário dono da lista
     * @param listaId ID da lista
     * @return Caminho absoluto da imagem salva, ou null se falhar
     */
    fun salvarImagem(uri: Uri, userId: String, listaId: String): String? {
        return try {
            // Criar diretório específico da lista
            val listaDir = File(baseDir, "$userId/$listaId")
            if (!listaDir.exists()) {
                listaDir.mkdirs()
            }

            // Arquivo de destino
            val imageFile = File(listaDir, "imagem.jpg")

            // Copiar conteúdo da URI para o arquivo
            context.contentResolver.openInputStream(uri)?.use { input ->
                imageFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Retornar caminho absoluto
            imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Carrega um arquivo de imagem do caminho local
     * @param path Caminho absoluto da imagem
     * @return File se existir, null caso contrário
     */
    fun carregarImagem(path: String?): File? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        return if (file.exists()) file else null
    }

    /**
     * Exclui uma imagem do storage local
     * @param path Caminho absoluto da imagem
     * @return true se foi excluída com sucesso
     */
    fun excluirImagem(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Exclui todas as imagens de uma lista
     * @param userId ID do usuário
     * @param listaId ID da lista
     */
    fun excluirImagensDaLista(userId: String, listaId: String): Boolean {
        return try {
            val listaDir = File(baseDir, "$userId/$listaId")
            if (listaDir.exists()) {
                listaDir.deleteRecursively()
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Limpa todas as imagens de um usuário (útil no logout)
     * @param userId ID do usuário
     */
    fun limparImagensDoUsuario(userId: String): Boolean {
        return try {
            val userDir = File(baseDir, userId)
            if (userDir.exists()) {
                userDir.deleteRecursively()
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

