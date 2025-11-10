package ict.mgame.bingogame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * ConfigActivity class allows users to update their login credentials.
 * It verifies existing credentials, updates new ones if provided, and stores them in SharedPreferences.
 * Includes a 2-second delay before closing on successful update.
 */
public class ConfigActivity extends Activity {

    // EditText fields for existing and new username/password
    private EditText existingUsername, existingPassword, newUsername, newPassword;
    // SharedPreferences for storing and retrieving credentials
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the config layout
        setContentView(R.layout.config);

        // Initialize UI elements
        existingUsername = findViewById(R.id.existing_username);
        existingPassword = findViewById(R.id.existing_password);
        newUsername = findViewById(R.id.new_username);
        newPassword = findViewById(R.id.new_password);
        Button btnConfirm = findViewById(R.id.btn_confirm);

        // Get SharedPreferences instance
        sharedPreferences = getSharedPreferences("login.xml", MODE_PRIVATE);

        // Pre-populate existing fields with stored values
        String storedUser = sharedPreferences.getString("username", "");
        String storedPass = sharedPreferences.getString("password", "");
        existingUsername.setText(storedUser);
        existingPassword.setText(storedPass);

        // Set click listener for confirm button
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get entered existing credentials
                String enteredExistingUser = existingUsername.getText().toString();
                String enteredExistingPass = existingPassword.getText().toString();
                // Retrieve stored credentials
                String storedUser = sharedPreferences.getString("username", "");
                String storedPass = sharedPreferences.getString("password", "");

                // Verify existing credentials match stored ones
                if (!enteredExistingUser.equals(storedUser) || !enteredExistingPass.equals(storedPass)) {
                    // Show error dialog if incorrect
                    new AlertDialog.Builder(ConfigActivity.this)
                            .setMessage("Existing username/password is not correct!")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                // Get new credentials
                String newUser = newUsername.getText().toString();
                String newPass = newPassword.getText().toString();

                // Check if new credentials are provided
                if (newUser.isEmpty() || newPass.isEmpty()) {
                    // Show dialog if not entered
                    new AlertDialog.Builder(ConfigActivity.this)
                            .setMessage("New username/password is not enter! Data is remained unchanged")
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    // Show success dialog
                    new AlertDialog.Builder(ConfigActivity.this)
                            .setMessage("New username/password is confirmed! Data is updated")
                            .setPositiveButton("OK", null)
                            .show();

                    // Update SharedPreferences with new credentials
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", newUser);
                    editor.putString("password", newPass);
                    editor.apply();

                    // Delay 2 seconds then finish activity
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 2000);
                }
            }
        });
    }
}