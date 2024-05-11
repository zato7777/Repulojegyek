package com.example.repulojegyek;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.repulojegyek.DataClasses.Flight;
import com.example.repulojegyek.DataClasses.PlaneType;
import com.example.repulojegyek.DataClasses.Ticket;
import com.example.repulojegyek.DataClasses.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class TicketBookingActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    int tag = 1;
    private String FLIGHT_ID;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private Flight flight;
    private TicketBookingAdapter ticketBookingAdapter;
    private FirebaseFirestore firestore;
    private final int gridNumber = 1;
    private String username;
    private boolean bookingFailure = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ticket_booking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            finish();
        }

        FLIGHT_ID = getIntent().getStringExtra("FLIGHT_ID");

        if (FLIGHT_ID == null) {
            finish();
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.ticketBookingRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));

        firestore = FirebaseFirestore.getInstance();

        queryTicketBookingData();
    }

    private void queryTicketBookingData() {
        CollectionReference flightsCollection = firestore.collection("flights");

        flightsCollection.whereEqualTo("flightId", FLIGHT_ID).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                        flight = documentSnapshot.toObject(Flight.class);
                    }

                    ticketBookingAdapter = new TicketBookingAdapter(this, flight);
                    recyclerView.setAdapter(ticketBookingAdapter);
                    ticketBookingAdapter.notifyDataSetChanged();
                });

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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.ticket_booking_menu, menu);
        return true;
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
            Intent intent = new Intent(this, TicketsActivity.class);
            intent.putExtra("INTENT_KEY", MainActivity.INTENT_KEY);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.profile) {
            Intent intent2 = new Intent(this, ProfileActivity.class);
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

    public void booking(int ticketCount, PlaneType planeType, String flightId, ArrayList<String> bookedSeats) {
        String currentUserEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        String[] seats = generateSeatStrings(ticketCount, planeType, bookedSeats);

        firestore.collection("users").whereEqualTo("email", currentUserEmail).get()
                .addOnSuccessListener(queryDS -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDS) {
                        User user = documentSnapshot.toObject(User.class);
                        username = user.getUsername();
                        for (int i = 0; i < ticketCount; i++) {
                            String ticketId = generateTicketId();
                            Ticket ticket = new Ticket(ticketId, username, flightId, seats[i]);
                            firestore.collection("tickets").add(ticket).addOnFailureListener(documentSnapshots -> {
                                Toast.makeText(TicketBookingActivity.this, "Hiba történt a jegyfoglalás közben!", Toast.LENGTH_SHORT).show();
                                bookingFailure = true;
                            });
                        }
                        if (!bookingFailure) {
                            Toast.makeText(TicketBookingActivity.this, "A jegyfoglalás sikeres!", Toast.LENGTH_SHORT).show();
                            backToFlightsActivity();
                        }
                    }
                });
    }

    private void backToFlightsActivity() {
        Intent intent = new Intent(this, FlightsActivity.class);
        startActivity(intent);
    }

    private String generateTicketId() {
        int length = 8;
        Random random = new Random();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            builder.append(randomChar);
        }

        return builder.toString();
    }

    private String[] generateSeatStrings(int ticketCount, PlaneType planeType, ArrayList<String> bookedSeats) {
        ArrayList<String> availableSeats = new ArrayList<>();

        for (int i = 1; i <= planeType.getRowCount(); i++) {
            for (int j = 0; j < planeType.getColumnCount(); j++) {
                String seat = i + String.valueOf((char) ('A' + j));

                if (!isSeatBooked(seat, bookedSeats)) {
                    availableSeats.add(seat);
                }
            }
        }

        String[] seats = new String[ticketCount];

        for (int i = 0; i < ticketCount; i++) {
            seats[i] = availableSeats.get(i);
        }

        return seats;
    }
    private static boolean isSeatBooked(String seat, ArrayList<String> bookedSeats) {
        for (String bookedSeat : bookedSeats) {
            if (bookedSeat.equals(seat)) {
                return true;
            }
        }
        return false;
    }


}