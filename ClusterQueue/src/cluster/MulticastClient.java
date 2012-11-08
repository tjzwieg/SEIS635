package cluster;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastClient
{
	DatagramPacket packet;
	MulticastSocket mcClient;
	
	public MulticastClient(String mcAddress, int mcPort, String serverPort) throws IOException
	{
		serverPort = "PORT " + serverPort;
		byte[] data = serverPort.getBytes();
		packet = new DatagramPacket(data, data.length,
				InetAddress.getByName(mcAddress), mcPort);
		mcClient = new MulticastSocket();
	}
	
	public void sendPacket()
	{
		try{ mcClient.send(packet); }
		catch (IOException e) { e.printStackTrace(); }
	}
}
