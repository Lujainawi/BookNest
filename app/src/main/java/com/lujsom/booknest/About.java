package com.lujsom.booknest;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The About activity displays information about the application.
 * It includes a back button to return to the previous screen.
 */

public class About extends AppCompatActivity {
    private Button btnBack;  // Button to navigate back to the previous screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initializeViews();
        setListeners();
    }

    /**
     * Initializes the UI components by finding their references in the layout.
     */
    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
    }

    /**
     * Sets the click listener for the back button.
     * When clicked, it finishes the activity and returns to the previous screen.
     */
    private void setListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}