package proj1b.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import proj1b.ssm.Session;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.SocketTimeoutException;

import com.amazonaws.services.ec2.model.Instance;

public class RPCClient {
	// private static int callID = 0;

	private static final Logger LOGGER = Logger.getLogger("RPC client logger");

	public Session sessionNoOp(String sessionID, int versionNumber, List<String> svrIDs) {
		for (String svrID : svrIDs) {
			DatagramSocket socket = null;
			try {
				svrID = "127.0.0.1"; // For RPC test

				socket = new DatagramSocket();
				socket.setSoTimeout(RPCConfig.SOCKET_TIMEOUT);
				byte[] sendBytes = RPCStream
						.marshall(
								String.join(RPCConfig.RPC_DELIMITER,
										new String[] { String.valueOf(RPCConfig.callID),
												String.valueOf(RPCConfig.NO_OP_CODE), sessionID,
												String.valueOf(versionNumber) }));
				DatagramPacket sendPkt = new DatagramPacket(sendBytes, sendBytes.length, InetAddress.getByName(svrID),
						RPCConfig.SERVER_PORT);
				LOGGER.info("Packet ready to send.");
				socket.send(sendPkt);
				LOGGER.info("Packet sent to server.");

				byte[] inBuf = new byte[RPCConfig.MAX_PACKET_LENGTH];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);

				String[] recvInfo = null;
				// String serverID =
				// RPCConfig.getServerID(recvPkt.getAddress().getHostAddress());
				String serverID = "127.0.0.1";
				try {
					do {
						recvPkt.setLength(inBuf.length);
						LOGGER.info("Ready to receive reply.");
						socket.receive(recvPkt);
						LOGGER.info("Reply received.");

						recvInfo = RPCStream.unmarshall(recvPkt.getData()).split(RPCConfig.RPC_DELIMITER);
						LOGGER.info("Server response: " + RPCStream.unmarshall(recvPkt.getData()));
					} while (serverID.compareTo(svrID) != 0 || !RPCConfig.isValidID(Integer.parseInt(recvInfo[0]))
							|| Integer.parseInt(recvInfo[1]) != RPCConfig.RPC_RESPONSE_OK);
				} catch (SocketTimeoutException e) {
					System.out.println("Socket Timeout: " + svrID);
					recvPkt = null;
					continue;
				}

				RPCConfig.callID++;
				// return Session.decode(recvInfo[2]);
				return null;
			} catch (IOException e) {
				System.out.println("IOException in communicating with server ID: " + svrID);
				e.printStackTrace();
			} finally {
				socket.close();
			}
		}

		RPCConfig.callID++;
		return null;
	}

	/**
	 * This function implements RPC sessionRead by go over all the IP addresses
	 * in the list and send the RPC call sequentially. If we receive one reply
	 * message, then return the session by decoding the stream bytes. If we
	 * didn't receive the message for all addresses, return null.
	 * 
	 * @param sessionID
	 *            Session ID to be included in the read RPC.
	 * @param versionNumber
	 *            Version Number to be included in the read RPC.
	 * @param svrIDs
	 *            A list of IP addresses which might contain the session
	 *            information.
	 * @return The session fetched from other nodes.
	 */
	public Session sessionRead(String sessionID, int versionNumber, List<String> svrIDs) {
		for (String svrID : svrIDs) {
			DatagramSocket socket = null;
			try {
				svrID = "127.0.0.1"; // For RPC test

				socket = new DatagramSocket();
				socket.setSoTimeout(RPCConfig.SOCKET_TIMEOUT);
				byte[] sendBytes = RPCStream
						.marshall(
								String.join(RPCConfig.RPC_DELIMITER,
										new String[] { String.valueOf(RPCConfig.callID),
												String.valueOf(RPCConfig.READ_CODE), sessionID,
												String.valueOf(versionNumber) }));
				DatagramPacket sendPkt = new DatagramPacket(sendBytes, sendBytes.length, InetAddress.getByName(svrID),
						RPCConfig.SERVER_PORT);
				LOGGER.info("Packet ready to send.");
				socket.send(sendPkt);
				LOGGER.info("Packet sent to server.");

				byte[] inBuf = new byte[RPCConfig.MAX_PACKET_LENGTH];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);

				String[] recvInfo = null;
				// String serverID =
				// RPCConfig.getServerID(recvPkt.getAddress().getHostAddress());
				String serverID = "127.0.0.1";
				try {
					do {
						recvPkt.setLength(inBuf.length);
						LOGGER.info("Ready to receive reply.");
						socket.receive(recvPkt);
						LOGGER.info("Reply received.");

						recvInfo = RPCStream.unmarshall(recvPkt.getData()).split(RPCConfig.RPC_DELIMITER);
						LOGGER.info("Server response: " + RPCStream.unmarshall(recvPkt.getData()));
					} while (serverID.compareTo(svrID) != 0 || !RPCConfig.isValidID(Integer.parseInt(recvInfo[0]))
							|| Integer.parseInt(recvInfo[1]) != RPCConfig.RPC_RESPONSE_OK);
				} catch (SocketTimeoutException e) {
					System.out.println("Socket Timeout: " + svrID);
					recvPkt = null;
					continue;
				}

				RPCConfig.callID++;
				return Session.decode(recvInfo[2]);
			} catch (IOException e) {
				System.out.println("IOException in communicating with server ID: " + svrID);
				e.printStackTrace();
			} finally {
				socket.close();
			}
		}

		RPCConfig.callID++;
		return null;
	}

	/**
	 * This function implements RPC sessionWrite by go over all the IP addresses
	 * in the list and send the RPC call sequentially. If we receive WQ replies
	 * message, then we are done. If not, there is not enough backups.
	 * 
	 * @param session
	 *            The session we need to write to other servers.
	 * @param svrIDs
	 *            A list of IP addresses which might contain the session
	 *            information.
	 */
	public List<String> sessionWrite(Session session, List<String> svrIDs) {
		List<String> backupList = new ArrayList<String>();
		
		for (String svrID : svrIDs) {
			DatagramSocket socket = null;
			try {
				svrID = "127.0.0.1"; // For RPC test
				
				socket = new DatagramSocket();
				socket.setSoTimeout(RPCConfig.SOCKET_TIMEOUT);
				
				byte[] sendBytes = RPCStream.marshall(String.join(RPCConfig.RPC_DELIMITER, new String[] {
						String.valueOf(RPCConfig.callID), String.valueOf(RPCConfig.WRITE_CODE), session.encode() }));
				DatagramPacket sendPkt = new DatagramPacket(sendBytes, sendBytes.length, InetAddress.getByName(svrID),
						RPCConfig.SERVER_PORT);
				
				LOGGER.info("Packet ready to send.");
				socket.send(sendPkt);
				LOGGER.info("Packet sent to server.");

				byte[] inBuf = new byte[RPCConfig.MAX_PACKET_LENGTH];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);

				String[] recvInfo = null;
				String serverID = null;
				try {
					do {
						recvPkt.setLength(inBuf.length);
						LOGGER.info("Ready to receive reply.");
						socket.receive(recvPkt);
						LOGGER.info("Reply received.");

						serverID = "127.0.0.1";
						recvInfo = RPCStream.unmarshall(recvPkt.getData()).split(RPCConfig.RPC_DELIMITER);
						LOGGER.info("Server response: " + RPCStream.unmarshall(recvPkt.getData()));
					} while (serverID.compareTo(svrID) != 0
							|| RPCConfig.isValidID(Integer.parseInt(recvInfo[0])) && addToList(backupList, serverID) <= 2);
					// 2 Should be WQ
				} catch (SocketTimeoutException e) {
					System.out.println("Socket Timeout: " + svrID);
					recvPkt = null;
					continue;
				}
			} catch (IOException e) {
				System.out.println("IOException in communicating with server ID: " + svrID);
				e.printStackTrace();
			} finally {
				socket.close();
			}
		}

		RPCConfig.callID++;
		if (backupList.size() <= 2) { // Again, 2 should be WQ
			LOGGER.info("Not enough backup.");
			return null;
		}
		else {
			backupList.add("127.0.0.1"); // Add myself
			return backupList;
		}
	}
	
	private int addToList(List<String> list, String item) {
		list.add(item);
		return list.size();
	}
	
	//
	// public void sessionUpdate(Session session, List<String> svrIDs) {
	// for (String svrID : svrIDs) {
	// DatagramSocket socket = null;
	// try {
	// socket = new DatagramSocket();
	// byte[] sendBytes =
	// RPCStream.marshall(String.join(RPCConfig.RPC_DELIMITER, new String[] {
	// String.valueOf(RPCConfig.callID), String.valueOf(RPCConfig.WRITE_CODE),
	// session.encode() }));
	// DatagramPacket sendPkt = new DatagramPacket(sendBytes, sendBytes.length,
	// InetAddress.getByName(svrID),
	// RPCConfig.SERVER_PORT);
	// socket.send(sendPkt);
	// } catch (IOException e) {
	// System.out.println("IOException in communicating with server ID: " +
	// svrID);
	// e.printStackTrace();
	// } finally {
	// socket.close();
	// }
	// }
	//
	// RPCConfig.callID++;
	// }
}
