package com.wisesharksoftware.camera;

import com.smsbackupandroid.lib.SettingsHelper;
import com.wisesharksoftware.core.ActionCallback;

import android.hardware.Camera;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * This class manages settings bar for CameraPreviewActivity
 * @author Roman
 *
 */
public class SettingsPaneViewController {
	
  public SettingsPaneViewController(BaseCameraPreviewActivity parentActivity) {
    parentActivity_ = parentActivity;

  }

  public void setRedrawRequiredCallback(ActionCallback<Void> redrawCallback) {
    redrawCallback_ = redrawCallback;
  }

  public void init(Preview preview, int frontCameraNum, int backCameraNum) {
    preview_ = preview;
    frontCameraNum_ = frontCameraNum;
    backCameraNum_ = backCameraNum;
  }


  private void drawSoundControl() {
    final ImageButton turnOffSoundButton = parentActivity_.getTurnOffSoundButton();
    final ImageButton turnOnSoundButton = parentActivity_.getTurnOnSoundButton();
    boolean soundOn = SettingsHelper.getBoolean(parentActivity_, SettingsConstants.SOUND, true);
    preview_.setShutterSound(soundOn);
    if (soundOn) {
      turnOnSoundButton.setVisibility(ImageButton.GONE);
      turnOffSoundButton.setVisibility(ImageButton.VISIBLE);
      //turnOffSoundButton.setVisibility(ImageButton.GONE);
    } else {
      turnOffSoundButton.setVisibility(ImageButton.GONE);
      turnOnSoundButton.setVisibility(ImageButton.VISIBLE);
      //turnOffSoundButton.setVisibility(ImageButton.GONE);
    }
    final ActionCallback<Boolean> soundChangeListener = new ActionCallback<Boolean>() {
      @Override
      public void onAction(Boolean newSoundOn) {
        boolean oldSoundOn = SettingsHelper.getBoolean(parentActivity_, SettingsConstants.SOUND, true);
        if (oldSoundOn != newSoundOn) {
          if (newSoundOn) {
            turnOnSoundButton.setVisibility(ImageButton.GONE);
            turnOffSoundButton.setVisibility(ImageButton.VISIBLE);
          } else {
            turnOnSoundButton.setVisibility(ImageButton.VISIBLE);
            turnOffSoundButton.setVisibility(ImageButton.GONE);
          }
          preview_.setShutterSound(newSoundOn);
          SettingsHelper.setBoolean(parentActivity_, SettingsConstants.SOUND, newSoundOn);
        }
      }
    };
    turnOnSoundButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        soundChangeListener.onAction(true);
      }
    });
    turnOffSoundButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        soundChangeListener.onAction(false);
      }
    });
  }

  private void drawFlashControl() {
    final boolean flashSupported = preview_.getFlashMode() != null;
    final ImageButton changeFlashSettingsButton = parentActivity_.getChangeFlashSettingsButton();
    final LinearLayout flashSelectPane = parentActivity_.getFlashSelectPanel();
    final Button btnTurnFlashAuto = parentActivity_.getAutoFlashButton();
    final Button btnTurnFlashOn = parentActivity_.getTurnOnFlashButton();
    final Button btnTurnFlashOff = parentActivity_.getTurnOffFlashButton();

    if (flashSupported) {
     
      final int flashMode = SettingsHelper.getInt(parentActivity_,
                                                  SettingsConstants.FLASH_MODE,
                                                  FlashState.FLASH_OFF);
      final ActionCallback<Integer> flashChangeListener = new ActionCallback<Integer>() {
        @Override
        public void onAction(Integer newFlashState) {
          int oldFlashState = SettingsHelper.getInt(parentActivity_,
                                                    SettingsConstants.FLASH_MODE,
                                                    FlashState.FLASH_AUTO);
          if (oldFlashState != newFlashState) {
            changeFlashSettingsButton.setImageResource(parentActivity_.getFlashImageResource(newFlashState));
            preview_.setFlashMode(getFlashMode(newFlashState));
            SettingsHelper.setInt(parentActivity_, SettingsConstants.FLASH_MODE, newFlashState);
          }
          changeFlashSettingsButton.setVisibility(ImageButton.VISIBLE);
          flashSelectPane.setVisibility(ImageButton.GONE);
        }
      };
      changeFlashSettingsButton.setImageResource(parentActivity_.getFlashImageResource(flashMode));

      changeFlashSettingsButton.setVisibility(ImageButton.VISIBLE);
      flashSelectPane.setVisibility(ImageButton.GONE);
      changeFlashSettingsButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          changeFlashSettingsButton.setVisibility(ImageButton.GONE);
          flashSelectPane.setVisibility(ImageButton.VISIBLE);
        }
      });
      btnTurnFlashAuto.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          flashChangeListener.onAction(FlashState.FLASH_AUTO);
        }
      });
      btnTurnFlashOff.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          flashChangeListener.onAction(FlashState.FLASH_OFF);
        }
      });
      btnTurnFlashOn.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          flashChangeListener.onAction(FlashState.FLASH_ON);
        }
      });

      preview_.setFlashMode(getFlashMode(flashMode));

    } else {
      changeFlashSettingsButton.setVisibility(ImageButton.GONE);
      flashSelectPane.setVisibility(ImageButton.GONE);
    }
  }

  private void drawCameraRotationControl() {
    final boolean cameraRotationSupported = frontCameraNum_ >= 0 && backCameraNum_ >= 0;
    final ImageButton rotateCameraButton = parentActivity_.getRotateCameraButton();
    
    if (cameraRotationSupported) {
      rotateCameraButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          int prevCameraNum = SettingsHelper.getInt(parentActivity_, SettingsConstants.CAMERA_VIEW, backCameraNum_);
          int newCameraNum;
          if (prevCameraNum == backCameraNum_) {
            newCameraNum = frontCameraNum_;
          } else {
            newCameraNum = backCameraNum_;
          }
          SettingsHelper.setInt(parentActivity_, SettingsConstants.CAMERA_VIEW, newCameraNum);
          // redraw preview area.
          redrawCallback_.onAction(null);
        }
      });
    } else {
      rotateCameraButton.setVisibility(ImageButton.GONE);
    }
  }

  public void redraw() {
    drawSoundControl();
    drawFlashControl();
    drawCameraRotationControl();    
  }
  
  /**
   * Converts from our internal camera enum into android camera enum
   * @param id
   * @return
   */
  private String getFlashMode(int id) {
    switch(id) {
      case FlashState.FLASH_OFF:
          return Camera.Parameters.FLASH_MODE_OFF;
        case FlashState.FLASH_ON:
          return Camera.Parameters.FLASH_MODE_ON;
        case FlashState.FLASH_AUTO:
        default:
          return Camera.Parameters.FLASH_MODE_AUTO;
    }
  }

  /*
  private int getFlashImageResource(int flashMode) {
	int orientation = parentActivity_.getResources().getConfiguration().orientation;
    switch(flashMode) {
      case FlashState.FLASH_OFF:
          return orientation == Configuration.ORIENTATION_LANDSCAPE ? R.drawable.step3_flash_off_land : R.drawable.step3_flash_off;
        case FlashState.FLASH_ON:
          return orientation == Configuration.ORIENTATION_LANDSCAPE ? R.drawable.step3_flash_on_land : R.drawable.step3_flash_on;
        case FlashState.FLASH_AUTO:
        default:
          return orientation == Configuration.ORIENTATION_LANDSCAPE ? R.drawable.step3_flash_auto_land : R.drawable.step3_flash_auto;
  }
  }*/

  private final BaseCameraPreviewActivity parentActivity_;

  private Preview preview_;
  int frontCameraNum_;
  int backCameraNum_;
  ActionCallback<Void> redrawCallback_;

  public class FlashState {
    public final static int FLASH_AUTO = 1;
    public final static int FLASH_ON = 2;
    public final static int FLASH_OFF = 3;
  }
}
