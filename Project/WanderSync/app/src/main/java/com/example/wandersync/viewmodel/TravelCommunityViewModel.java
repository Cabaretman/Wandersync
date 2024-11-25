package com.example.wandersync.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wandersync.Model.TravelPost;
import com.example.wandersync.Model.Accommodation;
import com.example.wandersync.Model.Destination;
import com.example.wandersync.Model.DiningReservation;
import com.example.wandersync.viewmodel.sorting.SortByStartDateStrategy;
import com.example.wandersync.viewmodel.sorting.SortStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TravelCommunityViewModel extends AndroidViewModel {

    private final DatabaseManager databaseManager;
    private final MutableLiveData<List<Destination>> destinationsLiveData =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Accommodation>> accommodationsLiveData =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<DiningReservation>> diningReservationsLiveData =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private String userId;
    private String userId2;
    private final MutableLiveData<List<TravelPost>> travelPostsLiveData =
            new MutableLiveData<>(new ArrayList<>());
    private SortStrategy sortStrategy = new SortByStartDateStrategy();


    public TravelCommunityViewModel(@NonNull Application application) {
        super(application);
        databaseManager = DatabaseManager.getInstance();

        SharedPreferences sharedPreferences = application.
                getSharedPreferences("WanderSyncPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("username", null);
        userId2 = userId;
        checkCollaboratorStatusAndLoadData();
    }

    public LiveData<List<Destination>> getDestinationsLiveData() {
        return destinationsLiveData;
    }

    public LiveData<List<Accommodation>> getAccommodationsLiveData() {
        return accommodationsLiveData;
    }

    public LiveData<List<DiningReservation>> getDiningReservationsLiveData() {
        return diningReservationsLiveData;
    }
    public LiveData<List<TravelPost>> getTravelPostsLiveData() {
        return travelPostsLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    private void checkCollaboratorStatusAndLoadData() {
        if (userId == null) {
            errorLiveData.setValue("User ID is not available.");
            return;
        }

        databaseManager.getUsersReference().child(userId).child("isCollaborator")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean isCollaborator = snapshot.getValue(Boolean.class);
                        if (Boolean.TRUE.equals(isCollaborator)) {
                            databaseManager.getUsersReference().child(userId).child("mainUserId")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String mainUserId = snapshot.getValue(String.class);
                                            if (mainUserId != null) {
                                                userId2 = mainUserId;
                                            }
                                            loadDropdownData();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            errorLiveData.setValue("Failed to load main user ID: "
                                                    + error.getMessage());
                                        }
                                    });
                        } else {
                            loadDropdownData();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        errorLiveData.setValue("Failed to check collaborator status: "
                                + error.getMessage());
                    }
                });
    }

    private void loadDropdownData() {
        // Load Destinations
        databaseManager.loadDestinations(userId2, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Destination> destinations = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Destination destination = data.getValue(Destination.class);
                    if (destination != null) {
                        destinations.add(destination);
                    }
                }
                destinationsLiveData.setValue(destinations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorLiveData.setValue("Failed to load destinations: " + error.getMessage());
            }
        });

        // Load Accommodations
        databaseManager.loadAccommodations(userId2, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Accommodation> accommodations = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Accommodation accommodation = data.getValue(Accommodation.class);
                    if (accommodation != null) {
                        accommodations.add(accommodation);
                    }
                }
                accommodationsLiveData.setValue(accommodations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorLiveData.setValue("Failed to load accommodations: " + error.getMessage());
            }
        });

        // Load Dining Reservations
        databaseManager.loadDiningReservations(userId2, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<DiningReservation> diningReservations = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    DiningReservation reservation = data.getValue(DiningReservation.class);
                    if (reservation != null) {
                        diningReservations.add(reservation);
                    }
                }
                diningReservationsLiveData.setValue(diningReservations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorLiveData.setValue("Failed to load dining reservations: " + error.getMessage());
            }
        });
    }
    public void addTravelPlan(String startDate, String endDate, String notes,
                              String destination, String accommodation,
                              String dining, String rating) {
        if (userId == null) {
            errorLiveData.setValue("User ID is not available.");
            return;
        }
        isLoading.setValue(true);

        databaseManager.getUsersReference().child(userId).child("isCollaborator")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean isCollaborator = snapshot.getValue(Boolean.class);
                        if (Boolean.TRUE.equals(isCollaborator)) {
                            // If the user is a collaborator, use mainUserId for data loading
                            databaseManager.getUsersReference().child(userId).child("mainUserId")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot
                                                                         mainUserSnapshot) {
                                            String mainUserId = mainUserSnapshot.
                                                    getValue(String.class);
                                            if (mainUserId != null) {
                                                List<String> date = new ArrayList<>();
                                                date.add(startDate);
                                                date.add(endDate);
                                                loadAndSaveTravelPlan(mainUserId,
                                                        date, notes,
                                                        destination, accommodation, dining, rating);
                                            } else {
                                                errorLiveData.setValue("Main user ID not "
                                                        + "found for collaborator.");
                                                isLoading.setValue(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            errorLiveData.setValue("Failed to load main user ID: "
                                                    + error.getMessage());
                                            isLoading.setValue(false);
                                        }
                                    });
                        } else {
                            List<String> date = new ArrayList<>();
                            date.add(startDate);
                            date.add(endDate);
                            loadAndSaveTravelPlan(userId, date,
                                    notes, destination, accommodation, dining, rating);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        errorLiveData.setValue("Failed to check collaborator status: "
                                + error.getMessage());
                        isLoading.setValue(false);
                    }
                });
    }

    private void loadAndSaveTravelPlan(String dataUserId, List<String> date,
                                        String notes, String destination,
                                       String accommodation, String dining, String rating) {
        databaseManager.loadDestinations(dataUserId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Destination> destinations = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Destination destination = data.getValue(Destination.class);
                    if (destination != null) {
                        destinations.add(destination);
                    }
                }

                databaseManager.loadAccommodations(dataUserId, new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Accommodation> accommodations = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Accommodation accommodation = data.getValue(Accommodation.class);
                            if (accommodation != null) {
                                accommodations.add(accommodation);
                            }
                        }

                        databaseManager.loadDiningReservations(dataUserId,
                                new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    List<DiningReservation> diningReservations = new ArrayList<>();
                                    for (DataSnapshot data : snapshot.getChildren()) {
                                        DiningReservation reservation =
                                                data.getValue(DiningReservation.class);
                                        if (reservation != null) {
                                            diningReservations.add(reservation);
                                        }
                                    }
                                    List<String> allStr = new ArrayList<>();
                                    allStr.add(databaseManager.
                                            getTravelPostsReference(userId).push().getKey());
                                    allStr.add(userId);
                                    allStr.add(date.get(0));
                                    allStr.add(date.get(1));
                                    allStr.add(notes);
                                    allStr.add(destination);
                                    TravelPost travelPost = new TravelPost(
                                            allStr,
                                            accommodation,
                                            dining,
                                            destinations,
                                            accommodations,
                                            diningReservations,
                                            rating
                                    );

                                    databaseManager.addTravelPost(travelPost, unused -> {
                                        isLoading.setValue(false);
                                        errorLiveData.setValue("Travel plan saved successfully.");
                                        loadTravelPosts();
                                    }, error -> {
                                        isLoading.setValue(false);
                                        errorLiveData.setValue("Failed to save travel plan: "
                                                + error.getMessage());
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    errorLiveData.setValue("Failed to load dining reservations: "
                                            + error.getMessage());
                                    isLoading.setValue(false);
                                }
                            });
                        }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        errorLiveData.setValue("Failed to load accommodations: "
                                + error.getMessage());
                        isLoading.setValue(false);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorLiveData.setValue("Failed to load destinations: " + error.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    public void loadTravelPosts() {
        if (userId == null) {
            errorLiveData.setValue("User ID is not available.");
            return;
        }

        isLoading.setValue(true);

        databaseManager.loadTravelPosts(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<TravelPost> travelPosts = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    TravelPost travelPost = postSnapshot.getValue(TravelPost.class);
                    if (travelPost != null) {
                        travelPosts.add(travelPost);
                    }
                }
                if (travelPosts.isEmpty()) {
                    addDefaultPost();
                    return;
                }
                travelPostsLiveData.setValue(travelPosts);
                isLoading.setValue(false);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorLiveData.setValue("Failed to load travel posts: " + error.getMessage());
                isLoading.setValue(false);
            }
        });
    }
    public void setSortStrategy(SortStrategy strategy) {
        this.sortStrategy = strategy;
    }

    private void addDefaultPost() {
        List<String> allStr = new ArrayList<>();
        allStr.add("1");
        allStr.add("N/A");
        allStr.add("01/01/2023");
        allStr.add("01/07/2023");
        allStr.add("Default");
        allStr.add("Default");
        TravelPost defaultPost = new TravelPost(allStr, "Default", "Default",
                null, null, null, "1");

        List<TravelPost> defaultList = new ArrayList<>();
        defaultList.add(defaultPost);
        travelPostsLiveData.setValue(defaultList);
    }

}

