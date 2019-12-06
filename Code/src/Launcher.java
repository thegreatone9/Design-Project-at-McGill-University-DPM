import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Sound;

/**
 * Launcher class that handles launching projectiles.
 * includes different modes of launching.
 * @author Bobak Hamed-Baghi, Victor Repkow
 * @version V1.1 Revision 2
 * 

 */
public class Launcher {

	/** Reference to motor that handles the launcher*/
	private final static NXTRegulatedMotor launcherMotor = Motor.C;
	
	/** Speed at which to run the motor
	 * ensures correct re-chambering of the launcher
	 */
	private int launchSpeed = 250;
	
	
	/**
	 * Performs a single shot from the launcher.
	 */
	public static void shoot() {

		launcherMotor.forward();
		launcherMotor.setSpeed(200);
		launcherMotor.rotate(-360);
	}
	
	/**
	 * Method to shoot the ball desired number of times 
	 * Requires a manual button press before shooting the next ball
	 * @param shots number of shots to perform
	 */
	public void pauseLaunch(int shots){
		launcherMotor.setSpeed(launchSpeed);
		for (int i = 0; i < shots; i++){
			//Display the launch on screen
			LCD.drawString("Launch", 8, 0);
			LCD.drawInt(i+1, 8, 1);
			//Wait for button press before launching
			Button.waitForAnyPress();
			//Rotate only 360 and wait before next launch
			launcherMotor.rotate(-360);
		}
	}
		
		/**
		 * Method to shoot the ball desired number of times (without button press req.)
		 * @param shots number of shots to perform
		 */
		public void autoLaunch(int shots){
			launcherMotor.setSpeed(launchSpeed);
			for (int i = 0; i < shots; i++){
				//Display launch on screen
				LCD.drawString("Launch", 8, 0);
				LCD.drawInt(i+1, 8, 1);
				//Go through one rotation (one launch)
				launcherMotor.rotate(-360);
				//Wait 2 seconds before next launch
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		/**
		 * returns the position data depending on the input target parameters.
		 * @param x the X coordinate of the target 
		 * @param y the Y coordinate of the target
		 * @return the position and orientation of the robot required for launching.
		 */
		public double[] getLaunchPos(int x, int y){
			double[] dest = new double[3];
			if (x<=8 && x>=6){
				dest[0] = 30*x+32.7;
				dest[1] = 30*y-140;
				dest[2] = 346.4;
			}
			else if (x>=8 && x<=11){
				dest[0] = 30*x-1.14;
				dest[1] = 30*y-143.77;
				dest[2] = 0;
			}
			else if (x>=11 && x<=13 && y<=15 && y>=12){
				dest[0] = 30*x-75;
				dest[1] = 30*y-122.65;
				dest[2] = 30.986;
			}
			else if(y<=13 && y>=11 && x<=15 && x>=12){
				dest[0] = 30*x+-122.65;
				dest[1] = 30*y-75;
				dest[2] = 38.106;
			}
			else if (y<=11 && y>=8 && x>=13 && x<=15){
				dest[0] = 30*x-143.77;
				dest[1] = 30*y+1.14;
				dest[2] = 90;
			}
			else if (y<=8 && y>=6 && x<=15 && x>=12){
				dest[0] = 30*x-142;
				dest[1] = 30*y+22.49;
				dest[2] = 98.546;
			}
			else if (x>=13 && x<=14 && y>=13 && y<=14){
				dest[0] = 30*x-101.66;
				dest[1] = 30*y-101.66;
				dest[2] = 44.546;
			}
			else{
				Sound.twoBeeps();
			}
			return dest;
		}
}
