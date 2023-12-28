package com.example.othertemplateapp.wifi;

import android.content.Context;
import android.util.Log;


import com.example.othertemplateapp.SignConnection;
import com.example.othertemplateapp.stream.ByteInputStream;
import com.example.othertemplateapp.threadpool.DispatchTask;
import com.example.othertemplateapp.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class WifiServerSocketConnection extends SignConnection {
    private static final String TAG = WifiServerSocketConnection.class.getSimpleName();
    private Context mContext;

    private Map<String,Integer> mListenPorts = new HashMap<>();

    private Selector mSelector;

    private Selector mReadSelector;

    private ByteInputStream mInputStream;

    private AtomicBoolean isConnect = new AtomicBoolean(false);

    private LinkedBlockingQueue<byte[]> outputQueue = new LinkedBlockingQueue<>(10);

    private ByteBuffer mBuffer = ByteBuffer.allocate(5210);

    private ByteBuffer mWriteBuffer = ByteBuffer.allocate(5210);


    private Set<SocketChannel> socketChannelSet = new HashSet<>();

    private Set<ServerSocketChannel> serverSocketChannelSet = new HashSet<>();

    private OutputStream mOutputStream = new OutputStream() {
        @Override
        public void write(int b) throws IOException {

        }

        @Override
        public void write(byte[] b) throws IOException {
            super.write(b);
            postValue(b);
        }
    };


    @Override
    public void init(Context context) {
        super.init(context);
        mContext = context;
        mInputStream = new ByteInputStream();
        outputQueue.clear();
        mListenPorts.put("10000",10000);
        mListenPorts.put("10001",10001);
        mListenPorts.put("10002",10002);
    }
    @Override
    public OutputStream getOutputStream() {
        return mOutputStream;
    }

    @Override
    public InputStream getInputStream() {
        return mInputStream;
    }

    @Override
    protected boolean startConnect() {
        if(isConnect.get()) {
            return true;
        }
        isConnect.set(true);
        Log.e(TAG, "startConnect()");
        closeConnection();
        DispatchTask.threadSleep(50);
        DispatchTask.doSomeBackgroundWork(new Runnable() {
            @Override
            public void run() {
                try {
                    mSelector = Selector.open();
                    Log.d(TAG, "startConnect: selector is open: " + mSelector.isOpen());
                    mReadSelector = Selector.open();
                    mInputStream.open();
                    if(mListenPorts.isEmpty()) {
                        Log.e(TAG, "startConnect: 监听端口列表为空");
                    }
                    for (Integer port : mListenPorts.values()) {
                        addListenPort(port);
                    }
                    int selectRet;
                    startListen();
                    while (true) {
                        if(!isConnect.get()) {
                            Log.e(TAG, "startConnect select is connect false");
                            break;
                        }
                        try {
                            selectRet = mSelector.select();
                            Log.d(TAG, "select ret: " + selectRet);
                            if(selectRet <= 0) {
                                Log.e(TAG, "startConnect: 监听端口结果数量小于0");
                                continue;
                            }
                            Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator();
                            while (iterator.hasNext()) {
                                SelectionKey key = iterator.next();
                                if(key.isAcceptable()) {
                                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                                    SocketChannel channel = server.accept();
                                    socketChannelSet.add(channel);
                                    channel.configureBlocking(false);
                                    Log.d(TAG, "startConnect: connect success: " + channel);
                                    channel.register(mReadSelector,
                                            SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                                }
                                iterator.remove();

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                    }



                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        if(mStateListener != null) {
            mStateListener.onConnected();
        }
        return true;


    }

    private void startListen() {
        Log.e(TAG, "startListen()");
        DispatchTask.doSomeBackgroundWork(new Runnable() {
            @Override
            public void run() {
                int selectRet;
                while (true) {
                    if(!isConnect.get()) {
                        Log.e(TAG, "startListen select is connect false");
                        break;
                    }
                    try {
                        selectRet = mReadSelector.selectNow();

//                        if(selectRet <= 0) {
//                            DispatchTask.threadSleep(50);
//                            continue;
//                        }
                        byte[] writeData = outputQueue.poll();
                        Iterator<SelectionKey> iterator = mReadSelector.selectedKeys().iterator();
                        while (iterator.hasNext()) {
                            SelectionKey key = iterator.next();
                            SocketChannel channel = (SocketChannel) key.channel();
                            socketChannelSet.add(channel);
                            if(key.isReadable()) {
                                Log.d(TAG, "readable");
                                int count = 0;
                                try {
                                    count = channel.read(mBuffer);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "socket close: " + channel);
                                    channel.close();
                                    iterator.remove();
                                    continue;
                                }
                                if(count < 0) {
                                    Log.e(TAG, "socket close: " + channel);
                                    channel.close();
                                    iterator.remove();
                                    continue;
                                }
                                byte[] data = new byte[count];
                                mBuffer.flip();
                                mBuffer.get(data);
                                mInputStream.addData(Util.hexString2Bytes(new String(data)));
                                mBuffer.clear();
                                Log.d(TAG, "wifi socket read data : " + new String(data));
//                                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);

                            }
                            if(key.isWritable()) {
                                if(writeData != null) {
                                    int port = getSendPort(writeData);
                                    if(port == channel.socket().getLocalPort()) {
                                        mWriteBuffer.put(writeData);
                                        mWriteBuffer.flip();
                                        while (mWriteBuffer.hasRemaining()) {
                                            int writeRet = channel.write(mWriteBuffer);
                                            Log.d(TAG, "writeRet: " + writeRet);
                                        }
                                        mWriteBuffer.clear();
                                    }
                                }

                            }
                            iterator.remove();
                        }
                    } catch (IOException | ClosedSelectorException e) {
                        e.printStackTrace();
                        break;
                    }
                    DispatchTask.threadSleep(10);
                }
            }
        });
    }

    private int getSendPort(byte[] sendData) {
        //TODO
        return 10000;
    }

    private void postValue(byte[] postData) {
        try {
            outputQueue.add(Util.byte2HexStr(postData).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "postValue failed");
        }

    }


    @Override
    public void stopConnect() {
        Log.e(TAG, "stopConnect()");
        super.stopConnect();
        isConnect.set(false);
        closeConnection();
    }

    private void closeConnection() {
        try {

            for (SocketChannel channel : socketChannelSet) {
                channel.close();
            }
            socketChannelSet.clear();

            for (ServerSocketChannel serverSocketChannel : serverSocketChannelSet) {
                serverSocketChannel.close();
            }
            serverSocketChannelSet.clear();
            if(mSelector != null) {
                mSelector.close();
            }
            if(mReadSelector != null) {
                mReadSelector.close();
            }
            if(mInputStream != null) {
                mInputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setStateListener(StateListener listener) {
        super.setStateListener(listener);
    }

    @Override
    public void disConnect() {
        Log.e(TAG, "disConnect()");
        super.disConnect();
        isConnect.set(false);
        closeConnection();
    }

    private void addListenPort(int port) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);
        serverSocketChannelSet.add(serverSocketChannel);
    }

    public void setListenPorts(Map<String,Integer> listenPorts) {
        mListenPorts.putAll(listenPorts);
    }
}
