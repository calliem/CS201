import java.util.HashMap;

public class LinkStrand implements IDnaStrand {
	
	private class Node {	
		String info;
		Node next;
		public Node (String s){
			info = s;
			next = null;
		}
	}
	
	private Node myFirst;
	private Node myLast;
	private int myAppends;
	private long mySize;
	
	public LinkStrand(String dna){
		myFirst = new Node(dna);
		myLast = myFirst;
		myAppends = 0;	
		mySize = dna.length();
	}
	
	public LinkStrand(){
		this("");
	}
	@Override
	public IDnaStrand cutAndSplice(String enzyme, String splicee) { 
		if (myFirst.next != null){
	        throw new RuntimeException("link strand has more than one node"); 
	    }
		
		int pos = 0;
        int start = 0;
        String search = this.toString();
        boolean first = true;
        LinkStrand ret = null;
        
        while ((pos = search.indexOf(enzyme, pos)) >= 0) {
            if (first){
                ret = new LinkStrand(search.substring(start, pos));
                first = false;
            }
            else {
                 ret.append(search.substring(start, pos));
                 
            }
            start = pos + enzyme.length();
            ret.append(splicee);
            pos++;
        }

        if (start < search.length()) {
        	// NOTE: This is an important special case! If the enzyme
        	// is never found, return an empty String.
        	if (ret == null){
        		ret = new LinkStrand("");
        	}
        	else {
        		ret.append(search.substring(start));
        	}
        }
        return ret;
	}

	@Override
	public long size() {
		return mySize;
	}

	@Override
	public void initializeFrom(String source) {
		myFirst = new Node(source);
		myLast = myFirst;
        mySize = source.length(); 
	}
	@Override 
	public String toString(){
		StringBuilder string = new StringBuilder();
		Node current = myFirst;
		while(current != null){
			string.append(current.info);
			current = current.next; 
		}
		return string.toString();
	}

	@Override
	public String strandInfo() { 
		return this.getClass().getName();
	}

	@Override
	public IDnaStrand append(IDnaStrand dna) {
			if (dna instanceof LinkStrand){
				LinkStrand linkDna = (LinkStrand)dna;
				myLast.next = linkDna.myFirst;
				myLast = linkDna.myLast;
				mySize += dna.size(); 
				myAppends++;
				return this;
			}
			else
				return append(dna.toString());
	}

	@Override
	public IDnaStrand append(String dna) {
		Node temp = new Node(dna);
		myLast.next = temp;
		myLast = temp;
		mySize += dna.length();
		myAppends++;
		return this;
	}

	@Override
	public IDnaStrand reverse() {  
		if (myFirst==null)
			return this;
		if (myFirst.next==null)
			return new LinkStrand(reverseString(myFirst.info));
		
		//initialize map that stores each unique string and reversed string pair
		HashMap<String, String> uniqueStrings = new HashMap<String, String>();
		
		//add in the first unique string to backwardsList and to the uniqueStrings hashMap
		String reversedString = reverseString(myFirst.info);
		uniqueStrings.put(myFirst.info, reversedString);
		IDnaStrand backwardsList = new LinkStrand(reverseString(myFirst.info));

		Node current = myFirst.next;
		LinkStrand newList;
		while(current != null) {
			if (uniqueStrings.containsKey(current.info)){
				newList = new LinkStrand(uniqueStrings.get(current.info));
			}
			else{
				newList = new LinkStrand(reverseString(current.info));
				uniqueStrings.put(current.info, reverseString(current.info));
			}
			backwardsList = newList.append(backwardsList);
		    current = current.next;
		}
		return backwardsList; 
		
		
		/*original method for reverse() - not including the extra credit portion
		  if (myFirst==null)
			return this;
		if (myFirst.next==null)
			return new LinkStrand(reverseString(myFirst.info));
		
		IDnaStrand backwardsList = new LinkStrand(reverseString(myFirst.info));

		 Node current = myFirst.next;
		 while(current != null) {
			 LinkStrand newList = new LinkStrand(reverseString(current.info));
			 backwardsList = newList.append(backwardsList);
		     current = current.next;

		 }
		 return backwardsList; */
		
		 
		 /* another method for reverse() - not including the extra credit portion
			if( myFirst == null) 
		        return this;
		     if (myFirst.next == null)
		       	return new LinkStrand(reverseString(myFirst.info));
		     Node backwardsList = new Node(reverseString(myFirst.info));
		     Node forwardsList = myFirst.next;
		     while(forwardsList != null) {
		     	Node newFront = new Node(reverseString(forwardsList.info)); 
		       	newFront.next = backwardsList;
		       	myFirst = newFront;
		       	backwardsList = myFirst;
		       	forwardsList = forwardsList.next;
		     }
		     return this; 
		     */
	}
	
	
	public String reverseString(String string){
		StringBuilder builder = new StringBuilder(string);
		return builder.reverse().toString();	
	}

	@Override
	public String getStats() { 
		return String.format("# append calls = %d",myAppends);
	}
		
	/*main method for testing/debugging
	 * public static void main(String[] args){
		IDnaStrand ls = new LinkStrand("abracgatadabra");
		System.out.println(ls);
		System.out.println(ls=ls.cutAndSplice("gat", "NO")); 
		ls.append("ba");
		ls.append("callie");
		System.out.println(ls);
		ls = ls.reverse();
		System.out.println(ls);
		ls2 = ls.smartReverse();
		System.out.println(ls2);
	}*/
}