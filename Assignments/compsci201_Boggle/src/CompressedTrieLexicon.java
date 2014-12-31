/*import java.util.ArrayList;
import java.util.Collections;

import TrieLexicon.Node;

public class CompressedTrieLexicon extends TrieLexicon {

    protected Node myRoot; 
    protected int mySize;

    public CompressedTrieLexicon() {
        myRoot = new Node('x', null);
        mySize = 0;
    }

    @Override
    public void load(ArrayList<String> list) {
    	super.load(list);
    	compress();
    }
    
    public void compress(){
    	ArrayList<Node> leaves = new ArrayList<Node>();
    	leaves = getLeaves(myRoot, leaves);
    	
    	for (Node n: leaves){
    		Node parent = n.parent;
    		System.out.println(parent.info);
    		System.out.println(parent.isWord);
    		
    		while (!parent.isWord && parent.children.size() == 1){ //ensure parent is not a word and has no more than 1 child
    			parent.info = parent.info + n.info;
    			parent.children.clear();	
    			n = parent;
    		}
    	}
    }
    
    public ArrayList<Node> getLeaves(Node root, ArrayList<Node> list){
    	if (root == null)
    		return null;

    	if (root.children.size() == 0)
    		list.add(root);
    	
    	for (Node n: root.children.values()){
    		getLeaves(n,list);
    	}
    	for (Node n: list)
    		System.out.print(n.info + " ");
    	return list;
    }
    
    public LexStatus wordStatus(String s) {
        return wordStatus(s.toString());
    }

    public LexStatus wordStatus(StringBuilder s){
        Node t = myRoot;
        for (int k = 0; k < s.length(); k++) {
            char ch = s.charAt(k);
            t = t.children.get(ch);
            if (t == null)
                return LexStatus.NOT_WORD; // no path below? done
        }
        return t.isWord ? LexStatus.WORD : LexStatus.PREFIX; 
    }
    
    public LexStatus wordStatus(String s) {
        int wordIndex = Collections.binarySearch(myWords, s);
        
        if (wordIndex >= 0) 
        	return LexStatus.WORD;
        int checkIndex = -1*wordIndex-1;
        if (checkIndex < myWords.size()){
        	if (myWords.get(checkIndex).startsWith(s)) 
        		return LexStatus.PREFIX; }
        return LexStatus.NOT_WORD;
    }
    
}*/