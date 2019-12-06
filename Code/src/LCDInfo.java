import lejos.nxt.LCD;
import lejos.util.Timer;
import lejos.util.TimerListener;

/**
 * LCD class that handles the display of the robot.
 * used to display odometer data.
 * @author Bobak Hamed-Baghi, Victor Repkow
 * @version V1.1 Revision 2
 * 

 *
 */
public class LCDInfo implements TimerListener{
	
	/** refresh rate of the LCD*/
	public static final int LCD_REFRESH = 100;
	
	/** reference to the odometer of the robot*/
	private Odometer odo;
	
	/**timer that handles execution of tasks at each refresh rate*/
	private Timer lcdTimer;
	
	/**array for displaying data*/
	private double [] pos;
	
	/**
	 * Default constructor
	 * 
	 * initializes all required parameter such as
	 * odometer and the lcd Refresh timer.
	 * @param odo odometer that is being used by the robot
	 */
	public LCDInfo(Odometer odo) {
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		
		// initialise the arrays for displaying data
		pos = new double [3];
		
		// start the timer
		lcdTimer.start();
	}
	
	/**
	 * method that is called at the end of each refresh rate.
	 * handles the drawing of new data on the display.
	 */
	public void timedOut() { 
		odo.getPosition(pos);
		LCD.clear();
		LCD.drawString("X: ", 0, 0);
		LCD.drawString("Y: ", 0, 1);
		LCD.drawString("H: ", 0, 2);
		LCD.drawInt((int)(pos[0] ), 3, 0);
		LCD.drawInt((int)(pos[1] ), 3, 1);
		LCD.drawInt((int)pos[2], 3, 2);
	}
}
