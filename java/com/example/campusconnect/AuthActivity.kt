package com.example.campusconnect

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var nameLayout: TextInputLayout
    private lateinit var nameField: TextInputEditText
    private lateinit var emailField: TextInputEditText
    private lateinit var passwordField: TextInputEditText
    private lateinit var btnMainAction: MaterialButton
    private lateinit var btnToggleAuth: MaterialButton

    private var isLoginMode = true

    companion object {
        private const val PREFS_NAME = "AppPrefs"
        private const val KEY_LANGUAGE = "app_language"
        private const val TAG = "AuthActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        nameLayout = findViewById(R.id.nameLayout)
        nameField = findViewById(R.id.nameField)
        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        btnMainAction = findViewById(R.id.btnMainAction)
        btnToggleAuth = findViewById(R.id.btnToggleAuth)

        val btnGoogleSignIn = findViewById<Button>(R.id.btnGoogleSignIn)
        val txtForgotPassword = findViewById<TextView>(R.id.txtForgotPassword)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnGoogleSignIn.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        btnMainAction.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                performRegistration()
            }
        }

        btnToggleAuth.setOnClickListener {
            isLoginMode = !isLoginMode
            if (isLoginMode) {
                nameLayout.visibility = View.GONE
                btnMainAction.text = "Login"
                btnToggleAuth.text = "Need an account? Register"
            } else {
                nameLayout.visibility = View.VISIBLE
                btnMainAction.text = "Register"
                btnToggleAuth.text = "Already have an account? Login"
            }
        }

        txtForgotPassword.setOnClickListener {
            val options = arrayOf("Reset Password", "Change Language")
            AlertDialog.Builder(this)
                .setTitle("Options")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> resetPassword()
                        1 -> showLanguageDialog()
                    }
                }.show()
        }
    }

    private fun resetPassword() {
        val email = emailField.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show()
        } else {
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun performLogin() {
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                it.user?.let { user -> redirectToRole(user) }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun performRegistration() {
        val name = nameField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val firebaseUser = it.user ?: return@addOnSuccessListener

                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                firebaseUser.updateProfile(profileUpdates)

                val user = User(
                    uid = firebaseUser.uid,
                    email = email.toLowerCase(Locale.getDefault()),
                    name = name,
                    name_lowercase = name.toLowerCase(Locale.getDefault())
                )

                db.collection("users").document(firebaseUser.uid).set(user)
                    .addOnSuccessListener {
                        redirectToRole(firebaseUser)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to save user: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user ?: return@addOnSuccessListener

                if (authResult.additionalUserInfo?.isNewUser == true) {
                    val name = account.displayName ?: "User"
                    val email = account.email?.toLowerCase(Locale.getDefault()) ?: ""
                    val user = User(
                        uid = firebaseUser.uid,
                        email = email,
                        name = name,
                        name_lowercase = name.toLowerCase(Locale.getDefault()),
                        profileImage = account.photoUrl?.toString()
                    )

                    db.collection("users").document(firebaseUser.uid).set(user)
                        .addOnSuccessListener {
                            redirectToRole(firebaseUser)
                        }
                } else {
                    redirectToRole(firebaseUser)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Firebase auth failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun redirectToRole(user: FirebaseUser) {
        user.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val isAdmin = task.result?.claims?.get("admin") as? Boolean ?: false

                    if (isAdmin) {
                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this, HomeActivity::class.java))
                }
                finishAffinity()
            }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Xhosa", "Zulu")
        AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                when (which) {
                    0 -> setLocale("en")
                    1 -> setLocale("xh")
                    2 -> setLocale("zu")
                }
            }.show()
    }

    private fun setLocale(languageCode: String) {
        Toast.makeText(this, "Language set to $languageCode", Toast.LENGTH_SHORT).show()
    }
}