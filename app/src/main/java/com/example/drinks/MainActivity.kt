package com.example.drinks

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.drinks.R
import org.json.JSONException

class MainActivity: AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etCocktailName = findViewById<EditText>(R.id.etCocktailName)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        // Inicializar Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this)

        btnSearch.setOnClickListener {
            val cocktailName = etCocktailName.text.toString().trim()
            if (cocktailName.isNotEmpty()) {
                fetchCocktailInfo(cocktailName, tvResult)
            } else {
                Toast.makeText(this, "Por favor ingresa un nombre de cóctel", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchCocktailInfo(cocktailName: String, tvResult: TextView) {
        val url = "https://www.thecocktaildb.com/api/json/v1/1/search.php?s=$cocktailName"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val drinksArray = response.getJSONArray("drinks")
                    if (drinksArray != null && drinksArray.length() > 0) {
                        // Seleccionar un cóctel aleatorio
                        val randomIndex = (0 until drinksArray.length()).random()
                        val drink = drinksArray.getJSONObject(randomIndex)

                        val name = drink.getString("strDrink")
                        val instructions = drink.getString("strInstructions")

                        // Obtener algunos ingredientes (hasta 5)
                        val ingredients = mutableListOf<String>()
                        for (i in 1..5) {
                            val ingredient = drink.optString("strIngredient$i", null)
                            if (!ingredient.isNullOrEmpty()) {
                                ingredients.add(ingredient)
                            }
                        }

                        // Mostrar información en el TextView
                        val resultText = """
                            Nombre: $name
                            Ingredientes: ${ingredients.joinToString(", ")}
                            Instrucciones: $instructions
                        """.trimIndent()

                        tvResult.text = resultText
                    } else {
                        tvResult.text = "No se encontraron cócteles con ese nombre."
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    tvResult.text = "Error al procesar los datos."
                }
            },
            { error ->
                error.printStackTrace()
                tvResult.text = "Error al realizar la solicitud."
            }
        )

        // Agregar la solicitud a la cola
        requestQueue.add(jsonObjectRequest)
    }

    override fun onStop() {
        super.onStop()
        requestQueue.cancelAll(this)
    }
}
