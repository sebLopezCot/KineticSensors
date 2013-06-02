package net.splashe.kineticsensors.util;

public class SensorHelper {

	// Nanoseconds to seconds conversion factor
	public static final float NS2S = 1.0f / 1000000000.0f;
		
	public static final double GYRO_NOISE_LIMIT = 0.06;
	public static final int X_IDX = 0;
	public static final int Y_IDX = 1;
	public static final int Z_IDX = 2;
	
	/* Return a value that cancels out gyro noise */
	public static double gyroNoiseLimiter(double gyroValue){
		if(Math.abs(gyroValue) < GYRO_NOISE_LIMIT)
			gyroValue = 0.0;
		return gyroValue;
	}
	
	/* Updates previous simulated gravity to current simulated gravity */
	public static void copySimulatedGravity(double previousSimulatedGravity[], 
			double simulatedGravity[]){
		previousSimulatedGravity[X_IDX] = simulatedGravity[X_IDX];
		previousSimulatedGravity[Y_IDX] = simulatedGravity[Y_IDX];
		previousSimulatedGravity[Z_IDX] = simulatedGravity[Z_IDX];
	}
	
	/* Check to see if a significant difference in gravity has been reached */
	public static boolean difflimit(double previousSimulatedGravity[], 
			double simulatedGravity[]) {
		if( previousSimulatedGravity == null ) {
			previousSimulatedGravity = new double[3];
			copySimulatedGravity(previousSimulatedGravity, simulatedGravity);
			return true;
		} else
		if( ( Math.abs( previousSimulatedGravity[X_IDX]-simulatedGravity[ X_IDX ]) > 0.1) ||
			( Math.abs( previousSimulatedGravity[Y_IDX]-simulatedGravity[ Y_IDX ]) > 0.1) ||
			( Math.abs( previousSimulatedGravity[Z_IDX]-simulatedGravity[ Z_IDX ]) > 0.1) ) {
				copySimulatedGravity(previousSimulatedGravity, simulatedGravity);
				return true;
			}
		return false;
	}
	
	/* Rotate vector about the X axis */
	public static void rotx( double vec[], double dx ) {
		double y = vec[Y_IDX];
		double z = vec[Z_IDX];
		vec[Y_IDX] = y*Math.cos(dx) - z*Math.sin(dx);
		vec[Z_IDX] = y*Math.sin(dx) + z*Math.cos(dx);
	}
	
	/* Rotate vector about the Y axis */
	public static void roty( double vec[], double dy ) {
		double x = vec[X_IDX];
		double z = vec[Z_IDX];
		vec[Z_IDX] = z*Math.cos(dy) - x*Math.sin(dy);
		vec[X_IDX] = z*Math.sin(dy) + x*Math.cos(dy);
	}
	
	/* Rotate vector about the Z axis */
	public static void rotz( double vec[], double dz ) {
		double x = vec[X_IDX];
		double y = vec[Y_IDX];
		vec[X_IDX] = x*Math.cos(dz) - y*Math.sin(dz);
		vec[Y_IDX] = x*Math.sin(dz) + y*Math.cos(dz);
	}
	
	/* Find the difference of two vectors */
	public static double[] vecdiff( double v1[], double v2[] ) {
		double diff[] = new double[3];
		diff[X_IDX] = v1[X_IDX] - v2[X_IDX];
		diff[Y_IDX] = v1[Y_IDX] - v2[Y_IDX];
		diff[Z_IDX] = v1[Z_IDX] - v2[Z_IDX];
		return diff;
	}

	public static double fixAtanDegree( double deg, double y, double x ) {
		double rdeg = deg;
		if( ( x < 0.0 ) && ( y > 0.0 ) )
			rdeg = Math.PI - deg;
		if( ( x < 0.0 ) && ( y < 0.0 ) )
			rdeg = Math.PI + deg;
		return rdeg;
	}

	public static double[] rotateToEarth( double diff[], double simulatedGravity[]) {
		double rotatedDiff[] = new double[3];
		rotatedDiff[X_IDX] = diff[X_IDX];
		rotatedDiff[Y_IDX] = diff[Y_IDX];
		rotatedDiff[Z_IDX] = diff[Z_IDX];
		double gravity[] = new double[3];
		gravity[ X_IDX ] = simulatedGravity[ X_IDX ];
		gravity[ Y_IDX ] = simulatedGravity[ Y_IDX ];
		gravity[ Z_IDX ] = simulatedGravity[ Z_IDX ];
		double dz = Math.atan2( gravity[Y_IDX], gravity[X_IDX]);
		dz = fixAtanDegree( dz, gravity[Y_IDX], gravity[X_IDX] );
		rotz( rotatedDiff, -dz );
		rotz( gravity, -dz );
		double dy = Math.atan2( gravity[X_IDX], gravity[Z_IDX]);
		dy = fixAtanDegree( dy, gravity[X_IDX], gravity[Z_IDX]);
		roty( rotatedDiff, -dy );
		return rotatedDiff;
	}
	
	public static double pythag(double x, double y, double z){
		return Math.sqrt(x*x + y*y + z*z);
	}

	public static double boundTo360Degrees(double angle){
		if(angle >= 360.0){
			angle -= 360.0 * ((int)angle / 360);
		} else if (angle < 0.0){
			angle = 360.0 - angle;
		}
		return angle;
	}
}
