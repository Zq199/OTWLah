/**
 * The PartyModeController class provides functionality for managing party mode activities
 * within the OTA Lah app. Party mode is designed to streamline group travel and coordinate
 * arrivals of users traveling to a shared destination.
 */
package com.project.sc2006.controllers;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.maps.android.SphericalUtil;
import com.project.sc2006.LoginActivity;
import com.project.sc2006.MainActivity;
import com.project.sc2006.R;
import com.project.sc2006.entities.Party;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.IOException;
import java.sql.Array;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class PartyModeController {
    private int partyID;
    private Party partyInfo;
    protected boolean isPartyIDUpdated = false;
    private boolean error = true;
    private int userID;

    private boolean isDeparted = false;
    private LatLng departureLocation = null;

    private ArrayList<Party> participatedParties;
    private static final String NOTIFICATION_ID_STRING = "0";

    private static  int NOTIFICATION_ID = 0;

    private int messageID = 10;

    private static PartyModeController partyModeController = null;

    /**
     * Private constructor to prevent external instantiation of `PartyModeController`.
     */
    private PartyModeController() {
        partyInfo = new Party();
        participatedParties = new ArrayList<>();
    }

    /**
     * Retrieves the single instance of the `PartyModeController` class. If an instance
     * does not exist, it creates one.
     *
     * @return The single instance of the `PartyModeController` class.
     */
    public static synchronized PartyModeController getInstance(){
        if (partyModeController == null){
            partyModeController = new PartyModeController();
        }
        return partyModeController;
    }

    public ArrayList<Party> getParticipatedParties() {
        return participatedParties;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void httpGETJson(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(callback);
    }

    public void setDepartureInformation(LatLng departureLocation){
        this.departureLocation = departureLocation;
        this.isDeparted = false;
    }

    public int getPartyID() {
        return partyID;
    }

    public Party getPartyInfo() {
        return partyInfo;
    }

    public void setPartyID(int partyID) {
        this.partyID = partyID;
    }

    public void updateParticipatedParties(String session, Activity runningActivity) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/party/joinedParties").newBuilder();
        urlBuilder.addQueryParameter("session", session);
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Party History Update Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    participatedParties.clear();
                                    // navigate to the party QR code
                                    JSONArray parties = json.getJSONArray("parties");
                                    for (int i = 0; i < parties.length(); i++) {
                                        try {
                                            JSONObject party = parties.getJSONObject(i);
                                            int partyID = party.getInt("PARTY_ID");
                                            String destination = party.getString("DESTINATION");
                                            String destinationAddress = party.getString("DESTINATION_LOCATION");
                                            String destination_coordinate = party.getString("DESTINATION_COORDINATE");
                                            String PREFERRED_ARRIVAL_TIME = party.getString("PREFERRED_ARRIVAL_TIME");
                                            int PARTY_LEADER = party.getInt("PARTY_LEADER");
                                            ArrayList<String> DESTINATION_COORDINATES = new ArrayList<>(Arrays.asList(destination_coordinate.split(",")));
                                            Location location = new Location(destination);
                                            location.setLatitude(Double.parseDouble(DESTINATION_COORDINATES.get(0)));
                                            location.setLongitude(Double.parseDouble(DESTINATION_COORDINATES.get(1)));
                                            Party oneParty = new Party(partyID, location, destination, destinationAddress, PREFERRED_ARRIVAL_TIME, PARTY_LEADER);
                                            participatedParties.add(oneParty);
                                        } catch (JSONException | NumberFormatException e) {
                                        }
                                    }
                                } else {
                                    Toast.makeText(runningActivity, "Party Creation Failure", Toast.LENGTH_SHORT).show();
                                    // handle error in activity_party
                                }
                            } catch (JSONException | NumberFormatException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "updateParticipatedParties Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public void getNotifications(String session, Activity runningActivity) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/notification/get").newBuilder();
        urlBuilder.addQueryParameter("session", session);
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Failed to get notification", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    // navigate to the party QR code
                                    JSONArray messages = json.getJSONArray("messages");
                                    NotificationManager mNotificationManager = (NotificationManager) runningActivity.getSystemService(NOTIFICATION_SERVICE);
                                    for (int i = 0; i < messages.length(); i++) {
                                        try {
                                            String oneMessage = messages.getString(i);

                                            NotificationCompat.Builder mBuilder =
                                                    new NotificationCompat.Builder(runningActivity.getApplicationContext(), "notify_001");

                                            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
                                            bigText.bigText(oneMessage);
                                            bigText.setBigContentTitle("Heads Up!");
                                            bigText.setSummaryText(oneMessage);

                                            mBuilder.setSmallIcon(R.mipmap.ic_launcher_foreground);
                                            mBuilder.setContentTitle("Time To Go!");
                                            mBuilder.setContentText(oneMessage);
                                            mBuilder.setPriority(Notification.PRIORITY_MAX);
                                            mBuilder.setStyle(bigText);

                                            mNotificationManager = (NotificationManager) runningActivity.getSystemService(NOTIFICATION_SERVICE);

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                            {
                                                String channelId = "New Channel" + LocalDateTime.now().toString();
                                                NotificationChannel channel = new NotificationChannel(
                                                        channelId,
                                                        "OTW Lah Quick Glance",
                                                        NotificationManager.IMPORTANCE_HIGH);
                                                mNotificationManager.createNotificationChannel(channel);
                                                mBuilder.setChannelId(channelId);
                                            }
                                            mNotificationManager.notify(messageID, mBuilder.build());
                                            messageID += 1;

                                        } catch (JSONException | NumberFormatException e) {
                                            Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    Toast.makeText(runningActivity, "Get notification Failure", Toast.LENGTH_SHORT).show();
                                    // handle error in activity_party
                                }
                            } catch (JSONException | NumberFormatException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "getNotifications Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public void sendNotifications(String session, String userIDTo, String notification, Activity runningActivity) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/user/notification/send").newBuilder();
        urlBuilder.addQueryParameter("session", session);
        urlBuilder.addQueryParameter("userid", userIDTo);
        urlBuilder.addQueryParameter("notification", notification);
//        Toast.makeText(runningActivity, "Get notification", Toast.LENGTH_SHORT).show();
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(runningActivity, "Failed to get notification", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
//                                    Toast.makeText(runningActivity, "Notification Sent to " + userIDTo, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(runningActivity, "Get notification Failure", Toast.LENGTH_SHORT).show();
                                    // handle error in activity_party
                                }
                            } catch (JSONException | NumberFormatException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    /**
     * Creates a new party session and sets up the necessary parameters.
     *
     * @param session         A unique identifier for the user session.
     * @param location        The destination location of the party.
     * @param arrivalTime     The estimated arrival time at the destination.
     * @param runningActivity The current activity where the party is created.
     */
    public void createParty(String session, Location location, String locationName, String locationAddress, String arrivalTime, Activity runningActivity) {
        isPartyIDUpdated = false;
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/party/create").newBuilder();
        urlBuilder.addQueryParameter("session", session);
        urlBuilder.addQueryParameter("destination", locationName);
        urlBuilder.addQueryParameter("destination_coordinate", location.getLatitude() + "," + location.getLongitude());
        urlBuilder.addQueryParameter("arrival_time", arrivalTime);
        urlBuilder.addQueryParameter("destination_location", locationAddress);
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
//                        Toast.makeText(runningActivity, "Party Creation Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    updateParticipatedParties(session, runningActivity);
                    runningActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    // navigate to the party QR code
                                    int partyIDStr = json.getInt("partyid");
//                                    Toast.makeText(runningActivity, "Party Creation Success" + " id is " + partyIDStr, Toast.LENGTH_SHORT).show();
                                    partyID = partyIDStr;
                                    partyInfo = new Party(partyID, location, locationName, locationAddress, arrivalTime, userID);
                                    isPartyIDUpdated = true;
                                    getPartyCoordinates(session, runningActivity);
                                } else {
                                    Toast.makeText(runningActivity, "Party Creation Failure", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException | NumberFormatException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "createParty Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Edit a party session and sets up the necessary parameters.
     *
     * @param session         A unique identifier for the user session.
     * @param location        The destination location of the party.
     * @param arrivalTime     The estimated arrival time at the destination.
     * @param runningActivity The current activity where the party is created.
     */
    public void editParty(String session, String editPartyID, Location location, String locationName, String locationAddress, String arrivalTime, Activity runningActivity) {
        isPartyIDUpdated = false;
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/party/updateparty").newBuilder();
        urlBuilder.addQueryParameter("session", session);
        urlBuilder.addQueryParameter("partyid", editPartyID);
        urlBuilder.addQueryParameter("destination", locationName);
        urlBuilder.addQueryParameter("destination_coordinate", location.getLatitude() + "," + location.getLongitude());
        urlBuilder.addQueryParameter("arrival_time", arrivalTime);
        urlBuilder.addQueryParameter("destination_location", locationAddress);
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
//                        Toast.makeText(runningActivity, "Party Creation Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    updateParticipatedParties(session, runningActivity);
                    runningActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    // navigate to the party QR code
                                    int partyIDStr = json.getInt("partyid");
//                                    Toast.makeText(runningActivity, "Party Creation Success" + " id is " + partyIDStr, Toast.LENGTH_SHORT).show();
                                    partyID = partyIDStr;
                                    partyInfo = new Party(partyID, location, locationName, locationAddress, arrivalTime, userID);
                                    isPartyIDUpdated = true;
                                    getPartyCoordinates(session, runningActivity);
                                } else {
                                    Toast.makeText(runningActivity, "Party Creation Failure", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException | NumberFormatException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "editParty Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public void getPartyInfo(String session, int partyID, Activity runningActivity) {
        isPartyIDUpdated = false;
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/party/get").newBuilder();
        urlBuilder.addQueryParameter("session", session);
        urlBuilder.addQueryParameter("partyid", String.valueOf(partyID));
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        error = true;
//                        Toast.makeText(runningActivity, "Party get Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    // navigate to the party QR code
                                    setPartyID(partyID);
                                    String DESTINATION = json.getString("DESTINATION");
                                    String destinationAddress = json.getString("DESTINATION_LOCATION");
                                    int PARTY_LEADER = json.getInt("PARTY_LEADER");
                                    String PREFERRED_ARRIVAL_TIME = json.getString("PREFERRED_ARRIVAL_TIME");
                                    String DESTINATION_COORDINATE = json.getString("DESTINATION_COORDINATE");
                                    ArrayList<String> DESTINATION_COORDINATES = new ArrayList<>(Arrays.asList(DESTINATION_COORDINATE.split(",")));
                                    Location location = new Location(DESTINATION);
                                    location.setLatitude(Double.parseDouble(DESTINATION_COORDINATES.get(0)));
                                    location.setLongitude(Double.parseDouble(DESTINATION_COORDINATES.get(1)));
                                    partyInfo = new Party(partyID, location, DESTINATION, destinationAddress, PREFERRED_ARRIVAL_TIME, PARTY_LEADER);
                                    isPartyIDUpdated = true;
//                                    updatePartyCoordinates(session, runningActivity, location.getLatitude() + "," + location.getLongitude(), PREFERRED_ARRIVAL_TIME);
                                    getPartyCoordinates(session, runningActivity);
                                    error = false;
//                                    Toast.makeText(runningActivity, "Party Info Fetch OK", Toast.LENGTH_SHORT).show();// handle error in activity_party
                                } else {
                                    error = true;
//                                    Toast.makeText(runningActivity, "Party Fetch Failure", Toast.LENGTH_SHORT).show();// handle error in activity_party
                                }
                            } catch (JSONException | NumberFormatException e) {
                                error = true;
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            error = true;
//                            Toast.makeText(runningActivity, "getPartyInfo Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public void updatePartyInfo(String session, Activity runningActivity) {
        isPartyIDUpdated = false;
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/party/get").newBuilder();
        urlBuilder.addQueryParameter("session", session);
        urlBuilder.addQueryParameter("partyid", String.valueOf(partyID));
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        error = true;
//                        Toast.makeText(runningActivity, "Party get Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    // navigate to the party QR code
                                    setPartyID(partyID);
                                    String DESTINATION = json.getString("DESTINATION");
                                    String destinationAddress = json.getString("DESTINATION_LOCATION");
                                    int PARTY_LEADER = json.getInt("PARTY_LEADER");
                                    String PREFERRED_ARRIVAL_TIME = json.getString("PREFERRED_ARRIVAL_TIME");
                                    String DESTINATION_COORDINATE = json.getString("DESTINATION_COORDINATE");
                                    ArrayList<String> DESTINATION_COORDINATES = new ArrayList<>(Arrays.asList(DESTINATION_COORDINATE.split(",")));
                                    Location location = new Location(DESTINATION);
                                    location.setLatitude(Double.parseDouble(DESTINATION_COORDINATES.get(0)));
                                    location.setLongitude(Double.parseDouble(DESTINATION_COORDINATES.get(1)));
                                    partyInfo.updateParty(partyID, location, DESTINATION, destinationAddress, PREFERRED_ARRIVAL_TIME, PARTY_LEADER);
                                    isPartyIDUpdated = true;
//                                    updatePartyCoordinates(session, runningActivity, location.getLatitude() + "," + location.getLongitude(), PREFERRED_ARRIVAL_TIME);
                                    getPartyCoordinates(session, runningActivity);
                                    error = false;
//                                    Toast.makeText(runningActivity, "Party Info Update OK", Toast.LENGTH_SHORT).show();// handle error in activity_party
                                } else {
                                    error = true;
//                                    Toast.makeText(runningActivity, "Party Fetch Failure", Toast.LENGTH_SHORT).show();// handle error in activity_party
                                }
                            } catch (JSONException | NumberFormatException e) {
                                error = true;
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            error = true;
//                            Toast.makeText(runningActivity, "updatePartyInfo Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    /**
     * Retrieves the coordinates and location information of the party session.
     *
     * @param session         The unique identifier of the user session.
     * @param runningActivity The current activity where the coordinates are requested.
     */
    public void getPartyCoordinates(String session, Activity runningActivity) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/party/getcoordinate").newBuilder();
        urlBuilder.addQueryParameter("session", session);
        urlBuilder.addQueryParameter("partyid", String.valueOf(partyID));
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
//                        Toast.makeText(runningActivity, "Failed to get coordinates", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
                                    JSONObject partySta = json.getJSONObject("partySta");
                                    Iterator<String> keys = partySta.keys();
//                                    Toast.makeText(runningActivity, "Party Coordinates Updated" + partySta, Toast.LENGTH_SHORT).show();
                                    for (Iterator<String> it = keys; it.hasNext(); ) {
                                        Integer userID = Integer.parseInt(it.next());
                                        JSONArray details = partySta.getJSONArray(String.valueOf(userID));
                                        String userName = details.getString(0);
                                        String coordinates = details.getString(1);
                                        String eta = details.getString(2);
                                        List<String> coordinatesList = Arrays.asList(coordinates.split(","));
                                        LatLng location = new LatLng(Double.parseDouble(coordinatesList.get(0)), Double.parseDouble(coordinatesList.get(1)));
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                        LocalDateTime localDateTimeETA = LocalDateTime.parse(eta, formatter);
                                        boolean isLeader = partyInfo.getPartyLeader() == userID;
                                        partyInfo.updateDetails(userID, userName, location, localDateTimeETA, isLeader);
//                                        Toast.makeText(runningActivity, userID + ": " + userName + ", " + coordinates + ", " + eta, Toast.LENGTH_SHORT).show();
                                    }
                                    // navigate to the party QR code
                                } else {
//                                    Toast.makeText(runningActivity, "Party Coordinates Update Failure " + message, Toast.LENGTH_SHORT).show();
                                    // handle error in activity_party
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "getPartyCoordinates Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public void updatePartyCoordinates(String session, Activity runningActivity, String coordinate, String arrival_time) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/party/updatecoordinate").newBuilder();
        urlBuilder.addQueryParameter("session", session);
        urlBuilder.addQueryParameter("partyid", String.valueOf(partyID));
        urlBuilder.addQueryParameter("coordinate", coordinate);
        urlBuilder.addQueryParameter("arrival_time", arrival_time);
        List<String> coordinatesList = Arrays.asList(coordinate.split(","));
        LatLng location = new LatLng(Double.parseDouble(coordinatesList.get(0)), Double.parseDouble(coordinatesList.get(1)));

//        double distance = SphericalUtil.computeDistanceBetween(location, departureLocation);

//        if(distance > 20 && !isDeparted){
//            Toast.makeText(runningActivity, "Just departed: " + distance, Toast.LENGTH_SHORT).show();
//            isDeparted = true;
//        }
//        else if(!isDeparted){
////            Toast.makeText(runningActivity, "Not departed: " +  distance, Toast.LENGTH_SHORT).show();
//        }
//        else{
////            Toast.makeText(runningActivity, "Departed: " + distance, Toast.LENGTH_SHORT).show();
//        }

        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
//                        Toast.makeText(runningActivity, "Failed to get coordinates", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    runningActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
//                                    Toast.makeText(runningActivity, "Party Coordinates Updated", Toast.LENGTH_SHORT).show();
                                    // navigate to the party QR code
                                } else {
//                                    Toast.makeText(runningActivity, "Party Coordinates Update Failure" + message, Toast.LENGTH_SHORT).show();
                                    // handle error in activity_party
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "updatePartyCoordinates Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    public void setPartyDetails(LatLng dest, String destName, String arrivalTime) {
        partyInfo.setDestination(dest);
        partyInfo.setDestinationName(destName);
        partyInfo.setPartyEta(arrivalTime);
    }

    public void generatePartyQRAndPin() {

    }

    /**
     * Allows a user to Quit an existing party session by providing the necessary details.
     *
     * @param session         The unique identifier of the party session to Quit.
     * @param partyid         The identifier of the party to Quit.
     * @param runningActivity The current activity where the user Quit the party.
     */
    public void quitParty(String session, String partyid, Activity runningActivity) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/party/quit").newBuilder();
        urlBuilder.addQueryParameter("session", session);
        urlBuilder.addQueryParameter("partyid", partyid);
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
//                        Toast.makeText(runningActivity, "Party Join Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    updateParticipatedParties(session, runningActivity);
                    runningActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
//                                    Toast.makeText(runningActivity, "Party Quit Success", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(runningActivity, "Party Quit Failure" + message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(runningActivity, "Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public void joinParty(String session, String partyid, Activity runningActivity) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sc2006.ericfan.win/party/join").newBuilder();
        urlBuilder.addQueryParameter("session", session);
        urlBuilder.addQueryParameter("partyid", partyid);
        String url = urlBuilder.build().toString();
        httpGETJson(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runningActivity.runOnUiThread(new Runnable() {
                    public void run() {
//                        Toast.makeText(runningActivity, "Party Join Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    updateParticipatedParties(session, runningActivity);
                    runningActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String status = json.getString("status");
                                String message = json.getString("message");
                                if (status.equals("ok")) {
//                                    Toast.makeText(runningActivity, "Party Join Success", Toast.LENGTH_SHORT).show();
                                    partyID = Integer.parseInt(partyid);
                                    getPartyInfo(session, Integer.parseInt(partyid), runningActivity);
                                    // navigate to the party map screen
                                } else {
                                    Toast.makeText(runningActivity, "Party Join Failure" + message, Toast.LENGTH_SHORT).show();
                                    // handle error in activity_party
                                }
                            } catch (JSONException e) {
                                Toast.makeText(runningActivity, "JSON Format Error!", Toast.LENGTH_SHORT).show();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (JSONException | IOException e) {
                    Log.e("Error", Log.getStackTraceString(e));
                    runningActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Toast.makeText(runningActivity, "joinParty Internal Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    public void updateMembers() {

    }

    public boolean getError() {
        return error;
    }

}
