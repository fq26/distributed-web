package proj1b.ssm;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
	private static Map<String, Session> sessionDataTable = new ConcurrentHashMap<String, Session>();
	
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
	
	public static Session getSession(String sessionName, String versionNumber) {
		String key = sessionName+"#"+versionNumber;
		if(!sessionDataTable.containsKey(key)) return null;
		return sessionDataTable.get(key);
	}
	
	public static Collection<Session> getTableValues() {
		return sessionDataTable.values();
	}
}