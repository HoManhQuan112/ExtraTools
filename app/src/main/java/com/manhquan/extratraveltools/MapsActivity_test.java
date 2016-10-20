package com.manhquan.extratraveltools;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.manhquan.extratraveltools.Modules.DirectionFinder;
import com.manhquan.extratraveltools.Modules.DirectionFinderListener;
import com.manhquan.extratraveltools.Modules.Route;
import com.manhquan.extratraveltools.RequestPermission.RequestPermission_Storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity_test extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private static final int REQUEST_ACCESS_FINE_LOCATION = 0;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 0;
    private static final String TAG = "MapsActivity_test";
    ListView lvLocation;
    String[] temp;
    private GoogleMap mMap;
    private Button btnFindPath;
    private Button btnEtOrigin;
    private Button btnEtDestination;
    private ImageButton loadLocation;
    private AutoCompleteTextView tvOrigin;
    private AutoCompleteTextView tvDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private LatLng currentLocation;
    private PlaceAutocompleteFragment autocompleteFragmentOrigin;
    private PlaceAutocompleteFragment autocompleteFragmentDestination;
    private String pOrigin;
    private String pDestination;
    private String currentAddress;
    private RequestPermission_Storage requestPermission_storage;
    private int data_block = 500;
    private String location_origin = "location_origin.txt";
    private String location_destination = "location_destination.txt";
    private String[] splitLocation;
    private String[] splitLocationReplace;
    private LinearLayout showLocation;
    private LinearLayout llShowConfig;
    private Button btnCloseLoadLocation;
    private Button btnSetOrigin;
    private Button btnSetDestination;
    private Button btnRemove;
    private int lvPosition;
    private TextView tvShowLocation;
    private LinearLayout lnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_test);
        requestPermission_storage.verifyStoragePermissions(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        autocompleteFragmentDestination = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment_destination);
        autocompleteFragmentOrigin = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment_origin);

        mapFragment.getMapAsync(this);

        setFindViewById();
        setOnClickListener();

        autocompleteFragmentOrigin.setHint(getResources().getString(R.string.enter_origin));
        autocompleteFragmentDestination.setHint(getResources().getString(R.string.enter_destination));

        autocompleteFragmentOrigin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                btnEtOrigin.setVisibility(View.GONE);
                pOrigin = place.getAddress().toString();
                tvOrigin.setText(pOrigin);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }

        });
        autocompleteFragmentDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                btnEtDestination.setVisibility(View.GONE);
                pDestination = place.getAddress().toString();
                tvDestination.setText(pDestination);
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    //region Back key
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Save location to memory?")
                .setMessage("Are you sure you want to save location to memory?")
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //region save file
                        File file = getFileStreamPath("location.txt");
                        if (!file.exists()) {
                            try {
                                FileOutputStream fou = openFileOutput("location.txt", MODE_WORLD_READABLE | MODE_APPEND);
                                OutputStreamWriter osw = new OutputStreamWriter(fou);
                                if (tvOrigin.getText().toString() != "") {
                                    try {
                                        osw.write(tvOrigin.getText().toString() + "\n");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (tvDestination.getText().toString() != "") {
                                    try {
                                        osw.write(tvDestination.getText().toString() + "\n");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                osw.flush();
                                osw.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                FileOutputStream fou = openFileOutput("location.txt", MODE_WORLD_READABLE | MODE_APPEND);
//                                BufferedWriter buffw = new BufferedWriter(fou);
                                OutputStreamWriter osw = new OutputStreamWriter(fou);
                                if (tvOrigin.getText().toString() != "") {
                                    try {
                                        osw.append(tvOrigin.getText().toString() + "\n");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (tvDestination.getText().toString() != "") {
                                    try {
                                        osw.append(tvDestination.getText().toString() + "\n");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                osw.flush();
                                osw.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }


                        //endregion
                    }
                })
                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.onBackPressed();
                    }
                })
                .setCancelable(true)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

//    public String loadJSONFromAsset() {
//        File file = getFileStreamPath("location.txt");
//        String json = null;
//        if (!file.exists()) {
//            Toast.makeText(MapsActivity_test.this, "Not found saved location file, save location to load", Toast.LENGTH_SHORT).show();
//        } else {
//            try {
//                InputStream is = getApplication().openFileInput("location.txt");
//                int size = is.available();
//                byte[] buffer = new byte[size];
//                is.read(buffer);
//                is.close();
//                json = new String(buffer, "utf-8");
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//        return json;
//    }

    private void setOnClickListener() {
        loadLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocation.setVisibility(View.VISIBLE);
                lnCancel.setVisibility(View.VISIBLE);
                //region load file location
                File file = getFileStreamPath("location.txt");
                if (!file.exists()) {
                    Toast.makeText(MapsActivity_test.this, "Not found saved location file, save location to load", Toast.LENGTH_SHORT).show();
                    lnCancel.setVisibility(View.GONE);
                } else {
                    try {
                        FileInputStream fis = openFileInput("location.txt");
                        InputStreamReader isr = new InputStreamReader(fis);
                        char[] data = new char[data_block];
                        String final_data = "";
                        int size;
                        try {
                            while ((size = isr.read(data)) > 0) {
                                String read_data = String.copyValueOf(data, 0, size);
                                final_data += read_data;
                                data = new char[data_block];
                                splitLocation = final_data.split("\n\n");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (splitLocation == null) {
                        Toast.makeText(MapsActivity_test.this,
                                "Not found saved location file, save location to load",
                                Toast.LENGTH_SHORT).show();
                        showLocation.setVisibility(View.GONE);
                    } else {
                        for (int i = 0; i < splitLocation.length; i++) {
                            splitLocation[i] = splitLocation[i].replace("\n", " ");
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity_test.this,
                                android.R.layout.simple_list_item_1, splitLocation);
                        lvLocation.setAdapter(adapter);
                        lvLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                tvShowLocation.setText(splitLocation[position]);
                                showLocation.setVisibility(View.GONE);
                                llShowConfig.setVisibility(View.VISIBLE);
                                lvPosition = position;

                            }
                        });
                    }
                }

                //endregion
            }
        });
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });
        btnEtOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationAddress(currentLocation.latitude, currentLocation.longitude);
                tvOrigin.setText(currentAddress);
                autocompleteFragmentOrigin.setText(tvOrigin.getText().toString());
                btnEtOrigin.setVisibility(View.GONE);
                btnEtDestination.setVisibility(View.GONE);
            }
        });
        btnEtDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationAddress(currentLocation.latitude, currentLocation.longitude);
                tvDestination.setText(currentAddress);
                autocompleteFragmentDestination.setText(tvDestination.getText().toString());
                btnEtOrigin.setVisibility(View.GONE);
                btnEtDestination.setVisibility(View.GONE);
            }
        });
        btnCloseLoadLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lnCancel.setVisibility(View.GONE);
                showLocation.setVisibility(View.GONE);
            }
        });
        btnSetOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocation.setVisibility(View.GONE);
                lnCancel.setVisibility(View.GONE);
                tvOrigin.setText(tvShowLocation.getText().toString());
                autocompleteFragmentOrigin.setText(tvShowLocation.getText().toString());
                llShowConfig.setVisibility(View.GONE);
            }
        });
        btnSetDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocation.setVisibility(View.GONE);
                lnCancel.setVisibility(View.GONE);
                tvDestination.setText(tvShowLocation.getText().toString());
                autocompleteFragmentDestination.setText(tvShowLocation.getText().toString());
                llShowConfig.setVisibility(View.GONE);
            }
        });
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocation.setVisibility(View.GONE);
                lnCancel.setVisibility(View.GONE);
                tvShowLocation.setText(splitLocation[lvPosition]);
                temp = new String[splitLocation.length - 1];
                int j = 0;
                for (int i = 0; i < splitLocation.length - 1; i++) {
                    if (i < lvPosition) {
                        temp[i] = splitLocation[i];
                    } else if (i == lvPosition) {
                        temp[i] = splitLocation[i + 1];
                    } else {
                        temp[i] = splitLocation[i + 1];
                    }
                }
                splitLocation = temp;

                try {
                    FileOutputStream fou = openFileOutput("location.txt", MODE_WORLD_READABLE);
                    OutputStreamWriter osw = new OutputStreamWriter(fou);
                    for (int i = 0; i < splitLocation.length; i++) {
                        osw.write(splitLocation[i] + "\n\n");
                    }
                    osw.flush();
                    osw.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MapsActivity_test.this,
                        android.R.layout.simple_list_item_1, splitLocation);
                lvLocation.setAdapter(adapter);
                lvLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        tvShowLocation.setText(splitLocation[position]);
                        showLocation.setVisibility(View.GONE);
                        llShowConfig.setVisibility(View.VISIBLE);
                        lvPosition = position;

                    }
                });
                llShowConfig.setVisibility(View.GONE);
                showLocation.setVisibility(View.VISIBLE);

            }
        });
        lnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocation.setVisibility(View.GONE);
                llShowConfig.setVisibility(View.GONE);
                lnCancel.setVisibility(View.GONE);
            }
        });
    }

    private void setFindViewById() {
        btnFindPath = (Button) findViewById(R.id.btnFindPath);
        btnEtOrigin = (Button) findViewById(R.id.btnEtOrigin);
        btnEtDestination = (Button) findViewById(R.id.btnEtDestination);
        tvOrigin = (AutoCompleteTextView) findViewById(R.id.tvOrigin);
        tvDestination = (AutoCompleteTextView) findViewById(R.id.etDestination);
        loadLocation = (ImageButton) findViewById(R.id.loadLocation);
        showLocation = (LinearLayout) findViewById(R.id.showLocation);
        llShowConfig = (LinearLayout) findViewById(R.id.llShowConfig);
        lvLocation = (ListView) findViewById(R.id.lvLocation);
        btnCloseLoadLocation = (Button) findViewById(R.id.btnCloseLoadLocation);
        btnSetOrigin = (Button) findViewById(R.id.btnSetOrigin);
        btnSetDestination = (Button) findViewById(R.id.btnSetDestination);
        btnRemove = (Button) findViewById(R.id.btnRemove);
        tvShowLocation = (TextView) findViewById(R.id.tvShowLocation);
        lnCancel = (LinearLayout) findViewById(R.id.lnCancel);
    }

    private void getLocationAddress(Double Lat, Double Lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {

            //Place latitude and longitude
            List<Address> addresses = geocoder.getFromLocation(Lat, Lng, 1);

            if (addresses != null) {
                android.location.Address fetchedAddress = addresses.get(0);
                StringBuilder strAddress = new StringBuilder();
                for (int i = 0; i < fetchedAddress.getMaxAddressLineIndex(); i++) {
                    strAddress.append(fetchedAddress.getAddressLine(i)).append("\n");
                }
                currentAddress = strAddress.toString();
            } else {
                Toast.makeText(this, "No location found...!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Could not get address..!", Toast.LENGTH_LONG).show();
        }
    }

    private void sendRequest() {
        String origin = tvOrigin.getText().toString();
        String destination = tvDestination.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //region Permission for setMyLocationEnabled
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //region manifest for Android 6
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);

            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
            }
            return;
            //endregion
        }
        //endregion
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.clear();
                StringBuilder strAddress = new StringBuilder();
                Geocoder geocoder = new Geocoder(MapsActivity_test.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses != null) {
                        android.location.Address fetchedAddress = addresses.get(0);
                        for (int i = 0; i < fetchedAddress.getMaxAddressLineIndex(); i++) {
                            strAddress.append(fetchedAddress.getAddressLine(i)).append("\n");
                        }
                        Toast.makeText(MapsActivity_test.this, strAddress.toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MapsActivity_test.this, "No location found...!", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Could not get address..!", Toast.LENGTH_LONG).show();
                }
                btnEtOrigin.setVisibility(View.VISIBLE);
                btnEtDestination.setVisibility(View.VISIBLE);

                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        .title(strAddress.toString())
                        .position(latLng)));
                currentLocation = latLng;
            }
        });
    }

    //region Request for access current location
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(getIntent());
                finish();
            }
        }
        if (requestCode == REQUEST_ACCESS_COARSE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(getIntent());
                finish();
            }
        }
    }
//endregion

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.maker_location_a))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.maker_location_b))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

}
