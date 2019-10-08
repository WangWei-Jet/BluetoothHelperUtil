package com.oneway.bluetooth;

import java.lang.reflect.Method;

import android.content.Context;

public class BluetoothUtilFactory {

	private static BluetoothHelper btHelper;

	public static BluetoothHelper getBluetoothHelper(Context context) {
		if (btHelper == null) {
			btHelper = new BluetoothHelperImpl(context);
		}
		return btHelper;
	}

	public static Object invokeReflection(Object instance, String functionName,
			Class<?>[] paramTypes, Object[] paramValues) throws Exception {
		if (paramTypes == null) {
			if (paramValues != null) {
				return null;
			}
		} else {
			if (paramValues == null) {
				return null;
			}
		}
		if (instance == null || functionName == null) {
			return null;
		}

		if (paramTypes == null && paramValues == null) {
			Method method = instance.getClass().getMethod(functionName,
					paramTypes);
			return method.invoke(instance, paramValues);
		} else {
			if (paramTypes.length != paramValues.length) {
				return null;
			}
			Method method = instance.getClass().getMethod(functionName);
			return method.invoke(instance);
		}
	}
}
