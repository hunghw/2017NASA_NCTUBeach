package nctu.nasa.gotobeach;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LocationSelectActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, View.OnClickListener {

    private GoogleMap mMap;
    private Marker last;
    private Button button;
    private LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int PLACE_PICKER_REQUEST = 1;
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

        //setContentView(R.layout.activity_location_select);
        //// Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        //        .findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
        //button = (Button) findViewById(R.id.select_location);
        //button.setOnClickListener(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (last != null) last.remove();
        last = mMap.addMarker(new MarkerOptions().position(latLng).title(latLng.latitude + " " + latLng.longitude));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        this.latLng = latLng;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                Intent intent = new Intent(this, InfoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("name", place.getName());
                intent.putExtra("lat", place.getLatLng().latitude);
                intent.putExtra("lng", place.getLatLng().longitude);
                startActivity(intent);
                finish();
            } else finish();
        }

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, InfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("lat", latLng.latitude);
        intent.putExtra("lng", latLng.longitude);

        Log.e("D", "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latLng.latitude+","+latLng.longitude+"&radius=500&type=beach&key=AIzaSyAQdY5bU2Hxd0at2OfvMXRbWtdUpYQZzhw");
        Http.get("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latLng.latitude+","+latLng.longitude+"&radius=500&type=beach&key=AIzaSyAQdY5bU2Hxd0at2OfvMXRbWtdUpYQZzhw", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray results = jsonObject.getJSONArray("results");
                    final String name = results.getJSONObject(0).getString("name");
                    Log.e("D", name);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LocationSelectActivity.this, name, Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        startActivity(intent);
    }
}
