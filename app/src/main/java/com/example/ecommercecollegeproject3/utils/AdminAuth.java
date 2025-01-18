package com.example.ecommercecollegeproject3.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ecommercecollegeproject3.R;

public class AdminAuth {
    private static final String ADMIN_PASSWORD = "bob123";
    private static boolean isAuthenticated = false;

    public static void checkAdminPassword(Context context, Runnable onSuccess) {
        if (isAuthenticated) {
            onSuccess.run();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_admin_password, null);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);

        builder.setView(dialogView)
                .setPositiveButton("Login", (dialog, which) -> {
                    String enteredPassword = passwordInput.getText().toString();
                    if (enteredPassword.equals(ADMIN_PASSWORD)) {
                        isAuthenticated = true;
                        onSuccess.run();
                    } else {
                        Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    public static void logout() {
        isAuthenticated = false;
    }

    public static boolean isAdminAuthenticated() {
        return isAuthenticated;
    }
} 