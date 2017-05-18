package com.camera.simplemjpeg;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class MjpegInputStream extends DataInputStream {
	private final static String TAG = MjpegInputStream.class.getName();
    private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };
    private final byte[] EOF_MARKER = { (byte) 0xFF, (byte) 0xD9 };
    private final String CONTENT_LENGTH = "Content-Length";
    private final static int HEADER_MAX_LENGTH = 100;
    private final static int FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH;
    private int mContentLength = -1;
    private int headerLen = 0;
    private byte[] header = new byte[HEADER_MAX_LENGTH];
    private byte[] frameData = new byte[FRAME_MAX_LENGTH];

    public static MjpegInputStream read(String surl) {
    	try {
    		URL url = new URL(surl);
    		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    		return new MjpegInputStream(urlConnection.getInputStream());
    	}catch(Exception e){}

        return null;
    }
	
    public MjpegInputStream(InputStream in) { super(new BufferedInputStream(in, FRAME_MAX_LENGTH)); }
	
    private int getEndOfSeqeunce(DataInputStream in, byte[] sequence) throws IOException {
        int seqIndex = 0;
        byte c;
        for(int i=0; i < FRAME_MAX_LENGTH; i++) {
            c = (byte) in.readUnsignedByte();
            if(c == sequence[seqIndex]) {
                seqIndex++;
                if(seqIndex == sequence.length) return i + 1;
            } else seqIndex = 0;
        }
        return -1;
    }
	
    private int getStartOfSequence(DataInputStream in, byte[] sequence) throws IOException {
        int end = getEndOfSeqeunce(in, sequence);
        return (end < 0) ? (-1) : (end - sequence.length);
    }

    private int parseContentLength(byte[] headerBytes, int offset, int length) 
    		throws IOException, NumberFormatException, IllegalArgumentException {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes, offset, length);
        Properties props = new Properties();
        props.load(headerIn);
        return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
    }	

    public Bitmap readMjpegFrame() throws IOException {
        mark(FRAME_MAX_LENGTH);
        headerLen = getStartOfSequence(this, SOI_MARKER);
        reset();
        if (headerLen > HEADER_MAX_LENGTH) {
        	header = new byte[headerLen];
        }
        readFully(header, 0, headerLen);
        try {
            mContentLength = parseContentLength(header, 0, headerLen);
        } catch (NumberFormatException nfe) { 
        	Log.d(TAG,"NumberFormatException in parseContentLength");
            mContentLength = getEndOfSeqeunce(this, EOF_MARKER); 
        } catch (IllegalArgumentException e) { 
        	Log.d(TAG,"IllegalArgumentException in parseContentLength");
        	mContentLength = getEndOfSeqeunceSimplified(this, EOF_MARKER); 
        	
        	if (mContentLength < 0){
        		Log.d(TAG,"Worst case for finding EOF_MARKER");
        		reset();
        		mContentLength = getEndOfSeqeunce(this, EOF_MARKER); 
        	}
        } catch (IOException e) { 
			Log.d(TAG, "IOException in parseContentLength");
			reset();
			return null;
		}

        reset();
        if (mContentLength > FRAME_MAX_LENGTH) {
        	frameData = new byte[mContentLength];
        }	
        skipBytes(headerLen);
        readFully(frameData, 0, mContentLength); 
    	//pixeltobmp(frameData, mContentLength, bmp);
    	//return bmp;
        return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData, 0, mContentLength));
    }
        
	private int getEndOfSeqeunceSimplified(DataInputStream in, byte[] sequence) throws IOException {
		int startPos = mContentLength / 2;
		int endPos = 3 * mContentLength / 2;

		skipBytes(headerLen + startPos);

		int seqIndex = 0;
		byte c;
		for (int i = 0; i < endPos - startPos; i++) {
			c = (byte) in.readUnsignedByte();
			if (c == sequence[seqIndex]) {
				seqIndex++;
				if (seqIndex == sequence.length) {

					return headerLen + startPos + i + 1;
				}
			} else
				seqIndex = 0;
		}
		return -1;
	}

}