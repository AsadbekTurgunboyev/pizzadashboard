package com.asd.mypizza.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asd.mypizza.R
import com.asd.mypizza.data.models.IngredientItem
import com.google.android.material.checkbox.MaterialCheckBox

class IngredientsAdapter(private val ingredients: List<IngredientItem>,  private val onIngredientChange: (IngredientItem, Boolean) -> Unit) :
    RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredients, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    override fun getItemCount(): Int = ingredients.size

    inner class IngredientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var isCheck = false
        private val ingredientName: TextView = view.findViewById(R.id.ingredientsName)
        private val checkBox: MaterialCheckBox = view.findViewById(R.id.ingredient_add_button)

        fun bind(ingredientItem: IngredientItem) {
            ingredientName.text = ingredientItem.name
            itemView.setOnClickListener {
                checkBox.isChecked = !isCheck
                onIngredientChange(ingredientItem,isCheck)
                isCheck =! isCheck
            }
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onIngredientChange(ingredientItem, isChecked)
            }
        }
    }
}
