package com.example.token.btcontrol;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ImageButton btnConsole, btnStart, btnDis;
    TextView statusText;
    String address = null;
    String btBuffer = null;
    int consoleStatus = 0;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;

    private TextView mReadRpm;
    private TextView mReadPres;
    private TextView mReadFuel;
    private TextView mReadBatt;
    private TextView mReadTemp;

    private TextView lReadRpm;
    private TextView lReadPres;
    private TextView lReadFuel;
    private TextView lReadBatt;
    private TextView lReadTemp;

    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data

    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3;





    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }


        Intent newint = getIntent();
        address = newint.getStringExtra(deviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the MainActivity
        setContentView(R.layout.activity_main);

        //call the widgtes
        btnConsole = (ImageButton)findViewById(R.id.buttonConsole);
        btnStart = (ImageButton)findViewById(R.id.buttonStart);
        btnDis = (ImageButton)findViewById(R.id.buttonDisconnect);
        statusText = (TextView) findViewById(R.id.textStatus);

        mReadRpm = (TextView) findViewById(R.id.textRpm);
        mReadPres = (TextView) findViewById(R.id.textPres);
        mReadFuel = (TextView) findViewById(R.id.textFuel);
        mReadBatt = (TextView) findViewById(R.id.textBattery);
        mReadTemp = (TextView) findViewById(R.id.textTemp);

        lReadRpm = (TextView) findViewById(R.id.labelRpm);
        lReadPres = (TextView) findViewById(R.id.labelPres);
        lReadFuel = (TextView) findViewById(R.id.labelFuel);
        lReadBatt = (TextView) findViewById(R.id.labelBatt);
        lReadTemp = (TextView) findViewById(R.id.labelTemp);


        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if(consoleStatus == 1) {
                        String[] separated = readMessage.split(" ");
                        if(separated.length == 6) {
                            if (separated[0] != null) {
                                mReadRpm.setText(separated[0].trim());
                            }
                            if (separated[1] != null) {
                                mReadPres.setText(separated[1].trim() + " ");
                            }
                            if (separated[2] != null) {
                                mReadFuel.setText(separated[2].trim() + " %");
                            }
                            if (separated[3] != null) {
                                mReadBatt.setText(separated[3].trim() + " V");
                            }
                            if (separated[4] != null) {
                                mReadTemp.setText(separated[4].trim() + " °");
                            }
                        }
                    }
                }
            }
        };

        new ConnectBT().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        btnConsole.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(consoleStatus==0) {
                    turnOnConsole();
                }else{
                    turnOffConsole();
                }
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Disconnect(); //close connection
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                motorStart(); //close connection
            }
        });

        btnStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        motorStart();
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        motorStop();
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });


    }





    private void Disconnect(){
        if (btSocket!=null){
            try{
                btSocket.close(); //close connection
            }
            catch (IOException e){ msg("Error");}
        }
        finish(); //return to the first layout
    }

    private void turnOffConsole(){
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write("CONSOLE 0".toString().getBytes());
                consoleStatus = 0;
                mReadRpm.setTextColor(0XFF666666);
                mReadPres.setTextColor(0XFF666666);
                mReadFuel.setTextColor(0XFF666666);
                mReadBatt.setTextColor(0XFF666666);
                mReadTemp.setTextColor(0XFF666666);
                statusText.setTextColor(0XFF666666);

                lReadRpm.setTextColor(0XFF666666);
                lReadPres.setTextColor(0XFF666666);
                lReadFuel.setTextColor(0XFF666666);
                lReadBatt.setTextColor(0XFF666666);
                lReadTemp.setTextColor(0XFF666666);

                btnConsole.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_red,this.getTheme())));

                statusText.setText("Quadro OFF");
                btnStart.setEnabled(false);
            }
            catch (IOException e){msg("Error");}
        }
    }
    private void turnOnConsole(){
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write("CONSOLE 1".toString().getBytes());
                consoleStatus = 1;

                mReadRpm.setTextColor(0XFFFFFFFF);
                mReadPres.setTextColor(0XFFFFFFFF);
                mReadFuel.setTextColor(0XFFFFFFFF);
                mReadBatt.setTextColor(0XFFFFFFFF);
                mReadTemp.setTextColor(0XFFFFFFFF);
                statusText.setTextColor(0XFFFFFFFF);

                lReadRpm.setTextColor(0XFFFFFFFF);
                lReadPres.setTextColor(0XFFFFFFFF);
                lReadFuel.setTextColor(0XFFFFFFFF);
                lReadBatt.setTextColor(0XFFFFFFFF);
                lReadTemp.setTextColor(0XFFFFFFFF);

                btnConsole.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_green,this.getTheme())));

                statusText.setText("Quadro ON");
                btnStart.setEnabled(true);

            }
            catch (IOException e) {msg("Error");}
        }
    }
    private void motorStart(){
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write("MOTOR 1".toString().getBytes());
                btnStart.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_green,this.getTheme())));
                statusText.setText("Accensione ...");
            }
            catch (IOException e) {msg("Error");}
        }
    }

    private void motorStop(){
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write("MOTOR 0".toString().getBytes());
                btnStart.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_grey,this.getTheme())));
                statusText.setText("Quadro ON");
            }
            catch (IOException e) {msg("Error");}
        }
    }














    // fast way to call Toast
    private void msg(String s){
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices){
            try{
                if (btSocket == null || !isBtConnected){
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice btDevice = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e){
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);

            if (!ConnectSuccess){
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }else{
                msg("Connesso.");
                isBtConnected = true;
                mConnectedThread = new ConnectedThread(btSocket);
                mConnectedThread.start();


            }
            progress.dismiss();
        }

    }





















    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        //private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            //OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                //tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            //mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if(bytes != 0) {
                        SystemClock.sleep(100);
                        
                        //mReadBuffer.setText(mmInStream.read(buffer));
                    }
                    // Send the obtained bytes to the UI activity

                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

    }

}
