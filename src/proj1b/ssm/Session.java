package proj1b.ssm;

import java.util.*;

import proj1b.util.*;

public class Session{
	private String sessionID; //<ServerID, reboot_num, session id>
	private Integer versionNumber; // default to 0
	private String message; // default "Hello, User!"
	private long expirationTime; // creation time + time out time
	private List<String> locationData; // ip-ip-ip....
	
	public Session(Integer serID, Integer rebootNum, Integer sessID, List<String> locations){
		sessionID = serID.toString() + Constants.SESSION_DELIMITER + rebootNum.toString() + Constants.SESSION_DELIMITER + sessID.toString();
		versionNumber = 0;
		message = "Hello, User!";
		expirationTime = System.currentTimeMillis() + Constants.SESSION_TIMEOUT * 1000;
		locationData = locations;
	}
	
	// for testing
	public Session(String sessionID) {
		this(0, 0, Integer.parseInt(sessionID), null);
	}
	
	/**
	 * Encode session information as cookie value. For example, "1_2_4_11_0_3" 
	 * represents an instance of the fourth session created at SvrID 1 on its 
	 * second reboot, session version number 11, currently stored at SvrIDs 0 and 3.
	 * @return a string representing the cookie value. 
	 */
	public String encode(){
		StringBuilder res = new StringBuilder();
		res.append(sessionID);
		res.append(Constants.SESSION_DELIMITER);
		res.append(String.valueOf(versionNumber));
		for (int i = 0; i < locationData.size(); i++){
			res.append(Constants.SESSION_DELIMITER);
			res.append(locationData.get(i));
		}
		return res.toString();
	}
	
	public static Session decode(String encodedSession){
		List<String> fields = Arrays.asList(encodedSession.split(Constants.SESSION_DELIMITER));
		return new Session(Integer.parseInt(fields.get(0)), Integer.parseInt(fields.get(1)), 
				Integer.parseInt(fields.get(2)), fields.subList(5, fields.size()));
	}
	
	/**
	 * Redisplay the session message, with an updated session expiration time
	 */
	public void refresh(){
		versionNumber += 1;
		expirationTime = System.currentTimeMillis() + Constants.SESSION_TIMEOUT * 1000 + Constants.SESSION_TIMEOUT_DELTA;
	}
	
	/**
	 * Replace the message with a new one (that the user typed into an HTML form field), and display the (new) message and expiration time;
	 */
	public void replace(String msg){
		message = msg;
		versionNumber += 1;
		expirationTime = System.currentTimeMillis() + Constants.SESSION_TIMEOUT * 1000 + Constants.SESSION_TIMEOUT_DELTA;
	}
	
	public void logout(){
		message = "You have logged out.";
		this.expirationTime = System.currentTimeMillis();
	}
	
	public String getMessage(){
		return message;
	}

	public String getSessionID() {
		return sessionID;
	}
	
	public Integer getVersionNumber() {
		return versionNumber;
	}
	
	public long getExpirationTime(){
		return expirationTime;
	}
	
	public String getCookieValue(){
		return this.encode();
	}
	
	public List<String> getLocationData(){
		return locationData;
	}

	public int addLocation(String location) {
		locationData.add(location);
		return locationData.size();
	}
	
	public int getLocationDataNumber() {
		return locationData.size();
	}
	
	public int resetLocation(List<String> newLocations){
		locationData = newLocations;
		return locationData.size();
	}
	
}