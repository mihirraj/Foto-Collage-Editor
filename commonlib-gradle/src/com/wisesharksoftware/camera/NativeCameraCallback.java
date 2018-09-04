package com.wisesharksoftware.camera;

import java.util.List;
import android.net.Uri;


public interface NativeCameraCallback {
	
	void onPhotoTaken(List<Uri> photos);

}
