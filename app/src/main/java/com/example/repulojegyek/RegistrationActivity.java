package com.example.repulojegyek;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.repulojegyek.DataClasses.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {
    EditText usernameEditText;
    EditText emailEditText;
    EditText passwordEditText;
    EditText passwordConfirmationEditText;

    private ArrayList<User> usersList;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private CollectionReference usersCollection;
    private boolean createUserSuccess = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int intentKey = getIntent().getIntExtra("INTENT_KEY", 0);

        if (intentKey != MainActivity.INTENT_KEY) {
            finish();
        }

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordConfirmationEditText = findViewById(R.id.passwordConfirmationEditText);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        usersCollection = firestore.collection("users");
        usersList = new ArrayList<>();
    }

    public void register(View view) {
        String username = usernameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordConfirmation = passwordConfirmationEditText.getText().toString();

        if (!password.equals(passwordConfirmation)) {
            Toast.makeText(RegistrationActivity.this, "A jelszavak nem egyeznek meg!", Toast.LENGTH_SHORT).show();
            passwordEditText.setBackgroundColor(Color.RED);
            passwordConfirmationEditText.setBackgroundColor(Color.RED);
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(RegistrationActivity.this, "A megadott jelszó túl rövid! A jelszónak legalább 6 karakter hosszúságúnak kell lennie.", Toast.LENGTH_SHORT).show();
            passwordEditText.setBackgroundColor(Color.RED);
            return;
        }

        if (username.length() > 30) {
            Toast.makeText(RegistrationActivity.this, "A megadott felhasználónév túl hosszú! A felhasználónév legfeljebb 30 karakter hosszúságú lehet.", Toast.LENGTH_SHORT).show();
            usernameEditText.setBackgroundColor(Color.RED);
            return;
        }

        boolean success = checkUsers(username, email);

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                if (success) {
                    createUser(username, email);
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegistrationActivity.this, "A regisztáció sikertelen!", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(RegistrationActivity.this, "A regisztáció sikertelen!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void cancel(View view) {
        finish();
    }

    private boolean checkUsers(String username, String email) {
        usersList.clear();

        usersCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                        User user = documentSnapshot.toObject(User.class);
                        usersList.add(user);
                    }

                    for (User user: usersList) {
                        if (Objects.equals(user.getUsername(), username) || Objects.equals(user.getEmail(), email)) {
                            createUserSuccess = false;
                            break;
                        }
                    }

                });
        return createUserSuccess;
    }

    private void createUser(String username, String email) {
        User user = new User(username, email);
        usersCollection.add(user).addOnSuccessListener(documentSnapshots -> {
            Toast.makeText(RegistrationActivity.this, "A regisztáció sikeres!", Toast.LENGTH_SHORT).show();
        });
    }
}