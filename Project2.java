import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.Scanner;


public class Project2
{
	public static int clock = 0;
	 static char[][] memory = new char[8][32];
	static  List<Process> allProcess = new ArrayList<Process>();
	 static Set<Map<Integer, Integer>> pT = new TreeSet<Map<Integer, Integer>>();
	public static boolean next_fit = true;
	public static boolean best_fit = false;
	public static boolean worst_fit = false;
	public static boolean non_contig = false;
	public static int mostRecentX = 0;
	public static int mostRecentY = 0;
	public static int numRemovals = 0;
	public static int totalRemovals = 0;
	private static void printMemory()
	{
		System.out.println("================================");
		
		for(int i=0; i<8; i++)
		{
			for(int j=0; j<32;j++)
			{
				System.out.print(memory[i][j]);
			}
			System.out.println("");
		}
		System.out.println("================================");
	}
	
	private static void printPageTable()
	{
		System.out.println("PAGE TABLE [page,frame]:");
		Collections.sort(allProcess, new Comparator<Process>(){
            public int compare(Process p1, Process p2) {
                return p1.name - p2.name;
            }
        });
		for(int i=0; i<allProcess.size();i++)
		{
			Process x = allProcess.get(i);
			if(!(x.pT.isEmpty()))
			{
				int numonLine = 0;
				boolean first = true;
				System.out.print(x.name + ": ");
				Set<Integer> allNums = x.pT.keySet();
				for(Integer temp: allNums)
				{
					if(first)
					{
						System.out.print("[" + temp + "," +  x.pT.get(temp) + "]");
						first = false;
					}
					else
					{
						System.out.print(" [" + temp + "," +  x.pT.get(temp) + "]");
					}
					numonLine++;
					if(numonLine == 10)
					{
						System.out.println("");
						numonLine = 0;
						first = true;
					}
				}
				System.out.println("");
			}
		}
		
	}
	
	private static void reset()
	{
		numRemovals = 0;
		next_fit = false;
		clock = 0;
		for(int i=0; i<8; i++)
		{
			for(int j=0; j<32; j++)
			{
				memory[i][j] = '.';
			}
		}
		allProcess.clear();
	}
	
	private static boolean allDots(int x, int y)
	{
		for(int i=x; i<8; i++)
		{
			for(int j=y; j<32; j++)
			{
				if(memory[i][j] != '.')
				{
					return false;
				}
			}
			y=0;
		}
		return true;
	}
	private static int getFirstDot()
	{
		int first = 0;
		boolean done = false;
		for(int i=0; i<8;i++)
		{
			for(int j=0; j<32; j++)
			{
				if(memory[i][j] == '.')
				{
					first = 32*i;
					first += j;
					done = true;
					break;
				}
			}
			if(done)
			{
				break;
			}
		}
		return first;
	}
	
	private static void setAllZerosAfter()
	{
		int temp = mostRecentY+1;
		for(int i=mostRecentX; i<8; i++)
		{
			for(int j=temp; j<32; j++)
			{
				memory[i][j] = '.';
			}
			temp = 0;
		}
	}

	
	public static ArrayList<Character> getallActive()
	{
		ArrayList<Character> all = new ArrayList<Character>();
		int start= getFirstDot();
		int x = start/32;
		int y= start%32;
		for(int i=x; i<8;i++)
		{
			for(int j=y;j<32; j++)
			{
				if(memory[i][j] != '.')
				{
					if(!all.contains(memory[i][j]))
					{
						all.add(memory[i][j]);
					}
					
				}
			}
			y=0;
		}
		return all;
	}
	
	
	
	private static int getFragmentSize(int r, int c)
	{
		int size = 0;
		boolean done = false;
		for(int i=r; i<8;i++)
		{
			for(int j=c; j<32; j++)
			{
				if(memory[i][j] == '.')
				{
					size++;
				}
				else
				{
					done = true;
					break;
				}
			}
			c=0;
			if(done){ break;}
		}
		return size;
			
	}
	private static int getTotalEmptySlots()
	{
		int sum = 0;
		for(int i=0; i<8; i++)
		{
			for(int j = 0; j<32; j++)
			{
				if(memory[i][j] == '.')
				{
					sum++;
				}
			}
		}
		return sum;
	}
	public static boolean memoryisEmpty()
	{
		for(int i=0; i<8; i++)
		{
			for(int j=0; j<32; j++)
			{
				if(memory[i][j] != '.')
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private static int skipToNextBlock(int i, int j, int size)
	{
		int org = (i*32) + j;
		return org+size;
	}
	
	private static int getSizeClosestTo(int size)
	{
		int closestSize = 32*8;
		int value = -1;
		if(memoryisEmpty())
		{
			return 0;
		}
		for(int i=0; i<8; i++)
		{
			for(int j=0; j<32; j++)
			{
				if(memory[i][j] == '.')
				{
					int temp = getFragmentSize(i,j);	
					if(temp == size)
					{
						return (i*32) + j;
					}
					else if(temp>size)
					{
						if(temp<closestSize)
						{
							closestSize = temp; 
							value = (32*i) + j;							
						}
					}
					int loc = skipToNextBlock(i,j,temp);
					i = loc/32;
					j = loc%32;
					if(i==8)
					{
						break;
					}
				}
			}
		}
		return value;
	}
	
	
	private static int getSizeFarthestTo(int size)
	{
		int farthestSize = 0;
		int value = -1;
		if(memoryisEmpty())
		{
			return 0;
		}
		for(int i=0; i<8; i++)
		{
			for(int j=0; j<32; j++)
			{
				if(memory[i][j] == '.')
				{
					int temp = getFragmentSize(i,j);
					if(temp>=size)
					{
						if(temp>farthestSize)
						{
							farthestSize = temp; 
							value = (32*i) + j;
						}
					}
					
				}
			}
		}
		return value;
	}
	
	
	private static void updateTimes(int x)
	{
		for(int i=0; i<allProcess.size(); i++)
		{
			allProcess.get(i).defraOffset(x,clock);
		}
	}
	
	private static void defragmentation()
	{
		boolean done = false;
		for(int i=0; i<8; i++)
		{
			for(int j=0; j<32; j++)
			{
				while(memory[i][j] == '.')
				{
					int temp = j;
					//System.out.println("i=" + i + "  j=" + j);
					for(int k=i; k<8; k++)
					{
						for(int p=temp; p<32; p++)
						{
							if(p==31 && k==7)
							{
								//nothing
							}
							else if(p==31)
							{
								memory[k][p] = memory[k+1][0];
							}
							else
							{
								memory[k][p] = memory[k][(p+1)];
							}
						}
						temp = 0;
					}
					if(allDots(i,j))
					{
						done=true;
						break;
					}
				}
				if(done){break;}
			}
			if(done){break;}
		}
	}
		
	private static void defragPrint(int i)
	{
		System.out.println("time " + clock + "ms: Cannot place process " + allProcess.get(i).name + " -- starting defragmentation");
		int firstDot = getFirstDot();
		int numMoved = (32*8) - getTotalEmptySlots() -firstDot;
		ArrayList allActive;
		allActive = getallActive();
		defragmentation();
		clock+=numMoved;
		updateTimes(numMoved);
		
		int totalAbove = numMoved + firstDot;
		
		System.out.print("time " + clock + "ms: Defragmentation complete (moved " + numMoved + " frames: ");
		

		for(int k=0; k<allActive.size();k++)
		{
			if(k==allActive.size()-1)
			{
				System.out.print(allActive.get(k));
			}
			else
			{
				System.out.print(allActive.get(k) + ", ");
			}
			
		}
		System.out.println(")");
		printMemory();
		placeProcess(allProcess.get(i).name, allProcess.get(i).size, totalAbove/32, totalAbove%32);
		System.out.println("time " + clock +"ms: Placed process " + allProcess.get(i).name + ":");
		setAllZerosAfter();
		printMemory();
		allProcess.get(i).setRemovalTime(clock);
	}
		
	private static void placeProcess(char name, int length, int x, int y)
	{
		int totalPlaced = 0;
		boolean done = false;
		for(int i=x; i<8; i++)
		{
			for(int j=y; j<32; j++)
			{
				memory[i][j] = name;
				totalPlaced++;
				mostRecentY = j;
				if(totalPlaced == length)
				{
					done = true;
					break;
				}
				
			}
			y=0;
			mostRecentX = i;
			if(done){ break;}
		}
	}
	
	public static void check_exit_times()
	{
		for(int i=0; i<allProcess.size(); i++)
		{
			if(allProcess.get(i).exitMemoryTime == clock)
			{
				numRemovals++;
				
				allProcess.get(i).setArrivalTime();
				char name = allProcess.get(i).name;
				for(int j=0; j<8; j++)
				{
					for(int k=0; k<32; k++)
					{
						if(memory[j][k] == name)
						{
							memory[j][k] = '.';
						}
					}
				}
				System.out.println("time " + clock + "ms: Process " + allProcess.get(i).name + " removed:");
				printMemory();
				if(non_contig)
				{
					allProcess.get(i).pT.clear();
					printPageTable();
				}
			}
		}
		
	}
	
	public static void check_arrival_times()
	{
		boolean placed = false;
		Collections.sort(allProcess, new Comparator<Process>(){
            public int compare(Process p1, Process p2) {
                return p1.name - p2.name;
            }
        });
		for(int i=0; i<allProcess.size(); i++)
		{
			if(allProcess.get(i).nextArrivalTime == clock)
			{
				placed = false;
				 System.out.println("time " + clock + "ms: Process " + allProcess.get(i).name + " arrived (requires " +  allProcess.get(i).size + " frames)");
				if(allProcess.get(i).size > getTotalEmptySlots())
				{
					System.out.println("time " + clock + "ms: Cannot place process " + allProcess.get(i).name + " -- skipped!");
					 allProcess.get(i).popRemovalTime();
					 allProcess.get(i).setArrivalTime();
					 numRemovals++;

				}
				//NEXT FIT
				else if(next_fit)
				{
					int numColsChecked = 0;
					int yCounter = mostRecentY+1;
					//System.out.println("RX: " + mostRecentX + "  RY: " + mostRecentY);
					for(int j=mostRecentX; numColsChecked<8; j++)
					{
						for(int k=yCounter; k<32; k++)
						{
							if(memory[j][k] == '.')
							{
								
								int fragSize = getFragmentSize(j,k);
								//System.out.println("J= " + j + " K= "+ k + " FS= " + fragSize);
								if(fragSize >= allProcess.get(i).size)
								{
									placeProcess(allProcess.get(i).name,allProcess.get(i).size, j,k);
									placed  = true;
									break;
								}
							}
						}
						yCounter = 0;
						numColsChecked++;
						if(j==7)
						{
							j=-1;
						}
						if(placed){ break;}
					}
					if(placed)
					{
						System.out.println("time " + clock +"ms: Placed process " + allProcess.get(i).name + ":");
						printMemory();
						allProcess.get(i).setRemovalTime(clock);
					}
					else
					{
						defragPrint(i);
					}
				}
				
				
				else if(best_fit)
				{
					int size = allProcess.get(i).size;
					int startPos = getSizeClosestTo(size);
					if(startPos != -1)
					{
						System.out.println("time " + clock +"ms: Placed process " + allProcess.get(i).name + ":");
						int x = startPos/32;
						int y = startPos%32;
						placeProcess(allProcess.get(i).name, size, x,y);
						printMemory();
						allProcess.get(i).setRemovalTime(clock);
					}
					else
					{
						defragPrint(i);
					}
				}
				
				else if(worst_fit)
				{
					int size = allProcess.get(i).size;
					int startPos = getSizeFarthestTo(size);
					if(startPos != -1)
					{
						System.out.println("time " + clock +"ms: Placed process " + allProcess.get(i).name + ":");
						int x = startPos/32;
						int y = startPos%32;
	
						placeProcess(allProcess.get(i).name, size, x,y);
						printMemory();
						allProcess.get(i).setRemovalTime(clock);
					}
					else
					{
						defragPrint(i);
					}
				}
					
				else if(non_contig)
				{
					int size = allProcess.get(i).size;
					int numRecorded = 0;
					boolean done= false;
					for(int j=0; j<8;j++)
					{
						for(int k=0; k<32;k++)
						{
							if(memory[j][k] == '.')
							{
								memory[j][k] = allProcess.get(i).name;
								allProcess.get(i).pT.put(numRecorded, (j*32) + k);
								numRecorded++;
								if(numRecorded == size)
								{
									done = true;
									break;
								}
							}
						}
						if(done){break;}
					}
				System.out.println("time " + clock +"ms: Placed process " + allProcess.get(i).name + ":");
				printMemory();
				allProcess.get(i).setRemovalTime(clock);
				printPageTable();
			}
			}
		}
	}
	
	 public static void main(String [] args) throws FileNotFoundException
	{
		 String fileName = args[0];
		 File file = new File(fileName);
		if(file.exists())
		 {
			 Scanner inFile = new Scanner(file);
				while(inFile.hasNext())
				{
					String line = inFile.nextLine();
					if(line.trim().equals("")|| line.charAt(0) == '#' || line.charAt(0) == ' ')
					{
						//DO NOTHING, NOT A VALID LINE INPUT				
					}
					else
					{
						String delims = "[ ]";
						String[] vals = line.split(delims);
						Queue<Integer> arrivalTimes = new LinkedList<Integer>();
						Queue<Integer> memLength = new LinkedList<Integer>();
						int count = 2;
						while(count<vals.length)
						{
							String pair = vals[count];
							totalRemovals++;
							String secondDelim ="[/]";
							String [] set = pair.split(secondDelim);
							arrivalTimes.add(Integer.parseInt(set[0]));
							memLength.add(Integer.parseInt(set[1]));
							count++;
						}
						Process temp = new Process(vals[0].charAt(0), Integer.parseInt(vals[1]), arrivalTimes, memLength);
						allProcess.add(temp);
					}
				}
				inFile.close();}
		 System.out.println("time 0ms: Simulator started (Contiguous -- Next-Fit)");		
		 int row = 0;
		 int col = 0;
		 int count = 0;
		 for(int i=0; i<8; i++)
		 {
			 for(int j=0;j<32; j++)
			 {
				 memory[i][j] = '.';
			 }
		 }
		 //get all time 0 arrivals
		 while(allProcess.get(count).arrivalTimes.peek() == 0)
		 {
			 System.out.println("time 0ms: Process " + allProcess.get(count).name + " arrived (requires " +  allProcess.get(count).size + " frames)");
			 if(allProcess.get(count).size > getTotalEmptySlots())
			 {
				 System.out.println("time " + clock + "ms: Cannot place process " + allProcess.get(count).name + " -- skipped!");
				
				 allProcess.get(count).popArrivalTimeZero();
				 allProcess.get(count).popRemovalTime();
				 allProcess.get(count).setArrivalTime();
				 numRemovals++;
				 count++;
			 }
			 else
			 {
				 for(int i=0; i<allProcess.get(count).size; i++)
				 {
					 memory[row][col] = allProcess.get(count).name;
					 col++;
					 if(col == 32)
					 {
						 col = 0;
						 row++;
					 }
				 }
				 System.out.println("time 0ms: Placed process " + allProcess.get(count).name + ":");
				 allProcess.get(count).setRemovalTime(0);
				 allProcess.get(count).popArrivalTimeZero();
				 printMemory();
				 count++;

				 mostRecentX = row;
				 mostRecentY = col-1;
				 if(count == allProcess.size())
				 {
					 break;
				 }
			 }
			 
		 }
		 for(int i=count; i<allProcess.size();i++)
		 {
			 allProcess.get(i).setArrivalTime();
		 }
		 //next fit
		 while(true)
		 {
			 clock++;
			 check_exit_times();
			 check_arrival_times();
			// System.out.println(numRemovals + " ----" + totalRemovals);
			 if(numRemovals == totalRemovals)
			 {
				 break;
			 }
		 }
		 System.out.println("time " + clock + "ms: Simulator ended (Contiguous -- Next-Fit)");
		 System.out.println("");
		 reset();
		 
		 
	 //int count, row, col;
		 file = new File(fileName);
		 if(file.exists())
		 {
			 Scanner inFile = new Scanner(file);
				while(inFile.hasNext())
				{
					String line = inFile.nextLine();
					if(line.trim().equals("")|| line.charAt(0) == '#' || line.charAt(0) == ' ')
					{
						//DO NOTHING, NOT A VALID LINE INPUT				
					}
					else
					{
						String delims = "[ ]";
						String[] vals = line.split(delims);
						Queue<Integer> arrivalTimes = new LinkedList<Integer>();
						Queue<Integer> memLength = new LinkedList<Integer>();
						count = 2;
						while(count<vals.length)
						{
							String pair = vals[count];
							String secondDelim ="[/]";
							String [] set = pair.split(secondDelim);
							arrivalTimes.add(Integer.parseInt(set[0]));
							memLength.add(Integer.parseInt(set[1]));
							count++;
						}
						Process temp = new Process(vals[0].charAt(0), Integer.parseInt(vals[1]), arrivalTimes, memLength);
						allProcess.add(temp);
					}
				}
		inFile.close();}
		 System.out.println("time 0ms: Simulator started (Contiguous -- Best-Fit)");		
		 //get all time 0 arrivals
		 count = 0;
		 row = 0;
		 col = 0;
		 while(allProcess.get(count).arrivalTimes.peek() == 0)
		 {
			 System.out.println("time 0ms: Process " + allProcess.get(count).name + " arrived (requires " +  allProcess.get(count).size + " frames)");
		 	if(allProcess.get(count).size > getTotalEmptySlots())
			 {
				 System.out.println("time " + clock + "ms: Cannot place process " + allProcess.get(count).name + " -- skipped!");
				
				 allProcess.get(count).popArrivalTimeZero();
				 allProcess.get(count).popRemovalTime();
				 allProcess.get(count).setArrivalTime();
				 numRemovals++;
				 count++;
			 }
			 else
			 {
			 for(int i=0; i<allProcess.get(count).size; i++)
			 {
				 memory[row][col] = allProcess.get(count).name;
				 col++;
				 if(col == 32)
				 {
					 col = 0;
					 row++;
				 }
			 }
			 System.out.println("time 0ms: Placed process " + allProcess.get(count).name + ":");
			 allProcess.get(count).setRemovalTime(0);
			 allProcess.get(count).popArrivalTimeZero();
			 printMemory();
			 count++;
			 if(count == allProcess.size())
			 {
				 break;
			 }
			 }
		 }
		 for(int i=count; i<allProcess.size();i++)
		 {
			 allProcess.get(i).setArrivalTime();
		 }
		 best_fit = true;
		 
		 while(true)
		 {
			 clock++;
			 check_exit_times();
			 check_arrival_times();
			 if(numRemovals == totalRemovals)
			 {
				 break;
			 }
		 }
		 System.out.println("time " + clock + "ms: Simulator ended (Contiguous -- Best-Fit)");
		 System.out.println("");
		 
		 
 reset();
		 
		 
		 
	//	int count, row, col;
		 
		 file = new File(fileName);
		 if(file.exists())
		 {
			 Scanner inFile = new Scanner(file);
				while(inFile.hasNext())
				{
					String line = inFile.nextLine();
					if(line.trim().equals("")|| line.charAt(0) == '#' || line.charAt(0) == ' ')
					{
						//DO NOTHING, NOT A VALID LINE INPUT				
					}
					else
					{
						String delims = "[ ]";
						String[] vals = line.split(delims);
						Queue<Integer> arrivalTimes = new LinkedList<Integer>();
						Queue<Integer> memLength = new LinkedList<Integer>();
						count = 2;
						while(count<vals.length)
						{
							String pair = vals[count];
							String secondDelim ="[/]";
							String [] set = pair.split(secondDelim);
							arrivalTimes.add(Integer.parseInt(set[0]));
							memLength.add(Integer.parseInt(set[1]));
							count++;
						}
						Process temp = new Process(vals[0].charAt(0), Integer.parseInt(vals[1]), arrivalTimes, memLength);
						allProcess.add(temp);
					}
				}
		inFile.close();}
		 System.out.println("time 0ms: Simulator started (Contiguous -- Worst-Fit)");		
		 //get all time 0 arrivals
		 count = 0;
		 row = 0;
		 col = 0;
		 while(allProcess.get(count).arrivalTimes.peek() == 0)
		 {
		 	System.out.println("time 0ms: Process " + allProcess.get(count).name + " arrived (requires " +  allProcess.get(count).size + " frames)");

		 	if(allProcess.get(count).size > getTotalEmptySlots())
			 {
				 System.out.println("time " + clock + "ms: Cannot place process " + allProcess.get(count).name + " -- skipped!");
				
				 allProcess.get(count).popArrivalTimeZero();
				 allProcess.get(count).popRemovalTime();
				 allProcess.get(count).setArrivalTime();
				 numRemovals++;
				 count++;
			 }
			 else
			 {
			 for(int i=0; i<allProcess.get(count).size; i++)
			 {
				 memory[row][col] = allProcess.get(count).name;
				 col++;
				 if(col == 32)
				 {
					 col = 0;
					 row++;
				 }
			 }
			 System.out.println("time 0ms: Placed process " + allProcess.get(count).name + ":");
			 allProcess.get(count).setRemovalTime(0);
			 allProcess.get(count).popArrivalTimeZero();
			 printMemory();
			 count++;
			 if(count == allProcess.size())
			 {
				 break;
			 }
			 }
		 }
		 for(int i=count; i<allProcess.size();i++)
		 {
			 allProcess.get(i).setArrivalTime();
		 }
		 best_fit = false;
		 worst_fit = true;
		 while(true)
		 {
			 clock++;
			 check_exit_times();
			 check_arrival_times();
			 if(numRemovals == totalRemovals)
			 {
				 break;
			 }
		 }
		 System.out.println("time " + clock + "ms: Simulator ended (Contiguous -- Worst-Fit)");
		 System.out.println("");
		 
		 reset();
		/// int count,row,col;
		 
		 System.out.println("time "+ clock +"ms: Simulator started (Non-contiguous)");
		 file = new File(fileName);
		 if(file.exists())
		 {
			 Scanner inFile = new Scanner(file);
				while(inFile.hasNext())
				{
					String line = inFile.nextLine();
					if(line.trim().equals("")|| line.charAt(0) == '#' || line.charAt(0) == ' ')
					{
						//DO NOTHING, NOT A VALID LINE INPUT				
					}
					else
					{
						String delims = "[ ]";
						String[] vals = line.split(delims);
						Queue<Integer> arrivalTimes = new LinkedList<Integer>();
						Queue<Integer> memLength = new LinkedList<Integer>();
						count =2;
						while(count<vals.length)
						{
							String pair = vals[count];
							String secondDelim ="[/]";
							String [] set = pair.split(secondDelim);
							arrivalTimes.add(Integer.parseInt(set[0]));
							memLength.add(Integer.parseInt(set[1]));
							count++;
						}
						Process temp = new Process(vals[0].charAt(0), Integer.parseInt(vals[1]), arrivalTimes, memLength);
						allProcess.add(temp);
					}
				}
		inFile.close();}
		 //get all time 0 arrivals
		 count = 0;
		 row = 0;
		 col = 0;
		 while(allProcess.get(count).arrivalTimes.peek() == 0)
		 {
		 	System.out.println("time 0ms: Process " + allProcess.get(count).name + " arrived (requires " +  allProcess.get(count).size + " frames)");

		 	if(allProcess.get(count).size > getTotalEmptySlots())
			 {
				 System.out.println("time " + clock + "ms: Cannot place process " + allProcess.get(count).name + " -- skipped!");
				
				 allProcess.get(count).popArrivalTimeZero();
				 allProcess.get(count).popRemovalTime();
				 allProcess.get(count).setArrivalTime();
				 numRemovals++;
				 count++;
			 }
			 else
			 {
	
			 for(int i=0; i<allProcess.get(count).size; i++)
			 {
				 memory[row][col] = allProcess.get(count).name;
				 allProcess.get(count).pT.put(i, (row*32)+ col);
			
				 col++;
				 if(col == 32)
				 {
					 col = 0;
					 row++;
				 }
				 
			 }
			 System.out.println("time 0ms: Placed process " + allProcess.get(count).name + ":");
			
			 allProcess.get(count).setRemovalTime(0);
			 allProcess.get(count).popArrivalTimeZero();
			 printMemory();
			 printPageTable();
			 count++;
			 if(count == allProcess.size())
			 {
				 break;
			 }
			 }
		 }
		 for(int i=count; i<allProcess.size();i++)
		 {
			 allProcess.get(i).setArrivalTime();
		 }
		 best_fit = false;
		 worst_fit = false;
		 non_contig = true;
		 while(true)
		 {
			 clock++;
			 check_exit_times();
			 check_arrival_times();
			 if(numRemovals == totalRemovals)
			 {
				 break;
			 }
		 }
		 System.out.println("time " + clock + "ms: Simulator ended (Non-contiguous)");

	}
}
