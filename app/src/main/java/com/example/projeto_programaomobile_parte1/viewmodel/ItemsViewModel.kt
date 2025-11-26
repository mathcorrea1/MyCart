package com.example.projeto_programaomobile_parte1.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projeto_programaomobile_parte1.data.model.Categoria
import com.example.projeto_programaomobile_parte1.data.model.Item
import com.example.projeto_programaomobile_parte1.data.model.Unidade
import com.example.projeto_programaomobile_parte1.di.ServiceLocator
import com.example.projeto_programaomobile_parte1.domain.AgrupamentoItens
import com.example.projeto_programaomobile_parte1.domain.agruparItensPorCategoria
import com.example.projeto_programaomobile_parte1.util.Result
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ItemsViewModel(private val context: Context, private val listaId: String) : ViewModel() {
    private val repo = ServiceLocator.listRepository(context)

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _busca = MutableLiveData("")
    val busca: LiveData<String> = _busca

    private val _todosItens = MutableLiveData<List<Item>>(emptyList())

    private val _agrupados = MutableLiveData<AgrupamentoItens>(AgrupamentoItens(emptyList(), emptyList()))
    val agrupados: LiveData<AgrupamentoItens> = _agrupados

    private val _mensagem = MutableLiveData<String?>()
    val mensagem: LiveData<String?> = _mensagem

    init {
        // Observar itens em tempo real do Firestore
        observarItens()
    }

    private fun observarItens() {
        repo.observarItens(listaId)
            .onEach { result ->
                when (result) {
                    is Result.Loading -> {
                        // Só mostra loading na primeira carga
                        if (_todosItens.value.isNullOrEmpty()) {
                            _loading.value = true
                        }
                    }
                    is Result.Success -> {
                        _loading.value = false
                        _todosItens.value = result.data
                        aplicarFiltroEAgrupamento()
                    }
                    is Result.Error -> {
                        _loading.value = false
                        _mensagem.value = result.message ?: "Erro ao carregar itens"
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun atualizarBusca(termo: String) {
        _busca.value = termo
        aplicarFiltroEAgrupamento()
    }

    private fun aplicarFiltroEAgrupamento() {
        val termo = _busca.value?.lowercase()?.trim().orEmpty()
        val itens = _todosItens.value ?: emptyList()

        val filtrados = if (termo.isBlank()) {
            itens
        } else {
            itens.filter { it.nome.lowercase().contains(termo) }
        }

        _agrupados.value = agruparItensPorCategoria(filtrados)
    }

    fun adicionarItem(nome: String, quantidade: Double, unidade: Unidade, categoria: Categoria) {
        viewModelScope.launch {
            when (val result = repo.adicionarItem(listaId, nome, quantidade, unidade, categoria)) {
                is Result.Success -> {
                    _mensagem.value = "Item adicionado"
                    // O item será atualizado automaticamente pelo listener
                }
                is Result.Error -> {
                    _mensagem.value = result.message ?: "Erro ao adicionar item"
                }
                is Result.Loading -> {}
            }
        }
    }

    fun editarItem(item: Item) {
        viewModelScope.launch {
            when (val result = repo.editarItem(item)) {
                is Result.Success -> {
                    _mensagem.value = "Item atualizado"
                    // O item será atualizado automaticamente pelo listener
                }
                is Result.Error -> {
                    _mensagem.value = result.message ?: "Erro ao atualizar item"
                }
                is Result.Loading -> {}
            }
        }
    }

    fun removerItem(itemId: String) {
        viewModelScope.launch {
            when (val result = repo.removerItem(listaId, itemId)) {
                is Result.Success -> {
                    _mensagem.value = "Item removido"
                    // O item será atualizado automaticamente pelo listener
                }
                is Result.Error -> {
                    _mensagem.value = result.message ?: "Erro ao remover item"
                }
                is Result.Loading -> {}
            }
        }
    }

    fun alternarComprado(item: Item) {
        viewModelScope.launch {
            when (val result = repo.alternarComprado(listaId, item.id, !item.comprado)) {
                is Result.Success -> {
                    // Sucesso silencioso - o listener atualizará automaticamente
                }
                is Result.Error -> {
                    _mensagem.value = result.message ?: "Erro ao atualizar item"
                }
                is Result.Loading -> {}
            }
        }
    }
}
