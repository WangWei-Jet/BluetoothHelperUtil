package com.oneway.bluetooth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class BluetoothHelperImpl extends BluetoothHelper {

	private BroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;

	private Context context;

	private int bondOperFlag = 0;
	private final int BONDING_VALUE = 22;
	private boolean bondFlag;

	private String tag = BluetoothHelperImpl.class.getSimpleName();

	public BluetoothHelperImpl(Context context) {
		super();
		this.context = context;
	}

	@Override
	public boolean autoParing(final BluetoothDevice device) {
		if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
			return true;
		} else {
			registerBroadcastReceiver(context);
			new Thread() {
				public void run() {
					try {
						Method method = device.getClass().getMethod(
								"createBond");
						method.invoke(device);
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}.run();
			while (bondOperFlag <= BONDING_VALUE) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			unregisterBroadcastReceiver();
			return bondFlag;
		}
	}

	private void registerBroadcastReceiver(Context context) {
		intentFilter = new IntentFilter();
		intentFilter
				.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device == null) {
					return;
				}
				if ("android.bluetooth.device.action.PAIRING_REQUEST"
						.equals(action)) {
					showLog("receiver bt pairing request");
					// int paringKey = intent.getIntExtra(
					// "android.bluetooth.device.extra.PAIRING_KEY", -11);
					// Log.d(tag, "paringkey:" + paringKey);
					// try {
					// Method method = device.getClass().getMethod(
					// "setPairingConfirmation",
					// new Class[] { boolean.class });
					// method.invoke(device, new Object[] { true });
					// } catch (NoSuchMethodException e) {
					// e.printStackTrace();
					// } catch (IllegalAccessException e) {
					// e.printStackTrace();
					// } catch (IllegalArgumentException e) {
					// e.printStackTrace();
					// } catch (InvocationTargetException e) {
					// e.printStackTrace();
					// }
					try {
						BluetoothUtilFactory.invokeReflection(device,
								"setPairingConfirmation",
								new Class[] { boolean.class },
								new Object[] { true });
					} catch (Exception e) {
						showLog(e.getMessage());
					}
					// device.setPairingConfirmation(true);
					if (abortBroadcast) {
						try {
							abortBroadcast();
						} catch (Exception e) {
							showLog("request pairing broadcast is not orded in current android version");
						}
					}
					int temp = intent.getIntExtra(
							"android.bluetooth.device.extra.PAIRING_VARIANT",
							-11);
					Log.d(tag, "pairing type:" + temp);
					if (temp == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION) {
						showLog("PAIRING_VARIANT_PASSKEY_CONFIRMATION");
						// device.setPairingConfirmation(true);
						// try {
						// Method method = device.getClass().getMethod(
						// "setPairingConfirmation",
						// new Class[] { boolean.class });
						// method.invoke(device, new Object[] { true });
						// } catch (Exception e) {
						// e.printStackTrace();
						// }
					} else if (temp == BluetoothDevice.PAIRING_VARIANT_PIN) {
						showLog("PAIRING_VARIANT_PIN");
					} else if (temp == -11) {
						showLog("failed getting pairing type");
					} else {
						showLog("unhandled pairing type");
					}
				} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED
						.equals(action)) {
					showLog("bt bond state change");
					int bondState = intent.getIntExtra(
							BluetoothDevice.EXTRA_BOND_STATE, -11);
					if (BluetoothDevice.BOND_BONDED == bondState) {
						showLog("BOND_BONDED");
						bondOperFlag++;
						bondFlag = true;
					} else if (bondState == BluetoothDevice.BOND_BONDING) {
						showLog("BOND_BONDING");
						bondOperFlag = BONDING_VALUE;
						bondFlag = false;
					} else if (bondState == BluetoothDevice.BOND_NONE) {
						showLog("BOND_NONE");
						bondOperFlag++;
						bondFlag = false;
					} else {
						showLog("failed getting changed bond state");
					}
				}
			}
		};
		context.registerReceiver(broadcastReceiver, intentFilter);
	}

	private void unregisterBroadcastReceiver() {
		if (broadcastReceiver != null) {
			context.unregisterReceiver(broadcastReceiver);
		}
	}

	private void showLog(String logInfo) {
		if (DEBUG) {
			Log.d(tag, logInfo);
		}
	}

}
