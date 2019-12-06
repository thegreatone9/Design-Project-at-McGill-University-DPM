import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;

/**
 * Class that corrects the odometer using the grid.
 * 
 * This thread makes use of the lightSensor to correct the position of the odometer.
 * It makes corrections each time a line is detected and overrites the values of the odometer
 * with the corrected values.
 * 
 * @author Bobak Hamed-Baghi, Victor Repkow.
 * @version V1.1 Revision 2
 *
 */
public class OdometryCorrection extends Thread {
	
	/**odometeryCorrection period value, controlls the frequency of which any potential error
	 * in the odometer is corrected by this class.
	 */
	private static final long CORRECTION_PERIOD = 50;
	private Odometer odometer;
	
	//Variables for ColorSensor
	private int[] Readings = new int[10];
	int TestRead;
	int mean =0;
	private LightListener lightListener;
	
	/**the constant which is considered to be the effective length of each tile on the floor */
	private final double CORR_CONSTANT = 30;
	
	/**Sensor Distance relative to origin
	 */
	public static double Dist = 11.8;
	

	/**
	 * default constructor, Initializes the variables.
	 * @param odometer 			the odometer which is used by this robot ( and hence corrected by this class)
	 * @param lightListener	 	the lightListener that controls the light sensor used by this class.
	 */
	public OdometryCorrection(Odometer odometer, LightListener lightListener) {
		this.odometer = odometer;
		this.lightListener = lightListener;
		
	}
	
	/** helper method that performs a leftShift on the elements of an array.
	 * 
	 * @param myArray array to be leftshifted.
	 */
	public void Shift(int[] myArray){
		for(int k=0; k < myArray.length-1; k++)
		{
			myArray[k] = myArray[k+1];
		}
	}
	
	/**
	 * Helper method that calculates the mean of the elements inside an array.
	 * @param myArray the array that contains the dataset.
	 * @return the mean of the dataset.
	 */
	public int calcMean(int[] myArray){
		int sum = 0;
		for(int i=0; i<myArray.length; i++)
		{
			if(myArray[i] != 0)
				sum+= myArray[i];
		}
		
		return sum/myArray.length;
	}
	
	/**
	 * function that corrects the position of the odometer based on the readings of 
	 * the lightSensor.
	 * 
	 * called each time a line is detected.
	 */
	void DoCorrection(){
		
		double xError, yError;
		double lsX, lsY;
		double pos[] = new double[3];
		odometer.getPosition(pos);
		
		//caculate perceived (wrong) position of the lightsensor on the grid.
		lsX = Math.sin(pos[2]*Math.PI/180)*Dist + pos[0]; 
		lsY = Math.cos(pos[2]*Math.PI/180)*Dist + pos[1];
		
		//calculate the error between where the lightsensor is and where it should be
		 xError =  CORR_CONSTANT - ((lsX %CORR_CONSTANT)) ;
		 if(xError > CORR_CONSTANT/2)
			 xError = ((lsX %CORR_CONSTANT));
		 yError =  CORR_CONSTANT - ((lsY %CORR_CONSTANT)) ;
		 if(yError > CORR_CONSTANT/2)
			 yError = ((lsY %CORR_CONSTANT));
		
		//calculate the true position of the odometer based on the offset error of the light sensor.
		double posY = pos[1] + yError;
		double posX = pos[0] + xError;
		pos[1] = posY;
		pos[0] = posX;
		
		//update the position of the odometer
		if( Math.abs(xError) <= Math.abs(yError))
		{
			
			odometer.setPosition(pos, new boolean[] {false, true, false});
			LCD.drawString("Up", 0, 6);

		}
		
		else{
			odometer.setPosition(pos, new boolean[] {true, false, false});
			LCD.drawString("Right", 0, 6);
		}

	}

	/**
	 * run method required for thread. starts a loop constantly checking for lines.
	 * when not interrupted, will perform corrections by calling DoCorrection()
	 */
	public void run() {
		
		while(!Thread.interrupted())
	    {
			long correctionStart, correctionEnd;
			while (true) {
				correctionStart = System.currentTimeMillis();
				
				
				double [] pos = new double[3];
				
				if(getGridLine())
				{
					Sound.beep();
					DoCorrection();
				}

				// this ensure the odometry correction occurs only once every period
				correctionEnd = System.currentTimeMillis();
						try {
							Thread.sleep(CORRECTION_PERIOD);
					} catch (InterruptedException e) {
						// there is nothing to be done here because it is not
						// expected that the odometry correction will be
						// interrupted by another thread
					}
				}
			}
	    }
		
		

	/** function that detects wether a line has been seen or not. 
	 * this calls the diffGetGridLine() method from the lightListener. 
	 * 
	 * @return true if line has been detected, false otherwise.
	 */
	private boolean getGridLine(){
		return lightListener.diffGetGridLine();
	}
}