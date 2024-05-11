package com.example.repulojegyek;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.repulojegyek.DataClasses.Airport;
import com.example.repulojegyek.DataClasses.Flight;
import com.example.repulojegyek.DataClasses.PlaneType;
import com.example.repulojegyek.DataClasses.Ticket;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TicketBookingAdapter extends RecyclerView.Adapter<TicketBookingAdapter.ViewHolder>{
    private Flight flight;
    private Context context;
    private PlaneType planeType;
    private int bookedTicketCount;
    private ArrayList<String> bookedSeats = new ArrayList<>();

    TicketBookingAdapter(Context context, Flight flight) {
        this.flight = flight;
        this.context = context;
    }

    @NonNull
    @Override
    public TicketBookingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TicketBookingAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.flight_ticket, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TicketBookingAdapter.ViewHolder holder, int position) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference airportsCollection = firestore.collection("airports");
        CollectionReference planeTypesCollection = firestore.collection("planeTypes");
        CollectionReference ticketsCollection = firestore.collection("tickets");

        String fromAirportCode = flight.getFromAirportCode();
        airportsCollection.whereEqualTo("code", fromAirportCode)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Airport fromAirport = documentSnapshot.toObject(Airport.class);
                        String toAirportCode = flight.getToAirportCode();
                        airportsCollection.whereEqualTo("code", toAirportCode)
                                .get().addOnSuccessListener(queryDocumentSnapshots2 -> {
                                    for (QueryDocumentSnapshot documentSnapshot2 : queryDocumentSnapshots2) {
                                        Airport toAirport = documentSnapshot2.toObject(Airport.class);
                                        String typeName = flight.getPlaneTypeName();
                                        planeTypesCollection.whereEqualTo("typeName", typeName)
                                                .get().addOnSuccessListener(queryDocumentSnapshots3 -> {
                                                    for (QueryDocumentSnapshot documentSnapshot3 : queryDocumentSnapshots3) {
                                                        planeType = documentSnapshot3.toObject(PlaneType.class);
                                                        ticketsCollection.whereEqualTo("flightId", flight.getFlightId())
                                                                .get().addOnSuccessListener(queryDocumentSnapshots4 -> {
                                                                    for (QueryDocumentSnapshot documentSnapshot4: queryDocumentSnapshots4) {
                                                                        Ticket ticket = documentSnapshot4.toObject(Ticket.class);
                                                                        String seat = ticket.getSeat();
                                                                        bookedSeats.add(seat);
                                                                        bookedTicketCount++;
                                                                    }
                                                                    holder.bindTo(flight, fromAirport, toAirport, planeType, bookedTicketCount);
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView flightIdText;
        private TextView planeTypeText;
        private TextView fromAirportText;
        private TextView toAirportText;
        private TextView fromAirportName;
        private TextView toAirportName;
        private TextView departureDatetimeText;
        private TextView arrivalDatetimeText;
        private TextView remainingSeatCountText;
        private EditText ticketCountText;
        private Button ticketBookingButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            flightIdText = itemView.findViewById(R.id.ticketBookingFlightId);
            planeTypeText = itemView.findViewById(R.id.ticketBookingPlaneType);
            fromAirportText = itemView.findViewById(R.id.ticketBookingFromAirport);
            toAirportText = itemView.findViewById(R.id.ticketBookingToAirport);
            fromAirportName = itemView.findViewById(R.id.ticketBookingFromAirportName);
            toAirportName = itemView.findViewById(R.id.ticketBookingToAirportName);
            departureDatetimeText = itemView.findViewById(R.id.ticketBookingDepartureDatetime);
            arrivalDatetimeText = itemView.findViewById(R.id.ticketBookingArrivalDatetime);
            remainingSeatCountText = itemView.findViewById(R.id.remainingSeatCount);
            ticketCountText = itemView.findViewById(R.id.editTextTicketCount);
            ticketBookingButton = itemView.findViewById(R.id.ticketBookingVerificationButton);

            ticketBookingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int ticketCount = Integer.parseInt(ticketCountText.getText().toString());
                    int remainingTickets = planeType.getRowCount() * planeType.getColumnCount() - bookedTicketCount;

                    if (ticketCount < 1) {
                        Toast.makeText(context, "Nem foglalhatsz 1-nél kevesebb jegyet!", Toast.LENGTH_SHORT).show();
                        ticketCountText.setBackgroundColor(Color.RED);
                        return;
                    }

                    if (ticketCount > remainingTickets) {
                        Toast.makeText(context, "Nem foglalhatsz több jegyet, mint ahány üres hely van a repülőgépen!", Toast.LENGTH_SHORT).show();
                        ticketCountText.setBackgroundColor(Color.RED);
                        return;
                    }

                    ((TicketBookingActivity) context).booking(ticketCount, planeType, flight.getFlightId(), bookedSeats);
                }
            });
        }

        public void bindTo(Flight flight, Airport fromAirport, Airport toAirport, PlaneType planeType, int bookedTicketCount) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss");

            String departureDatetime = dateFormat.format(flight.getDeparture().toDate());
            String arrivalDatetime = dateFormat.format(flight.getArrival().toDate());

            String fromAirportNameFormated = getAirportName(fromAirport.getName());
            String toAirportNameFormated = getAirportName(toAirport.getName());

            String remainingSeatCount = String.valueOf(planeType.getColumnCount() * planeType.getRowCount() - bookedTicketCount);

            flightIdText.setText(flight.getFlightId());
            planeTypeText.setText(flight.getPlaneTypeName());
            fromAirportText.setText(fromAirport.toString());
            toAirportText.setText(toAirport.toString());
            fromAirportName.setText(fromAirportNameFormated);
            toAirportName.setText(toAirportNameFormated);
            departureDatetimeText.setText(departureDatetime);
            remainingSeatCountText.setText(remainingSeatCount);
            arrivalDatetimeText.setText(arrivalDatetime);
        }

        private String getAirportName(String airportName) {
            StringBuilder textBuilder = new StringBuilder();

            for (int i = 0; i < airportName.length(); i += 15) {
                int substringLength = Math.min(15, airportName.length() - i);
                textBuilder.append(airportName.substring(i, i + substringLength));

                if (i + substringLength < airportName.length()) {
                    textBuilder.append("-\n");
                }
            }

            return textBuilder.toString();
        }
    }
}
