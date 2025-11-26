package com.example.projeto_programaomobile_parte1.ui.listas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projeto_programaomobile_parte1.R
import com.example.projeto_programaomobile_parte1.data.model.ListaCompra
import com.example.projeto_programaomobile_parte1.databinding.ItemListaBinding
import java.io.File

class ListaAdapter(
    private val aoClicar: (ListaCompra, View) -> Unit,
    private val aoClicarMenu: (ListaCompra, View) -> Unit
) : ListAdapter<ListaCompra, ListaAdapter.VH>(DIFF) {

    object DIFF : DiffUtil.ItemCallback<ListaCompra>() {
        override fun areItemsTheSame(oldItem: ListaCompra, newItem: ListaCompra): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ListaCompra, newItem: ListaCompra): Boolean = oldItem == newItem
    }

    inner class VH(val b: ItemListaBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemListaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.txtTitulo.text = item.titulo

        // Usar Glide para carregar imagem do caminho local
        val imagemPath = item.imagemPath
        if (!imagemPath.isNullOrBlank()) {
            val file = File(imagemPath)
            if (file.exists()) {
                Glide.with(holder.itemView.context)
                    .load(file)
                    .placeholder(R.drawable.ic_lista_placeholder)
                    .error(R.drawable.ic_lista_placeholder)
                    .centerCrop()
                    .into(holder.b.imgThumb)
            } else {
                holder.b.imgThumb.setImageResource(R.drawable.ic_lista_placeholder)
            }
        } else {
            holder.b.imgThumb.setImageResource(R.drawable.ic_lista_placeholder)
        }

        holder.itemView.setOnClickListener { aoClicar(item, it) }
        holder.b.btnMenu.setOnClickListener { aoClicarMenu(item, it) }
    }
}
