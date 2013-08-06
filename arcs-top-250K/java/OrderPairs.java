import java.io.*;
import java.util.*;

public class OrderPairs implements Comparator<Integer> {

	static final int HASHSIZE = 38889946;
	static final int NUMWORDS = 296964;

	static long pairs[] = new long[HASHSIZE];
	static Integer frequencies[] = new Integer[HASHSIZE];
	static String words[] = new String[NUMWORDS]; // list of words, use indexOf to find order

	public OrderPairs() {
	}

	public Integer[] createArray() {

		Integer[] indices = new Integer[HASHSIZE];
		for(int i=0; i<HASHSIZE; i++) {
			indices[i] = i;
		}
		return indices;

	}

	@Override
	public int compare(Integer i1, Integer i2) {

		return frequencies[i1].compareTo(frequencies[i2]);

	}

	public static void main(String args[]) {

		OrderPairs comparator = new OrderPairs();
		Integer[] indices = comparator.createArray();

		int index = 0, count = 0;
		String s;

		try {

			BufferedReader in = new BufferedReader(new FileReader("../word-values.txt"));

			while ((s = in.readLine()) != null) {

				s = s.trim();

				words[count] = s;
				count++;

			}
			in.close();

			System.out.print("reading");
			count = 0;

			in = new BufferedReader(new FileReader("../pair-counts.condensed.txt"));
			boolean meh = true;

			while ((s = in.readLine()) != null) {

				count++;
				if (count % 300000 == 0) System.out.print(".");

				try {
					int tab = s.indexOf('\t');

					pairs[index] = Long.parseLong(s.substring(0,tab));
					frequencies[index] = Integer.parseInt(s.substring(tab+1));

					index++;

				} catch (Exception e) {
					//e.printStackTrace();
					break;
				}
			}
			in.close();
			System.out.println("\n");

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error with reading");
			System.exit(1);
		}

		System.out.println("sorting...");
		Arrays.sort(indices, comparator);
		System.out.print("done\n\nwriting");

		try {

			count = 0;

			BufferedWriter out = new BufferedWriter(new FileWriter("../itemsets-frequencies.txt"));
			List list = Arrays.asList(words);

			for (int i : indices) {

				count++;
				if (count % 300000 == 0) System.out.print(".");

				long key = pairs[i];
				int val = frequencies[i];

				String w1 = words[(int)(key & 0x7FFFF)];
				String w2 = words[(int)((key >> 32) & 0x7FFFF)];

				if (w1.substring(w1.length()-2).equals(w2.substring(w2.length()-2))) {

					out.write(val+"\t"+key+"\n");
				}
			}
			out.close();
			System.out.println("\ndone");

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error with writing");
		}
	}
}