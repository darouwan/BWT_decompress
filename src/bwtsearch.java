import java.io.*;
import java.util.*;

/**
 * @author CJF
 * 
 */
public class bwtsearch {
	/**
	 * The BL array. 
	 */
	private HashMap<Character, Integer>[] BL;
	
	/**
	 * standard length of BL. The practice length of BL may be changed based on this value. 
	 */
	static private long l;
	/**
	 * the file name of bwt file
	 */
	private String fileName;
	/**
	 * the size of BL block
	 */
	private long sizeOfBL;
	/**
	 * the number of BL blocks
	 */
	private long lengthOfBL;
	/**
	 * Reader used for reading in bwt encoded file
	 */
	private RandomAccessFile reader;
	/**
	 * save the first position of found results
	 */
	private static int first = -1;
	/**
	 * save the last position of found results
	 */
	private static int last = -1;
	/**
	 * the specified character in F
	 */
	private char chInF;
	/**
	 * position of specified character in F
	 */
	private int indexOfChInF;
	/**
	 * Assisted variable used for calculate the C[c]. Contain how many times
	 * character c occurs in encoded file
	 */
	static private HashMap<Character, Integer> CArray;

	/**
	 * Return the position of first found key word in F. Only available after
	 * using backwardSearch() method.
	 * 
	 * @return the position of first found key word in F
	 */
	public static int getFirst() {
		return first;
	}

	/**
	 * Return the position of last found key word in F. Only available after
	 * using backwardSearch() method.
	 * 
	 * @return the position of last found key word in F
	 */
	public static int getLast() {
		return last;
	}

	/**
	 * Search the keyword P using HashMap C.
	 * <p>
	 * Return the status of search result in boolean value and save the position
	 * of first result in variable {@link #first } and the position of last
	 * result in variable {@link #last }.
	 * 
	 * @param P
	 *            the key word
	 * @param C
	 *            contains the total number of text chars in T which are
	 *            alphabetically smaller then c (including repetitions of chars)
	 * @return if the key word is found
	 */
	public boolean backwardSearch(String P, HashMap<Character, Integer> C) {

		int i = P.length() - 1;
		char c = P.charAt(P.length() - 1);
		int First = 0;
		int Last = -1;
		Object Ci = C.get(c);
		if (Ci == null) {
			//System.out.println("Not Found");

			this.first = -1;
			this.last = -1;
		} else {
			First = C.get(c) + 1;
		}

		Object[] key = C.keySet().toArray();
		Arrays.sort(key);

		int i2 = 0;
		for (int i1 = 0; i1 < key.length; i1++) {
			char temp = (Character) key[i1];
			if (temp == c) {
				i2 = i1 + 1;
				break;
			}
		}
		Object oTemp2 = key[i2];
		if (oTemp2 == null) {
			oTemp2 = (char)128;
		}
		char temp2 = (Character) oTemp2;
		Last = C.get(temp2);
		while (First <= Last && i >= 1) {
			c = P.charAt(i - 1);
			Object Cj = C.get(c);
			if (Cj == null) {
				this.first = First;
				this.last = Last;
				return false;
			} else {
				// if yes, continue
				First = (Integer) Cj + this.getOcc(c, First - 1) + 1;
				Last = (Integer) Cj + this.getOcc(c, Last);
				i = i - 1;
			}
		}

		this.first = First;
		this.last = Last;
		//System.out.println("First = " + first + " Last = " + last);
		if (Last < First) {
			//System.out.println("Not Found");
			return false;
		} else {
			//System.out.println(P + " Found " + (Last - First + 1)
			//		+ " Result(s)");
			return true;
		}
	}

	/**
	 * Use {@link #CArray} to calculate the C[c] HashMap.
	 * <p>
	 * C[c] contains the total number of text chars in T which are
	 * alphabetically smaller then c (including repetitions of chars).
	 * <p>
	 * The key '€' record all number of character in bwt encoded file
	 * 
	 * @return C[c] in HashMap
	 * @throws IOException
	 */
	public HashMap<Character, Integer> getC() throws IOException {
		HashMap<Character, Integer> hm = new HashMap<Character, Integer>();
		reader = new RandomAccessFile(fileName, "r");

		hm = CArray;

		Object[] key = hm.keySet().toArray();
		Arrays.sort(key);
		HashMap<Character, Integer> resultHM = new HashMap<Character, Integer>();
		resultHM.put((Character) key[0], 0);
		
		int i;
		for (i = 1; i < key.length; i++) {
			int value = hm.get(key[i - 1]) + resultHM.get(key[i - 1]);
			resultHM.put((Character) key[i], value);
		}

		int value = hm.get(key[i - 1]) + resultHM.get(key[i - 1]);
		resultHM.put((char)128, value);

		//System.out.println("C[ ] for T = " + resultHM.toString());
		return resultHM;
	}

	/**
	 * calculate the Occ. Occ means how many times character c occurs before
	 * position i in whole file.
	 * 
	 * @param c
	 *            the character
	 * @param i
	 *            the position of c
	 * @return number of occurrences of char c in prefix L[1,q]
	 */
	public int getOcc(char c, int i) {

		int indexOfBL = (int) (i / this.sizeOfBL + 1);
		long start = (indexOfBL - 1) * this.sizeOfBL + 1;
		int occ = 0;
		try {

			byte[] cbuf = new byte[(int) this.sizeOfBL];
			reader.seek(start - 1);
			reader.read(cbuf);

			// get c occurrences in this block
			int countOfC = 0;
			for (int j = 0; j < i - start + 1; j++) {
				if (cbuf[j] == c) {
					countOfC++;
				}
			}

			int sum = 0;

			if (BL[indexOfBL - 1] != null) {
				Object temp = BL[indexOfBL - 1].get(c);
				if (temp != null) {
					sum = (Integer) temp;
				}
			}

			occ = sum + countOfC;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return occ;
	}

	/**
	 * Get how many character c in this block before index
	 * 
	 * @param index
	 *            : the index of c in current block
	 * @param c
	 *            : the char need to be searched
	 * @return number of c in this block before index
	 */
	public int getSumOfChar(int index, char c) {
		int sum = 0;
		for (int i = 1; i <= index; i++) {
			Object temp = BL[i].get(c);
			if (temp != null) {
				sum += (Integer) temp;
			}
		}
		return sum;
	}

	/**
	 * calculate BL using byte array ch.
	 * <p>
	 * The program read in a fixed length byte array, then get how many times
	 * each character in this block occurs by this method. 喵
	 * 
	 * @param ch
	 *            the character array used for getting BL
	 * @return the BL in HashMap
	 */
	public HashMap<Character, Integer> getBLElement(byte[] ch) {
		HashMap<Character, Integer> hm = new HashMap<Character, Integer>();
		for (int i = 0; i < ch.length; i++) {
			char temp = (char) ch[i];
			if (!hm.containsKey(temp)) {
				hm.put(temp, 1);
			} else {
				int number = hm.get(temp);
				number++;
				hm.put(temp, number);
			}

			if (!CArray.containsKey(temp)) {
				CArray.put(temp, 1);
			} else {
				int number = CArray.get(temp);
				number++;
				CArray.put(temp, number);
			}

		}
		return hm;
	}

	/**
	 * Initialize all needed data. It contains size of BL, BL array, and so on.
	 * <p>
	 * CArray also complete the initialization work in this function.
	 * 
	 * @param fileName
	 *            input file name
	 */
	public void init(String fileName) {
		this.fileName = fileName;
		File file = new File(fileName);
		long fileLength = file.length();
		l = (long) (Math.log(fileLength) / Math.log(2));
		CArray = new HashMap<Character, Integer>();
		//System.out.println("L = " + l);

		sizeOfBL = l * l * l;
		//System.out.println("Size of BL = " + sizeOfBL);
		lengthOfBL = fileLength / (sizeOfBL) + 2;
		//System.out.println("Length of BL = " + lengthOfBL);
		BL = new HashMap[(int) lengthOfBL];

		try {
			byte[] tempchars = new byte[(int) sizeOfBL];
			int charread = 0;
			reader = new RandomAccessFile(fileName, "r");
			int index = 0;
			while ((charread = reader.read(tempchars)) != -1) {

				if (charread != this.sizeOfBL) {
					String temp = new String(tempchars);
					temp = temp.substring(0, charread);
					tempchars = temp.getBytes();
				}
				index++;
				HashMap<Character, Integer> hm = this.getBLElement(tempchars);
				BL[index] = this.hashMapPlus(hm, BL[index - 1]);

			}

		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {

		}

	}

	/**
	 * Add one HashMap to another. Add value with the same key. If the key
	 * doesn't exist in target HashMap, create it in it.
	 * <p>
	 * If the parameters contain a empty HashMap, put it on h2.
	 * 
	 * @param h1
	 *            the new hashmap
	 * @param h2
	 *            the origin hashmap
	 * @return the result of new HashMap plus origin HashMap
	 */
	public HashMap<Character, Integer> hashMapPlus(
			HashMap<Character, Integer> h1, HashMap<Character, Integer> h2) {

		if (h2 != null) {
			Object[] keys = h2.keySet().toArray();
			for (int j = 0; j < keys.length; j++) {
				if (h1.containsKey(keys[j])) {
					int value = h2.get(keys[j]);
					int origin = h1.get(keys[j]);
					h1.put((Character) keys[j], value + origin);
				} else {
					int value = h2.get(keys[j]);
					h1.put((Character) keys[j], value);
				}
			}
			return h1;
		} else {
			return h1;
		}
	}

	/**
	 * Decode the bwt file from the position indicated by {@link #first} in
	 * backward. It keeps running until meet the '['.
	 * <p>
	 * Then return the string before the character pointed by {@link #first}
	 * 
	 * @param first
	 *            first position of searched String in F array
	 * @param P
	 *            the String which is searched
	 * @param C
	 *            C array - C[c]
	 * @return plain string before searched string
	 * @throws IOException
	 */
	public String backwardDecode(int first, String P,
			HashMap<Character, Integer> C) throws IOException {
		String result = "";
		int nowIndex = first;
		char temp = P.charAt(0);
		int indexInF;
		int occOfTemp = 0;

		indexInF = first;
		nowIndex = indexInF;

		reader.seek(nowIndex - 1);
		temp = (char) reader.read();
		while (temp != '[') {
			result = result + temp;
			occOfTemp = this.getOcc(temp, nowIndex);
			indexInF = C.get(temp) + occOfTemp;
			nowIndex = indexInF;
			reader.seek(nowIndex - 1);

			temp = (char) reader.read();

		}

		String strTemp = "";
		for (int i = result.length() - 1; i >= 0; i--) {

			strTemp = strTemp + result.charAt(i);
		}
		return strTemp;
	}

	/**
	 * Decode the bwt file from the position indicated by {@link #first} in
	 * forward. It keeps running until meet the '['.
	 * <p>
	 * Then return the string before the character pointed by {@link #first}
	 * 
	 * @param first
	 *            the first position of found string
	 * @param P
	 *            the key word to be searched
	 * @param C
	 *            C array in HashMap
	 * @return whole string after key word
	 * @throws IOException
	 */
	public String forwardDecode(int first, String P,
			HashMap<Character, Integer> C) throws IOException {
		String result = "";
		int nowIndex = first;
		this.chInF = 128;
		while (this.chInF != '[') {
			this.getCharInF_BinarySearch(nowIndex, C);
			result += this.chInF;
			nowIndex = this.reverseOcc(this.indexOfChInF, this.chInF);
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}

	/**
	 * Used for getting position of c when knowing occurrence times of character
	 * c. The contrary progress of normal Occ
	 * 
	 * @param index
	 *            times of occurrence of char c
	 * @param c
	 *            the char c
	 * @return the position which c occur index times
	 * @throws IOException
	 */
	public int reverseOcc(int index, char c) throws IOException {
		int num = 0;
		int indexOfBL = 0;
		if (BL != null) {
			for (int i = 1; i <= BL.length - 1; i++) {
				if (BL[i] != null) {
					if (BL[i].containsKey(c)) {
						num = BL[i].get(c);
						if (index <= num) {
							indexOfBL = i;
							break;
						}
					}
				}
			}
		}
		reader.seek((indexOfBL - 1) * this.sizeOfBL);
		byte[] buf = new byte[(int) this.sizeOfBL];
		reader.read(buf);
		int count = 0;
		if (BL[indexOfBL - 1] != null) {
			if (BL[indexOfBL - 1].containsKey(c)) {
				count = BL[indexOfBL - 1].get(c);
			}
		}
		int j = 0;
		while (count < index) {
			char temp = (char) buf[j];
			if (temp == c) {
				count++;
			}
			j++;
		}
		int result = (int) ((indexOfBL - 1) * this.sizeOfBL + j);
		return result;
	}

	/**
	 * Used for getting what character in the position of pos in F
	 * 
	 * @param pos
	 *            position in L array
	 * @param C
	 *            C Array in HashMap
	 */
	public void getCharInF(int pos, HashMap<Character, Integer> C) {
		int count;
		int i;
		Object[] keys = C.keySet().toArray();
		Arrays.sort(keys);
		for (i = 0; i < keys.length; i++) {
			count = C.get(keys[i]);
			if (count < pos) {
				continue;
			} else {
				break;
			}
		}
		this.chInF = (Character) keys[i - 1];
		this.indexOfChInF = pos - C.get(keys[i - 1]);
		// System.out.println(this.chInF+" "+this.indexOfChInF);
	}

	/**
	 * Used for getting what character in the position of pos in F.
	 * <p>
	 * Function is as same as {@link #getCharInF(int, HashMap)}, but this method
	 * uses binary search in order to improve performance.
	 * 
	 * @param pos
	 *            position in L array
	 * @param C
	 *            C Array in HashMap
	 */
	public void getCharInF_BinarySearch(int pos, HashMap<Character, Integer> C) {
		int low = 0;
		int high = C.size() - 1;
		Object[] objectKeys = C.keySet().toArray();
		Arrays.sort(objectKeys);
		int mid = (high + low) / 2;
		while (!(pos > C.get(objectKeys[mid]) && pos <= C
				.get(objectKeys[mid + 1]))) {
			if (pos <= C.get(objectKeys[mid])) {
				high = mid;
			} else {
				low = mid;
			}
			mid = (high + low) / 2;
			// System.out.println("mid = "+mid);
		}

		this.chInF = (Character) objectKeys[mid];
		this.indexOfChInF = pos - C.get(objectKeys[mid]);
		// System.out.println(this.chInF+" "+this.indexOfChInF);
	}

	public static void main(String[] args) throws IOException {

		long startTime=System.currentTimeMillis();
		
		bwtsearch bwt = new bwtsearch();

		//String fileName = "tiny.bwt";
		String fileName = args[0];
		//String[] keywords = new String[] { "Object" };
		String[] keywords = new String[args.length-1];
		for(int i=1;i<args.length;i++){
			if(args[i].charAt(0)=='"'&&args[i].charAt(args[i].length()-1)=='"'){
				keywords[i-1] = args[i].substring(1, args[i].length()-1);
			} else{
				keywords[i-1] = args[i];
			}
			
		}
		//String[] keywords = 
		bwt.init(fileName);

		HashMap C = bwt.getC();
		long begin = Runtime.getRuntime().totalMemory();
		//System.out.println("Memory " + ((double) begin / (1024 * 1024)) + "MB");
		
		int[] firsts = new int[keywords.length];
		int[] lasts = new int[keywords.length];
		int[] resultCount = new int[keywords.length];
		int min = 0;
		for (int i = 0; i < keywords.length; i++) {
			bwt.backwardSearch(keywords[i], C);
			firsts[i] = bwt.getFirst();
			lasts[i] = bwt.getLast();
			resultCount[i] = lasts[i] - firsts[i] + 1;
			if (resultCount[min] > resultCount[i]) {
				min = i;
			}
		}
		//System.out.println("The minimum result length = " + resultCount[min]);
		 String[] results = new String[resultCount[min]];
		HashMap<Integer, String> resultshm = new HashMap<Integer, String>();
		String complete = "";
		for (int i = firsts[min]; i <= lasts[min]; i++) {
			complete = "";
			try {
				complete += bwt.backwardDecode(i, keywords[min], C);
				complete += bwt.forwardDecode(i, keywords[min], C);
				int indexOfCloseBrace = complete.indexOf(']');
				int key = Integer.parseInt(complete.substring(0,
						indexOfCloseBrace));
				complete = complete.substring(indexOfCloseBrace + 1,
						complete.length());
				resultshm.put(key, complete);
				// System.out.println(key + " = " + resultshm.get(key));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		//System.out.println("result number = " + resultshm.size());
		Object[] keysOfResult = resultshm.keySet().toArray();
		for (int i = 0; i < keysOfResult.length; i++) {
			for (int j = 0; j < keywords.length; j++) {
				if (resultshm.get(keysOfResult[i]) != null) {
					if (resultshm.get(keysOfResult[i]).indexOf(keywords[j]) == -1) {
						resultshm.remove((Integer) keysOfResult[i]);
						// resultshm.put((Integer) keysOfResult[i], "");
					}
				}

			}
		}

		keysOfResult = resultshm.keySet().toArray();
		Arrays.sort(keysOfResult);
		
		for (int i = 0; i < keysOfResult.length; i++) {
			System.out.println(resultshm.get(keysOfResult[i]));
		}
		//System.out.println(resultshm.size() + " Result(s) in total");
		long end = Runtime.getRuntime().totalMemory();
		//System.out.println("Memory " + ((double) end / (1024 * 1024)) + "MB");
		long endTime=System.currentTimeMillis(); //获取结束时间  
		//System.out.println("Runtime:"+(double)(endTime-startTime)/1000+"s");   
	}
}
