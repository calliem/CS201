import java.util.*;

/**
 * @author Callie Mao
 *
 */

public class HangmanStats {
	
	//------------------ PART 1A METHOD ----------------
	public void numberOfWords(HangmanFileLoader loader, int start, int end){	
		System.out.println("Part 1A");
		for (int i = start; i <= end; i ++){
			HashSet<String> set = new HashSet<String>();
			for(int k=0; k < 10000; k ++) { 
				set.add(loader.getRandomWord(i));
			}
			System.out.println("number of words of length " + i + " = \t" + set.size());
		}
	}
	
	//------------------ PART 1B METHOD ----------------
	//return # of calls to getRandomWord() to get a repeat of a word of length 10 
	public int numCalls(HangmanFileLoader loader, int num){
		HangmanFileLoader data = new HangmanFileLoader();
		data.readFile("lowerwords.txt"); 
		String randomWord = data.getRandomWord(num);
		
		String newWord="";
		int counter = 0;
		while (!newWord.equals(randomWord)){
			newWord = data.getRandomWord(num);
			counter ++;
		}
		return counter;	
	}
	
	public static void main(String[] args) {
		HangmanStats stats = new HangmanStats();
		HangmanFileLoader loader = new HangmanFileLoader();
		loader.readFile("lowerwords.txt");
		
		//PART 1A MAIN
		stats.numberOfWords(loader,4,20);
		System.out.println();
		
		//PART 1B MAIN
		System.out.println("Part 1B (please allow time for result to calculate)");
		int totalCalls = 0;
		for (int i = 0; i < 100; i++){
			totalCalls += stats.numCalls(loader, 10); //why doesn't this line of code work?
		}
		int avgCount = totalCalls/100;
		System.out.println("Average calls to get a repeat of a word of length 10: " + avgCount);
		//Last print statement takes a while to load as the for loop progresses through all 100 iterations
	}
}
	