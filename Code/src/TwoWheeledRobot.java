import lejos.nxt.NXTRegulatedMotor;

/**
 * Class that holds all instances to motors. Contains methods which facilitate the use of the motors
 * for movement purposes. also acts as a repository for most calibrated values concerning measurements 
 * on the robot body and calibrations.
 *  
 * @author Bobak Hamed-Baghi, Victor Repkow.
 * @version V1.1 revision 2
 *
 */
public class TwoWheeledRobot {
	/**the default calibrated effective left wheel radius */
	public static final double DEFAULT_LEFT_RADIUS = 2.05;
	
	/**the default calibrated effective right wheel radius */
	public static final double DEFAULT_RIGHT_RADIUS = 2.075;
	
	/**the default calibrated effective distance between the center of two wheels */
	public static final double DEFAULT_WIDTH = 15.4;
	
	/**the default acceleration to be used for motors for smooth movement. can be overwritten
	 * by the setAccelerations() function. */
	public static final int  ACCELERATION = 500;
	
	//Motors that control the left and right wheels
	/**
	 * The motors controlling the left wheel of this robot
	 */
	public NXTRegulatedMotor leftMotor;
	
	/**
	 * The motors controlling the right wheel of this robot
	 */
	public NXTRegulatedMotor rightMotor;
	
	//non-default values for left and right wheel radii and robot wheelbase.
	private double leftRadius, rightRadius, width;
	
	//the forward and rotational components of the speed
	private double forwardSpeed, rotationSpeed;

	/**
	 * default constructor.
	 * 
	 * This constructor takes in the motors controlling both wheels, along with a custom robot wheelbase 
	 * and right and left wheel radii.
	 * 
	 * @param leftMotor motor Controlling left wheel
	 * @param rightMotor motor controlling right wheel 
	 * @param width custom wheelbase for robot (distance between two wheel centers)
	 * @param leftRadius custom left wheel radius
	 * @param rightRadius custom rigt wheel radius 
	 */
	public TwoWheeledRobot(NXTRegulatedMotor leftMotor,
						   NXTRegulatedMotor rightMotor,
						   double width,
						   double leftRadius,
						   double rightRadius) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.setAccelerations(ACCELERATION);
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.width = width;

	}
	
	/**
	 * secondary constructor. Instantiates this class with default values for wheelbase, left and right wheel radii.
	 * @param leftMotor the motor controlling the left wheel.
	 * @param rightMotor the motor controlling the right wheel.
	 */
	public TwoWheeledRobot(NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor) {
		this(leftMotor, rightMotor, DEFAULT_WIDTH, DEFAULT_LEFT_RADIUS, DEFAULT_RIGHT_RADIUS);
	}
	
	/**
	 * secondary constructor. Instantiates with the default wheel radii, but with a custom wheelbase.
	 * @param leftMotor the motor controlling the left wheel.
	 * @param rightMotor the motor controlling the right wheel.
	 * @param width the wheelabse of the robot.
	 */
	public TwoWheeledRobot(NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor, double width) {
		this(leftMotor, rightMotor, width, DEFAULT_LEFT_RADIUS, DEFAULT_RIGHT_RADIUS);
	}
	
	// accessors
	
	/**
	 * accessor method that gets the displacement of the robot based on motor revolutions.
	 * @return the displacement of the robot.
	 */
	public double getDisplacement() {
		return (leftMotor.getTachoCount() * leftRadius +
				rightMotor.getTachoCount() * rightRadius) *
				Math.PI / 360.0;
	}
	
	/**
	 * accessor method that obtains the heading of the robot based on the difference
	 * between the amount rotated by the left and right wheels.
	 * @return the heading of the robot.
	 */
	public double getHeading() {
		return (leftMotor.getTachoCount() * leftRadius -
				rightMotor.getTachoCount() * rightRadius) / width;
	}
	
	
	/**
	 * helper method, takes an array data and fills it with the displacement and heading information.
	 * @param data the array that acts as a container for displacement and heading.
	 */
	public void getDisplacementAndHeading(double [] data) {
		int leftTacho, rightTacho;
		leftTacho = leftMotor.getTachoCount();
		rightTacho = rightMotor.getTachoCount();
		
		data[0] = (leftTacho * leftRadius + rightTacho * rightRadius) *	Math.PI / 360.0;
		data[1] = (leftTacho * leftRadius - rightTacho * rightRadius) / width;
	}
	
	// mutators
	
	/**
	 * sets the forward speed component of the robot.
	 * @param speed the new forward speed component of the robot.
	 */
	public void setForwardSpeed(double speed) {
		forwardSpeed = speed;
		setSpeeds(forwardSpeed, rotationSpeed);
	}
	
	/**
	 * sets the rotation speed component of the robot.
	 * @param speed the new rotation speed component of the robot.
	 */
	public void setRotationSpeed(double speed) {
		rotationSpeed = speed;
		setSpeeds(forwardSpeed, rotationSpeed);
	}
	
	/**
	 * calculates and sets the speeds of the left and right wheels based on the forward speed and 
	 * rotational speed components passed in.
	 * @param forwardSpeed forward speed component of the robot.
	 * @param rotationalSpeed rotational speed component of the robot.
	 */
	public void setSpeeds(double forwardSpeed, double rotationalSpeed) {
		double leftSpeed, rightSpeed;

		this.forwardSpeed = forwardSpeed;
		this.rotationSpeed = rotationalSpeed; 

		leftSpeed = (forwardSpeed + rotationalSpeed * width * Math.PI / 360.0) *
				180.0 / (leftRadius * Math.PI);
		rightSpeed = (forwardSpeed - rotationalSpeed * width * Math.PI / 360.0) *
				180.0 / (rightRadius * Math.PI);
		
		
		//FORWARD-BACKWARD FLIPPED
		// set motor directions
		if (leftSpeed > 0.0)
			leftMotor.backward(); 
		else {
			leftMotor.forward();
			leftSpeed = -leftSpeed;
		}
		
		if (rightSpeed > 0.0)
			rightMotor.backward();
		else {
			rightMotor.forward();
			rightSpeed = -rightSpeed;
		}
		
		
		// set motor speeds
		if (leftSpeed > 900.0)
			leftMotor.setSpeed(900);
		else
			leftMotor.setSpeed((int)leftSpeed);
		
		if (rightSpeed > 900.0)
			rightMotor.setSpeed(900);
		else
			rightMotor.setSpeed((int)rightSpeed);
	}
	
	/**
	 * sets the acceleration of both the left and right motors. used for smooth transition between
	 * starting movement and stopping.
	 * @param acceleration the new acceleration of the robot wheels.
	 */
	public void setAccelerations(int acceleration){
		leftMotor.setAcceleration(acceleration);
		rightMotor.setAcceleration(acceleration);
	}
}
