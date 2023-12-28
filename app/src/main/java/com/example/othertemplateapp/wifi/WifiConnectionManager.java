package com.example.othertemplateapp.wifi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.example.othertemplateapp.threadpool.DispatchTask;
import com.example.othertemplateapp.util.GsonUtil;
import com.example.othertemplateapp.util.MMKVUtil;
import com.google.gson.reflect.TypeToken;


import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiConnectionManager implements LifecycleObserver {
    private static final String TAG = WifiConnectionManager.class.getSimpleName();
    private static WifiConnectionManager instance;

    private Context mContext;
    private WifiManager mWifiManager;
    private WifiClientReceiver mWifiClientReceiver;
    private ScanCallback mScanCallback;

    private StateChangeCallback mStateChangeCallback;

    private Map<String,String> mSavedWifiMap = new HashMap<>();

    private NetworkInfo.State lastState;


    public static WifiConnectionManager getInstance(Context context) {
        if(instance == null) {
            synchronized (WifiConnectionManager.class) {
                if(instance == null) {
                    instance = new WifiConnectionManager(context);
                }
            }
        }
        return instance;
    }

    private WifiConnectionManager(Context context) {
        mContext = context;
        if (context instanceof LifecycleOwner) {
            ((LifecycleOwner) context).getLifecycle().addObserver(this);
        }
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        register(context);
        String json = MMKVUtil.getInstance().getString("savedWifiList");
        Map<String, String> map = GsonUtil.getInstance().getGson().fromJson(json , new TypeToken<Map<String, String>>(){}.getType());
        if(map != null) {
            mSavedWifiMap.putAll(map);
        }

    }

    @SuppressLint("InlinedApi")
    private void register(Context context) {
        mWifiClientReceiver = new WifiClientReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        context.registerReceiver(mWifiClientReceiver, intentFilter);
    }

    public boolean startScan(ScanCallback scanCallback) {
        mScanCallback = scanCallback;
        return mWifiManager.startScan();
    }

    public void setStateChangeCallback(StateChangeCallback stateChangeCallback) {
        mStateChangeCallback = stateChangeCallback;
    }


    @SuppressLint("NewApi")
    public void connectWifi(String ssid, String bssid, String password, boolean hidden,
                               String capabilities) {

         WifiNetworkSpecifier wifiNetworkSpecifier = new WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
//                 .setBssid(MacAddress.fromString(bssid))
                .build();
        //网络请求
        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                .setNetworkSpecifier(wifiNetworkSpecifier)
                .build();


        //网络回调处理
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                Log.d(TAG, "======onAvailable: ====连接成功======");
                mSavedWifiMap.put(bssid, password);
                MMKVUtil.getInstance().putString("savedWifiList",
                        GsonUtil.getInstance().toJson(mSavedWifiMap));

            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.d(TAG, "======onAvailable: ====连接失败======");
                mSavedWifiMap.remove(bssid);
                MMKVUtil.getInstance().putString("savedWifiList", GsonUtil.getInstance().toJson(mSavedWifiMap));

            }
        };

        //连接网络
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.bindProcessToNetwork(null);
        connectivityManager.requestNetwork(request, networkCallback);


//        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
//                .setSsid(ssid)
//                .setBssid(MacAddress.fromString(bssid))
//                .setWpa2Passphrase(password)
//                .build();
//
//        int status = mWifiManager.addNetworkSuggestions(Collections.singletonList(suggestion));
//        if(status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
//            Log.d(TAG, "connectWifi: suggest success");
//        } else {
//            Log.e(TAG, "connectWifi: suggest failed ret = " + status);
//        }

//
//        WifiConfiguration config = createWifiInfo(ssid, password, hidden, capabilities);
//        return connectWifi(config);


    }

    public boolean connectWifi(WifiConfiguration config) {
        int id = mWifiManager.addNetwork(config);
        WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
        mWifiManager.disableNetwork(connectionInfo.getNetworkId());
        boolean connectRet = mWifiManager.enableNetwork(id, true);

        if(connectRet) {
            Log.d(TAG, "connectWifi: wifi连接成功");
            return true;
        } else {
            Log.e(TAG, "connectWifi: wifi连接失败: " + config);
            mWifiManager.removeNetwork(config.networkId);
            return false;
        }
    }

    public void connectWifi(ScanResult scanResult, String password) {
        DispatchTask.doSomeBackgroundWork(new Runnable() {
            @Override
            public void run() {
                connectWifi(scanResult.SSID.replaceAll("\"", ""), scanResult.BSSID, password, false,
                        scanResult.capabilities);
            }
        });

    }

    public void disconnectWifi() {
        DispatchTask.doSomeBackgroundWork(new Runnable() {
            @Override
            public void run() {
                WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
                boolean disconnectRet = mWifiManager.disableNetwork(connectionInfo.getNetworkId());
                Log.d(TAG, "disconnectWifi: " + disconnectRet);
            }
        });

    }

    public int getWifiState() {
        return mWifiManager.getWifiState();
    }

    public boolean enableWifi() {
        return mWifiManager.setWifiEnabled(true);
    }

    public boolean disableWifi() {
        return mWifiManager.setWifiEnabled(false);
    }

    public WifiInfo getConnectedInfo() {
        return mWifiManager.getConnectionInfo();
    }

    @SuppressLint("MissingPermission")
    public List<WifiConfiguration> getConfiguredWifi() {
        return mWifiManager.getConfiguredNetworks();
    }

    public Map<String, String> getSavedWifiList() {
        return mSavedWifiMap;
    }


    /**
     * 创建Wifi配置
     *
     * @param SSID         wifi名称
     * @param password     wifi密码
     * @param hidden       网络是否隐藏（该方法与添加隐藏网络通用）
     * @param capabilities 网络安全协议
     * @return 配置好的wifi
     */
    private WifiConfiguration createWifiInfo(String SSID, String password, boolean hidden,
                                       String capabilities) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = "\"" + SSID + "\"";
        configuration.status = WifiConfiguration.Status.ENABLED;
        if (hidden) {
            configuration.hiddenSSID = true;
        }
        Log.d("WifiManagerUtils", "createWifiInfo: " + capabilities);
        if (capabilities.contains("SAE") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setWPA3(configuration, password);
        } else if (capabilities.contains("WPA-PSK") || capabilities.contains("WPA2-PSK")) {
            setWPA(configuration, password);
        } else if (capabilities.contains("WEP")) {
            setWEP(configuration, password);
        } else {
            setESS(configuration);
        }
        return configuration;
    }

    /**
     * 设置wpa3协议
     *
     * @param configuration 配置
     * @param password      密码
     */
    private void setWPA3(WifiConfiguration configuration, String password) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.SAE);
        }
        configuration.preSharedKey = "\"" + password + "\"";
    }

    /**
     * WPA协议
     *
     * @param configuration 配置
     * @param password      密码
     */
    private void setWPA(WifiConfiguration configuration, String password) {
        configuration.preSharedKey = "\"" + password + "\"";
        //公认的IEEE 802.11验证算法。
        configuration.allowedAuthAlgorithms.clear();
        configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        //公认的的公共组密码。
        configuration.allowedGroupCiphers.clear();
        configuration.allowedGroupCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        //公认的密钥管理方案。
        configuration.allowedKeyManagement.clear();
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        //密码为WPA。
        configuration.allowedPairwiseCiphers.clear();
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        //公认的安全协议。
        configuration.allowedProtocols.clear();
        configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
    }

    /**
     * WEP协议
     *
     * @param configuration 配置
     * @param password      密码
     */
    private void setWEP(WifiConfiguration configuration, String password) {
        configuration.wepKeys[0] = "\"" + password + "\"";
        configuration.wepTxKeyIndex = 0;
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
    }


    /**
     * 无密码
     *
     * @param configuration 配置
     */
    private void setESS(WifiConfiguration configuration) {
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
    }

    private class WifiClientReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION:
                    Log.d(TAG, "Wifi onReceive: 连接状态改变");
                    break;
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    boolean ret = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                    if(ret) {
                        Log.d(TAG, "Wifi onReceive: 扫描完成");
                        List<ScanResult> scanResults = mWifiManager.getScanResults();
                        scanResults.removeIf(scanResult -> StringUtils.isBlank(scanResult.SSID));
                        if(mScanCallback != null) {
                            mScanCallback.onScanFinish(scanResults);
                        }
                    }
                    break;
                case WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION:
                    Log.d(TAG, "Wifi onReceive: suggest received");
                    break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    Log.d(TAG, "Wifi onReceive: network info ==" + networkInfo);
                    NetworkInfo.State state = networkInfo.getState();
                    if(state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.DISCONNECTED) {
                        if(mStateChangeCallback != null) {
                            if(lastState == null || lastState != state) {
                                mStateChangeCallback.onStateChange(state);
                                lastState = state;
                            }

                        }
                    }
                    break;
            }


        }
    }

    public interface ScanCallback {
        void onScanFinish(List<ScanResult> scanResults);
    }

    public interface StateChangeCallback {
        void onStateChange(NetworkInfo.State state);
    }
}
