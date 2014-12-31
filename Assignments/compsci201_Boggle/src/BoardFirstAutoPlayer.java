import java.util.ArrayList;
import java.util.List;

public class BoardFirstAutoPlayer extends AbstractAutoPlayer {
    
    @Override
    public void findAllValidWords(BoggleBoard board, ILexicon lex, int minLength) {
    	clear();
    	for (int row = 0 ; row < board.size(); row++){
    		for (int col = 0; col < board.size(); col++){
    			StringBuilder currentWord = new StringBuilder(); 
    			currentWord.append("");
    			List<BoardCell> usedCells = new ArrayList<BoardCell>(); 
    			helper(board, row, col, usedCells, currentWord, lex, minLength);
    		}
    	}
    }
    
    public void helper(BoggleBoard board, int row, int col, 
    		List<BoardCell> usedCells, StringBuilder currentWord, 
    		ILexicon lex, int minLength){
    	if (row < 0 || col < 0 || row > board.size() - 1 || col > board.size() - 1)
    		return; //stop search if out of bounds
    	BoardCell currentCell = new BoardCell(row,col);
    	if (usedCells.contains(currentCell))
    		return; //stop search if the cell has already been used previously
    			
    	//append letter to the word
    	String currentLetter = board.getFace(row,col); //get the letter at (row,col)
    	currentWord.append(currentLetter);
    	
    	//check if currentWord is a prefix or word. if so, then continue search
    	if (lex.wordStatus(currentWord) == LexStatus.PREFIX 
    			|| lex.wordStatus(currentWord) == LexStatus.WORD){
    		usedCells.add(currentCell);
    		if (lex.wordStatus(currentWord) == LexStatus.WORD 
    				&& currentWord.length() >= minLength)
    			add(currentWord.toString());
    		int[] rdelta = {-1,-1,-1, 0, 0, 1, 1, 1};
    		int[] cdelta = {-1, 0, 1,-1, 1,-1, 0, 1};
    		for(int k=0; k < rdelta.length; k++){
    			helper(board, row+rdelta[k], col+cdelta[k], usedCells, 
    					currentWord, lex, minLength);
    		}
    	}
    	//backtrack
    	usedCells.remove(currentCell);
    	for(int i=0; i<currentLetter.length(); i++){ 
    		currentWord.setLength(currentWord.length()-1); //delete last letter
    		if (currentCell.toString().equals("Qu")) 
				currentWord.setLength(currentWord.length()-2); 
		}
    }
    		

    	
}
