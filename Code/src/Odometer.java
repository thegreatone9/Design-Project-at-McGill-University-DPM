import lejos.nxt.ColorSensor;
import lejos.util.Timer;
import lejos.util.TimerListener;

/**
 * Odometer Class. 
 * 
 * Keeps track of the (x,y) location of the robot in addition of its rotation (Heading). These values can be read or
 * overwritten when necessary.
 * 
 * @author Bobak Hamed-Baghi, Victor Repkow.
 * @version V1.1 Revision 2
 *
 */
public class Odometer implements TimerListener {
	
	/** odometer period, used to control the update frequency of the odometer. */
	public static final int DEFAULT_PERIOD = 25;
	
	/** reference to the TwoWheeledRobot class used by robot*/
	private TwoWheeledRobot robot;
	
	/**timer that handles updating the odometer */
	private Timer odometerTimer;
	
	/**Navigation class used by robot to handle mobility tasks*/
	private Navigation nav;
	
	// position data
	/**Lock object to ensure synchronization of position values when getting and setting position Variables*/
	private Object lock;
	
	/**Position values used by odometer:
	 * x: the x-coordinate of the odometery center
	 * y: the y-coordinate of the odometery center
	 * theta : angle of the odometery center with respect to +y axis and increasing clockwise.
	 */
	private double x, y, theta;
	
	//Difference variables used to hold error calculation values.
	private double [] oldDH, dDH;
	
	
	/**
	 * default Constructor. 
	 * 
	 * Initializes and starts the timer.
	 * @param robot the TwoWheeledRobot class used by the robot.
	 * @param period the period of the odometery timer. controlls the update frequency of the odomter.
	 * @param start start the odometer immediately or not. True starts the odometer timer immediately, while 
	 * 			false requires manual prompts later on.
	 * @param ls the colorSensor used by the robot.
	 */
	public Odometer(TwoWheeledRobot robot, int period, boolean start, ColorSensor ls) {
		// initialise variables
		this.robot = robot;
		this.nav = new Navigation(this);
		odometerTimer = new Timer(period, this);
		x = 0.0;
		y = 0.0;
		theta = 0.0;
		oldDH = new double [2];
		dDH = new double [2];
		lock = new Object();
		
		// start the odometer immediately, if necessary
		if (start)
			odometerTimer.start();
	}
	
	/**
	 *  secondary constructor that takes in only the twoWheeledRobot, uses
	 *  the default period defined in the fields, does not start the robot and 
	 *  does not make use of the lightSensor.
	 * @param robot the twoWheeledRobot class being used by the robot.
	 */
	public Odometer(TwoWheeledRobot robot) {
		this(robot, DEFAULT_PERIOD, false, null);
	}
	
	/**
	 * secondary constructor. 
	 * Creates an odometer with the default period instead of a custom one.
	 * @param robot the twoWheeledRobot Class used by the robot.
	 * @param start boolean to determine wether to start polling the odometer or not.
	 * @param ls Colorsensor used by this robot.
	 */
	public Odometer(TwoWheeledRobot robot, boolean start, ColorSensor ls) {
		this(robot, DEFAULT_PERIOD, start, ls);
	}
	
	/**
	 * seconday constructor. 
	 * 
	 * Creates an odometer with a custom polling frequency. Does not start the odometer 
	 * and does not provide a colorsensor instance reference to the odometer.
	 * @param robot		the twoWheeledRobot instance used by the robot.
	 * @param period	the period at which the odometer is updates.
	 */
	public Odometer(TwoWheeledRobot robot, int period) {
		this(robot, period, false, null);
	}
	
	/**
	 * Called when the odometer timer is over. updates the odometer.
	 * 
	 * Calculates the difference in each call, using the robot motor tachometers. 
	 * updates x,y,theta using these differences.
	 */
	public void timedOut() {
		robot.getDisplacementAndHeading(dDH);
		dDH[0] -= oldDH[0];
		dDH[1] -= oldDH[1];
		
		// update the position in a critical region
		synchronized (lock) {
			theta -= dDH[1];
			theta = fixDegAngle(theta);
			
			x -= dDH[0] * Math.sin(Math.toRadians(theta));
			y -= dDH[0] * Math.cos(Math.toRadians(theta));
		}
		
		oldDH[0] += dDH[0];
		oldDH[1] += dDH[1];
	}
	
	// accessors
	
	/**
	 * Position array getter method. returns a position array that contains x,y, and theta in order.
	 * 
	 * @param pos the position array containing the x,y,theta values of the odometer.
	 */
	public void getPosition(double [] pos) {
		synchronized (lock) {
			pos[0] = x;
			pos[1] = y;
			pos[2] = theta;
		}
	}
	
	
	/**
	 * accessor method that returns the twoWheeledRobot used by this odometer.
	 * 
	 * @return the twoWheeledRobot used by this odometer.
	 */
	public TwoWheeledRobot getTwoWheeledRobot() {
		return robot;
	}
	
	
	/**
	 * navigation class accessor. 
	 * 
	 * @return the navigation class used by this odometer.
	 */
	public Navigation getNavigation() {
		return this.nav;
	}
	
	// mutators
	/**
	 * mutator that sets the position of the odometer (x,y, and theta) 
	 * @param pos 		the position array (x,y,theta in that order) 
	 * @param update	boolean array that masks the update of the pos array. If an index has a true value
	 * 					in the boolean array, that index in the pos array will be updated.
	 */
	public void setPosition(double [] pos, boolean [] update) {
		synchronized (lock) {
			if (update[0]) x = pos[0];
			if (update[1]) y = pos[1];
			if (update[2]) theta = pos[2];
		}
	}
	
	
	// static 'helper' methods
	/**
	 * helper method that normalizes an angle to a range between [0,360]
	 * @param angle anlge to be normalized.
	 * @return normalized angle.
	 */
	public static double fixDegAngle(double angle) {		
		if (angle < 0.0)
			angle = 360.0 + (angle % 360.0);
		
		return angle % 360.0;
	}
	
	/**
	 * helper method that determines the minimum angle between two selected angles.
	 * 
	 * @param a start angle 
	 * @param b end angle
	 * @return least amount angle between a and b.
	 */
	public static double minimumAngleFromTo(double a, double b) {
		double d = fixDegAngle(b - a);
		
		if (d < 180.0)
			return d;
		else
			return d - 360.0;
	}
}
