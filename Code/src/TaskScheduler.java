import lejos.nxt.Button;
import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.NXTConnection;
import lejos.nxt.comm.RConsole;
import lejos.util.Datalogger;

/**
 * Core class that handles the scheduling and execution of the steps given to the robot
 * in an orderly fashion.
 * @author Bobak Hamed-Baghi, Victor Repkow
 * @version V1.1 revision 2
 *
 */
public class TaskScheduler {
	
	

	 /*** sensor frequency control for ultrasonic sensor*/
	public static int usPeriod = 20;
	
	/** sensor frequency control for colorSensor */
	public static int lsPeriod = 20;
	
	//datalogging connections
	/** the connection used to connect to the datalogger to collect the stored data*/
	public static NXTConnection conn;
	/** the datalogger used to collect and store information for later retrieval */
	public static Datalogger dl = new Datalogger();
	
	//navigation class for 
	private static Navigation nav;
	
	
	//***********MAPS********************//
	
	//MAP1
	/** Map 1 X Coordinates*/
	public static final double[] MAP1_COORDS_X={15,15,45,45,-15,-15,15,15,75,75,300};
	/** Map 1 Y Coordinates*/
	public static final double[] MAP1_COORDS_Y={15,75,75,135,135,195,195,225,225,315,315};
	
	//MAP2
	/** Map 2 X Coordinates*/
	public static final double[] MAP2_COORDS_X={15,15,75,75,45,45,-15,-15,300};
	/** Map 2 Y Coordinates*/
	public static final double[] MAP2_COORDS_Y={15,75,75,165,165,255,255,315,315};
	
	//MAP3
	/** Map 3 X Coordinates*/
	public static final double[] MAP3_COORDS_X={-15,-15,75,75,-15,-15,15,15,300};
	/** Map 3 Y Coordinates*/
	public static final double[] MAP3_COORDS_Y={15,135,135,225,225,285,285,315,315};
	
	//Launch locations
	//first launch
	/**first launching X position*/
	private static final int firstLaunchX=8;
	/**first launching Y position*/
	private static final int firstLaunchY=14;
	
	//second launch
	/**second launching X position*/
	private static final int secondLaunchX=14;
	/**second launching X position*/
	private static final int secondLaunchY=10;
	
	
	/**
	 * Main function of the program
	 * 
	 * Individual tasks are fed in under tasks section to 
	 * be performed.
	 */
	public static void main(String Args[]){
		
		
		//***************Creating CLasses**********//
		
		//Sensors & related controllers
		UltrasonicSensor usMiddle = new UltrasonicSensor(SensorPort.S2);
		ColorSensor colorSensor	= new ColorSensor(SensorPort.S4);
		LightListener lightListener= new LightListener(colorSensor);
		
		//robotometery
		TwoWheeledRobot patBot = new TwoWheeledRobot(Motor.B, Motor.A);
		Odometer odo = new Odometer(patBot, true, colorSensor);
		OdometryCorrection odoCorr = new OdometryCorrection(odo, lightListener);
		
		//Localizer Classes
		USLocalizer usLocalizer = new USLocalizer(odo, usMiddle, USLocalizer.LocalizationType.RISING_EDGE);
		LightLocalizer lightLocalizer = new LightLocalizer(odo, lightListener );
		
		//LCD
		LCDInfo lcd = new LCDInfo(odo);
		
		//Launcher
		Launcher launcher = new Launcher();
		
		//*********************INITIALIZATIONS***************//
		lightListener.start();
		nav = odo.getNavigation();
		
		
		//***************PRE-TASK PROMPT***********************//
		Button.waitForAnyPress(); 
		
		
		//********************TASKS****************************//
		
		//FIRST LOCALIZATION
		usLocalizer.doLocalization();
		nav.turnTo(225.0);
		patBot.setSpeeds(0, 0);
		lightLocalizer.doLocalization();
		
		//***************Navigation*******************
		
		//MAP 1
//		double targetSeqX[] = MAP1_COORDS_X;
//		double targetSeqY[] = MAP1_COORDS_Y;
		
		//MAP 2
//		double targetSeqX[] = MAP2_COORDS_X;
//		double targetSeqY[] = MAP2_COORDS_Y;
		
		//MAP 3
		 double targetSeqX[] = MAP3_COORDS_X;
		 double targetSeqY[] = MAP3_COORDS_Y;
		
		nav.travelToSequence(targetSeqX, targetSeqY);
	
		//SECOND LOCALIZATION
		usLocalizer.doLocalization();
		nav.turnTo(225.0);
		patBot.setSpeeds(0, 0);
		lightLocalizer.doLocalization();
		
		nav.travelTo(0, 0);
		nav.turnTo(0);
		
		/*
		 * the odometer values get reset here
		 * to the correct values
		 * this allows recycling the localization from the first step.
		 */
		double tempPos[] = new double[3];
		odo.getPosition(tempPos);
		tempPos[0]= tempPos[0]+300;
		tempPos[1]= tempPos[1]+300;
		tempPos[2]= tempPos[2]+180;
		odo.setPosition(tempPos, new boolean[]{true, true, true});

		
		//***************LAUNCH ATTEMPTS********************/
		double[] launchDestionations = new double[3];
		
		//first launch
		launchDestionations = launcher.getLaunchPos(firstLaunchX, firstLaunchY);
		nav.travelTo(launchDestionations[0], launchDestionations[1]);
		nav.turnTo(launchDestionations[2]);
		launcher.autoLaunch(3);
		
		//second launch
		launchDestionations = launcher.getLaunchPos(secondLaunchX, secondLaunchY);
		nav.travelTo(launchDestionations[0], launchDestionations[1]);
		nav.turnTo(launchDestionations[2]);
		launcher.autoLaunch(3);
		
		
		//travel Back to the original starting position
		
		//Restack the waypoints stack
		targetSeqX = invertArray(targetSeqX);
		targetSeqY = invertArray(targetSeqY);
		
		//traveling back
		nav.travelToSequence(targetSeqX, targetSeqY);
		nav.travelTo(0, 0);
		nav.turnTo(0);

		
		
		//*****************************END OF TASK PROMPT********************//
		Button.waitForAnyPress();

		

}
	
	/**
	 * takes an array stack and reverses the order of elements
	 * @param a the double array to be re-stacked
	 * @return returns the re-stacked array
	 */
	public static double[] invertArray(double[] a){
		double[] b= new double[a.length]; 
		for(int i=0; i<a.length; i++){
			b[i]= a[a.length-i-1];
		}
		return b;
	}
	
	
	/**
	 * Helper function that encapsulates a desired navigation test.
	 * put test code inside and execute under the tasks section in Main()
	 */
	private static void navTest(){
		for(int i=0; i<10; i++){
			nav.turnTo(80);
			nav.turnTo(-80);
		}
		Button.waitForAnyPress();
		nav.turnTo(0);
		nav.travelTo(0,60);
	}
	

	
}