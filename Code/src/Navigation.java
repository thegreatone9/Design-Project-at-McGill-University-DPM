import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.RConsole;


/**
 * Class that holds all navigation functionality of the robot. 
 * 
 * Works with the robot class to controll motors of the robot and with the
 * odometer class to update the position and calculate target destination.
 * 
 * @author Bobak Hamed-Baghi, Victor Repkow.
 * @version V1.1 Revision 2
 *
 */
public class Navigation{
	
	/** Reference to the odometer which is used by the robot*/
	private Odometer odo;
	
	/** robot class reference. This class holds all the references to the motors and
	 * contains helper function for movement. 
	 */
	private TwoWheeledRobot robot;
	
	/**error margins to judge whether destination has been reached or not. */
	final static double DEG_ERR = 2.0, CM_ERR = 1.0;
	
	final static int FAST = 10, SLOW = 40, ACCELERATION = 500;
	
	
	/**
	 * Default Constructor.
	 * 
	 * Initializes class instances.
	 * 
	 * @param odo odometer used by the robot.
	 */
	public Navigation(Odometer odo) {
		this.odo = odo;
		this.robot = odo.getTwoWheeledRobot();
	}
	
	/**
	 * Default Traveling method of the robot.
	 * 
	 * Takes in the (x,y) coordinates of the destination, orients itself
	 * towards the destination, and starts traveling until destination is reached.
	 * 
	 * @param x target destination's x-cooridinate
	 * @param y target destination's y-cooridinate
	 */
	public void travelTo(double x, double y) {
		
		robot.setSpeeds(0, 0);
		robot.setAccelerations(robot.ACCELERATION);
		double pos[] = new double[3];
		double myX,myY,angle;
		double minAng;
		this.odo.getPosition(pos);
		myX = pos[0];
		myY = pos[1];
		angle = pos[2];
		
		//Rotate to correct heading
		minAng = (Math.atan2(x - myX, y - myY)) * (180.0 / Math.PI);
		if (minAng < 0)
			minAng += 360.0;
		
		turnTo(minAng);
		
		while (Math.abs(x - myX) > CM_ERR || Math.abs(y - myY) > CM_ERR) {
			this.odo.getPosition(pos);
			myX = pos[0];
			myY = pos[1];
			angle = pos[2];
			
			//ANGULAR CORRECTION
			minAng = (Math.atan2(x - myX, y - myY)) * (180.0 / Math.PI);
			if (minAng < 0)
				minAng += 360.0;
			
			double angleError = minAng - angle;
			if(angleError >180){
				angleError = angleError-360;
			}
			if(angleError <-180){
				angleError = angleError+360;
			}
			
			robot.setRotationSpeed(angleError);
			

			robot.setForwardSpeed(10);
		}
		
		robot.setAccelerations(6000);
		robot.setSpeeds(0,0);
	}
	
	
	/**
	 * Helper function for a sequence of travelTo() commands to be 
	 * executed in sequence.
	 * 
	 * This function takes in two arrays of x and y coordinates and performs a 
	 * sequqnce of travelTo().
	 * 
	 * @param x the array of x Coordinates to travel to.
	 * @param y the array of y Coordinates to travel to.
	 */
	public void travelToSequence(double[] x, double[] y){
		for(int i=0; i<x.length; i++ ){
			travelTo(x[i], y[i]);
		}
	}
	
	/**
	 * Main Turning method which rotates the robot to face the desired angle. 
	 * 
	 * Takes in a desired angle parameter, calculates the amount of rotation needed, 
	 * and rotate the robot to face to the correct orientation. The rotation is centered on the odometerey
	 * center of the robot and is essentially stationary rotation.
	 * 
	 * @param desiredAngle angle at which robot must face at the end of the rotation process.
	 */
	void turnTo(double desiredAngle){
		robot.leftMotor.stop();
		robot.rightMotor.stop();
		double pos[]= new double[3]; 
		odo.getPosition(pos);
		double myAngle = pos[2];
		
		
		double angleError = desiredAngle - myAngle;
		if(angleError >180){
			angleError = angleError-360;
		}
		if(angleError <-180){
			angleError = angleError+360;
		}
		
		double leftRotateAmount = angleError*robot.DEFAULT_WIDTH/(robot.DEFAULT_LEFT_RADIUS*2);
		double rightRotateAmount = angleError*robot.DEFAULT_WIDTH/(robot.DEFAULT_RIGHT_RADIUS*2);
		
		robot.setAccelerations(robot.ACCELERATION);
		robot.setForwardSpeed(10);
		
		robot.leftMotor.rotate((int) -leftRotateAmount,true);
		robot.rightMotor.rotate((int) rightRotateAmount, false);
		robot.setForwardSpeed(0);		
	}
	
	
	/**
	 * helper method to convert angle to turn by robot into rotation angle amount to turn by wheels.
	 * @param radius radius of the wheel.
	 * @param width bandwidth of the robot (distance between two wheel centers)
	 * @param angle desired angle of robot rotation.
	 * @return
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return (int) ((width*(angle))/(2*radius));
	}
	
	/**
	 * helper method to calculate the difference between two angles. This method
	 * returns the value in a normalized format between [0,360] and can handle finding the closest
	 * angle between any two angles.
	 * 
	 * @param angleA start angle
	 * @param angleB end angle
	 * @return difference in degrees between angleA and angleB in a normalized [0,360] range.
	 */
	private double getOffAngle(double angleA, double angleB){
		
		angleA = normalize(angleA);
		angleB = normalize(angleB);
		
		double diff = Math.abs(angleA - angleB);
		if(diff < 360-diff)
			return diff;
		else return 360-diff;
		
	}
	
	/**
	 *  helper method that normalizes an angle to a range between [0,360]
	 * @param A angle to be normalized
	 * @return normalized angle
	 */
	private double normalize(double A){
		if(A < 0)
			return A+360;
		else return A;
	}
	
	}
