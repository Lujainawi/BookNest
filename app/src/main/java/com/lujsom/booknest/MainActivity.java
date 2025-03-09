package com.lujsom.booknest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * MainActivity serves as the entry point of the application.
 * It allows users to navigate to the Login, Register, and About pages.
 * If a user is already logged in, they are redirected to the LibraryPage.
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button btnLogin, btnRegister, btnAbout;

    /**
     * Called when the activity is first created.
     * Initializes Firebase authentication, checks if a user is already logged in,
     * and sets up UI components.
     *
     * @param savedInstanceState The saved state of the activity, if available.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase authentication instance
        mAuth = FirebaseAuth.getInstance();
        checkUserLogin(); // Redirect to LibraryPage if the user is already logged in

        initializeViews(); // Set up UI components
        setButtonListeners(); // Set up button click listeners
    }

    /**
     * Initializes UI components by finding their references in the layout.
     */
    private void initializeViews() {
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnAbout = findViewById(R.id.btn_about);
    }

    /**
     * Sets event listeners for buttons to handle navigation.
     */
    private void setButtonListeners() {
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> navigateTo(Login.class, android.R.anim.fade_in, android.R.anim.fade_out));
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> navigateTo(Register.class, android.R.anim.slide_in_left, android.R.anim.slide_out_right));
        }

        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> navigateTo(About.class, android.R.anim.fade_in, android.R.anim.fade_out));
        }
    }

    /**
     * Navigates to the specified activity with transition animations.
     *
     * @param targetActivity The target activity class.
     * @param enterAnim      Animation for entering the new activity.
     * @param exitAnim       Animation for exiting the current activity.
     */
    private void navigateTo(Class<?> targetActivity, int enterAnim, int exitAnim) {
        Intent intent = new Intent(MainActivity.this, targetActivity);
        startActivity(intent);
        overridePendingTransition(enterAnim, exitAnim);
    }

    /**
     * Checks if a user is already logged in.
     * If so, redirects them to the LibraryPage and prevents returning to this screen.
     */
    private void checkUserLogin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(MainActivity.this, LibraryPage.class));
            finishAffinity(); // Closes all previous activities to prevent returning to the login screen
        }
    }
}