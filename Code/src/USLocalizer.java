import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.UltrasonicSensor;

/**
 * Localizer class that handles approximate localization. Makes use of the ultrasonic sensor chiefly.
 * 
 * @author Bobak Hamed-Baghi, Victor Repkow.
 * @version V1.1 Revision 2
 *
 */
public class USLocalizer {
	/**enum defining the type of the localization used by the class.
	 */
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	
	/**speed at which rotation is done while performing ultrasonic localization.
	 * This is tor ensure no sensor errors and necessary precision.*/
	public static double ROTATION_SPEED = 30;

	//Class references.
	private Odometer odo;
	private TwoWheeledRobot robot;
	private UltrasonicSensor us;
	private LocalizationType locType;
	private Navigation nav;
	
	private int usData[] = new int[5];
	
	//Value to clip US results by
	private int clippingValue =55; 
	private int marginValue =2;
	
	//value to rotate motors by
	private int rotateSpeed = 40;
	int usTrialCounter;
	
	/**
	 * default constructor. Initializes class variables.
	 * @param odo the odometer instance which is used by the robot.
	 * @param us the ultrasonic sensor instance which is used by the robot.
	 * @param locType the type of localizatin to use (either RISING_EDGE or FALLING_EDGE)
	 */
	public USLocalizer(Odometer odo, UltrasonicSensor us, LocalizationType locType) {
		this.odo = odo;
		this.robot = odo.getTwoWheeledRobot();
		this.us = us;
		this.locType = locType;
		this.nav = odo.getNavigation();
		usTrialCounter =5;
		// switch off the ultrasonic sensor
		us.off();
	}
	
	/**
	 * method that starts the localization process. This method performs ultrasonic localizatiion ONLY. the
	 * lightLocalization should be scheduled accordingly to be performed after this task in the taskScheduler.
	 */
	public void doLocalization() {
		double [] pos = new double [3];
		double angleA, angleB ;
		
		
		if (locType == LocalizationType.FALLING_EDGE) {
			
			// rotate the robot until it sees no wall
			while(getFilteredData() < clippingValue+10){
				this.robot.setSpeeds(0, -rotateSpeed);
			}
			

			//TODO HANDLE ODO THETA CHANGES
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(getFilteredData() > clippingValue){
				this.robot.setSpeeds(0, rotateSpeed);
			}
			odo.getPosition(pos);
			angleA = pos[2];
			LCD.drawString(String.valueOf(angleA), 0, 5);
				
			// switch direction and wait until it sees no wall
			while(getFilteredData() < clippingValue+10){
				this.robot.setSpeeds(0, -rotateSpeed);
			}
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(getFilteredData() > clippingValue){
				this.robot.setSpeeds(0, -rotateSpeed);
			}
			
			odo.getPosition(pos);
			angleB = pos[2];
			LCD.drawString(String.valueOf(angleB), 0, 6);
			robot.setSpeeds(0,0);
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			odo.getPosition(pos);
			if(angleA < angleB)
				//pos[2] =  (45 + ((angleA+angleB))/2);
				pos[2] =  pos[2] + 230 -((angleA+angleB))/2;
			else
				pos[2] = pos[2] + 50 -((angleA+angleB))/2;
			

			
			// update the odometer position (example to follow:)
			
			//odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
			odo.setPosition(pos, new boolean [] {false, false, true});

			
		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			
			//value to hold when the robot has entered the noise margin

			// rotate the robot until it sees the wall
			
			//Current and previous values for the margins.
						double prevMarginVal;
						double currentMarginVal;
						
						
						//Rotate the robot until it sees a wall
						while(getFilteredData() > clippingValue-15){
							this.robot.setSpeeds(0, -rotateSpeed);
						}
						

						
						// keep rotating until the robot doesn't see a wall, then latch the angle
						while(getFilteredData() < clippingValue-marginValue){
							this.robot.setSpeeds(0, rotateSpeed);
						}
						odo.getPosition(pos);
						prevMarginVal = pos[2];
						
						while(getFilteredData() < clippingValue+marginValue){
							this.robot.setSpeeds(0, rotateSpeed);
						}
						odo.getPosition(pos);
						currentMarginVal = pos[2];
						
						angleA = (currentMarginVal+prevMarginVal)/2;
						


							
						// switch direction and wait until it sees a wall
						while(getFilteredData() > clippingValue-15){
							this.robot.setSpeeds(0, -rotateSpeed);
						}

						
						// keep rotating until the robot doesnt see a wall, then latch the angle
						while(getFilteredData() < clippingValue-marginValue){
							this.robot.setSpeeds(0, -rotateSpeed);
						}
						     
						odo.getPosition(pos);
						prevMarginVal = pos[2];
						
						while(getFilteredData() < clippingValue+marginValue){
							this.robot.setSpeeds(0, -rotateSpeed);
						}
						
						odo.getPosition(pos);
						currentMarginVal = pos[2];
						
						
						angleB = (currentMarginVal + prevMarginVal)/2;


						// angleA is clockwise from angleB, so assume the average of the
						// angles to the right of angleB is 45 degrees past 'north'
						odo.getPosition(pos);
						if(angleA < angleB)
							//pos[2] =  (45 + ((angleA+angleB))/2);
							pos[2] =  pos[2] + 47.7 -((angleA+angleB))/2;
						else
							pos[2] = pos[2] + 227.7 -((angleA+angleB))/2;
						
						
						// update the odometer position (example to follow:)
						
						//odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
						odo.setPosition(pos, new boolean [] {false, false, true});
						robot.setSpeeds(0,0);
						
						
		}
		
		//Do calbration after finding the approximate angle.
		doInitialPosCalibration();
	}

	
	/**
	 * gets the data from the ultrasonic sensor through an outlier-filter.
	 * 
	 * @return the filtered ultrasonic sensor values.
	 */
	private int getFilteredData() {
		int distance;

		// do a ping
		us.ping();
		
		// wait for the ping to complete
		try { Thread.sleep(50); } catch (InterruptedException e) {}
		
		// there will be a delay here
		distance = us.getDistance();
		this.leftShiftArray(usData);
		if(distance > 250){
			if(usTrialCounter ==0){
				usData[usData.length-1]=distance;
			}
			else{
				usTrialCounter--;
				usData[usData.length-1]=usData[usData.length-2];
			}
		}
		
		else{
			if(usTrialCounter<5)
				usTrialCounter=5;
			usData[usData.length-1]=distance;
		}
		//filter
		if(usData[usData.length-1] > 250)
			return 250;
		else
		return usData[usData.length-1];
	}
	
	/**
	 * 	finds approximate position of robot, required for ls localization
	 */
	private void doInitialPosCalibration(){
		double pos[] = new double[3];
		nav.turnTo(270);
		pos[0] = -20 + getFilteredData();
		nav.turnTo(180);
		pos[1] = -20 + getFilteredData();
		
		
		odo.setPosition(pos, new boolean[] {true,true, false});
		LCD.drawString("x =" + pos[0], 0, 1);
		LCD.drawString("y =" + pos[1], 0, 2);
		nav.travelTo(-5,-5);
		
	}
	
	
	/**helper function to left shift an array input
	 * 
	 * @param usData2 array to be leftshifted
	 */
	private void leftShiftArray(int[] usData2){
		for(int i=1; i<usData2.length; i++){
			usData2[i -1]= usData2[i];
		}
		usData2[usData2.length-1]=0;
	}

}
