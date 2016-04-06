package proj1b.ssm;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager{
	private static SessionManager instance = new SessionManager();
	private static Integer serverID; //TODO mapping
	private static Integer rebootNum; 	// TODO read reboot_num from file system
	private static Integer nextSessionID = 0;
	private static Map<String, Session> sessionDataTable = new ConcurrentHashMap<String, Session>();
	
	private SessionManager(){
		
	}
	
	public static SessionManager getInstance(){
		return instance;
	}
	
	// TODO garbage collection
	
	
	// From Shibo, for testing RPC
	
	private static final int TIME_TO_LIVE = 60;
	public static final String SESSION_DELIMITER = "#";
	
	public static int getTimeToLive() {
		return TIME_TO_LIVE;
	}
	
	public static void addToTable(Session session) {
		String key = session.getSessionID() + SESSION_DELIMITER + session.getVersionNumber();
		sessionDataTable.put(key, session);
	}
	
	public static void removeFromTable(Session session) {
		try {
			sessionDataTable.remove(session.getSessionID());
		}
		catch(Exception e) {
			System.out.println("Session: " + session.getSessionID() + " has already been removed.");
		}
	}
	
	public static Session getSession(String sessionName, int versionNumber) {
		String key = sessionName+"#"+versionNumber;
		if(!sessionDataTable.containsKey(key)) return null;
		return sessionDataTable.get(key);
	}
	
	public static Collection<Session> getTableValues() {
		return sessionDataTable.values();
	}
}