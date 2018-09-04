package com.wisesharksoftware.core.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.StatFs;
import com.smsbackupandroid.lib.ExceptionHandler;
import com.wisesharksoftware.core.Filter;
import com.wisesharksoftware.core.FilterFactory;
import com.wisesharksoftware.core.Image2;
import com.wisesharksoftware.core.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

public class MultiPicturesFilter extends Filter
{
    private enum ComposeType
    {
        square,
        vertical,
        horizontal,
        blend
    }
    
    private static final long serialVersionUID = 1L;

    private final static String PICTURES_COUNT = "pictures_count";
    private final static String COMPOSE_TYPE = "compose_type";

//    private DiskLruCache diskCache;
//    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 20; // 20MB
//    private static final String DISK_CACHE_SUBDIR = "MultiPicturesFilter";
//    private static final String CACHED_FILE_NAME = "tmp.png";
//    private static final int DISK_CACHE_INDEX = 0;

    private Integer picturesCount;
    private int currentPicture;
    private ComposeType composeType;
    
//    private Context context;

//    private int[][] imageData;

    private int[][] imageData;
    private Bitmap resultBitmap;

    public MultiPicturesFilter( )
    {
    	filterName = FilterFactory.MULTI_PICTURES_FILTER;
    	picturesCount = 0;
        composeType = null;    	
    }
    
    @Override
    public boolean init( Context context )
    {
//        this. context = context;
//        initDiskCache();
        return true;
    }

//    private void initDiskCache()
//    {
//        if( diskCache == null || diskCache.isClosed() )
//        {
//            File diskCacheDir = getDiskCacheDir( context, DISK_CACHE_SUBDIR );
//            if( diskCacheDir != null )
//            {
//                if( !diskCacheDir.exists() )
//                {
//                    diskCacheDir.mkdirs();
//                }
//                if( getUsableSpace( diskCacheDir ) > DISK_CACHE_SIZE )
//                {
//                    try
//                    {
//                        diskCache = DiskLruCache.open( diskCacheDir, 1, 1, DISK_CACHE_SIZE );
//                    }
//                    catch( final IOException e )
//                    {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }
    
    private File getDiskCacheDir( Context context, String uniqueName )
    {
        final String cachePath = Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() ) ? getExternalCacheDir( context ).getPath() : context.getCacheDir().getPath();
        return new File( cachePath + File.separator + uniqueName );
    }

    private File getExternalCacheDir( Context context )
    {
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File( Environment.getExternalStorageDirectory().getPath() + cacheDir );
    }

    private long getUsableSpace( File path )
    {
        final StatFs stats = new StatFs( path.getPath() );
        return ( long )stats.getBlockSize() * ( long )stats.getAvailableBlocks();
    }

    @Override
    protected void onSetParams()
    {
        for( Entry<String, String> paramsEntry : params.entrySet() )
        {
            String name = paramsEntry.getKey();
            String value = paramsEntry.getValue();
            
            if( name.equals( PICTURES_COUNT ) )
            {
                picturesCount = Integer.parseInt( value );
            }
            if( name.equals( COMPOSE_TYPE ) )
            {
                composeType = ComposeType.valueOf( value );
            }
        }
    }

    @Override
    public void processImage( Image2 image, Context context, boolean square, boolean isPortraitPhoto, boolean hd )
    {
        try
        {
        if( picturesCount <= 1 || composeType == null )
        {
            currentPicture = 0;
            return;
        }
        switch( composeType )
        {
            case square:
                processSquare( image, isPortraitPhoto );
                break;
            case vertical:
                processCrop( image, true );
                break;
            case horizontal:
                processCrop( image, false );
                break;
            case blend:
                processBlend( image );
                break;
        }
        finishProcessing( image );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            new ExceptionHandler( e, "MultiPicturesFilter" );
        }
    }

    @Override
    public boolean hasNativeProcessing()
    {
        return false;
    }

    private void processBlend( Image2 image )
    {
        if( currentPicture == 0 )
        {
            System.gc();
            imageData = new int[ image.height ][ image.width ];
            for( int i = 0; i < image.height; i++ )
            {
                System.arraycopy( image.data[ i ], 0, imageData[ i ], 0, imageData[ i ].length );
            }
        }
        else
        {
            for( int i = 0; i < image.height; i++ )
            {
                for( int j = 0; j < image.width; j++ )
                {
                    int srcColor = imageData[ i ][ j ];
                    int blendColor = image.data[ i ][ j ];

                    int srcRed = ( srcColor >> 16 ) & 0xFF;
                    int srcGreen = ( srcColor >> 8 ) & 0xFF;
                    int srcBlue = srcColor & 0xFF;
                    int blendRed = ( blendColor >> 16 ) & 0xFF;
                    int blendGreen = ( blendColor >> 8 ) & 0xFF;
                    int blendBlue = blendColor & 0xFF;

                    int resRed = ( srcRed * blendRed ) / 255;
                    int resGreen = ( srcGreen * blendGreen ) / 255;
                    int resBlue = ( srcBlue * blendBlue ) / 255;
                    int res = ( 0xFF << 24 ) | ( resRed << 16 ) | ( resGreen << 8 ) | resBlue;
                    imageData[ i ][ j ] = res;
                    image.data[ i ][ j ] = res;
                }
            }
        }
    }

    private void processCrop( Image2 image, boolean verticalCrop )
    {
        System.gc();
        Bitmap processedBitmap = image.toBitmap();
        int w = processedBitmap.getWidth();
        int h = processedBitmap.getHeight();
        int cropedW = verticalCrop ? w / picturesCount : w;
        int cropedH = verticalCrop ? h : h / picturesCount;
        imageData = new int[ cropedH ][ cropedW ];
        int xOffset = verticalCrop ? getCropOffset( w ) : 0;
        int yOffset = verticalCrop ? 0 : getCropOffset( h );
        for( int i = 0; i < cropedH; i++ )
        {
            processedBitmap.getPixels( imageData[ i ], 0, cropedW, xOffset, yOffset + i, cropedW, 1 );
        }
        processedBitmap.recycle();
        System.gc();

        if( resultBitmap == null )
        {
            try
            {
                resultBitmap = Bitmap.createBitmap( w, h, processedBitmap.getConfig() );
            }
            catch( Error e )
            {
                e.printStackTrace();
                System.gc();
                resultBitmap = Bitmap.createBitmap( w, h, processedBitmap.getConfig() );
            }
        }

        int j = verticalCrop ? currentPicture : 0;
        int i = verticalCrop ? 0 : currentPicture;
        for( int idx = 0; idx < cropedH; idx++ )
        {
            resultBitmap.setPixels( imageData[ idx ], 0, cropedW, j * cropedW, i * cropedH + idx, cropedW, 1 );
        }
        imageData = null;
    }

    private int getCropOffset( int size )
    {
        return ( size - ( size / picturesCount ) ) / 2;
    }

    private void processSquare( Image2 image, boolean isPortraitPhoto )
    {
        System.gc();
        Bitmap processedBitmap = image.toBitmap();
        image = null;
        int w = processedBitmap.getWidth();
        int h = processedBitmap.getHeight();
        int squareSize = picturesCount / 2;
        int scaledW = w / squareSize;
        int scaledH = h / squareSize;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap( processedBitmap, scaledW, scaledH, false );
        processedBitmap.recycle();
        System.gc();
        imageData = new int[ scaledH ][ scaledW ];
        for( int i = 0; i < scaledH; i++ )
        {
            scaledBitmap.getPixels( imageData[ i ], 0, scaledW, 0, i, scaledW, 1 );
        }
        scaledBitmap.recycle();
        
        if( resultBitmap == null )
        {
            try
            {
                System.gc();
                resultBitmap = Bitmap.createBitmap( w, h, processedBitmap.getConfig() );
            }
            catch( Error e )
            {
                e.printStackTrace();
                System.gc();
                resultBitmap = Bitmap.createBitmap( w, h, processedBitmap.getConfig() );
            }
        }

        int i = currentPicture / ( squareSize );
        int j = currentPicture % ( squareSize );
        for( int idx = 0; idx < scaledH; idx++ )
        {
            resultBitmap.setPixels( imageData[ idx ], 0, scaledW, j * scaledW, i * scaledH + idx, scaledW, 1 );
        }
        imageData = null;
    }

    private void finishProcessing( Image2 image )
    {
        if( currentPicture != picturesCount - 1 )
        {
            currentPicture++;
            return;
        }
        currentPicture = 0;

        System.gc();
        if( outFileName == null )
        {
            if( resultBitmap != null )
            {
                int w = resultBitmap.getWidth();
                for( int i = 0; i < image.height; i++ )
                {
                    resultBitmap.getPixels( image.data[ i ], 0, w, 0, i, w, 1 );
                }
            }
        }
        else
        {
            try
            {
                Utils.saveBitmapJpeg( resultBitmap, new File( outFileName ), true );
            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
        }
        if( resultBitmap != null && !resultBitmap.isRecycled() )
        {
            resultBitmap.recycle();
        }
        resultBitmap = null;
        imageData = null;
    }
    
//    private void clearCache()
//    {
//            if( diskCache != null && !diskCache.isClosed() )
//            {
//                try
//                {
//                    diskCache.delete();
//                }
//                catch( IOException e )
//                {
//                    e.printStackTrace();
//                }
//                diskCache = null;
//                initDiskCache();
//            }
//    }
//
//    private boolean addBitmapToCache( String data, Bitmap bitmap )
//    {
//        if( data == null || bitmap == null )
//        {
//            return false;
//        }
//        if( diskCache != null )
//        {
//            final String key = hashKeyForDisk( data );
//            OutputStream out = null;
//            try
//            {
//                DiskLruCache.Snapshot snapshot = diskCache.get( key );
//                if( snapshot == null )
//                {
//                    final DiskLruCache.Editor editor = diskCache.edit( key );
//                    if( editor != null )
//                    {
//                        out = editor.newOutputStream( DISK_CACHE_INDEX );
//                        bitmap.compress( CompressFormat.PNG, 100, out );
//                        editor.commit();
//                        out.close();
//                        return true;
//                    }
//                }
//                else
//                {
//                    snapshot.getInputStream( DISK_CACHE_INDEX ).close();
//                }
//            }
//            catch( final IOException e )
//            {
//                e.printStackTrace();
//            }
//            catch( Exception e )
//            {
//                e.printStackTrace();
//            }
//            finally
//            {
//                try
//                {
//                    if( out != null )
//                    {
//                        out.close();
//                    }
//                }
//                catch( IOException e )
//                {
//                }
//            }
//        }
//        return false;
//    }
//    
//    private Bitmap getBitmapFromDiskCache( String data )
//    {
//        final String key = hashKeyForDisk( data );
//        InputStream inputStream = null;
//        try
//        {
//            final DiskLruCache.Snapshot snapshot = diskCache.get( key );
//            if( snapshot != null )
//            {
//                inputStream = snapshot.getInputStream( DISK_CACHE_INDEX );
//                if( inputStream != null )
//                {
//                    System.gc();
//                    Bitmap immutableBitmap = BitmapFactory.decodeStream( inputStream );
//                    Bitmap mutableBitmap = immutableBitmap.copy( immutableBitmap.getConfig(), true );
//                    immutableBitmap.recycle();
//                    return mutableBitmap;
//                }
//            }
//        }
//        catch( final IOException e )
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            try
//            {
//                if( inputStream != null )
//                {
//                    inputStream.close();
//                }
//            }
//            catch( IOException e )
//            {
//            }
//        }
//        return null;
//    }
//
//    private static String hashKeyForDisk( String key )
//    {
//        String cacheKey;
//        try
//        {
//            final MessageDigest mDigest = MessageDigest.getInstance( "MD5" );
//            mDigest.update( key.getBytes() );
//            cacheKey = bytesToHexString( mDigest.digest() );
//        }
//        catch( NoSuchAlgorithmException e )
//        {
//            cacheKey = String.valueOf( key.hashCode() );
//        }
//        return cacheKey;
//    }
//
//    private static String bytesToHexString( byte[] bytes )
//    {
//        StringBuilder sb = new StringBuilder();
//        for( int i = 0; i < bytes.length; i++ )
//        {
//            String hex = Integer.toHexString( 0xFF & bytes[ i ] );
//            if( hex.length() == 1 )
//            {
//                sb.append( '0' );
//            }
//            sb.append( hex );
//        }
//        return sb.toString();
//    }

    @Override
    public boolean hd()
    {
        return false;
    }
    
    @Override
    public String convertToJSON() {
    	String s = "{";
    	s += "\"type\":" + "\"" + filterName + "\",";
    	//start params array
    	s += "\"params\":" + "[";
    	//param PICTURES_COUNT
      	s += "{";
      	s += "\"name\":" + "\"" + PICTURES_COUNT + "\",";
      	s += "\"value\":" + "\"" + picturesCount + "\"";
      	s += "},";
        //param COMPOSE_TYPE
      	s += "{";
      	s += "\"name\":" + "\"" + COMPOSE_TYPE + "\",";
      	s += "\"value\":" + "\"" + composeType + "\"";
      	s += "}";
        s += "]";
    	//end params array
    	s += "}";
    	return s;
    }
}
