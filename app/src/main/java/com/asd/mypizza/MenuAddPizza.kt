package com.asd.mypizza

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.asd.mypizza.adapter.IngredientsAdapter
import com.asd.mypizza.data.models.Ingredient
import com.asd.mypizza.data.models.IngredientItem
import com.asd.mypizza.databinding.ActivityMenuAddPizzaBinding
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

class MenuAddPizza : AppCompatActivity() {

    private lateinit var binding: ActivityMenuAddPizzaBinding
    private lateinit var ingredientsAdapter: IngredientsAdapter

    private val ingredientList = mutableListOf<IngredientItem>()
    private val firestore = FirebaseFirestore.getInstance()

    private val storage = FirebaseStorage.getInstance()
    private var imageUri: Uri? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.ivPizzaImage.setImageURI(imageUri)
        } else {
            Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuAddPizzaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        ingredientsAdapter = IngredientsAdapter(ingredientList){_,_->}
        binding.rvIngredients.layoutManager = LinearLayoutManager(this)
        binding.rvIngredients.adapter = ingredientsAdapter

        binding.btnUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            selectImageLauncher.launch(intent)
        }
        // Ingredient qo‘shish tugmasiga listener
        binding.btnAddIngredient.setOnClickListener {
            val ingredient = binding.etIngredient.text.toString().trim()
            val price = binding.etIngredientPrice.text.toString().toIntOrNull()
            if (ingredient.isNotEmpty() && price != null) {
                ingredientList.add(IngredientItem(name = ingredient, price = price))
                ingredientsAdapter.notifyDataSetChanged()
                binding.etIngredient.text.clear()
            } else {
                Toast.makeText(this, "Please enter an ingredient", Toast.LENGTH_SHORT).show()
            }
        }

        // Pitsani menyuga qo‘shish tugmasiga listener
        binding.btnAddPizza.setOnClickListener {
            uploadPizzaImageAndSave()
        }
    }

    private fun uploadPizzaImageAndSave() {
        val name = binding.etPizzaName.text.toString().trim()
        val price25 = binding.etPizzaPrice25.text.toString().toDoubleOrNull()
        val price30 = binding.etPizzaPrice30.text.toString().toDoubleOrNull()
        val price35 = binding.etPizzaPrice35.text.toString().toDoubleOrNull()
        if (name.isEmpty() || price25 == null ||price30 == null ||price35 == null || imageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }
        val sizes = mapOf(
            "25 cm" to price25,
            "30 cm" to price30,
            "35 cm" to price35
        )

        // Generate unique filename for the image
        val imageRef = storage.reference.child("pizzas/${UUID.randomUUID()}.jpg")

        imageUri?.let { uri ->
            try {
                // Rasmni siqish
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val compressedImage = compressBitmap(bitmap)

                // Compressed bitmapni Firebase Storage'ga yuklash
                val baos = ByteArrayOutputStream()
                compressedImage.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val imageData = baos.toByteArray()

                val uploadTask = imageRef.putBytes(imageData)
                uploadTask.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        savePizzaToFirestore(name, sizes, downloadUri.toString())
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to process image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val quality = 80
        val width = 1024 // Maksimal kenglik
        val height = (bitmap.height * (1024.0 / bitmap.width)).toInt() // Proportsiyani saqlash

        return Bitmap.createScaledBitmap(bitmap, width, height, true).also {
            val baos = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        }
    }

    private fun savePizzaToFirestore(name: String, price: Map<String, Double>, imageUrl: String) {
        val pizza = hashMapOf(
            "name" to name,
            "imageUrl" to imageUrl,
            "sizes" to price,
            "ingredients" to ingredientList
        )

        firestore.collection("Menus")
            .add(pizza)
            .addOnSuccessListener {
                Toast.makeText(this, "Pizza added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}