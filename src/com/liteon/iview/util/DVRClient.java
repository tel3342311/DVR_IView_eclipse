package com.liteon.iview.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class DVRClient {

    private final static String TAG = DVRClient.class.getName();
    private static DVRClient mDVRClient;
    private Context mContext;
    private String username = "admin";
    private String password = "admin";
    private Uri mUri;
    private String mCameraMode = Def.FRONT_CAM_MODE;
    private SharedPreferences mSharedPref;
    private Gson mGson;
    private DVRClient(Context c) {
        mContext = c;
        mUri = new Uri.Builder()
                    .scheme("http")
                    .authority("192.168.10.1").build();
        mSharedPref = mContext.getSharedPreferences(
                Def.SHARE_PREFERENCE, Context.MODE_PRIVATE);
        mGson = new GsonBuilder().create();
    }

    public static DVRClient newInstance(Context context) {
        if (mDVRClient == null) {
            mDVRClient = new DVRClient(context);
        }
        return mDVRClient;
    }

    public void setSystemMode(String mode) {
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.system_cgi));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }
            Uri.Builder builder = mUri.buildUpon()
                    .appendQueryParameter("page", "system_configuration")
                    .appendQueryParameter("listbox_usbmode", mode);

            String query = builder.build().getEncodedQuery();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Length", Integer.toString(query.getBytes().length));

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Set DVR mode to " + mode + ", Response is " + response);
            urlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //public String getCameraMode() {
    //    return mCameraMode;
    //}

    public void setCameraMode(String mode) {

        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.camera_cgi));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }
            Uri.Builder builder = mUri.buildUpon()
                    .appendQueryParameter("page", "camera_configuration")
                    //.appendQueryParameter("listbox_capture", "cha")
                    //.appendQueryParameter("listbox_video_length", "2m")
                    .appendQueryParameter("listbox_resolution", mode);

            String query = builder.build().getEncodedQuery();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Length", Integer.toString(query.getBytes().length));

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Set Camera mode to " + mode + ", Response is " + response);
            mCameraMode = mode;
            urlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRecordingLength(String length) {

        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.camera_cgi));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }
            Uri.Builder builder = mUri.buildUpon()
                    .appendQueryParameter("page", "camera_configuration")
                    .appendQueryParameter("listbox_video_length", length);

            String query = builder.build().getEncodedQuery();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Length", Integer.toString(query.getBytes().length));

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Set recording length to " + length + ", Response is " + response);
            urlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSystemMode() {
        String mode = "";
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.system_setting));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            InputStream is = urlConnection.getInputStream();
            Document doc = Jsoup.parse(is, "UTF-8", url.toString());

            Elements element = doc.getElementsByAttributeValue("language", "JavaScript");
            String data = element.first().data();
            Pattern pattern= Pattern.compile("var opmod  = \"(.*)\";");
            Matcher matcher = pattern.matcher(data);

            if (matcher.find()) {
                mode = matcher.group(1);
            }
            Log.i(TAG, "Get System mode , mode is " + mode);
            is.close();
            urlConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return mode;
    }

    public String getCameraMode() {
        String mode = "";
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.camera_setting));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            InputStream is = urlConnection.getInputStream();
            Document doc = Jsoup.parse(is, "UTF-8", url.toString());
            Elements element = doc.getElementsByAttributeValue("language", "JavaScript");
            String data = element.first().data();
            Pattern pattern= Pattern.compile("var resol  = \"(.*)\";");
            Matcher matcher = pattern.matcher(data);

            if (matcher.find()) {
                mode = matcher.group(1);
            }
            Log.i(TAG, "Get Camera mode , mode is " + mode);
            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Get Camera mode , Response is " + response);
            is.close();
            urlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mode;
    }

    public String getRecordingLength() {
        String length = "";
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.camera_setting));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            InputStream is = urlConnection.getInputStream();
            Document doc = Jsoup.parse(is, "UTF-8", url.toString());
            Elements element = doc.getElementsByAttributeValue("language", "JavaScript");
            String data = element.first().data();
            Pattern pattern= Pattern.compile("var clipl  = \"(.*)\";");
            Matcher matcher = pattern.matcher(data);

            if (matcher.find()) {
                length = matcher.group(1);
            }
            Log.i(TAG, "Get recording length, length is " + length);
            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Get recording length , Response is " + response);
            is.close();
            urlConnection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return length;
    }

    public Map<String, String> get3GModemList() {
        Map<String, String> map = new HashMap<>();
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.net_setting));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            InputStream is = urlConnection.getInputStream();
            Document doc = Jsoup.parse(is, "UTF-8", url.toString());
            Elements elements = doc.select("select[name=Dev3G] > option");
            for (Element e : elements) {
                map.put(e.val(),e.text());
            }
            Log.i(TAG, "Get 3GModem List, map is " + map.toString());
            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Get 3GModem List , Response is " + response);
            is.close();
            urlConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    //public get infomation from ADM page "adm/management.shtml"
    //NTP server
    //Timezone list

    public void getInfoFromADMPage() {
        Map<String, String> map = new HashMap<>();
        String timeZoneListJson = "";
        String timeZone = "";
        String ntp_server = "";
        String ntp_sync_value = "";
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.adm_setting));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            InputStream is = urlConnection.getInputStream();
            Document doc = Jsoup.parse(is, "UTF-8", url.toString());
            Elements elements = doc.select("select[name=time_zone] > option");
            for (Element e : elements) {
                map.put(e.text(), e.val());
            }

            timeZoneListJson = mGson.toJson(map);

            //Get Current TimeZone
            elements = doc.getElementsByAttributeValue("language", "JavaScript");
            String data = elements.first().data();
            Pattern pattern = Pattern.compile("var tz = \"(.*)\";");
            Matcher matcher = pattern.matcher(data);
            if (matcher.find()) {
                timeZone = matcher.group(1);
            }

            //Get NTP server
            elements = doc.select("input[name=NTPServerIP]");
            ntp_server = elements.val();

            elements = doc.select("input[name=NTPSync]");
            ntp_sync_value = elements.val();

            Log.i(TAG, "getInfoFromADMPage Timezone List, map is " + map.toString());
            Log.i(TAG, "getInfoFromADMPage Timezone is " + timeZone);
            Log.i(TAG, "getInfoFromADMPage NTPServerIP " + ntp_server);
            Log.i(TAG, "getInfoFromADMPage NTP Sync value " + ntp_sync_value);

            int response = urlConnection.getResponseCode();
            Log.i(TAG, "getInfoFromADMPage, Response is " + response);
            is.close();
            urlConnection.disconnect();

            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(Def.SP_TIMEZONE_LIST, timeZoneListJson);
            editor.putString(Def.SP_TIMEZONE, timeZone);
            editor.putString(Def.SP_NTPSERVER, ntp_server);
            editor.putString(Def.SP_NTP_SYNC_VALUE, ntp_sync_value);
            editor.commit();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getCameraSetting() {

        //option is [2m,3m,5m]
        String length = "";
        //option is [cha,chb,chab]
        String recording_channel = "";
        //option is [cha,chb]
        String preview_channel = "";
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.camera_setting));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            InputStream is = urlConnection.getInputStream();
            Document doc = Jsoup.parse(is, "UTF-8", url.toString());
            Elements element = doc.getElementsByAttributeValue("language", "JavaScript");
            String data = element.first().data();
            Pattern pattern = Pattern.compile("var clipl  = \"(.*)\";");
            Matcher matcher = pattern.matcher(data);

            if (matcher.find()) {
                length = matcher.group(1);
            }

            pattern = Pattern.compile("var resol  = \"(.*)\";");
            matcher = pattern.matcher(data);

            if (matcher.find()) {
                recording_channel = matcher.group(1);
            }

            pattern = Pattern.compile("var dspch  = \"(.*)\";");
            matcher = pattern.matcher(data);

            if (matcher.find()) {
                preview_channel = matcher.group(1);
            }

            Log.i(TAG, "getCameraSetting recording length, length is " + length);
            Log.i(TAG, "getCameraSetting recording_camera, camera is " + recording_channel);
            Log.i(TAG, "getCameraSetting camera_mode, mode is " + preview_channel);

            int response = urlConnection.getResponseCode();
            Log.i(TAG, "getCameraSetting, Response is " + response);
            is.close();
            urlConnection.disconnect();
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(Def.SP_RECORDING_LENGTH, length);
            editor.putString(Def.SP_RECORDING_CAMERA, recording_channel);
            editor.putString(Def.SP_PREVIEW_CAMERA, preview_channel);
            editor.commit();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getWifiBasic() {
        String ssid = "";
        String passphase = "";
        String bssid = "";
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.wifi_setting));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            InputStream is = urlConnection.getInputStream();
            Document doc = Jsoup.parse(is, "UTF-8", url.toString());
            Elements element = doc.getElementsByAttributeValue("language", "JavaScript");
            String data = element.first().data();
            Pattern pattern = Pattern.compile("document.wireless_basic.mssid_0.value = \"(.*)\";");
            Matcher matcher = pattern.matcher(data);

            if (matcher.find()) {
                ssid = matcher.group(1);
            }
            Log.i(TAG, "Get SSID is " + ssid);

            element = doc.select("td[id=basicBSSID] + td");
            //remove &nbsp;
            bssid = element.text().replace("\u00a0","");
            Log.i(TAG, "Get BSSID is " + bssid);

            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Get getWifiBasic , Response is " + response);
            is.close();
            urlConnection.disconnect();
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(Def.SP_SSID, ssid);
            editor.putString(Def.SP_BSSID, bssid);
            editor.commit();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getWifiSecurity() {
        String securityMode = "";
        String encryptType = "";
        String passPhase = "";
        String keyRenew = "";
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.security_setting));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            InputStream is = urlConnection.getInputStream();
            Document doc = Jsoup.parse(is, "UTF-8", url.toString());
            Elements elements = doc.getElementsByAttributeValue("language", "JavaScript");
            String data = elements.first().data();
            Pattern pattern = Pattern.compile("PreAuth = str\\.split\\(\";\"\\);\n\tstr = \"(.*)\";");
            Matcher matcher = pattern.matcher(data);

            if (matcher.find()) {
                securityMode = matcher.group(1);
            }

            pattern = Pattern.compile("AuthMode = str\\.split\\(\";\"\\);\n\tstr = \"(.*)\";");
            matcher = pattern.matcher(data);

            if (matcher.find()) {
                encryptType = matcher.group(1);
            }
            //get Passphase
            pattern = Pattern.compile("WPAPSK\\[0\\] = \"(.*)\"");
            matcher = pattern.matcher(data);
            if (matcher.find()) {
                passPhase = matcher.group(1);
            }
            //get Key renew interval
            pattern = Pattern.compile("RekeyMethod = str\\.split\\(\";\"\\);\n\tstr = \"(.*)\";");
            matcher = pattern.matcher(data);
            if (matcher.find()) {
                keyRenew = matcher.group(1);
            }
            Log.i(TAG, "Get Key Renew Interval" + keyRenew);
            Log.i(TAG, "Get passphase is " + passPhase);
            Log.i(TAG, "Get securityMode is " + securityMode);
            Log.i(TAG, "Get encrypType is " + encryptType);
            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Get wifi security, Response is " + response);

            is.close();
            urlConnection.disconnect();
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(Def.SP_SECURITY, securityMode);
            editor.putString(Def.SP_ENCRYPTTYPE, encryptType);
            editor.putString(Def.SP_PASSPHASE, passPhase);
            editor.putString(Def.SP_KEYRENEW, keyRenew);
            editor.commit();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getNetworkSetting() {
        //3g setting
        String apn = "";
        String pin = "";
        String dial_num = "";
        String username_3g = "";
        String password_3g = "";
        String modem_name = "";
        Map<String, String> modemList = new HashMap<>();
        String modemListJson = "";
        //VPN setting
        String pptp_server = "";
        String username_pptp = "";
        String password_pptp = "";


        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.net_setting));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            InputStream is = urlConnection.getInputStream();
            Document doc = Jsoup.parse(is, "UTF-8", url.toString());
            //Get current 3G modem
            Elements elements = doc.getElementsByAttributeValue("language", "JavaScript");
            String data = elements.first().data();
            Pattern pattern= Pattern.compile("var dev_3g = \"(.*)\";");
            Matcher matcher = pattern.matcher(data);

            if (matcher.find()) {
                modem_name = matcher.group(1);
            }
            Log.i(TAG, "Get modem_name, modem_name is " + modem_name);

            //Get 3G modem list
            elements = doc.select("select[name=Dev3G] > option");
            for (Element e : elements) {
                modemList.put(e.text(), e.val());
            }
            modemListJson = mGson.toJson(modemList);

            Log.i(TAG, "Get 3GModem List, map is " + modemList.toString());

            elements = doc.select("input[name*=\"3G\"], input[name^=\"pptp\"]");
            for (Element e : elements) {
                Log.i(TAG, "data name" + e.attr("name") + " , val " + e.val() );
                if (e.attr("name").equals("APN3G")) {
                    apn = e.val();
                } else if (e.attr("name").equals("PIN3G")) {
                    pin = e.val();
                } else if (e.attr("name").equals("Dial3G")) {
                    dial_num = e.val();
                } else if (e.attr("name").equals("User3G")) {
                    username_3g = e.val();
                } else if (e.attr("name").equals("Password3G")) {
                    password_3g = e.val();
                } else if (e.attr("name").equals("pptpServer")) {
                    pptp_server = e.val();
                } else if (e.attr("name").equals("pptpUser")) {
                    username_pptp = e.val();
                } else if (e.attr("name").equals("pptpPass")) {
                    password_pptp = e.val();
                }
            }

            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(Def.SP_APN3G, apn);
            editor.putString(Def.SP_PIN3G, pin);
            editor.putString(Def.SP_DIAL3G, dial_num);
            editor.putString(Def.SP_USER3G, username_3g);
            editor.putString(Def.SP_MODEM_NAME, modem_name);
            editor.putString(Def.SP_MODEM_LIST_JSON, modemListJson);
            editor.putString(Def.SP_PASSWORD3G, password_3g);
            editor.putString(Def.SP_PPTPSERVER, pptp_server);
            editor.putString(Def.SP_PPTPUSER, username_pptp);
            editor.putString(Def.SP_PPTPPASS, password_pptp);
            editor.commit();

            int response = urlConnection.getResponseCode();
            Log.i(TAG, "getNetworkSetting , Response is " + response);
            is.close();
            urlConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getTimeZoneList() {
        Map<String, String> map = new HashMap<>();
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.adm_setting));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            InputStream is = urlConnection.getInputStream();
            Document doc = Jsoup.parse(is, "UTF-8", url.toString());
            Elements elements = doc.select("select[name=time_zone] > option");
            for (Element e : elements) {
                map.put(e.val(), e.text());
            }
            Log.i(TAG, "Get Timezone List, map is " + map.toString());
            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Get Timezone List , Response is " + response);
            is.close();
            urlConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public List<RecordingItem> getRecordingList() {

        List<RecordingItem> list = new ArrayList<>();
        try {
            URL url = new URL(Def.DVR_RECORDINGS_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);

            InputStream is = urlConnection.getInputStream();
            Document doc = Jsoup.parse(is, "UTF-8", Def.DVR_RECORDINGS_URL);
            Elements elements = doc.select("a[href*=.mp4]");
            for (Element element : elements) {
                String uri = element.attr("abs:href");
                String name = element.text();
                String time = element.parent().siblingElements().get(0).text();
                String size = element.parent().siblingElements().get(1).text();

                Log.i(TAG, "Get recording clips , <a> uri is " + uri);
                Log.i(TAG, "Get recording clips , <a> name is " + name);
                Log.i(TAG, "Get recording clips , <a> time is " + time);
                Log.i(TAG, "Get recording clips , <a> size is " + size);
                RecordingItem item = new RecordingItem(uri, name, time, size);
                list.add(item);
            }
            is.close();
            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Get recording clips , Response is " + response);
            urlConnection.disconnect();
            String recordingListJson = mGson.toJson(list);
            if (response == HttpURLConnection.HTTP_OK) {
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putString(Def.SP_RECORDING_LIST, recordingListJson);
                editor.commit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void testInputData(InputStream is) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(is, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, line);
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getAuthorizationHeader() {
        try {
            return "Basic " + new String(Base64.encode((username + ":" + password).getBytes("UTF-8"), Base64.NO_WRAP));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public void setTimezone(String timezone, String ntpServer) {
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.adm_cgi));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }
            String syncValue = mSharedPref.getString(Def.SP_NTP_SYNC_VALUE, "300");
            Uri.Builder builder = mUri.buildUpon()
                    .appendQueryParameter(Def.PAGE, Def.KEY_PAGE_TIMEZONE)
                    .appendQueryParameter("time_zone", timezone)
                    .appendQueryParameter("NTPServerIP", ntpServer)
                    .appendQueryParameter("NTPSync", syncValue);

            String query = builder.build().getEncodedQuery();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Length", Integer.toString(query.getBytes().length));

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int response = urlConnection.getResponseCode();
            Log.i(TAG, "setTimezone to " + timezone + ", Response is " + response);
            urlConnection.disconnect();
            //Save to share preference
            if (response == HttpURLConnection.HTTP_OK) {
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putString(Def.SP_TIMEZONE, timezone);
                editor.putString(Def.SP_NTPSERVER, ntpServer);
                editor.commit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRecordings(String recordingLength, String recordingChannel) {
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.camera_cgi));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }
            Uri.Builder builder = mUri.buildUpon()
                    .appendQueryParameter(Def.PAGE, Def.KEY_PAGE_RECORDINGS)
                    .appendQueryParameter(Def.VIDEO_LENGTH, recordingLength)
                    .appendQueryParameter(Def.RECORDING_CHANNEL, recordingChannel);

            String query = builder.build().getEncodedQuery();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Length", Integer.toString(query.getBytes().length));

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Set recording length to " + recordingLength + ", Set recording Channel to " + recordingChannel + ", Response is " + response);
            urlConnection.disconnect();
            //Save to share preference
            if (response == HttpURLConnection.HTTP_OK) {
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putString(Def.SP_RECORDING_LENGTH, recordingLength);
                editor.putString(Def.SP_RECORDING_CAMERA, recordingChannel);
                editor.commit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setInternets(String apn, String pin, String dial_num, String username, String password, String modem) {

        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.net_cgi));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }

            Uri.Builder builder = mUri.buildUpon()
                    .appendQueryParameter(Def.PAGE, Def.KEY_PAGE_WAN)
                    .appendQueryParameter(Def.CONNECTIONTYPE, "3G")
                    .appendQueryParameter(Def.APN3G, apn)
                    .appendQueryParameter(Def.PIN3G, pin)
                    .appendQueryParameter(Def.DIAL3G, dial_num)
                    .appendQueryParameter(Def.USER3G, username)
                    .appendQueryParameter(Def.PASSWORD3G, password)
                    .appendQueryParameter(Def.DEV3G, modem);

            String query = builder.build().getEncodedQuery();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Length", Integer.toString(query.getBytes().length));

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Set Internets to, Response is " + response);
            urlConnection.disconnect();
            //Save to share preference
            if (response == HttpURLConnection.HTTP_OK) {
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putString(Def.SP_APN3G, apn);
                editor.putString(Def.SP_PIN3G, pin);
                editor.putString(Def.SP_DIAL3G, dial_num);
                editor.putString(Def.SP_USER3G, username);
                editor.putString(Def.SP_PASSWORD3G, password);
                editor.putString(Def.SP_MODEM_NAME, modem);
                editor.commit();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setVPNs(String pptpServer, String pptpUsername, String pptpPassword) {
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.net_cgi));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }

            Uri.Builder builder = mUri.buildUpon()
                    .appendQueryParameter(Def.PAGE, Def.KEY_PAGE_WAN)
                    .appendQueryParameter(Def.PPTPSERVER, pptpServer)
                    .appendQueryParameter(Def.PPTPUSER, pptpUsername)
                    .appendQueryParameter(Def.PPTPPASS, pptpPassword);

            String query = builder.build().getEncodedQuery();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Length", Integer.toString(query.getBytes().length));

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Set VPNs to , Response is " + response);
            urlConnection.disconnect();
            //Save to share preference
            if (response == HttpURLConnection.HTTP_OK) {
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putString(Def.SP_PPTPSERVER, pptpServer);
                editor.putString(Def.SP_PPTPUSER, pptpUsername);
                editor.putString(Def.SP_PPTPPASS, pptpPassword);
                editor.commit();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setWIFIs(String ssid, String securityMode, String encryptType, String passPhase) {

        setWifiBasic(ssid);
        setWifiSecurity(securityMode,encryptType,passPhase);
    }

    private void setWifiBasic(String ssid) {
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.wifi_cgi));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }

            Uri.Builder builder = mUri.buildUpon()
                    .appendQueryParameter(Def.PAGE, Def.KEY_PAGE_BASIC)
                    .appendQueryParameter(Def.WLAN_CONF, "2860")
                    .appendQueryParameter(Def.MSSID_0, ssid);

            String query = builder.build().getEncodedQuery();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Length", Integer.toString(query.getBytes().length));

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Set setWifiBasic SSID to " + ssid + ", Response is " + response);
            urlConnection.disconnect();
            //Save to share preference
            if (response == HttpURLConnection.HTTP_OK) {
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putString(Def.SP_SSID, ssid);
                editor.commit();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setWifiSecurity(String securityMode, String encryptType, String passPhase) {
        try {
            URL url = new URL(String.format(Def.DVR_Url, Def.wifi_cgi));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!TextUtils.isEmpty(password)) {
                urlConnection.setRequestProperty("Authorization", getAuthorizationHeader());
            }
            String cipher = "0";
            if (TextUtils.equals(encryptType, "AES") || TextUtils.equals(encryptType, "TKIPAES")) {
                cipher = "1";
            }
            String security;
            if (TextUtils.equals(securityMode, "OPEN")) {
                security = "Disable";
            } else {
                security = securityMode;
            }
            String keyRenew = mSharedPref.getString(Def.SP_KEYRENEW, "");
            Uri.Builder builder = mUri.buildUpon()
                    .appendQueryParameter(Def.PAGE, Def.KEY_PAGE_SECURITY)
                    .appendQueryParameter(Def.WLAN_CONF, "2860")
                    .appendQueryParameter(Def.SSIDINDEX, "0")
                    .appendQueryParameter(Def.SECURITY_MODE, security);

            if (!TextUtils.equals(security,"Disable" )) {
                builder.appendQueryParameter(Def.CIPHER, cipher)
                        .appendQueryParameter(Def.PASSPHRASE, passPhase)
                        .appendQueryParameter(Def.KEYRENEWALINTERVAL, keyRenew);
            }
            String query = builder.build().getEncodedQuery();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Length", Integer.toString(query.getBytes().length));

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            int response = urlConnection.getResponseCode();
            Log.i(TAG, "Set setWifiSecurity securityMode to " + securityMode + ", Response is " + response);
            urlConnection.disconnect();
            //Save to share preference
            if (response == HttpURLConnection.HTTP_OK) {
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putString(Def.SP_SECURITY, securityMode);
                editor.putString(Def.SP_PASSPHASE, passPhase);
                editor.putString(Def.SP_ENCRYPTTYPE,encryptType);
                editor.commit();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public boolean isURLReachable(Context context, String Url) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL(Url);   // Change to "http://google.com" for www  test.
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(10 * 1000);          // 10 s.
                urlc.connect();
                int responseCode = urlc.getResponseCode();
                if (responseCode == 200) {        // 200 = "OK" code (http connection is fine).
                    Log.i(TAG, "Connect to "+ Url +" Success !");
                    return true;
                } else {
                    Log.i(TAG, "Connect to " + Url + " Fail ! Response code is " + responseCode);
                    return false;
                }
            } catch (MalformedURLException e1) {
                return false;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }
    
    public static boolean downloadFileFromURL(String Url, OutputStream fileSaveTo) {
    	URL url;
		try {
			url = new URL(Url);
	    	HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
	    	urlc.connect();
	    	int lengthOfFile = urlc.getContentLength();
	    	InputStream is = new BufferedInputStream(urlc.getInputStream(), 8192);
	    	OutputStream os = fileSaveTo;
	    	byte data[] = new byte[1024];
	    	int count = 0;
	    	long total = 0;

	    	while ((count = is.read(data)) != -1) {
		    	total += count;
		    	// publishing the progress….
		    	int progress = (int)((total*100)/lengthOfFile);
		    	Log.d(TAG, "[downloadFileFromURL] Progress is " + progress + "%, current bytes :" + total);
		    	// writing data to file
		    	os.write(data, 0, count);
	    	}

	    	// flushing output
	    	os.flush();

	    	// closing streams
	    	os.close();
	    	is.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
    }
}
