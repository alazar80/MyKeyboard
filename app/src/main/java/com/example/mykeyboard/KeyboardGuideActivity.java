package com.example.mykeyboard;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

public class KeyboardGuideActivity extends AppCompatActivity {

    private Button btnEnableKeyboard;
    private Button btnChooseKeyboard;
    private TextView stepText;

    private final String keyboardId = "com.example.mykeyboard/.AmharicKeyboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard_guide);
        btnEnableKeyboard = findViewById(R.id.btnEnableKeyboard);
        btnChooseKeyboard = findViewById(R.id.btnChooseKeyboard);
        stepText = findViewById(R.id.stepText);
        btnEnableKeyboard.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
        });

        btnChooseKeyboard.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showInputMethodPicker();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isKeyboardEnabled()) {
            btnChooseKeyboard.setEnabled(true);
            stepText.setText("Step 2: Choose Amharic Keyboard as your input method");
        } else {
            btnChooseKeyboard.setEnabled(false);
            stepText.setText("Step 1: Activate the Amharic Keyboard");
        }
    }

    private boolean isKeyboardEnabled() {
        String enabled = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS);
        return enabled != null && enabled.contains(keyboardId);
    }
}
