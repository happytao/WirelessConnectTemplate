package com.example.othertemplateapp.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;


import com.example.othertemplateapp.SignConnection;
import com.example.othertemplateapp.stream.ByteInputStream;
import com.example.othertemplateapp.threadpool.DispatchTask;
import com.example.othertemplateapp.util.UUIDUtil;
import com.example.othertemplateapp.util.Util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

public class BleConnection extends SignConnection {
    private static final String TAG = "BLEServer";

    private BluetoothLeAdvertiser mBleAdvertiser;
    private AdvertiseCallback mAdsCallback;
    private BluetoothGattServer mBtGattServer;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothDevice conDevice;
    private Context mContext;

    private final ByteInputStream mInputStream = new ByteInputStream();
    private static int mtuSize = 20;
    private final Semaphore mSemaphore = new Semaphore(0);
    private AdvertiseSettings mAdvertiseSettings;
    private AdvertiseData mAdvertiseData;
    private AdvertiseData mScanResponse;

    @Override
    public void init(Context context) {
        mContext = context;

        if (mBleAdvertiser == null) {
            mBleAdvertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        }

        if (mAdvertiseSettings == null) {
            mAdvertiseSettings = new AdvertiseSettings.Builder()
                    .setConnectable(true)
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                    .setTimeout(0)
                    .build();
        }

        if (mAdvertiseData == null) {
            mAdvertiseData = new AdvertiseData.Builder()
                    .setIncludeTxPowerLevel(true)
                    .setIncludeDeviceName(false)
                    .addServiceUuid(new ParcelUuid(UUIDUtil.UUID_SERVICE)) //服务UUID
                    .build();
        }

        if (mScanResponse == null) {
            mScanResponse = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .build();
        }

        if (mAdsCallback == null) {
            mAdsCallback = new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    Log.i(TAG, "!!! startAdvertising is succeed ");
                    initBleService();
                }

                @Override
                public void onStartFailure(int errorCode) {
                    Log.i(TAG, "!!! startAdvertising is failed. with code: " + errorCode);
                }
            };
        }
    }

    @Override
    protected boolean startConnect() {
        if (mBleAdvertiser == null) {
            return false;
        }

        mBleAdvertiser.startAdvertising(mAdvertiseSettings, mAdvertiseData, mScanResponse, mAdsCallback);

        try {
            mSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return conDevice != null;
    }

    @Override
    public void stopConnect() {
        super.stopConnect();
        if (mBleAdvertiser != null) {
            mBleAdvertiser.stopAdvertising(mAdsCallback);
        }

        if (mSemaphore.getQueueLength() != 0) {
            mSemaphore.release();
        }
    }

    @Override
    public void disConnect() {
        super.disConnect();
        if (mBleAdvertiser != null) {
            mBleAdvertiser.stopAdvertising(mAdsCallback);
        }
    }

    @Override
    public OutputStream getOutputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) {
                write(new byte[]{(byte) b});
            }

            @Override
            public void write(byte[] b, int off, int len) {
                sendDataToCentralDevice(Util.arrayCopy(b, off, len));
            }

            @Override
            public void write(byte[] b) {
                sendDataToCentralDevice(b);
            }
        };
    }

    @Override
    public InputStream getInputStream() {
        return mInputStream;
    }

    @SuppressLint("MissingPermission")
    private void initBleService() {
        if (mBtGattServer == null) {
            BluetoothManager btManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            mBtGattServer = btManager.openGattServer(mContext, mBtGattServerCallback);
        }
        if (mBtGattServer == null) {
            Log.d(TAG, "!!! openGattServer is failed");
            return;
        }

        BluetoothGattService gattService = new BluetoothGattService(UUIDUtil.UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic writableCharacteristic = new BluetoothGattCharacteristic(
                UUIDUtil.UUID_CHAR_WRITE,
                BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        gattService.addCharacteristic(writableCharacteristic);

        mNotifyCharacteristic = new BluetoothGattCharacteristic(
                UUIDUtil.UUID_DESC_NOTITY,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothGattDescriptor gD = new BluetoothGattDescriptor(UUIDUtil.UUID_NOTIFY_DESCRIPTOR,
                BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ);
        mNotifyCharacteristic.addDescriptor(gD);

        gattService.addCharacteristic(mNotifyCharacteristic);

        mBtGattServer.clearServices();
        mBtGattServer.addService(gattService);
    }

    private final BluetoothGattServerCallback mBtGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
//                    device.connectGatt()
                    conDevice = device;
                    mBleAdvertiser.stopAdvertising(mAdsCallback);
                    mSemaphore.release();
                    mInputStream.open();
                    if (mStateListener != null)
                        mStateListener.onConnected();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.d(TAG, "蓝牙连接断开,status = " + status);
                    conDevice = null;
                    mInputStream.close();
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    Log.d(TAG, "蓝牙正在连接");
                    break;
            }
        }

        //接收数据
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            mBtGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);// 响应客户端
            Log.d(TAG, "客户端写入Characteristic[" + characteristic.getUuid() + "]:\n" + Util.bytesToHexString(value));
            mInputStream.addData(value);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            Log.d(TAG, "onNotificationSent:status = " + status);
            super.onNotificationSent(device, status);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicReadRequest");
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            conDevice = device;
            mBtGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            mtuSize = mtu - 3;
            super.onMtuChanged(device, mtu);
        }
    };

    private void sendDataToCentralDevice(byte[] data) {
        if (mNotifyCharacteristic == null) {
            return;
        }
        int alreadsendlen = 0;
        int sendDataLen;
        if (conDevice != null) {
            long beforeSendTime = System.currentTimeMillis();
            while (alreadsendlen < data.length) {
                sendDataLen = Math.min(data.length - alreadsendlen, mtuSize);
                mNotifyCharacteristic.setValue(Util.arrayCopy(data, alreadsendlen, sendDataLen));
                boolean succeed = mBtGattServer.notifyCharacteristicChanged(conDevice, mNotifyCharacteristic, false);
                DispatchTask.threadSleep(10);
                if (succeed) {
                    Log.e(TAG, "Ble send data = " + Util.bytesToHexString(Util.arrayCopy(data, alreadsendlen, sendDataLen))
                            + "\n发送耗时时间: " + (System.currentTimeMillis() - beforeSendTime));
                } else {
                    Log.e(TAG, "Ble send failed");
                }
                alreadsendlen += sendDataLen;
            }
        }
    }
}
