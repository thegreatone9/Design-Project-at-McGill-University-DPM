
import lejos.nxt.ColorSensor;
import lejos.nxt.Sound;

/**
 * Buffer class between Color sensor and anything that uses it.
 * Adds extra functionality and filters that are not present in 
 * the default implementation of the color sensor.
 * 
 * @author Bobak Hamed-Baghi, Victor Repkow
 * @version V1.1 Revision 2
 */
public class LightListener extends Thread{
	
	/**Light sensor period length. Controlls the frequency at which the light sensor polls. */
	private final int LS_PERIOD = 20;
	
	/**Amount of data to collect in the filter data collector. More data adds resolution to the filter. */
	private final int FILTER_DATA_SIZE = 5;
	
	/**Triggering threshold that decides when the change in the data is "abrupt" enough. */
	private final int DIFF_THRESH_AMOUNT = 1;
	
	/** Array that contains the sensor data being processed by the filter*/
	private double[] lsData= new double[FILTER_DATA_SIZE];
	
	/** The color sensor which is used (and encapsulated) using this class */
	ColorSensor ls;
	
	/** Default constructor 
	 * 
	 * @param ls Light sensor to use 
	 */
	public LightListener(ColorSensor ls){
		this.ls = ls;
	}
	
	
	/** run method to start this thread. 
	 * this initiates a loop that constantly takes in sensor data 
	 * and stores it in the data array "lsData"
	 */
	public void run(){
		
		ls.setFloodlight(true);
		
		while(true){
			leftShiftArray(lsData);
			lsData[lsData.length-1]= ls.getRawLightValue();
			
			try{
				Thread.sleep(LS_PERIOD);
			} catch(Exception e){}
		}
	}
	
	/**
	 * method that obtains the rawLightValue read by the light sensor
	 * @return ColorSensor's reading of the light intensity
	 */
	public int getValue(){
		return ls.getRawLightValue();
	}
	
	/**
	 * helper method that performs a left shift operation on the 
	 * members of an array. 
	 * 
	 * used for maintaining a moving window data set lsData 
	 * which is used by the filter to detect lines
	 * 
	 * @param array the array to perform left shift on.
	 */
	public void leftShiftArray(double[] array){
		for(int i=1; i<array.length; i++){
			array[i -1]= array[i];
		}
		array[array.length-1]=0;
	}
	
	/**
	 * Light sensor's differential filter.
	 * 
	 * this method takes the dataSet of the lightsensor readings, lsData
	 * and performs a differentiation operation on each element to determine wether
	 * a line has been detected or not depending on a threashold value DIFF_THRESH_AMOUNT
	 * 
	 * @return true if line has been detected, false otherwise
	 */
	public boolean diffGetGridLine(){
		double Data[] = lsData;
		int counter=0;
		for(int i=0; i<Data.length-1; i++){
			if((Data[i+1]-Data[i])/LS_PERIOD > DIFF_THRESH_AMOUNT){
				counter++;
			}
			
			if((Data[i+1]-Data[i])/LS_PERIOD < -DIFF_THRESH_AMOUNT){
				counter--;
			}
		}
		if(counter >0){
			Sound.beep();
			return true;
		}
		return false;
	}


}
