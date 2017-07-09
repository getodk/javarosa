/**
 * 
 */
package org.javarosa.core.util;

import org.javarosa.core.model.instance.TreeReferenceLevel;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.xpath.expr.XPathStep;

/**
 * J2ME suffers from major disparities in the effective use of memory. This
 * class encompasses some hacks that sadly have to happen pertaining to high
 * memory throughput actions.
 * 
 * This was implemented in a hurry, and urgently needs a major refactor to be less...
 * hacky, static, and stupid.
 * 
 * @author ctsims
 *
 */
public class MemoryUtils {
	
	//These 3 are used to hold the profile of the heapspace, only relevant 
	//if you are doing deep memory profiling
	private static long[] memoryProfile;
	private static byte[][] memoryHolders;
	static int currentCount = 0;
	
	//Variables to keep track of the state of some of the internal 
	//interning options 
	//TODO: I think we can get rid of this, depending on fragmentation analysis
	static boolean oldterning;
	static boolean otrt;
	static boolean oldxpath;
	public static void stopTerning() {
		oldterning = ExtUtil.interning;
		otrt = TreeReferenceLevel.treeRefLevelInterningEnabled;
		oldxpath = XPathStep.XPathStepInterningEnabled;
		ExtUtil.interning = false;
		TreeReferenceLevel.treeRefLevelInterningEnabled = false;
		XPathStep.XPathStepInterningEnabled = false;
	}
	
	public static void revertTerning() {
		ExtUtil.interning = oldterning;
		TreeReferenceLevel.treeRefLevelInterningEnabled = otrt;
		XPathStep.XPathStepInterningEnabled = oldxpath;
	}
	
	
	//Used once at the beginning of an execution to enable memory profiling for
	//this run through. If you get an error when you try to profile memory,
	//due to lack of space, you can increase the profile size.
	private static final int MEMORY_PROFILE_SIZE = 5000;
	public static void enableMemoryProfile() {
		memoryProfile = new long[MEMORY_PROFILE_SIZE * 2];
		memoryHolders = new byte[MEMORY_PROFILE_SIZE][];
	}
	
	//#if javarosa.memory.print
	//# private static boolean MEMORY_PRINT_ENABLED = true;
	//#else
	private static boolean MEMORY_PRINT_ENABLED = false;
	//#endif
	
	/**
	 * Prints a memory test debug statement to stdout.
	 * Requires memory printing to be enabled, otherwise
	 * is a no-op
	 */
	public static void printMemoryTest() {
		printMemoryTest(null);
	}
	
	/**
	 * Prints a memory test debug statement to stdout 
	 * with a tag to reference
	 * Requires memory printing to be enabled, otherwise
	 * is a no-op 
	 * @param tag
	 */
	public static void printMemoryTest(String tag) {
		printMemoryTest(tag, -1);
	}
	
	/**
	 * Prints a memory test debug statement to stdout
	 * with a tag to reference. After printing the message
	 * the app waits for Pause milliseconds to allow profiling
	 * 
	 * Requires memory printing to be enabled, otherwise
	 * is a no-op
	 * @param tag
	 * @param pause
	 */
	public static void printMemoryTest(String tag, int pause) {
		if(!MEMORY_PRINT_ENABLED) { return; }
		System.gc();
		Runtime r = Runtime.getRuntime();
		long free = r.freeMemory();
		long total = r.totalMemory();
		
		if(tag != null) {
			System.out.println("=== Memory Evaluation: " + tag + " ===");
		}
		
		System.out.println("Total: " +total + "\nFree: " + free);
		
		int chunk = 100;
		int lastSuccess = 100;
		
		//Some environments provide better or more accurate numbers for available memory than 
		//others. Just in case, we go through and allocate the largest contiguious block of
		//memory that is available to see what the actual upper bound is for what we can
		//use
		
		//The resolution is the smallest chunk of memory that we care about. We won't bother
		//trying to add resolution more bytes if we couldn't add resolution * 2 bytes.
		int resolution = 10000;
		
		while(true) {
			System.gc();
			try {
				int newAmount  = lastSuccess + chunk;
				byte[] allocated = new byte[newAmount];
				lastSuccess = newAmount;
				//If we succeeded, keep trying a larger piece. 
				chunk = chunk * 10;
			} catch(OutOfMemoryError oom) {
				chunk = chunk / 2;
				if(chunk < resolution) { 
					break;
				}
			}
		}
			

		int availPercent = (int)Math.floor((lastSuccess * 1.0 / total) * 100);
		int fragmentation = (int)Math.floor((lastSuccess * 1.0 / free) * 100);
		System.out.println("Usable Memory: " +lastSuccess + "\n" + availPercent + "% of available memory");
		System.out.println("Fragmentation: " + fragmentation + "%");
		
		if(pause != -1) {
			try {
				Thread.sleep(pause);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Experimental.
	 * 
	 * This method builds a profile of what the current memory allocation looks like in the current heap.
	 * 
	 * You must initialize the profiler once in your app (preferably immediately upon entering) to pre-allocate
	 * the space for the profile.
	 */
	public static void profileMemory() {
		if(memoryProfile == null) {
			System.out.println("You must initialize the memory profiler before it can be used!");
			return;
		}
		currentCount = 0;
		int chunkSize = 100000;
		long memoryAccountedFor = 0;
		boolean succeeded = false;
		
		int threshold  = 4;
		Runtime r = Runtime.getRuntime();
		
		System.gc();
		long memory = r.freeMemory();
		
		//Basically: We go through here and allocate arrays over and over, making them smaller and smaller 
		//until we reach the smallest unit we care about allocating. The parameters can be tuned depending
		//on the type of fragmentation you are concerned about. 
		while(true) {
			if(currentCount >= MEMORY_PROFILE_SIZE) {
				System.out.println("Memory profile is too small for this device's usage!");
				break;
			}
			if(chunkSize < threshold) { succeeded = true; break;}
			
			try {
				memoryHolders[currentCount] = new byte[chunkSize];
				memoryProfile[currentCount * 2] = (memoryHolders[currentCount].hashCode() & 0x00000000ffffffffL);
				memoryProfile[(currentCount * 2) + 1] = chunkSize;
				currentCount++;
				memoryAccountedFor += chunkSize;
			} catch(OutOfMemoryError oom) {
				chunkSize = chunkSize - (chunkSize < 100 ? 1 : 50);
			}
		}
		for(int i = 0 ; i < currentCount; ++i) {
			memoryHolders[i] = null;
		}		
		System.gc();
		
		//For now, just print out the profile. Eventually we should compress it and output it in a useful format.
		if(succeeded) {
			System.out.println("Acquired memory profile for " + memoryAccountedFor + " of the " + memory + " available bytes, with " + currentCount + " traces");
			for(int i = 0 ; i < currentCount * 2 ; i+=2) { 
				System.out.println("Address: " + memoryProfile[i] + " -> " + memoryProfile[i + 1]);
			}
		}
	}
}
