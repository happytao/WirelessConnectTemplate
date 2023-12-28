package com.example.othertemplateapp;

import android.content.Context;

public abstract class SignConnection implements IConnection {
    protected StateListener mStateListener;

    @Override
    public void init(Context context) {}

    @Override
    public boolean listenConnect() {
        return startConnect();
    }

    protected abstract boolean startConnect();

    @Override
    public void stopConnect() {

    }

    @Override
    public void setStateListener(StateListener listener) {
        mStateListener = listener;
    }

    @Override
    public void disConnect() {
        if (mStateListener != null)
            mStateListener.onDisconnect();
    }
}
