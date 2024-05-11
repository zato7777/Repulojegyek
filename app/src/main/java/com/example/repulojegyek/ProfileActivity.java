package com.example.repulojegyek;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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

public class ProfileActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    int tag = 1;
    TextView currentUsername;

    EditText usernameEditText;
    Button updateUserButton;
    private FirebaseAuth firebaseAuth;
    private ArrayList<User> usersList;
    private CollectionReference usersCollection;
    private boolean updateUserSuccess = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentUsername = findViewById(R.id.currentUsername);
        usernameEditText = findViewById(R.id.usernameChangeEditText);
        updateUserButton = findViewById(R.id.saveProfileChangesButton);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        usersCollection = firestore.collection("users");
        usersList = new ArrayList<>();

        usersCollection.whereEqualTo("email", Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail()).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                                User user = documentSnapshot.toObject(User.class);
                                currentUsername.setText(user.getUsername());
                            }
                        });

        updateUserButton.setOnClickListener(v -> updateUser());
    }

    public void updateUser() {
        String username = usernameEditText.getText().toString();

        if (username.length() > 30) {
            Toast.makeText(this, "A megadott felhasználónév túl hosszú! A felhasználónév legfeljebb 30 karakter hosszúságú lehet.", Toast.LENGTH_SHORT).show();
            usernameEditText.setBackgroundColor(Color.RED);
            return;
        }

        if (username.isEmpty()) {
            Toast.makeText(this, "Adj meg új felhasználónevet! A felhasználónévnek legalább 1 karakter hosszúnak kell lennie.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = checkUsers(username);

        if (!success){
            Toast.makeText(this, "Ez a felhasználónév már létezik!", Toast.LENGTH_SHORT).show();
            return;
        }

        usersCollection.whereEqualTo("email", Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                        String documentId = documentSnapshot.getId();
                        usersCollection.document(documentId).update("username", username)
                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                    Intent intent = new Intent(this, MainActivity.class);
                                    startActivity(intent);
                                });
                    }
                });
        }

    private boolean checkUsers(String username) {
        usersList.clear();

        usersCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                User user = documentSnapshot.toObject(User.class);
                usersList.add(user);
            }

            for (User user: usersList) {
                if (Objects.equals(user.getUsername(), username)) {
                    updateUserSuccess = false;
                    break;
                }
            }
        });
        return updateUserSuccess;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu, menu);

        return true;
    }

    public void openCamera() {
        checkUserPermission();
    }

    void checkUserPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }

        takePicture();
    }

    private void takePicture() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, tag);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    Toast.makeText(this, "Hozzáférés megtagadva!", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.flights) {
            Intent intent = new Intent(this, FlightsActivity.class);
            intent.putExtra("INTENT_KEY", MainActivity.INTENT_KEY);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.ownTickets) {
            Intent intent2 = new Intent(this, TicketsActivity.class);
            intent2.putExtra("INTENT_KEY", MainActivity.INTENT_KEY);
            startActivity(intent2);
            return true;
        } else if (itemId == R.id.openCamera) {
            openCamera();
            return true;
        } else if (itemId == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent3 = new Intent(this, MainActivity.class);
            startActivity(intent3);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
}