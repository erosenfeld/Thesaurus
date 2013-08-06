import java.io.*;
import java.util.*;

public class NodeStatistics {

	static final int DICTSIZE = 92318;
	static final int NUMWORDS = 296964;
	static final int THESAURUS_SIZE = 50000;

	static String words[] = new String[NUMWORDS]; // list of words, use indexOf to find order
	static String dictionary[] = new String[DICTSIZE]; // English dictionary as provided by Mathematica
	static double totals[] = new double[NUMWORDS]; // list of total sums of entropies for quick calculations
	static int starts[] = new int[NUMWORDS]; // list of all token's starting indices
	static long list[] = new long[61000000]; // a list of every line for each word, without spaces (actual size = 60986966)
	static long rw2List[] = new long[61000000]; // a list of every line without sums or empties
	static long avgLargest = 0, avgSmallest = 0;
	
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

		int totalsIndex = 0, index = 0, rwIndex = 0;

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
							rw2List[rwIndex] = x;
							rwIndex++;
						}
						
						index++;
						first = false;

					} else if (s.equals("")) {

						first = true;
						totalsIndex++;

					} else {

						long x = Long.parseLong(s);
						list[index] = x;
						rw2List[rwIndex] = x;
						index++;
						rwIndex++;

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

	public static void main(String args[]) {

		init();

		int[] w1List = new int[NUMWORDS-1];
		int[] tempList = new int[60236362];
		int count = 0, tempCount = 0;

		for(int i=1; i<NUMWORDS; i++) {
			w1List[count] = starts[i]-starts[i-1]-2;
			count++;
		}

		System.out.println("sorting...");


		Arrays.sort(rw2List);

		System.out.println("writing...");

		try {

			long temp = rw2List[0];

			long prev = temp << 28; // bit-shift to just the relation and word
			long small = temp & 0xFFFFFFF;
			long large = small;

			count = 1;

			for(int i=61000000-1; i>=0; i--) {

				long test = rw2List[i];
				if (test == 0) continue;

				long next = test >> 28;

				if (next == prev) {
					count++;

					temp = test & 0xFFFFFFF;
					if (temp > large) large = temp;
					if (temp < small) small = temp;

					continue;
				}

				tempList[tempCount] = count;
				tempCount++;
				count = 1;
				prev = next;

				avgSmallest += small;
				avgLargest += large;

				small = next & 0xFFFFFFF;
				large = small;

			}

			avgSmallest /= (tempCount*1000000.0);
			avgLargest /= (tempCount*1000000.0);

			System.out.println("avgSmallest: "+avgSmallest/1000000.0+"    avgLargest: "+avgLargest);

			Arrays.sort(tempList);
			Arrays.sort(w1List);

			BufferedWriter out = new BufferedWriter(new FileWriter("../../Mathematica/rw2_degree_distributions.txt"));

			for(int i=60236362-1; i>=0; i--) {
				int x = tempList[i];
				if (x>0) out.write(tempList[i]+"\n"); // 60236362 total
			}
			out.close();

			out = new BufferedWriter(new FileWriter("../../Mathematica/w1_degree_distributions.txt"));

			for(int i=NUMWORDS-2; i>=0; i--) {
				int x = w1List[i];
				if (x>0) out.write(x+"\n");
			}
			
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}