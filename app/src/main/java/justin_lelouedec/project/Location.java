package justin_lelouedec.project;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by 40214897 on 08/04/2016.
 */
public class Location extends Activity implements LocationListener,BleManager.BleManagerListener {

    Context mcontext;

    BluetoothAdapter mBluetoothAdapter;
    BleManager mBleManager;

    // Service Constants
    public static final String UUID_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_RX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_TX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_DFU = "00001530-1212-EFDE-1523-785FEABCD123";
    public static final int kTxMaxCharacters = 20;
    protected BluetoothGattService mUartService;
    LinearLayout ln;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location);



        mBleManager = BleManager.getInstance(this);
        mBleManager.setBleListener(this);


         ln = (LinearLayout) findViewById(R.id.position);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);//inizialyse the bluetooth adapter
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        final BluetoothDevice device =mBluetoothAdapter.getRemoteDevice("F4:A5:0B:8C:8B:41");

        boolean isConnecting = mBleManager.connect(getApplicationContext(), device.getAddress());
        if (isConnecting) {
            Log.i("Connected","Succesfully Connected");

        }

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
    public void onLocationChanged(android.location.Location location) {

        Toast.makeText(
                mcontext,
                "Location changed: Lat: " + location.getLatitude() + " Lng: "
                        + location.getLongitude(), Toast.LENGTH_SHORT).show();


        /*------- To get city name from coordinates -------- */
        String cityName = null;
        Geocoder gcd = new Geocoder(mcontext, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String s = location.getLongitude() + "\n" + location.getLatitude() + "\n\nMy Current City is: "
                + cityName;


        TextView textview = new TextView(getApplicationContext());
        textview.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        textview.setTextSize(20);
        textview.setTextColor(Color.parseColor("#0B0719"));
        textview.setText(Html.fromHtml("Position:  " + s));

        ln.addView(textview);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBleManager.disconnect();


    }

    // region BleManagerListener
    @Override
    public void onConnected() {

        sendData("Hello device you are connected to me :) ");



    }

    @Override
    public void onConnecting() {

    }

    @Override
    public void onDisconnected() {
        Log.d("Disconnected", "Disconnected");

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
    public void onDataAvailable(BluetoothGattCharacteristic characteristic) {

    }


    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor) {

    }



    @Override
    public void onReadRemoteRssi(int rssi) {

    }
}
