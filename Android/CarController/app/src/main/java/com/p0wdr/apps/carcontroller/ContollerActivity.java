package com.p0wdr.apps.carcontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.p0wdr.apps.carcontroller.util.SensorFusion;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.UUID;


public class ContollerActivity extends ActionBarActivity implements SensorEventListener {

    private SensorFusion sensorFusion;

    SensorManager sensorManager;
    private float x = 0, y = 0, z = 0;
    private TextView xAccel, yAccel, zAccel;

    private static final String TAG = "ContollerActivity";


    BluetoothDevice bluetoothDevice;
    private ProgressDialog progress;
    Button disconnectButton;
    Button motorAForward;
    Button motorAReverse;
    Button motorAStop;
    Button servo45;
    Button servo90;
    Button servo135;

    Switch accelCtrltoggle;
    String lastServoCommand = "";
    String lastMotorCommand = "";
    private boolean isBluetoothConnected = false;
    private boolean acceptAccelerometerInput = false;
    BluetoothSocket bluetoothSocket;

    private static UUID SERIAL_DEVICE_UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private class BluetoothConnectTask extends AsyncTask<Void, Void, Void> {

        private boolean connected = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ContollerActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(SERIAL_DEVICE_UUID);
                bluetoothSocket.connect();
            } catch (IOException e) {
                connected = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!connected) {
                Toast.makeText(getApplicationContext(), "Connection Failed.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Connected.", Toast.LENGTH_LONG).show();
                isBluetoothConnected = true;
            }
            progress.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contoller);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        disconnectButton = (Button) findViewById(R.id.disconnect);
        motorAForward = (Button) findViewById(R.id.motoraforward);
        motorAReverse = (Button) findViewById(R.id.motorareverse);
        motorAStop = (Button) findViewById(R.id.motorastop);

        servo45 = (Button) findViewById(R.id.servo45);
        servo90 = (Button) findViewById(R.id.servo90);
        servo135 = (Button) findViewById(R.id.servo135);

        azimuthText = (TextView) findViewById(R.id.azimuth);
        pitchText = (TextView) findViewById(R.id.pitch);
        rollText = (TextView) findViewById(R.id.roll);

        if (null != getIntent().getExtras()) {
            bluetoothDevice = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        }

        if (bluetoothDevice != null) {
            new BluetoothConnectTask().execute();
        }


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        registerSensorManagerListeners();

        sensorFusion = new SensorFusion();
        sensorFusion.setMode(SensorFusion.Mode.FUSION);

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        motorAForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMotorAForward();
            }
        });
        motorAReverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMotorAReverse();
            }
        });
        motorAStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMotorAStop();
            }
        });
        servo45.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendServoAAngle(45);
            }
        });
        servo90.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendServoAAngle(90);
            }
        });
        servo135.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendServoAAngle(135);
            }
        });


        accelCtrltoggle = (Switch) findViewById(R.id.accel_ctrl);
        accelCtrltoggle.setChecked(acceptAccelerometerInput);
        accelCtrltoggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                acceptAccelerometerInput = isChecked;
            }
        });
    }

    private void setMotorAForward() {
        sendCommandViaBluetooth("CMDMAF255;".toString());
    }

    private void setMotorAReverse() {
        sendCommandViaBluetooth("CMDMAR255;".toString());
    }

    private void setMotorAStop() {
        sendCommandViaBluetooth("CMDMAS;".toString());
    }

    private void sendCommandViaBluetooth(String command) {
        if (isBluetoothConnected) {
            try {
                Log.d(TAG, command);
                bluetoothSocket.getOutputStream().write(command.getBytes());
            } catch (IOException e) {
                Log.e(TAG, command + ' ' + e.getLocalizedMessage());
            }
        } else {
            Log.d(TAG, "Tried to send :" + command + " but bluetooth is not connected");
        }
    }

    private void sendMotorAForward(int speed) {
        sendMotorCommand("CMDMAF" + speed + ";");
    }

    private void sendMotorCommand(String command) {
        if (!lastMotorCommand.equals(command)) {
            lastMotorCommand = command;
            sendCommandViaBluetooth(command);
        }
    }

    private void sendMotorAReverse(int speed) {
        sendMotorCommand("CMDMAR" + speed + ";");
    }

    private void stopMotorA() {
        sendMotorCommand("CMDMAS;");
    }



    private void sendServoAAngle(int angle) {
        String command = "CMDSA" + angle + ";";
        if (!lastServoCommand.equals(command)){
            lastServoCommand = command;
            sendCommandViaBluetooth(command);
        }
    }

    private void sensorFusionControl(){
        if (acceptAccelerometerInput) {
            double rollValue =  sensorFusion.getRoll();
            double pitchValue =  sensorFusion.getPitch();

            // Motor control
            // Upper bound the roll
            if (rollValue > 0){
                rollValue = 0;
            }
            int speed = makeSpeedFromRoll(rollValue);
            if (speed == 0){
                stopMotorA();
            } else {
                if (rollValue > -90){
                    sendMotorAForward(speed);
                } else {
                    sendMotorAReverse(speed);
                }
            }

            // Servo Control
            // Gate the pitch
            pitchValue = Math.min(pitchValue, 50);
            pitchValue = Math.max(pitchValue, -50);
            int angle = makeAngleFromPitch(pitchValue);
            sendServoAAngle(angle);

        }
    }

    private int makeAngleFromPitch(double pitchValue) {
        double swing = 90*(pitchValue/50);
        return 90 + (int)swing;
    }
    private int makeSpeedFromRoll(double roll){
        roll = Math.abs(Math.round(Math.abs(roll)) - 90);
        double speed = 255 * (roll/90);
        return (int) speed;
    }

    private TextView azimuthText, pitchText, rollText;
    private DecimalFormat d = new DecimalFormat("#");

    public void updateOrientationDisplay(){
        double azimuthValue = sensorFusion.getAzimuth();
        double rollValue =  sensorFusion.getRoll();
        double pitchValue =  sensorFusion.getPitch();

        azimuthText.setText(String.valueOf(d.format(azimuthValue)));
        pitchText.setText(String.valueOf(d.format(pitchValue)));
        rollText.setText(String.valueOf(d.format(rollValue)));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                sensorFusion.setAccel(event.values);
                sensorFusion.calculateAccMagOrientation();
                break;

            case Sensor.TYPE_GYROSCOPE:
                sensorFusion.gyroFunction(event);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                sensorFusion.setMagnet(event.values);
                break;
        }

        sensorFusionControl();
        updateOrientationDisplay();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void registerSensorManagerListeners(){
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_UI);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onResume() {
        registerSensorManagerListeners();
        super.onResume();
    }


    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
        disconnect();
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(this);
        super.onStop();
    }

    private void disconnect() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error.", Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contoller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
