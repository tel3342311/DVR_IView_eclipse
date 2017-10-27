package com.camera.simplemjpeg;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.MathContext;

import com.liteon.iview.Preview;
import com.liteon.iview.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {
	private final static String TAG = MjpegView.class.getName();
    public final static int POSITION_UPPER_LEFT  = 9;
    public final static int POSITION_UPPER_RIGHT = 3;
    public final static int POSITION_LOWER_LEFT  = 12;
    public final static int POSITION_LOWER_RIGHT = 6;

    public final static int SIZE_STANDARD   = 1; 
    public final static int SIZE_BEST_FIT   = 4;
    public final static int SIZE_FULLSCREEN = 8;
    
    private MjpegViewThread thread;
    private MjpegInputStream mIn = null;    
    private boolean showFps = false;
    private boolean mRun = false;
    private boolean surfaceDone = false;    
    private Paint overlayPaint;
    private int overlayTextColor;
    private int overlayBackgroundColor;
    private int ovlPos;
    private int dispWidth;
    private int dispHeight;
    private int displayMode;
	private boolean suspending = false;
	private Bitmap mBitmap;
    private SurfaceHolder holder;
	
    private final class MjpegViewThread extends Thread {
        private final WeakReference<SurfaceHolder> mSurfaceHolder;
        private int frameCounter = 0;
        private long start;
        private Bitmap ovl;
        private Bitmap warning;
        private boolean isUnstable;
        private final static int WARNING_HEIGHT = 90;
        private final static int WARNING_FPS_THRESHOLD = 5;
        private final WeakReference<Context> mContext; 
        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) { 
        	mSurfaceHolder = new WeakReference<SurfaceHolder>(surfaceHolder);
        	mContext = new WeakReference<Context>(context);
        }

        private Rect destRect(int bmw, int bmh) {
            int tempx;
            int tempy;
            if (displayMode == MjpegView.SIZE_BEST_FIT) {
                float bmasp = (float) bmw / (float) bmh;
                bmw = dispWidth;
                bmh = (int) (dispWidth / bmasp);
                if (bmh > dispHeight) {
                    bmh = dispHeight;
                    bmw = (int) (dispHeight * bmasp);
                }
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            } else if (displayMode == MjpegView.SIZE_STANDARD) {
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            } else if (displayMode == MjpegView.SIZE_FULLSCREEN) {
            	return new Rect(0, 0, dispWidth, dispHeight);
            }
            return null;
        }
         
        public void setSurfaceSize(int width, int height) {
            synchronized(mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;
            }
        }
         
        private Bitmap makeFpsOverlay(Paint p, String text) {
            Rect b = new Rect();
            p.getTextBounds(text, 0, text.length(), b);
            int bwidth  = b.width()+4;
            int bheight = WARNING_HEIGHT;//b.height()+4;
            Bitmap bm = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ALPHA_8);
            Canvas c = new Canvas(bm);
            c.drawText(text, 0, (WARNING_HEIGHT / 2) - ((p.ascent()+p.descent())/2) + 1, p);
            return bm;        	 
        }

        public void run() {
        	ovl = makeFpsOverlay(overlayPaint, "Signal Unstable");
        	warning = BitmapFactory.decodeResource(getResources(),R.drawable.setting_img_warning);
    		warning = Bitmap.createScaledBitmap(warning, WARNING_HEIGHT, WARNING_HEIGHT, true);
            start = System.currentTimeMillis();
            PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
            Bitmap bm;
            int width;
            int height;
            Rect destRect;
            Canvas c = null;
            Paint p = new Paint();
            while (mRun) {
                if(surfaceDone && mSurfaceHolder.get() != null) {
                    try {
                        c = mSurfaceHolder.get().lockCanvas();
                        if (c == null) {
                        	continue;
                        }
                        synchronized (mSurfaceHolder.get()) {
                            try {
                                bm = mIn.readMjpegFrame();
                                mBitmap = bm;
                                if (bm == null) {
                                	if (mContext.get() != null) {
	                                	((Activity)mContext.get()).runOnUiThread(new Runnable() {
											
											@Override
											public void run() {
												((Activity)mContext.get()).recreate();
											}
										});
                                	}
                                	return;
                                }
                                destRect = destRect(bm.getWidth(),bm.getHeight());
                                c.drawColor(Color.BLACK);
                                c.drawBitmap(bm, null, destRect, p);
                                if(showFps) {
                                    p.setXfermode(mode);
                                    if(isUnstable) {
                                    	height = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom - ovl.getHeight();
                                    	width  = ((ovlPos & 8) == 8) ? destRect.left : destRect.right - ovl.getWidth();
                                    	int height2 = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom - ovl.getHeight() - warning.getHeight();
                                    	int width2  = ((ovlPos & 8) == 8) ? destRect.left : destRect.right - ovl.getWidth() - warning.getWidth();
                                        c.drawBitmap(ovl, width, height, null);
                                        c.drawBitmap(warning, width2, height2, null);
                                    }
                                    p.setXfermode(null);
                                    frameCounter++;
                                    if((System.currentTimeMillis() - start) >= 1000) {
                                        start = System.currentTimeMillis();
                                        if (frameCounter <= WARNING_FPS_THRESHOLD) {
                                        	isUnstable = true;
                                        } else {
                                        	isUnstable = false;
                                        }
                                        frameCounter = 0; 
                                    }
                                }
                            } catch (IOException e) {}
                        }
                    } finally { 
                    	if (c != null) mSurfaceHolder.get().unlockCanvasAndPost(c); 
                    }
                }
            }
        }
    }

    private void init(Context context) {
        holder = getHolder();
        holder.addCallback(this);
        thread = new MjpegViewThread(holder, context);
        setFocusable(true);
        overlayPaint = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(48);
        overlayPaint.setTypeface(Typeface.DEFAULT);
        overlayTextColor = Color.BLACK;
        overlayBackgroundColor = Color.TRANSPARENT;
        ovlPos = MjpegView.POSITION_UPPER_RIGHT;
        displayMode = MjpegView.SIZE_STANDARD;
        dispWidth = getWidth();
        dispHeight = getHeight();
        Log.d(TAG,"dispWidth =  " + dispWidth + " dispHeight = " + dispHeight);
        Log.d(TAG,"dispWidth =  " + dispWidth + " dispHeight = " + dispHeight);
    }
    
    public void startPlayback() { 
        if (mIn != null) {
            mRun = true;
            if(thread==null){
            	thread = new MjpegViewThread(holder, getContext());
            }
            thread.start();    		
        }
    }
    
    public void stopPlayback() { 
    	if(mRun){
    		suspending = true;
    	}
        mRun = false;
        if(thread!=null){
        	boolean retry = true;
	        while(retry) {
	            try {
	                thread.join();
	                retry = false;
	            } catch (InterruptedException e) {}
	        }
	        thread = null;
        }
        if(mIn!=null){
	        try{
	        	mIn.close();
	        }catch(IOException e){}
	        mIn = null;
        }

    }

    public void resumePlayback() { 
        if (suspending){
            if(mIn != null) {
                mRun = true;
                holder = getHolder();
                holder.addCallback(this);
                thread = new MjpegViewThread(holder, getContext());		
                thread.start();
                suspending=false;
            }
        }
    }
    
    public void freeCameraMemory(){
    	if (mIn != null){
    		//mIn.freeCameraMemory();
    		mBitmap.recycle();
    		try {
				mIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
    public MjpegView(Context context, AttributeSet attrs) { 
    	super(context, attrs); init(context); 
    	
    }
    
	public boolean isStreaming(){
		return mRun;
	}
	
	public Bitmap getBitmap(){
        if (mBitmap != null) {
            return Bitmap.createBitmap(mBitmap);
        }
        return null;
	}

    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) { 
    	if (thread != null) {
    		thread.setSurfaceSize(w, h); 
    	}
    }
    public void surfaceDestroyed(SurfaceHolder holder) { 
        surfaceDone = false; 
        stopPlayback(); 
    }
    
    public MjpegView(Context context) { super(context); init(context); }    
    public void surfaceCreated(SurfaceHolder holder) { surfaceDone = true; }
    public void showFps(boolean b) { showFps = b; }
    public void setSource(MjpegInputStream source) {
    	mIn = source; 
    	if (!suspending) {
    		startPlayback();
    	} else {
    		resumePlayback();
    	}
    }
    public void setOverlayPaint(Paint p) { overlayPaint = p; }
    public void setOverlayTextColor(int c) { overlayTextColor = c; }
    public void setOverlayBackgroundColor(int c) { overlayBackgroundColor = c; }
    public void setOverlayPosition(int p) { ovlPos = p; }
    public void setDisplayMode(int s) { displayMode = s; }
}