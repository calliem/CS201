import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
 
 
 
public class SimpleHuffProcessorFINAL implements IHuffProcessor {
   
    private HuffViewer myViewer;
    private Map<Integer,String> myMap;
    private  TreeNode myRoot;
    private int myWeights[];
   
    /**
     * This method preprocesses data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is not a BitInputStream, so convert into the right type.
     * @param in is the stream which could be subsequently compressed
     * @return number of bits saved by compression or some other measure
     */
    public int preprocessCompress(InputStream in) throws IOException {
               
        BitInputStream bis = new BitInputStream(in);
        myWeights = new int[IHuffConstants.ALPH_SIZE];
       
        int current = bis.readBits(IHuffConstants.BITS_PER_WORD);
        while(current != -1){
                myWeights[current]++;
                current = bis.readBits(IHuffConstants.BITS_PER_WORD);
        }
        bis.close();
       
        PriorityQueue<TreeNode> q = new PriorityQueue<TreeNode>();
        makeQueue(myWeights, q);
       
        q.add(new TreeNode(PSEUDO_EOF, 1));
        makeTree(q, myRoot);
        q.clear();
       
        myRoot= createTree();
        //recursiveprint(myRoot); //LILA
        Map<Integer,String> bitMap = new TreeMap<Integer, String>();
        createMap("", bitMap, myRoot);
        myMap = bitMap;
       
        //LILA debug
        recursiveprint(myRoot); System.out.println(myRoot.myWeight);
       
        System.out.println("-------");
        for(Integer i: myMap.keySet()){
            int k = i;
            System.out.println(myMap.get(i) + " " + (char) k );
        }
 
       /*       System.out.println("precompress myWeights:");
        for (int i = 0; i < myWeights.length; i++)
                System.out.print(myWeights[i] + " ");*/
       
        return bitsSaved(myWeights, myMap);
    }
   
    void makeQueue(int[] values, PriorityQueue<TreeNode> q) {
        for (int i = 0; i < ALPH_SIZE; i++) {
                if (values[i] > 0) {
                        q.add(new TreeNode(i, values[i]));
                }
        }
        }
       
        void makeTree(PriorityQueue<TreeNode> qCopy, TreeNode cur) {
        while (!qCopy.isEmpty()) {
                if (qCopy.size() < 2) {
                        break;
                }
                TreeNode left = qCopy.poll();
                TreeNode right = qCopy.poll();
                TreeNode newNode = new TreeNode(-1, left.myWeight + right.myWeight,
                                left, right);
                qCopy.add(newNode);
        }
        myRoot = qCopy.poll();
 
        }
 
        public void createMap(String bitcode, Map<Integer, String> freqMapping,
                        TreeNode curNode) {
       
                if (curNode == null) {
                        return;
                }
                if (curNode.myValue != -1) {
                        freqMapping.put(curNode.myValue, bitcode);
                }
                createMap(bitcode + "0", freqMapping, curNode.myLeft);
                createMap(bitcode + "1", freqMapping, curNode.myRight);
              //  return freqMapping;
        }
           
    public int bitsSaved (int[] letterWeights, Map<Integer, String> mapping){
        int saved = 0;
        for (Integer i : mapping.keySet()){
                if (i < letterWeights.length){ //ignore psuedo EOF
                        int count = letterWeights[i];
                        saved+= (8*count)- (count* mapping.get(i).toString().length());
                }
        }
        saved = saved - (32*256); //header     
        return saved;
    }
   
    //This method creates a map where the keys are the Integer value, where the value is the new compressed path from the tree
 
   
   
    /**
     * This method compresses input to output, where the same InputStream has
     * previously been pre-processed via preprocessCompress
     * storing state used by this call.
     * @param in is the stream being compressed (not a BitInputStream)
     * @param out is bound to a file/stream to which bits are written
     * for the compressed file (not a BitOutputStream)
     * @return the number of bits written
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        int saved= bitsSaved(myWeights, myMap);
       
        if (force==false && saved<0){ // counter > saved ??? overflow: counter - saved ( from preprocess compress)
                throw new IOException("Compression uses "+saved*(-1)+" more bits. Use force compression");
        }
        BitInputStream input = new BitInputStream(in);
        BitOutputStream output = new BitOutputStream (out);
       
        int counter=0;
        output.writeBits(BITS_PER_INT, MAGIC_NUMBER);
        counter += BITS_PER_INT;
       
        //create header (weights of each character)
        for (int i = 0; i < myWeights.length;i++){
                output.writeBits(BITS_PER_INT,  myWeights[i]);
                counter += BITS_PER_INT;
        }
       
        int currentWord = input.readBits(IHuffConstants.BITS_PER_WORD);
        System.out.println("currentword: "); //ascii code
         while(currentWord != -1){
                 System.out.println(currentWord);
             for (int j = 0; j < myMap.get(currentWord).length();j++){
                 output.writeBits(1,  myMap.get(currentWord).charAt(j));;
                 counter +=1;
             }
             currentWord = input.readBits(BITS_PER_WORD);
         }
         
         String temp = myMap.get(PSEUDO_EOF);
         for (int j = 0; j < temp.length();j++){
                 output.writeBits(1,temp.charAt(j));
                 counter +=1;
         }
         
         output.flush();
         output.close();
         input.close();
         
         return counter;
    }
   
 
 
    //This method creates the header using a Standard Tree Format
 /*   public BitOutputStream createTreeHeader(TreeNode currentplace, BitOutputStream output){
   
        if (currentplace!=null){
                if (currentplace.myValue!= -1)
                {
                        output.writeBits(1, 1);
                       
                        output.writeBits(9, currentplace.myValue);
                       
                }
                else{
                        output.writeBits(1, 0);
                        createTreeHeader(currentplace.myLeft, output);
                        createTreeHeader(currentplace.myRight,output);
                }
        }
        return output;
    }*/
   
    //This method creates a header for a Standard Count Format
 /*   public BitOutputStream createCountHeader(BitOutputStream output){
        //output.writeBits(BITS_PER_INT, IHuffConstants.STORE_COUNTS);
        for(int k=0; k < ALPH_SIZE; k++){
            output.writeBits(BITS_PER_INT, myWeights[k]);
            counter += BITS_PER_INT;
        }
        return output;
    }*/
   
    /**
     * This method uncompresses a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * The only headers that this method recognizes is the standard tree format or the standard count format.
     * @param in is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
        BitInputStream uncompressIn = new BitInputStream(in);
        BitOutputStream uncompressOut = new BitOutputStream(out);
       
        int magic = uncompressIn.readBits(BITS_PER_INT);
        if (magic != MAGIC_NUMBER){
                uncompressIn.close();
                uncompressOut.close();
                //LILA2
                throw new IOException("magic number not right");
        }
               
        //update weights of array to recreate your tree
        for (int k = 0; k < ALPH_SIZE; k++){
                int bits = uncompressIn.readBits(BITS_PER_INT);
                myWeights[k] = bits;
        }
       
        /*System.out.println("uncompress myWeights:");
        for (int i = 0; i < myWeights.length; i++)
                System.out.print(myWeights[i] + " ");*/
   
        TreeNode root = createTree(); //can you just call myRoot here?
        TreeNode temp = root;
   
        System.out.println("-------- uncompress tree:");
        recursiveprint(myRoot); System.out.println(myRoot.myWeight); //LILA - debugged here: the trees are the same!
 
       
        Map<Integer, String> huffMap = new HashMap<Integer, String>();
        createMap("", huffMap, myRoot); //same as in preprocess with the 0's and 1's
                //LILA i never use the map here...need the map to figure out the pseudo_eof right...?
       
        int bitsWritten = 0;
                while (true){
                        int bit = uncompressIn.readBits(1);
                        System.out.print(bit);
                        if (bit == -1){
                                //throw new IOException("error reading bits, no PSEUDO-EOF");
                                System.out.println("error reading bits, no PSEUDO-EOF");
                                break;
                        }
                        if ((bit & 1) == 0)
                                temp = temp.myLeft;
                        //if (bit == 1)
                        else
                                temp = temp.myRight;
                       
                        if (temp.myLeft == null && temp.myRight == null){ //is leaf
                                System.out.println(" temp.myValue: " + temp.myValue);
                                if (temp.myValue == PSEUDO_EOF)
                                        break;
                                else{
                                        bitsWritten += BITS_PER_WORD;
                                        uncompressOut.writeBits(BITS_PER_WORD, temp.myValue);
                                        temp = root;
                                }
                        }
                }
                uncompressIn.close();
                uncompressOut.close();
                return bitsWritten;
        }
                                               
       
        //preprocess = original - compress
 
 
   
    //This method generates the tree from an array of counts
    public TreeNode createTree (){
        PriorityQueue<TreeNode> pq = new PriorityQueue<TreeNode>();
        for(int i = 0; i < myWeights.length; i++){
                if(myWeights[i]>0)
                        pq.add(new TreeNode(i,myWeights[i]));
        }
        pq.add(new TreeNode (IHuffConstants.PSEUDO_EOF,1));
       
    //  TreeNode root= new TreeNode(-1, 0);
    //  TreeNode root = null;
        while (pq.size() > 1){
                TreeNode left = pq.remove();
                TreeNode right= pq.remove();
                TreeNode node = new TreeNode(-1, left.myWeight+right.myWeight, left, right); // -1 means it is an internal node
                pq.add(node);
        //      root = node;
        }
        TreeNode root = pq.remove();
        return root;
    }
   
 
    public static void recursiveprint(TreeNode root) {
        TreeNode temp = root;
        if (root != null) {
                if (root.myValue == -1) {
                        System.out.println(-1 + ": " + root.myWeight);
                } else {
                        System.out.println((char) root.myValue + ": " + root.myWeight);
                }
 
                if (root.myValue == -1) {
                        recursiveprint(root.myLeft);
                        recursiveprint(root.myRight);
                }
        }
        root = temp;
}
   
   
    //This method shows the string onto the GUI myViewer
    private void showString(String s){
        myViewer.update(s);
    }
 
  //This method sets the viewer in the GUI myViewer to the most updated current viewer
    public void setViewer(HuffViewer viewer) {
        myViewer = viewer;
    }
 
}