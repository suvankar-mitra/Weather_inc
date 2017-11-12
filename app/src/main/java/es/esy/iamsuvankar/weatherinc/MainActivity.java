package es.esy.iamsuvankar.weatherinc;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.github.pwittchen.weathericonview.WeatherIconView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION_ACCESS = 1;
    private static final String TAG = "MainActivity";
    private ImageView mBackgroud;
    //private TextView mTitle;
    private TextView mWeatherText;
    private TextView mWeatherLocation;
    private WeatherIconView mWeatherIconView;
    //private TextView mUVIndex;
    private ProgressBar mProgressBar;
    private ImageView mBlurBackground;
    private TextView mSunriseSet;
    private ImageView mMenuButton;

    private TextView mPressure;
    private TextView mHumidity;
    private TextView mVisibility;
    private TextView mPressureSubText;
    private TextView mVisibilitySubText;
    private TextView mHumiditySubText;
    private TextView mWind;
    private TextView mCloud;
    private TextView mRain;
    private TextView mWindSubText;
    private TextView mCloudSubText;
    private TextView mRainSubText;

    private boolean isDay = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_main);

        mBackgroud = (ImageView) findViewById(R.id.start_screen_background);
        Glide.with(this).load("https://drive.google.com/uc?export=view&id=1qFIzYnTHynA2Pmj-obothYGqOXpvnUNL").into(mBackgroud);

        FontManager fm = new FontManager(this);
        //mTitle = (TextView) findViewById(R.id.start_screen_title);
        //fm.setTypefaceJosefinBold(mTitle);

        mWeatherText = (TextView) findViewById(R.id.weather_text);
        fm.setTypefaceJosefinRegular(mWeatherText);

        mWeatherIconView = (WeatherIconView) findViewById(R.id.my_weather_icon);
        mWeatherLocation = (TextView) findViewById(R.id.weather_location);
        fm.setTypefaceJosefinRegular(mWeatherLocation);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mBlurBackground = (ImageView) findViewById(R.id.blur_all);

        mProgressBar.setVisibility(View.VISIBLE);
        mBlurBackground.setVisibility(View.VISIBLE);

        mPressure = (TextView) findViewById(R.id.pressure);
        mHumidity = (TextView) findViewById(R.id.humidity);
        mVisibility = (TextView) findViewById(R.id.visibility);
        mWind = (TextView) findViewById(R.id.wind);
        mCloud = (TextView) findViewById(R.id.cloudiness);
        mRain = (TextView) findViewById(R.id.rain);
        mPressureSubText = (TextView) findViewById(R.id.pressure_text);
        mHumiditySubText = (TextView) findViewById(R.id.humidity_text);
        mVisibilitySubText = (TextView) findViewById(R.id.visibility_text);
        mWindSubText = (TextView) findViewById(R.id.wind_text);
        mCloudSubText = (TextView) findViewById(R.id.cloud_text);
        mRainSubText = (TextView) findViewById(R.id.rain_text);
        //mUVIndex = (TextView) findViewById(R.id.weather_uvi);
        mSunriseSet = (TextView) findViewById(R.id.weather_sun_rise_set);

        fm.setTypefaceJosefinRegular(mPressure,mHumidity,mVisibility,mWind,mCloud,mRain,
                mPressureSubText,mHumiditySubText,mVisibilitySubText,mWindSubText,mCloudSubText,mRainSubText,
                mSunriseSet);

        mMenuButton = (ImageView) findViewById(R.id.main_menu);

        //Location access
        getLocationDetails();
    }

    public void getLocationDetails() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION_ACCESS);
        } else {
            //Check for location data in local
            double[] loc = checkLocalLocationData();
            if(loc.length == 2) {
                //get weather info from OpeWeatherAPI
                getWeatherDetails(loc[0],loc[1]);
                return;
            }

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location loc) {
                    Log.d(TAG, "onLocationChanged1: "+loc);
                    double lat = loc.getLatitude();
                    double lng = loc.getLongitude();
                    // Save location details to local
                    saveLocalLocationData(lat,lng);

                    //get weather info from OpeWeatherAPI
                    getWeatherDetails(lat,lng);
                    //getUVIndexDetails(lat,lng);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {
                    Log.d(TAG, "onProviderEnabled: ");
                }

                @Override
                public void onProviderDisabled(String provider) {}
            };
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1000, locationListener);
        }
    }

    private void getWeatherDetails(double lat, double lng) {

        // Check for local saved info
        String response = checkLocalWeatherData();

        // parse weather data if we have
        // saved data that is not 30 mins older
        if(response.length()>0){
            //Toast.makeText(this, "Already latest data", Toast.LENGTH_SHORT).show();
            showSnackBarDoNothing("Weather Data refreshed.");
            Log.d(TAG, "getWeatherDetails: Not going for api request");
            parseWeatherData(response);
            return;
        }

        Log.d(TAG, "getWeatherDetails: Going for api request");
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        String url ="http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lng+"&appid="+getString(R.string.open_weather_api)+
                "&units=metric";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: Api call");
                        saveLocalWeatherData(response);
                        parseWeatherData(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    private void parseWeatherData(String response) {
        String weatherText = "";
        String local = "";
        String description = "";
        char degree = '\u00B0';
        String temp = "";
        // Display the first 500 characters of the response string.
        Log.d(TAG, "parseWeatherData: "+response);
        try {
            JSONObject jsonObject = new JSONObject(response);

            local = jsonObject.getString("name");

            JSONObject obj = jsonObject.getJSONObject("sys");
            String country = new Locale("",obj.getString("country")).getDisplayCountry();
            mWeatherLocation.setText(Utility.firstCharCaps(local)+", "+Utility.firstCharCaps(country));

            long rise= (long) (obj.getLong("sunrise") * 1e3);
            long set= (long) (obj.getLong("sunset") * 1e3);
            String srise = Utility.getUTCtoISTTime(rise);
            String sset = Utility.getUTCtoISTTime(set);
            mSunriseSet.setText("Sunrise: "+srise+" AM, Sunset: "+sset+" PM");

            long now = new Date().getTime();
            if(now>set || now<rise) {
                isDay = false;
            } else {
                isDay = true;
            }

            local = jsonObject.getString("name");
            JSONArray wth = jsonObject.getJSONArray("weather");
            if(wth.length()>0) {
                description = wth.getJSONObject(0).getString("description");
                setWeatherIcons(description,isDay);
            }
            JSONObject obj2 = jsonObject.getJSONObject("main");
            temp = obj2.getString("temp");

            temp = String.valueOf(Integer.valueOf(Math.round(Float.valueOf(temp))));
            weatherText = temp+degree+"C | "+Utility.firstCharCaps(description);
            mWeatherText.setText(weatherText);
            String pr = obj2.getString("pressure");
            mPressure.setText(pr+" hPa");
            String hu = obj2.getString("humidity");
            mHumidity.setText(hu+"%");
            String vi = jsonObject.getString("visibility");
            if(Integer.valueOf(vi)>=1000) {
                vi = String.valueOf(Integer.valueOf(vi)/1000) + " km";
                mVisibility.setText(vi);
            }
            else
                mVisibility.setText(vi + " m");

            try {
                JSONObject obj4 = jsonObject.getJSONObject("wind");
                if(obj4!=null) {
                    String wi = obj4.getString("speed");
                    int speed = Math.round(Float.valueOf(wi) * 3.6f);
                    mWind.setText(speed+" kmph");
                    double deg = obj4.getDouble("deg");
                    String di = Utility.getDirection(deg);
                    mWind.setText(speed+" kmph\n"+di);
                }
            } catch (JSONException je1) {
                je1.printStackTrace();
            }

            try {
                JSONObject obj5 = jsonObject.getJSONObject("clouds");
                if(obj5!=null) {
                    String cl = obj5.getString("all");
                    mCloud.setText(cl+"%");
                }
            } catch (JSONException je) {
                je.printStackTrace();
            }

            try {
                JSONObject obj6 = jsonObject.getJSONObject("rain");
                if(obj6!=null) {
                    String ra = obj6.getString("3h");
                    mRain.setText(ra+" mm");
                }
            } catch (JSONException je) {
                je.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if(mProgressBar!=null)
                mProgressBar.setVisibility(View.INVISIBLE);
            if(mBlurBackground!=null)
                mBlurBackground.setVisibility(View.INVISIBLE);
        }
    }

    private double[] checkLocalLocationData() {
        double[] loc = new double[2];
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        loc[0] = prefs.getFloat("WEATHER_INC_LAT",0f);
        loc[1] = prefs.getFloat("WEATHER_INC_LONG",0f);
        Long ts = prefs.getLong("WEATHER_INC_LOC_TS",0l);
        Long now = new Date().getTime();
        if(now-ts > 900000) { // 15 min time interval
            return new double[0];
        }
        return loc;
    }

    private void saveLocalLocationData(double lat, double lng) {
        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefEditor.putFloat("WEATHER_INC_LAT", (float) lat);
        prefEditor.putFloat("WEATHER_INC_LONG", (float) lng);
        prefEditor.putLong("WEATHER_INC_LOC_TS",new Date().getTime());
        prefEditor.apply();
    }

    private String checkLocalWeatherData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherData = prefs.getString("WEATHER_INC_DATA", "");
        Long ts = prefs.getLong("WEATHER_INC_TS",0l);
        Long now = new Date().getTime();
        if(now-ts > 900000) { // 15 min time interval
            Log.d(TAG, "checkLocalWeatherData1: "+weatherData);
            return "";
        }
        Log.d(TAG, "checkLocalWeatherData2: "+weatherData);
        return weatherData;
    }

    private void saveLocalWeatherData(String data) {
        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefEditor.putString("WEATHER_INC_DATA", data);
        prefEditor.putLong("WEATHER_INC_TS",new Date().getTime());
        prefEditor.apply();
    }

//    private void getUVIndexDetails(double lat, double lng) {
//        // Instantiate the RequestQueue.
//        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
//        String url ="http://api.openweathermap.org/data/2.5/uvi?lat="+lat+"&lon="+lng+"&appid="+getString(R.string.open_weather_api);
//
//        // Request a string response from the provided URL.
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        String uvi = "";
//                        Log.d(TAG, "onResponse: Response is: "+ response);
//                        try {
//                            JSONObject jsonObject = new JSONObject(response);
//                            uvi = jsonObject.getString("value");
//                            float uv = Float.valueOf(uvi);
//                            String level = "";
//                            if(uv>=0f && uv<=2.9f) {
//                                level = "Low";
//                            } else if(uv>2.9f && uv<=5.9f) {
//                                level = "Moderate";
//                            } else if(uv>5.9f && uv<=7.9f) {
//                                level = "High";
//                            } else if(uv>7.9f && uv<=10.9f) {
//                                level = "Very High";
//                            } else if(uv>10.9f) {
//                                level = "Extreme";
//                            }
//                            uvi = "UV Index: "+uvi+", "+level;
//                            mUVIndex.setText(uvi);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.d(TAG, "onErrorResponse: That didn't work!");
//            }
//        });
//        // Add the request to the RequestQueue.
//        queue.add(stringRequest);
//    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    LocationListener locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location loc) {
                            double lat = loc.getLatitude();
                            double lng = loc.getLongitude();
                            //get weather info from OpeWeatherAPI
                            getWeatherDetails(lat,lng);
                            //getUVIndexDetails(lat,lng);
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {}

                        @Override
                        public void onProviderEnabled(String provider) {}

                        @Override
                        public void onProviderDisabled(String provider) {}
                    };
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1000, locationListener);
                } else {

                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("We need location access to show you weather information.")
                            .setPositiveButton("Ok",new DialogInterface.OnClickListener(){

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getLocationDetails();
                                }
                            })
                            .create().show();
                }
                return;
            }
        }
    }

    public void setWeatherIcons(String description, boolean isDay) {
        if(isDay) {
            mWeatherText.setTextColor(Color.BLACK);
            mWeatherLocation.setTextColor(Color.BLACK);
            mSunriseSet.setTextColor(Color.BLACK);
            mWeatherIconView.setIconColor(Color.BLACK);
            mMenuButton.setImageResource(R.drawable.ic_more_vert_black_24dp);
        } else {
            mWeatherText.setTextColor(Color.WHITE);
            mWeatherLocation.setTextColor(Color.WHITE);
            mSunriseSet.setTextColor(Color.WHITE);
            mWeatherIconView.setIconColor(Color.WHITE);
            mMenuButton.setImageResource(R.drawable.ic_more_vert_white_24dp);
        }
        if(description.equalsIgnoreCase("few clouds") ||
                description.equalsIgnoreCase("scattered clouds") ||
                description.equalsIgnoreCase("broken clouds")) {
            mMenuButton.setImageResource(R.drawable.ic_more_vert_white_24dp);
            if(isDay) {
                mWeatherIconView.setIconResource(getString(R.string.wi_day_cloudy));
                Glide.with(this).load("https://drive.google.com/uc?export=view&id=1f0gbdSw5JcJC906cxLgzYlEZ4T5nuZ9X").into(mBackgroud);
            }
            else {
                mWeatherIconView.setIconResource(getString(R.string.wi_night_cloudy));
                Glide.with(this).load("https://drive.google.com/uc?export=view&id=1mjhXCN0sIvqwmOY9sLRKtyA7Q74dRWx_").into(mBackgroud);
            }
        } else if(description.equalsIgnoreCase("clear sky")) {
            //mWeatherIconView.setIconColor(Color.WHITE);
            if(isDay) {
                mWeatherIconView.setIconResource(getString(R.string.wi_wu_clear));
                Glide.with(this).load("https://drive.google.com/uc?export=view&id=1pDda67x9B20St5qDuyEjjPU7HFYwoaf1").into(mBackgroud);
            } else {
                mWeatherIconView.setIconResource(getString(R.string.wi_night_clear));
                Glide.with(this).load("https://drive.google.com/uc?export=view&id=1NXrQkDVKXLd_HMbl6gBgrl6z51ULxzFy").into(mBackgroud);
            }
        } else if(description.equalsIgnoreCase("shower rain") || description.equalsIgnoreCase("rain")) {
            if(isDay) {
                mWeatherIconView.setIconResource(getString(R.string.wi_day_rain));
                Glide.with(this).load("https://drive.google.com/uc?export=view&id=1Yyq-SIRfp0eCqNdtyZOYnV84mAtzUYfF").into(mBackgroud);
            } else {
                mWeatherIconView.setIconResource(getString(R.string.wi_night_rain));
                Glide.with(this).load("https://drive.google.com/uc?export=view&id=1ewBgGOWT9xTSK9Z1Ch8qUagueX_ngy_5").into(mBackgroud);
            }
        } else if(description.equalsIgnoreCase("thunderstorm")) {
            mWeatherText.setTextColor(Color.WHITE);
            mWeatherLocation.setTextColor(Color.WHITE);
            mSunriseSet.setTextColor(Color.WHITE);
            mWeatherIconView.setIconColor(Color.WHITE);
            mMenuButton.setImageResource(R.drawable.ic_more_vert_white_24dp);
            if(isDay)
                mWeatherIconView.setIconResource(getString(R.string.wi_day_thunderstorm));
            else
                mWeatherIconView.setIconResource(getString(R.string.wi_night_thunderstorm));
            Glide.with(this).load("https://drive.google.com/uc?export=view&id=1y1rP-FkdYlSY2HauEDRLRARcP_KfnCAg").into(mBackgroud);
        } else if(description.equalsIgnoreCase("snow")) {
            mWeatherText.setTextColor(Color.WHITE);
            mWeatherLocation.setTextColor(Color.WHITE);
            mSunriseSet.setTextColor(Color.WHITE);
            mWeatherIconView.setIconColor(Color.WHITE);
            mMenuButton.setImageResource(R.drawable.ic_more_vert_white_24dp);
            if(isDay)
                mWeatherIconView.setIconResource(getString(R.string.wi_day_snow));
            else
                mWeatherIconView.setIconResource(getString(R.string.wi_night_snow));
            Glide.with(this).load("https://drive.google.com/uc?export=view&id=18I62kpOB7HTf_3FWOB0fUEgx0uFjw-Ob").into(mBackgroud);
        } else if(description.equalsIgnoreCase("mist")) {
            if(isDay) {
                mWeatherIconView.setIconResource(getString(R.string.wi_day_fog));
                Glide.with(this).load("https://drive.google.com/uc?export=view&id=1CXHY5MacCXne_IlhmtNLhvuMtPyqeV9R").into(mBackgroud);
            }
            else {
                mWeatherIconView.setIconResource(getString(R.string.wi_night_fog));
                Glide.with(this).load("https://drive.google.com/uc?export=view&id=1sj3R8Scf-anGQ1Hl4AEzR7CR9zVeVkHn").into(mBackgroud);
            }
        }
    }

    public void refreshData() {
        //Toast.makeText(this, "Refreshing", Toast.LENGTH_SHORT).show();
        showSnackBarDoNothing("Refreshing");
        mProgressBar.setVisibility(View.VISIBLE);
        mBlurBackground.setVisibility(View.VISIBLE);
        getLocationDetails();
    }

    public void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this,mMenuButton);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.menu_refrsh:
                        refreshData();
                        break;
                    case R.id.menu_settings:
                        showSnackBarDoNothing("Settings is not available yet.");
                        break;
                }
                return true;
            }
        });

        popup.show(); //showing popup menu
    }

    private void showSnackBarDoNothing(String msg) {
        Snackbar.make(findViewById(R.id.start_screen_layout),msg,Snackbar.LENGTH_LONG)
                .setAction("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //do nothing
                    }
                })
                .show();
    }
}
