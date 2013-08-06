import java.io.*;
import java.util.*;

public class CreateThesaurus {

	static final int DICTSIZE = 92318;
	static final int NUMWORDS = 907756;
	static final int ARRAYSIZE = 400;
	static final int nineteen = (int) Math.pow(2,19)-1;
	static final long fortyfour = (long) Math.pow(2,44)-1;

	static long[][] data = new long[NUMWORDS][ARRAYSIZE+2]; // first element is minimum, second is individual sum of Is
	static String[] words = new String[NUMWORDS]; // list of words, use indexOf to find order
	static String[] dictionary = new String[DICTSIZE]; // English dictionary as provided by Mathematica

	public static long getWordIndex(long l) {
		return ((l >> 19) & nineteen);
	}

	public static String getTag(String w) {
		return w.substring(w.length()-2);
	}

	public static int binary(long[] a, int lo, int hi, long key) {

		while (lo < hi) {

			int mid = (lo+hi) / 2;
			long midVal = a[mid] >> 44;
			if (midVal < key) lo = mid+1;
			else if (midVal > key) hi = mid;
			else return mid;

		}

		return -1;
	}
	
	public static void init(int startVal, int endVal) {

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

		count = 0;

		System.out.print("\nReading:");
		for (int i=startVal; i<=endVal; i++) {

			String n = String.format("%02d",i);
			String filename = "../entropies/rwIndexed/degree-sorted/entropies.degree-sorted."+n+".txt";

			System.out.print(String.format(" %02d /", i));

			try {
				BufferedReader reader = new BufferedReader(new FileReader(filename));
				ArrayList<Long> temp = new ArrayList<Long>();

				while ((s = reader.readLine()) != null) {

					if (s.equals("")) {

						addVs(temp);
						temp = new ArrayList<Long>();
						continue;

					}

					temp.add(Long.parseLong(s));

				}

				reader.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("");
	}

	public static void addVs(ArrayList<Long> temp) {

		for(int i=0; i<temp.size(); i++) {
			for(int j=i+1; j<temp.size(); j++) {

				long l1 = temp.get(i);
				long l2 = temp.get(j);

				//assert: (l1 >> 38) & nineteen == (l2 >> 38) & nineteen;

				long w1 = getWordIndex(l1);
				long count1 = l1 & nineteen;

				long w2 = getWordIndex(l2);
				long count2 = l2 & nineteen;

				String word1 = words[(int)w1];
				String word2 = words[(int)w2];

				if (!getTag(word1).equals(getTag(word2))) continue;

				long sum = count1 + count2;

				long[] arr1 = data[(int)w1]; //w1's list
				long[] arr2 = data[(int)w2]; //w2's list

				int index1 = binary(arr1, 2, arr1.length, w2); //position of w2 in w1's list
				int index2 = binary(arr2, 2, arr2.length, w1); //position of w1 in w2's list


				if (index1 > 0) { // if w2 is already in w1's list

					long total = sum + (arr1[index1] & fortyfour); // 44 bits for the count, which leaves 19 bits for the word index
					if (total > fortyfour) System.out.println("\nFATAL ERROR: SUM REACHES > 44 BITS\n");
					arr1[index1] += sum;
					arr1[1] += count1;

				} else { // it's not in the list

					long min = arr1[0];
					if (min == 0.0) {

						int minInd = Arrays.binarySearch(arr1, 2, arr1.length, min);
						arr1[minInd] = (w2 << 44) + sum;
						arr1[1] += count2;

					}
				}

				Arrays.sort(arr1, 2, arr1.length);
				arr1[0] = Long.MAX_VALUE;

				for(int c=2; c<arr1.length; c++) { // update the minimum value
					arr1[0] = (arr1[c] < arr1[0]) ? arr1[c] : arr1[0];
				}


				/* REPEATED FOR SECOND WORD */


				if (index2 > 0) { // if w1 is already in w2's list

					long total = sum + (arr2[index2] & fortyfour); // 44 bits for the count, which leaves 19 bits for the word index
					if (total > fortyfour) System.out.println("\nFATAL ERROR: SUM REACHES > 44 BITS\n");
					arr2[index2] += sum;
					arr2[1] += count1;

				} else { // it's not in the list

					long min = arr2[0];
					if (min == 0.0) {

						int minInd = Arrays.binarySearch(arr2, 2, arr2.length, min);
						arr2[minInd] = (w1 << 44) + sum;
						arr2[1] += count2;

					}
				}

				Arrays.sort(arr2, 2, arr2.length);
				arr2[0] = Long.MAX_VALUE;

				for(int c=2; c<arr2.length; c++) { // update the minimum value
					arr2[0] = (arr2[c] < arr2[0]) ? arr2[c] : arr2[0];
				}
					
			}
		}
	}

	public static void write(int start, int end) {

		System.out.println("Writing...");

		try {

			BufferedWriter out = new BufferedWriter(new FileWriter("../thesaurus_data/thesaurus."+start+"-"+end+".txt"));

			for(int i=0; i<NUMWORDS; i++) {

				boolean skip = false;

				long[] arr = data[i];
				for(int c=2; c<arr.length; c++){
					if (arr[c] != 0) break;
					if (c == arr.length-1) skip = true;
				}

				if (skip) continue;

				out.write(i+"\n");

				for(long l : arr) {

					out.write(l+"\n");
				}
				out.write("\n");
			}
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {

		int start = Integer.parseInt(args[0]);
		int end = Integer.parseInt(args[1]);

		init(start, end);
		write(start, end);
	}
}