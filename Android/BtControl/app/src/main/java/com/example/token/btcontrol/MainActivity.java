package com.example.token.btcontrol;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;

import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btnConsole, btnStart, btnDis;
    TextView statusText;
    String address = null;
    String btBuffer = null;
    int consoleStatus = 0;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;


    private TextView mReadBuffer;
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

        Intent newint = getIntent();
        address = newint.getStringExtra(deviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the MainActivity
        setContentView(R.layout.activity_main);

        //call the widgtes
        btnConsole = (Button)findViewById(R.id.button2);
        btnStart = (Button)findViewById(R.id.button3);
        btnDis = (Button)findViewById(R.id.button4);
        statusText = (TextView) findViewById(R.id.textStatus);
        mReadBuffer = (TextView) findViewById(R.id.readBuffer);


        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    //try {
                        //readMessage = new String((byte[]) msg.obj, "UTF-8");
                        readMessage = msg.toString();
                    /*} catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }*/
                    mReadBuffer.setText(readMessage);
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
                        // PRESSED
                        motorStart();
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
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
                btSocket.getOutputStream().write("CONSOLE\t0\r".toString().getBytes());
                consoleStatus = 0;
                btnConsole.setBackgroundColor(0xFFFF0000);
                btnConsole.setText("Quadro OFF");
                btnStart.setEnabled(false);
            }
            catch (IOException e){msg("Error");}
        }
    }
    private void turnOnConsole(){
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write("CONSOLE\t1\r".toString().getBytes());
                consoleStatus = 1;
                btnConsole.setBackgroundColor(0xFF00FF00);
                btnConsole.setText("Quadro ON");
                btnStart.setEnabled(true);
            }
            catch (IOException e) {msg("Error");}
        }
    }
    private void motorStart(){
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write("MOTOR\t1\r".toString().getBytes());
                btnStart.setBackgroundColor(0xFF00FF00);
                statusText.setText("Accensione ...");
            }
            catch (IOException e) {msg("Error");}
        }
    }

    private void motorStop(){
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write("MOTOR\t0\r".toString().getBytes());
                btnStart.setBackgroundColor(0xFFCCCCCC);
                statusText.setText("...");
            }
            catch (IOException e) {msg("Error");}
        }
    }






    // fast way to call Toast
    private void msg(String s){
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
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
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
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
                        mmInStream.read(buffer);
                    }
                    // Send the obtained bytes to the UI activity

                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}
