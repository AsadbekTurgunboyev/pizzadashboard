package com.asd.mypizza

import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.asd.mypizza.adapter.IngredientsAdapter
import com.asd.mypizza.adapter.PizzaAdapter
import com.asd.mypizza.data.models.IngredientItem
import com.asd.mypizza.data.models.Pizza
import com.asd.mypizza.databinding.ActivityMainBinding
import com.asd.mypizza.databinding.PizzaDetailsDialogBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(),PizzaAdapter.OnPizzaClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pizzaAdapter: PizzaAdapter
    private val selectedIngredients = mutableSetOf<IngredientItem>()

    private val firestore = FirebaseFirestore.getInstance()
    private val pizzaList = mutableListOf<Pizza>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupRecyclerView()
        fetchPizzaList()
    }

    private fun setupRecyclerView() {
        pizzaAdapter = PizzaAdapter(pizzaList,this)
        binding.rvPizzaList.layoutManager = GridLayoutManager(this,4)
        binding.rvPizzaList.adapter = pizzaAdapter
    }

    private fun fetchPizzaList() {
        firestore.collection("Menus")
            .get()
            .addOnSuccessListener { documents ->
                pizzaList.clear()
                for (document in documents) {
                    val pizza = document.toObject(Pizza::class.java)
                    pizzaList.add(pizza) // Har bir pitsani ro'yxatga qo'shish
                }
                pizzaAdapter.notifyDataSetChanged() // RecyclerView adapterini yangilash
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }    }

    private fun showPizzaDetailDialog(pizza: Pizza) {
        val dialogBinding = PizzaDetailsDialogBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.size25.isSelected = true
        dialogBinding.size25.setOnClickListener {
            dialogBinding.size30.isSelected = false
            dialogBinding.size35.isSelected = false
            it.isSelected = true
        }

        dialogBinding.size30.setOnClickListener {
            dialogBinding.size25.isSelected = false
            dialogBinding.size35.isSelected = false
            it.isSelected = true
        }

        dialogBinding.size35.setOnClickListener {
            dialogBinding.size25.isSelected = false
            dialogBinding.size30.isSelected = false
            it.isSelected = true
        }

        Glide.with(this).load(pizza.imageUrl).into(dialogBinding.imageOrder)
        dialogBinding.titleOrder.text = pizza.name

        dialogBinding.showIngredientRv.adapter = IngredientsAdapter(pizza.ingredients){ingredient, isChecked ->
            if (isChecked) {
                selectedIngredients.add(ingredient)
            } else {
                selectedIngredients.remove(ingredient)
            }
            updateTotalPrice()

        }
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setGravity(Gravity.END)
            attributes.windowAnimations = R.style.DialogAnimation
        }

        dialog.setCancelable(true)
        dialog.show()

        dialogBinding.bottomRoot.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSaveOrder.setOnClickListener {
            val selectedSize = when {
                dialogBinding.size25.isSelected -> "25 cm"
                dialogBinding.size30.isSelected -> "30 cm"
                dialogBinding.size35.isSelected -> "35 cm"
                else -> "25 cm" // Default qiymat
            }

//            saveOrderToFirestore(phoneNumber, pizza, selectedSize)
            dialog.dismiss()
        }
    }

    private fun updateTotalPrice() {

    }


    override fun onPizzaClick(pizza: Pizza) {
        showPizzaDetailDialog(pizza)
    }

    private fun saveOrderToFirestore(phoneNumber: String, pizza: Pizza, size: String) {

        val orderData = hashMapOf(
            "phoneNumber" to phoneNumber,
            "pizzaName" to pizza.name,
            "size" to size,
            "ingredients" to pizza.ingredients,
            "imageUrl" to pizza.imageUrl,
//            "price" to pizza.price,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("orders")
            .add(orderData)
            .addOnSuccessListener {
                Toast.makeText(this, "Order saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save order: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}