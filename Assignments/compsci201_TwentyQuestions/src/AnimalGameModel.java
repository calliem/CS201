/*
 * AnimalGameModel.java
 * TODO: YOUR NAME HERE.
 * 
 * Implementation of IAnimalGameModel.
 */
import java.io.*;
import java.util.*;

public class AnimalGameModel implements IAnimalModel {
	
	// In Model-View systems, the Model & View must be able to communicate;
	// this pointer to the view lets you tell the view to do things (show messages,
	// for example).
	private AnimalGameViewer myView;
	private AnimalNode myRoot;
	private AnimalNode myCurrent;
	private AnimalNode myNewLeaf;
	private AnimalNode myPrevious;
	private int mySize;
	private StringBuilder myPath;
	private boolean selectedYes;

	@Override
	public void addNewKnowledge(String question) {
		question = question.replace("?", ""); //remove the question mark
		AnimalNode internalNode = new AnimalNode(question,myCurrent, myNewLeaf);
		if (!selectedYes) //user selected no to the last question
			myPrevious.setNo(internalNode);
		else //user selected yes
			myPrevious.setYes(internalNode);
		newGame();
	}

	@Override
	public void addNewQuestion(String noResponse) {
		noResponse = noResponse.replace("?", "");
		myNewLeaf = new AnimalNode(noResponse, null, null); //new question
		myView.getDifferentiator();
	}

	@Override
	public void initialize(Scanner s) { 
		mySize = 0;
		myRoot = readHelper(s); 
		myView.showMessage("# nodes in tree read: " + mySize);
		myView.setEnabled(true);		
		newGame();
	}
	
	
	private AnimalNode readHelper(Scanner s){
		String line = s.nextLine(); 
		mySize++;
		if (!line.startsWith("#Q:")){ //if line is a leaf
			AnimalNode node = new AnimalNode(line,null,null);
			return node;
		}
		else 
			line = line.substring(3);
		AnimalNode leftNode = readHelper(s);
		AnimalNode rightNode = readHelper(s);
		AnimalNode newNode = new AnimalNode(line,leftNode,rightNode);
		return newNode;			
	}

	@Override
	public void newGame() {
		myPath = new StringBuilder();
		myPath.append("Please phrase as a question (ie. \"is it a cat?\")\n");
		myPath.append("\nYour path so far:\n");
		myPrevious = null;
		myCurrent = myRoot; 
		myView.update(myCurrent.toString()); //ask the first question	
		
	}

	@Override
	public void processYesNo(boolean yes) {
		AnimalNode node;
		String displayAnswers; 
		String question = myCurrent.toString();
	
		//check if user selected yes or no to the parent of the leaf (which is
		//the last question that the user answered)	
		if (myPrevious != null){ //check this condition first
			if (myPrevious.getNo() == myCurrent) 	
				selectedYes = false;
			if (myPrevious.getYes() == myCurrent)	
		 		selectedYes = true;
		}
			
		if(yes){ 
			node = myCurrent.getYes(); 
			displayAnswers = "You answered YES to " + question + "\n";
		}
		else{ 
			node = myCurrent.getNo();	
			displayAnswers = "You answered NO to " + question + "\n";
		}

		myPath.append(displayAnswers);
		myView.update(myPath.toString()); 
		
		// handle next question or end of game
		if(node==null && yes){ 
			myView.showDialog("I win!"); 
			newGame();
		}
		else if(node==null && !yes){ 
			myView.getNewInfoLeaf(); //allows user to add to the tree
		}
		else{ //node has a question in it
			myPrevious = myCurrent;
			myCurrent = node;
			myView.update(myCurrent.toString());	
		}		
	}
	
	@Override
	public void setView(AnimalGameViewer view) {
		myView = view;
	}

	@Override
	public void write(FileWriter writer) throws IOException {
		AnimalNode root = myRoot;
		writeHelper(root, writer);
	}
	
	public void writeHelper(AnimalNode node, FileWriter writer) 
			throws IOException {
		String string = "";
		if (node.getNo() == null &&  node.getYes()==null){ //if node is a leaf
			writer.write(node.toString() + "\n");
			return;
		}
		else
			string = "#Q:" + node.toString() + "\n"; 
		writer.write(string);
		writeHelper(node.getYes(), writer);
		writeHelper(node.getNo(), writer);
	}		
	
	/*DEBUG: display the tree
  	public String toString() {
		AnimalNode copy = myRoot;
		return toString(copy, "") + "\n";
	}

public String toString(AnimalNode current, String level) {
		String leftString = "null";
		String rightString = "null";

		if (current.getYes() != null) {

			leftString = toString(current.getYes(), level + "   ");
		}
		if (current.getNo() != null) {
			rightString = toString(current.getNo(), level + "   ");
		}

		return current.toString() + "\n" + level + "Y: " + leftString + "\n"
				+ level + "N: " + rightString;
		}
	*/


}
