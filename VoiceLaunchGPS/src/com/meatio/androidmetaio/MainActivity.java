
/**
 * autor: Carla Marisa Lobo Simões , Ana Catarina Marinho Ribeiro
 */
package com.meatio.androidmetaio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;




public class MainActivity extends Activity {

    // Default values for the language model and maximum number of recognition results
    // They are shown in the GUI when the app starts, and they are used when the user selection is not valid
    private final static int DEFAULT_NUMBER_RESULTS = 10;
    private final static String DEFAULT_LANG_MODEL = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;


    private int numberRecoResults = DEFAULT_NUMBER_RESULTS;
    private String languageModel = DEFAULT_LANG_MODEL;

    private static final String LOGTAG = "ASRBEGIN";
    private static int ASR_CODE = 123;

    private boolean latitud = false;
    private boolean longitud = false;

    private String longitud_str = "";
    private String latitud_str = "";
    private double longitud_num = 0;
    private double latitud_num = 0;

    private Button startGPS;
    GPSActivity gpsA;

    /**
     * Sets up the activity initializing the GUI
     */
    @SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speak_recognition);

        setSpeakButton();

        startGPS = (Button) findViewById(R.id.button_startGPS);

        // show location button click event
        startGPS.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // create class object
                gpsA = new GPSActivity(MainActivity.this);

                // check if GPS enabled
                if(gpsA.canGetLocation()){

                    double latitude = gpsA.getLatitude();
                    double longitude = gpsA.getLongitude();

                    if(longitud_num != 0 && latitud_num != 0){
                        String url = "http://maps.google.com/maps?saddr="+latitude+","+longitude+
                                "&daddr="+latitud_num+","+longitud_num;
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                        startActivity(intent);
                    }
                    Toast.makeText(getApplicationContext(), "Current Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gpsA.showSettingsAlert();
                }

            }
        });
    }

    /**
     * Initializes the speech recognizer and starts listening to the user input
     */
    private void listen()  {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Specify language model
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageModel);

        // Specify how many results to receive
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, numberRecoResults);

        // Start listening
        startActivityForResult(intent, ASR_CODE);
    }

    /**
     * Reads the values for the language model and the maximum number of recognition results
     * from the GUI
     */
    private void setRecognitionParams()  {
        numberRecoResults = DEFAULT_NUMBER_RESULTS;
        languageModel = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
    }

    /**
     * Sets up the listener for the button that the user
     * must click to start talking
     */
    @SuppressLint("DefaultLocale")
    private void setSpeakButton() {
        //Gain reference to speak button
        Button speak_longitud = (Button) findViewById(R.id.button_longitud);
        Button speak_latitud = (Button) findViewById(R.id.button_latitud);

        //Set up click listener
        speak_longitud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Speech recognition does not currently work on simulated devices,
                //it the user is attempting to run the app in a simulated device
                //they will get a Toast
                if("generic".equals(Build.BRAND.toLowerCase())){
                    Toast toast = Toast.makeText(getApplicationContext(),"ASR is not supported on virtual devices", Toast.LENGTH_SHORT);
                    toast.show();
                    Log.d(LOGTAG, "ASR attempt on virtual device");
                }
                else{
                    setRecognitionParams(); //Read speech recognition parameters from GUI
                    listen(); 				//Set up the recognizer with the parameters and start listening
                    longitud = true;
                }
            }
        });
        speak_latitud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Speech recognition does not currently work on simulated devices,
                //it the user is attempting to run the app in a simulated device
                //they will get a Toast
                if("generic".equals(Build.BRAND.toLowerCase())){
                    Toast toast = Toast.makeText(getApplicationContext(),"ASR is not supported on virtual devices", Toast.LENGTH_SHORT);
                    toast.show();
                    Log.d(LOGTAG, "ASR attempt on virtual device");
                }
                else{
                    setRecognitionParams(); //Read speech recognition parameters from GUI
                    listen(); 				//Set up the recognizer with the parameters and start listening
                    latitud = true;
                }
            }
        });
    }

    /**
     *  Shows the formatted best of N best recognition results (N-best list) from
     *  best to worst in the <code>ListView</code>.
     *  For each match, it will render the recognized phrase and the confidence with
     *  which it was recognized.
     */
    @SuppressLint("InlinedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ASR_CODE)  {
            if (resultCode == RESULT_OK)  {
                if(data!=null) {
                    //Retrieves the N-best list and the confidences from the ASR result
                    ArrayList<String> nBestList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    if(longitud){
                        longitud_str = nBestList.get(0);
                        longitud_str = longitud_str.replaceAll(" ", "");
                        longitud_str = longitud_str.replaceAll("menos", "-");
                        longitud_str = longitud_str.replaceAll("ponto", ".");
                        longitud_str = longitud_str.replaceAll("coma", ",");
                        TextView textResLong = (TextView) findViewById(R.id.data_longitud);
                        textResLong.setText(longitud_str);
                        longitud = false;
                        try {
                            longitud_num = Double.parseDouble(longitud_str);
                        } catch (Exception e) {
                            textResLong.setText("#ERROR#");
                        }
                    }
                    else if(latitud){
                        latitud_str = nBestList.get(0);
                        latitud_str = latitud_str.replaceAll(" ", "");
                        latitud_str = latitud_str.replaceAll("menos", "-");
                        latitud_str = latitud_str.replaceAll("ponto", ".");
                        latitud_str = latitud_str.replaceAll("coma", ",");
                        TextView textResLat = (TextView) findViewById(R.id.data_latitud);
                        textResLat.setText(latitud_str);
                        latitud = false;
                        try {
                            latitud_num = Double.parseDouble(latitud_str);
                        } catch (Exception e) {
                            textResLat.setText("#ERROR#");
                        }
                    }
                }
            }
            else {
                //Reports error in recognition error in log
                Log.e(LOGTAG, "Recognition was not successful");
            }
        }
    }
}