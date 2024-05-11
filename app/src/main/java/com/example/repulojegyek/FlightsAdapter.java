package com.example.repulojegyek;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.repulojegyek.DataClasses.Airport;
import com.example.repulojegyek.DataClasses.Flight;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FlightsAdapter extends RecyclerView.Adapter<FlightsAdapter.ViewHolder> implements Filterable {
    private ArrayList<Flight> flights;
    private ArrayList<Flight> allFlights;
    private Context context;
    private int lastPosition = -1;

    private String fromAirportString;
    private String toAirportString;
    FlightsAdapter(Context context, ArrayList<Flight> flights) {
        this.flights = flights;
        this.allFlights = flights;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_flights, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FlightsAdapter.ViewHolder holder, int position) {
        Flight currentFlight = flights.get(position);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference airportsCollection = firestore.collection("airports");

        String fromAirportCode = currentFlight.getFromAirportCode();
        airportsCollection.whereEqualTo("code", fromAirportCode)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Airport fromAirport = documentSnapshot.toObject(Airport.class);
                        fromAirportString = fromAirport.toString();
                        String toAirportCode = currentFlight.getToAirportCode();
                        airportsCollection.whereEqualTo("code", toAirportCode)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                    for (QueryDocumentSnapshot documentSnapshot2 : queryDocumentSnapshots2) {
                                        Airport toAirport = documentSnapshot2.toObject(Airport.class);
                                        toAirportString = toAirport.toString();
                                        holder.bindTo(currentFlight);
                                    }
                                });
                    }
                });


        if(holder.getAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.flights_animation);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return flights.size();
    }

    @Override
    public Filter getFilter() {
        return flightsFilter;
    }

    private Filter flightsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Flight> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.count = allFlights.size();
                results.values = allFlights;
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Flight flight: allFlights) {
                    if (toAirportString.toLowerCase().contains(filterPattern) || fromAirportString.toLowerCase().contains(filterPattern)) {
                        filteredList.add(flight);
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            }


            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            flights = (ArrayList<Flight>) results.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView flightIdText;
        private TextView fromAirportText;
        private TextView toAirportText;
        private TextView departureDatetimeText;
        private TextView arrivalDatetimeText;
        private Button ticketBookingButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            flightIdText = itemView.findViewById(R.id.flightId);
            fromAirportText = itemView.findViewById(R.id.fromAirport);
            toAirportText = itemView.findViewById(R.id.toAirport);
            departureDatetimeText = itemView.findViewById(R.id.departureDatetime);
            arrivalDatetimeText = itemView.findViewById(R.id.arrivalDatetime);
            ticketBookingButton = itemView.findViewById(R.id.ticketBookingButton);

            ticketBookingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String flightId = flightIdText.getText().toString();
                    ((FlightsActivity) context).booking(flightId);
                }
            });
        }

        public void bindTo(Flight currentFlight) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss");

            String departureDatetime = dateFormat.format(currentFlight.getDeparture().toDate());
            String arrivalDatetime = dateFormat.format(currentFlight.getArrival().toDate());


            flightIdText.setText(currentFlight.getFlightId());
            fromAirportText.setText(fromAirportString);
            toAirportText.setText(toAirportString);
            departureDatetimeText.setText(departureDatetime);
            arrivalDatetimeText.setText(arrivalDatetime);
        }
    }
}


