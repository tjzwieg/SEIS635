import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cluster.Cluster;
import cluster.RunCluster;


public class Driver
{

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		Cluster cluster;
		RunCluster runCluster;
		Thread thread;
		System.out.println("Make Cluster Class");
		cluster = new Cluster(6000);
		System.out.println("Make RunCluster Class");
		runCluster = new RunCluster(cluster, 6000, "225.4.5.6", 5000);
		System.out.println("Make RunCluster Thread");
		thread = new Thread(runCluster);
		System.out.println("Start RunCluster Thread");
		thread.start();
		System.out.println("Get Input");
		
		
		while(!getInput().equals("exit"))
		{
			
			
		}//end while
		
		
		System.out.println("Ending");
		runCluster.done();
		System.out.println("Done");
	}
	
	private static String getInput() throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		return br.readLine();
	}
}
