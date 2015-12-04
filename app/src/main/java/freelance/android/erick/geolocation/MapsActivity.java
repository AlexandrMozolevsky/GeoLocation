package freelance.android.erick.geolocation;

import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import freelance.android.erick.geolocation.Model.Coordinates;
import freelance.android.erick.geolocation.Model.Distance;
import freelance.android.erick.geolocation.Model.Duration;
import freelance.android.erick.geolocation.Model.End_location;
import freelance.android.erick.geolocation.Model.MyMarker;
import freelance.android.erick.geolocation.Model.Polyline;
import freelance.android.erick.geolocation.Model.Start_location;
import freelance.android.erick.geolocation.Model.Step;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();

    private HashMap<Marker, MyMarker> mMarkersHashMap;
    private ArrayList<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();

    private FloatingActionButton fab;

    private CardView supportCardView;

    private EditText editTextMapsActivityTaxiWindowUserPhone;

    private TextView mapsActivityTextViewTaxiName;
    private TextView mapsActivityTextViewCostPerKm;
    private TextView mapsActivityTextViewDriverName;
    private TextView textViewMapsActivityTaxiWindowUserPosition;

    private Button buttonMapsActivityTaxiWindowRefreshPosition;
    private Button buttonMapsActivityTaxiWindowGetTaxi;

    private Marker myPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        initFab();
        initCardView();
        initTextViews();
        initButtons();
        initEditTexts();
    }

    private void initTextViews() {
        mapsActivityTextViewTaxiName = (TextView) findViewById(R.id.mapsActivityTextViewTaxiName);
        mapsActivityTextViewCostPerKm = (TextView) findViewById(R.id.mapsActivityTextViewCostPerKm);
        mapsActivityTextViewDriverName = (TextView) findViewById(R.id.mapsActivityTextViewDriverName);
        textViewMapsActivityTaxiWindowUserPosition = (TextView) findViewById(R.id.textViewMapsActivityTaxiWindowUserPosition);
    }

    private void initButtons() {
        buttonMapsActivityTaxiWindowRefreshPosition = (Button) findViewById(R.id.buttonMapsActivityTaxiWindowRefreshPosition);
        buttonMapsActivityTaxiWindowGetTaxi = (Button) findViewById(R.id.buttonMapsActivityTaxiWindowGetTaxi);

        buttonMapsActivityTaxiWindowRefreshPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewMapsActivityTaxiWindowUserPosition.setText("Waiting for Location");
                updateLocality();
            }
        });

        buttonMapsActivityTaxiWindowGetTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "GET TAXI", Toast.LENGTH_SHORT).show();
//                showShortestTaxi();
            }
        });
    }

    private void initEditTexts() {
        editTextMapsActivityTaxiWindowUserPhone = (EditText) findViewById(R.id.editTextMapsActivityTaxiWindowUserPhone);
    }

    private void initCardView() {
        supportCardView = (CardView) findViewById(R.id.supportCardView);
    }

    private void initFab() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mMap) {
                    handleNewLocation();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        if (null != mMap) {
            mMap.clear();
            createMarkers();
            plotMarkers(mMyMarkersArray);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    Marker selected;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (supportCardView.getVisibility() == View.GONE)
                    supportCardView.setTranslationY(supportCardView.getHeight());
                supportCardView.setVisibility(View.VISIBLE);
                if (null != marker) {
                    if (myPosition != null && marker.getId().equals(myPosition.getId())) {
                        return true;
                    }
                    if (null == selected) {
                        selected = marker;
                    }

                    MyMarker myMarker = mMarkersHashMap.get(marker);
                    mapsActivityTextViewTaxiName.setText(myMarker.getTaxiName());
                    mapsActivityTextViewCostPerKm.setText(myMarker.getCost());
                    mapsActivityTextViewDriverName.setText(myMarker.getVoditelName());

                    if (supportCardView.getTranslationY() != 0) {
                        supportCardView.animate().translationY(0).setDuration(200).start();
                    } else {
                        if (!marker.getId().equals(selected.getId())) {
                            selected = marker;
                        } else {
                            supportCardView.animate().translationY(supportCardView.getHeight()).setDuration(200).start();
                        }
                    }

                    updateLocality();
                }
                //marker.showInfoWindow();
                return true;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                if (supportCardView.getTranslationY() == 0)
                    supportCardView.animate().translationY(supportCardView.getHeight()).setDuration(200).start();
                else
                    new AlertDialog.Builder(MapsActivity.this).
                            setTitle("FROM WHERE")
                            .setNegativeButton("FROM HERE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    first = latLng;
                                }
                            })
                            .setPositiveButton("TO HERE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    second = latLng;
                                    asyncSearchUsers(first, second, new CallbackCoordinates() {
                                        @Override
                                        public void onSuccess(Coordinates model) {
                                            LatLng[] latLngs = new LatLng[model.getSteps().length];
                                            for (int i = 0; i < model.getSteps().length; i++) {
                                                latLngs[i] = new LatLng(Double.parseDouble(model.getSteps()[i].getStart_location().getLat()), Double.parseDouble(model.getSteps()[i].getStart_location().getLng()));
                                            }
                                            mMap.clear();
                                            mMap.addPolyline((new PolylineOptions())
                                                    .add(latLngs).width(5).color(Color.BLUE)
                                                    .geodesic(false));
                                        }

                                        @Override
                                        public void onError(String error) {

                                        }
                                    });
                                }
                            }).show();
            }
        });

        createMarkers();
        plotMarkers(mMyMarkersArray);
    }

    LatLng first;
    LatLng second;

    private void updateLocality() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Geocoder geo = new Geocoder(MapsActivity.this.getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = geo.getFromLocation(myPosition.getPosition().latitude, myPosition.getPosition().longitude, 1);
                    if (addresses.isEmpty()) {
                        textViewMapsActivityTaxiWindowUserPosition.setText("Waiting for Location");
                    } else {
                        if (addresses.size() > 0) {
                            textViewMapsActivityTaxiWindowUserPosition.setText(getResources().getString(R.string.additionalMapsActivityUserAreYouHere) + addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName() + "?");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // getFromLocation() may sometimes fail
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "Location services connected.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void handleNewLocation() {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        if (null == myPosition) {
            myPosition = mMap.addMarker(new MarkerOptions().position(latLng));
        } else {
            if (currentLatitude != myPosition.getPosition().latitude && currentLongitude != myPosition.getPosition().longitude) {
                myPosition.remove();
                myPosition = mMap.addMarker(new MarkerOptions().position(latLng));
            }

            Toast.makeText(this, location.toString(), Toast.LENGTH_SHORT).show();
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myPosition.getPosition().latitude, myPosition.getPosition().longitude), 14.0f));
    }

    private void createMarkers() {
        mMarkersHashMap = new HashMap<Marker, MyMarker>();
        mMyMarkersArray.add(new MyMarker("ул. Островского", R.drawable.taxi_green, Double.parseDouble("53.690191"), Double.parseDouble("23.826134"), "9 т/км", "Такси сити", "Анатолий Григорьевич"));
        mMyMarkersArray.add(new MyMarker("ул. Ленина 32", R.drawable.taxi_green, Double.parseDouble("53.684602"), Double.parseDouble("23.840616"), "6 т/км", "ТаксиПати", "Коленька Петрович"));
        mMyMarkersArray.add(new MyMarker("ул. Славинского", R.drawable.taxi_green, Double.parseDouble("53.647673"), Double.parseDouble("23.834608"), "11 т/км", "157", "Василий Апанасьевич"));
        mMyMarkersArray.add(new MyMarker("ул. Советских пограничников", R.drawable.taxi_red, Double.parseDouble("53.673096"), Double.parseDouble("23.794372"), "7 т/км", "88005553535", "Анатолий Вассерман"));
    }

    private void plotMarkers(final ArrayList<MyMarker> markers) {
        if (markers.size() > 0) {
            for (final MyMarker myMarker : markers) {
                MarkerOptions markerOption = new MarkerOptions().position(new LatLng(myMarker.getmLatitude(), myMarker.getmLongitude()));
                Marker currentMarker = mMap.addMarker(markerOption);
                currentMarker.setTitle(myMarker.getmLabel());
                currentMarker.setIcon(BitmapDescriptorFactory.fromResource(myMarker.getmIcon()));
                mMarkersHashMap.put(currentMarker, myMarker);
                //mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(mMarkersHashMap, getLayoutInflater()));
//                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//                    @Override
//                    public void onInfoWindowClick(Marker marker) {
//                        Log.e("click", marker.getId());
//                    }
//                });
            }
        }
    }

    /*получает дистанции напрямую*/
    private void showShortestTaxi() {
        HashMap<Float, MyMarker> distance = new HashMap<>();

        for (int i = 0; i < mMyMarkersArray.size(); i++) {
            float[] distanceTemp = new float[4];
            Location.distanceBetween(myPosition.getPosition().latitude, myPosition.getPosition().longitude, mMyMarkersArray.get(i).getmLatitude(), mMyMarkersArray.get(i).getmLongitude(), distanceTemp);
            distance.put(distanceTemp[0], mMyMarkersArray.get(i));
        }
    }

    Timer updateTimer;

    private void updateMarkersPositions() {
        updateTimer.scheduleAtFixedRate(new timer(), 0, 10000);
    }

    private class timer extends TimerTask {
        @Override
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    /*TODO ДОДЕЛАТЬ ОБНОВУ */
                    HashMap<Marker, MyMarker> newHash = new HashMap<Marker, MyMarker>();

                    for(Map.Entry<Marker, MyMarker> entry : mMarkersHashMap.entrySet()) {
                        entry.getKey().remove();

                    }
                }
            }).start();
        }
    }

    public static abstract class CallbackCoordinates {
        public abstract void onSuccess(Coordinates model);

        public abstract void onError(String error);
    }

    public static void asyncSearchUsers(final LatLng from, final LatLng to, final CallbackCoordinates callback) {
        new AsyncTask<Void, Void, Coordinates>() {
            @Override
            protected Coordinates doInBackground(Void... params) {
                Uri uri = Uri.parse("https://maps.googleapis.com/maps/api/directions/json?origin=" + from.latitude + "," + from.longitude + "&destination=" + to.latitude + "," + to.longitude + "&sensor=false&units=metric&mode=driving&language=RU");//&key=AIzaSyAcG0NTb9hBBRitUmYEHDx0Ie3vpMZQ5rw");

                String result = "";
                String responseLine = "";

                Coordinates model = new Coordinates();

//                Uri.Builder builder = new Uri.Builder();
//                builder.scheme("https")
//                        .authority("api.vk.com")
//                        .appendPath("method")
//                        .appendPath("users.search")
//                        .appendQueryParameter("q", params[0])
//                        .appendQueryParameter("fields", "photo_100,last_seen,online")
//                        .appendQueryParameter("count", "50")
//                        .appendQueryParameter("access_token", Constants.VK_TOKEN);
//                String myUrl = builder.build().toString();

                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(uri.toString()).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Length", "0");
                    connection.setConnectTimeout(10000);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((responseLine = bufferedReader.readLine()) != null) {
                        result += responseLine;
                    }
                    bufferedReader.close();

                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray routes = jsonObject.getJSONArray("routes");
                    JSONObject routesObj = routes.getJSONObject(0);
                    JSONArray legs = routesObj.getJSONArray("legs");

                    for (int i = 0; i < legs.length(); i++) {
                        JSONObject currLot = legs.getJSONObject(i);
                        if (currLot.has("duration")) {
                            Duration duration = new Duration();
                            JSONObject data = new JSONObject(currLot.getString("duration"));
                            if (data.has("value"))
                                duration.setValue(data.getString("value"));
                            if (data.has("text"))
                                duration.setText(data.getString("text"));
                            model.setDuration(duration);
                        }

                        if (currLot.has("distance")) {
                            Distance distance = new Distance();
                            JSONObject data = new JSONObject(currLot.getString("distance"));
                            if (data.has("value"))
                                distance.setValue(data.getString("value"));
                            if (data.has("text"))
                                distance.setText(data.getString("text"));
                            model.setDistance(distance);
                        }

                        if (currLot.has("end_address")) {
                            model.setEnd_address(currLot.getString("end_address"));
                        }

                        if (currLot.has("end_location")) {
                            End_location end_location = new End_location();
                            JSONObject data = new JSONObject(currLot.getString("distance"));
                            if (data.has("lat"))
                                end_location.setLat(data.getString("lat"));
                            if (data.has("lng"))
                                end_location.setLng(data.getString("lng"));
                            model.setEnd_location(end_location);
                        }

                        if (currLot.has("start_address")) {
                            model.setStart_address(currLot.getString("start_address"));
                        }

                        if (currLot.has("start_location")) {
                            Start_location start_location = new Start_location();
                            JSONObject data = new JSONObject(currLot.getString("start_location"));
                            if (data.has("lat"))
                                start_location.setLat(data.getString("lat"));
                            if (data.has("lng"))
                                start_location.setLng(data.getString("lng"));
                            model.setStart_location(start_location);
                        }

                        if (currLot.has("steps")) {
                            JSONArray stepsObj = new JSONArray(currLot.getString("steps"));
                            Step[] steps = new Step[stepsObj.length()];
                            for (int j = 0; j < stepsObj.length(); j++) {
                                Step stepTemp = new Step();
                                JSONObject currLotStep = new JSONObject(stepsObj.get(j).toString());

                                if (currLotStep.has("duration")) {
                                    Duration duration = new Duration();
                                    JSONObject data = new JSONObject(currLotStep.getString("duration"));
                                    if (data.has("value"))
                                        duration.setValue(data.getString("value"));
                                    if (data.has("text"))
                                        duration.setText(data.getString("text"));
                                    stepTemp.setDuration(duration);
                                }

                                if (currLotStep.has("distance")) {
                                    Distance distance = new Distance();
                                    JSONObject data = new JSONObject(currLotStep.getString("distance"));
                                    if (data.has("value"))
                                        distance.setValue(data.getString("value"));
                                    if (data.has("text"))
                                        distance.setText(data.getString("text"));
                                    stepTemp.setDistance(distance);
                                }

                                if (currLotStep.has("end_location")) {
                                    End_location end_location = new End_location();
                                    JSONObject data = new JSONObject(currLotStep.getString("end_location"));
                                    if (data.has("lat"))
                                        end_location.setLat(data.getString("lat"));
                                    if (data.has("lng"))
                                        end_location.setLng(data.getString("lng"));
                                    stepTemp.setEnd_location(end_location);
                                }

                                if (currLotStep.has("start_location")) {
                                    Start_location start_location = new Start_location();
                                    JSONObject data = new JSONObject(currLotStep.getString("start_location"));
                                    if (data.has("lat"))
                                        start_location.setLat(data.getString("lat"));
                                    if (data.has("lng"))
                                        start_location.setLng(data.getString("lng"));
                                    stepTemp.setStart_location(start_location);
                                }

                                if (currLotStep.has("html_instructions")) {
                                    stepTemp.setHtml_instructions(currLotStep.getString("html_instructions"));
                                }

                                if (currLotStep.has("travel_mode")) {
                                    stepTemp.setTravel_mode(currLotStep.getString("travel_mode"));
                                }

                                if (currLotStep.has("polyline")) {
                                    Polyline polyline = new Polyline();
                                    JSONObject data = new JSONObject(currLotStep.getString("polyline"));
                                    if (data.has("points"))
                                        polyline.setPoints(data.getString("points"));
                                    stepTemp.setPolyline(polyline);
                                }
                                steps[j] = stepTemp;
                            }
                            model.setSteps(steps);
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                return model;
            }

            @Override
            protected void onPostExecute(Coordinates result) {
                callback.onSuccess(result);
            }
        }.execute();
    }
}
