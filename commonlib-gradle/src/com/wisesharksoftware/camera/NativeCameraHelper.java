package com.wisesharksoftware.camera;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.wisesharksoftware.core.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;

public class NativeCameraHelper {

	public NativeCameraHelper(Activity context, String photoFolder) {
		this.context = context;
		this.photoFolder = photoFolder;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent returnedIntent, NativeCameraCallback callback) {
		if ((requestCode == TAKE_PHOTO || requestCode == SELECT_PHOTO) && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = null; 
            if (returnedIntent != null && returnedIntent.getData() != null) {
            	selectedImage = returnedIntent.getData();
            	if (requestCode == TAKE_PHOTO && outputFileUri != null) {
            		File file = new File(outputFileUri.getPath());
            		file.delete();
            	}
            } else {
            	selectedImage = outputFileUri;
            	Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(outputFileUri);
                context.sendBroadcast(mediaScanIntent);
            }
            selectedImages.add( selectedImage );
            if( photoCount == selectedImages.size() ) {
            	callback.onPhotoTaken(selectedImages);
            	selectedImages.clear();
            } else {
            	if (requestCode == SELECT_PHOTO) {
            		selectPhoto();
            	} else {
            		takePhoto();
            	}
            }
		}
	}
	
	public void takePhoto() {
		fromCamera = true;
    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    	File file = new File(Utils.getFolderPath( photoFolder ), Utils.getDateFileName() + ".jpg");
    	outputFileUri = Uri.fromFile(file);
    	intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        context.startActivityForResult(intent, TAKE_PHOTO);
	}
	
    public void selectPhoto()
    {
    	fromCamera = false;
        Intent photoPickerIntent = new Intent( Intent.ACTION_PICK );
        photoPickerIntent.setType( "image/*" );
        context.startActivityForResult( photoPickerIntent, SELECT_PHOTO );
    }

	public Dialog createSelectDialog(int titleResId, int selectResId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
	    
		builder.setTitle(titleResId);
	    
	    builder.setItems(selectResId, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	if (which == 0) {
            		takePhoto();
            		return;
            	}
            	if (which == 1) {
              	   selectPhoto();
              	   return;
            	}
            }
	    });
	    return builder.create();
	}

	
    public boolean hasCamera() {
    	Class<Camera> cameraClass = Camera.class;
        Class<?> partypes[] = new Class[0];
        try {
            Method getNumCamerasMethod = cameraClass.getMethod("getNumberOfCameras", partypes);
			int numCameras = (Integer)getNumCamerasMethod.invoke(null);
			return numCameras > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
    }
    
    public void setPhotoCount(int photoCount) {
    	this.photoCount = photoCount;
    }

	
	public List<Uri> getSelectedImages() {
		return selectedImages;
	}
	
	public boolean isCameraSource() {
		return fromCamera;
	}

	private Activity context;
	private String photoFolder;
	private Uri outputFileUri;
	private int photoCount = 1;
	private List<Uri> selectedImages = new ArrayList<Uri>();
	private boolean fromCamera = true;
	
    private static final int SELECT_PHOTO = 1;
    private static final int TAKE_PHOTO = 2;

}
