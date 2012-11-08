package cluster;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class MultiCastListen extends Thread
{
	private boolean active;
	private MulticastSocket mcSock;
	private RunCluster runCluster;
	
	public MultiCastListen(MulticastSocket mcSock, RunCluster runCluster)
	{
		this.mcSock = mcSock;
		this.runCluster = runCluster;
		active = true;
	}
	
	/**
	 * This method will set the timeout of the socket
	 * receive call in order to force it to at least
	 * check to see if it should keep running once
	 * every second. While running it will continue
	 * receiving packets from other machine in the
	 * multicast.
	 */
	public void run()
	{
		String stringPort = "";
		try { mcSock.setSoTimeout(1000); }
		catch (SocketException e1) { e1.printStackTrace(); }
		while(active)//active is located in the parent class.
		{
			DatagramPacket data = new DatagramPacket(new byte[1024], 1024);
			try
			{
				mcSock.receive(data);
				stringPort = new String(data.getData(), 0, data.getData().length);
				if(stringPort.contains("PORT")) { runCluster.addNewServer(data); }
			}
			catch (SocketTimeoutException stoe) {  }
			catch (IOException e) { e.printStackTrace(); }
			
		}
		//try { mcSock.leaveGroup(InetAddress.getByName(mcAddress)); }//mcAddress is located in the parent class.
		//catch (UnknownHostException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
		mcSock.close();
	}
	
	public void done()
	{
		active = false;
	}
}
