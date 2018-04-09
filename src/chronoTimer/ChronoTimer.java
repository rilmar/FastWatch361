package chronoTimer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import chronoSimulator.ChronoTimerSimulator;
//other possible imports

/**
 *  The Sport Timing system that will perform specific actions provided by certain commands from the simulator (either from
 *  file or console input). It will record and keep track of racers and runs of multiple sporting racing events. The simulator and hardware will only 
 *  have access to use the execute method of this system to run and execute.
 * @authors Isaac Kadera, Philip Sauvey and Fue Her 
 *
 */

public class ChronoTimer {

	private class Channel{
		private boolean isActive;
		private Sensor sensor;
		
		protected Channel(){
			isActive=false;
			sensor = null;
		}
		
		protected boolean getState(){
			return isActive;
		}
		
		protected void toggle(){
			isActive=!isActive;
		}
		
		protected Sensor getSensor(){
			return this.sensor;
		}
		protected void setSensor(Sensor s){
			this.sensor = s;
		}
	}
	
	private class Sensor{
		String type;
		
		protected Sensor(String type){
			this.type = type;
		}
		
		protected String getType(){
			return this.type;
		}
	}
	//fields
	private boolean powerState; //CT on or off
	private Run	currentRun; //run in progress
	private ArrayList<Run> pastRuns; //run history
	private Channel[] channels; //8 sensor inputs
	private String log; //text of previous commands
	//private ArrayList<String> log; //
	//private Event currentEvent;
	private ChronoTimerSimulator sim; // Chronotimer simulator
	private String eventType; // their test text file has a EVENT command 
	private LocalTime currentTime;
	private ArrayList<Racer> queuedRacers;
	
	// used for testing
	private boolean showMessage = true; //  TRUE - print invalid sysout msg, FALSE - print no invalid sysout msg 
	/**
	 * Helper method for printing invalid input messages and always return false. 
	 * Used in methods that requires a boolean return value of false.
	 * @param message message to print what was invalid/incorrect
	 * @return false always
	 */
	private boolean printMessage(String message){
		log+=message+"\n";
		if(showMessage == true){
			sim.execute(message); 
		}
		return false;
	}

	//field initialization (default constructor);
	public ChronoTimer(){ 
		powerState = false;
		currentRun = null;
		pastRuns = new ArrayList<Run>();
		channels = new Channel[8];
		for(int i = 0;i<8;i++) channels[i] = new Channel();
		log = "";
		eventType = "IND"; // default run event to start
		currentTime = null;
	}

	/**
	 * Toggles the ChronoTimer power on/off.
	 * @return True if the ChronoTimer is on and false if the ChronoTimer
	 * is off.
	 */
	private boolean power(){ 
		powerState = !powerState;
		return powerState;
	}

	/**
	 * Set the ChronoTimer to it's initial state
	 */
	private void reset(){
		currentRun = null;
		pastRuns = new ArrayList<Run>();
		channels = new Channel[8];
		for(int i = 0;i<8;i++) channels[i] = new Channel();
		log = "";
		eventType = "IND"; //default event 
		currentTime = null;
	}

	/**
	 * Connect a sensor to the specified channel
	 * @param newSensorChannel the channel to connect the sensor to
	 * @param sensorType the sensor type to connect
	 * @return The previous sensor at that channel, or null if there were no sensor before
	 */
	public Sensor conn(int newSensorChannel, String sensorType){
		Sensor oldInput = channels[newSensorChannel].getSensor();
		channels[newSensorChannel].setSensor(new Sensor(sensorType));
		printMessage("Connected Sensor of Type " + sensorType + " to channel " +(newSensorChannel+1));
		return oldInput;
	}

	/**
	 * Disconnect a channel
	 * @param disabledChannel the channel to be disconnected
	 * @return The sensor that was previously connected to that channel
	 */
	public Sensor disc(int disabledChannel){
		Sensor oldInput = channels[disabledChannel].getSensor();
		if(oldInput == null){
			printMessage("No sensor connected to that channel");
		}else{
			printMessage("Disconnecting " + oldInput.getType()+ " sensor from channel "+ (disabledChannel+1));
		}
		channels[disabledChannel].setSensor(null);
		return oldInput;
	}


	//	private event(String){
	//		
	//	}

	//changes to specified event and returns previous event
	//private Event setEvent(Event newEvent){
	//	oldEvent = currentEvent;
	//	currentEvent = newEvent;
	//	return oldEvent;
	//}

	//Event getter
	//private Event getEvent(){
	//	return currentEvent;
	//}

	/**
	 * Prints the time of all the 
	 * queued, running, and completed racers of the
	 * given run.
	 * 
	 * @param time time stamp of printing the run
	 * @param run the run to print
	 * @return String of all racers time in the run
	 */
	private String print(LocalTime time, Run run){
		return run.standings(time);
	}

	//prints out given run to a new file
	/*private void export(Run run){
		PrintWriter out;
		try{
			out= new PrintWriter(new FileWriter("C:\\location\\outputfile.txt")); 
			out.print(run.toString()); 
			out.println("world");
		}
		catch (IOException e){
			//do something?
			printMessage("Cannot open the file.");
		}
	}*/

	/**
	 * This will parse the command input and execute accordingly to what the command will have the system perform.
	 * It will only execute if the command is valid and can be performed depending on the state of the system is
	 * currently at. eg: You cannot create a new run unless the system is powered on.
	 * 
	 * @param c command to be parse and executed by the ChronoTimer
	 * @return True if the command input is parsed and executed successfully according to the current state of the
	 * system. It will return false whenever the command input cannot be executed either due to it's power is off
	 *  , the command cannot be executed at it's current state or invalid commands.
	 */
	public boolean execute(String c){
		String[] commands = c.split("\\s+");
		LocalTime time = null;
		
		if(commands.length < 2) return printMessage("Not enough arguments.");
		
		if(powerState == true){
			log+=c+"\n";
			time = parseTime(commands[0]);
			if (commands[1].equalsIgnoreCase("TIME")){
				if(commands.length != 3) return printMessage("Need a time to set. Should be after the time arg.");
				time = parseTime(commands[2]);
				if(time == null) return printMessage("Time is null");
					currentTime = time;
					return printMessage("Time has been set to " + time);
			}
			if (currentTime != null && currentTime.compareTo(time) > 0){
				return printMessage("Current Time is before system time. Use \"TIME\" command to set system time");
			}
			currentTime = time;
			switch(commands[1].toUpperCase()){
				case("POWER"):
					power();
					if(powerState == false) {
						reset();
						//printMessage("Command log:\n" + log);
						printMessage("ChronotTimer is now off.");
					}
					break; 
					
				case("CANCEL"): // Discard current run for racer and place racer back to queue
					if(currentRun == null) return printMessage("Cannot cancel run, there is currently no run.");
					try {
						currentRun.cancel();
					}catch(IllegalStateException e) {
						return printMessage(e.getMessage());
					}
					break;
					
				case("EVENT"): // Set Run to this particular event <TIME> <EVENT> <TYPE>
					if(commands.length != 3) return printMessage("Need a event type. Should be after the event arg");
				
					if(currentRun != null && currentRun.raceInProgress() == true) return printMessage("Must end the current run before assiging a event type.");
					
					String prevEvent = eventType;
					if(commands[2].equalsIgnoreCase("IND")) {
						eventType = "IND";
					}else if(commands[2].equalsIgnoreCase("PARIND")){
						eventType = "PARIND";
					}
					else{
						return printMessage("Unsupported event type.");
					}
					
					printMessage("Event set to " + eventType);
					if(currentRun != null && !eventType.equalsIgnoreCase(prevEvent)) {
						printMessage("Converting " + prevEvent + " run to " + eventType + " run");
						try {
							queuedRacers = currentRun.getQueue();
							newRun();
							for(int i = 0; i < queuedRacers.size(); i++) {
								currentRun.num(queuedRacers.get(i).getBibNum());
							}
						}catch(IllegalStateException e) {
							return printMessage("Error on converting run: " + e.getMessage());
						}
					}
					break;
					
				case("CONN"): // CONN <sensor> <NUM>
					if(commands.length!=4) return printMessage("Need a sensor type and channel number. Should be args 3 and 4 respectively.");
					
					try{
						int channelNum = Integer.parseInt(commands[3]);
						conn(channelNum-1, commands[2]);
					}catch(NumberFormatException ex){
						return printMessage("Error on parsing Channel to a number.");
					}
					break;
					
				case("DISC"): // CONN <sensor> <NUM>
					if(commands.length!=3) return printMessage("Need a channel number. Should be after the sensor arg.");
					
					try{
						int channelNum = Integer.parseInt(commands[2]);
						disc(channelNum-1);
					}catch(NumberFormatException ex){
						return printMessage("Error on parsing Channel to a number.");
					}catch(ArrayIndexOutOfBoundsException e) {
						return printMessage("Channel is not supported.");
					}
					break;
				
				case("NEWRUN"): // Create a new Run (must end a Run first)
					try {
						newRun();
					}catch(IllegalStateException e) {
						return printMessage("Cannot create new run. Must end a run first.");
					}
					break;
				
				case("ENDRUN"): // Done with a Run
					if(endRun() == false) return printMessage("There is no current run.");
					printMessage("Ending Run.");
					break;
				
				case("RESET"): // Reset system to initial state
					reset();
					break;
				
				case("NUM"): // Set	NUM<NUMBER> as the next	competitor to start.
					if(commands.length != 3) return printMessage("Need a bib number to set racer. Should be after the num arg.");
					if(currentRun == null) return printMessage("Cannot add racer, create a Run first.");
					try {
						int bibNum = Integer.parseInt(commands[2]);
						currentRun.num(bibNum);
						printMessage("Racer with bib number " + bibNum + " has been added successfully.") ;
					}catch(NumberFormatException e) {
						return printMessage("Error on parsing bib number to a number.");
					}catch(IllegalArgumentException e) {
						return printMessage(e.getMessage());
					}
					break;
				
				case("TOG"): // Toggle the state of the channel TOG<CHANNEL>
					if(commands.length != 3) return printMessage("Need a channel number to toggle.");
					try{
						int channel = Integer.parseInt(commands[2]);
						if(channel < 0 || channel >= 9){
							return printMessage("Channel Not Supported.");
						}
						channels[channel-1].toggle();
						String message = "Channel " + channel + " is now";
						if (channels[channel-1].getState()){
							message += " active";
						}else{
							message += " inactive";
						}
						printMessage(message);
					}catch(NumberFormatException e){
						return printMessage("Error on parsing Channel to a number.");
					}
					break;
					
				case("DNF"): // DNF says the run for the bib number is over, and their end time is DNF
					if(currentRun == null) return printMessage("Cannot set run to dnf. There is no run.");
					try {
						
						//if parind pass commands[2]
						String message = "";
						if(currentRun.type.equals("PARIND")) {
							try {
								if(commands.length != 3) return printMessage("Need a lane number to dnf racers.");
								int lane = Integer.parseInt(commands[2]);
								message = currentRun.dnf(lane);
							} catch (NumberFormatException e) {
								message = "Cannot read argument";
							}
						} else {
							message = currentRun.dnf();
						}
						
						printMessage(message);
					}catch(IllegalArgumentException e) {
						return printMessage(e.getMessage());
					}
					break;
				
				case("SWAP"): // Start trigger channel 1 (shorthand for TRIG 1)
					if(currentRun == null) return printMessage("Cannot swap racers. There is no run.");
					try {
						if(currentRun.type.equalsIgnoreCase("PARIND")){
							if(commands.length != 3) return printMessage("Need a lane number to swap racers.");
							int lane = Integer.parseInt(commands[2]);
							currentRun.swap(lane);
						}else
							currentRun.swap();
					}catch(IllegalArgumentException e) {
						return printMessage(e.getMessage());
					}
					break;
				
				case("CLR"): // Start trigger channel 1 (shorthand for TRIG 1)
					if(commands.length != 3) return printMessage("Need a bib number to remove racer. Should be after the num arg.");
					if(currentRun == null) return printMessage("Cannot add racer, create a Run first.");
					try {
						int bibNum = Integer.parseInt(commands[2]);
						currentRun.clr(bibNum);
						printMessage("Racer with bib number " + bibNum + " has been removed successfully.") ;
					}catch(NumberFormatException e) {
						return printMessage("Error on parsing bib number to a number.");
					}catch(IllegalArgumentException e) {
						return printMessage(e.getMessage());
					}
					break;
					
				case("TRIG"): // Trigger channel Trig<NUM>
					try{
						if(commands.length != 3) return printMessage("Need a channel number to trigger. Should be after the trig arg.");
						int channel = Integer.parseInt(commands[2]);
						if(currentRun == null) return printMessage("Must have current Run to trigger Channel. Please create a run first.");
						try {
							if(!channels[channel-1].getState())  return printMessage("Channel is inactive");
							printMessage(currentRun.trig(time, channel));
						}catch(IllegalStateException e) {
							return printMessage(e.getMessage());
						}
					}catch(NumberFormatException e){
						return printMessage("Error on parsing Channel to a number.");
					}
					break;
					
				case("START"): // Start trigger channel 1 (shorthand for TRIG 1)
					return execute(time.toString()+" TRIG 1");
					
				case("FINISH"): // Finish trigger channel 2 (shorthand for TRIG 2)
					return execute(time.toString()+" TRIG 2");
				
				case("PRINT"): // print <RUN>
					if(currentRun == null && pastRuns.isEmpty()) 
						return printMessage("Cannot print Run, there is no Run.");
				
					if(commands.length == 2) {
						if(currentRun == null) {
							printMessage("Printing previous run."); // sim.execute(msg)
							printMessage(print(time, pastRuns.get(pastRuns.size() - 1))); // print the previous run since user didn't specify the run
						}else {
							printMessage(print(time, currentRun)); // print current run
						}
					}else if(commands.length == 3) {
						try {
							int runNumber = Integer.parseInt(commands[2]);
							if(runNumber <= 0) return printMessage("Cannot print run. Run number starts at 1.");
							--runNumber; // decrement since we start at index 0
							printMessage("Printing run " + commands[2] +"\n"+ print(time, pastRuns.get(runNumber))); //print the specified run the user gave us
						}catch(NumberFormatException e) {
							return printMessage("Cannot parse string to a number.");
						}catch(IndexOutOfBoundsException e) {
							return printMessage("No such run exist in system history.");
						}
					}else {
						return printMessage("Invalid print command. Must be PRINT or PRINT <NUMBER>");
					}
						
					break;
					
				case("EXPORT"):
					if(currentRun == null && pastRuns.isEmpty()) return printMessage("Cannot export. There is no run in system history.");
					if(commands.length == 2){
						writeFile();
					}else if(commands.length == 3){
						try {
							int runNumber = Integer.parseInt(commands[2]);
							if(runNumber <= 0) return printMessage("Cannot export run. Run number starts at 1.");
							else if(runNumber > pastRuns.size()) return printMessage("Cannot export. No such run exist in system history.");
							--runNumber; // decrement since we start at index 0
							writeFile(runNumber);
						}catch(NumberFormatException e) {
							return printMessage("Cannot parse string to a number.");
						}
					}else {
						return printMessage("Invalid export command. Must be EXPORT or EXPORT <NUMBER>");
					}
					
				
					break;
					
				default:
					return printMessage("Invalid command.");
			}
			return true; // when a block of code break out of the switch, it will return true.
		}
		else{ // only listen for POWER since the power to the system is off
			time = parseTime(commands[0]);
			if(time == null)
				return false;
			if(commands[1].equalsIgnoreCase("POWER")){
				log+=c+"\n";
				power();
				printMessage("ChronoTimer is now on.");
				return true;
			}
			return printMessage("ChronoTimer is off. Please turn on the power before providing commands.");
		}
		
	}

	/**
	 * Helper method to parse the given string to time.
	 * @param commands string to be parsed to time
	 * @return The time that was provided. It will return null if the time cannot be parsed into a LocalTime object
	 */
	private LocalTime parseTime(String commands) {
		LocalTime time = null;
		try{
			time = LocalTime.parse(commands);
		}catch(DateTimeParseException e){
			printMessage("Something went wrong when parsing time.\nNote: Time should be the first arg in format of 00:00:00");
			return null;
		}
		return time;
	}

	/**
	 * Creates a new run if there is no current run. If no type of event is provided to the run, IND will be the 
	 * default run event type.
	 */
	private void newRun(){
		if (currentRun != null && currentRun.raceInProgress() == true){
			throw new IllegalStateException("run in progress");
		}
		if(eventType.equalsIgnoreCase("IND")) {
			currentRun = new IND(); 
		}else if(eventType.equalsIgnoreCase("PARIND")){
			currentRun = new PARIND();
		}
		printMessage("Run event: " + eventType + " created."); // sim.execute(msg);
	}

	/**
	 * Add the current run to the past run history and resets
	 * the current run to null. All racers that are currently running
	 * will be set to dnf ("Did Not Finish") state.
	 * @return False if there is no current run
	 */
	private boolean endRun(){
		if (currentRun == null){
			return false;
		}
		pastRuns.add(currentRun);
		try {
			currentRun.end();
		}catch(Exception e) {
			return printMessage(e.getMessage());
		}
		currentRun = null;
		return true;
	}

	public void setSim(ChronoTimerSimulator sim){
		this.sim = sim;
	}
	/*
	public void writeFile(){
		if (pastRuns.isEmpty()) return;
		Run mostRecentRun = pastRuns.get(pastRuns.size()-1);
		switch(mostRecentRun.type){
			case("IND"): 
				try{
					PrintWriter pw = new PrintWriter("RUN"+(pastRuns.size()-1)+".txt");
			//		pw.write(print(pastRuns.get(pastRuns.size()-1), );
				}catch(Exception e){
					
				}
				break;
			case("PARIND"):
				try{
					PrintWriter pw = new PrintWriter("RUN"+(pastRuns.size()-1)+".txt");
			//		pw.write(print(pastRuns.get(pastRuns.size()-1), );
				}catch(Exception e){
					
				}
				break;
				
		}
	}
	*/
	
	private void writeFile(){
		;
		File file;
		if(currentRun == null)
			file = new File("RUN" + pastRuns.size() + ".txt");
		else
			file = new File("currentRun.txt");
		PrintWriter pw;
		try{
			pw = new PrintWriter(file);
			if (pastRuns.isEmpty() && currentRun != null || !pastRuns.isEmpty() && currentRun != null){
				printMessage("Beginning to export current run...");
				pw.write(currentRun.export());
			}else{
				printMessage("Attempting to export previous run in system history...");
				Run mostRecentRun = pastRuns.get(pastRuns.size()-1);
				pw.write(mostRecentRun.export());
			}
			pw.close();
		}catch(Exception e){
			printMessage("Cannot write to file.\n" + e.getMessage());
			return;
		}
		printMessage("Data has been exported successfully to " + file.getAbsolutePath());
	}
	
	private void writeFile(int runNumber) {
		int rn = runNumber + 1; // user enter this number but we decrement it earlier before calling this method
		File file = new File("RUN"+(rn)+".txt");
		try {
			PrintWriter pw = new PrintWriter(file);
			printMessage("Attempting to export run " + rn + " from system history...");
			pw.write(pastRuns.get(runNumber).export());
			pw.close();
		} catch (Exception e) {
			printMessage("Cannot write to file.\n" + e.getMessage());
			return;
		}
		printMessage("Data has been exported successfully to " + file.getAbsolutePath());
	}
}