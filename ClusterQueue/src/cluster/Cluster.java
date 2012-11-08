package cluster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import bigDecimalPrimeThread.BigDecimalPrimer;

/**
 * This class is used to form a cluster between multiple computers
 * with no central server node with each client connected to each other.
 * @author Chris Hansohn
 * @version 1.6.1
 *
 */
public class Cluster
{
	/* This is the list of threads that are all the connections
	 * the cluster will have. */
	private List<ConnectionMaintaining> cConnections;
	private List<ConnectionMaintaining> sConnections;
	
	private List<Thread> runningThreads;//Added 5/12/2011
	private List<Thread> completeThreads;//Added 5/12/2011
	private int maxRunningThreads;//Added 5/12/2011
	private ConnectionMaintaining master;//Added 5/12/2011
	private InputStreamReader text;//Added 5/12/2011
	private BufferedReader reader;//Added 5/12/2011
	
	/* This boolean is used to kill the server listening thread
	 * when it is final time for the program to shutdown. */
	private boolean serverKeepListening;
	/* There is only of thread that is listening for new clients
	 * and forming new connections if its a unique connection. */
	private Thread serverThread;
	private boolean imMaster;//Added 5/12/2011
	private UserInput user;//Added 5/12/2011
	private BigDecimal number;
	
	/**
	 * This constructor initializes the lists of threads which are used to
	 * maintain connections. Then it sets the server
	 * running boolean variable and begins the server thread.
	 * @param port this is the port number the server will be listening on.
	 * if the port number is less then 1000 (or the OS reserved ports) then
	 * it uses the empty constructor.
	 */
	public Cluster(int port)
	{		
		cConnections = new ArrayList<ConnectionMaintaining>();
		sConnections = new ArrayList<ConnectionMaintaining>();
		
		number = new BigDecimal("3");
		
		runningThreads = new ArrayList<Thread>();//Added 5/12/2011
		completeThreads = new ArrayList<Thread>();//Added 5/12/2011
		maxRunningThreads = 2;//Added 5/12/2011
		text = new InputStreamReader(System.in);//Added 5/12/2011
		reader = new BufferedReader(text);//Added 5/12/2011
		
		serverKeepListening = true;
		serverThread =(port < 1000) ?  new Thread(new ServerListen()) : new Thread( new ServerListen(port));
		serverThread.start();
		imMaster = false;;
		
		for(int i=0; i<cConnections.size(); i++)
		{
			System.out.println(cConnections.get(i));
			System.out.println(sConnections.get(i));
		}
		user = new UserInput();
		user.start();
	}
	
	/**
	 * This method is used to form a new server socket connection. the connection
	 * is stored in the list of connections.
	 * @param socket The socket of the server side connection
	 * @param address the ip address of the connected client used for makeing
	 * sure each connection stored is unique.
	 */
	private synchronized void addSConnection(Socket socket, String address)
	{
		ConnectionMaintaining newConnection = new ConnectionMaintaining(address, socket, this, imMaster);
		newConnection.start();
		sConnections.add(newConnection);
		System.out.println("sConnection Added "+address+'\n'+"sConnections: "+sConnections.size());
	}
	
	/**
	 * This method is used to from a new connection from client side of the cluster.
	 * the client connections and addresses are stored in the new thread in the list.
	 * @param socket The socket of the client side connection
	 * @param address The ip address of the connected server used
	 * to make sure the client does not connect to the same server twice.
	 */
	private synchronized void addCConnection(Socket socket, String address)
	{
		ConnectionMaintaining newConnection = new ConnectionMaintaining(address, socket, this, imMaster);
		newConnection.start();
		cConnections.add(newConnection);
		System.out.println('\n'+"cConnection Added: "+address+'\n'+"cConnections: "+cConnections.size());
	}
	
	/**
	 * This method is used by the server thread to see if it
	 * should keep trying to obtain connections or if it should
	 * stop looping.
	 * @return This boolean is what tells the server thread to
	 * loop or stop.
	 */
	private boolean keepListening() { return serverKeepListening; }
	
	/**
	 * This method allows the calling class/method to stop the
	 * server from accepting connections and to end the thread.
	 */
	public void stop()
	{
		serverKeepListening = false;
		for(ConnectionMaintaining a : cConnections)
		{
			a.pDisconnect();
		}
		for(ConnectionMaintaining a : sConnections)
		{
			a.pDisconnect();
		}
		user.stopNow();
	}
	
	public synchronized void lost(String connectionAddress) 
	{ 
		int sNumber = -1;
		int cNumber = -1;
		for(int i = 0; i < sConnections.size()
		|| i < cConnections.size(); i++)
		{
			String cAddress = cConnections.get(i).toString();
			String sAddress = sConnections.get(i).toString();
			if(sAddress != null)
				sAddress = sAddress.substring(1);
			else
			{
				sConnections.get(i).pDisconnect();
				break;
			}
			if(i < sConnections.size() && sAddress.equalsIgnoreCase(connectionAddress))
			{
				if(sConnections.get(i) == master)//Added 5/12/2011
					master = null;//Added 5/12/2011
				sConnections.get(i).pDisconnect();
				sNumber = i;
			}
			if(i < cConnections.size() && cAddress.equalsIgnoreCase(connectionAddress))
			{
				cConnections.get(i).pDisconnect();
				cNumber = i;
			}
		}
		System.out.println("Lost Connection: "+cNumber);
		System.out.println("cConnections size: "+this.cConnections.size());
		
		try
		{
			this.cConnections.remove(cNumber); 
			this.sConnections.remove(sNumber);
			
		}
		catch(IndexOutOfBoundsException iobe)
		{
			System.out.println("IOBE Caught");
		}
		
	}//end lost
	
	//Added 5/12/2011
	public void runThread(Object input)
	{
		try
		{
			Thread runner = (Thread) input;
			runner.start();
			runningThreads.add(runner);
		}
		catch(Exception e){ System.out.println("this is not a thread object"); } 
	}
	
	public void cleanDoneThreads()
	{
		Thread.yield();
		for(int i = 0; i < runningThreads.size(); i++)
		{
			if(((BigDecimalPrimer)runningThreads.get(i)).isRunning())
			{
				completeThreads.add(runningThreads.get(i));
				runningThreads.remove(i);
				i--;
			}
		}
		if(runningThreads.size() < maxRunningThreads && master != null)
		{
			boolean masterThere = master.sendReady(maxRunningThreads - runningThreads.size());
			if(!masterThere)
				master = null;
		}
	}
	
	public int numberOfRunningThreads()
	{
		cleanDoneThreads();
		return runningThreads.size();
	}
	
	public void setMaster(ConnectionMaintaining newMaster)
	{
		String masterIP = newMaster.toString();
		if(masterIP.contains("/"))
			masterIP = masterIP.substring(1);
		for(int i = 0; i < sConnections.size(); i++)
		{
			String findingMaster = sConnections.get(i).toString();
			if(findingMaster.contains("/"))
				findingMaster = findingMaster.substring(1);
			if(findingMaster.equalsIgnoreCase(masterIP))
				master = sConnections.get(i);
		}
		if(master == null)
			System.out.println("Master was not found");
	}
	
	public boolean isMaster() { return this.imMaster; }

	public synchronized Thread getNextPrimeData()
	{
		if(number.compareTo(new BigDecimal("10000")) == -1)
		{
			BigDecimal[] threadData = new BigDecimal[100];
			for(int i = 0; i < 100; i++)
			{
				threadData[i] = getNextNumber();
			}
			return new BigDecimalPrimer(threadData);
		}
		else
		{
			BigDecimal threadData = getNextNumber();
			return new BigDecimalPrimer(threadData);
		}
	}
	
	private synchronized BigDecimal getNextNumber()
	{
		BigDecimal stored = new BigDecimal(number.toPlainString());
		number = number.add(new BigDecimal("2"));
		return stored;
	}
	
	public void newClient(String address, int port){ new ClientConnect(address, port); }
	
	/**
	 * This is the server class which will keep listening
	 * for clients until the keepListening flag in the parent
	 * class is changed. Once a new connection is obtained it
	 * is stored in the parent class.
	 * @author Chris Hansohn
	 *
	 */
	private class ServerListen implements Runnable
	{
		//The server socket the thread uses to listen on.
		private ServerSocket serverSocket;
		
		/**
		 * This constructor is called when no parameter
		 * is passed. as a result the client listens on
		 * the default port of 5000. the timeout time is
		 * also set here.
		 */
		public ServerListen()
		{
			try
			{
				serverSocket = new ServerSocket(5000);
				serverSocket.setSoTimeout(1000);
			}
			catch (IOException e) { e.printStackTrace(); }
		}
		
		/**
		 * This thread is called when a parameter is passed.
		 * The parameter is the port the server will listen on.
		 * it also sets the default timeout time.
		 * @param port The port the server will be listening
		 * on waiting for connections.
		 */
		public ServerListen(int port)
		{
			try
			{
				serverSocket = new ServerSocket(port);
				serverSocket.setSoTimeout(1000);
			}
			catch (IOException e) { e.printStackTrace(); }
		}
		
		/**
		 * The run method that will wait for a TCPconnection.
		 * when one is obtained it will check to see if it is
		 * already connected. if not it will tell the parent class
		 * to store it.
		 */
		public void run()
		{
			String address = "";
			Socket connection;
			while(keepListening())
			{
				try
				{
					boolean addressFound = false;
					connection = serverSocket.accept();
					address = connection.getInetAddress().toString();
					for(ConnectionMaintaining a : sConnections)
					{
						if(a.toString() == null)
						{
							a.pDisconnect();
							break;
						}
						if(a.toString().equalsIgnoreCase(address))
							addressFound = true;
					}
					if(!addressFound)
						addSConnection(connection, address);
				}
				catch (SocketTimeoutException ste)
				{
					/* This catches the timeout and ignores it.
					 * This was done so that the method can keep
					 * looping but does not need a final connection
					 * to stop.*/
				}
				catch (IOException e) { e.printStackTrace(); }
			}//while
		}//run
	}//class ServerListen

	/**
	 * This class is used to make new client side connections
	 * to serves.
	 */
	private class ClientConnect
	{
		/**
		 * The constructor is all the logic for this class.
		 * it checks to see if the connection already exists.
		 * if it doesn't then it will proceed at making it.
		 * @param address The address of the server
		 * @param port the port the server is listening on.
		 */
		public ClientConnect(String address, int port)
		{
			boolean alreadyConnected = false;
			int count = 0;
			//System.out.println(address);
			for(ConnectionMaintaining a : cConnections)
			{
				try
				{
					if(a.toString().equalsIgnoreCase(address))
						alreadyConnected = true;
					count++;
				}
				catch(Exception e) { cConnections.remove(count); }
			}
			if(!alreadyConnected)
			{
				try
				{
					Socket socket = new Socket(InetAddress.getByName(address), port);
					addCConnection(socket, address);
				}
				catch (UnknownHostException e) { e.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
			}//if
		}//ClientConnect
	}//class ClientConnect	

	private class UserInput extends Thread
	{
		boolean done;
		public UserInput()
		{
			done = false;
		}
		
		public void run()
		{
			while(!done)
			{
				try
				{
					System.out.println("get user input");
					String input = reader.readLine();
					if(input.equalsIgnoreCase("is master"))
					{
						imMaster = true;
						for(int i = 0; i < sConnections.size(); i++)
						{
							sConnections.get(i).sendImMaster();
						}
					}
				}
				catch (IOException e) { e.printStackTrace(); }
			}
		}
		
		public synchronized void stopNow()
		{
			done = true;
			try
			{
				reader.close();
				reader = null;
			}
			catch (IOException e) { e.printStackTrace(); }
		}
	}
//Added 5/12/2011
}//class Cluster
