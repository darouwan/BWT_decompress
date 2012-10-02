import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import junit.framework.TestCase;

public class Test extends TestCase {
	public void test1() throws IOException {
		long begin = Runtime.getRuntime().totalMemory();
		System.out.println("Memory " + ((double) begin / (1024 * 1024)) + "MB");
		bwtsearch bwt = new bwtsearch();

		String fileName = "dblp300k.bwt";

		// int first = 0;
		// bwt.Occ(fileName, '[', 7289400);dblp300k
		bwt.init(fileName);

		HashMap C = bwt.getC();
		begin = Runtime.getRuntime().totalMemory();
		System.out.println("Memory " + ((double) begin / (1024 * 1024)) + "MB");
		String[] keywords = new String[] {"Frank"};
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
		System.out.println("The minimum result length = " + resultCount[min]);
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
		System.out.println("result number = " + resultshm.size());
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
		System.out.println(resultshm.size() + " Result(s) in total");
		long end = Runtime.getRuntime().totalMemory();
		System.out.println("Memory " + ((double) end / (1024 * 1024)) + "MB");
	}
}
