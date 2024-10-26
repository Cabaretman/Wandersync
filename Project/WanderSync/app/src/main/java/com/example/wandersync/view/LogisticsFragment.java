package com.example.wandersync.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wandersync.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class LogisticsFragment extends Fragment {

    private PieChart pieChart;
    private ArrayList<String> invitedUsers;
    private HashMap<String, ArrayList<String>> userNotesMap;
    private LinearLayout contributorsLayout;
    private LinearLayout notesListLayout;
    private EditText inviteUsernameInput;
    private EditText noteInput;
    private DatabaseReference databaseReference;
    private String currentUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logistics, container, false);

        Button visualizeButton = view.findViewById(R.id.visualize_button);
        Button inviteButton = view.findViewById(R.id.invite_button);
        Button addNoteButton = view.findViewById(R.id.add_note_button);
        inviteUsernameInput = view.findViewById(R.id.invite_username_input);
        noteInput = view.findViewById(R.id.note_input);
        pieChart = view.findViewById(R.id.pie_chart);
        contributorsLayout = view.findViewById(R.id.contributors_layout);
        notesListLayout = view.findViewById(R.id.notes_list_layout);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        invitedUsers = new ArrayList<>();
        userNotesMap = new HashMap<>();

        currentUsername = getCurrentUsername();

        visualizeButton.setOnClickListener(v -> visualizeTripDays());
        inviteButton.setOnClickListener(v -> inviteUser());
        addNoteButton.setOnClickListener(v -> addNote());

        return view;
    }

    private String getCurrentUsername() {
        return "current_user";
    }

    public boolean getPieChartVisibility() {
        return pieChart.getVisibility() == View.VISIBLE;
    }

    private void visualizeTripDays() {
        int allottedDays = 10;
        int plannedDays = 6;

        if (pieChart.getVisibility() == View.VISIBLE) {
            pieChart.setVisibility(View.GONE);
        } else {
            ArrayList<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(plannedDays, "Planned Days"));
            entries.add(new PieEntry(allottedDays, "Allotted Days"));

            PieDataSet dataSet = new PieDataSet(entries, "Trip Days");

            int[] colors = {0xFFFF5733, 0xFF33FF57};
            dataSet.setColors(colors);

            PieData pieData = new PieData(dataSet);
            pieChart.setData(pieData);
            pieChart.invalidate();
            pieChart.setVisibility(View.VISIBLE);
        }
    }


    private void inviteUser() {
        String username = inviteUsernameInput.getText().toString();
        if (username.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.equals(currentUsername)) {
            Toast.makeText(getContext(), "You cannot invite yourself.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (invitedUsers.contains(username)) {
            Toast.makeText(getContext(), username + " is already invited.", Toast.LENGTH_SHORT).show();
            return;
        }

        String safeUsername = username.replace(".", ",").replace("@", ",");

        databaseReference.child(safeUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    invitedUsers.add(username);
                    userNotesMap.put(username, new ArrayList<>());

                    TextView contributorView = new TextView(getContext());
                    contributorView.setText(username);
                    contributorView.setPadding(16, 8, 16, 8);
                    contributorView.setBackgroundResource(R.drawable.button_background);
                    contributorView.setTextColor(getResources().getColor(R.color.buttonTextColor));

                    contributorView.setOnClickListener(v -> showUserNotes(username));
                    contributorsLayout.addView(contributorView);
                    inviteUsernameInput.setText("");
                    Toast.makeText(getContext(), username + " invited!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "User " + username + " does not exist.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserNotes(String username) {
        ArrayList<String> notes = userNotesMap.get(username);
        if (notes != null && !notes.isEmpty()) {
            StringBuilder notesDisplay = new StringBuilder();
            for (String note : notes) {
                notesDisplay.append(note).append("\n");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Notes for " + username)
                    .setMessage(notesDisplay.toString())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Toast.makeText(getContext(), username + " has no notes.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addNote() {
        String note = noteInput.getText().toString();
        if (!note.isEmpty()) {
            displayNoteInList(note);
            noteInput.setText("");
            Toast.makeText(getContext(), "Note added.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Note cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayNoteInList(String note) {
        TextView noteView = new TextView(getContext());
        noteView.setText(note);
        notesListLayout.addView(noteView);
    }
}
