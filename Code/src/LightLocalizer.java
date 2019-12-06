
import lejos.nxt.LCD;
import lejos.nxt.Sound;

/**
 * class that handles the light localization of the robot
 * 
 * This localization typically follows the ultrasonic localization and serves
 * as a much more exact method of localizing using the light sensor and the grid lines.
 * 
 * @author Bobak Hamed-Baghi, Victor Repkow
 * @version V1.1 Revision 2
 *
 */
public class LightLocalizer {
	/** reference to the robot's odometer
	 * 
	 * essential since the odometer's values are read and overwritten in this class. 
	 */
	private Odometer odo;
	
	/**reference to the robot class of taskScheduler. */
	private TwoWheeledRobot robot;

	
	/** Color Sensor distance from the odometery center
	 * used in calculations in the localization process to determine the exact position
	 */
	private double lsDistance = 13.4 ;
	
	/** variable that stores the angle detected when hitting the x-axis */
	private double thetaX;
	
	/** variable that stores the angle detected when hitting the y-axis */
	private double thetaY;
	
	/**variable that stores the corrected x-position of the odometer.*/
	private double x;
	
	/**variable that stores the corrected y-position of the odometer.*/
	private double y;
	
	/**array that stores the angle of the odometer after each line is hit*/
	private double [] theta = new double[4];
	
	/**counter that keeps track of how many lines are hit*/
	private int gridLineNum = 0;
	
	/**amount by which the correction is shifted to improve precision of correction.
	 * this is decided an calibrated through testing.
	 */
	private final double MANUAL_ANGLE_SHIFT=95.8;
	
	/**Light listener class that holds the lightsensor used by this class*/
	private LightListener lightListener;
	
	
	/**
	 * Default constructor. Initializes the required references to other components of the robot.
	 * @param odo the odometer that is used by the robot (and is corrected).
	 * @param lightListener the lightListener that containts the light sensor used by this class.
	 */
	public LightLocalizer(Odometer odo, LightListener lightListener) {
		this.odo = odo;
		this.robot = odo.getTwoWheeledRobot();
		this.lightListener = lightListener;	
	}
	
	/**
	 * Performs light localization.
	 * 
	 * The robot starts rotating in a clockwise motion and
	 * detects lines. when four lines are detected, it uses trigonometery to calculate 
	 * the correction of its position and orientation
	 */
	public void doLocalization() {
		// start rotating and clock all 4 gridlines
		// do trig to compute position and orientation
		 
		double pos[] = new double[3];
		double deltaT;
		while (gridLineNum < 4) {
			odo.getPosition(pos);
			robot.setRotationSpeed(20);
			if (getGridLine()) {
				Sound.beep();
				theta[gridLineNum] = pos[2];
				gridLineNum++;
				
				try { 
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.robot.setRotationSpeed(0);	

			}	
		}


		
		odo.getPosition(pos);
		thetaX = theta[2]-theta[0];
		thetaY = theta[3]-theta[1];
		x = -lsDistance * Math.cos(Math.toRadians(thetaY/2)); 
		y = lsDistance * Math.cos(Math.toRadians(thetaX/2));
		
		LCD.drawString(String.valueOf(thetaY/2),0,5);
		
		deltaT = MANUAL_ANGLE_SHIFT - theta[3] + thetaY/2;
		pos[0] = x;
		pos[1] = y;
		pos[2] = pos[2] + deltaT;
		
		odo.setPosition(pos, new boolean[] {true,true,true});

		}
	
		
	/**
	 * true when grid line detected. This is verified using the lightListener's implemented
	 * differencial filter functionality.
	 * @return
	 */
	private boolean getGridLine(){
		return lightListener.diffGetGridLine();
	}

/**
 * converts an angle from degrees to radians
 * @param inAngle angle in degrees 
 * @return angle in radians
 */
	public double degToRad(double inAngle){
		return inAngle*Math.PI/180.0;
	}
}


