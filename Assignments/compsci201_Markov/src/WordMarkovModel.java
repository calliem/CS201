import java.util.*;

/**
 *
 * @author Callie Mao
 *
 */
public class WordMarkovModel extends AbstractModel {

    private String myString;
    private Random myRandom;
    private String[] myWords; //list of all words in myString
    private TreeMap<WordNgram, ArrayList<WordNgram>> myMap;
    public static final int DEFAULT_COUNT = 100; // default # random words generated
    private int myLastK;
    private boolean remakeMap;

    public WordMarkovModel() {
       
        myRandom = new Random(1234);
        myMap = new TreeMap<WordNgram, ArrayList<WordNgram>>();
        myLastK = -1; //set to -1 so that the first call in process guarantees that a map will be made
        remakeMap = true; 
    }

    /**
     * Create a new training text for this model based on the information read
     * from the scanner.
     * @param s is the source of information
     */
    public void initialize(Scanner s) {
        double start = System.currentTimeMillis();
        int charCount = readChars(s); 
        myWords = myString.split("\\s+");
        int wordCount = myWords.length;
        double end = System.currentTimeMillis();
        double time = (end - start) / 1000.0;
        super.messageViews("#read: " + wordCount + " words in: " + time + " secs");
    }

    protected int readChars(Scanner s) { 
        String newString = s.useDelimiter("\\Z").next();
        if (newString != myString){
        	remakeMap = true;
        	myString = newString;
        }
        else
        	remakeMap = false;
        
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
        int numWords = DEFAULT_COUNT;
        if (nums.length > 1) {
        	numWords = Integer.parseInt(nums[1]);
        }
        smart(k, numWords);
    }

    // k is the number of characters in the n-gram
    // numWords is the number of words in the output
    public void smart(int k, int numWords){ 
    	double stime = System.currentTimeMillis(); //time to generate text (including map generation time)

    	//checks to make sure the map is not recreated when no new text file or k value is inputted
    	if (k != myLastK || remakeMap == true){ //makes map only if the k value is different or if remakeMap is true (based on the readChars method)     
    		myMap = makeMap(k);
			myLastK = k;
			remakeMap = false;
    	}

    	int start = myRandom.nextInt(myWords.length - k + 1);
    	
    	WordNgram words = new WordNgram(myWords, start, k);
    	StringBuilder build = new StringBuilder();

    	for(int i=0; i<numWords; i++){
    		ArrayList<WordNgram> nextNgrams = myMap.get(words);
       		WordNgram newNgram = nextNgrams.get(myRandom.nextInt(nextNgrams.size()));
       		build.append(newNgram.wordAt(k-1) + " "); //append the last word
       		words = newNgram;		
    	}
    	double etime = System.currentTimeMillis();
        double time = (etime - stime) / 1000.0;
        this.messageViews("Time to generate: " + time);
    	this.notifyViews(build.toString());
    }
    
    public TreeMap<WordNgram, ArrayList<WordNgram>> makeMap(int k){    	
        //create wrapAroundWords string array
    	String[] wrapAroundWords = createWrapAround(k);
               
    	TreeMap<WordNgram, ArrayList<WordNgram>> map = new TreeMap<WordNgram, ArrayList<WordNgram>>();

        for (int i = 0; i < myWords.length; i++){ //goes through all indexes of myString (allows you to generate many different ngrams)
        	WordNgram nword = new WordNgram(wrapAroundWords, i, k);
        	if (!map.containsKey(nword)){
        		map.put(nword, new ArrayList<WordNgram>());
        	}
        	ArrayList<WordNgram> list = map.get(nword);
        	list.add(new WordNgram(wrapAroundWords, i+1, k));
        	map.put(nword, list);
        }
        
        return map;       	
    }
    
    public String[] createWrapAround(int k){
    	String[] wrapAroundWords = new String[myWords.length+k];
        for (int i=0; i<myWords.length; i++){
        	wrapAroundWords[i]=myWords[i];
        }
        for(int j = 0; j < k; j++){
    		wrapAroundWords[myWords.length+j] = myWords[j]; 
    	}
        return wrapAroundWords;
    }
}
