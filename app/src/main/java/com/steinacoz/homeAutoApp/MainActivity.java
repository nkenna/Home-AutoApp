package com.steinacoz.homeAutoApp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;


import java.util.Arrays;


public class MainActivity extends Activity {

    //private MediaPlayer mediaPlayer;
    // private SurfaceHolder vidHolder;
    private WebView webSurface;
    String vidAddress = "http://192.168.43.25/view";

    PubnubConfig pubnubConfig;
    PubNub pubnub;

    //create textViews widgets
    TextView tempTxtview, humidTxtview, statusTxtview;

    ToggleButton securityModebtn;
    Button leftbtn, ritebtn;
    SeekBar securityliteBar, garageliteBar, bedroomliteBar, sittingroomliteBar, fanBar, frontDoorBar, garageDoorBar;
    Switch fan, garageDoor, frontDoor;

    PubConSubThread pubConSubThread;// new PubConSubThread();
    PubPublishThread pubPublishThread;// new PubPublishThread();

    //pubnub channels

    String MODE = "MODE";
    String SENSORS_TEMP = "SENSORS_TEMP";
    String SENSORS_HUMID = "SENSORS_HUMID";
    String FEEDBACKS = "FEEDBACKS";
    String PHOTO = "PHOTO";
    String GARAGE_DOOR = "GARAGE_DOOR";
    String FRONT_DOOR = "FRONT_DOOR";
    String FAN = "FAN";
    String BEDROOM_LIGHT = "BEDROOM_LIGHT";
    String SITTINGROOM_LIGHT = "SITTINGROOM_LIGHT";
    String GARAGE_LIGHT = "GARAGE_LIGHT";
    String SECURITY_LIGHT = "SECURITY_LIGHT";
    String ELECTRICAL = "ELECTRICAL";
    String CAMERA = "CAMERA";
    String SENSORS = "SENSORS";


    //payloads


    String sensors_Data = "get data";

    String MOTION_PIR = "MOTION DETECTED";

    String take_photo = "take photo";

    // operating lights
    String securityLight_ON = "SECURITY LIGHT ON";
    String securityLight_OFF = "SECURITY LIGHT OFF";

    String garageLight_ON = "GARAGE LIGHT ON";
    String garageLight_OFF = "GARAGE LIGHT OFF";

    String sittingRoomLight_ON = "SITTING ROOM LIGHT ON";
    String sittingRoomLight_OFF = "SITTING ROOM LIGHT OFF";

    String bedroomLight_ON = "BEDROOM LIGHT ON";
    String bedroomLight_OFF = "BEDROOM LIGHT OFF";

    // operating doors
    String garageDoor_OPEN = "GARAGE DOOR OPEN";
    String garageDoor_CLOSE = "GARAGE DOOR CLOSE";

    String frontDoor_OPEN = "FRONT DOOR OPEN";
    String frontDoor_CLOSE = "FRONT DOOR CLOSE";

    // operating electrical appliances
    String fan_ON = "FAN ON";
    String fan_OFF = "FAN OFF";

    String light_ON = "LIGHT ON";
    String light_OFF = "LIGHT OFF";

    // operating camera
    String camera_Right = "RIGHT";  // right is to sitting room
    String camera_Left = "LEFT";  // left is to bedroom
    String camera_pic = "PIC";

    //changing mode
    String changeMode_AUTO = "AUTO";
    String changeMode_MANUAL = "MANUAL";

    // detecting motion
    String MOTION_PIR1 = "PIR1 MOTION";
    String MOTION_PIR2 = "PIR2 MOTION";
    String MOTION_PIR3 = "PIR3 MOTION";


    // message and channel that will be used during publishing
    String payload;
    String channel;

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize Pubnub Configuration and PubNub
        pubnubConfig = new PubnubConfig();
        pubnub = new PubNub(pubnubConfig.pConfig());

        tempTxtview = (TextView) findViewById(R.id.temp_txtView);
        humidTxtview = (TextView) findViewById(R.id.humid_textView);
        statusTxtview = (TextView) findViewById(R.id.status_txtView);

        securityModebtn = (ToggleButton) findViewById(R.id.securityModebtn);
        ritebtn = (Button)findViewById(R.id.ritebtn);
        leftbtn = (Button)findViewById(R.id.leftbtn);

        securityliteBar = (SeekBar) findViewById(R.id.securityliteBar);
        garageliteBar = (SeekBar) findViewById(R.id.garageliteBar);
        sittingroomliteBar = (SeekBar) findViewById(R.id.sittingroomBar);
        fan = (Switch) findViewById(R.id.fanswitch);
        frontDoor = (Switch) findViewById(R.id.frontdoorSwitch);
        garageDoor = (Switch) findViewById(R.id.garagedoorswitch);

        bedroomliteBar = (SeekBar) findViewById(R.id.bedroomliteBar);

        webSurface = (WebView) findViewById(R.id.webView);
        webSurface.setWebChromeClient(new WebChromeClient());
        webSurface.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSurface.getSettings().setJavaScriptEnabled(true);
        webSurface.loadUrl(vidAddress);

        //subscribe to pubnub channels
        pubnubSubscribe();



        ritebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ritebtnPressed();
            }
        });

        leftbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leftbtnPressed();
            }
        });

        securityModebtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                securityModeChanged(b);
            }
        });

        securityModebtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                securityModeChanged(b);
            }
        });

        securityliteBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int value = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                value = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pubPublishThread.pubnubPublish(String.valueOf(value), SECURITY_LIGHT);
            }
        });

        garageliteBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int value = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                value = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pubPublishThread.pubnubPublish(String.valueOf(value), GARAGE_LIGHT);
            }
        });

        bedroomliteBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int value = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                value = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pubPublishThread.pubnubPublish(String.valueOf(value), BEDROOM_LIGHT);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pubPublishThread.pubnubPublish(String.valueOf(value), BEDROOM_LIGHT);
            }
        });

        sittingroomliteBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int value = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                value = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pubPublishThread.pubnubPublish(String.valueOf(value), SITTINGROOM_LIGHT);
            }
        });



        fan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                switchFan(b);
            }
        });

        frontDoor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                operateFrontDoor(b);
            }
        });

        garageDoor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                operateGarageDoor(b);
            }
        });


            }


    public void plsyTone(){
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone ringtone = RingtoneManager.getRingtone(mContext, uri);
        ringtone.play();
    }

    public void switchFan(boolean b) {
        if (b){
            pubPublishThread.pubnubPublish(fan_ON, FAN);
        }else{
            pubPublishThread.pubnubPublish(fan_OFF, FAN);
        }
    }

    public void operateGarageDoor(boolean b){
        if (b){
            pubPublishThread.pubnubPublish(garageDoor_OPEN, GARAGE_DOOR);
        }else{
            pubPublishThread.pubnubPublish(garageDoor_CLOSE, GARAGE_DOOR);
        }
    }

    public void operateFrontDoor(boolean b){
        if (b){
            pubPublishThread.pubnubPublish(frontDoor_OPEN, FRONT_DOOR);
        }else{
            pubPublishThread.pubnubPublish(frontDoor_CLOSE, FRONT_DOOR);
        }
    }

    void pubnubSubscribe(){

        pubPublishThread = new PubPublishThread();
        pubConSubThread = new PubConSubThread();


        try {

            pubConSubThread.start();
            pubPublishThread.start();
        }catch (Exception e){
            statusTxtview.setText(e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //this method is called from the PubConSub thread
    public void updateStatusTxtView(String message){
        statusTxtview.setText(message);
    }

    /**this method is called from the Pubpublish thread
     and updates the status textView **/
    public void updatePubTextView(int colour, String publishStatus){
        statusTxtview.setTextColor(colour);
        statusTxtview.setText(publishStatus);
    }

    /**this method is called from the PubConSub thread
     and updates the connect textView **/
    public void updateConnectTextView(int colour, String connectStatus){
        statusTxtview.setTextColor(colour);
        statusTxtview.setText(connectStatus);
    }





    /**this method is called from the Pubpublish thread
     and updates the amb. temp textView**/
    public void updateTempTextView(String value){
        tempTxtview.setText(value);
        }


    /**this method is called from the Pubpublish thread
     and updates the status textView **/
    public void updateHumdityTextView(String value){
        humidTxtview.setText(value);
    }



    void ritebtnPressed(){
        payload = camera_Right;
        channel = CAMERA;
        pubPublishThread.pubnubPublish(payload, channel);

    }

    void leftbtnPressed(){
        payload = camera_Left;
        channel = CAMERA;
        pubPublishThread.pubnubPublish(payload, channel);

    }

    public void securityModeChanged(boolean chk) {
        if (chk) {
            pubPublishThread.pubnubPublish(changeMode_AUTO, MODE);
        } else {
            pubPublishThread.pubnubPublish(changeMode_MANUAL, MODE);
        }
    }




    //subcribe class outside UI thread
    public class PubConSubThread extends Thread {
        public static final String Tag = "Pubnub Connect Subscribe Thread";
        private static final int DELAY = 5000;

        PubPublishThread pp = new PubPublishThread();

       @Override
        public void run() {
            try {
                pubnubConSubscribe();
            }catch (Exception e){
                final String err = e.getMessage();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(MainActivity.this, err, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        //pubnub connect and subscribe
        private void pubnubConSubscribe(){

            connectProgress(Color.RED, "Connecting");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "inside pubnubConSubscribe method", Toast.LENGTH_SHORT).show();
                }
            });


            //final String direction = "direction";


            pubnub.addListener(new SubscribeCallback() {
                @Override
                public void status(PubNub pubnub, PNStatus status) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "inside status pubnub addlistener", Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory){
                        connectProgress(Color.RED, "Disconnected");
                        pubnub.reconnect();
                    }else if (status.getCategory() == PNStatusCategory.PNConnectedCategory){
                        pp.pubnubPublish(sensors_Data, SENSORS);
                        if (status.getCategory() == PNStatusCategory.PNConnectedCategory){
                            connectProgress(Color.GREEN, "Connected");
                            //pubnub.subscribe().channels(Arrays.asList(FEEDBACKS, SENSOR_DATA, SENSORS_HUMID, SENSORS_SOIL, SENSORS_TEMP, CAMERA, IRRIGATE )).execute();

                        }else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory){
                            pp.pubnubPublish(sensors_Data, SENSORS);
                            connectProgress(Color.GREEN, "Reconnected");
                            // pubnub.subscribe().channels(Arrays.asList(FEEDBACKS, SENSOR_DATA, SENSORS_HUMID, SENSORS_SOIL, SENSORS_TEMP, CAMERA, IRRIGATE )).execute();

                        }else if (status.getCategory() == PNStatusCategory.PNDecryptionErrorCategory){
                            pubnub.reconnect();

                        }else if (status.getCategory() == PNStatusCategory.PNTimeoutCategory){
                            connectProgress(Color.RED, "Network Timeout");
                            pubnub.reconnect();

                        }else {
                            pubnub.reconnect();

                            connectProgress(Color.RED, "No Connection");
                        }
                    }
                }

                @Override
                public void message(PubNub pubnub, final PNMessageResult message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (message.getChannel().equalsIgnoreCase(FEEDBACKS)) {

                                if (message.getMessage().getAsString().equalsIgnoreCase("BEDROOM MOTION") ||
                                        message.getMessage().getAsString().equalsIgnoreCase("GARAGE MOTION") ||
                                        message.getMessage().getAsString().equalsIgnoreCase("SITTINGROOM MOTION")){
                                    plsyTone();
                                }

                                updateStatusTxtView(message.getMessage().getAsString());

                            }else if (message.getChannel().equalsIgnoreCase(SENSORS_TEMP)){
                                updateTempTextView(message.getMessage().getAsString());

                            }else if (message.getChannel().equalsIgnoreCase(SENSORS)){
                                updateStatusTxtView(message.getMessage().getAsString());

                            }else if (message.getChannel().equalsIgnoreCase(SENSORS_HUMID)){
                                updateHumdityTextView(message.getMessage().getAsString());

                            }else if (message.getChannel().equalsIgnoreCase(PHOTO)){
                                updateStatusTxtView(message.getMessage().getAsString());

                            }


                        }
                    });
                }

                @Override
                public void presence(PubNub pubnub, PNPresenceEventResult presence) {

                }
            });

            pubnub.subscribe().channels(Arrays.asList(FEEDBACKS, SENSORS, CAMERA, SENSORS_HUMID, SENSORS_TEMP, MODE, PHOTO,
                    ELECTRICAL, GARAGE_DOOR, FRONT_DOOR, FAN, BEDROOM_LIGHT, SITTINGROOM_LIGHT, GARAGE_LIGHT, SECURITY_LIGHT )).execute();
        }

        private void connectProgress(int colour, String connectStatus){
            final int col = colour;
            final String cStatus = connectStatus;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateConnectTextView(col, cStatus);
                }
            });

        }


    }

    //publish class thread
    public class PubPublishThread extends Thread {
        public static final String Tag = "Pubnub Publish Thread";
        private static final int DELAY = 5000;

        MainActivity mainActivity = new MainActivity();


        @Override
        public void run() {
            super.run();

        }



        public void pubnubPublish(String payload, String channel){

            try {
                pubnub.publish().message(payload).channel(channel).async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        if (!status.isError()) {
                            publishProgress(Color.GREEN, "publish success");
                        } else {
                            publishProgress(Color.RED, status.getCategory().toString());
                            status.retry();
                        }

                    }
                });
            }catch (Exception e){
                final String err = e.getMessage();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, err, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private void publishProgress(final int colour, final String publishStatus){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatePubTextView(colour, publishStatus);
                }
            });
        }
    }






}
