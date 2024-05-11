package com.example.repulojegyek;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public static final int INTENT_KEY = new Random().nextInt();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    public void openLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("INTENT_KEY", INTENT_KEY);
        startActivity(intent);
    }

    public void openRegistration(View view) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.putExtra("INTENT_KEY", INTENT_KEY);
        startActivity(intent);
    }


}