package com.example.projeto_programaomobile_parte1.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_programaomobile_parte1.data.model.ListaCompra
import com.example.projeto_programaomobile_parte1.di.ServiceLocator
import com.example.projeto_programaomobile_parte1.domain.ordenarListas
import com.example.projeto_programaomobile_parte1.util.Result
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ListsViewModel(private val context: Context, private val userId: String) : ViewModel() {
    private val repo = ServiceLocator.listRepository(context)

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _busca = MutableLiveData("")
    val busca: LiveData<String> = _busca

    private val _todasListas = MutableLiveData<List<ListaCompra>>(emptyList())

    private val _listasFiltradas = MutableLiveData<List<ListaCompra>>(emptyList())
    val listasFiltradas: LiveData<List<ListaCompra>> = _listasFiltradas

    private val _mensagem = MutableLiveData<String?>()
    val mensagem: LiveData<String?> = _mensagem

    init {
        // Observar listas em tempo real do Firestore
        observarListas()
    }

    private fun observarListas() {
        repo.observarListas(userId)
            .onEach { result ->
                when (result) {
                    is Result.Loading -> {
                        // Só mostra loading na primeira carga
                        if (_todasListas.value.isNullOrEmpty()) {
                            _loading.value = true
                        }
                    }
                    is Result.Success -> {
                        _loading.value = false
                        _todasListas.value = result.data
                        aplicarFiltro()
                    }
                    is Result.Error -> {
                        _loading.value = false
                        _mensagem.value = result.message ?: "Erro ao carregar listas"
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun atualizarBusca(termo: String) {
        _busca.value = termo
        aplicarFiltro()
    }

    private fun aplicarFiltro() {
        val termo = _busca.value?.lowercase()?.trim().orEmpty()
        val listas = _todasListas.value ?: emptyList()

        val filtradas = if (termo.isBlank()) {
            listas
        } else {
            listas.filter { it.titulo.lowercase().contains(termo) }
        }

        _listasFiltradas.value = ordenarListas(filtradas)
    }

    fun adicionarLista(titulo: String, uri: Uri?) {
        viewModelScope.launch {
            when (val result = repo.adicionarLista(userId, titulo, uri)) {
                is Result.Success -> {
                    _mensagem.value = "Lista criada com sucesso"
                    // A lista será atualizada automaticamente pelo listener
                }
                is Result.Error -> {
                    _mensagem.value = result.message ?: "Erro ao criar lista"
                }
                is Result.Loading -> {}
            }
        }
    }

    fun editarLista(id: String, novoTitulo: String, novaImagem: Uri?) {
        viewModelScope.launch {
            when (val result = repo.editarLista(userId, id, novoTitulo, novaImagem)) {
                is Result.Success -> {
                    _mensagem.value = "Lista atualizada"
                    // A lista será atualizada automaticamente pelo listener
                }
                is Result.Error -> {
                    _mensagem.value = result.message ?: "Erro ao atualizar lista"
                }
                is Result.Loading -> {}
            }
        }
    }

    fun excluirLista(id: String) {
        viewModelScope.launch {
            when (val result = repo.excluirLista(userId, id)) {
                is Result.Success -> {
                    _mensagem.value = "Lista removida"
                    // A lista será atualizada automaticamente pelo listener
                }
                is Result.Error -> {
                    _mensagem.value = result.message ?: "Erro ao remover lista"
                }
                is Result.Loading -> {}
            }
        }
    }
}
