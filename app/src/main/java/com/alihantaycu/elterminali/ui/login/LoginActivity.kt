package com.alihantaycu.elterminali.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alihantaycu.elterminali.databinding.ActivityLoginBinding
import com.alihantaycu.elterminali.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLoginButton()
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (validateInput(email, password)) {
                if (authenticateUser(email, password)) {
                    navigateToMainActivity()
                } else {
                    showLoginError()
                }
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "E-posta alanı boş bırakılamaz"
            return false
        }
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Şifre alanı boş bırakılamaz"
            return false
        }
        return true
    }

    private fun authenticateUser(email: String, password: String): Boolean {
        // Gerçek bir uygulamada, bu işlem bir API çağrısı veya yerel veritabanı sorgusu olabilir
        // Şimdilik basit bir kontrol yapıyoruz
        return email == "admin@example.com" && password == "password123"
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // LoginActivity'yi kapat
    }

    private fun showLoginError() {
        Toast.makeText(this, "Geçersiz e-posta veya şifre", Toast.LENGTH_SHORT).show()
    }
}