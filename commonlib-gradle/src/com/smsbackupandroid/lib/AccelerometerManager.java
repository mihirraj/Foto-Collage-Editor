package com.smsbackupandroid.lib;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
 
/**
 * Android Accelerometer Sensor Manager Archetype
 * @author antoine vianey
 * under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 */
public class AccelerometerManager {
 
    static String LOG_TAG = "Log";
    
    private static Sensor sensor;
    private static SensorManager sensorManager;
    // you could use an OrientationListener array instead
    // if you plans to use more than one listener
    private static AccelerometerListener listener;
 
    /** indicates whether or not Accelerometer Sensor is supported */
    private static Boolean supported;
    /** indicates whether or not Accelerometer Sensor is running */
    private static boolean running = false;
 
    private static float threshold     = 0.2f;
    private static int max_interval = 2000; // maxim counting interval. if longer - it is not shake
//    private static int min_interval = 1000; // minimum interval between shake events
    
    /**
     * Returns true if the manager is listening to orientation changes
     */
    public static boolean isListening() {
        return running;
    }
 
    /**
     * Unregisters listeners
     */
    public static void stopListening() {
        running = false;
        try {
            if (sensorManager != null && sensorEventListener != null) {
                sensorManager.unregisterListener(sensorEventListener);
            }
        } catch (Exception e) {}
    }
 
    /**
     * Returns true if at least one Accelerometer sensor is available
     */
    public static boolean isSupported(Context context) {
        if (supported == null) {
            if (context != null) {
            	Log.d(LOG_TAG, "check if supported");
                sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                List<Sensor> sensors = sensorManager.getSensorList(
                        Sensor.TYPE_ACCELEROMETER);
                supported = new Boolean(sensors.size() > 0);
            } else {
                supported = Boolean.FALSE;
            }
        }
        return supported;
    }
 
    /**
     * Configure the listener for shaking
     * @param threshold
     *             minimum acceleration variation for considering shaking
     * @param interval
     *             minimum interval between to shake events
     */
    public static void configure(int threshold, int interval) {
        AccelerometerManager.threshold = threshold;
        AccelerometerManager.max_interval = interval;
    }
 
    /**
     * Registers a listener and start listening
     * @param accelerometerListener
     *             callback for accelerometer events
     */
    public static void startListening(Context context, AccelerometerListener accelerometerListener) {
    	Log.d(LOG_TAG, "start_listening");
    	sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(
                Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            sensor = sensors.get(0);
            running = sensorManager.registerListener(
                    sensorEventListener, sensor, 
                    SensorManager.SENSOR_DELAY_GAME);
            listener = accelerometerListener;
        }
    }
 
    /**
     * Configures threshold and interval
     * And registers a listener and start listening
     * @param accelerometerListener
     *             callback for accelerometer events
     * @param threshold
     *             minimum acceleration variation for considering shaking
     * @param interval
     *             minimum interval between to shake events
     */
    public static void startListening(
            Context context, AccelerometerListener accelerometerListener, 
            int threshold, int interval) {
        configure(threshold, interval);
        startListening(context, accelerometerListener);
    }
    
    /**
     * The listener that listen to events from the accelerometer listener
     */
    private static SensorEventListener sensorEventListener = 
        new SensorEventListener() {

        /** Accuracy configuration */        
        private float accel =  0.0f; // acceleration apart from gravity
    	private float currentAccel = SensorManager.GRAVITY_EARTH; // current acceleration including gravity
    	private float lastAccel = SensorManager.GRAVITY_EARTH; // last acceleration including gravity
    	
        private long lastShake = 0;
 
        private float x = 0;
        private float y = 0;
        private float z = 0;
 
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
 
    	private boolean isShakeDelay() {
    		long time = System.currentTimeMillis();
    		if (time - lastShake > max_interval) {
    			lastShake = time;
    			return false;
    		} else {
    	    	return true;
    		}
    	}
        
    	@Override
        public void onSensorChanged(SensorEvent event) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            
            lastAccel = currentAccel;
            currentAccel = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = currentAccel - lastAccel;
            accel = accel * 0.9f + delta; // perform low-cut filter
            if (accel > threshold) {
                if (isShakeDelay()) {
                	return;
                }
                listener.onShake(accel);
            }
            // trigger change event
            listener.onAccelerationChanged(x, y, z);
        }
    };
 
}