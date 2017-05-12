package com.liteon.iview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.camera.simplemjpeg.MjpegInputStream;
import com.camera.simplemjpeg.MjpegView;
import com.liteon.iview.service.DvrInfoService;
import com.liteon.iview.util.Def;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Preview extends Activity {

	private static final String TAG = Preview.class.getName();
	private MjpegView mv;
    private ImageView mSnapshot;
    private ImageView mThumbnail;
    private View mToolbar;
    private View mBottomBar;
    private ImageView mRecordings;
    private ImageView mSettings;
    private ImageView mPreview;
    private boolean mShowingMenu;
    private Handler mHandlerTime;
    private boolean mIsSuspend;
	private TextView mTitleView;
	private ImageView mCamera1;
	private ImageView mCamera2;
	private View mCameraloadingIndicator;
	private View mMenuLoadingIndicator;
	private Uri mCurrentSnapShotUri;
	private Animation animToolbar;
	private Animation animBottom;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
		findViews();
		setListener();
		new Preview.ReadDVR().execute(Def.getPreviewURL());
		mHandlerTime = new Handler();
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mShowingMenu = true;
    	setMenuVisible(true);
		mPreview.setSelected(true);
        IntentFilter intentFilter = new IntentFilter(Def.ACTION_GET_CAM_MODE);
        intentFilter.addAction(Def.ACTION_GET_SYS_MODE);
        registerReceiver(mBroadcastReceiver, intentFilter);
        resumeMJpegVideo();
        checkSystemMode();
        checkCameraStatus();
    }
    
    private void checkSystemMode() {
    	SharedPreferences sp = getSharedPreferences(Def.SHARE_PREFERENCE, Context.MODE_PRIVATE);
    	String system_mode = sp.getString(Def.SP_SYSTEM_MODE, Def.RECORDING_MODE);
        if (!TextUtils.equals(system_mode, Def.RECORDING_MODE)) {
        	mMenuLoadingIndicator.setVisibility(View.VISIBLE);
    		Intent intent = new Intent(getApplicationContext(), DvrInfoService.class);
    		intent.setAction(Def.ACTION_SET_SYS_MODE);
    		intent.putExtra(Def.EXTRA_SET_SYS_MODE, Def.RECORDING_MODE);
    		startService(intent);
        }
    }
    
    private void checkCameraStatus() {
    	
    	SharedPreferences sp = getSharedPreferences(Def.SHARE_PREFERENCE, Context.MODE_PRIVATE);
        String recordingChannel = sp.getString(Def.SP_RECORDING_CAMERA, Def.FRONT_REAR_CAM_MODE);
        String previewChannel = sp.getString(Def.SP_PREVIEW_CAMERA, Def.FRONT_CAM_MODE);
        //if recording channel is not front + rear, disable switch button
        if (!TextUtils.equals(recordingChannel, Def.FRONT_REAR_CAM_MODE)) {
        	mCamera1.setEnabled(false);
        	mCamera2.setEnabled(false);
        } else {
        	mCamera1.setEnabled(true);
        	mCamera2.setEnabled(true);
        }
        mCamera1.setVisibility(View.INVISIBLE);
        mCamera2.setVisibility(View.INVISIBLE);
        if (TextUtils.equals(previewChannel, Def.FRONT_CAM_MODE)) {
        	mTitleView.setText("Camera 1");
        	mCamera2.setVisibility(View.VISIBLE);
        } else {
        	mTitleView.setText("Camera 2");
        	mCamera1.setVisibility(View.VISIBLE);
        }
	}

	@Override
    protected void onPause() {
    	super.onPause();
    	pauseMJpegVideo();
        unregisterReceiver(mBroadcastReceiver);
    }
    
    public void onDestroy() {
    	
    	if (mv!=null){
    		mv.freeCameraMemory();
    	}
        super.onDestroy();
    }

    @Override
    protected void onStart() {
    	super.onStart();
    	mHandlerTime.postDelayed(HideUIControl, 1500);
    }
    
    private void pauseMJpegVideo() {
        if (mv != null) {
        	if(mv.isStreaming()) {
		        mv.stopPlayback();
		        mIsSuspend = true;
        	}
        }
    }
    
    private void resumeMJpegVideo() {
        if (mv != null){
        	if (mIsSuspend) {
        		new Preview.ReadDVR().execute(Def.getPreviewURL());
        		mIsSuspend = false;
        	}
        }
    }
    
    public void setListener() {

        mv.setOnClickListener(mOnClickListener);
        mSnapshot.setOnClickListener(mOnSnapshotClickListener);
        mThumbnail.setOnClickListener(mOnThumbnailClickListener);
        mRecordings.setOnClickListener(mOnRecordingsClickListener);
        mSettings.setOnClickListener(mOnSettingsClickListener);
        mCamera1.setOnClickListener(mOnCameraClickListener);
        mCamera2.setOnClickListener(mOnCameraClickListener);
    }
    
	private void findViews() {
		mv = (MjpegView) findViewById(R.id.preview);
        mSnapshot = (ImageView) findViewById(R.id.snapshot);
        mThumbnail = (ImageView) findViewById(R.id.thumbnail);
        mToolbar = findViewById(R.id.toolbar_preview);
        mBottomBar = findViewById(R.id.bottombar);
        mPreview = (ImageView) findViewById(R.id.preview_icon);
        mRecordings = (ImageView) findViewById(R.id.recordings_icon);
        mSettings = (ImageView) findViewById(R.id.setting_icon);
        mTitleView = (TextView) findViewById(R.id.toolbar_title);
        mCamera1 = (ImageView) findViewById(R.id.icon_camera_1);
        mCamera2 = (ImageView) findViewById(R.id.icon_camera_2);
        mCameraloadingIndicator = (View) findViewById(R.id.icon_loading);
        mMenuLoadingIndicator = (View) findViewById(R.id.progress);
   }

	public View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleMenu();
        }
    };
    
    private void setMenuVisible(boolean show) {
    	if (show) {
    		mToolbar.setVisibility(View.VISIBLE);
    		mBottomBar.setVisibility(View.VISIBLE);
    	} else {
    		mToolbar.setVisibility(View.GONE);
    		mBottomBar.setVisibility(View.GONE);
    	}
    }
    private void toggleMenu() {
    	if (mShowingMenu) {
    		setMenuVisible(false);
    		mHandlerTime.removeCallbacks(HideUIControl);
    	} else {
    		setMenuVisible(true);
    		mHandlerTime.postDelayed(HideUIControl, 1500);
    	}
    	mShowingMenu = !mShowingMenu;
    }

	public View.OnClickListener mOnRecordingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	mPreview.setSelected(false);
        	mRecordings.setSelected(true);
        	Intent intent = new Intent(getApplicationContext(), Records.class);
    		startActivity(intent);
    		finish();
    	}
    };
    
	public View.OnClickListener mOnSettingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	mPreview.setSelected(false);
        	mSettings.setSelected(true);
        	Intent intent = new Intent(getApplicationContext(), Settings.class);
    		startActivity(intent);
    		finish();
    	}
    };
    
    private View.OnClickListener mOnSnapshotClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
        	
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    String uri = saveImageToGallery(Preview.this, snapShot());
                    ContentResolver cr = getContentResolver();
                    mCurrentSnapShotUri = Uri.parse(uri);
                    long id = ContentUris.parseId(mCurrentSnapShotUri);
                    final Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mThumbnail.setImageBitmap(miniThumb);
                            mThumbnail.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }.start();
        }
    };
    
    private View.OnClickListener mOnThumbnailClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			startActivity(new Intent(Intent.ACTION_VIEW, mCurrentSnapShotUri));
		}
    };
    
    private View.OnClickListener mOnCameraClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        	pauseMJpegVideo();
            final String mode;
            mCameraloadingIndicator.setVisibility(View.VISIBLE);
            
            switch (view.getId()) {
                case R.id.icon_camera_1:
                    mode = Def.FRONT_CAM_MODE;
                    mCamera1.setVisibility(View.INVISIBLE);
                    mTitleView.setText("Camera 1");
                    break;
                case R.id.icon_camera_2:
                    mode = Def.REAR_CAM_MODE;
                    mCamera2.setVisibility(View.INVISIBLE);
                    mTitleView.setText("Camera 2");
                    break;
                default:
                    mode = Def.FRONT_CAM_MODE;
                    break;
            }
            //set DVR camera 
    		Intent intent = new Intent(getApplicationContext(), DvrInfoService.class);
    		intent.setAction(Def.ACTION_SET_CAM_MODE);
    		intent.putExtra(Def.EXTRA_SET_CAM_MODE, mode);
    		startService(intent);
    		resumeMJpegVideo();
        }
    };

    private Bitmap snapShot() {
        return mv.getBitmap();
    }
    
    public Runnable HideUIControl = new Runnable()
    {
        public void run() {

            if (mShowingMenu) {
                mBottomBar.setVisibility(View.GONE);
                if (animToolbar == null) {
                	animToolbar = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.toolbar_hide);
                	animToolbar.setAnimationListener(mToolbarAnimation);
                }
                
                mToolbar.startAnimation(animToolbar);

                if (animBottom == null) {
                	animBottom = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_hide);
                	animBottom.setAnimationListener(mBottomBarAnimation);
                }
                mBottomBar.startAnimation(animBottom); 
            }
        }
    };
    
    private Animation.AnimationListener mToolbarAnimation = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        	mToolbar.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private Animation.AnimationListener mBottomBarAnimation = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
        	mBottomBar.setVisibility(View.GONE);
        	mShowingMenu = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
    
    public class ReadDVR extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {

            try {
                URL _url = new URL(url[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) _url.openConnection();
                InputStream is = urlConnection.getInputStream();
                if (is == null) {
                    return null;
                }
                return MjpegInputStream.read(is);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            if (result != null) {
                mv.setSource(result);
                //result.setSkip(3);
                mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
                //mv.showFps(true);
            } else {
                Toast.makeText(getApplicationContext(),"Fail to open preview URL", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

 		@Override
         public void onReceive(Context context, Intent intent) {
 			if (TextUtils.equals(intent.getAction(), Def.ACTION_GET_CAM_MODE)) {
 				mCameraloadingIndicator.setVisibility(View.INVISIBLE);
 				checkCameraStatus();
 			} else if (TextUtils.equals(intent.getAction(), Def.ACTION_GET_SYS_MODE)) {
 				recreate();
 			}

 		}
 	};
 	
 	public static String saveImageToGallery(Context context, Bitmap bmp) {
 	    
 		String uri = "";
        // Get the directory for the user's public pictures directory.
        File appDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), context.getString(R.string.app_name));

 	    if (!appDir.exists()) {
 	    	appDir.mkdir();
 	    }
 	    String fileName = System.currentTimeMillis() + ".jpg";
 	    File file = new File(appDir, fileName);
 	    try {
 	        FileOutputStream fos = new FileOutputStream(file);
 	        bmp.compress(CompressFormat.JPEG, 100, fos);
 	        fos.flush();
 	        fos.close();
 	        
 	    } catch (FileNotFoundException e) {
 	        e.printStackTrace();
 	    } catch (IOException e) {
 	        e.printStackTrace();
 		}
 	    try {
 	    	uri = MediaStore.Images.Media.insertImage(context.getContentResolver(),
 					file.getAbsolutePath(), fileName, null);
 	    } catch (FileNotFoundException e) {
 	        e.printStackTrace();
 	    }
 	    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
 	    return uri;
 	}
 	
}
