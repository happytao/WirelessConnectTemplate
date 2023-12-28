package com.example.othertemplateapp.classic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.example.othertemplateapp.SignConnection;
import com.example.othertemplateapp.threadpool.DispatchTask;
import com.sunyard.commonlib.threadpool.DispatchTask;
import com.sunyard.vi218_a_standardapp_bld.connectcontroller.BluetoothServer;
import com.sunyard.vi218_a_standardapp_bld.connectcontroller.SignConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class BluetoothConnection extends SignConnection {
    private static final String TAG = BluetoothConnection.class.getSimpleName();
    private static final UUID CUSTOM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//    private static final UUID CUSTOM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private volatile BluetoothSocket socket;
    private BluetoothServerSocket mSSocket;
    private BlueToothReceiver mReceiver;
    private final AtomicBoolean mConnectFlag = new AtomicBoolean(true);

    @Override
    public void init(Context context) {
        if (mReceiver == null) {
            mReceiver = new BlueToothReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
            intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
            intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");
            intentFilter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
            context.registerReceiver(mReceiver, intentFilter);
        }
    }

    @Override
    public boolean startConnect() {
        setDiscoverableTimeout(Integer.MAX_VALUE);
        mConnectFlag.set(true);

        while (mConnectFlag.get()) {
            try {
                updateServiceSocket();
                socket = mSSocket.accept(); // 监听经典蓝牙连接
            } catch (IOException e) {
                if (!"Try again".equals(e.getMessage())) {
                    Log.e(TAG, "经典蓝牙连接异常: ", e);

                    if (!mConnectFlag.get()) {
                        return false;
                    }
                }
            }

            if (socket != null && socket.isConnected()) {
                if (mStateListener != null)
                    mStateListener.onConnected();
                break;
            }
            DispatchTask.threadSleep(50);
        }

        return mConnectFlag.get();
    }

    private void updateServiceSocket() throws IOException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//        if (!adapter.isEnabled())
//            adapter.enable();
        mSSocket = adapter.listenUsingRfcommWithServiceRecord(TAG, CUSTOM_UUID);
    }

    @Override
    public void stopConnect() {
        super.stopConnect();
        closeBluetoothDiscoverable();
        closeConnect();
    }

    @Override
    public void disConnect() {
        super.disConnect();
        closeConnect();
    }

    private void closeConnect() {
        mConnectFlag.set(false);
        try {
            if (socket != null) {
                socket.close();
            }
            if (mSSocket != null) {
                mSSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public OutputStream getOutputStream() {
        try {
            return socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param timeout 实际无效,设置后为永久可见
     */
    public static void setDiscoverableTimeout(int timeout) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", Duration.class);
                setDiscoverableTimeout.setAccessible(true);
                Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class);
                setScanMode.setAccessible(true);
                setDiscoverableTimeout.invoke(adapter, Duration.ofSeconds(timeout));
                setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
            }else{
                Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
                setDiscoverableTimeout.setAccessible(true);
                Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
                setScanMode.setAccessible(true);
                setDiscoverableTimeout.invoke(adapter, timeout);
                setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, timeout);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void closeBluetoothDiscoverable() {
        //尝试关闭蓝牙可见性
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", Duration.class);
                setDiscoverableTimeout.setAccessible(true);
                Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class);
                setScanMode.setAccessible(true);

                setDiscoverableTimeout.invoke(adapter, Duration.ofSeconds(1));
                setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE);
            }else{
                Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
                setDiscoverableTimeout.setAccessible(true);
                Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
                setScanMode.setAccessible(true);

                setDiscoverableTimeout.invoke(adapter, 1);
                setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class BlueToothReceiver extends BroadcastReceiver {
        // 监听蓝牙状态
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_ON:
                            if (mStateListener != null) {
                                mStateListener.onStateOn();
                            }
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            if (mStateListener != null) {
                                mStateListener.onStateOff();
                            }
                            break;
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                case BluetoothDevice.ACTION_FOUND:
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                case "android.bluetooth.device.action.PAIRING_REQUEST":
            }
        }
    }
}
