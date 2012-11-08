package bigDecimalPrimeThread;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import primeCalculator.BigDecimalPrime;

public class BigDecimalPrimer extends Thread implements Serializable
{
	private BigDecimal[] toCheck;
	private int whatsChecked;
	private List<BigDecimal> foundPrimes;
	private BigDecimalPrime findPrime;
	private double status;
	private boolean isDone;
	
	public BigDecimalPrimer(BigDecimal[] check)
	{
		status = 0;
		foundPrimes = new ArrayList<BigDecimal>();
		this.toCheck = check;
		whatsChecked = 0;
		findPrime = new BigDecimalPrime();
	}
	
	public BigDecimalPrimer(BigDecimal check)
	{
		status = 0;
		foundPrimes = new ArrayList<BigDecimal>();
		this.toCheck = new BigDecimal[1];
		this.toCheck[0] = check;
		whatsChecked = 0;
		findPrime = new BigDecimalPrime();
	}
	
	public void run()
	{
		isDone = false;
		double incrementStatus = 100 / toCheck.length;
		for( ; whatsChecked < toCheck.length; whatsChecked++)
		{
			if(findPrime.isPrime(toCheck[whatsChecked]))
				foundPrimes.add(toCheck[whatsChecked]);
			status += incrementStatus;
		}
		isDone = true;
	}
	
	public boolean isRunning() { return isDone; }

	public synchronized void addStatus(double increment)
	{
		this.status+=increment;
	}
	
	public synchronized double getStatus()
	{
		return status;
	}
	
	public BigDecimal[] getPrimes()
	{
		BigDecimal[] toReturn = new BigDecimal[foundPrimes.size()];
		for(int i = 0; i < foundPrimes.size(); i++)
			toReturn[i] = foundPrimes.get(i);
		return toReturn;
	}

}
