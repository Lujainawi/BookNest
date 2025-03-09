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
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Register activity handles user registration with Firebase Authentication and Firestore.
 * Users can enter their details, and the system validates the input before creating an account.
 */
public class Register extends AppCompatActivity {

    private EditText username, email, password, confirmPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private Button registerBtn;
    private ImageButton back;
    private TextView loginRedirect;
    private static final String TAG_FIRESTORE = "FIRESTORE";
    private static final String TAG_AUTH = "AUTH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews(); // Setup UI elements
        setListeners(); // Attach event listeners
    }

    /**
     * Initializes UI components by finding their references in the layout.
     */
    private void initializeViews() {
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        registerBtn = findViewById(R.id.btn_register);
        loginRedirect = findViewById(R.id.login_redirect);
        progressBar = findViewById(R.id.progressBar);
        back = findViewById(R.id.register_back);
    }

    /**
     * Sets event listeners for buttons to handle user interactions.
     */
    private void setListeners() {
        if (registerBtn != null) {
            registerBtn.setOnClickListener(v -> registerUser());
        }

        if (loginRedirect != null) {
            loginRedirect.setOnClickListener(v -> navigateTo(Login.class));
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
        Intent intent = new Intent(Register.this, targetActivity);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    /**
     * Handles user registration by validating input and creating a Firebase account.
     */
    private void registerUser() {
        String userName = username.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();
        String userConfirmPassword = confirmPassword.getText().toString().trim();

        if (!validateInput(userName, userEmail, userPassword, userConfirmPassword)) return;

        progressBar.setVisibility(View.VISIBLE);
        registerBtn.setEnabled(false); // Disable button to prevent multiple clicks

        // Create a new user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    registerBtn.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            saveUserToFirestore(firebaseUser.getUid(), userName, userEmail);
                            reloadUserData(firebaseUser, userName);
                        } else {
                            Log.e(TAG_AUTH, " FirebaseUser is NULL after registration!");
                        }
                    } else {
                        handleRegistrationError(task.getException());
                    }
                });
    }

    /**
     * Saves the registered user's details to Firestore.
     *
     * @param userId    The unique ID of the user.
     * @param userName  The username of the user.
     * @param userEmail The email of the user.
     */
    private void saveUserToFirestore(String userId, String userName, String userEmail) {
        User newUser = new User(userId, userName, userEmail);
        db.collection("users").document(userId)
                .set(newUser)
                .addOnSuccessListener(aVoid -> Log.d(TAG_FIRESTORE, " User profile added successfully!"))
                .addOnFailureListener(e -> Log.e(TAG_FIRESTORE, " Error adding user profile", e));
    }

    /**
     * Reloads the user data to ensure the account is up to date after registration.
     *
     * @param firebaseUser The FirebaseUser object of the newly registered user.
     * @param userName     The username of the user.
     */
    private void reloadUserData(FirebaseUser firebaseUser, String userName) {
        firebaseUser.reload().addOnCompleteListener(reloadTask -> {
            if (reloadTask.isSuccessful()) {
                showToast("Welcome, " + userName + "!");
                navigateTo(LibraryPage.class);
            } else {
                Log.e(TAG_AUTH, " Failed to reload user data.");
            }
        });
    }

    /**
     * Handles registration errors and provides appropriate feedback to the user.
     *
     * @param exception The exception received during registration failure.
     */
    private void handleRegistrationError(Exception exception) {
        String errorMessage = "Registration failed. Please try again.";

        if (exception != null) {
            try {
                throw exception;
            } catch (FirebaseAuthWeakPasswordException e) {
                errorMessage = "Password is too weak!";
            } catch (FirebaseAuthInvalidCredentialsException e) {
                errorMessage = "Invalid email format!";
            } catch (FirebaseAuthUserCollisionException e) {
                errorMessage = "This email is already registered!";
            } catch (Exception e) {
                errorMessage = e.getMessage();
            }
        }

        showToast(errorMessage);
    }

    /**
     * Validates user input before registration.
     *
     * @param userName           The entered username.
     * @param userEmail          The entered email.
     * @param userPassword       The entered password.
     * @param userConfirmPassword The confirmed password.
     * @return True if input is valid, false otherwise.
     */
    private boolean validateInput(String userName, String userEmail, String userPassword, String userConfirmPassword) {
        // Username validation
        if (TextUtils.isEmpty(userName)) {
            username.setError("Username is required");
            username.requestFocus();
            return false;
        }

        // Email validation
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
        if (!userEmail.endsWith("@gmail.com")) {
            email.setError("Email must end with @gmail.com");
            email.requestFocus();
            return false;
        }

        // Password validation
        if (TextUtils.isEmpty(userPassword)) {
            password.setError("Password is required");
            password.requestFocus();
            return false;
        }
        if (userPassword.length() < 8) {
            password.setError("Password must be at least 8 characters");
            password.requestFocus();
            return false;
        }
        if (!userPassword.matches(".*[A-Z].*")) {
            password.setError("Password must contain at least one uppercase letter");
            password.requestFocus();
            return false;
        }
        if (!userPassword.matches(".*[a-z].*")) {
            password.setError("Password must contain at least one lowercase letter");
            password.requestFocus();
            return false;
        }
        if (!userPassword.matches(".*\\d.*")) {
            password.setError("Password must contain at least one number");
            password.requestFocus();
            return false;
        }
        if (!userPassword.matches(".*[!@#$%^&*].*")) {
            password.setError("Password must contain at least one special character (!@#$%^&*)");
            password.requestFocus();
            return false;
        }
        if (!userPassword.equals(userConfirmPassword)) {
            confirmPassword.setError("Passwords do not match");
            confirmPassword.requestFocus();
            return false;
        }

        return true; // if all validations passed successfully
    }

    /**
     * Displays a toast message to the user.
     *
     * @param message The message to be displayed in the toast.
     */
    private void showToast(String message) {
        Toast.makeText(Register.this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Called when the activity becomes visible to the user.
     * Checks if a user is already logged in and redirects them to the LibraryPage.
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            navigateTo(LibraryPage.class);
        }
    }

}