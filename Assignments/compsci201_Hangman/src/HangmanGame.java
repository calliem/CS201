
import java.io.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * 
 * @author Callie Mao
 */

public class HangmanGame {

	// Used for reading data from the console.
	Scanner myInput;
	Set<Character> myGuesses; //sets up a hashset of all guesses (hashset does not allow for duplicates)
	
	public HangmanGame() {
		// Set up our read-from-console.
		myInput = new Scanner(
				new BufferedReader(new InputStreamReader(System.in)));
		myGuesses = new HashSet<Character>();
	}
	
	/**
	 * Get a line from the user and return the line as a String.
	 * 
	 * @param prompt is printed as an instruction to the user
	 * @return entire line entered by the user
	 */
	public String readString(String prompt) {
		System.out.printf("%s ", prompt);
		String entered = myInput.nextLine();
		return entered;
	}
	
	//creates a charArray gameBoard with size "size" and values '_'
	public char[] makeGameBoard(String secretWord) {
		char[] gameBoard = new char[secretWord.length()];
		for (int i = 0; i < gameBoard.length; i++) {
			if (secretWord.charAt(i) == ' ') //sets gameboard value as a space if there is a space in the secret word (for the extra credit portion)
				gameBoard[i] = ' ';
			else if (secretWord.charAt(i) == ',')
				gameBoard[i] = ',';
			else
				gameBoard[i] = '_';
		}
		return gameBoard;
	}
	
	public int makeWordLength(){
		 int wordLength = 0;
		 do{
			 String input = readString("How many letters in guess word?");
			 wordLength = Integer.parseInt(input);
			 if (wordLength < 2 || wordLength > 22) 
				 System.out.println("Please enter a number between 2 and 20."); 
		 } while (wordLength < 2 || wordLength > 22);
		 return wordLength;
	}
	
	//Extra Credit Method
	public int makeActorLength(){
		 int wordLength = 0;
		 do{
			 String input = readString("How many letters in actor's name?");
			 wordLength = Integer.parseInt(input);
			 if (wordLength < 7 || wordLength > 17) 
				 System.out.println("Please enter a number between 7 and 17.");
		 } while (wordLength < 7 || wordLength > 17); 
		 return wordLength;
	}
	
	//Extra Credit Method
	public int makeBookLength(){
		 int wordLength = 0;
		 do{
			 String input = readString("How many letters in book's title?");
			 wordLength = Integer.parseInt(input);
			 if (wordLength < 5 || wordLength > 23) 
				 System.out.println("Please enter a number between 5 and 23."); 
		 } while (wordLength < 5 || wordLength > 23);
		 return wordLength;
	}
	
	public int makeGuessesLeft(){
		int guessesLeft = 0;
		 do{
			 String input = readString("How many guesses until hanging?");
			 guessesLeft = Integer.parseInt(input); 
			 if (guessesLeft < 1)
				 System.out.println("Please enter a number greater than 0.");
		 } while (guessesLeft < 1); 
		 return guessesLeft;
	}
	
	public void displayGuesses(int totalGuesses){
		 System.out.println("Misses left: " + (totalGuesses - myGuesses.size()));
		 System.out.print("Guesses so far: ");
		 for (char i: myGuesses)
			 System.out.print(i + " ");
		 System.out.println();
		 System.out.println();
	}
	

	/**
	 * Play one game of Hangman. This should prompt
	 * user for parameters and then play a complete game.
	 * You'll likely want to call other functions from this one. 
	 * The existing code may provide some helpful examples.
	 */

	public void play(){	
		 String type = readString("From what category of words do you want to guess: \n\"actors\", \"books\", or \"dictionary\"? ");
		 type.toLowerCase();
		 
		 
		 HangmanFileLoader data = new HangmanFileLoader();
		 int wordLength = 0;
		 if (type.equals("actors")){ //EXTRA CREDIT
			 data.readFile("actors.txt");
			 wordLength=makeActorLength();
		 }
		 if (type.equals("books")){ //EXTRA CREDIT
			 data.readFile("books.txt");
			 wordLength=makeBookLength();
		 }
		 if (type.equals("dictionary")){ //ORIGINAL ASSIGNMENT
			 data.readFile("lowerwords.txt");
			 wordLength=makeWordLength();
		 }
		 
		 String secretWord = data.getRandomWord(wordLength).toLowerCase(); //sets up random word of length "wordLength";
		 //toLowerCase() is used for the extra credit part of the assignment since all actors and books are proper nouns in the text document
		
		 char[] gameBoard = makeGameBoard(secretWord);	 //sets up array of "_" of all current guesses 
		 
		 int totalGuesses = makeGuessesLeft(); //sets up number of guesses left
		 boolean gameWon = false;
	
		 while (myGuesses.size() < totalGuesses) {
			 String currentWord = new String(gameBoard);
			 String guess = readString("What letter do you want to guess?");
			 char guessedChar = Character.toLowerCase(guess.charAt(0)); 
			 int index = secretWord.indexOf(guessedChar);	
			 
			 if (index >= 0){ //checks to make sure at least one of the guessedChar is in the secret word
				 while (index >=0){ //loops through to check for all occurrences of guessedChar in the secret word
					gameBoard[index] = guessedChar;
					index = secretWord.indexOf(guessedChar, index+1);
				 }
			     currentWord = new String(gameBoard);

				 if (currentWord.equals(secretWord)){  
					 System.out.println("Congratulations, you guessed the word! The word was " + secretWord + "!");
					 gameWon = true;
					 break; 
				 }
				 else {
					 String letter = Character.toString(guessedChar);
					 System.out.print("\"" + letter + "\" is in the word: " );
					 myGuesses.add(guessedChar);
					 System.out.println(currentWord);
					 displayGuesses(totalGuesses);
				 }
			 } 
			 else {//occurs if none of the guessedChar is in the secret word
				 String letter = Character.toString(guessedChar);
				 System.out.print("There is no \"" + letter + "\" in this word: ");
				 myGuesses.add(guessedChar);
				 System.out.println(currentWord);
				 displayGuesses(totalGuesses);
			 }
		 }
		 
		 if (!gameWon) {
			 System.out.println("You got hung :(. The secret word was " + secretWord + ".");
		 }	 
	}
}
