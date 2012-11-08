package primeCalculator;
import java.io.Serializable;
import java.math.BigDecimal;


public class BigDecimalPrime implements Serializable
{		
	private BigDecimalSquareRoot sqre;
	
	public BigDecimalPrime()
	{
		sqre = new BigDecimalSquareRoot();
	}
	
	public boolean isPrime(BigDecimal value)
	{
		boolean isPrime = true;
		BigDecimal two = new BigDecimal("2");
		BigDecimal sqrRootOfValue = sqre.squareRoot(value).add(BigDecimal.ONE);
		for(BigDecimal i = new BigDecimal("3");
			i.compareTo(sqrRootOfValue) == -1 && isPrime;
			i = i.add(two))
		{
			BigDecimal temp = value.remainder(i);
			if(temp.compareTo(BigDecimal.ZERO) == 0)
			{
				isPrime = false;
			}
		}
		return isPrime;
	}
}
