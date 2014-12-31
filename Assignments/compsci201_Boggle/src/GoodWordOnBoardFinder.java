import java.util.ArrayList;
import java.util.List;


public class GoodWordOnBoardFinder implements IWordOnBoardFinder {
	
	public List<BoardCell> cellsForWord(BoggleBoard board, String word) { 
		List<BoardCell> list = new ArrayList<BoardCell>();
		for(int r=0; r < board.size(); r++){
			for(int c=0; c < board.size(); c++){
				int index = 0; 
				//index denotes the character in "word" that is being matched to 
				//the (row,col) board cell. initialized @ 0 to start @ 1st character
				if (helper(board,r,c,list,word,index)){
					return list;
				}
			}
		}
		return new ArrayList<BoardCell>();
	}

	public boolean helper (BoggleBoard board, int r, int c, List<BoardCell> list, 
			String word, int index){
		//index on the string is larger than the number of letters left to 
		//find on the board; word has been found
		if (index > word.length() - 1)
			return true;
		//row and col out of bounds; don't continue the search
		if (r < 0 || c < 0 || r > board.size() - 1 || c > board.size() - 1)
			return false;
		BoardCell cell = new BoardCell(r,c);
		if (list.contains(cell)) 
			return false; // check to ensure no reuse of previously-used board cells
		
		String boardLetter = board.getFace(r,c);
		int lastIndex;
		if (boardLetter.equals("qu")){
			//check to remain in within word.length()
			if (word.length()-1 >= index+2)
				lastIndex = index+2;
			else
				lastIndex = word.length()-1; 
		}
		else{
			lastIndex = index+1; 
		}
		String currentLetter = word.substring(index,lastIndex); 
		//currentLetter stored for comparison against current boardLetter being checked
		
		if (currentLetter.equals(boardLetter)){
			list.add(cell);
			
			int[] rdelta = {-1,-1,-1, 0, 0, 1, 1, 1};
			int[] cdelta = {-1, 0, 1,-1, 1,-1, 0, 1};
			for(int k=0; k < rdelta.length; k++){
				if (helper(board, r+rdelta[k], c+cdelta[k], list, word, lastIndex))
						return true;
			}
			//next letter has not been found and need to backtrack
			list.remove(cell);
		}
		return false;
	}
}
