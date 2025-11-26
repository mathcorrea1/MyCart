package com.example.projeto_programaomobile_parte1.data.repo

import com.example.projeto_programaomobile_parte1.data.model.Usuario
import com.example.projeto_programaomobile_parte1.data.repo.api.IAuthRepository
import com.example.projeto_programaomobile_parte1.util.Result
import com.example.projeto_programaomobile_parte1.util.toAuthErrorMessage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository para gerenciar autenticação com Firebase
 * Usa Firebase Authentication para login/cadastro e Firestore para dados do usuário
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : IAuthRepository {

    private val usersCollection = firestore.collection("users")

    /**
     * Retorna o usuário atualmente logado
     */
    override fun usuarioAtual(): Usuario? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let {
            Usuario(
                uid = it.uid,
                nome = it.displayName ?: "",
                email = it.email ?: "",
                criadoEm = Timestamp.now()
            )
        }
    }

    /**
     * Verifica se há um usuário logado
     */
    override fun isLoggedIn(): Boolean = auth.currentUser != null

    /**
     * Retorna o UID do usuário logado
     */
    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Cadastra novo usuário no Firebase Auth e salva dados no Firestore
     */
    override suspend fun cadastrar(nome: String, email: String, senha: String): Result<Usuario> {
        return try {
            // 1. Criar usuário no Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(email, senha).await()
            val firebaseUser = authResult.user
                ?: return Result.Error(Exception("Erro ao criar usuário"))

            // 2. Criar documento do usuário no Firestore
            val usuario = Usuario(
                uid = firebaseUser.uid,
                nome = nome,
                email = email,
                criadoEm = Timestamp.now()
            )

            // 3. Salvar no Firestore
            usersCollection.document(firebaseUser.uid)
                .set(usuario.toMap())
                .await()

            Result.Success(usuario)
        } catch (e: Exception) {
            Result.Error(e, e.toAuthErrorMessage())
        }
    }

    /**
     * Realiza login com Firebase Authentication
     */
    override suspend fun efetuarLogin(email: String, senha: String): Result<Usuario> {
        return try {
            // 1. Fazer login no Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, senha).await()
            val firebaseUser = authResult.user
                ?: return Result.Error(Exception("Erro ao fazer login"))

            // 2. Buscar dados do usuário no Firestore
            val snapshot = usersCollection.document(firebaseUser.uid).get().await()
            val usuarioData = snapshot.data

            val usuario = if (usuarioData != null) {
                Usuario(
                    uid = usuarioData["uid"] as? String ?: firebaseUser.uid,
                    nome = usuarioData["nome"] as? String ?: firebaseUser.displayName ?: "",
                    email = usuarioData["email"] as? String ?: firebaseUser.email ?: "",
                    criadoEm = usuarioData["criadoEm"] as? Timestamp ?: Timestamp.now()
                )
            } else {
                Usuario(
                    uid = firebaseUser.uid,
                    nome = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    criadoEm = Timestamp.now()
                )
            }

            Result.Success(usuario)
        } catch (e: Exception) {
            Result.Error(e, e.toAuthErrorMessage())
        }
    }

    /**
     * Envia email de recuperação de senha
     */
    override suspend fun enviarEmailRecuperacaoSenha(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.toAuthErrorMessage())
        }
    }

    /**
     * Realiza logout
     */
    override fun efetuarLogout() {
        auth.signOut()
    }

    /**
     * Busca dados completos do usuário no Firestore
     */
    suspend fun buscarDadosUsuario(uid: String): Result<Usuario> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            val data = snapshot.data
                ?: return Result.Error(Exception("Usuário não encontrado"))

            val usuario = Usuario(
                uid = data["uid"] as? String ?: uid,
                nome = data["nome"] as? String ?: "",
                email = data["email"] as? String ?: "",
                criadoEm = data["criadoEm"] as? Timestamp ?: Timestamp.now()
            )
            Result.Success(usuario)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao buscar dados do usuário")
        }
    }

    /**
     * Atualiza dados do usuário no Firestore
     */
    suspend fun atualizarUsuario(usuario: Usuario): Result<Unit> {
        return try {
            usersCollection.document(usuario.uid)
                .set(usuario.toMap())
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Erro ao atualizar usuário")
        }
    }
}
