package com.liteon.iview.util;

import java.io.IOException;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;

import android.app.ActionBar;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.text.StaticLayout;
import android.util.Log;

public class UsbUtil {

	public static boolean discoverDevice(Context context, Intent intent) {
		UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(context);

		if (devices.length == 0) {
			Log.w("setupDevice", "no device found!");
			return false;
		}

		// we only use the first device
		UsbMassStorageDevice device = devices[0];

		UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

		if (usbDevice != null && usbManager.hasPermission(usbDevice)) {
			Log.d("setupDevice", "received usb device via intent");
			// requesting permission is not needed in this case
			return setupDevice(device);
		} else {
			// first request permission from user to communicate with the
			// underlying
			// UsbDevice
			PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(
					Def.ACTION_USB_PERMISSION), 0);
			usbManager.requestPermission(device.getUsbDevice(), permissionIntent);
		}
		return false;
	}

	public static boolean setupDevice(UsbMassStorageDevice device) {
		try {
			device.init();

			// we always use the first partition of the device
			FileSystem currentFs = device.getPartitions().get(0).getFileSystem();
			Log.d("setupDevice", "Capacity: " + currentFs.getCapacity());
			Log.d("setupDevice", "Occupied Space: " + currentFs.getOccupiedSpace());
			Log.d("setupDevice", "Free Space: " + currentFs.getFreeSpace());
            Log.d("setupDevice", "Chunk size: " + currentFs.getChunkSize());
			UsbFile root = currentFs.getRootDirectory();
		} catch (IOException e) {
			Log.e("setupDevice", "error setting up device", e);
			return false;
		}
		return true;
	}
}
