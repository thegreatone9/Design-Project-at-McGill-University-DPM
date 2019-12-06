import lejos.nxt.Button;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import lejos.util.Datalogger;


/**
 * Class that encapsulates the ultrasonic sensor. Acts as intermediary between the sensor and the program and
 * ensures realtime data polling from the sensor. Used chiefely for sensor testing and characterization.
 * 
 * @author Bobak Hamed-Baghi, Victor Repkow
 * @Version V1.1 Revision 2
 *
 */
public class USlistener extends Thread{
	
	
	private UltrasonicSensor usLeft;
	private UltrasonicSensor usRight;
	private UltrasonicSensor usCenter;
	/** datalogger class that logs the entries of the ultrasonic sensor inside an array*/
	public Datalogger dl;
	
	/** while true, the data recording process will continue until otherwise interrupted*/
	public boolean bRecordData = true;
	
	//Frequency of the Ultarsonic sensor reading
	private int period;
	
	/**default constructor. initializes class variables.
	 * 
	 * @param period the operating period of the thread and ultimately the ultrasonic sensor.
	 */
	public USlistener(int period){
		usLeft =  new UltrasonicSensor(SensorPort.S4);
		usRight=  new UltrasonicSensor(SensorPort.S1);
		usCenter= new UltrasonicSensor(SensorPort.S2);
		this.period = period;
		dl = new Datalogger();
	}
	
	/**
	 * required for the operation of the thread. Called when the thread is started. 
	 * starts a data collection loop which is then recorded using the datalogger class.s
	 */
	public void run(){
		while(bRecordData){
			
			//get value from sensor
			dl.writeLog(usRight.getDistance(), usLeft.getDistance(), usCenter.getDistance());
			//sleep the sensor to establish frequency of reading
			try{
				Thread.sleep(period);
			} catch(Exception e){}
			
			
		}
		dl.transmit();
	}
	
}
