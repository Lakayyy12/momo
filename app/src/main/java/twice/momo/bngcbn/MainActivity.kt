package twice.momo.bngcbn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import twice.momo.bngcbn.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val connection = NoConnection()
    private var checknet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        networkConnectionCheck()

        binding.bong1.apply {
            setOnClickListener {
                val intent = Intent(context, BongWeb::class.java)
                intent.putExtra("title", "webview")
                startActivity(intent)
            }
        }

        binding.bong2.apply {
            setOnClickListener {
                val intent = Intent(context, BongWeb::class.java)
                intent.putExtra("title", "webview2")
                startActivity(intent)
            }
        }

        // Fetch and update button text
        fetchValue()
    }
    private fun networkConnectionCheck() {
        checknet = connection.connectionError(this)
        if (checknet) {
            BongWeb()
        } else {
            Toast.makeText(this, "PLEASE CHECK YOUR INTERNET CONNECTION!!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun fetchValue() {
        firestore.collection("Kalistofu")
            .document("FCB8BONGDACOBAN")
            .get()
            .addOnSuccessListener { documentSnapshot: DocumentSnapshot? ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val isValueTrue = documentSnapshot.getBoolean("ban3") ?: false
                    // Update button text using the extension function
                    binding.updateButtonText(isValueTrue)
                }
            }
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}

fun ActivityMainBinding.updateButtonText(isValueTrue: Boolean) {
    this.bong1.text = if (isValueTrue) {
        "ĐĂNG KÝ"
    } else {
        this.root.context.getString(R.string.about)
    }
    this.bong2.text = if (isValueTrue) {
        "ĐĂNG NHẬP"
    } else {
        this.root.context.getString(R.string.mg)
    }
}