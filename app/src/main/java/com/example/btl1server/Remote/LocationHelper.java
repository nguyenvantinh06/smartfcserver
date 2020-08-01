package com.example.btl1server.Remote;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.util.List;

public class LocationHelper {
    private Context mContext;

    public LocationHelper(Context context) {
        this.mContext = context;
    }

    public String getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(mContext);
        List<Address> address;

        try {
            address = coder.getFromLocationName(strAddress, 1);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            return lat + "," + lng;
        } catch (Exception e) {
            return null;
        }
    }

}
