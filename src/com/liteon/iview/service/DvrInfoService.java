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
import com.liteon.iview.util.DVRClient.ProgressChangeCallback;
import com.liteon.iview.util.Def;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

public class DvrInfoService extends IntentService {

    private static final String TAG = DvrInfoService.class.getName();

    public DvrInfoService() {
        super("DvrInfoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Def.ACTION_GET_ALL_INFO.equals(action)) {
                handleActionGetAllInfo();
            } else if (Def.ACTION_GET_SYS_MODE.equals(action)) {
                handleActionGetSysInfo();
            } else if (Def.ACTION_SET_SYS_MODE.equals(action)) {
                String mode = intent.getStringExtra(Def.EXTRA_SET_SYS_MODE);
                handleActionSetSysInfo(mode);
            } else if (Def.ACTION_GET_CAM_MODE.equals(action)) {
                handleActionGetCamMode();
            } else if (Def.ACTION_SET_CAM_MODE.equals(action)) {
                final String mode = intent.getStringExtra(Def.EXTRA_SET_CAM_MODE);
            	new Thread() {
            		public void run() {
            			handleActionSetCamInfo(mode);
            		};
            	}.start();
                Intent intentResponse = new Intent(Def.ACTION_GET_CAM_MODE);
                intentResponse.putExtra(Def.EXTRA_GET_CAM_MODE, mode);
                sendBroadcast(intentResponse);
            } else if (Def.ACTION_GET_NETWORKING.equals(action)) {
                handleActionGetInternet();
            } else if (Def.ACTION_GET_WIRELESS.equals(action)) {
                handleActionGetWireless();
            } else if (Def.ACTION_GET_SECURITY.equals(action)) {
                handleActionGetSecurity();
            } else if (Def.ACTION_GET_ADMIN.equals(action)) {
                handleActionGetAdmin();
            } else if (Def.ACTION_GET_RECORDINGS.equals(action)) {
            	handleActionGetRecordings();
            } else if (Def.ACTION_SET_TIMEZONE.equals(action)) {
                String timezone = intent.getStringExtra(Def.EXTRA_TIMEZONE);
                String ntpServer = intent.getStringExtra(Def.EXTRA_NTP_SERVER);
                handleActionSetTimezone(timezone, ntpServer);
            } else if (Def.ACTION_SET_RECORDINGS.equals(action)) {
                String recordingLength = intent.getStringExtra(Def.EXTRA_RECORDING_LENGTH);
                String recordingChannel = intent.getStringExtra(Def.EXTRA_RECORDING_CHANNEL);
                String recordingOutput = intent.getStringExtra(Def.EXTRA_RECORDING_OUTPUT);
                handleActionSetRecordings(recordingLength, recordingChannel, recordingOutput);
            } else if (Def.ACTION_SET_INTERNET.equals(action)) {
                String apn = intent.getStringExtra(Def.EXTRA_APN);
                String pin = intent.getStringExtra(Def.EXTRA_PIN);
                String dial_Num = intent.getStringExtra(Def.EXTRA_DIAL_NUM);
                String username = intent.getStringExtra(Def.EXTRA_USERNAME_3G);
                String password = intent.getStringExtra(Def.EXTRA_PASSWORD_3G);
                String modem =intent.getStringExtra(Def.EXTRA_MODEM);
                handleActionSetInternet(apn, pin, dial_Num, username, password, modem);
            } else if (Def.ACTION_SET_VPN.equals(action)) {
                String PPTPServer = intent.getStringExtra(Def.EXTRA_PPTP_SERVER);
                String PPTPUsername = intent.getStringExtra(Def.EXTRA_PPTP_USERNAME);
                String PPTPPassword = intent.getStringExtra(Def.EXTRA_PPTP_PASSWORD);
                String PPTPClientIP = intent.getStringExtra(Def.EXTRA_PPTP_CLIENT_IP);
                handleActionSetVPN(PPTPServer, PPTPUsername, PPTPPassword, PPTPClientIP);
            } else if (Def.ACTION_SET_WIFI.equals(action)) {
                String ssid = intent.getStringExtra(Def.EXTRA_SSID);
                String securityMode = intent.getStringExtra(Def.EXTRA_SECURITYMODE);
                String encryptType = intent.getStringExtra(Def.EXTRA_ENCRYPTTYPE);
                String passPhase =intent.getStringExtra(Def.EXTRA_PASSPHASE);
                handleActionSetWifi(ssid,securityMode,encryptType,passPhase);
            } else if (Def.ACTION_SAVE_TO_PHONE.equals(action)) {
            	final String[] url = intent.getStringArrayExtra(Def.EXTRA_SAVE_ITEM_URL);
            	final String[] id = intent.getStringArrayExtra(Def.EXTRA_VIDEO_ITEM_ID);
            	final String[] name = intent.getStringArrayExtra(Def.EXTRA_SAVE_ITEM_NAME);
            	new Thread() {
            		public void run() {
                    	handleActionSaveToPhone(url, id, name);
            		};
            	
            	}.start();
            } else if (Def.ACTION_SAVE_TO_OTG.equals(action)) {
            	final String[] url = intent.getStringArrayExtra(Def.EXTRA_SAVE_ITEM_URL);
            	final String[] id = intent.getStringArrayExtra(Def.EXTRA_VIDEO_ITEM_ID);
            	final String[] name = intent.getStringArrayExtra(Def.EXTRA_SAVE_ITEM_NAME);
            	new Thread() {
            		public void run() {
                    	handleActionSaveToOTG(url, id, name);
            		};
            	}.start();
            } else if (Def.ACTION_GET_RECORDING_LIST.equals(action)) {
            	new Thread() {
            		public void run() {
                    	handleActionGetRecordingList();
            		};
            	}.start();
            }
        }
    }

    private void handleActionGetRecordingList() {
    	DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
    	dvrClient.getRecordingList();
    	NotifyRecordingListChange();
    }
    private void handleActionSaveToPhone(String[] url, String[] id, String[] name) {
    	
    	Intent intent = new Intent(Def.ACTION_SAVE_TO_PHONE_STATUS);
    	if (!isExternalStorageWritable()) {
    		intent.putExtra(Def.EXTRA_SAVE_STATUS, false);
    		sendBroadcast(intent);
    		return ;
    	}
    	File path = getAlbumStorageDir(getString(R.string.app_name)) ;   	
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
				status[i] = DVRClient.downloadFileFromURL(url[i], new FileOutputStream(file), mOnProgressChange, i + 1, count);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
    		downloadPath[i] = file.getAbsolutePath();
    		if (!status[i]) {
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
    private ProgressChangeCallback mOnProgressChange = new ProgressChangeCallback () {
    	public void onProgressChange(int progress, int idx, int count) {
        	Intent intent = new Intent(Def.ACTION_SAVE_TO_PROGRESS);
        	intent.putExtra(Def.EXTRA_SAVE_TO_PROGRESS, progress);
        	intent.putExtra(Def.EXTRA_SAVE_TO_IDX, idx);
        	intent.putExtra(Def.EXTRA_SAVE_TO_COUNT, count);
    		sendBroadcast(intent);
    	};
    };
    private void handleActionSaveToOTG(String[] url, String[] id, String[] name) {
    	
    	Intent intent = new Intent(Def.ACTION_SAVE_TO_OTG_STATUS);
    	UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(getApplicationContext() /* Context or Activity */);
    	OutputStream os = null;
    	if (devices.length > 0) {
    		UsbMassStorageDevice device = devices[0];
    		try {
				device.init();
	    		FileSystem currentFs = device.getPartitions().get(0).getFileSystem();
	    		UsbFile root = currentFs.getRootDirectory();
	    		UsbFile downloadDir = root.search(getString(R.string.app_name));
	    		if (downloadDir == null) {
	    			downloadDir = root.createDirectory(getString(R.string.app_name));
	    		}
		    	int count = url.length;
		    	boolean isSuccess = true;
		    	boolean[] status = new boolean[count];
		    	String[] downloadPath = new String[count];
		    	for (int i = 0; i < url.length; i++) {
		    		UsbFile file = downloadDir.search(name[i]);
		    		if ( file == null) {
		    			file = downloadDir.createFile(name[i]);
		    		}
		    		os = UsbFileStreamFactory.createBufferedOutputStream(file, currentFs);
					status[i] = DVRClient.downloadFileFromURL(url[i], os, mOnProgressChange, i + 1, count);
		    		//TODO get Actual path 
					downloadPath[i] = url[i];
		    		if (!status[i]) {
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
			} catch (IOException e) {
				e.printStackTrace();
				intent.putExtra(Def.EXTRA_SAVE_STATUS, false);
	    		sendBroadcast(intent);
			}
    	}
	}
    
	private void handleActionGetAllInfo() {
    	DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
    	Intent intent = new Intent(Def.ACTION_GET_ALL_INFO);
    	boolean isDVRReachable = true;
    	boolean isStorageMode = false;
    	SharedPreferences SharedPref = getApplicationContext().getSharedPreferences(
                Def.SHARE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = SharedPref.edit();
        Def.DVR_VPN_IP = SharedPref.getString(Def.SP_VPN_IP, Def.DVR_VPN_IP);
    	if (DVRClient.isURLReachable(getApplicationContext(), Def.getLocalPreviewURL())) {
            intent.putExtra(Def.EXTRA_GET_ALL_INFO, Def.RECORDING_MODE);
    	} else if (DVRClient.isURLReachable(getApplicationContext(), Def.getLocalRecordingsURL())) {
            intent.putExtra(Def.EXTRA_GET_ALL_INFO, Def.STORAGE_MODE);
            isStorageMode = true;
    	} else {
            Def.IS_VPN_MODE = true;
            if (DVRClient.isURLReachable(getApplicationContext(), Def.getRemotePreviewURL())) {
                intent.putExtra(Def.EXTRA_GET_ALL_INFO, Def.RECORDING_MODE);
        	} else if (DVRClient.isURLReachable(getApplicationContext(), Def.getRemoteRecordingsURL())) {
                intent.putExtra(Def.EXTRA_GET_ALL_INFO, Def.STORAGE_MODE);
                isStorageMode = true;
        	} else {
        		isDVRReachable = false;
        	}
    	}
    	intent.putExtra(Def.EXTRA_IS_DVR_REACHABLE, isDVRReachable);
        sendBroadcast(intent);
        if (isDVRReachable) {
            
            if (isStorageMode) {
            	dvrClient.getRecordingList();
            	NotifyRecordingListChange();
                editor.putString(Def.SP_SYSTEM_MODE, Def.STORAGE_MODE);
                
            } else {
                editor.putString(Def.SP_SYSTEM_MODE, Def.RECORDING_MODE);
            }
            editor.commit();
	    	dvrClient.getStatusInfo();
	    	dvrClient.getCameraSetting();
        	dvrClient.getWifiBasic();
	    	dvrClient.getWifiSecurity();
	    	dvrClient.getNetworkSetting();
	    	dvrClient.getInfoFromADMPage();
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
    
    private void handleActionSetCamInfo(String mode) {
    	DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        dvrClient.setCameraMode(mode);
        Log.v(TAG, "[handleActionSetCamMode] Camera Mode is " + mode);
    }

    private void handleActionGetInternet(){
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        dvrClient.getNetworkSetting();
        Log.v(TAG, "[handleActionGetInternet] ");
        Intent intent = new Intent(Def.ACTION_GET_NETWORKING);
        sendBroadcast(intent);
    }

    private void handleActionGetWireless(){
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
    	dvrClient.getWifiBasic();
        Log.v(TAG, "[handleActionGetWireless] ");
        Intent intent = new Intent(Def.ACTION_GET_WIRELESS);
        sendBroadcast(intent);
    }

    private void handleActionGetSecurity(){
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
    	dvrClient.getWifiSecurity();
        Log.v(TAG, "[handleActionGetSecurity] ");
        Intent intent = new Intent(Def.ACTION_GET_SECURITY);
        sendBroadcast(intent);
    }

    private void handleActionGetAdmin(){
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        dvrClient.getInfoFromADMPage();
        Log.v(TAG, "[handleActionGetAdmin] ");
        Intent intent = new Intent(Def.ACTION_GET_ADMIN);
        sendBroadcast(intent);
    }
    
    private void handleActionGetRecordings(){
    	DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        dvrClient.getCameraSetting();
        Log.v(TAG, "[handleActionGetRecordings] ");
        Intent intent = new Intent(Def.ACTION_GET_RECORDINGS);
        sendBroadcast(intent);
    }

    private void handleActionSetTimezone(String timezone, String ntpServer) {
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        dvrClient.setTimezone(timezone, ntpServer);
    }

    private void handleActionSetRecordings(String recordingLength, String recordingChannel, String recordingOutput) {
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        dvrClient.setRecordings(recordingLength, recordingChannel, recordingOutput);
    }

    private void handleActionSetInternet(String apn, String pin, String dial_num, String username, String password, String modem) {
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        dvrClient.setInternets(apn, pin, dial_num, username, password, modem);
    }

    private void handleActionSetVPN(String pptpServer, String pptpUsername, String pptpPassword, String pptpClientIP) {
        DVRClient dvrClient = DVRClient.newInstance(getApplicationContext());
        Def.setRemotePreviewURL(pptpClientIP);
        SharedPreferences SharedPref = getApplicationContext().getSharedPreferences(
                Def.SHARE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = SharedPref.edit();
        editor.putString(Def.SP_VPN_IP, pptpClientIP);
        editor.commit();
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
