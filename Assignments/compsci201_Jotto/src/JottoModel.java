import java.util.*;

/**
 * JottoModel.java
 * @author ola, mac
 * 
 * Callie Mao
 * 
 * 
 */

public class JottoModel {
	// The View. We store this so that we can call its methods, 
	// like "Show a message."
    private JottoViewer myView;
    
    // The list of legal words.
    private ArrayList<String> myWordList;
    private String myGuess;
    private ArrayList<String> myOriginalList;
    private int myGuessesLeft;
    private Random myRandom;
    
 
    /**
     * Initialize the model appropriately. This is going to include 
     * initializing myWordList (which is the list of possible words), 
     * plus any instance variables you choose to add.
     */
    public JottoModel() {
    	myWordList = new ArrayList<String>();
        myOriginalList = new ArrayList<String>();
        myGuess = new String();
        myGuessesLeft = new Integer(15);
        myRandom = new Random();
    }

    /**
     * Associate a view with this model.
     * @param view is view that's notified when model
     * changes.
     */
    public void addView(JottoViewer view) {
        myView = view;
    }

    /**
     * Display a dialog box. This can be used to tell the user that the 
     * game is over (for example), or to provide other messages. You 
     * don't need to modify this method, but you will use it!
     * 
     * (And yes, that's "modal" with an 'a', not a typo. A "modal" dialog
     * box is one where the user must respond to it by clicking on 
     * something.) This is probably what you're going to use to announce
     * the end of the game.
     * 
     * @param s is the string displayed in the modal dialog
     */
    private void showModalMessage(String s){
        myView.showModalInfo(s);
    }

    /**
     * Display a small message in the view's message area. Unlike 
     * showModalMessage, this doesn't interrupt anything.
     * 
     * You don't need to modify this method. You might use it to inform
     * the user of what they should be doing.
     * 
     * @param s is message displayed
     */
    private void messageViews(String s) {
        myView.showMessage(s);
    }
    
    /**
     * Communicate the guess to the view.
     * 
     * You don't need to modify this method, although you might want to.
     * 
     * You will need to use it! After you figure out what word to guess 
     * next, call this method with that word as an argument.
     * 
     * @param s is the guess
     */
    private void doGuess(String s){
        myView.processGuess(s);
    }

    /**
     * Read in words. 
     * 
     * You don't need to modify this method at all.
     * 
     * @param s is scanner that is source of words.
     */
    public void initialize(Scanner s) {

    	myOriginalList.clear(); 
        while (s.hasNext()) {
            myOriginalList.add(s.next());
        }
        messageViews("Choose \"New Game\" from the menubar.");
    }

    /**
     * Process input from the user. The input is the
     * number of letters in common with the user's secret
     * word. This method does rudimentary analysis of the response for 
     * legality (like "Did they really type a number?") and then calls 
     * processResponse, which is where your code goes.
     * 
     * You don't need to modify this method at all.
     * 
     * @param o is the response from the user. This is a String
     * representing an int that's the number of letters in
     * common with last word guessed by computer. 
     */
    public void process(Object o) {
        String response = (String) o;
        if (response.length() == 0) {
            myView.badUserResponse("Not a number!");
            return;
        }
        try {
            int n = Integer.parseInt(response);
            if (n < 0 || n > 6) {
                myView.badUserResponse("Out of range: " + n);
                return;
            }
            processResponse(n);
        } catch (NumberFormatException e) {
            myView.badUserResponse("Not a number: " + response);
        }
    }

    /**
     * Make the view not respond to user input except choosing new game 
     * or quit (By calling the view's method that disables user input).
     * 
     * You don't need to modify this method at all.
     * 
     * You might want to call it when the game ends.
     */
    public void stopGame() {
    	messageViews("Choose \"New Game\" from the menubar.");
        myView.setStopped();
    }
 
    /**
     * This method is where you do your work. The human player has told 
     * you how many letters overlap with your previous guess; that gets 
     * passed in as n. You must now generate a new guess and call doGuess 
     * with that value. Note that the current version is pretty naive: 
     * it always guesses "bagel", which (while a great word) isn't 
     * likely to do a good job of guessing their word. This method also 
     * needs to be aware of the game ending, and should display a modal 
     * dialog when that happens.
     * 
     * @param n is the number of letters in common with the
     * last computer-generated guess
     */
    public void processResponse(int n) {  
    	
	    	if (n == 6){
	    		showModalMessage("Great! I got your word!");
	    		stopGame();
	    	}	
	    		
	    	else{
				shrinkWordList(n);

	    	
			    if (myGuessesLeft == 1){ 
		    		showModalMessage("I ran out of guesses :(");
		    		stopGame(); 
			    }
		    	
		    	else if (n!= 6 && myWordList.size() == 0){
		    		showModalMessage("I give up! :( Either I don't know the word or you entered conlicting common counts.");
		    		stopGame();
		    	}
    	
		    	else{
		    		myGuess = getRandomWord();
		    		myGuessesLeft --;
		    		messageViews("Guesses Left: " + myGuessesLeft);
		    		doGuess(myGuess);
		    	}
	    	}
    }
    
    //removes words from myWordList that do not have the same common count as "n"
    public void shrinkWordList(int n){
    	ArrayList<String> temp = new ArrayList<String>(); //create temp so that can iterate through myWordList even with deletions
    	for (int i = 0; i < myWordList.size(); i ++){
    		String word = myWordList.get(i);
    		if (commonCount(word,myGuess) == n)
        		temp.add(word);
        	if (temp.contains(myGuess))
        		temp.remove(word);
    	}
    	
    	myWordList = new ArrayList<String>(temp);
    }
    
    
    /**
     * Start a new game -- set up whatever state you want, and generate
     * the first guess made by the computer.
     */
    public void newGame() {
    	myWordList.clear(); 
    	for (String word: myOriginalList)
    		myWordList.add(word);
    	myGuessesLeft = 15; 
    	messageViews("Guesses Left: " + myGuessesLeft);
        myGuess = getRandomWord();
    	doGuess(myGuess); 
    }
    
    public String getRandomWord(){
    	
    	if (myWordList == null || myWordList.size() == 0) {
			return null; //there are no words of this length
		}
		
    	int selection = myRandom.nextInt(myWordList.size());
    	return myWordList.get(selection);
    	//based on hangmanLoader getRandomWord();    	
    }
    
    /**
     * Extra credit! If the player selects the "Smarter AI" choice from 
     * the menu, the view calls this method. This method should set some 
     * instance variable that tells the rest of the code to do a better 
     * job of guessing.
     */
    public void playSmarter() {
    		// TODO: extra credit
    	
    }
   
    /**
     * Returns number of letters in common to a and b, ensuring
     * each common letter only counts once in total returned.
     * 
     * TODO: Implement this method! You're going to need it to actually
     * implement Jotto.
     * 
     * NOTE: This is the trickiest algorithmic part of this assignment.
     * 
     * @param a is one string being compared
     * @param b is other string being compared
     * @return number of letters in common to a and b
     */
    private int commonCount(String a, String b) {
    		HashMap<Character,Integer> lettersA = countLetters(a);
    		HashMap<Character,Integer> lettersB = countLetters(b);
    		
    		int count = 0;
    		for (char letter: lettersA.keySet()){
    			if (lettersB.containsKey(letter)){
    				count += Math.min(lettersA.get(letter), lettersB.get(letter)); // if there are multiple occurances for each letter; take the lowest occurance
    			}
    		}
    		return count;
    }
    
    //sets up the hashmap of character-integer for the string "word"
    public HashMap<Character,Integer> countLetters(String word)
    {
		HashMap<Character,Integer> letterMap = new HashMap<Character,Integer>();
		for (int i = 0; i < word.length(); i++)
		{
			char letter = word.charAt(i);
			if (letterMap.containsKey(letter))
			{
				int numLetters = letterMap.get(letter);
				letterMap.put(letter,++numLetters); 
			}
				
			else
				letterMap.put(letter,1);
		}
		return letterMap;

    }

}
