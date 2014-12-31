/**
 * Implementation of IHuffProcessor with normal headers
 * @author Roger Zou & Cheng Ma
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.PriorityQueue;

public class SimpleHuffProcessor5 implements IHuffProcessor {

	private HuffViewer myViewer;

	private int[] bitCounts;	// maps the ascii character (index) to its frequency (value)
	private TreeNode root;		// root of the huffcode tree
	private String[] huffCode;	// maps the ascii character (index) to its huffcode (value)
	private int inputFileBitSize;	// size of the input file
	private int outputFileBitSize;	// size of the output file

	public int compress(InputStream in, OutputStream out, boolean force) throws IOException {

		// Force Compression
		if (!force) {
			if (outputFileBitSize > inputFileBitSize) {
				throw new IOException("compression uses " + (outputFileBitSize - inputFileBitSize ) + 
						" more bits. Use force compression to compress");	
			}
		}

		int bitsWritten = 0;
		BitInputStream inStream = new BitInputStream(in);
		BitOutputStream outStream = new BitOutputStream(out);

		// write out the magic number
		outStream.writeBits(BITS_PER_INT, STORE_COUNTS);
		bitsWritten += BITS_PER_INT;

		// Header
		for(int k = 0; k < ALPH_SIZE; k++){
			outStream.writeBits(BITS_PER_INT, bitCounts[k]);
			bitsWritten += BITS_PER_INT;
		}

		//reread file being compressed, look up each character's huff code, and print huff code
		int inbits;
		while ((inbits = inStream.readBits(BITS_PER_WORD)) != -1) {
			String code = huffCode[inbits];
			for (int i = 0; i < code.length(); i++) {
				outStream.writeBits(1, Integer.parseInt("" + code.charAt(i)));
				bitsWritten++;
			}

		}

		//printing eof's huff code
		String eofCode = huffCode[256];
		for (int i = 0; i < eofCode.length(); i ++) {
			outStream.writeBits(1, Integer.parseInt("" + eofCode.charAt(i)));
			bitsWritten++;
		}

		outStream.close();
		inStream.close();

		return bitsWritten;
	}

	public int preprocessCompress(InputStream in) throws IOException {
		// clears variables each time preprocessCompress is called
		bitCounts = new int[ALPH_SIZE+1];
		huffCode = new String[ALPH_SIZE+1];
		inputFileBitSize = 0;
		outputFileBitSize = 0;

		// read the bits, add to array table of frequencies (bitCounts)
		int inbits;
		BitInputStream bits = new BitInputStream(in);
		while ((inbits = bits.readBits(BITS_PER_WORD)) != -1) {
			bitCounts[inbits]++;
			inputFileBitSize += BITS_PER_WORD;
		}
		bits.close();

		// create huff tree
		buildTreeFromCounts();

		// calculate output file size
		outputFileBitSize = BITS_PER_INT*ALPH_SIZE;	//header
		outputFileBitSize += BITS_PER_INT;	// magic number
		System.out.println(outputFileBitSize);
		System.out.println("huffCode[i].length() " + huffCode[0]);
		for (int i = 0; i < bitCounts.length; i++) {
			if (huffCode[i] != null){
				outputFileBitSize += huffCode[i].length()*bitCounts[i];
				System.out.println("outputSize" + outputFileBitSize);
			}

		}

		return inputFileBitSize-outputFileBitSize;
	}

	// helper method used by preprocessCompress and uncompress
	private void buildTreeFromCounts() {
		root = null;
		PriorityQueue<TreeNode> queue = new PriorityQueue<TreeNode> (ALPH_SIZE+1);

		bitCounts[PSEUDO_EOF] = 1;
		// adds characters with frequencies > 0 to priority queue
		for (int i = 0; i < bitCounts.length; i++) {
			if (bitCounts[i] > 0)
				queue.add(new TreeNode(i, bitCounts[i]));
		}

		// build tree from queue
		while(queue.size() > 1)
			queue.add(buildTree(queue.remove(), queue.remove()));
		root = queue.remove();

		// map each ASCII character to its huffman code
		preOrder(root, "");
	}

	// takes the left and right nodes, joins them to a parent tree node that is returned
	private TreeNode buildTree(TreeNode left, TreeNode right) {
		TreeNode parent = new TreeNode(0, left.myWeight + right.myWeight);
		parent.myLeft = left;
		parent.myRight = right;
		return parent;
	}

	// preOrder traversal of tree, records path taken {0,1} from root to leaf
	private void preOrder(TreeNode current, String path) {
		if (current == null) return;
		if (current.myLeft == null && current.myRight == null) {
			huffCode[current.myValue] = path;
		}
		preOrder(current.myLeft, path + "0");
		preOrder(current.myRight, path + "1");
	}

	public int uncompress(InputStream in, OutputStream out) throws IOException {

		// clears data structures
		bitCounts = new int[ALPH_SIZE+1];
		huffCode = new String[257];

		int bitsWritten = 0;
		BitInputStream inStream = new BitInputStream(in);
		BitOutputStream outStream = new BitOutputStream(out);

		//magic number
		int magic = inStream.readBits(BITS_PER_INT);
		if (magic != STORE_COUNTS) {
			outStream.close();
			inStream.close();
			throw new IOException("magic number not right");
		}

		//header
		for (int k = 0; k < ALPH_SIZE; k++) { 
			bitCounts[k] = inStream.readBits(BITS_PER_INT);
		}

		// recreate tree
		buildTreeFromCounts();

		// decompress file using Huff tree
		TreeNode current = root;
		while (true) {
			int bits = inStream.readBits(1);
			if (bits == -1) {
				outStream.close();
				inStream.close();
				throw new IOException("error reading bits, no PSEUDO-EOF");
			}

			if ( (bits & 1) == 0)
				current = current.myLeft;
			else
				current = current.myRight;

			if (current.myLeft == null && current.myRight == null) {	// if a leaf node
				if (current.myValue == PSEUDO_EOF)
					break; // out of while-loop
				else {
					outStream.writeBits(BITS_PER_WORD, current.myValue);
					bitsWritten += ("" + current.myValue).length();
				}
				current = root; // start back at top
			}
		}
		outStream.close();
		inStream.close();

		//return num bits written to outStream
		return bitsWritten;

	}

	public void setViewer(HuffViewer viewer) {
		myViewer = viewer;
	}

	@SuppressWarnings("unused")
	private void showString(String s){
		myViewer.update(s);
	}

}
