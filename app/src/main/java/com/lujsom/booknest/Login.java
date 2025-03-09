package com.lujsom.booknest;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Login activity allows users to sign in with their email and password.
 * It handles authentication using Firebase Authentication.
 */
public class Login extends AppCompatActivity {

    private EditText email, password;
    private Button loginBtn, backToRegisterBtn;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        initializeViews(); // Setup UI elements
        setListeners(); // Attach event listeners
    }

    /**
     * Initializes UI components by finding their references in the layout.
     */
    private void initializeViews() {
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.btn_login);
        backToRegisterBtn = findViewById(R.id.back_register);
        progressBar = findViewById(R.id.progressBar);
        back = findViewById(R.id.login_back);
    }

    /**
     * Sets event listeners for buttons to handle login, navigation, and back button actions.
     */
    private void setListeners() {
        if (loginBtn != null) {
            loginBtn.setOnClickListener(v -> loginUser());
        }

        if (backToRegisterBtn != null) {
            backToRegisterBtn.setOnClickListener(v -> navigateTo(Register.class));
        }

        if (back != null) {
            back.setOnClickListener(v -> navigateTo(MainActivity.class));
        }
    }

    /**
     * Navigates to a specified activity with a smooth transition.
     *
     * @param targetActivity The activity class to navigate to.
     */
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(Login.this, targetActivity);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    /**
     * Handles the login process using Firebase Authentication.
     * Validates input fields and attempts to authenticate the user.
     */
    private void loginUser() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        if (!validateInput(userEmail, userPassword)) return;

        progressBar.setVisibility(View.VISIBLE);
        loginBtn.setEnabled(false); // Disable login button to prevent multiple clicks

        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    loginBtn.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(Login.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        navigateTo(LibraryPage.class);
                    } else {
                        handleLoginError(task.getException()); // Handle errors properly
                    }
                });
    }

    /**
     * Validates the user's email and password input.
     *
     * @param userEmail    The email entered by the user.
     * @param userPassword The password entered by the user.
     * @return True if input is valid, false otherwise.
     */
    private boolean validateInput(String userEmail, String userPassword) {
        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Email is required");
            email.requestFocus();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            email.setError("Invalid email format");
            email.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(userPassword)) {
            password.setError("Password is required");
            password.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Handles login errors and provides appropriate feedback to the user.
     *
     * @param exception The exception received during login failure.
     */
    private void handleLoginError(Exception exception) {
        String errorMessage = "Login failed. Please try again.";

        if (exception != null) {
            try {
                throw exception;
            } catch (FirebaseAuthInvalidUserException e) {
                errorMessage = "No account found with this email.";
                email.setError(errorMessage);
                email.requestFocus();
            } catch (FirebaseAuthInvalidCredentialsException e) {
                errorMessage = "Incorrect password.";
                password.setError(errorMessage);
                password.requestFocus();
            } catch (Exception e) {
                Log.e("AUTH", " Login error: " + e.getMessage());
            }
        }

        Toast.makeText(Login.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Checks if a user is already logged in when the activity starts.
     * If the user is authenticated, they are redirected to the LibraryPage.
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(Login.this, LibraryPage.class));
            finish();
        }
    }
}