package com.asd.mypizza.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asd.mypizza.R
import com.asd.mypizza.data.models.Pizza
import com.bumptech.glide.Glide

class PizzaAdapter(private val pizzas: List<Pizza>,private val listener: OnPizzaClickListener) :
    RecyclerView.Adapter<PizzaAdapter.PizzaViewHolder>() {


    interface OnPizzaClickListener {
        fun onPizzaClick(pizza: Pizza)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PizzaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return PizzaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PizzaViewHolder, position: Int) {
        val pizza = pizzas[position]
        holder.bind(pizza)
    }

    override fun getItemCount(): Int = pizzas.size

    inner class PizzaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pizzaName: TextView = itemView.findViewById(R.id.textView)
        private val pizzaPrice: TextView = itemView.findViewById(R.id.tvPizzaPrice)
        private val pizzaImage: ImageView = itemView.findViewById(R.id.imageView)
//        private val pizzaIngredients: TextView = itemView.findViewById(R.id.tvPizzaIngredients)

        @SuppressLint("SetTextI18n")
        fun bind(pizza: Pizza) {
            pizzaName.text = pizza.name
            pizzaPrice.text = "Price: $${pizza.sizes["25 cm"]}"
//            pizzaIngredients.text = "Ingredients: ${pizza.ingredients.joinToString(", ")}"
            Glide.with(itemView.context).load(pizza.imageUrl).into(pizzaImage)

            itemView.setOnClickListener {
                listener.onPizzaClick(pizza)
            }
        }
    }
}