package com.example.btl1server;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.btl1server.Common.Common;
import com.example.btl1server.Remote.IGeoCoordinates;
import com.example.btl1server.Remote.LocationHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;

    private final static int PLAY_SERVICE_RESOLUTION_REQUEST = 1000;
    private final static int LOCATION_PERMISSION_REQUEST = 1000;

    private Location mLocation;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static int UPDATE_INTERVAL = 1000;
    private static int FASTEST_INTERVAL = 1000;
    private static int DISPLAYMENT = 1000;

    private IGeoCoordinates mService;

    LatLng yourLocation, orderLocation;

    private Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);

        mService = Common.getGeoCodeservice();
        yourLocation = null;
        orderLocation = null;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestRuntimePermission();
        }
        else {
            if(checkPlayService()) {
                buildGoogleAptClient();
                createLocationRequest();
            }
        }

        displayLocation();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestRuntimePermission();
        }
        else {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLocation != null) {
                double latitude = mLocation.getLatitude();
                double longitude = mLocation.getLongitude();
                yourLocation = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(yourLocation)).setTitle("Vị trí của bạn");
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

                drawRoute(yourLocation, Common.currentRequest.getAddress());

            }
            else if (mLocation == null){
                //Toast.makeText(this, "Không tìm được vị trí của bạn", Toast.LENGTH_SHORT).show();
                Log.d("DEBUG", "Không tìm được vị trí của bạn");
            }
        }
    }

    private void drawRoute(LatLng yourLocation, String address) {
        LocationHelper findLocation = new LocationHelper(this);
        String strLocation;
        strLocation = findLocation.getLocationFromAddress(address);
        String latLng[] = strLocation.split(",");
        orderLocation = new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.box);
        bitmap = Common.scaleBitmap(bitmap, 70, 70);

        MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .title("Đơn hàng của " + Common.currentRequest.getPhone())
                .position(orderLocation);
        mMap.addMarker(markerOptions);


    }

//    private void drawRoute(LatLng yourLocation, String address) {
//        mService.getGeoCode(address).enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(Call<String> call, @NonNull Response<String> response) {
//                try {
//
//                    JSONObject jsonObject = null;
//
//                    jsonObject = new JSONObject(response.body().toString());
//
//
//                    String lat = ((JSONArray)jsonObject.get("results"))
//                            .getJSONObject(0)
//                            .getJSONObject("geometry")
//                            .getJSONObject("location")
//                            .get("lat").toString();
//
//                    String lng = ((JSONArray)jsonObject.get("results"))
//                            .getJSONObject(0)
//                            .getJSONObject("geometry")
//                            .getJSONObject("location")
//                            .get("lng").toString();
//
//                    LatLng orderLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
//
//                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.box);
//                    bitmap = Common.scaleBitmap(bitmap, 70, 70);
//
//                    MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
//                            .title("Đơn hàng của " + Common.currentRequest.getPhone())
//                            .position(orderLocation);
//                    mMap.addMarker(markerOptions);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t) {
//
//            }
//        });
//    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLAYMENT);
    }

    protected synchronized void buildGoogleAptClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RESOLUTION_REQUEST).show();
            }
            else {
                Toast.makeText(this, "Thiết bị không được cung cấp dịch vụ này", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    //
    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, LOCATION_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayService()) {
                        buildGoogleAptClient();
                        createLocationRequest();

                        displayLocation();
                    }
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        displayLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

//    private class ParserTask extends AsyncTask<String, Integer,List<List<HashMap<String, String>>>> {
//
//        ProgressDialog dialog = new ProgressDialog(TrackingOrder.this);
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            dialog.setMessage("Vui lòng chờ ...");
//            dialog.show();
//        }
//
//
//        @Override
//        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
//            JSONObject jObject;
//            List<List<HashMap<String, String>>> routes = null;
//            try {
//                jObject = new JSONObject(strings[0]);
//                DirectionJSONParser parser = new DirectionJSONParser();
//
//                routes = parser.parse(jObject);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return routes;
//        }
//
//        @Override
//        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
//            dialog.dismiss();
//
//            ArrayList points = null;
//            PolylineOptions polylineOptions = null;
//
//            for (int i = 0; i < lists.size(); i++) {
//                points = new ArrayList();
//                polylineOptions = new PolylineOptions();
//
//                List<HashMap<String, String>> path = lists.get(i);
//                for (int j = 0;j < path.size(); j++) {
//                    HashMap<String, String> point = path.get(j);
//                    Double lat = Double.parseDouble(point.get("lat"));
//                    Double lng = Double.parseDouble(point.get("lng"));
//                }
//
//                polylineOptions.addAll(points);
//                polylineOptions.width(12);
//                polylineOptions.color(Color.BLUE);
//                polylineOptions.geodesic(true);
//            }
//            mMap.addPolyline(polylineOptions);
//        }
//    }
}
