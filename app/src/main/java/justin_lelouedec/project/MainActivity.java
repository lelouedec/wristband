package justin_lelouedec.project;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainActivity extends AppCompatActivity implements BleManager.BleManagerListener,TextToSpeech.OnInitListener {

    LinearLayout ln;

    Button connectB;
    Button disconnectB;
    Button notif;
    Button test;
    Button battery;
    BluetoothAdapter mBluetoothAdapter;
    BleManager mBleManager;

    // Service Constants
    public static final String UUID_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_RX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_TX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_DFU = "00001530-1212-EFDE-1523-785FEABCD123";
    public static final int kTxMaxCharacters = 20;
    protected BluetoothGattService mUartService;

    TextToSpeech tts;



    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(getBaseContext(),this) ;

        int result = tts.setLanguage(Locale.ENGLISH);
        if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("TTS", "This Language is not supported");
        }

        mBleManager = BleManager.getInstance(this);
        mBleManager.setBleListener(this);

        ln = (LinearLayout) findViewById(R.id.notifs);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);//inizialyse the bluetooth adapter
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {//start bluetooth if not started yet
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }



        connectB = (Button)findViewById(R.id.button3);
        disconnectB = (Button)findViewById(R.id.button5);
        notif = (Button)findViewById(R.id.button6);
        battery = (Button)findViewById(R.id.button8);



        test = (Button)findViewById(R.id.button7);

        final BluetoothDevice device =mBluetoothAdapter.getRemoteDevice("F4:A5:0B:8C:8B:41");


        connectB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isConnecting = mBleManager.connect(getApplicationContext(),device.getAddress());
                if (isConnecting) {
                    Log.i("Connected","Succesfully Connected");

                }

            }
        });
        battery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUartService!=null){
                    sendData("batterie");
                }
                }
        });

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendData("Test envoie"+0);


            }
        });

        disconnectB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleManager.disconnect();
            }
        });
        notif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ln.removeAllViews();
            }
        });

    }



    // region Send Data to UART
    protected void sendData(String text) {
        final byte[] value = text.getBytes(Charset.forName("UTF-8"));
        sendData(value);
    }


    protected void sendData(byte[] data) {
        if (mUartService != null) {
            // Split the value into chunks (UART service has a maximum number of characters that can be written )
            for (int i = 0; i < data.length; i += kTxMaxCharacters) {
                final byte[] chunk = Arrays.copyOfRange(data, i, Math.min(i + kTxMaxCharacters, data.length));
                mBleManager.writeService(mUartService, UUID_TX, chunk);
            }
        } else {
            Log.w("data sent : ", "Uart Service not discovered. Unable to send data");
        }
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.compas:
                Intent intent = new Intent(getApplicationContext(),Compas.class);
                startActivity( intent);
                return true;
            case R.id.action_settings:
                Toast toast = Toast.makeText(getApplicationContext(), "settings", Toast.LENGTH_LONG);
                toast.show();
                return true;
            case R.id.TTS:
                Intent intent2 = new Intent(getApplicationContext(),TTS.class);
                startActivity(intent2);
                return true;
            case R.id.Location:
                Intent intent3 = new Intent(getApplicationContext(),Location.class);
                startActivity(intent3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private BroadcastReceiver onNotice= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {



            String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");


            TextView textview = new TextView(getApplicationContext());
            textview.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            textview.setTextSize(20);
            textview.setTextColor(Color.parseColor("#0B0719"));
            textview.setText(Html.fromHtml(pack+","+title + " : </b>" + text));

            ln.addView(textview);

            String valuetosend= pack+","+title+"\n"+text+0;
        if(mUartService!=null){
            if(findViewById(R.id.switch1).isActivated() && pack=="msg"){
                sendData(valuetosend);
            }
            if(findViewById(R.id.switch2).isActivated() && pack=="call"){
                sendData(valuetosend);
            }
             if(!findViewById(R.id.switch1).isActivated() && !findViewById(R.id.switch2).isActivated()){
                sendData(valuetosend);
            }



        }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBleManager.disconnect();
    }

    // region BleManagerListener
    @Override
    public void onConnected() {

        sendData("Hello device you are connected to me :)"+0);

        //tts.speak("Connected to the device", TextToSpeech.QUEUE_FLUSH, null);


    }

    @Override
    public void onConnecting() {

    }

    @Override
    public void onDisconnected() {
        Log.d("Disconnected", "Disconnected");
        mBleManager.disconnect();
        //tts.speak("Disconnected  from the device", TextToSpeech.QUEUE_FLUSH, null);

    }

    @Override
    public void onServicesDiscovered() {
        Log.i("notification", "finding service");
        mUartService = mBleManager.getGattService(UUID_SERVICE);
        mBleManager.enableNotification(mUartService, UUID_RX, true);
        if(mUartService!=null)
            Log.i("notification", "enable");
    }



    @Override
    public synchronized void onDataAvailable(BluetoothGattCharacteristic characteristic) {
        // UART RX
        if (characteristic.getService().getUuid().toString().equalsIgnoreCase(UUID_SERVICE)) {
            if (characteristic.getUuid().toString().equalsIgnoreCase(UUID_RX)) {
                final byte[] bytes = characteristic.getValue();
                final String data = new String(bytes, Charset.forName("UTF-8"));


                TextView textview = new TextView(getApplicationContext());
                textview.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
                textview.setTextSize(20);
                textview.setTextColor(Color.parseColor("#0B0719"));
                textview.setText(Html.fromHtml("received:  " + data));

                ln.addView(textview);
                Log.i("received:",data);



            }
        }
    }

    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor) {

    }

    @Override
    public void onReadRemoteRssi(int rssi) {

    }

    @Override
    public void onInit(int i) {

    }
    // endregion




}
