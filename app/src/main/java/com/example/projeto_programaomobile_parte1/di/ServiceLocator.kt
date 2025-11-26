package com.example.projeto_programaomobile_parte1.di

import android.content.Context
import com.example.projeto_programaomobile_parte1.data.repo.AuthRepository
import com.example.projeto_programaomobile_parte1.data.repo.ListRepository
import com.example.projeto_programaomobile_parte1.util.ImageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Service Locator para prover instâncias de repositórios e utilitários
 * Centraliza a criação de dependências da aplicação
 */
object ServiceLocator {

    private var authRepositoryInstance: AuthRepository? = null
    private var listRepositoryInstance: ListRepository? = null
    private var imageManagerInstance: ImageManager? = null

    /**
     * Fornece instância do AuthRepository (Singleton)
     */
    fun authRepository(): AuthRepository {
        return authRepositoryInstance ?: AuthRepository(
            FirebaseAuth.getInstance(),
            FirebaseFirestore.getInstance()
        ).also { authRepositoryInstance = it }
    }

    /**
     * Fornece instância do ListRepository (Singleton)
     * Requer Context para o ImageManager
     */
    fun listRepository(context: Context): ListRepository {
        return listRepositoryInstance ?: ListRepository(
            context.applicationContext,
            FirebaseFirestore.getInstance()
        ).also { listRepositoryInstance = it }
    }

    /**
     * Fornece instância do ImageManager (Singleton)
     */
    fun imageManager(context: Context): ImageManager {
        return imageManagerInstance ?: ImageManager(
            context.applicationContext
        ).also { imageManagerInstance = it }
    }

    /**
     * Limpa todas as instâncias (útil para logout completo)
     */
    fun clear() {
        authRepositoryInstance = null
        listRepositoryInstance = null
        imageManagerInstance = null
    }
}
