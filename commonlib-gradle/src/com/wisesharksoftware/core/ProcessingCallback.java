package com.wisesharksoftware.core;

import android.graphics.Bitmap;

public interface ProcessingCallback
{
    void onStart();

    void onSuccess(String outFileName);

    void onFail( Throwable e );

    void onBitmapCreated( Bitmap bitMap );

    void onBitmapCreated( Image2 bitMap );

    void onBitmapCreatedOpenCV();
    
    void onCancelled();
}
