import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.PriorityQueue;

public class SimpleHuffProcessor implements IHuffProcessor {
    
	private HuffViewer myViewer;
    private TreeNode myRoot;
    private int[] weights;
    private HashMap<Integer, String> bitMap;
    private int inputSize;
    private int outputSize;
   
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
    	if (force==false && preprocessCompress(in) < 0) //LILA is this right?
        	throw new IOException("compress is not implemented");
    	
    	BitOutputStream output = new BitOutputStream(out);
        output.writeBits(BITS_PER_INT, MAGIC_NUMBER);
       
        for(int i = 0; i < ALPH_SIZE; i++){
                output.writeBits(BITS_PER_INT, weights[i]);
        }
       
        BitInputStream inputStream = new BitInputStream(in);
        int ascIIValue = inputStream.readBits(BITS_PER_WORD);
        int codedNumber = 0;
       
        while(ascIIValue != -1){
                String currentBit = bitMap.get(ascIIValue);
                for(int i = 0; i < currentBit.length(); i++){
                        int currentInt = Integer.parseInt("" + currentBit.charAt(i));
                        output.writeBits(BITS_PER_INT, currentInt);
                        codedNumber++;
                }
        }
       
        output.close();
        inputStream.close();
        System.out.println(codedNumber);
       
       
        return codedNumber;
        }
    
    public int preprocessCompress(InputStream in) throws IOException {
        
    	BitInputStream bis = new BitInputStream(in);
    	int[] weights = new int[256];
    	
    	int current = bis.readBits(BITS_PER_WORD);
    	int badBits = 0;
    	while(current != -1){ 
    		weights[current]++;
    		current = bis.read();
    		badBits += 8;
    	}
    	
    	PriorityQueue<TreeNode> pq = new PriorityQueue<TreeNode>();
		for(int ascII = 0; ascII < weights.length; ascII++){
			if (weights[ascII] != 0)
				pq.add(new TreeNode(ascII, weights[ascII]));
		}
		
        pq.add(new TreeNode (IHuffConstants.PSEUDO_EOF,1));
        

    
    	while (!pq.isEmpty()){
    		if (pq.size() == 1) 
    			break;
    		TreeNode left = pq.poll();
    		TreeNode right = pq.poll();
    		
    		TreeNode node = new TreeNode(0, left.myWeight + right.myWeight, left, right);
    	//	node.myLeft = left;
    	//	node.myRight = right;
    		pq.add(node);
    	}
    	myRoot = pq.remove();
    	
    	HashMap<Integer, String> bitMap = makeMap();
    	int goodBits = 0;
    	
    	for (Integer ascII : bitMap.keySet()){
    		System.out.println("weights[ascII]:" + weights[ascII]);
        	System.out.println("bitMap.get(ascII).length(): " + bitMap.get(ascII).length());
    		goodBits = goodBits + bitMap.get(ascII).length()*weights[ascII];
    	}
    	System.out.println(badBits);
    	System.out.println(goodBits);
    	return badBits - goodBits;   
    	
      }
    
    public HashMap<Integer, String> makeMap(){
        HashMap<Integer, String> output = new HashMap<Integer, String>();
        makeMap(output, myRoot, "");
        return output;    
    }
   
    private void makeMap(HashMap<Integer,String> newBitMap, TreeNode cur, String newBits){
        if(cur ==null) return;
        if(cur.myLeft==null && cur.myRight==null){
	        int ascII = cur.myValue;
            newBitMap.put(ascII, newBits);
        }
 
        makeMap(newBitMap, cur.myLeft, newBits + "0");
        makeMap(newBitMap, cur.myRight, newBits + "1");
 
     }

    public void setViewer(HuffViewer viewer) {
        myViewer = viewer;
    }

    public int uncompress(InputStream in, OutputStream out) throws IOException {
        throw new IOException("uncompress not implemented");
        //return 0;
    }
    
    private void showString(String s){
        myViewer.update(s);
    }

}