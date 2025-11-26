package com.example.projeto_programaomobile_parte1.ui.itens

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.projeto_programaomobile_parte1.R
import com.example.projeto_programaomobile_parte1.data.model.Categoria
import com.example.projeto_programaomobile_parte1.data.model.Unidade
import com.example.projeto_programaomobile_parte1.databinding.ActivityAddItemBinding

class AddItemActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NOME = "extra_nome"
        const val EXTRA_QTD = "extra_qtd"
        const val EXTRA_UNIDADE = "extra_unidade"
        const val EXTRA_CATEGORIA = "extra_categoria"
    }

    private lateinit var binding: ActivityAddItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.spnUnidade.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Unidade.entries)
        binding.spnCategoria.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Categoria.entries)

        binding.btnSalvar.setOnClickListener {
            val nome = binding.tilNome.editText?.text?.toString()?.trim().orEmpty()
            val qtdStr = binding.tilQuantidade.editText?.text?.toString()?.replace(",", ".")
            val qtd = qtdStr?.toDoubleOrNull() ?: 0.0

            var valido = true
            if (nome.isBlank()) {
                binding.tilNome.error = getString(R.string.erro_campo_obrigatorio)
                valido = false
            } else {
                binding.tilNome.error = null
            }
            if (qtd <= 0) {
                binding.tilQuantidade.error = getString(R.string.erro_campo_obrigatorio)
                valido = false
            } else {
                binding.tilQuantidade.error = null
            }
            if (!valido) return@setOnClickListener

            val unidade = Unidade.entries[binding.spnUnidade.selectedItemPosition]
            val categoria = Categoria.entries[binding.spnCategoria.selectedItemPosition]

            val data = Intent().apply {
                putExtra(EXTRA_NOME, nome)
                putExtra(EXTRA_QTD, qtd)
                putExtra(EXTRA_UNIDADE, unidade.name)
                putExtra(EXTRA_CATEGORIA, categoria.name)
            }
            setResult(RESULT_OK, data)
            finish()
        }
    }
}
