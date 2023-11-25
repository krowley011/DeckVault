package com.example.deckvault

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    // Firebase elements
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // If user is signed in, go to home page. If not, go to sign in
        Handler().postDelayed({
            if (user != null) {
                val i = Intent(this, NavigationActivity::class.java)
                startActivity(i)
            } else {
                val s = Intent(this, SignInActivity::class.java)
                startActivity(s)
            }
        }, 1500)
    }

    // Deck Menu Button setup
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.deckmenu, menu)
            return true
    }

    //Deck Menu click events
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.deckMenuBTN -> {
                    // Handle the click on the menu item
                    // Perform the desired action here
                    true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
