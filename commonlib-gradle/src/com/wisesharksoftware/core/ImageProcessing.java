package com.wisesharksoftware.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import com.flurry.android.FlurryAgent;

import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.camera.AppSettings;
import com.wisesharksoftware.core.opencv.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageProcessing
{
    private final static String LOG_TAG = "ImageProcessing";
    
    private Context context;
    private Preset cameraPreset;
    private Preset processingPreset;
    private Preset watermarkPreset;
    private ProcessingCallback callback;
    private ProcessingTask processingTask;
    private List<String> inFiles;
    private String outFile;
    private boolean processPreview;
    private boolean cancellable = false;

    private final boolean useCamera;
    private final boolean useProcessing;
    private final boolean useOpenCVFilters;
    private final boolean hdEnabled;
    
    private final int maxWidth;
    private final int maxHeight;
    
    private String presetsJson = "";
    public boolean tagDone = false;
    
    public ImageProcessing( Context context, Preset cameraPreset, Preset processingPreset, ProcessingCallback callback )
    {
    	this(context, cameraPreset, processingPreset, null, true, true,
    			AppSettings.getWidth(context, AppSettings.getHdEnabled(context)),
    			AppSettings.getHeight(context, AppSettings.getHdEnabled(context)),
    			callback);
    	Log.d(LOG_TAG, "ImageProcessing");
    }

    public ImageProcessing( Context context, Preset cameraPreset, Preset processingPreset, int maxWidth, int maxHeight, ProcessingCallback callback )
    {
    	this(context, cameraPreset, processingPreset, null, true, true,	maxWidth, maxHeight, callback);
    	Log.d(LOG_TAG, "ImageProcessing");
    }

    public ImageProcessing( Context context, String presetsJson, int maxWidth, int maxHeight, ProcessingCallback callback )
    {
    	this(context, null, null, null, false, false, maxWidth, maxHeight, callback);
    	this.presetsJson = presetsJson;
    	Log.d(LOG_TAG, "ImageProcessing");
    }

    public ImageProcessing( Context context,
                            Preset cameraPreset,
                            Preset processingPreset, Preset watermarkPreset, boolean useCamera, boolean useProcessing,
                            int maxWidth, int maxHeight, 
                            ProcessingCallback callback )
    {
        this.context = context;
        this.cameraPreset = cameraPreset;
        this.processingPreset = processingPreset;
        this.callback = callback;
        this.useCamera = useCamera;
        this.useProcessing = useProcessing;
        this.watermarkPreset = watermarkPreset;
        this.useOpenCVFilters = AppSettings.getOpenCvEnabled(context);
        hdEnabled = AppSettings.getHdEnabled(context);
        processPreview = false;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        Log.d(LOG_TAG, "ImageProcessing");
    }

    public void processPictureAsync(List<String> inFiles, String outFile)
    {
    	processPictureAsync(inFiles, outFile, false);
    }

    public void processPictureAsync(List<String> inFiles, String outFile, boolean processPreview)
    {
        this.inFiles = inFiles;
        this.outFile = outFile;
        this.processPreview = processPreview;
        callback.onStart();
        if( processingTask != null )
        {
            processingTask.cancel( true );
        }
        processingTask = null;
        synchronized( context )
        {
            processingTask = new ProcessingTask( callback , outFile);
            processingTask.execute( this );
        }
    }

    public void cancel() {
    	try {
    		Log.w(LOG_TAG, "Cancel processing");
    		if (processingTask != null && !processingTask.isCancelled()) {
    			cancelProcessing();
    			processingTask.cancel(true);
    			FlurryAgent.logEvent("CancelProcessing");
    		} else {
    			if (processingTask == null) {
    	    		Log.w(LOG_TAG, "Cancel processing: processing task is null");
    			}
    			if (processingTask.isCancelled()) {
    				Log.w(LOG_TAG, "Cancel processing: processing task is cancelled");
    			}
    		}
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		new ExceptionHandler(e, "CancelProcessingError");
    	}
    }

    
    public void processPictureSync( String inFile, String outFile)
    {
        this.inFiles = new ArrayList<String>();
        inFiles.add( inFile );
        this.outFile = outFile;
        processPicture();
    }

    public void processPictures( int picturesCount ) throws IOException
    {
        Image2 modifiedImage = null;
        boolean isSquare = false;

        ArrayList<Preset> processings = new ArrayList<Preset>();
        processings.add( processingPreset );

        Boolean hd = false;
        
        String fileName = outFile;
        for( int idx = 0; idx < picturesCount; ++idx )
        {
        	modifiedImage = null;
            Bitmap original = Utils.loadBitmap( new File( inFiles.get( idx ) ), picturesCount > 1, isSquare, false );
            boolean isPortraitPhoto = original.getWidth() < original.getHeight();
            modifiedImage = Image2.fromBitmap( original );
            original.recycle();
            System.gc();
            
            if( cameraPreset.getzIndex() > processingPreset.getzIndex() )
            {
                if( idx == 0 )
                {
                    callback.onBitmapCreated( modifiedImage );
                }
                for( Preset p : processings )
                {
                    p.process( modifiedImage, context, false, isPortraitPhoto, hd, null, null );
                    callback.onBitmapCreated( modifiedImage );
                }
            }
            else
            {
                fileName = null;
            }
            cameraPreset.process( modifiedImage, context, false, isPortraitPhoto, hd, fileName, null );
        }

        if( cameraPreset.getzIndex() <= processingPreset.getzIndex() )
        {
        	boolean isPortraitPhoto = modifiedImage.width < modifiedImage.height;
            callback.onBitmapCreated( modifiedImage );
            for( Preset p : processings )
            {
                p.process( modifiedImage, context, modifiedImage.height == modifiedImage.width, isPortraitPhoto, hd, null, null );
                callback.onBitmapCreated( modifiedImage );
            }
            try
            {
                System.gc();
                Bitmap bitmap = modifiedImage.toBitmap();
                modifiedImage = null;
                Utils.saveBitmapJpeg( bitmap, new File( outFile ), true );
                bitmap.recycle();
            }
            catch( IOException e )
            {
                callback.onFail( e );
                e.printStackTrace();
            }
        }

        modifiedImage = null;
    }
    
    private static native void processPictureOpenCV(String inFileName, String outFileName, String origFileName, int filterW, int filterH, int reqW, int reqH);
    //private static native void processBarrel(String inFileName, String outFileName, int filterW, int filterH, int reqW, int reqH);
    private static native void processBarrelOpenCV(String inFileName, String outFileName, boolean convex, int filterW, int filterH, int reqW, int reqH);

    //private static native void fullProcessOpenCV(Object[] inFiles, String outFileName, String resourcePath , Object[] presets, int maxWidth, int maxHeight);

    private static native void fullProcessOpenCV(Object[] inFiles, String outFileName, String resourcePath , Object[] presets, String presetsJson, int maxWidth, int maxHeight, boolean cancellable);
    
    private static native void cancelProcessing();

    public static native double[] detectSheetCorners(String inFileName);
    
    public static native void combinePhotos(String inFileName1, String inFileName2, String outFileName, int algorithm, int alpha);

    @SuppressLint("NewApi")
	public void processPictureOpenCV() 
    {
        try
        {
        	Log.d(LOG_TAG, "processPictureOpenCV!");
        	if (cameraPreset != null) {
        		Log.d(LOG_TAG, cameraPreset.getName());
        	}

        	if( inFiles == null || inFiles.size() == 0 || outFile == null ) {
        	  Log.d( LOG_TAG, "nothing to process" );
            callback.onFail( new Exception( "input or output paths not specified" ) );
            return;
          }
          FlurryAgent.onEvent( "ProcessPhoto:Process" );
//          int picturesCount = useCamera ? cameraPreset.getPicturesCount() : 1;
//          if( picturesCount > 1 ) {
//              processPictures( picturesCount );
//              return;
//          }
          ArrayList<Preset> processings = new ArrayList<Preset>();
          if (useCamera && cameraPreset != null) {
          	processings.add( cameraPreset );
          }
          if (useProcessing && processingPreset != null) {
          	processings.add( processingPreset );
          }
//          if( useCamera && useProcessing && (cameraPreset.getzIndex() > processingPreset.getzIndex()) ) {
//              Collections.reverse( processings );
//          }
//          if (watermarkPreset != null) {
//          	processings.add(watermarkPreset);
//          }
          String [] presets = new String[processings.size()];
          for (int i = 0; i < processings.size(); ++i) {
        	  presets[i] = processings.get(i).getName();
          }
          
          String basePath = context.getExternalFilesDir(null) + "/assets/";
          String [] files = new String[inFiles.size()];
          inFiles.toArray(files);
          fullProcessOpenCV(files, outFile, basePath, (presetsJson != null && presetsJson.length() > 0) ? null : presets, presetsJson, maxWidth, maxHeight, cancellable);
        } catch (Exception e) {
        	e.printStackTrace();
            callback.onFail( e );
            new ExceptionHandler( e, "ProcessPictureOpenCV" );
        }
    	
    }
    
    public void setPresetsJson(String presetsJson) {
    	this.presetsJson = presetsJson;
    }

    public void processPicture()
    {
        try
        {
        	Log.d(LOG_TAG, "processPicture!");
        	Log.d(LOG_TAG, cameraPreset.getName());

        	if( inFiles == null || inFiles.size() == 0 || outFile == null ) {
        	  Log.d( LOG_TAG, "nothing to process" );
            callback.onFail( new Exception( "input or output paths not specified" ) );
            return;
          }
          FlurryAgent.onEvent( "ProcessPhoto:Process" );
          int picturesCount = useCamera ? cameraPreset.getPicturesCount() : 1;
          boolean isSquare = cameraPreset.isSquare();
          if( picturesCount > 1 ) {
              processPictures( picturesCount );
              return;
          }

          boolean isHd = false;
          BitmapFactory.Options options = new BitmapFactory.Options();
          options.inJustDecodeBounds = true;
          BitmapFactory.decodeFile(inFiles.get(0), options);

          int height = options.outHeight;
          int width = options.outWidth;
          if (hdEnabled) {
            isHd = AppSettings.isHdImage(context, width, height);
          }
          if (cameraPreset.getName().equals("Fish Eye")) {
            Log.d(LOG_TAG, "inFiles" + inFiles.get( 0 ));
            if (AppSettings.getPreviewEnabled(context) && processPreview) {
            	int previewWidth = AppSettings.getPreviewWidth(context);
            	int previewHeight = AppSettings.getPreviewHeight(context);
              	processPictureOpenCV(inFiles.get( 0 ), inFiles.get( 1 ), inFiles.get( 2 ), previewWidth * 2, previewHeight * 2, previewWidth, previewHeight);
              	//return;
            } else {
            	processPictureOpenCV(inFiles.get( 0 ), inFiles.get( 1 ), inFiles.get( 2 ),
          					isHd ? 2400 : width, isHd ? 2400 : height,
          					isHd ? 1200 : width / 2, isHd ? 1200 : height / 2);
            }
          	inFiles.add( 0 ,inFiles.get( 1 ));
          	//return;
          }
            
           if (cameraPreset.getName().equals("Barrel") || cameraPreset.getName().equals("BarrelConvex")){
             boolean convex = false;
        	   if (cameraPreset.getName().equals("BarrelConvex")){
        		   convex = true;
        	   }
        	   Log.d(LOG_TAG, "inFiles" + inFiles.get( 0 ));
        	   Log.d(LOG_TAG, "inFiles" + inFiles.get( 1 ));
        	   processBarrelOpenCV(inFiles.get( 0 ), inFiles.get( 1 ), convex,
                        	       isHd ? 2400 : width, isHd ? 2400 : height,
                                 isHd ? 1200 : width / 2, isHd ? 1200 : height/2);
        	   inFiles.add( 0 ,inFiles.get( 1 ));
           }
            
            ArrayList<Preset> processings = new ArrayList<Preset>();
            if (useCamera) {
            	processings.add( cameraPreset );
            }
            if (useProcessing) {
            	processings.add( processingPreset );
            }

            if( useCamera && useProcessing && (cameraPreset.getzIndex() > processingPreset.getzIndex()) )
            {
                Collections.reverse( processings );
            }
            
            if (watermarkPreset != null) {
            	processings.add(watermarkPreset);
            }

            if (this.useOpenCVFilters) {
              processImageOpenCV(processings, picturesCount, callback);
            } else {
              processImageNotOpenCV(processings, picturesCount, isSquare, callback);
            }
            
        }
        catch( Exception e )
        {
        	e.printStackTrace();
            callback.onFail( e );
            new ExceptionHandler( e, "ProcessPicture" );
        }
    }

    private void processImageOpenCV(ArrayList<Preset> processings, int picturesCount, ProcessingCallback callback) {
      boolean isFirst = true;
      String inFile = inFiles.get(0);

      // get information about input image
      for( Preset p : processings )
      {
        p.processOpenCV(context, inFile, outFile);
        if (isFirst) {
          isFirst = false;
          inFile = outFile;
        }
      }
      callback.onBitmapCreatedOpenCV();
      return;
    }
    
    private void processImageNotOpenCV(ArrayList<Preset> processings, int picturesCount, Boolean isSquare, ProcessingCallback callback) {
      try {
        Bitmap original;
        original = Utils.loadBitmap( new File( inFiles.get( 0 ) ), picturesCount > 1, isSquare, false );
        boolean isPortraitPhoto = original.getWidth() < original.getHeight();

        Image2 modifiedImage = null;
        if( ( !cameraPreset.useNativeProcessing() ) || ( !processingPreset.useNativeProcessing() ) )
        {
            System.gc();
            modifiedImage = Image2.fromBitmap( original );
            original.recycle();
            callback.onBitmapCreated( modifiedImage );
            original = null;
        }
        Boolean hd = false;
        for( Preset p : processings )
        {
            p.process( modifiedImage, context, isSquare, isPortraitPhoto, hd, outFile, original );
            callback.onBitmapCreated( original );
        }
                    
        if( outFile != null )
        {
            try
            {
                if( original != null )
                {
                    Utils.saveBitmapJpeg( original, new File( outFile ), true );
                    original.recycle();
                }
                else if( modifiedImage != null )
                {
                    System.gc();
                    Bitmap modifiedBitmap = modifiedImage.toBitmap();modifiedImage = null;
                    Utils.saveBitmapJpeg( modifiedBitmap, new File( outFile ), true );
                    modifiedBitmap.recycle();
                }
            }
            catch( IOException e )
            {
                callback.onFail( e );
                e.printStackTrace();
            }
        } 
      } catch( Exception e ) {
        e.printStackTrace();
          callback.onFail( e );
          new ExceptionHandler( e, "ProcessPicture" );
      }
    }
    
    public void setCancellable(boolean cancellable){
    	this.cancellable = cancellable;
    }
    
    private class ProcessingTask extends AsyncTask<ImageProcessing, Integer, Integer>
    {
        Throwable th;
        ProcessingCallback callback;
        String outFileName;

        public ProcessingTask( ProcessingCallback callback, String outFileName )
        {
            this.callback = callback;
            this.outFileName = outFileName;
        }

        @Override
        protected Integer doInBackground( ImageProcessing... params )
        {
            Log.d( LOG_TAG, "doInBackground" );
            try
            {
                //params[ 0 ].processPicture();
                params[ 0 ].processPictureOpenCV();
            }
            catch(UnsatisfiedLinkError e) 
            {
            	FlurryAgent.logEvent("UnsatisfiedLinkError");
        	    if (!OpenCVLoader.initDebug()) {
        	    	Utils.reportFlurryEvent("OpenCVLoaderReload", "ERROR");
        	    } else {
        	    	Utils.reportFlurryEvent("OpenCVLoaderReload", "OK");
        	    }
            	try {
            		System.loadLibrary("processing");
        	    	Utils.reportFlurryEvent("LoadLibraryReload", "OK");
            	} catch (Error error) {
        	    	Utils.reportFlurryEvent("LoadLibraryReload", "ERROR");
            		e.printStackTrace();
            		new ExceptionHandler(e, "LoadLibraryReload");
            	}
            	this.th = e;
            }
            catch (Exception e) {
            	e.printStackTrace();
            	this.th = e;
            }
            catch( Throwable th )
            {
                this.th = th;
            }
            return 0;
        }

        @Override
        protected void onPreExecute()
        {
            Log.d( LOG_TAG, "onPreExecute" );
        }

        @Override
        protected void onPostExecute( Integer result )
        {
            Log.d( LOG_TAG, "onPostExecute" );
            tagDone = true;
            if( th != null )
            {
                callback.onFail( th );
            }
            else
            {
                callback.onSuccess(outFileName);
            }
        }
        
        @Override
        protected void onCancelled( ) {
            Log.e( LOG_TAG, "onCancelled" );
        	if (callback != null) {
        		callback.onCancelled();
        	}
        }
        
    }
}
