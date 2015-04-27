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

import java.io.IOException;
import java.util.Date;
import java.util.UUID;


public class ContollerActivity extends ActionBarActivity implements SensorEventListener {


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
    Switch accelCtrltoggle;
    Date lastCommand = new Date();
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


        xAccel = (TextView) findViewById(R.id.xAccel);
        yAccel = (TextView) findViewById(R.id.yAccel);
        zAccel = (TextView) findViewById(R.id.zAccel);

        if (null != getIntent().getExtras()) {
            bluetoothDevice = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        }

        if (bluetoothDevice != null) {
            new BluetoothConnectTask().execute();
        }


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

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
        Date now = new Date();
        if (isBluetoothConnected) {
           if (now.getTime() - lastCommand.getTime() > 10) {
               Log.d(TAG, command);
               lastCommand = new Date();
               try {
                   bluetoothSocket.getOutputStream().write(command.getBytes());
               } catch (IOException e) {

               }
           } else {
               Log.d(TAG, command + " drop on the floor due to throttling");
           }
        } else {
            Log.d(TAG, "Tried to send :" + command + " but bluetooth is not connected");
        }
    }

    private void sendMotorAForward(int speed) {
        String command = "CMDMAF" + speed + ";";
        sendCommandViaBluetooth(command);
    }

    private void sendMotorAReverse(int speed) {
        String command = "CMDMAR" + speed + ";";
        sendCommandViaBluetooth(command);
    }

    private int makeSpeedFrom(float in){
        int speed = (int) (255 * (Math.abs(in) / 10));
        return Math.min(speed, 255); //Make 255 the ceiling
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

        if (acceptAccelerometerInput) {
            int speed = makeSpeedFrom(z); // use the Z index as a percentage
            if (z > 0) { //Forward
                sendMotorAForward(speed);
            }
            if (z < 0) {//Reverse
                sendMotorAReverse(speed);
            }
        }
        xAccel.setText("x = " + x);
        yAccel.setText("y = " + y);
        zAccel.setText("z = " + z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }


    @Override
    protected void onPause() {
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
