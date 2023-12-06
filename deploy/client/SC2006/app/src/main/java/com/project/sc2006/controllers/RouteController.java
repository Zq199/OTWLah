/**
 * The MapActivity class provides functionality for map-related actions within the OTA Lah app.
 * This class is responsible for generating URLs for map requests, parsing JSON data from URLs,
 * and interacting with the map interface.
 */
package com.project.sc2006.controllers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.project.sc2006.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RouteController {
    public static class direction{
        public LatLng start;
        public LatLng end;
        public String mode;
        public String startName;
        public String endName;
        public String instructions;

        public direction(LatLng Start,LatLng end,String mode){
            this.start = Start;
            this.end = end;
            this.mode = mode;
            this.startName = "";
            this.endName = "";
            this.instructions = "";
        }

        public void setInstructions(String instructions) {
            this.instructions = instructions;
        }

        public void setEndName(String endName) {
            this.endName = endName;
        }

        public void setStartName(String startName) {
            this.startName = startName;
        }
    }
    public static direction getInstruction(JSONObject step, String mode,direction direction){
        if(mode.equals("WALKING")){
            try {
                direction.setInstructions(step.getString("html_instructions"));
//                Log.d("MyTag", direction.instructions);
            }catch (JSONException e) {
                Log.d("Error", String.valueOf(e.getMessage()));
            }
        }
        if(mode.equals("TRANSIT")){
            try {
                JSONObject transitDetail = step.getJSONObject("transit_details");
                JSONObject line = transitDetail.getJSONObject("line");
                String startName = transitDetail.getJSONObject("departure_stop").getString("name");
                direction.setStartName(startName);
                String endName = transitDetail.getJSONObject("arrival_stop").getString("name");
                direction.setStartName(endName);
                JSONObject vehicle = line.getJSONObject("vehicle");
                String type = vehicle.getString("type");
                if(type.equals("BUS")){
                    String busNo = line.getString("name");
                    String stops = transitDetail.getString("num_stops");
                    direction.setInstructions( "Take Bus "+ busNo + " for " + stops + " stops.");
//                    Log.d("MyTag", direction.instructions);
                }
                if(type.equals("SUBWAY")){
                    String mrt = line.getString("name");
                    String stops = transitDetail.getString("num_stops");
                    direction.setInstructions("Take MRT "+ mrt + " for " + stops + " stops.");
//                    Log.d("MyTag", direction.instructions);
                }
            }catch (JSONException e) {
                Log.d("Error", String.valueOf(e.getMessage()));
            }
        }
        return direction;
    }

    public static List<direction> getDirections(JSONObject direction){
        List<direction> directions = new ArrayList<direction>();
        try {
            JSONArray route = direction.getJSONArray("legs");
            JSONObject details = route.getJSONObject(0);
//            Log.d("MyTag", String.valueOf(details));
            JSONArray steps = details.getJSONArray("steps");
//            Log.d("MyTag", String.valueOf(steps));
            for (int i = 0 ; i < steps.length(); i++) {
                JSONObject stepObj = steps.getJSONObject(i);
//                Log.d("MyTag", String.valueOf(stepObj));
                JSONObject startObj = stepObj.getJSONObject("start_location");
                double startLat = startObj.getDouble("lat");
                double startLng = startObj.getDouble("lng");
                LatLng start = new LatLng(startLat,startLng);
                JSONObject endObj = stepObj.getJSONObject("end_location");
                double endLat = endObj.getDouble("lat");
                double endLng = endObj.getDouble("lng");
                LatLng end = new LatLng(endLat,endLng);
                String mode =stepObj.getString("travel_mode");
//                Log.d("MyTag", String.valueOf(mode));
                direction direction1=new direction(start,end,mode);
                direction1 = getInstruction(stepObj,mode,direction1);
                directions.add(direction1);
            }
        }catch (JSONException e) {
            Log.d("Error", String.valueOf(e.getMessage()));
        }

        return directions;
    }

    public interface OKHttpNetwork {
        void onSuccess(String body);

        void onFailure();
    }

    /**
     * Generates a URL for map-related requests, specifying the source and destination coordinates.
     *
     * @param sourcelat  The latitude of the source location.
     * @param sourcelog  The longitude of the source location.
     * @param destlat    The latitude of the destination location.
     * @param destlog    The longitude of the destination location.
     * @return A URL for map-related requests.
     */
    public static String makeURL(double sourcelat, double sourcelog, double destlat, double destlog) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString.append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=transit&alternatives=true");
        urlString.append("&key=AIzaSyCboBOfKpF5BvbpR1JsAZBsqclVtZraD6U");
        return urlString.toString();
    }

    /**
     * The JSONParser class provides methods for retrieving JSON data from a specified URL.
     */
    public static class JSONParser {

        InputStream is = null;
        String json = "";

        // constructor
        public JSONParser() {
        }

        /**
         * Retrieves JSON data from the provided URL and handles it using a callback.
         *
         * @param url            The URL to fetch JSON data from.
         * @param runningActivity The MainActivity instance where the data retrieval is initiated.
         * @throws IOException if an I/O error occurs while fetching data from the URL.
         */
        public void getJSONFromUrl(String url, MainActivity runningActivity, boolean animateCamera) throws IOException {

            // Making HTTP request
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                    Log.w("Error", response.body().string());
                    is = response.body().byteStream();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                is, "iso-8859-1"), 8);
                        StringBuilder sb = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line + "\n");
                        }

                        json = sb.toString();
                        is.close();
                        JSONObject routes = null;
                        int seconds = 0;
                        try {
                            final JSONObject result = new JSONObject(json);
                            JSONArray routeArray = result.getJSONArray("routes");
//                            Log.d("MyTag", String.valueOf(routeArray));
                            routes = routeArray.getJSONObject(0);
//                            Log.d("MyTag", String.valueOf(routes));
                            JSONArray route = routes.getJSONArray("legs");
//                            Log.d("MyTag", "legs"+String.valueOf(route));
                            JSONObject details = route.getJSONObject(0);
//                            Log.d("MyTag", "legsObj"+String.valueOf(route));
                            JSONObject duration = details.getJSONObject("duration");
//                            Log.d("MyTag", "duration"+String.valueOf(duration));
                            seconds = duration.getInt("value");
//                            Log.d("MyTag", String.valueOf(seconds));
                        }catch (JSONException e) {
                            Log.d("Error", String.valueOf(e.getMessage()));
                        }
                        int finalSeconds = seconds;
                        JSONObject finalRoutes = routes;
                        List<direction> directions = null;
                        if(finalRoutes != null){
                            directions = getDirections(routes);
                        }
                        List<direction> finalDirections = directions;
                        runningActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                runningActivity.drawPath(finalRoutes, animateCamera);
                                runningActivity.setRemainingTime(finalSeconds);
                                if (!MainActivity.isDirectionListSet && finalDirections != null){
                                    runningActivity.setDirectionsList((ArrayList<direction>) finalDirections);
                                    MainActivity.isDirectionListSet = true;
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e("Buffer Error", "Error converting result " + e.toString());
                    }
//                    Log.i("Error", response.toString())
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("Error", e.toString());
                }

            });

        }
    }

}