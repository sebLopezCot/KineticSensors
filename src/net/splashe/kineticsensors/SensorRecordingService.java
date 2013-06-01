package net.splashe.kineticsensors;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import static net.splashe.kineticsensors.util.SensorHelper.*;


public class SensorRecordingService extends Service implements SensorEventListener {
	
	public static final int ENGINESTATE_IDLE = 0;
	public static final int ENGINESTATE_CALIBRATING = 1;
	public static final int ENGINESTATE_MEASURING = 2;
	public static final int SENSORTYPE_NA = 0;
	public static final int SENSORTYPE_ACCEL = 1;
	public static final int SENSORTYPE_GYRO = 0;
	
	IBinder mBinder;
	SensorManager sensorMgr;
	Sensor accelSensor, gyroSensor;
	
	public class LocalBinder extends Binder{
		public SensorRecordingService getSensorRecordingServiceInstance(){
			return SensorRecordingService.this;
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Initialize LocalBinder
		mBinder = new LocalBinder();
		
		// Initialize the sensors
		accelData = new float[3];
		gyroData = new float[3];
		angleData = new float[3];
		previousError = new float[3];
		errorDerivative = new float[3];
		
		sensorMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gyroSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		
		// Register sensor event listener
		sensorMgr.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI);
		sensorMgr.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_UI);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// Unregister sensor event listener
		sensorMgr.unregisterListener(this);
		Log.d(MainActivity.TAG, "Listener Unregistered");
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Change for actual accelerometer data (with static helper methods)
//		processSample(event);
		switch(getSensorType(event.sensor)){
		case SENSORTYPE_ACCEL:
			for(int i=0; i < accelData.length; i++) 
				accelData[i] = event.values[i];
			break;
			
		case SENSORTYPE_GYRO:
			updateAngleData(event);
			break;
		default:
			break;
		}
	}
	

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	
	private int getSensorType(Sensor sensor){
		if(sensor == accelSensor)
		{
			return SENSORTYPE_ACCEL;
		} 
		else if(sensor == gyroSensor)
		{
			return SENSORTYPE_GYRO;
		}
		else 
		{
			return SENSORTYPE_NA;
		}
	}
	
	private void updateAngleData(SensorEvent event){
		if (previousTimestamp >= 0L) {
			// Get the change in time to use for integration
			float dt = (float)(event.timestamp - previousTimestamp) * NS2S;

			float[] gyroInput = event.values;
			
			// Add the change in angle to angleData by integrating
			for(int i=0; i < angleData.length; i++)
				angleData[i] += (float) Math.toDegrees(gyroInput[i] * dt);
			
			// Remove accumulated angle error
			removeAccumulatedAngleError(angleData, dt);
			
			// Bound the angle to 360 degrees
			for(int i=0; i < angleData.length; i++)
				angleData[i] = (float) boundTo360Degrees(angleData[i]);
		}
		
		// Update timestamp
		previousTimestamp = event.timestamp;
	}
	
	private void removeAccumulatedAngleError(float[] angleInput, float dt){
		if(calibrated){
			for(int i=0; i < angleInput.length; i++)
				angleInput[i] -= errorDerivative[i] * dt;
		}
	}
	
	
	/* Update sample counter to allow for calibration */
	private void updateSampleCounter() {
		// TODO Auto-generated method stub
		
	}
	
	/* Where all of the sampling for each sensor is done */
	private void processSample(SensorEvent event) {
		float rawVals[] = event.values;
		if(rawVals.length < 3)
			return;

		// Detemine sensor type
		int sensorType = getSensorType(event.sensor);
		
		updateSampleCounter(); // Update sample counter to eventually end calibration
		
		switch(state){
		case ENGINESTATE_CALIBRATING: // Still needs calibrating
			processCalibrating(event.timestamp, sensorType, rawVals);
			break;
		case ENGINESTATE_MEASURING: // Measuring values and calculating
			processMeasuring(event.timestamp, sensorType, rawVals);
			break;
		default:
			break;
		}
	}
	
	private void processCalibrating(long timestamp, int sensorType,
			float[] rawVals) {
		// TODO Auto-generated method stub
		
	}
	
	private void processMeasuring(long timestamp, int sensorType,
			float[] rawVals) {
		// TODO Auto-generated method stub
		
	}

	/* Sensor data variables and arrays */
	public float[] accelData;
	public float[] gyroData;
	public float[] angleData;
	private long previousTimestamp;
	private float[] previousError;
	private float[] errorDerivative;
	private boolean calibrated;
	private int state;
}
