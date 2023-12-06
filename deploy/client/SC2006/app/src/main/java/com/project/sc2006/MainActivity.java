/**
 * This class is part of the OTA Lah project, which aims to improve bus commuting
 * in Singapore by providing real-time transportation information and weather forecasts.
 */

package com.project.sc2006;

import static java.lang.Math.*;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.provider.Settings;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.project.sc2006.controllers.AccountController;
import com.project.sc2006.controllers.DirectionsListAdapter;
import com.project.sc2006.controllers.PartyModeController;
import com.project.sc2006.controllers.RouteController;
import com.project.sc2006.entities.Party;
import com.project.sc2006.entities.PartyParticipantDetails;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONException;
import org.json.JSONObject;

//import java.io.IOException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * This class represents an activity in the OTA Lah app, providing functionality for
 * managing user interactions and screen transitions.
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    protected RouteController routeController;
    protected MainActivity thisActivity;
    protected static PartyModeController partyModeController = PartyModeController.getInstance();
    private MaterialSearchBar searchBar;
    private SlidingUpPanelLayout slider;
    private TextView sliderLocationNameText;
    private TextView sliderLocationAddressText;
    private RelativeLayout sliderMaxHeight;
    private int screenHeight;
    ViewGroup.LayoutParams params;
    private FloatingActionButton preciseLocation;
    private FloatingActionButton createParty;
    private FloatingActionButton checkWeather;
    private FloatingActionsMenu menuFab;

    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private Marker currentMarker;
    private Marker userLastLocation;
    private Place selectedLocation;
    private MaterialDatePicker<Long> datePickerDialog;
    private MaterialTimePicker timePickerDialog;
    private Button dateButton;
    private Button timeButton;
    private Button submitButton;
    private TextView arriveByTxt;
    private ImageView notificationBell;
    private ViewFlipper viewFlipper;
    private MaterialButton editPartyButton;
    private TextView sliderDepartureTime;
    private Polyline currentPolyLine;
    private Date currentArrivalTime;
    private String partyArrivalTimeDisplay;
    private String currentETA;
    private ListView directionsListParty;
    private ListView directionsListSolo;

    private static Map<TimeUnit, Long> remainingTime;
    private boolean isCancelled = false;

    private CardView arrivalTimeCard;
    private TextView arrivalTimeDisplay;
    private LinearLayout profileLayoutAdder;
    private ArrayList<ProfileView> profileViews;
    private String session;
    private Handler mHandler;
    private boolean mStatusCheckerIsRunning = false;
    public static boolean isDirectionListSet = false;

    enum STEP {
        ENTERING_INFO,
        TRAVELLING,
        PARTY_MODE,
        EDIT_PARTY
    }

    private STEP step = STEP.ENTERING_INFO;

    private final float DEFAULT_ZOOM = 16;

    private final String weatherUrl = "http://api.openweathermap.org/data/2.5/weather";

    private final String appid = "5c9accb0d6c1dc2044a286d7f85cc6ef";


    public MainActivity() throws IOException {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            partyModeController.setDepartureInformation(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
            if (resultCode == RESULT_OK) {
                if (currentETA != null) partyModeController.updatePartyCoordinates(session, MainActivity.this, mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude(), currentETA);
                else partyModeController.updatePartyCoordinates(session, MainActivity.this, mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude(), partyModeController.getPartyInfo().getPartyEta());
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                String returnedResult = data.getData().toString();
//                Toast.makeText(MainActivity.this, "Party id" + returnedResult, Toast.LENGTH_SHORT).show();
                new CountDownTimer(300, 1) {
                    public void onTick(long millisUntilFinished) {
                        if (params.height < 0.75 * screenHeight) {
                            params.height += (int) (screenHeight * 0.02);
                            sliderMaxHeight.setLayoutParams(params);
                        }
                    }

                    public void onFinish() {

                    }
                }.start();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        partyModeController.getPartyCoordinates(session, MainActivity.this);
                    }
                }, 1500);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!partyModeController.getError()) {
                            partyModeController.getPartyInfo(session, Integer.parseInt(returnedResult), MainActivity.this);
                            joinPartyView(true);
                        }
                        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                }, 1000);
            }
        } else if (requestCode == 124) {
            partyModeController.setDepartureInformation(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
            if (resultCode == RESULT_OK) {
                if (currentETA != null) partyModeController.updatePartyCoordinates(session, MainActivity.this, mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude(), currentETA);
                else partyModeController.updatePartyCoordinates(session, MainActivity.this, mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude(), partyModeController.getPartyInfo().getPartyEta());
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                String returnedResult = data.getData().toString();
//                Toast.makeText(MainActivity.this, "Party id" + returnedResult, Toast.LENGTH_SHORT);
                new CountDownTimer(300, 1) {
                    public void onTick(long millisUntilFinished) {
                        if (params.height < 0.75 * screenHeight) {
                            params.height += (int) (screenHeight * 0.02);
                            sliderMaxHeight.setLayoutParams(params);
                        }
                    }
                    public void onFinish() {

                    }
                }.start();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        partyModeController.getPartyCoordinates(session, MainActivity.this);
                    }
                }, 1500);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!partyModeController.getError()) {
                            partyModeController.getPartyInfo(session, Integer.parseInt(returnedResult), MainActivity.this);
                            joinPartyView(true);
                        }
                        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                }, 1000);
            }
        }
    }

    public static PartyModeController getPartyModeController() {
        return partyModeController;
    }

    public void updatePartyJob(){
        partyModeController.updatePartyInfo(session, MainActivity.this);
    }


    public void joinPartyView(boolean animateCamera) {
        String arrivalTimeFull;
        String destinationName;
        String destinationAddress;
        Party partyInfo = partyModeController.getPartyInfo();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        if (step == STEP.EDIT_PARTY){
//            arrivalTimeFull = format.format(currentArrivalTime);
//            destinationName = selectedLocation.getName();
//            destinationAddress = selectedLocation.getAddress();
//        } else {
            arrivalTimeFull = partyInfo.getPartyEta();
            destinationName = partyInfo.getDestinationName();
            destinationAddress = partyInfo.getDestinationAddress();
//        }
        step = STEP.PARTY_MODE;
        selectedLocation = Place.builder().setName(destinationName).setLatLng(partyInfo.getDestination()).setAddress(destinationAddress).build();
        remainingTime = null;
        Date date = null;

        if (AccountController.getUserID() == partyInfo.getPartyLeader()) {
            editPartyButton.setVisibility(View.VISIBLE);
            editPartyButton.setEnabled(true);
        } else {
            editPartyButton.setVisibility(View.INVISIBLE);
            editPartyButton.setEnabled(false);
        }

        try {
            if (arrivalTimeFull != null) currentArrivalTime = format.parse(arrivalTimeFull);
            RouteController.JSONParser jParser = new RouteController.JSONParser();
            String url = RouteController.makeURL(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), partyInfo.getDestination().latitude, partyInfo.getDestination().longitude);
            date = format.parse(arrivalTimeFull);
//            Log.d("MyTag", "time in  main"+ String.valueOf(remainingTime));

            try {
                jParser.getJSONFromUrl(url, MainActivity.this, animateCamera);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            Log.d("MyTag", "testing");
//            Log.d("MyTag", "time in  main"+ String.valueOf(remainingTime));
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Time parse error" + e.toString(), Toast.LENGTH_SHORT).show();
        }

        if(currentMarker != null && !currentMarker.getPosition().equals(partyInfo.getDestination())) {
            currentMarker.remove();
            currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(partyInfo.getDestination())
                    .title(partyInfo.getDestinationName()));
        } else if (currentMarker == null) {
            currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(partyInfo.getDestination())
                    .title(partyInfo.getDestinationName()));
        }

        new CountDownTimer(300, 1) {
            public void onTick(long millisUntilFinished) {
                if (params.height < 0.75 * screenHeight) {
                    params.height += (int) (screenHeight * 0.02);
                    sliderMaxHeight.setLayoutParams(params);
                }
            }

            public void onFinish() {

            }
        }.start();

        preciseLocation.animate().translationY(-preciseLocation.getHeight());
        searchBar.animate().alpha(0.0f);
        searchBar.setEnabled(false);
        viewFlipper.setDisplayedChild(3);

        createParty.setVisibility(View.VISIBLE);
        createParty.setEnabled(true);
        createParty.animate().translationY(-preciseLocation.getHeight());
        createParty.animate().alpha(1.0f);

        checkWeather.setVisibility(View.VISIBLE);
        checkWeather.setEnabled(true);
        checkWeather.animate().translationY(-preciseLocation.getHeight());
        checkWeather.animate().alpha(1.0f);

        arrivalTimeCard = findViewById(R.id.arrival_time_card);
        arrivalTimeDisplay = findViewById(R.id.arrival_time_display);

        String arrivalTime = "Meet @ ";
        arrivalTime += String.format("%02d", date.getHours()) + ":";
        arrivalTime += String.format("%02d", date.getMinutes());

        arriveByTxt.setText(arrivalTime);
        arrivalTimeCard.setVisibility(View.VISIBLE);
        arrivalTimeCard.animate().alpha(1.0f);

        slider.setPanelHeight((int) (screenHeight / 4));
        if (!mStatusCheckerIsRunning) slider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                displayIcon();
                if(!mStatusCheckerIsRunning) mStatusChecker.run();
//                mStatusChecker.run();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }, 300);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentETA = "2100-12-12 00:00:00";

//        NotificationManager mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "0")
//                .setSmallIcon(R.drawable.crown)
//                .setContentTitle("OTA Heads Up")
//                .setContentText("Welcome!")
//                .setPriority(NotificationCompat.PRIORITY_MAX);
//        Notification mNotification = builder.build();
//        mNotifyManager.notify(0, mNotification);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);


//        mLastKnownLocation = new Location("none");
//        mLastKnownLocation.setLatitude(1.3521);
//        mLastKnownLocation.setLongitude(103.8198);

        step = STEP.ENTERING_INFO;

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        Intent i = getIntent();
        session = i.getExtras().getString("session");

        searchBar = findViewById(R.id.searchBar);
        slider = findViewById(R.id.slidingUp);
        sliderLocationNameText = findViewById(R.id.slider_location_name_txt);
        sliderLocationAddressText = findViewById(R.id.slider_location_address_txt);
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        sliderMaxHeight = findViewById(R.id.slider_max_height);
        Button sliderSetDestination = findViewById(R.id.slider_set_destination);
        viewFlipper = findViewById(R.id.viewFlipper);
        dateButton = findViewById(R.id.datePickerButton);
        timeButton = findViewById(R.id.timePickerButton);
        submitButton = findViewById(R.id.arrival_time_submit);
        preciseLocation = findViewById(R.id.precise_location);
        createParty = findViewById(R.id.create_party);
        checkWeather = findViewById(R.id.check_weather_fab);
        arriveByTxt = findViewById(R.id.arrive_by_txt);
        editPartyButton = findViewById(R.id.edit_button);
        directionsListParty = findViewById(R.id.list_directions);
        directionsListSolo = findViewById(R.id.list_directions_solo);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        requestPermissions(false,false);
        Places.initialize(MainActivity.this, "AIzaSyCboBOfKpF5BvbpR1JsAZBsqclVtZraD6U");
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        searchBar.setMaxSuggestionCount(10);
        params = sliderMaxHeight.getLayoutParams();
        params.height = (int) (screenHeight / 4.25);
        sliderMaxHeight.setLayoutParams(params);

        profileViews = new ArrayList<>();
        mHandler = new Handler();
        mLocationUpdater.run();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String arrivalDate = dateButton.getText().toString();
                String arrivalTime = timeButton.getText().toString();
                String arrivalTimeFull = arrivalDate + " " + arrivalTime + ":00";
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                remainingTime = null;
                try {
                    RouteController.JSONParser jParser = new RouteController.JSONParser();
                    String url = RouteController.makeURL(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), selectedLocation.getLatLng().latitude, selectedLocation.getLatLng().longitude);
                    Date date = format.parse(arrivalTimeFull);
//                    Log.d("MyTag", "time in  main"+ String.valueOf(remainingTime));
                    currentArrivalTime = date;
                    remainingTime = MainActivity.computeDiff(Date.from(Instant.now()), date);
                    try {
                        jParser.getJSONFromUrl(url, MainActivity.this, true);
//                        Log.d("MyTag", "testing");
//                        Log.d("MyTag", "time in  main"+ String.valueOf(remainingTime));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Time parse error" + e.toString(), Toast.LENGTH_SHORT).show();
                }

                Location destination = new Location(selectedLocation.getName());
                destination.setLongitude(selectedLocation.getLatLng().longitude);
                destination.setLatitude(selectedLocation.getLatLng().latitude);
                if (step == STEP.ENTERING_INFO) {
                    arriveByTxt.setText("Arrive by");
                    createParty.setEnabled(true);
                    partyModeController.createParty(session, destination, selectedLocation.getName(), selectedLocation.getAddress(), arrivalTimeFull, MainActivity.this);
                    partyModeController.setDepartureInformation(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                    step = STEP.TRAVELLING;
                } else if (step == STEP.EDIT_PARTY) {
                    partyModeController.editParty(session, Integer.toString(partyModeController.getPartyID()), destination, selectedLocation.getName(), selectedLocation.getAddress(), arrivalTimeFull, MainActivity.this);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (profileViews != null) {
                                Iterator<ProfileView> iter = profileViews.iterator();
                                while (iter.hasNext()) {
                                    ProfileView view = iter.next();

                                    ((ViewGroup) view.profile.getParent()).removeView(view.profile);
                                    iter.remove();
                                }
                            }
                            joinPartyView(true);
                        }
                    }, 500);
                    return;
                }
                slider.setPanelHeight((int)(screenHeight/4.5));

                new CountDownTimer(300, 1) {
                    public void onTick(long millisUntilFinished) {
                        slider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        if (params.height < 0.75 * screenHeight) {
                            params.height += (int) (screenHeight * 0.02);
                            sliderMaxHeight.setLayoutParams(params);
                        }
                    }

                    public void onFinish() {

                    }
                }.start();

                preciseLocation.animate().translationY(-preciseLocation.getHeight());
                searchBar.animate().alpha(0.0f);
                searchBar.setEnabled(false);
                viewFlipper.setDisplayedChild(2);
            }
        });


        preciseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(true, false);
            }
        });

        searchBar.findViewById(com.mancj.materialsearchbar.R.id.mt_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.closeSearch();
                if (currentMarker != null && step == STEP.ENTERING_INFO) {currentMarker.remove(); currentMarker = null;}
            }
        });

        slider.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (step == STEP.ENTERING_INFO && newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    params.height = (int) (screenHeight / 4.25);
                    sliderMaxHeight.setLayoutParams(params);
                    viewFlipper.setDisplayedChild(0);
                    selectedLocation = null;
                    if (currentMarker != null) {currentMarker.remove(); currentMarker = null;}
                }
                if (step == STEP.EDIT_PARTY && newState == SlidingUpPanelLayout.PanelState.COLLAPSED){
                    currentMarker.remove();
                    currentMarker = null;
                    createParty.setVisibility(View.VISIBLE);
                    joinPartyView(true);
                }
            }
        });

        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (enabled) menuFab.collapse();
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                searchBar.clearSuggestions();
            }

            @Override
            public void onButtonClicked(int buttonCode) {
            }
        });

        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    searchBar.updateLastSuggestions(new ArrayList<>());
                    return;
                }
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(token)
                        .setQuery(charSequence.toString())
                        .setCountries("sg")
                        .setOrigin(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()))
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString() + " (" + prediction.getDistanceMeters() + "m)");
                                }
                                searchBar.updateLastSuggestions(suggestionsList);
                                if (!searchBar.isSuggestionsVisible()) {
                                    searchBar.showSuggestionsList();
                                }
                            }
                        } else {
                            Log.i("error", "prediction fetching task unsuccessful");
                            Toast.makeText(MainActivity.this, "Prediction failed", Toast.LENGTH_SHORT);
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

        searchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (predictionList == null) return;
                if (position >= predictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                if (currentMarker != null) {currentMarker.remove(); currentMarker = null;}

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchBar.clearSuggestions();
                    }
                }, 150);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(searchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                final String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.NAME);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.i("found", "Place found: " + place.getName());
                        LatLng latLngOfPlace = place.getLatLng();
                        if (latLngOfPlace != null) {
                            if (slider.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                                if (currentMarker != null) currentMarker.remove();
                                currentMarker = mMap.addMarker(new MarkerOptions()
                                        .position(latLngOfPlace)
                                        .title(place.getName()));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM));
                                selectedLocation = place;
                                searchBar.closeSearch();
                                searchBar.hideSuggestionsList();
                                viewFlipper.setDisplayedChild(0);
                                sliderLocationNameText.setText(selectedLocation.getName());
                                sliderLocationAddressText.setText(selectedLocation.getAddress());
                                new CountDownTimer(300, 1) {
                                    public void onTick(long millisUntilFinished) {
                                        if (params.height >= screenHeight / 4.25) {
                                            params.height -= (int) (screenHeight * 0.02);
                                            sliderMaxHeight.setLayoutParams(params);
                                        }
                                    }

                                    public void onFinish() {

                                    }
                                }.start();
                                return;
                            }
                            if (currentMarker != null) currentMarker.remove();
                            currentMarker = mMap.addMarker(new MarkerOptions()
                                    .position(latLngOfPlace)
                                    .title(place.getName()));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM));
                            selectedLocation = place;
                            searchBar.closeSearch();
                            searchBar.hideSuggestionsList();

                            slider.setPanelHeight(0);
                            slider.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                            sliderLocationNameText.setText(place.getName());
                            sliderLocationAddressText.setText(place.getAddress());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i("error", "place not found: " + e.getMessage());
                            Log.i("status", "status code: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

        sliderSetDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper.setDisplayedChild(1);
                SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Calendar cal = Calendar.getInstance();
                Date date = cal.getTime();
                if (step == STEP.EDIT_PARTY){
                    String[] time = partyModeController.getPartyInfo().getPartyEta().split(" ");
                    dateButton.setText(time[0]);
                    timeButton.setText(time[1].substring(0, time[1].length() - 3));
                } else {
                    dateButton.setText(simpleFormat.format(date));
                    timeButton.setText(getTime());
                }
                initDatePicker();
                initTimePicker();
                TextView arrivalName = findViewById(R.id.arrival_location_name_txt);
                TextView arrivalAddress = findViewById(R.id.arrival_location_address_txt);
                arrivalName.setText(selectedLocation.getName());
                arrivalAddress.setText(selectedLocation.getAddress());
                if (step == STEP.ENTERING_INFO){
                    submitButton.setText("Submit");
                } else if (step == STEP.EDIT_PARTY){
                    submitButton.setText("Update");

                }
                new CountDownTimer(300, 1) {
                    public void onTick(long millisUntilFinished) {
                        if (params.height <= (int) (screenHeight * 0.45)) {
                            params.height += (int) (screenHeight * 0.02);
                            sliderMaxHeight.setLayoutParams(params);
                        }
                    }

                    public void onFinish() {

                    }
                }.start();
            }
        });

        menuFab = findViewById(R.id.open_fab_menu);

        final FloatingActionButton joinPartyFab = findViewById(R.id.join_party_fab);
        joinPartyFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuFab.collapse();
                Intent i = new Intent(MainActivity.this, JoinPartyActivity.class);
                i.putExtra("session", session);
                startActivityForResult(i, 123);
            }
        });

        final FloatingActionButton settingsFab = findViewById(R.id.settings_fab);
        settingsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuFab.collapse();
            }
        });

        final FloatingActionButton historyFab = findViewById(R.id.party_history);
        historyFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuFab.collapse();
                Intent i = new Intent(MainActivity.this, PartyHistoryActivity.class);
                i.putExtra("session", session);
//                MainActivity.this.startActivity(i);
                MainActivity.this.startActivityForResult(i, 124);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (step == STEP.PARTY_MODE) {
            joinPartyView(false);
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            mStatusCheckerIsRunning = true;
            if (step != STEP.EDIT_PARTY){
                if (currentETA != null && currentETA.length() > 0) partyModeController.updatePartyCoordinates(session, MainActivity.this, mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude(), currentETA);
                else partyModeController.updatePartyCoordinates(session, MainActivity.this, mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude(), partyModeController.getPartyInfo().getPartyEta());
                partyModeController.getPartyCoordinates(session, MainActivity.this);
                partyModeController.getNotifications(session, MainActivity.this);
                partyModeController.updatePartyInfo(session, MainActivity.this);
                displayIcon();
                joinPartyView(false);
            }
            else{
            }
            mHandler.postDelayed(mStatusChecker, 2000);
        }
    };

    public Handler getmHandler() {
        return mHandler;
    }

    Runnable mLocationUpdater = new Runnable() {
        @Override
        public void run() {
            getDeviceLocation(false);
            mHandler.postDelayed(mLocationUpdater, 5000);
        }
    };

    public void displayIcon() {
        if (isCancelled) {
            if (profileViews != null) {
                Iterator<ProfileView> iter = profileViews.iterator();
                while (iter.hasNext()) {
                    ProfileView view = iter.next();

                    ((ViewGroup) view.profile.getParent()).removeView(view.profile);
                    iter.remove();
                }
            }
            isCancelled = false;
        }
        LayoutInflater mInflater = LayoutInflater.from(getApplicationContext());
        profileLayoutAdder = findViewById(R.id.profile_linear_layout);
        ArrayList<PartyParticipantDetails> partyMembers = partyModeController.getPartyInfo().getMembersDetails();
        for (PartyParticipantDetails member : partyMembers) {
            View profile = mInflater.inflate(R.layout.party_profile_picture, null, false);

            String time = String.format("%02d",member.getEta().getHour()) + ":" + String.format("%02d",member.getEta().getMinute());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime partyETA = LocalDateTime.parse(partyModeController.getPartyInfo().getPartyEta(), formatter);

            if (member.getIsDisplayed()) {
                ProfileView profileView;
                for(int i = 0 ; i < profileViews.size() ; i ++){
                    if(profileViews.get(i).userID == member.getUserID()){
                        profileView = profileViews.get(i);

                        if (profileView.userID != AccountController.getUserID() && !profileView.marker.getPosition().equals(member.getLocation())){
                            if (profileView.marker != null) animateMarkerToHC(profileView.marker, member.getLocation(), new LatLngInterpolator.Spherical());
                        }

                        View targetProfile = profileView.profile;
                        if (member.getEta().isAfter(partyETA)) {
                            if(targetProfile.findViewById(profileView.timeId) == null) {
//                                Toast.makeText(MainActivity.this, "Empty View @" + member.getUserID() + ", " + profileView.timeId, Toast.LENGTH_SHORT).show();
                                continue;
                            }
                            ((TextView) targetProfile.findViewById(profileView.timeId)).setTextColor(Color.RED);
                            ((TextView) targetProfile.findViewById(profileView.timeId)).setTypeface(null, Typeface.BOLD);
                            targetProfile.findViewById(profileView.bellId).setVisibility(View.VISIBLE);
                        } else {
                            if(targetProfile.findViewById(profileView.timeId) == null) {
//                                Toast.makeText(MainActivity.this, "Empty View @" + member.getUserID() + ", " + profileView.timeId, Toast.LENGTH_SHORT).show();
                                continue;
                            }
                            ((TextView) targetProfile.findViewById(profileView.timeId)).setTextColor(Color.parseColor("#808080"));
                            ((TextView) targetProfile.findViewById(profileView.timeId)).setTypeface(null, Typeface.NORMAL);
                            targetProfile.findViewById(profileView.bellId).setVisibility(View.INVISIBLE);
                        }
                        ((TextView)targetProfile.findViewById(profileView.timeId)).setText(time);
                        if (member.getIsLeader())
                            targetProfile.findViewById(profileView.crownId).setVisibility(View.VISIBLE);
                        ((TextView) targetProfile.findViewById(profileView.usernameId)).setText(member.getUserName());
                        break;
                    }
                }
//                Toast.makeText(MainActivity.this, "User Not Found!" + member.getUserID(), Toast.LENGTH_SHORT).show();
                continue;
            }
            Marker userMarker = null;
            if (member.getUserID() != AccountController.getUserID()) userMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(member.getLocation().latitude, member.getLocation().longitude))
                    .title(member.getUserName()));
            else userMarker = userLastLocation;
            ProfileView profileView = new ProfileView(profile, member.getUserID(), View.generateViewId(), View.generateViewId(), View.generateViewId(), View.generateViewId(), View.generateViewId(), userMarker);

            profile.findViewById(R.id.notify_bell).setId(profileView.bellId);
            profile.findViewById(R.id.user_crown).setId(profileView.crownId);
            profile.findViewById(R.id.time).setId(profileView.timeId);
            profile.findViewById(R.id.username).setId(profileView.usernameId);
            profile.findViewById(R.id.user_icon).setId(profileView.iconId);
            profileViews.add(profileView);

            if (member.getEta().isAfter(partyETA)){
                ((TextView)profile.findViewById(profileView.timeId)).setTextColor(Color.RED);
                ((TextView)profile.findViewById(profileView.timeId)).setTypeface(null, Typeface.BOLD);
                profile.findViewById(profileView.bellId).setVisibility(View.VISIBLE);
            } else {
                ((TextView) profile.findViewById(profileView.timeId)).setTextColor(Color.parseColor("#808080"));
                ((TextView)profile.findViewById(profileView.timeId)).setTypeface(null, Typeface.NORMAL);
                profile.findViewById(profileView.bellId).setVisibility(View.INVISIBLE);
            }

            ((TextView)profile.findViewById(profileView.timeId)).setText(time);

            if (member.getIsLeader())
                profile.findViewById(profileView.crownId).setVisibility(View.VISIBLE);
            ((TextView) profile.findViewById(profileView.usernameId)).setText(member.getUserName());
            switch (member.getUserID() % 5) {
                case 0:
                    ((ImageView) profile.findViewById(profileView.iconId)).setImageResource(R.drawable.user_icon_1);
                    if (member.getUserID() != AccountController.getUserID()) userMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.user_icon_1));
                    break;
                case 1:
                    ((ImageView) profile.findViewById(profileView.iconId)).setImageResource(R.drawable.user_icon_2);
                    if (member.getUserID() != AccountController.getUserID()) userMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.user_icon_2));
                    break;
                case 2:
                    ((ImageView) profile.findViewById(profileView.iconId)).setImageResource(R.drawable.user_icon_3);
                    if (member.getUserID() != AccountController.getUserID()) userMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.user_icon_3));
                    break;
                case 3:
                    ((ImageView) profile.findViewById(profileView.iconId)).setImageResource(R.drawable.user_icon_4);
                    if (member.getUserID() != AccountController.getUserID()) userMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.user_icon_4));
                    break;
                case 4:
                    ((ImageView) profile.findViewById(profileView.iconId)).setImageResource(R.drawable.user_icon_5);
                    if (member.getUserID() != AccountController.getUserID()) userMarker.setIcon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.user_icon_5));
                    break;
            }

            profileLayoutAdder.addView(profile);
            profile.findViewById(profileView.iconId).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(profileView.marker.getPosition(), DEFAULT_ZOOM));
                }
            });
            profile.findViewById(profileView.bellId).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    profile.findViewById(profileView.bellId).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake));
                    partyModeController.sendNotifications(session, String.valueOf(member.getUserID()),  LoginActivity.accountController.getUserName() + ": Hey It's Time to Go!", MainActivity.this);
                }
            });
            member.setIsDisplayed(true);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(MainActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation(true);
            }
        });

        task.addOnFailureListener(MainActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(MainActivity.this, 51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (searchBar.isSuggestionsVisible())
                    searchBar.clearSuggestions();
                return false;
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation(boolean moveCamera) {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                if(moveCamera) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                // display user location here
                                LatLng newLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                                if(userLastLocation != null && !userLastLocation.getPosition().equals(newLatLng)) {
//                                    userLastLocation.setPosition(newLatLng);
                                    animateMarkerToHC(userLastLocation, newLatLng, new LatLngInterpolator.Spherical());
                                } else if (userLastLocation == null) {
                                    userLastLocation = mMap.addMarker(new MarkerOptions().position(newLatLng).title("Your location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user_current_location)));
                                }
                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(3000);
                                locationRequest.setFastestInterval(2000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        mLastKnownLocation = locationResult.getLastLocation();
                                        if(moveCamera) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        if (userLastLocation != null) userLastLocation.remove();
                                        userLastLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("Your location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user_current_location)));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                        displayIcon();
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        } else {
                            Toast.makeText(MainActivity.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;

    /**
     * Styles the appearance of a Polyline on the map.
     *
     * @param polyline The Polyline object to be styled.
     */
    private void stylePolyline(Polyline polyline) {
        String type = "";
        // Get the data object stored with the polyline.
        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "A":
                // Use a custom bitmap as the cap at the start of the line.
                polyline.setStartCap(
                        new CustomCap(
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10));
                break;
            case "B":
                // Use a round cap at the start of the line.
                polyline.setStartCap(new RoundCap());
                break;
        }

        polyline.setEndCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(COLOR_BLACK_ARGB);
        polyline.setJointType(JointType.ROUND);
    }

    /**
     * Draws a path on the map based on the provided result.
     *
     * @param result The path result to be displayed on the map.
     */
    public void drawPath(JSONObject result, boolean animateCamera) {

        try {

            JSONObject overviewPolylines = result.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(12)
                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                    .geodesic(true)
            );
            if (animateCamera) routeCameraZoom(line);
            line.setTag("Route");
            if (currentPolyLine != null) currentPolyLine.remove();
            currentPolyLine = line;
           /*
           for(int z = 0; z<list.size()-1;z++){
                LatLng src= list.get(z);
                LatLng dest= list.get(z+1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                .width(2)
                .color(Color.BLUE).geodesic(true));
            }
           */
        } catch (JSONException e) {

        }
    }

    /**
     * Decodes a polyline encoded string into a list of LatLng coordinates.
     *
     * @param encoded The encoded polyline string to be decoded.
     * @return A list of LatLng coordinates representing the decoded path.
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    /**
     * Adjusts the camera zoom to focus on a specific Polyline on the map.
     *
     * @param polyline The Polyline to center the camera view on.
     */
    public void routeCameraZoom(Polyline polyline) {
        boolean hasPoints = false;
        Double maxLat = null, minLat = null, minLon = null, maxLon = null;

        if (polyline != null && polyline.getPoints() != null) {
            List<LatLng> pts = polyline.getPoints();
            for (LatLng coordinate : pts) {
                // Find out the maximum and minimum latitudes & longitudes
                // Latitude
                maxLat = maxLat != null ? Math.max(coordinate.latitude, maxLat) : coordinate.latitude;
                minLat = minLat != null ? Math.min(coordinate.latitude, minLat) : coordinate.latitude;

                // Longitude
                maxLon = maxLon != null ? Math.max(coordinate.longitude, maxLon) : coordinate.longitude;
                minLon = minLon != null ? Math.min(coordinate.longitude, minLon) : coordinate.longitude;

                hasPoints = true;
            }
        }

        if (hasPoints) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(maxLat, maxLon));
            builder.include(new LatLng(minLat, minLon));
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 48));
        }
    }


    private void initDatePicker() {
        CalendarConstraints constraintsBuilder = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now()).build();
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker().setCalendarConstraints(constraintsBuilder);
        builder.setTitleText("Choose arrival date");
        datePickerDialog = builder.build();
        datePickerDialog.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        TimeZone timeZoneUTC = TimeZone.getDefault();
                        // It will be negative, so that's the -1
                        int offsetFromUTC = timeZoneUTC.getOffset(new Date().getTime()) * -1;
                        // Create a date format, then a date object with our offset
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        Date date = new Date(selection + offsetFromUTC);
                        dateButton.setText(simpleFormat.format(date));

                    }
                });
    }

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }

    private String makeDateString(int day, int month, int year) {
        String dayString;

        if (day < 10) dayString = "0" + day;
        else dayString = Integer.toString(day);
        return getMonthFormat(month) + " " + dayString + " " + year;
    }

    private String getMonthFormat(int month) {
        if (month == 1)
            return "Jan";
        if (month == 2)
            return "Feb";
        if (month == 3)
            return "Mar";
        if (month == 4)
            return "Apr";
        if (month == 5)
            return "May";
        if (month == 6)
            return "Jun";
        if (month == 7)
            return "Jul";
        if (month == 8)
            return "Aug";
        if (month == 9)
            return "Sep";
        if (month == 10)
            return "Oct";
        if (month == 11)
            return "Nov";
        if (month == 12)
            return "Dec";

        //default should never happen
        return "Jan";
    }

    public void openDatePicker(View view) {
        datePickerDialog.show(getSupportFragmentManager(), "Set Arrival Date");
    }

    private void initTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        timePickerDialog = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(hour)
                .setMinute(minute)
                .build();

        timePickerDialog.addOnPositiveButtonClickListener(dialog -> {
            int newHour = timePickerDialog.getHour();
            int newMinute = timePickerDialog.getMinute();
            timeButton.setText(makeTimeString(newHour, newMinute));
        });
    }

    private String makeTimeString(int hour, int minute) {
        String hourString, minString;
        if (hour < 10) hourString = "0" + hour;
        else hourString = Integer.toString(hour);

        if (minute < 10) minString = "0" + minute;
        else minString = Integer.toString(minute);
        return hourString + ":" + minString;
    }

    private String getTime() {
        String hourString, minString;
        Calendar cal = Calendar.getInstance();

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour < 10) hourString = "0" + hour;
        else hourString = Integer.toString(hour);

        int min = cal.get(Calendar.MINUTE);
        if (min < 10) minString = "0" + min;
        else minString = Integer.toString(min);

        return hourString + ":" + minString;
    }

    /**
     * Calculates the remaining time until the user needs to leave based on the given duration,
     * and updates the user interface to display the calculated remaining time.
     *
     * @param duration The total duration in minutes the user needs to reach the destination.
     *                 This can be the estimated travel time or any relevant time parameter.
     *
     * Example usage:
     * If the estimated travel time is 45 minutes, calling setRemainingTime(45) will calculate
     * the remaining time until departure and update the user interface accordingly.
     */
    public void setRemainingTime(int duration){
        long time = currentArrivalTime.getTime()/1000;
//        Log.d("MyTag", "arrival time " + String.valueOf(currentArrivalTime));

        LocalDateTime localDateTime = LocalDateTime.now();
        localDateTime = localDateTime.plusSeconds(duration);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        currentETA = localDateTime.format(formatter);

        time-=duration;
        Date departureDate = new Date(time*1000);
//        Log.d("MyTag", "departure time " + String.valueOf(departureDate));
        MainActivity.remainingTime = MainActivity.computeDiff(Calendar.getInstance().getTime(), departureDate);
        sliderDepartureTime = findViewById(R.id.slider_time);
        if (remainingTime != null) {
            int days = Math.toIntExact(remainingTime.get(TimeUnit.DAYS));
            int hours = Math.toIntExact(remainingTime.get(TimeUnit.HOURS));
            int min = Math.toIntExact(remainingTime.get(TimeUnit.MINUTES));
            if (min < 0 || hours < 0 || days < 0) sliderDepartureTime.setText("LATE!!!");
            else if (days == 0 && hours == 0) sliderDepartureTime.setText(min + "min");
            else if (days == 0) sliderDepartureTime.setText(hours + "hr " + min + "min");
            else sliderDepartureTime.setText(days + "d " + hours + "hr " + min + "min");

            partyArrivalTimeDisplay = "";
            if (days == 0 && hours == 0) partyArrivalTimeDisplay += min + "min";
            else if (days == 0) partyArrivalTimeDisplay += hours + "hr " + min + "min";
            else partyArrivalTimeDisplay += days + "d " + hours + "hr " + min + "min";

            if (step == STEP.PARTY_MODE) {
                if (min < 0 || hours < 0 || days < 0) arrivalTimeDisplay.setText("LATE!!!");
                else arrivalTimeDisplay.setText("Leave in " + partyArrivalTimeDisplay);
                return;
            }

//            Log.d("MyTag", "testing");
//            Log.d("MyTag", "time in  main" + String.valueOf(remainingTime));
            createParty.setVisibility(View.VISIBLE);
            createParty.animate().translationY(-preciseLocation.getHeight());
            createParty.animate().alpha(1.0f);

            checkWeather.setVisibility(View.VISIBLE);
            checkWeather.setEnabled(true);
            checkWeather.animate().translationY(-preciseLocation.getHeight());
            checkWeather.animate().alpha(1.0f);

            arrivalTimeCard = findViewById(R.id.arrival_time_card);
            arrivalTimeDisplay = findViewById(R.id.arrival_time_display);

            arrivalTimeDisplay.setText(String.format("%02d",currentArrivalTime.getHours()) + ":" + String.format("%02d", currentArrivalTime.getMinutes()));
            arrivalTimeCard.setVisibility(View.VISIBLE);
            arrivalTimeCard.animate().alpha(1.0f);
        }
    }

    public void openTimePicker(View view) {
        timePickerDialog.show(getSupportFragmentManager(), "Set Arrival Time");
    }

    /**
     * Requests necessary permissions for the OTA Lah app to function properly.
     * This method should be called to ensure that the app has the required permissions
     * for location services, map usage, and other essential features.
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestPermissions(boolean location, boolean camera) {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            if (location) getDeviceLocation(true);
                            else if (!location) getDeviceLocation(false);
                            else if (camera){
                                // do camera stuff
                            }
                        }
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(error -> {
                    Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                })
                .onSameThread().check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Need Permissions");

        builder.setMessage("This app requires location, notifications and camera permissions. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            dialog.cancel();

            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 101);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
        });
        builder.show();
    }

    public void returnToDestinationSelection(View view) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        for (ProfileView profileView : profileViews) {
            if (AccountController.getUserID() != profileView.userID) profileView.marker.remove();
        }

        isDirectionListSet = false;

        currentETA = "2100-12-12 00:00:00";
        isCancelled = true;
        params.height = (int) (screenHeight / 4.25);
        sliderMaxHeight.setLayoutParams(params);
        slider.setPanelHeight(0);
        slider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        preciseLocation.animate().translationY(0);
        searchBar.animate().alpha(1.0f);
        createParty.animate().alpha(0.0f);
        checkWeather.animate().alpha(0.0f);
        arrivalTimeCard.animate().alpha(0.0f);
        searchBar.setEnabled(true);
        createParty.setEnabled(false);
        checkWeather.setEnabled(false);
        if (step == STEP.PARTY_MODE) {
            mHandler.removeCallbacks(mStatusChecker);
            mStatusCheckerIsRunning = false;
        }
        createParty.setVisibility(View.GONE);
        checkWeather.setVisibility(View.GONE);
        viewFlipper.setDisplayedChild(0);
        arrivalTimeCard.setVisibility(View.GONE);
        if (currentPolyLine != null) currentPolyLine.remove();
        currentPolyLine = null;
        if (currentMarker != null) currentMarker.remove();
        currentMarker = null;
        step = STEP.ENTERING_INFO;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentPolyLine != null) returnToDestinationSelection(null);
            }
        }, 1000);

    }

    public static Map<TimeUnit, Long> computeDiff(Date date1, Date date2) {
        long diffInMillies = date2.getTime() - date1.getTime();
        List<TimeUnit> units = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);
        Map<TimeUnit, Long> result = new LinkedHashMap<TimeUnit, Long>();
        long milliesRest = diffInMillies;
        for (TimeUnit unit : units) {
            long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
            long diffInMilliesForUnit = unit.toMillis(diff);
            milliesRest = milliesRest - diffInMilliesForUnit;
            result.put(unit, diff);
        }
        return result;
    }

    public void openCreateParty(View view) {
        slider.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        Intent i = new Intent(MainActivity.this, CreatePartyActivity.class);
        i.putExtra("location", selectedLocation);
        i.putExtra("time", currentArrivalTime);
        i.putExtra("isCancelled", isCancelled);
        i.putExtra("session", session);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        step = STEP.PARTY_MODE;
        if (mHandler != null) mHandler.removeCallbacks(mStatusChecker);
        mStatusCheckerIsRunning = false;

        startActivity(i);
    }

    public void openDisplayWeather(View view){
        Intent i = new Intent(MainActivity.this, WeatherActivity.class);
        i.putExtra("location", selectedLocation);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if(mHandler != null) {
            mHandler.removeCallbacks(mStatusChecker);
            mStatusCheckerIsRunning = false;
        }

        startActivity(i);
    }

    public void editParty(View view){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        isDirectionListSet = false;
        mHandler.removeCallbacks(mStatusChecker);
        mStatusCheckerIsRunning = false;
        arrivalTimeCard.setVisibility(View.GONE);
        step = STEP.EDIT_PARTY;
        if (currentPolyLine != null) currentPolyLine.setVisible(false);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentMarker.getPosition(), DEFAULT_ZOOM));
        slider.setPanelHeight(0);
        new CountDownTimer(300, 1) {
            public void onTick(long millisUntilFinished) {
                if (params.height > screenHeight / 4.25) {
                    params.height -= (int) (screenHeight * 0.02);
                    sliderMaxHeight.setLayoutParams(params);
                    slider.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }
            }

            public void onFinish() {

            }
        }.start();

        viewFlipper.setDisplayedChild(0);
        sliderLocationNameText.setText(selectedLocation.getName());
        sliderLocationAddressText.setText(selectedLocation.getAddress());
        arrivalTimeCard.animate().alpha(0.0f);
        preciseLocation.animate().translationY(0);
        createParty.animate().alpha(0.0f);
        checkWeather.animate().translationY(0);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                createParty.setEnabled(false);
                createParty.setVisibility(View.GONE);

                searchBar.animate().alpha(1.0f);
                searchBar.setEnabled(true);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }, 350);




    }

    public void setDirectionsList(ArrayList<RouteController.direction> list) {
        DirectionsListAdapter adapter = new DirectionsListAdapter(this, list);
        if (step == STEP.PARTY_MODE) directionsListParty.setAdapter(adapter);
        else if (step == STEP.TRAVELLING) directionsListSolo.setAdapter(adapter);
    }

    class ProfileView {
        public View profile;
        public int userID;
        public int bellId;
        public int crownId;
        public int usernameId;
        public int timeId;
        public int iconId;
        public Marker marker;

        public ProfileView(View profile, int userID, int bell, int crown, int username, int icon, int time, Marker marker){
            this.profile = profile;
            this.userID = userID;
            bellId = bell;
            crownId = crown;
            usernameId = username;
            timeId = time;
            iconId = icon;
            this.marker = marker;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static void animateMarkerToHC(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = animation.getAnimatedFraction();
                LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition);
                marker.setPosition(newPosition);
            }
        });
        valueAnimator.setFloatValues(0, 1); // Ignored.
        valueAnimator.setDuration(3000);
        valueAnimator.start();
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, 75, 75);
        Bitmap bitmap = Bitmap.createBitmap(75, 75, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public interface LatLngInterpolator {
        public LatLng interpolate(float fraction, LatLng a, LatLng b);
        public class Spherical implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng from, LatLng to) {
                double fromLat = toRadians(from.latitude);
                double fromLng = toRadians(from.longitude);
                double toLat = toRadians(to.latitude);
                double toLng = toRadians(to.longitude);
                double cosFromLat = cos(fromLat);
                double cosToLat = cos(toLat);

                // Computes Spherical interpolation coefficients.
                double angle = computeAngleBetween(fromLat, fromLng, toLat, toLng);
                double sinAngle = sin(angle);
                if (sinAngle < 1E-6) {
                    return from;
                }
                double a = sin((1 - fraction) * angle) / sinAngle;
                double b = sin(fraction * angle) / sinAngle;

                // Converts from polar to vector and interpolate.
                double x = a * cosFromLat * cos(fromLng) + b * cosToLat * cos(toLng);
                double y = a * cosFromLat * sin(fromLng) + b * cosToLat * sin(toLng);
                double z = a * sin(fromLat) + b * sin(toLat);

                // Converts interpolated vector back to polar.
                double lat = atan2(z, sqrt(x * x + y * y));
                double lng = atan2(y, x);
                return new LatLng(toDegrees(lat), toDegrees(lng));
            }

            private double computeAngleBetween(double fromLat, double fromLng, double toLat, double toLng) {
                // Haversine's formula
                double dLat = fromLat - toLat;
                double dLng = fromLng - toLng;
                return 2 * asin(sqrt(pow(sin(dLat / 2), 2) +
                        cos(fromLat) * cos(toLat) * pow(sin(dLng / 2), 2)));
            }
        }
    }

}