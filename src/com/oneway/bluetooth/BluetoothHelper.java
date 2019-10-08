package com.oneway.bluetooth;

import android.bluetooth.BluetoothDevice;

public abstract class BluetoothHelper {
	public boolean abortBroadcast = true;
	public boolean DEBUG = false;

	public abstract boolean autoParing(BluetoothDevice device);
}
