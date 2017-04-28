package com.liteon.iview.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;
import com.github.mjdev.libaums.fs.UsbFileStreamFactory;
import com.liteon.iview.R;
import com.liteon.iview.util.DVRClient;
import com.liteon.iview.util.Def;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

public class DvrInfoService extends IntentService {

    private static final String TAG = DvrInfoService.class.getName();
    private static final String ACTION_GET_ALL_INFO = Def.ACTION_GET_ALL_INFO;
    private static final String ACTION_GET_SYS_MODE = Def.ACTION_GET_SYS_MODE;
    private static final String ACTION_SET_SYS_MODE = Def.ACTION_SET_SYS_MODE;
    private static final String ACTION_GET_CAM_MODE = Def.ACTION_GET_CAM_MODE;
    private static final String ACTION_GET_INTERNET = Def.ACTION_GET_INTERNET;
    private static final String ACTION_GET_WIRELESS = Def.ACTION_GET_WIRELESS;
    private static final String ACTION_GET_SECURITY = Def.ACTION_GET_SECURITY;
    private static final String ACTION_GET_ADMIN    = Def.ACTION_GET_ADMIN;
    private static final String ACTION_SET_TIMEZONE = Def.ACTION_SET_TIMEZONE;
    private static final String ACTION_SET_RECORDINGS = Def.ACTION_SET_RECORDINGS;
    private static final String ACTION_SET_INTERNET = Def.ACTION_SET_INTERNET;
    private static final String ACTION_SET_VPN = Def.ACTION_SET_VPN;
    private static final String ACTION_SET_WIFI = Def.ACTION_SET_WIFI;
    private static final String ACTION_SAVE_TO_PHONE = Def.ACTION_SAVE_TO_PHONE;
    private static final String ACTION_SAVE_TO_OTG = Def.ACTION_SAVE_TO_OTG;

    public DvrInfoService() {
        super("DvrInfoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_ALL_INFO.equals(action)) {
                handleActionGetAllInfo();
            } else if (ACTION_GET_SYS_MODE.equals(action)) {
                handleActionGetSysInfo();
            } else if (ACTION_SET_SYS_MODE.equals(action)) {
                String mode = intent.getStringExtra(Def.EXTRA_SET_SYS_MODE);
                handleActionSetSysInfo(mode);
            } else if (ACTION_GET_CAM_MODE.equals(action)) {
                handleActionGetCamMode();
            } else if (ACTION_GET_INTERNET.equals(action)) {
                handleActionGetInternet();
            } else if (ACTION_GET_WIRELESS.equals(action)) {
                handleActionGetWireless();
            } else if (ACTION_GET_SECURITY.equals(action)) {
                handleActionGetSecurity();
            } else if (ACTION_GET_ADMIN.equals(action)) {
                handleActionGetAdmin();
            } else if (ACTION_SET_TIMEZONE.equals(action)) {
                String timezone = intent.getStringExtra(Def.EXTRA_TIMEZONE);
                String ntpServer = intent.getStringExtra(Def.EXTRA_NTP_SERVER);
                handleActionSetTimezone(timezone, ntpServer);
            } else if (ACTION_SET_RECORDINGS.equals(action)) {
                String recordingLength = intent.getStringExtra(Def.EXTRA_RECORDING_LENGTH);
                String recordingChannel = intent.getStringExtra(Def.EXTRA_RECORDING_CHANNEL);
                handleActionSetRecordings(recordingLength, recordingChannel);
            } else if (ACTION_SET_INTERNET.equals(action)) {
                String apn = intent.getStringExtra(Def.EXTRA_APN);
                String pin = intent.getStringExtra(Def.EXTRA_PIN);
                String dial_Num = intent.getStringExtra(Def.EXTRA_DIAL_NUM);
                String username = intent.getStringExtra(Def.EXTRA_USERNAME_3G);
                String password = intent.getStringExtra(Def.EXTRA_PASSWORD_3G);
                String modem =intent.getStringExtra(Def.EXTRA_MODEM);
                handleActionSetInternet(apn, pin, dial_Num, username, password, modem);
            } else if (ACTION_SET_VPN.equals(action)) {
                String PPTPServer = intent.getStringExtra(Def.EXTRA_PPTP_SERVER);
                String PPTPUsername = intent.getStringExtra(Def.EXTRA_PPTP_USERNAME);
                String PPTPPassword = intent.getStringExtra(Def.EXTRA_PPTP_PASSWORD);
                handleActionSetVPN(PPTPServer, PPTPUsername, PPTPPassword);
            } else if (ACTION_SET_WIFI.equals(action)) {
                String ssid = intent.getStringExtra(Def.EXTRA_SSID);
                String securityMode = intent.getStringExtra(Def.EXTRA_SECURITYMODE);
                String encryptType = intent.getStringExtra(Def.EXTRA_ENCRYPTTYPE);
                String passPhase =intent.getStringExtra(Def.EXTRA_PASSPHASE);
                handleActionSetWifi(ssid,securityMode,encryptType,passPhase);
            } else if (ACTION_SAVE_TO_PHONE.equals(action)) {
            	String[] url = intent.getStringArrayExtra(Def.EXTRA_SAVE_ITEM_URL);
            	String[] id = intent.getStringArrayExtra(Def.EXTRA_VIDEO_ITEM_ID);
            	String[] name = intent.getStringArrayExtra(Def.EXTRA_SAVE_ITEM_NAME);
            	handleActionSaveToPhone(url, id, name);
            } else if (ACTION_SAVE_TO_OTG.equals(action)) {
            	String[] url = intent.getStringArrayExtra(Def.EXTRA_SAVE_ITEM_URL);
            	String[] id = intent.getStringArrayExtra(Def.EXTRA_VIDEO_ITEM_ID);
            	String[] name = intent.getStringArrayExtra(Def.EXTRA_SAVE_ITEM_NAME);
            	handleActionSaveToOTG(url, id, name);
            }
        }
    }

    private void handleActionSaveToPhone(String[] url, String[] id, String[] name) {
    	
    	Intent intent = new Intent(Def.ACTION_SAVE_TO_PHONE_STATUS);
    	if (!isExternalStorageWritable()) {
    		intent.putExtra(Def.EXTRA_SAVE_STATUS, false);
    		sendBroadcast(intent);
    		return ;
    	}
    	File path = getAlbumStorageDir("Download") ;   	
    	if (!path.exists()) {
    		intent.putExtra(Def.EXTRA_SAVE_STATUS, false);
    		sendBroadcast(intent);
    		return ;
    	}
    	int count = url.length;
    	boolean isSuccess = true;
    	boolean[] status = new boolean[count];
    	String[] downloadPath = new String[count];
    	for (int i = 0; i < url.length; i++) {
    		File file = new File(path, name[i]);
    		try {
				status[i] = DVRClient.downloadFileFromURL(url[i], new FileOutputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
    		downloadPath[i] = file.getAbsolutePath();
    		if (status[i]) {
    			isSuccess = false;
    		}
    	}
    	intent.putExtra(Def.EXTRA_SAVE_STATUS_ARY, status);
    	intent.putExtra(Def.EXTRA_SAVE_STATUS_FILE_PATH, downloadPath);
    	intent.putExtra(Def.EXTRA_VIDEO_ITEM_ID, id);
    	if (isSuccess) {
    		intent.putExtra(Def.EXTRA_SAVE_STATUS, true);
    		sendBroadcast(intent);
    	} else {
    		intent.putExtra(Def.EXTRA_SAVE_STATUS, false);
    		sendBroadcast(intent);
    	}
	}
    
    private void handleActionSaveToOTG(String[] url, String[] id, String[] name) {
    	
    	Intent intent = new Intent(Def.ACTION_SAVE_TO_OTG_STATUS);
    	UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(this /* Context or Activity */);
    	OutputStream os = null;
    	if (devices.length > 0) {
    		UsbMassStorageDevice device = devices[0];
    		try {
				device.init();
	    		FileSystem currentFs = device.getPartitions().get(0).getFileSystem();
	    		UsbFile root = currentFs.getRootDirectory();
	    		UsbFile newDir = root.createDirectory("Download");
	    		UsbFile file = newDir.createFile(name[0]);
	    		//os = new UsbFileOutputStream(file);
	    		os = UsbFileStreamFactory.createBufferedOutputStream(file, currentFs);
			} catch (IOException e) {
				e.printStackTrace();
				intent.putExtra(Def.EXTRA_SAVE_STATUS, false);
	    		sendBroadcast(intent);
			}

	    	int count = url.length;
	    	boolean isSuccess = true;
	    	boolean[] status = new boolean[count];
	    	String[] downloadPath = new String[count];
	    	for (int i = 0; i < url.length; i++) {
				status[i] = DVRClient.downloadFileFromURL(url[i], os);
	    		//TODO get Actual path 
				downloadPath[i] = url[i];
	    		if (status[i]) {
	    			isSuccess = false;
	    		}
	    	}
	    	device.close();
	    	intent.putExtra(Def.EXTRA_SAVE_STATUS_ARY, status);
	    	intent.putExtra(Def.EXTRA_SAVE_STATUS_FILE_PATH, downloadPath);
	    	intent.putExtra(Def.EXTRA_VIDEO_ITEM_ID, id);
	    	if (isSuccess) {
	    		intent.putExtra(Def.EXTRA_SAVE_STATUS, true);
	    		sendBroadcast(intent);
	    	} else {
	    		intent.putExtra(Def.EXTRA_SAVE_STATUS, false);
	    		sendBroadcast(intent);
	    	}
    	}
	}
    

	private void handleActionGetAllInfo() {
    	DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
    	Intent intent = new Intent(Def.ACTION_GET_ALL_INFO);
    	boolean isLocalUrlReachable = true;
    	boolean isStorageMode = false;
    	if (DVRClient.isURLReachable(getApplicationContext(), Def.DVR_PREVIEW_URL)) {
            intent.putExtra(Def.EXTRA_GET_ALL_INFO, Def.RECORDING_MODE);
    	} else if (DVRClient.isURLReachable(getApplicationContext(), Def.DVR_RECORDINGS_URL)) {
            intent.putExtra(Def.EXTRA_GET_ALL_INFO, Def.STORAGE_MODE);
            isStorageMode = true;
    	} else {
            intent.putExtra(Def.EXTRA_GET_ALL_INFO, Def.VPN_MODE);
            isLocalUrlReachable = false;
    	}
        sendBroadcast(intent);
        if (isStorageMode) {
        	dvrClient.getRecordingList();
        	NotifyRecordingListChange();
        }
        if (isLocalUrlReachable) {
	    	dvrClient.getWifiBasic();
	    	dvrClient.getWifiSecurity();
	    	dvrClient.getNetworkSetting();
	    	dvrClient.getInfoFromADMPage();
	    	dvrClient.getCameraSetting();
        }

    }
    private void NotifyRecordingListChange() {
    	Intent intent = new Intent();
    	intent.setAction(Def.ACTION_GET_RECORDING_LIST);
    	sendBroadcast(intent);
    }
    private void handleActionGetSysInfo() {
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        String mode = dvrClient.getSystemMode();
        Log.v(TAG, "[handleActionGetSysInfo] sys mode is " + mode);
        Intent intent = new Intent(Def.ACTION_GET_SYS_MODE);
        intent.putExtra(Def.EXTRA_GET_SYS_MODE, mode);
        sendBroadcast(intent);
    }

    private void handleActionSetSysInfo(String mode) {
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        dvrClient.setSystemMode(mode);
        Log.v(TAG, "[handleActionSetSysInfo] sys mode is " + mode);
        Intent intent = new Intent(Def.ACTION_GET_SYS_MODE);
        intent.putExtra(Def.EXTRA_GET_SYS_MODE, mode);
        sendBroadcast(intent);
    }

    private void handleActionGetCamMode(){
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        String mode = dvrClient.getCameraMode();
        Log.v(TAG, "[handleActionGetCamMode] Camera Mode is " + mode);
        Intent intent = new Intent(Def.ACTION_GET_CAM_MODE);
        intent.putExtra(Def.EXTRA_GET_CAM_MODE, mode);
        sendBroadcast(intent);
        //TODO reduce query times
        String length = dvrClient.getRecordingLength();
        Log.v(TAG, "[handleActionGetCamMode] recording length is " + length);

    }

    private void handleActionGetInternet(){
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        Map map = dvrClient.get3GModemList();
        Log.v(TAG, "[handleActionGetInternet] get3GModemList is " + map.toString());
    }

    private void handleActionGetWireless(){

    }

    private void handleActionGetSecurity(){

    }

    private void handleActionGetAdmin(){
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        Map map = dvrClient.getTimeZoneList();
        Log.v(TAG, "[handleActionGetAdmin] getTimeZoneList is " + map.toString());
    }

    private void handleActionSetTimezone(String timezone, String ntpServer) {
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        dvrClient.setTimezone(timezone, ntpServer);
    }

    private void handleActionSetRecordings(String recordingLength, String recordingChannel) {
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        dvrClient.setRecordings(recordingLength, recordingChannel);
    }

    private void handleActionSetInternet(String apn, String pin, String dial_num, String username, String password, String modem) {
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        dvrClient.setInternets(apn, pin, dial_num, username, password, modem);
    }

    private void handleActionSetVPN(String pptpServer, String pptpUsername, String pptpPassword) {
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        dvrClient.setVPNs(pptpServer,pptpUsername,pptpPassword);
    }

    private void handleActionSetWifi(String ssid, String securityMode, String encryptType, String passPhase) {
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        dvrClient.setWIFIs(ssid,securityMode,encryptType,passPhase);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
    }
    
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    
    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }
}
