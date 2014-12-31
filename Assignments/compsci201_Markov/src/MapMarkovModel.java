import java.util.*;
import java.io.*;

/**
 * The model for the Markov text generation assignment. See methods
 * for details. This model uses a _extremely_ brute-force algorithm for 
 * generating text, e.g., the entire training text is rescanned each time
 * a new character is generated.
 * @author ola
 * @author Callie Mao
 *
 */
public class MapMarkovModel extends AbstractModel {
    
    private String myString;
    private Random myRandom;
    private HashMap<String, ArrayList<String>> myMap;
    private int myLastK;
    private boolean remakeMap;
    public static final int DEFAULT_COUNT = 100; // default # random letters generated

    public MapMarkovModel() {
        myRandom = new Random(1234);
        myMap = new HashMap<String, ArrayList<String>>();
        remakeMap = true;
        myLastK = -1; //set to -1 so that the first call in process guarantees that a map will be made
    }

    /**
     * Create a new training text for this model based on the information read
     * from the scanner.
     * @param s is the source of information
     */
    public void initialize(Scanner s) {
        double start = System.currentTimeMillis();
        int count = readChars(s);
        double end = System.currentTimeMillis();
        double time = (end - start) / 1000.0;
        super.messageViews("#read: " + count + " chars in: " + time + " secs");
    }

    protected int readChars(Scanner s) {
        String newString = s.useDelimiter("\\Z").next(); 
        if (!newString.equals(myString)){
        	remakeMap = true;
        	myString = newString;
        }
        else{
        	remakeMap = false;
        }
        
        s.close();      
        return myString.length();
    }

    /**
     * Generate N letters using an order-K markov process where
     * the parameter is a String containing K and N separated by
     * whitespace with K first. If N is missing it defaults to some
     * value.
     */
    public void process(Object o) {
        String temp = (String) o;
        String[] nums = temp.split("\\s+");
        int k = Integer.parseInt(nums[0]);
        int numLetters = DEFAULT_COUNT;
        if (nums.length > 1) {
            numLetters = Integer.parseInt(nums[1]);
        }
        
        smart(k, numLetters);
    }

    // k is the number of characters in the n-gram
    // numLetters is the number of letters in the output
    public void smart(int k, int numLetters){
    	//checks to make sure the map is not recreated when no new text file or k value is inputted
    	if (k != myLastK || remakeMap == true){ //makes map only if the k value is different or if remakeMap is true (based on the readChars method)     
            double start = System.currentTimeMillis();
    		myMap = makeMap(k);
            double end = System.currentTimeMillis();
            System.out.println("makeMap time: " + ((end-start)/1000)); //measures map generation time for analysis
			myLastK = k;
			remakeMap = false;
    	}
    	
    	int start = myRandom.nextInt(myString.length() - k + 1);
    	String str = myString.substring(start, start + k);
    	StringBuilder build = new StringBuilder();

    	double stime = System.currentTimeMillis(); //measures time to generate text (not including map generation time) for analysis
    	for(int i=0; i<numLetters; i++){
    		ArrayList<String> nextNgrams = myMap.get(str);
       		String newNgram = nextNgrams.get(myRandom.nextInt(nextNgrams.size())); 
       		build.append(newNgram.charAt(k-1)); //append the last character
       		str = newNgram;		
    	}
    	double etime = System.currentTimeMillis();
        double time = (etime - stime) / 1000.0;
        this.messageViews("Time to generate: " + time);
    	this.notifyViews(build.toString());
    }
    
    public HashMap<String, ArrayList<String>> makeMap(int k){    	
        String wrapAroundString = myString + myString.substring(0,k);   
    	HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();

        for (int i = 0; i < myString.length(); i++){ //goes through all indexes of myString (allows you to generate many different ngrams)
        	String ngram = wrapAroundString.substring(i,i+k); //gets n gram at the given indexes
        	if (!map.containsKey(ngram)){
        		map.put(ngram, new ArrayList<String>());
        	}
        	ArrayList<String> list = map.get(ngram);
        	list.add(wrapAroundString.substring(i+1,i+k+1));
        	map.put(ngram, list);
        }
        return map;       	
    }
}
