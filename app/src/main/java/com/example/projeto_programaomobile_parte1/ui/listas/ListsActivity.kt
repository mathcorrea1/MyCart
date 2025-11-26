package com.example.projeto_programaomobile_parte1.ui.listas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projeto_programaomobile_parte1.R
import com.example.projeto_programaomobile_parte1.data.model.ListaCompra
import com.example.projeto_programaomobile_parte1.databinding.ActivityListsBinding
import com.example.projeto_programaomobile_parte1.di.ServiceLocator
import com.example.projeto_programaomobile_parte1.ui.login.LoginActivity
import com.example.projeto_programaomobile_parte1.viewmodel.ListsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File

class ListsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListsBinding
    private lateinit var vm: ListsViewModel

    private var listaSelecionadaParaImagem: ListaCompra? = null
    private var imagemCameraUri: Uri? = null

    private val abrirDocumento = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        val lista = listaSelecionadaParaImagem
        if (uri != null && lista != null) {
            vm.editarLista(lista.id, lista.titulo, uri)
        }
        listaSelecionadaParaImagem = null
    }

    private val tirarFoto = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicture()) { sucesso: Boolean ->
        val lista = listaSelecionadaParaImagem
        if (sucesso && imagemCameraUri != null && lista != null) {
            vm.editarLista(lista.id, lista.titulo, imagemCameraUri)
        }
        listaSelecionadaParaImagem = null
        imagemCameraUri = null
    }

    private val novaListaLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val titulo = data?.getStringExtra(AddListActivity.EXTRA_TITULO).orEmpty()
            val imagemStr = data?.getStringExtra(AddListActivity.EXTRA_IMAGEM)
            val imagemUri = imagemStr?.let { Uri.parse(it) }
            if (titulo.isNotBlank()) {
                vm.adicionarLista(titulo, imagemUri)
            }
        }
    }

    private val editarListaLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val id = data?.getStringExtra(EditListActivity.EXTRA_ID).orEmpty()
            val excluir = data?.getBooleanExtra(EditListActivity.EXTRA_DELETE, false) == true
            if (excluir) {
                if (id.isNotBlank()) vm.excluirLista(id)
            } else {
                val titulo = data?.getStringExtra(EditListActivity.EXTRA_TITULO).orEmpty()
                val imagemStr = data?.getStringExtra(EditListActivity.EXTRA_IMAGEM)
                val imagemUri = imagemStr?.let { Uri.parse(it) }
                if (id.isNotBlank() && titulo.isNotBlank()) {
                    vm.editarLista(id, titulo, imagemUri)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar se há usuário logado
        val auth = ServiceLocator.authRepository()
        val userId = auth.getCurrentUserId()
        if (userId == null || !auth.isLoggedIn()) {
            irParaLogin()
            return
        }

        // Configurar a ActionBar
        setSupportActionBar(binding.toolbar)

        // Criar ViewModel com Factory (precisa de Context e userId)
        vm = ViewModelProvider(
            this,
            ListsViewModelFactory(applicationContext, userId)
        )[ListsViewModel::class.java]

        val adapter = ListaAdapter(
            aoClicar = { lista, _ -> abrirItensDaLista(lista) },
            aoClicarMenu = { lista, view -> mostrarMenuLista(lista, view) }
        )
        binding.rvListas.layoutManager = GridLayoutManager(this, 2)
        binding.rvListas.adapter = adapter

        binding.fabAdd.setOnClickListener {
            val i = Intent(this, AddListActivity::class.java)
            novaListaLauncher.launch(i)
        }

        // Observar estados
        vm.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        vm.listasFiltradas.observe(this) { listas ->
            adapter.submitList(listas)
            binding.txtVazio.visibility = if (listas.isEmpty()) View.VISIBLE else View.GONE
        }

        vm.mensagem.observe(this) { msg ->
            msg?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_lists, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? androidx.appcompat.widget.SearchView
        searchView?.queryHint = getString(R.string.hint_buscar_lista)
        searchView?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                vm.atualizarBusca(query.orEmpty())
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                vm.atualizarBusca(newText.orEmpty())
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                realizarLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun realizarLogout() {
        // Limpar dados e fazer logout
        ServiceLocator.clear()
        ServiceLocator.authRepository().efetuarLogout()
        irParaLogin()
    }

    private fun irParaLogin() {
        val i = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(i)
        finish()
    }

    private fun mostrarMenuLista(lista: ListaCompra, anchor: View) {
        val menu = PopupMenu(this, anchor)
        menu.menuInflater.inflate(R.menu.menu_lista_item, menu.menu)
        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_editar -> { abrirTelaEditarLista(lista); true }
                R.id.menu_excluir -> { vm.excluirLista(lista.id); true }
                R.id.menu_imagem -> { selecionarImagemPara(lista); true }
                else -> false
            }
        }
        menu.show()
    }

    private fun abrirTelaEditarLista(lista: ListaCompra) {
        val i = Intent(this, EditListActivity::class.java).apply {
            putExtra(EditListActivity.EXTRA_ID, lista.id)
            putExtra(EditListActivity.EXTRA_TITULO, lista.titulo)
            putExtra(EditListActivity.EXTRA_IMAGEM, lista.imagemPath)
        }
        editarListaLauncher.launch(i)
    }

    private fun abrirItensDaLista(lista: ListaCompra) {
        val i = Intent(this, com.example.projeto_programaomobile_parte1.ui.itens.ItemsActivity::class.java).apply {
            putExtra("listaId", lista.id)
            putExtra("listaTitulo", lista.titulo)
        }
        startActivity(i)
    }

    private fun selecionarImagemPara(lista: ListaCompra) {
        listaSelecionadaParaImagem = lista
        mostrarDialogoEscolhaImagem()
    }

    private fun mostrarDialogoEscolhaImagem() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.titulo_escolher_origem_imagem)
            .setItems(arrayOf(
                getString(R.string.opcao_camera),
                getString(R.string.opcao_galeria)
            )) { _, which ->
                when (which) {
                    0 -> abrirCamera()
                    1 -> abrirGaleria()
                }
            }
            .show()
    }

    private fun abrirCamera() {
        val imagemArquivo = File(cacheDir, "foto_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            imagemArquivo
        )
        imagemCameraUri = uri
        tirarFoto.launch(uri)
    }

    private fun abrirGaleria() {
        abrirDocumento.launch(arrayOf("image/*"))
    }
}

// Factory para criar ViewModel com parâmetros
class ListsViewModelFactory(
    private val context: android.content.Context,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListsViewModel(context, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
