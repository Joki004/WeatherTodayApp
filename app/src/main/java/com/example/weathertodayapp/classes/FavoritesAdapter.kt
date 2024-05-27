package com.example.weathertodayapp.classes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weathertodayapp.R

class FavoritesAdapter(
    private val context: Context,
    private val favorites: List<String>,
    private val loadFavorite: (String) -> Unit,
    private val removeFavorite: (Int) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.favorite_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val location = favorites[position]
        holder.bind(location, loadFavorite, removeFavorite)
    }

    override fun getItemCount(): Int = favorites.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val locationName: TextView = itemView.findViewById(R.id.favorite_name)
        private val removeIcon: ImageView = itemView.findViewById(R.id.trash_icon)

        fun bind(location: String, loadFavorite: (String) -> Unit, removeFavorite: (Int) -> Unit) {
            locationName.text = location
            itemView.setOnClickListener { loadFavorite(location) }
            removeIcon.setOnClickListener { removeFavorite(adapterPosition) }
        }
    }
}