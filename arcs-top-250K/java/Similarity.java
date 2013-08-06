//THIS IS THE THESAURUS

import java.io.*;
import java.util.*;

public class Similarity {

	static final int DICTSIZE = 92318;
	static final int THESAURUS_SIZE = 50000;

	static String words[] = new String[300000]; // list of words, use indexOf to find order
	static double totals[] = new double[300000]; // list of total sums of entropies for quick calculations
	static int starts[] = new int[300000]; // list of all token's starting indices
	static long list[] = new long[61000000]; // a list of every line for each word, without spaces
	static String dictionary[] = new String[DICTSIZE]; // English dictionary as provided by Mathematica

	public static void init() {

		int count = 0;
		String s;

		try {
			BufferedReader in = new BufferedReader(new FileReader("../word-values.txt"));

			while ((s = in.readLine()) != null) {

				words[count] = s.trim();
				count++;

			}
			in.close();

			count = 0;
			in = new BufferedReader(new FileReader("../dict.txt"));

			while ((s = in.readLine()) != null) {
				dictionary[count] = s.toLowerCase();
				count++;
			}
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		int totalsIndex = 0, index = 0;

		System.out.print("\nReading:");
		for (int i=0; i<99; i++) {

			String n = String.format("%02d",i);
			String filename = "../entropies/entropies."+n+".txt";

			boolean first = true;

			if (i%10==0) System.out.println();
			System.out.print(String.format(" %02d /", i));

			try {
				BufferedReader reader = new BufferedReader(new FileReader(filename));

				while ((s = reader.readLine()) != null) {

					if (first) {

						if (s.equals("X")) {
							totals[totalsIndex] = 0.0;
							starts[totalsIndex] = index;
							list[index] = 0L;
						} else {
							long x = Long.parseLong(s);
							
							totals[totalsIndex] = x / 1000000.0;
							starts[totalsIndex] = index;
							list[index] = x;
						}
						
						index++;
						first = false;

					} else if (s.equals("")) {

						first = true;
						totalsIndex++;

					} else {

						list[index] = Long.parseLong(s);
						index++;

					}
				}
				reader.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("\nDatabase created. Size: " + index);
		System.out.println("Total # of words: " + totalsIndex);
	}

	public static long findIntersection(long[] arr1, long[] arr2) {

		long[] shorter,longer;

		if (arr1.length > arr2.length) {

			shorter = arr2;
			longer = arr1;

		} else {

			shorter = arr1;
			longer = arr2;

		}

		long sum = 0;

		for (int i=0; i<shorter.length; i++) {
			for (int j=0; j<longer.length; j++) {

				long a = shorter[i];
				long b = longer[j];

				if ((a >> 28) == (b >> 28)) { //shifted by 28 to give (w,r,w')
					sum += (a & 0xFFFFFFF); // this gives I(w,r,w')
					sum += (b & 0xFFFFFFF);
					break;
				}
			}
		}

		return sum;
	}

	public static void printLongArray(String word) {

		int wInd = Arrays.asList(words).indexOf(word);
		int start = starts[wInd]+1;
		int end = starts[wInd+1];

		for (int i=start; i<end; i++) {
			System.out.println(list[i]);
		}

	}

	public static long[] getLongArray(String word) {

		int wInd = Arrays.asList(words).indexOf(word);
		int start = starts[wInd]+1;
		int end = starts[wInd+1];
		long[] toReturn = new long[end-start];

		for (int i=start; i<end; i++) {
			toReturn[i-start] = list[i];
		}

		return toReturn;

	}

	public static double calcSim(String w1, String w2) {

		String w1s = w1.substring(0,w1.length()-2);
		String w2s = w2.substring(0,w2.length()-2);

		if (Arrays.binarySearch(dictionary,w1s) < 0 || Arrays.binarySearch(dictionary,w2s) < 0) return 0.0;

		List<String> l = Arrays.asList(words);

		long[] arr1 = getLongArray(w1);
		long[] arr2 = getLongArray(w2);

		double sum1 = totals[l.indexOf(w1)];
		double sum2 = totals[l.indexOf(w2)];

		long intersection = findIntersection(arr1, arr2);

		if (sum1 == 0.0 || sum2 == 0.0 || intersection == 0.0) return 0.0;

		return (intersection / (sum1+sum2) / Math.pow(10,6));

	}

	public static boolean tooSimilar(String a, String b) {
		if (a.length() < 4 || b.length() < 4) return false;
		for(int i=0; i<4; i++) {
			if (a.charAt(i) != b.charAt(i)) return false;
		}
		return true;
	}
	
	public static void main(String args[]) {

		init();

		Scanner in = new Scanner(System.in);

		while(true) {

			System.out.print("\nEnter word: ");

			String w1 = in.nextLine();
			char type = w1.charAt(w1.length()-1);
			if (type != 'f') {

				System.out.println(Arrays.asList(words).indexOf(w1)+"\n");

				double max = 0.0;
				String maxs = "";
				for(int i=1; i<296964; i++) { // '%' isn't in the dictionary, can be skipped
					String w2 = words[i];

					if (i%50000==0) System.out.println("\nTotal tested: "+i+"  (up to "+w2+", max is "+maxs
						+" with "+max+")\n");

					if (w2.charAt(w2.length()-1) != type) continue;
					if (w2.equals(w1) || tooSimilar(w1,w2)) continue;

					double z = 0.0;

					try { z = calcSim(w1, w2); } catch (Exception e) {};

					if (z > 0.1) {

						if (z > max) {
							max = z;
							maxs = w2;
						}
						String add = "";
						for (int x=0; x<14-w2.length(); x++) {add += " ";}
						//System.out.println(w1+" - "+w2+": "+z+add);
						System.out.println(w2+" - "+z);
					}
				}
				System.out.println("\nMax is "+maxs+" with "+max);
			} else { // provided an itemsets file

				String filename = w1.substring(0,w1.length()-2);
				Hashtable<Long, Double> thesaurus = new Hashtable<Long, Double>();

				try {

					BufferedReader reader = new BufferedReader(new FileReader(filename));
					String s;

					System.out.println("reading file");
					int count = 0;

					while ((s = reader.readLine()) != null) {

						count++;
						if (count % 100 == 0) System.out.println(count);
						if (count > THESAURUS_SIZE) break;

						double sim = 0.0;
						long key = Long.parseLong(s.substring(s.indexOf('\t')+1));

						String word1 = words[(int)(key & 0x7FFFF)];
						String word2 = words[(int)((key >> 32) & 0x7FFFF)];

						try { sim = calcSim(word1, word2); } catch (Exception e) {};

						thesaurus.put(key, sim);
					}
					System.out.println("\ndone\n");

				} catch (Exception e) {
					System.out.println("Invalid file name");
					e.printStackTrace();
					continue;
				}

				while(true) {
					System.out.print("\nEnter word: ");

					String w = in.nextLine();
					int index = Arrays.asList(words).indexOf(w);

					for (long key : Collections.list(thesaurus.keys())) {

						int ind1 = (int)(key & 0x7FFFF);
						int ind2 = (int)((key >> 32) & 0x7FFFF);


						if (index == ind1) {
							double sim = thesaurus.get(key);
							if (sim > 0.01) System.out.println(words[ind2]+" - "+sim);
						} else if (index == ind2) {
							double sim = thesaurus.get(key);
							if (sim > 0.01) System.out.println(words[ind1]+" - "+sim);
						}
					}
				}
			}
		}
	}
}