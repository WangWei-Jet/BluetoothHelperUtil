package com.oneway.bluetooth.ui;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.oneway.bluetooth.BluetoothUtilFactory;
import com.oneway.bluetoothhelperutil.R;

public class MainActivity extends Activity {

	private Button search;
	// private SearchLib lib;
	// private SearchDeviceListener listener;
	// private Handler mHandler;
	private TextView result;
	// private ProgressDialog mProgressDialog;
	private BroadcastReceiver btReceiver;
	private BroadcastReceiver testReceiver;
	private IntentFilter testFilter;
	private IntentFilter btFilter;

	private UUID uuid;
	private BluetoothSocket clientSocket = null;
	String tag = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		search = (Button) findViewById(R.id.search);
		result = (TextView) findViewById(R.id.textresult);
		// lib = new SearchLib(getApplicationContext());
		// mHandler = new MyHandler();
		// listener = new SearchDeviceListenerImpl(mHandler);
		// mProgressDialog = new ProgressDialog(MainActivity.this);
		// mProgressDialog.setCancelable(isFinishing());
		Log.d(tag, "new 11111111");
		uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		btReceiver = new MyBroadcastReceiver();
		testReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if ("android.bluetooth.device.action.PAIRING_REQUEST"
						.equals(action)) {
					Log.e(tag, "����filter���յ��������");
				}

			}
		};
		testFilter = new IntentFilter();
		testFilter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
		testFilter.setPriority(30);
		this.registerReceiver(testReceiver, testFilter);
		btFilter = new IntentFilter();
		btFilter.addAction(BluetoothDevice.ACTION_FOUND);
		btFilter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
		btFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		btFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		// ���ȼ����
		btFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		// this.registerReceiver(btReceiver, btFilter);
		search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				result.setText("");
				new Thread() {
					@Override
					public void run() {
						// lib.SearchDevice(listener);
						// ɨ��
						// BluetoothAdapter.getDefaultAdapter().startDiscovery();
						// BM77
						// device:TYHestia711_A1 mac:8C:DE:52:78:D9:AB
						// Toshiba
						// device:TY71249_7DABC9 mac:B8:32:41:40:01:23
						// �������
						BluetoothAdapter adapter = BluetoothAdapter
								.getDefaultAdapter();
						// BluetoothDevice device = adapter
						// .getRemoteDevice("8C:DE:52:78:D9:AB");
						BluetoothDevice device = adapter
								.getRemoteDevice("B8:32:41:40:01:23");
						if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
							Log.d(tag, "�����豸�󶨣�ֱ������");
							connectDevice(device);
						} else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
							Log.d(tag, "δ���豸��");
							// device.createBond();
							// ������÷���
							// try {
							// Method method = device.getClass().getMethod(
							// "createBond");
							// method.invoke(device);
							// Method cancelInput = device.getClass()
							// .getMethod("cancelPairingUserInput");
							// cancelInput.invoke(device);
							// } catch (NoSuchMethodException e) {
							// e.printStackTrace();
							// } catch (IllegalAccessException e) {
							// e.printStackTrace();
							// } catch (IllegalArgumentException e) {
							// e.printStackTrace();
							// } catch (InvocationTargetException e) {
							// e.printStackTrace();
							// }
							BluetoothUtilFactory.getBluetoothHelper(
									MainActivity.this).autoParing(device);
							connectDevice(device);
						}
					}
				}.start();

			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (btReceiver != null) {
			this.unregisterReceiver(btReceiver);
		}
		if (testReceiver != null) {
			this.unregisterReceiver(testReceiver);
		}
	}

	class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			BluetoothDevice device = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if ("android.bluetooth.device.action.PAIRING_REQUEST"
					.equals(action)) {
				// �յ��������
				Log.d(tag, "�յ��������");
				int paringKey = intent.getIntExtra(
						"android.bluetooth.device.extra.PAIRING_KEY", -11);
				Log.d(tag, "paringkey:" + paringKey);

				// ������÷���
				try {
					Method method = device.getClass().getMethod(
							"setPairingConfirmation",
							new Class[] { boolean.class });
					method.invoke(device, new Object[] { true });
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				// device.setPairingConfirmation(true);
				abortBroadcast();
				int temp = intent.getIntExtra(
						"android.bluetooth.device.extra.PAIRING_VARIANT", -11);
				Log.d(tag, "���ֵ:" + temp);
				if (temp == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION) {
					Log.d(tag, "����ȷ�Ϸ�ʽ���");
					// device.setPairingConfirmation(true);
					// ������÷���
					try {
						Method method = device.getClass().getMethod(
								"setPairingConfirmation",
								new Class[] { boolean.class });
						method.invoke(device, new Object[] { true });
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				} else if (temp == BluetoothDevice.PAIRING_VARIANT_PIN) {
					Log.d(tag, "�����뷽ʽȷ���䱸");
				} else {
					Log.d(tag, "��ȡ��Է�ʽʧ��");
				}
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// ���������豸
				String deviceName = device.getName();
				if (deviceName != null
						&& (deviceName.startsWith("TY71249") || deviceName
								.startsWith("TYHestia"))) {
					// BM77
					// device:TYHestia711_A1 mac:8C:DE:52:78:D9:AB
					// Toshiba
					// device:TY71249_7DABC9 mac:B8:32:41:40:01:23
					Log.d(tag, "device:" + device.getName() + "   mac:"
							+ device.getAddress());
				}
			} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				Log.d(tag, "�豸��״̬�ı�");
				int bondState = intent.getIntExtra(
						BluetoothDevice.EXTRA_BOND_STATE, -11);
				if (BluetoothDevice.BOND_BONDED == bondState) {
					Log.d(tag, "�豸�Ѱ�");
					result.setText("�豸�Ѱ�");
					connectDevice(device);
				} else if (bondState == BluetoothDevice.BOND_BONDING) {
					Log.d(tag, "���ڰ�");
					result.setText("���ڰ�");
				} else if (bondState == BluetoothDevice.BOND_NONE) {
					Log.d(tag, "�豸�����");
					result.setText("�豸�����");
				} else {
					Log.d(tag, "��ȡ��״̬ʧ��");
				}
			} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
				Log.d(tag, "�յ����ӹ㲥");
				Log.e(tag, "time4:" + SystemClock.elapsedRealtime());
			}

		}
	}

	private void connectDevice(BluetoothDevice device) {
		Log.d(tag, "׼�������豸");
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yy-MM-dd hh:mm:ss", Locale.getDefault());
			Log.e(tag, "time1:" + dateFormat.format(new Date()));
			if (Build.VERSION.SDK_INT >= 10) {
				// clientSocket = device
				// .createInsecureRfcommSocketToServiceRecord(uuid);
				Method method = device.getClass().getMethod(
						"createInsecureRfcommSocketToServiceRecord",
						new Class[] { UUID.class });
				clientSocket = (BluetoothSocket) method.invoke(device,
						new Object[] { uuid });
			} else {
				clientSocket = device.createRfcommSocketToServiceRecord(uuid);
			}
			Log.e(tag, "time2:" + dateFormat.format(new Date()));
			clientSocket.connect();
			Log.e(tag, "time3:" + dateFormat.format(new Date()));
			Log.d(tag, "�������ӳɹ�:" + dateFormat.format(new Date()));
			// result.setText("�������ӳɹ�     " + dateFormat.format(new Date()));
			Field tempField = clientSocket.getClass().getDeclaredField("mPort");
			tempField.setAccessible(true);
			int port = (Integer) tempField.get(clientSocket);
			Log.d(tag, "port:" + port);
		} catch (IOException e) {
			e.printStackTrace();
			if (clientSocket != null) {
				try {
					clientSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}
}
