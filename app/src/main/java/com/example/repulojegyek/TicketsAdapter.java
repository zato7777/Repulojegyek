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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.repulojegyek.DataClasses.Airport;
import com.example.repulojegyek.DataClasses.Flight;
import com.example.repulojegyek.DataClasses.Ticket;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TicketsAdapter extends RecyclerView.Adapter<TicketsAdapter.ViewHolder> implements Filterable {
    private ArrayList<Ticket> tickets;
    private ArrayList<Ticket> allTickets;
    private Context context;
    private int lastPosition = -1;
    private Flight flight;
    private String fromAirportString;
    private String toAirportString;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public TicketsAdapter( Context context, ArrayList<Ticket> tickets) {
        this.tickets = tickets;
        this.allTickets = tickets;
        this.context = context;
    }


    @NonNull
    @Override
    public TicketsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TicketsAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_tickets, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TicketsAdapter.ViewHolder holder, int position) {
        Ticket currentTicket = tickets.get(position);


        CollectionReference flightsCollection = firestore.collection("flights");
        CollectionReference airportCollection = firestore.collection("airports");

        String flightId = currentTicket.getFlightId();
        flightsCollection.whereEqualTo("flightId", flightId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        flight = documentSnapshot.toObject(Flight.class);
                        String fromAirportCode = flight.getFromAirportCode();
                        airportCollection.whereEqualTo("code", fromAirportCode)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                    for (QueryDocumentSnapshot documentSnapshot2 : queryDocumentSnapshots2) {
                                        Airport fromAirport = documentSnapshot2.toObject(Airport.class);
                                        fromAirportString = fromAirport.toString();
                                        String toAirportCode = flight.getToAirportCode();
                                        airportCollection.whereEqualTo("code", toAirportCode)
                                                .get()
                                                .addOnSuccessListener(queryDocumentSnapshots3 -> {
                                                    for (QueryDocumentSnapshot documentSnapshot3 : queryDocumentSnapshots3) {
                                                        Airport toAirport = documentSnapshot3.toObject(Airport.class);
                                                        toAirportString = toAirport.toString();
                                                        holder.bindTo(currentTicket);
                                                    }
                                                });
                                    }
                                });
                    }
                });

        if(holder.getAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.tickets_animation);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    @Override
    public Filter getFilter() {
        return ticketsFilter;
    }

    private Filter ticketsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Ticket> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.count = allTickets.size();
                results.values = allTickets;
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Ticket ticket: allTickets) {
                    if (toAirportString.toLowerCase().contains(filterPattern) || fromAirportString.toLowerCase().contains(filterPattern)) {
                        filteredList.add(ticket);
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            }


            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            tickets = (ArrayList<Ticket>) results.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView ticketIdText;
        private TextView flight_idText;
        private TextView usernameText;
        private TextView seatText;
        private TextView fromAirportText;
        private TextView toAirportText;
        private TextView departureDatetimeText;
        private TextView arrivalDatetimeText;
        private Button ticketDeleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            CollectionReference ticketsCollection = FirebaseFirestore.getInstance().collection("tickets");

            ticketIdText = itemView.findViewById(R.id.ticketId);
            flight_idText = itemView.findViewById(R.id.ticketBookingFlightId);
            usernameText = itemView.findViewById(R.id.username);
            seatText = itemView.findViewById(R.id.seat);
            fromAirportText = itemView.findViewById(R.id.ticketBookingFromAirport);
            toAirportText = itemView.findViewById(R.id.ticketBookingToAirport);
            departureDatetimeText = itemView.findViewById(R.id.ticketBookingDepartureDatetime);
            arrivalDatetimeText = itemView.findViewById(R.id.ticketBookingArrivalDatetime);
            ticketDeleteButton = itemView.findViewById(R.id.ticketBookingVerificationButton);

            ticketDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ticketsCollection.whereEqualTo("id", ticketIdText.getText().toString()).get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    String toBeDeletedDocument = documentSnapshot.getId();
                                    ticketsCollection.document(toBeDeletedDocument).delete()
                                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                                ((TicketsActivity) context).queryTicketsData();
                                                Toast.makeText(context, "Sikeresen törölted a jegyet!", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            });
                }
            });
        }

        public void bindTo(Ticket currentTicket) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss");

            String departureDatetime = dateFormat.format(flight.getDeparture().toDate());
            String arrivalDatetime = dateFormat.format(flight.getArrival().toDate());

            ticketIdText.setText(currentTicket.getId());
            usernameText.setText(currentTicket.getOwnerName());
            seatText.setText(currentTicket.getSeat());
            flight_idText.setText(currentTicket.getFlightId());
            fromAirportText.setText(fromAirportString);
            toAirportText.setText(toAirportString);
            departureDatetimeText.setText(departureDatetime);
            arrivalDatetimeText.setText(arrivalDatetime);
        }
    }
}
