package com.example.hew_2.lightdomo;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Timer;

import static com.example.hew_2.lightdomo.R.layout.control_frame;


public class BleActivity extends Activity implements DeviceListFragment.OnDeviceListFragmentInteractionListener {
    public static final String TAG = "BluetoothLE";
    private final int ENABLE_BT = 1;

    private final Messenger mMessenger;
    private Intent mServiceIntent;
    //与BLEService里的mService重名，改掉
    private Messenger mService = null;
    private BleService.State mState = BleService.State.UNKNOWN;
    //private MenuItem mRefreshItem = null;

    private DeviceListFragment mDeviceList = DeviceListFragment.newInstance();
    private ColorLight mControl = ColorLight.newInstance();
    //private WhiteLight mControl = WhiteLight.newInstance();
    private String[] addresses;
    private static boolean isReconnected = false;


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, BleService.MSG_REGISTER);
                if (msg != null) {
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } else {
                    mService = null;
                }
                startScan();

            } catch (Exception e) {
                Log.w(TAG, "Error connecting to BleService", e);
                mService = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    public BleActivity() {
        super();
        mMessenger = new Messenger(new IncomingHandler(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        mServiceIntent = new Intent(this, BleService.class);
        FragmentTransaction tx = getFragmentManager().beginTransaction();
        tx.add(R.id.main_content, mDeviceList);
        tx.commit();

    }

    @Override
    protected void onStop() {
        isReconnected = true;

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(mServiceIntent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //mRefreshItem = menu.findItem(R.id.action_refresh);
        //mDeviceListFragment = DeviceListFragment.newInstance(null);
        return true;
    }
/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
       /* int id = item.getItemId();
        if (id == R.id.action_refresh) {
            if (mService != null) {
                startScan();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
    public void startScan() {
        //mRefreshItem.setEnabled(false);
        mDeviceList.setDevices(this, null);
        mDeviceList.setScanning(true);
        Message msg = Message.obtain(null, BleService.MSG_START_SCAN);
        if (msg != null) {
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Lost connection to service", e);
                unbindService(mConnection);
            }
        }
    }
/*
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mRefreshItem != null) {
            mRefreshItem.setEnabled(mState == BleService.State.IDLE || mState == BleService.State.UNKNOWN);
        }
        return super.onPrepareOptionsMenu(menu);
    }
*/
    @Override
    public void onDeviceListFragmentInteraction(String macAddress) {
        Message msg = Message.obtain(null, BleService.MSG_DEVICE_CONNECT);
        if (msg != null) {
            msg.obj = macAddress;
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Lost connection to service", e);
                unbindService(mConnection);
            }
        }
    }

    private static class IncomingHandler extends Handler {
        private final WeakReference<BleActivity> mActivity;

        public IncomingHandler(BleActivity activity) {
            mActivity = new WeakReference<BleActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BleActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case BleService.MSG_STATE_CHANGED:
                        activity.stateChanged(BleService.State.values()[msg.arg1]);
                        break;
                    case BleService.MSG_DEVICE_FOUND:
                        Bundle data = msg.getData();
                        if (data != null && data.containsKey(BleService.KEY_MAC_ADDRESSES)) {
                            activity.mDeviceList.setDevices(activity, data.getStringArray(BleService.KEY_MAC_ADDRESSES));
                          }
                        break;
                     /*
                    case BleService.MSG_DEVICE_HUMIDITY_DATA:
                        float temperature = msg.arg1 / 100f;
                        float humidity = msg.arg2 / 100f;
                        activity.mDisplay.setData(temperature, humidity);
                        break;
                    case BleService.MSG_DEVICE_LUXOMETER_DATA:
                        float luxometer = msg.arg1;
                        activity.mDisplay.setLuxometerData(luxometer);
                        break;
                    case BleService.MSG_DEVICE_LIGHTCONTROL_STATUS:
                        activity.mDisplay.setControlStatus();
                        break;
                        */
                }
            }
            super.handleMessage(msg);
        }
    }

    private void stateChanged(BleService.State newState) {
        boolean disconnected = mState == BleService.State.CONNECTED;
        mState = newState;
        switch (mState) {
            case SCANNING:
                //mRefreshItem.setEnabled(true);
                mDeviceList.setScanning(true);
                break;
            case BLUETOOTH_OFF:
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, ENABLE_BT);
                break;
            case IDLE:
                if (disconnected) {
                    FragmentTransaction tx = getFragmentManager().beginTransaction();
                    tx.replace(R.id.main_content, mDeviceList);
                    tx.commit();
                }
              //  mRefreshItem.setEnabled(true);
                mDeviceList.setScanning(false);
                break;
            case CONNECTED:
                FragmentTransaction tx = getFragmentManager().beginTransaction();
                Control_frame control_frame=new Control_frame();
                tx.replace(R.id.main_content,control_frame);
                tx.commit();
                //IntentFilter intentFilter=new IntentFilter();
                //intentFilter.addAction(ColorLight.ACTION_LAMP_RGB_COLOR_CHANGED);
                //registerReceiver(lightControlReceiver,intentFilter);
                break;
            /*
            case LIGHTCONTROL:
                FragmentTransaction ty = getFragmentManager().beginTransaction();
                ty.replace(R.id.main_content, mDisplay);
                ty.commit();
                break;*/
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startScan();
            } else {
                //The user has elected not to turn on
                //Bluetooth. There's nothing we can do
                //without it, so let's finish().
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mService != null &isReconnected ==true) {
            Message msg = Message.obtain(null, BleService.MSG_DEVICE_RECONNECT);
            try {
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                Log.w(TAG, "Lost connection to service", e);
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            try {
                Message msg = Message.obtain(null, BleService.MSG_UNREGISTER);
                if (msg != null) {
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error unregistering with BleService", e);
                mService = null;
            } finally {
                unbindService(mConnection);
            }
        }
    }

    @Override
    protected  void onPause(){
        super.onPause();
    }

}
