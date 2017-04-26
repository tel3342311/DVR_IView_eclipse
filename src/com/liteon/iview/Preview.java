package com.liteon.iview;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.camera.simplemjpeg.MjpegInputStream;
import com.camera.simplemjpeg.MjpegView;
import com.liteon.iview.util.Def;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
		findViews();
		setListener();
		new Preview.ReadDVR().execute(Def.DVR_PREVIEW_URL);
		mHandlerTime = new Handler();
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mShowingMenu = true;
    	setMenuVisible(true);
		mPreview.setSelected(true);
        if(mv!=null){
        	if(mIsSuspend){
        		new Preview.ReadDVR().execute(Def.DVR_PREVIEW_URL);
        		mIsSuspend = false;
        	}
        }
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
        if(mv!=null){
        	if(mv.isStreaming()){
		        mv.stopPlayback();
		        mIsSuspend = true;
        	}
        }
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
    
    public void setListener() {

        mv.setOnClickListener(mOnClickListener);
        mSnapshot.setOnClickListener(mOnSnapshotClickListener);
        mRecordings.setOnClickListener(mOnRecordingsClickListener);
        mSettings.setOnClickListener(mOnSettingsClickListener);
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
    		mToolbar.setVisibility(View.INVISIBLE);
    		mBottomBar.setVisibility(View.INVISIBLE);
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
        	Intent intent = new Intent(getApplicationContext(), Records.class);
    		startActivity(intent);        
    	}
    };
    
	public View.OnClickListener mOnSettingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        	Intent intent = new Intent(getApplicationContext(), Settings.class);
    		startActivity(intent);        
    	}
    };
    
    private View.OnClickListener mOnSnapshotClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    ContentResolver cr = getContentResolver();
                    String uri = MediaStore.Images.Media.insertImage(cr, snapShot(), "", "" );
                    Log.d(TAG, "The URI of insert image is " + uri);
                    if (uri == null) {
                        return ;
                    }
                    long id = ContentUris.parseId(android.net.Uri.parse(uri));
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

    private Bitmap snapShot() {
        return mv.getBitmap();
    }
    
    public Runnable HideUIControl = new Runnable()
    {
        public void run() {

            if (mShowingMenu) {
                mBottomBar.setVisibility(View.GONE);

                Animation animToolbar = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.toolbar_hide);
                animToolbar.setAnimationListener(mToolbarAnimation);
                mToolbar.startAnimation(animToolbar);


                Animation animBottom = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_hide);
                animBottom.setAnimationListener(mBottomBarAnimation);
                mBottomBar.startAnimation(animBottom);                
            }
        }
    };
    
    private Animation.AnimationListener mToolbarAnimation = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        	mToolbar.setVisibility(View.INVISIBLE);
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
        	mBottomBar.setVisibility(View.INVISIBLE);
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
                //result.setSkip(1);
                mv.setDisplayMode(MjpegView.SIZE_FULLSCREEN);
                mv.showFps(true);
            } else {
                Toast.makeText(getApplicationContext(),"Fail to open preview URL", Toast.LENGTH_LONG).show();
            }
        }
    }
}
