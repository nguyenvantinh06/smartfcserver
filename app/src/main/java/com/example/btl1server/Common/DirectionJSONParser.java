package com.example.btl1server.Common;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionJSONParser {
    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray routesJSONArray;
        JSONArray legsJSONArray;
        JSONArray stepsJSONArray;
        JSONObject distanceJSONObject;
        JSONObject durationJSONObject;

        try {
            routesJSONArray = jObject.getJSONArray("routes");

            for (int i = 0; i < routesJSONArray.length(); i++) {
                List<HashMap<String, String>> path = new ArrayList<>();

                legsJSONArray = ((JSONObject) routesJSONArray.get(i)).getJSONArray("legs");
                for (int j = 0; j < legsJSONArray.length(); j++) {
                    distanceJSONObject = ((JSONObject) legsJSONArray.get(j)).getJSONObject("distance");
                    HashMap<String, String> distanceHashMap = new HashMap<>();
                    distanceHashMap.put("distance", distanceJSONObject.getString("text"));
                    path.add(distanceHashMap);

                    durationJSONObject = ((JSONObject) legsJSONArray.get(j)).getJSONObject("duration");
                    HashMap<String, String> durationHashMap = new HashMap<>();
                    durationHashMap.put("duration", durationJSONObject.getString("text"));
                    path.add(durationHashMap);

                    stepsJSONArray = ((JSONObject) legsJSONArray.get(j)).getJSONArray("steps");
                    for (int k = 0; k < stepsJSONArray.length(); k++) {
                        String tempPolyline;
                        tempPolyline = (String) ((JSONObject) ((JSONObject) stepsJSONArray.get(k)).get("polyline")).get("points");
                        List<LatLng> decodedList = decodePolyline(tempPolyline);

                        for (int l = 0; l < decodedList.size(); l++) {
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("lat", Double.toString(decodedList.get(l).latitude));
                            hashMap.put("lng", Double.toString(decodedList.get(l).longitude));
                            path.add(hashMap);
                        }
                    }
                }
                routes.add(path);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception ignored) {
        }
        Log.d("DirectionsJSONParser", "routes have been specified successfully");
        return routes;
    }

    // ---------------------------------------------------------------------------------------------
    // Method that decodes the encoded polyline that is returned from Google Directions API. -------
    private List<LatLng> decodePolyline(String encodedPolyline) {
        List<LatLng> decodedPolyline = new ArrayList<>();
        int encodedPolylineLength = encodedPolyline.length();
        int tempLat = 0, tempLng = 0;
        LatLng tempLatLng;
        int index = 0;
        int shift;
        int result;

        while (index < encodedPolylineLength) {
            int tempInt;

            shift = 0;
            result = 0;
            do {
                tempInt = encodedPolyline.charAt(index++) - 63;
                result |= (tempInt & 0x1F) << shift;
                shift += 5;
            } while (tempInt >= 0x20);
            int dLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            tempLat += dLat;

            shift = 0;
            result = 0;
            do {
                tempInt = encodedPolyline.charAt(index++) - 63;
                result |= (tempInt & 0x1F) << shift;
                shift += 5;
            } while (tempInt >= 0x20);
            int dLng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            tempLng += dLng;

            tempLatLng = new LatLng((((double) tempLat / 1E5)), (((double) tempLng / 1E5)));
            decodedPolyline.add(tempLatLng);
        }
        return decodedPolyline;
    }
}
