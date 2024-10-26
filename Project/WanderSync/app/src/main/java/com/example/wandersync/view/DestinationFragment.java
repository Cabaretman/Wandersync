package com.example.wandersync.view;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wandersync.Model.Destination;
import com.example.wandersync.R;
import com.example.wandersync.viewmodel.DestinationAdapter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DestinationFragment extends Fragment {

    private RecyclerView recyclerView;
    private DestinationAdapter adapter;
    private List<Destination> destinationList;

    private LinearLayout formLayout;
    private LinearLayout formLayout1;
    private LinearLayout resultLayout;
    private EditText travelLocationEditText, estimatedStartEditText, estimatedEndEditText, duration, estimatedStartEditText1, estimatedEndEditText1;
    private Button logTravelButton, cancelButton, submitButton, cancelButton1, submitButton1;
    private TextView resultAmountTextView;
    private Button resetResultButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_destination, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_destinations);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        destinationList = new ArrayList<>();
        destinationList.add(new Destination("Destination", 0));
        destinationList.add(new Destination("Destination", 0));

        adapter = new DestinationAdapter(destinationList);
        recyclerView.setAdapter(adapter);

        formLayout = view.findViewById(R.id.form_layout);
        formLayout1 = view.findViewById(R.id.form_layout1);
        resultLayout = view.findViewById(R.id.result_layout);
        travelLocationEditText = view.findViewById(R.id.travel_location);
        duration = view.findViewById(R.id.duration);
        estimatedStartEditText = view.findViewById(R.id.estimated_start);
        estimatedStartEditText1 = view.findViewById(R.id.estimated_start1);
        estimatedEndEditText = view.findViewById(R.id.estimated_end);
        estimatedEndEditText1 = view.findViewById(R.id.estimated_end1);
        logTravelButton = view.findViewById(R.id.log_travel_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        cancelButton1 = view.findViewById(R.id.cancel_button1);
        submitButton = view.findViewById(R.id.submit_button);
        submitButton1 = view.findViewById(R.id.submit_button1);
        Button calculateButton = view.findViewById(R.id.calc_button);
        resultAmountTextView = view.findViewById(R.id.result_amount);
        resetResultButton = view.findViewById(R.id.reset_result_button);

        formLayout.setVisibility(View.GONE);
        formLayout1.setVisibility(View.GONE);
        resultLayout.setVisibility(View.GONE);

        logTravelButton.setOnClickListener(v -> {
            formLayout.setVisibility(View.VISIBLE);
        });

        cancelButton.setOnClickListener(v -> {
            travelLocationEditText.setText("");
            estimatedStartEditText.setText("");
            estimatedEndEditText.setText("");
            formLayout.setVisibility(View.GONE);
        });

        submitButton.setOnClickListener(v -> {
            String location = travelLocationEditText.getText().toString();
            if (location.isEmpty()) {
                Toast.makeText(requireContext(), "Location must be a valid name", Toast.LENGTH_SHORT).show();
                return;
            }
            String start = estimatedStartEditText.getText().toString();
            String end = estimatedEndEditText.getText().toString();
            LocalDate startDate;
            try {
                startDate = LocalDate.parse(start);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Start date must be in valid format", Toast.LENGTH_SHORT).show();
                return;
            }
            LocalDate endDate;
            try {
                endDate = LocalDate.parse(end);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "End date must be in valid format", Toast.LENGTH_SHORT).show();
                return;
            }
            long amount = startDate.until(endDate, ChronoUnit.DAYS);
            if (amount <= 0) {
                Toast.makeText(requireContext(), "Start date must be before end date", Toast.LENGTH_SHORT).show();
                return;
            }

            destinationList.add(new Destination(location, amount));
            adapter.notifyDataSetChanged();

            travelLocationEditText.setText("");
            estimatedStartEditText.setText("");
            estimatedEndEditText.setText("");
            formLayout.setVisibility(View.GONE);
        });

        calculateButton.setOnClickListener(v -> {
            formLayout1.setVisibility(View.VISIBLE);
            resultLayout.setVisibility(View.GONE);
        });

        cancelButton1.setOnClickListener(v -> {
            duration.setText("");
            estimatedStartEditText1.setText("");
            estimatedEndEditText1.setText("");
            formLayout1.setVisibility(View.GONE);
        });

        submitButton1.setOnClickListener(v -> {
            String durationStr = duration.getText().toString();
            long durationLong;
            if (!durationStr.isEmpty()) {
                try {
                    durationLong = Long.parseLong(durationStr);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Duration must be in correct format of 'DAYS'", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(requireContext(), "Duration must be in correct format of 'DAYS'", Toast.LENGTH_SHORT).show();
                return;
            }

            String start = estimatedStartEditText1.getText().toString();
            String end = estimatedEndEditText1.getText().toString();
            LocalDate startDate;
            try {
                startDate = LocalDate.parse(start);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Start date must have form of YYYY-MM-DD", Toast.LENGTH_SHORT).show();
                return;
            }
            LocalDate endDate;
            try {
                endDate = LocalDate.parse(end);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "End date must have form of YYYY-MM-DD", Toast.LENGTH_SHORT).show();
                return;
            }
            long amount = startDate.until(endDate, ChronoUnit.DAYS);
            long finalAmount;
            if (amount == durationLong) {
                finalAmount = durationLong;

                resultAmountTextView.setText(finalAmount + " days");
                resultLayout.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(requireContext(), "Duration must equal days between start and end date", Toast.LENGTH_SHORT).show();
                return;
            }

            estimatedStartEditText1.setText("");
            estimatedEndEditText1.setText("");
            formLayout1.setVisibility(View.GONE);
        });

        resetResultButton.setOnClickListener(v -> {
            resultAmountTextView.setText("0 days");
        });

        return view;
    }
}
