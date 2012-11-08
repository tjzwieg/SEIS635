package cluster;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * This class maintains a connection passed to it. each
 * connection passed is watched for a valid transmission
 * and this class will do what is needed from that connection.
 * @author Chris Hansohn
 * @version 1.6.1
 *
 */
public class ConnectionMaintaining extends Thread
{
	private String connectedAddress;
	private Socket connection;
	private boolean stayConnected;
	private boolean done;
	private ObjectInputStream in;//5/12/2011 Changed from BufferedReader to ObjectInputStream
	private ObjectOutputStream out;//Added 5/12/2011
	private Cluster cluster;
	
	
	/**
	 * The constructor is used to store the address of the connection,
	 * and the socket containing the connection.
	 * @param address The address of the connected machine
	 * @param connection the socket that is connected to the other machine.
	 */
	public ConnectionMaintaining(String address, Socket connection, Cluster cluster, boolean isMaster)
	{
		this.cluster = cluster;
		connectedAddress = address;
		this.connection = connection;
		stayConnected = true;
		done = false;
		//try { this.connection.setSoTimeout(10); }
		//catch (SocketException e) { e.printStackTrace(); }
		if(isMaster)
			sendImMaster();
	}
	
	/**
	 * loops until it is told to stop all connections.
	 */
	public void run()
	{
		Object input;//5/12/2011 changed from String to Object
		try
		{
			out = new ObjectOutputStream ( this.connection.getOutputStream());//Added 5/12/2011
			//out.flush();
			in = new ObjectInputStream( this.connection.getInputStream() );
		}
		catch (IOException e1) { e1.printStackTrace(); }//5/12/2011 Changed from BufferedReader to ObjectInputStream
		while(stayConnected)
		{
			if(connection.isClosed())
				disconnect();
			try
			{
					input = in.readObject();//5/12/2011 changed for Object Reading.
					if(input == null)
						disconnect();
//Added 5/12/2011
					else
					{
						if(input.toString().startsWith("<COMMAND>"))
							executeCommand(input.toString());
						else
							cluster.runThread(input);
					}
//Added 5/12/2011
			}
			catch(SocketException se){disconnect();}//ADDED BY TED 5-11-2011
			catch (SocketTimeoutException stoe) { }
			catch (IOException e)
			{
				e.printStackTrace();
				disconnect();
			}
			catch (ClassNotFoundException e) { e.printStackTrace(); }//5/12/2011 changed for Object Reading.
			catch (NullPointerException npe)
			{
				disconnect();
			}
		}
		if(stayConnected==false)
		{
		System.out.println(connectedAddress + " has disconnected");
		}
		try
		{
			connection.close();
			connectedAddress = null;
			connection = null;
			done = true;
		}
		catch (IOException e) { e.printStackTrace(); }
	}
	

	private void executeCommand(String string)
	{
		if(string.equals("<COMMAND> I MASTER"))
			cluster.setMaster(this);
		else if(string.startsWith(("<COMMAND> READY"))
				&& cluster.isMaster())
		{
			String amount = string.substring(15);
			amount = amount.trim();
			int intAmount = Integer.parseInt(amount);
			for(int i = 0; i < intAmount; i++)
			{
				Object data = cluster.getNextPrimeData();
				sendData(data);
			}
		}
	}
	//Added 5/12/2011
	/**
	 * this will just return the address of the connection
	 */
	public String toString() { return connectedAddress; }
	
	/**
	 * when called this class will tell the run loop to stop.
	 */
	private void disconnect()
	{
		stayConnected = false;
		cluster.lost(connectedAddress);
	}
	
	public synchronized void pDisconnect()
	{
		stayConnected = false;
	}
	
	/**
	 * this is used by the calling class to tell if the
	 * thread has stopped running.
	 * @return the return value is whether or not the thread has stopped running.
	 */
	public boolean isDone() { return done; }

	public void sendData(Object data)
	{
		try
		{
			out.writeObject(data);
			out.flush();
		}
		catch(IOException e) { disconnect(); }
	}
	
	public boolean sendReady(int i)
	{
		boolean isThere = true;
		String send = "<COMMAND> READY " + i;
		try
		{
			out.writeObject(send);
			out.flush();
		}
		catch (IOException e) { isThere = false; }
		return isThere;
	}

	public void sendImMaster()
	{
		String send = "<COMMAND> I MASTER";
		try
		{
			out.writeObject(send);
			out.flush();
		}
		catch (IOException e) { e.printStackTrace(); }
		catch (NullPointerException npe){ disconnect(); }
	}
//Added 5/12/2011
}
