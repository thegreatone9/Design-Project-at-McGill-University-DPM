import lejos.nxt.Button;
import lejos.nxt.ColorSensor;
import lejos.nxt.SensorPort;
import lejos.util.Datalogger;


/**
 * Special version of lightListener. 
 * 
 * This is a simple class that contians only functions needed for
 * testing the behavior of the light sensors. Used only for testing purposes.
 * 
 * @author Bobak Hamed-Baghi, Victor Repkow.
 * @version V1.1 Revision 2
 *
 */
public class LightSensListener extends Thread{
	/** the color sensor which is used by this class (being tested) */
	private ColorSensor ls;
	
	/**Lejos Datalogger object which collects data for later transmission to a PC*/
	private Datalogger dl;
	
	/**Frequency of the LightSensor sensor reading*/
	private int period;
	
	/**
	 * Default constructor. Initializes all variables. 
	 * 
	 * @param period the period which the lightsensor will be reading at.
	 */
	public LightSensListener(int period){
		ls =  new ColorSensor(SensorPort.S2);
		this.period = period;
		dl = new Datalogger();
		ls.setFloodlight(true);
	}
	
	/**
	 * Starts the thread. 
	 * This starts a loop of data collection from the light sensor which is 
	 * stored in an array log in the class datalogger. This is then transmitted at the 
	 * end of the loop to a PC for analysis.
	 */
	public void run(){
		while(Button.ENTER.isUp()){
			
			//get value from sensor
			dl.writeLog(ls.getRawLightValue());
			//sleep the sensor to establish frequency of reading
			try{
				Thread.sleep(period);
			} catch(Exception e){}
		}
		dl.transmit();
	}

}
