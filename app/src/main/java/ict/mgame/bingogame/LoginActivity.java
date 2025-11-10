package ict.mgame.bingogame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * LoginActivity class handles user login functionality.
 * It verifies username and password against stored values in SharedPreferences,
 * provides default credentials if none exist, and navigates to MainActivity on success.
 */
public class LoginActivity extends Activity {

    // EditText fields for username and password input
    private EditText editUsername, editPassword;
    // SharedPreferences for storing and retrieving login credentials
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the login layout
        setContentView(R.layout.login);

        // Initialize UI elements
        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        Button btnEnter = findViewById(R.id.btn_enter);

        // Get SharedPreferences instance named "login.xml"
        sharedPreferences = getSharedPreferences("login.xml", MODE_PRIVATE);

        // Set default credentials if not already present
        if (!sharedPreferences.contains("username")) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", "Tommy");
            editor.putString("password", "123456");
            editor.apply();
        }

        // Set click listener for the enter button
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get entered username and password
                String username = editUsername.getText().toString();
                String password = editPassword.getText().toString();
                // Retrieve stored credentials
                String storedUsername = sharedPreferences.getString("username", "");
                String storedPassword = sharedPreferences.getString("password", "");

                // Check if entered credentials match stored ones
                if (username.equals(storedUsername) && password.equals(storedPassword)) {
                    // Start MainActivity on successful login
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // Show error dialog on incorrect credentials
                    new AlertDialog.Builder(LoginActivity.this)
                            .setMessage("You data is incorrect! Enter again!!")
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
        });
    }
}