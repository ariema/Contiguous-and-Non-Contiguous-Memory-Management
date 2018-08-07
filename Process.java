import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.TreeMap;



public class Process 
{
	public static String numRemainingArrivals;
	public char name;
	public int size;
	public int numArrivalsRemaining=0; 
	public int exitMemoryTime =0;
	public int nextArrivalTime = 0;
	public int numBursts = 0;
	public Queue<Integer> arrivalTimes;
	public Queue<Integer> memLengths;
	public int offset = 0;
	public Map<Integer, Integer> pT;
	
	public Process(char n, int s, Queue<Integer> at, Queue<Integer> ml)
	{
		name= n;
		size = s;
		arrivalTimes = at;
		memLengths = ml;	 
		numArrivalsRemaining = arrivalTimes.size();
		pT= new TreeMap<Integer, Integer>();
	}

	void setRemovalTime(int start)
	{
		exitMemoryTime = nextArrivalTime +  memLengths.remove();
	}
	
	void popArrivalTimeZero()
	{
		if(numArrivalsRemaining > 0)
		{
			arrivalTimes.remove();
			numArrivalsRemaining--;
		}
		
	}
	
	void popRemovalTime()
	{
		if(memLengths.size() > 0)
		{
			memLengths.remove();
		}
		
	}
	
	void setArrivalTime()
	{
		if(numArrivalsRemaining > 0)
		{
			nextArrivalTime = arrivalTimes.remove() + offset;
			numArrivalsRemaining--;
		}
	}
	
	void defraOffset(int x, int clock)
	{
		offset+=x;
		nextArrivalTime+=x;
		exitMemoryTime+=x;
		
	}
}
