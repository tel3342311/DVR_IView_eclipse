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
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class Preview extends Activity {

	private static final String TAG = Preview.class.getName();
	private MjpegView mv;
    private ImageView mSnapshot;
    private ImageView mThumbnail;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
		findViews();
		setListener();
		new Preview.ReadDVR().execute(Def.DVR_PREVIEW_URL);
	}
    
    public void setListener() {

        mv.setOnClickListener(mOnClickListener);
        mSnapshot.setOnClickListener(mOnSnapshotClickListener);
    }
	private void findViews() {
		mv = (MjpegView) findViewById(R.id.preview);
        mSnapshot = (ImageView) findViewById(R.id.snapshot);
        mThumbnail = (ImageView) findViewById(R.id.thumbnail);
   }

	public View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //((MainActivity)getActivity()).toggleMenu();
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
