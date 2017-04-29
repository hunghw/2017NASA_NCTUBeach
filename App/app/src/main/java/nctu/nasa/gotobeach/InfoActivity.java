package nctu.nasa.gotobeach;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class InfoActivity extends AppCompatActivity {
    private enum Weather {
        rain,
        cloudly,
        clear,
        none,
    }
    private double lat = 24.86522521525702;
    private double lon = 120.9576031565666;
    private String name;
    private String uv_desc = "";
    private String phrase_32char = "";
    private String wx_phrase = "";
    private Weather weather = Weather.none;
    private Weather willweather = Weather.none;
    private int temp = 0;
    private int chlorophyll;
    private Boolean rain_alert = null;
    private TextView location_name;
    private TextView weather_header;
    private TextView weather_value;
    private TextView uv_value;
    private TextView sum_protection;
    private TextView temp_value;
    private TextView go_beach;
    private TextView chlorophyll_text;
    private TextView uv_info;
    private TextView about;

    private ImageView share;
    private ImageView weather_icon;
    private ImageView weather_next_icon;
    private ImageView willweather_icon;
    private TimerTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        lat = getIntent().getDoubleExtra("lat", 24.86522521525702);
        lon = getIntent().getDoubleExtra("lng", 120.9576031565666);
        name = getIntent().getStringExtra("name");

        location_name = (TextView) findViewById(R.id.location_name);
        location_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InfoActivity.this, LocationSelectActivity.class));
                finish();
            }
        });
        uv_value = (TextView) findViewById(R.id.uv_value);
        sum_protection = (TextView) findViewById(R.id.sum_protection);
        temp_value = (TextView) findViewById(R.id.temp_value);
        go_beach = (TextView) findViewById(R.id.go_beach);
        weather_header = (TextView) findViewById(R.id.weather_header);
        weather_value = (TextView) findViewById(R.id.weather_value);
        chlorophyll_text = (TextView) findViewById(R.id.chlorophyll_text);
        uv_info = (TextView) findViewById(R.id.uv_info);
        uv_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InfoActivity.this, FactActivity.class));
            }
        });
        about = (TextView) findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InfoActivity.this, AboutActivity.class));
            }
        });

        weather_icon = (ImageView) findViewById(R.id.weather_icon);
        weather_next_icon = (ImageView) findViewById(R.id.weather_next_icon);
        willweather_icon = (ImageView) findViewById(R.id.willweather_icon);
        share = (ImageView) findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeScreenshot();
            }
        });

        location_name.setText(name);

        task = new TimerTask() {
            @Override
            public void run() { fetchData.run(); }
        };
        new Timer().schedule(task, 0, 600000);
    }

    private Runnable fetchData = new Runnable() {
        @Override
        public void run() {
            Log.e("T", "timer");
            //Http.get("http://linux4.cs.nctu.edu.tw:9000/?lat="+lat+"&long="+lon, new Callback() {
            Http.get("http://52.168.28.84:9000/?lat="+lat+"&long="+lon, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    task.cancel();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            go_beach.setText(R.string.no_data);
                            sum_protection.setText(R.string.no_data);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        final JSONObject jsonObject = new JSONObject(response.body().string());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    uv_desc = jsonObject.getString("uv_desc");
                                    phrase_32char = jsonObject.getString("phrase_32char");
                                    temp = jsonObject.getInt("temp");
                                    wx_phrase = jsonObject.getString("wx_phrase");
                                    chlorophyll = jsonObject.getInt("Chlorophyll");
                                    updateData();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        task.cancel();
    }

    private void updateData() {
        boolean outside = true;
        uv_value.setText(uv_desc);
        temp_value.setText(temp + "");

        if ("Low".equals(uv_desc)) {
            go_beach.setText(R.string.low_moderate);
            go_beach.setTextColor(getResources().getColor(R.color.low));
            uv_value.setTextColor(getResources().getColor(R.color.low));
            sum_protection.setText(R.string.sun_protection_low);
        } else if ("Moderate".equals(uv_desc)){
            go_beach.setText(R.string.low_moderate);
            go_beach.setTextColor(getResources().getColor(R.color.low));
            uv_value.setTextColor(getResources().getColor(R.color.moderate));
            sum_protection.setText(R.string.sun_protection_moderate);
        } else if ("High".equals(uv_desc)) {
            go_beach.setText(R.string.high);
            go_beach.setTextColor(getResources().getColor(R.color.high));
            uv_value.setTextColor(getResources().getColor(R.color.high));
            sum_protection.setText(R.string.sun_protection_high);
        } else if ("Very High".equals(uv_desc)) {
            outside = false;
            go_beach.setText(R.string.veryhigh);
            go_beach.setTextColor(getResources().getColor(R.color.veryhigh));
            uv_value.setTextColor(getResources().getColor(R.color.veryhigh));
            sum_protection.setText(R.string.sun_protection_veryhigh);
        } else if ("Extreme".equals(uv_desc)) {
            outside = false;
            go_beach.setText(R.string.extreme);
            go_beach.setTextColor(getResources().getColor(R.color.veryhigh));
            uv_value.setTextColor(getResources().getColor(R.color.extreme));
            sum_protection.setText(R.string.sun_protection_extreme);
        }

        Pattern pattern;
        Matcher matcher;
        weather = Weather.none;
        willweather = Weather.none;

        pattern = Pattern.compile("rain", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(phrase_32char);
        if (matcher.find()) {
            willweather = Weather.rain;
        }

        pattern = Pattern.compile("rain", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(wx_phrase);
        if (matcher.find()) {
            weather = Weather.rain;
        }

        pattern = Pattern.compile("cloud", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(phrase_32char);
        if (matcher.find()) {
            willweather = Weather.cloudly;
        }

        pattern = Pattern.compile("cloud", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(wx_phrase);
        if (matcher.find()) {
            weather = Weather.cloudly;
        }

        pattern = Pattern.compile("clear", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(phrase_32char);
        if (matcher.find()) {
            willweather = Weather.clear;
        }

        pattern = Pattern.compile("clear", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(wx_phrase);
        if (matcher.find()) {
            weather = Weather.clear;
        }

        if (willweather == Weather.rain) {
            if (rain_alert == null)
                rain_alert = true;
            else if (!rain_alert) {
                alert();
                rain_alert = true;
            }
        } else {
            if (rain_alert == null)
                rain_alert = false;
        }

        if (chlorophyll > 0)
            chlorophyll_text.setVisibility(View.VISIBLE);
        else
            chlorophyll_text.setVisibility(View.GONE);

        if (outside && (weather == Weather.rain || willweather == Weather.rain || chlorophyll > 0)) {
                go_beach.setText(R.string.veryhigh);
        }

        switch (willweather) {
            case rain:
                weather_value.setText(R.string.willrain);
                weather_value.setVisibility(View.VISIBLE);
                weather_header.setVisibility(View.VISIBLE);

                willweather_icon.setImageDrawable(getDrawable(R.drawable.rain));
                willweather_icon.setVisibility(View.VISIBLE);
                weather_next_icon.setVisibility(View.VISIBLE);
                break;

            case cloudly:
                willweather_icon.setImageDrawable(getDrawable(R.drawable.cloud));
                willweather_icon.setVisibility(View.VISIBLE);
                weather_next_icon.setVisibility(View.VISIBLE);
                break;

            case clear:
                willweather_icon.setImageDrawable(getDrawable(R.drawable.sun));
                willweather_icon.setVisibility(View.VISIBLE);
                weather_next_icon.setVisibility(View.VISIBLE);
                break;

            default:
                willweather_icon.setVisibility(View.GONE);
                weather_next_icon.setVisibility(View.GONE);
        }

        if (willweather == weather) {
            willweather_icon.setVisibility(View.GONE);
            weather_next_icon.setVisibility(View.GONE);
        }

        switch (weather) {
            case rain:
                weather_icon.setImageDrawable(getDrawable(R.drawable.rain));
                weather_icon.setVisibility(View.VISIBLE);

                weather_value.setText(R.string.rain);
                weather_value.setVisibility(View.VISIBLE);
                weather_header.setVisibility(View.VISIBLE);
                break;

            case clear:
                weather_icon.setImageDrawable(getDrawable(R.drawable.sun));
                weather_icon.setVisibility(View.VISIBLE);

                weather_value.setVisibility(View.GONE);
                weather_header.setVisibility(View.GONE);
                break;
            case cloudly:
                weather_icon.setImageDrawable(getDrawable(R.drawable.cloud));
                weather_icon.setVisibility(View.VISIBLE);

                weather_value.setVisibility(View.GONE);
                weather_header.setVisibility(View.GONE);
                break;
            default:
            case none:
                weather_icon.setVisibility(View.GONE);
                weather_value.setVisibility(View.GONE);
                weather_header.setVisibility(View.GONE);
                break;
        }
    }

    private void alert() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.willrain)
                .setMessage("就快要下雨了!\n趕快收收躲雨吧")
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takeScreenshot();
        }
    }

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return;
        }

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {
        Intent shareIntent = new Intent();
        Uri uri = Uri.fromFile(imageFile);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, name + "\n\n#BeachTripHelper\n#NASAHackthon");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/jpeg");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "分享"));
    }
}
