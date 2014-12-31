import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
 
public class SimpleHuffProcessor3 implements IHuffProcessor {
 
        private HuffViewer myViewer;
        private Map<Integer, String> myMap;
        private TreeNode myRoot;
        private int myWeights[];
 
        /**
         * This method preprocesses data so that compression is possible --- count
         * characters/create tree/store state so that a subsequent call to compress
         * will work. The InputStream is not a BitInputStream, so convert into the
         * right type.
         *
         * @param in
         *            is the stream which could be subsequently compressed
         * @return number of bits saved by compression or some other measure
         */
        public int preprocessCompress(InputStream in) throws IOException {
 
                BitInputStream bis = new BitInputStream(in);
                myWeights = new int[IHuffConstants.ALPH_SIZE];
                int originalSize = 0;
 
                int current = bis.readBits(IHuffConstants.BITS_PER_WORD);
                while (current != -1) {
                        myWeights[current]++;
                        originalSize += 32;
                        current = bis.readBits(IHuffConstants.BITS_PER_WORD);
                }
                bis.close();
 
                PriorityQueue<TreeNode> q = new PriorityQueue<TreeNode>();
                makeQueue(myWeights, q);
                
                q.add(new TreeNode(PSEUDO_EOF, 1));
                makeTree(q, myRoot);
                q.clear();

                recursiveprint(myRoot); System.out.println(myRoot.myWeight);

                Map<Integer, String> temp = new HashMap<Integer, String>();
                createMap("", temp, myRoot);
                myMap = temp;
               
                for(Integer i: myMap.keySet()){
                	int k = i;
                	System.out.println(myMap.get(i) + " " + (char) k );
            }
                System.out.println();
 
                return bitsSaved(myWeights, myMap);// bitsSaved(myWeights, bitMap);
        }
 
        void makeQueue(int[] values, PriorityQueue<TreeNode> q) {
 
                for (int i = 0; i < ALPH_SIZE; i++) {
                        if (values[i] > 0) {
                                q.add(new TreeNode(i, values[i]));
                                // System.out.println((char) i + ": " + myWeights[i]);
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
                        // create new TreeNode as parent, add to pq
                        qCopy.add(newNode);
                        // get remaining node and set as root
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
        }
 
        public int bitsSaved(int[] letterWeights, Map<Integer, String> mapping) {
                int saved = 0;
                for (Integer i : mapping.keySet()) {
                        if (i < letterWeights.length) { // ignore psuedo EOF
                                int count = letterWeights[i];
                                saved += (8 * count)
                                                - (count * mapping.get(i).toString().length());
                        }
                }
                saved = saved - (32 * 256); // header
                return saved;
        }
 
        // This method prints out the tree; used for debugging
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
 
        /**
         * This method compresses input to output, where the same InputStream has
         * previously been pre-processed via preprocessCompress storing state used
         * by this call.
         *
         * @param in
         *            is the stream being compressed (not a BitInputStream)
         * @param out
         *            is bound to a file/stream to which bits are written for the
         *            compressed file (not a BitOutputStream)
         * @return the number of bits written
         */
        public int compress(InputStream in, OutputStream out, boolean force)
                        throws IOException {
               
                //check for force compression and saved bits
                int saved = bitsSaved(myWeights, myMap);
 
                if (force == false && saved < 0) {
                        throw new IOException("Compression uses " + saved * (-1)
                                        + " more bits. Use force compression");
                }
               
                BitOutputStream output = new BitOutputStream(out);
                int counter = 0;
                //Write Magic number
                output.writeBits(BITS_PER_INT, MAGIC_NUMBER);
                counter += BITS_PER_INT;
 
                // create header
                for (int i = 0; i < ALPH_SIZE; i++) {
                       
                        if(myWeights[i]>0){
                                output.writeBits(BITS_PER_INT, myWeights[i]);
                                //System.out.println(myWeights[i] + " " + i + " " + (char) i);
                                counter += BITS_PER_INT;
                        }
                       
                }
                System.out.println();
               
 
                BitInputStream input = new BitInputStream(in);
                int currentWord = input.readBits(IHuffConstants.BITS_PER_WORD);
                while (currentWord != -1) {
                        for (int j = 0; j < myMap.get(currentWord).length(); j++) {
                                output.writeBits(1, myMap.get(currentWord).charAt(j));
                                counter += 1;
                        }
                        currentWord = input.readBits(BITS_PER_WORD);
                }
 
                //System.out.println(myMap.get(PSEUDO_EOF));
               
                output.writeBits(BITS_PER_INT, PSEUDO_EOF);
                counter += BITS_PER_INT;
 
                output.flush();
                output.close();
                input.close();
               
                return counter;
        }
 
        // This method creates the header using a Standard Tree Format
        /*
         * public BitOutputStream createTreeHeader(TreeNode currentplace,
         * BitOutputStream output){
         *
         * if (currentplace!=null){ if (currentplace.myValue!= -1) {
         * output.writeBits(1, 1);
         *
         * output.writeBits(9, currentplace.myValue);
         *
         * } else{ output.writeBits(1, 0); createTreeHeader(currentplace.myLeft,
         * output); createTreeHeader(currentplace.myRight,output); } } return
         * output; }
         */
 
        // This method creates a header for a Standard Count Format
        /*
         * public BitOutputStream createCountHeader(BitOutputStream output){
         * //output.writeBits(BITS_PER_INT, IHuffConstants.STORE_COUNTS); for(int
         * k=0; k < ALPH_SIZE; k++){ output.writeBits(BITS_PER_INT, myWeights[k]);
         * counter += BITS_PER_INT; } return output; }
         */
 
        /**
         * This method uncompresses a previously compressed stream in, writing the
         * uncompressed bits/data to out. The only headers that this method
         * recognizes is the standard tree format or the standard count format.
         *
         * @param in
         *            is the previously compressed data (not a BitInputStream)
         * @param out
         *            is the uncompressed file/stream
         * @return the number of bits written to the uncompressed file/stream
         */
        public int uncompress(InputStream in, OutputStream out) throws IOException {
                int bitsWritten = 0;
                BitInputStream uncompressIn = new BitInputStream(in);
                BitOutputStream uncompressOut = new BitOutputStream(out);
 
                int magic = uncompressIn.readBits(BITS_PER_INT);
                if (magic != MAGIC_NUMBER){
                        uncompressIn.close();
                        uncompressOut.close();
                        throw new IOException("magic number not right");
                }
 
                // update weights of array to recreate your tree
                int[] weights = new int[ALPH_SIZE];
                for (int k = 0; k < ALPH_SIZE; k++) {
                        int bits = uncompressIn.readBits(BITS_PER_INT);
                        if(bits > 0 ){
                                //System.out.println();
                                weights[bits]++;
                        }
                }
 
                PriorityQueue<TreeNode> pq = new PriorityQueue<TreeNode>();
                makeQueue(weights, pq);
                TreeNode temp = myRoot;
                makeTree(pq, temp);
               
                while (!pq.isEmpty()) {
                        int w = 0;
                        if (pq.peek() != null) {
                                w = pq.peek().myWeight;
                        } else {
                                w = 0;
                        }
                        char c;
                        if(pq.peek().myValue == PSEUDO_EOF){
                                c = '@';
                        }else{
                                c = (char) pq.poll().myValue;
                        }  
                        System.out.println(c + ": " + w);
                }
 
                Map<Integer, String> huffMap = new HashMap<Integer, String>();
                createMap("", huffMap, temp); // same as in preprocess with the 0's
                                                                                // and 1's
 
                int bitsWritten1 = 0;
                int iterations = 1;
                while (iterations < myRoot.myWeight) {
                        int bit = uncompressIn.readBits(1);
                       
                        if (bit == -1){
                                uncompressOut.close();
                                uncompressIn.close();
                                throw new IOException("error reading bits, no PSEUDO-EOF");
                        }
 
 
                        if (temp.myLeft == null && temp.myRight == null) { // is leaf
                                if (temp.myValue == PSEUDO_EOF)
                                        break;
                                else {
                                        bitsWritten += BITS_PER_WORD;
                                        uncompressOut.writeBits(BITS_PER_WORD, temp.myValue);
                                        temp = myRoot;
                                        iterations++;
                                        bitsWritten+=8;
                                }
                        if (bit == 0)
                                temp = temp.myLeft;
                        if (bit == 1)
                                temp = temp.myRight;
                               
                        }
                }
                uncompressIn.close();
                uncompressOut.close();
                return bitsWritten;
        }
 
        // This method shows the string onto the GUI myViewer
        private void showString(String s) {
                myViewer.update(s);
        }
 
        // This method sets the viewer in the GUI myViewer to the most updated
        // current viewer
        public void setViewer(HuffViewer viewer) {
                myViewer = viewer;
        }
 
}