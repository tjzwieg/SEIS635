package cluster;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * This is the master control class which is in charge
 * of adding new server Machine information to the cluster
 * class. it is also in charge of making sure everything
 * closes on command.
 * @author Chris Hansohn
 * @version 1.6.1
 */
public class RunCluster extends Thread
{
	private Cluster cluster;
	private transient boolean active;
	//The next variables are used but the multicast system.
	private String mcAddress;
	private int mcPort;
	private MulticastSocket mcSock;
	private int serverPort;
	
	/**
	 * This is the constructor used to hand this
	 * control class a fully functional Cluster
	 * class object.
	 * @param cluster this is the class object of the cluster.
	 * The class that initializes it must pass it down.
	 */
	public RunCluster(Cluster cluster, int serverPort, String mcAddress, int mcPort)
	{
		this.mcAddress = mcAddress;
		this.mcPort = mcPort;
		multiCast();
		this.serverPort = serverPort;
		this.cluster = cluster;
		active = true;
	}
	
	/**
	 * The run class that takes care of all the multicast
	 * information of receiving data and sending data.
	 * When the loop ends it tells the CLuster class to end
	 * all threads and close all connections. then waits
	 * for it's child thread to end.
	 */
	public void run()
	{
		MulticastClient mcClient;
		MultiCastListen mcListen = new MultiCastListen(mcSock, this);
		mcListen.start();
		long startTime = 0;
		long stopTime = 1000;
		
		try
		{
			mcClient = new MulticastClient(mcAddress, mcPort, String.valueOf(serverPort));
			while(active)
			{
				if(stopTime - startTime >= 1000)
				{
					mcClient.sendPacket();
					cluster.cleanDoneThreads();
					startTime = System.currentTimeMillis();
				}
				stopTime = System.currentTimeMillis();
			}
		}
		catch (IOException e1) { e1.printStackTrace(); }
		mcListen.done();
		cluster.stop();
		try { mcListen.join(); }
		catch (InterruptedException e) { e.printStackTrace(); }
	}
	
	/**
	 * This method is called by the subclass when a
	 * packet is received through the multicast.
	 * @param packet The received packet from the subclass.
	 */
	public void addNewServer(DatagramPacket packet)
	{
		String portString;
		String address;
		int port;
		
		address = packet.getAddress().toString();
		address = address.substring(1);
		portString = new String(packet.getData(), 0, packet.getData().length);
		portString = portString.substring(4);
		portString = portString.trim();
		port = Integer.parseInt(portString);
		cluster.newClient(address, port);
	}
	
	/**
	 * The driver class uses this to tell this class to end
	 * all threads and connections.
	 */
	public void done() { active = false; }
	
	
	/**
	 * This initializes the multicast socket to be
	 * used by the subclass.
	 */
	private void multiCast()
	{
		try
		{
			mcSock = new MulticastSocket(mcPort);
			mcSock.joinGroup(InetAddress.getByName(mcAddress));
		}
		catch (IOException e) { e.printStackTrace(); }
	}
		
}
