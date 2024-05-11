package com.example.repulojegyek;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.repulojegyek.DataClasses.Flight;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.Manifest;
import android.widget.Toast;

import java.util.ArrayList;

public class FlightsActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    int tag = 1;
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private ArrayList<Flight> flightsList;
    private FlightsAdapter flightsAdapter;
    private FirebaseFirestore firestore;
    private CollectionReference flightsCollection;
    private final int gridNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flights);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            finish();
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));

        flightsList = new ArrayList<>();

        firestore = FirebaseFirestore.getInstance();
        flightsCollection = firestore.collection("flights");

        queryFlightsData();
    }

    private void queryFlightsData() {
        flightsList.clear();

        //flight-ok indulas szerinti sorrendben lekerdezes
        flightsCollection.orderBy("departure", Query.Direction.ASCENDING).limit(10).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots) {
                        Flight flight = documentSnapshot.toObject(Flight.class);
                        flightsList.add(flight);
                    }

                    if (flightsList.isEmpty()) {
                        queryFlightsData();
                    }
                    flightsAdapter = new FlightsAdapter(this, flightsList);
                    recyclerView.setAdapter(flightsAdapter);
                    flightsAdapter.notifyDataSetChanged();
                });
    }


    public void openCamera() {
        checkUserPermission();
    }

    void checkUserPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
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
        getMenuInflater().inflate(R.menu.flights_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                flightsAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.ownTickets) {
            Intent intent = new Intent(this, TicketsActivity.class);
            intent.putExtra("INTENT_KEY", MainActivity.INTENT_KEY);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.profile) {
            Intent intent2 = new Intent(this, ProfileActivity.class);
            intent2.putExtra("INTENT_KEY", MainActivity.INTENT_KEY);
            startActivity(intent2);
            return true;
        }else if (itemId == R.id.openCamera) {
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

    public void booking(String flightId) {
        Intent intent = new Intent(this, TicketBookingActivity.class);
        intent.putExtra("FLIGHT_ID", flightId);
        startActivity(intent);
    }
}