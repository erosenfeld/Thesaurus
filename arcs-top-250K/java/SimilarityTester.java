import java.io.*;
import java.util.*;

public class SimilarityTester {

	static String words[] = new String[425000]; // list of words, use indexOf to find order
	static double totals[] = new double[425000]; // list of total sums of entropies for quick calculations
	static int starts[] = new int[425000]; // list of all token's starting indices
	static long list[] = new long[300000000]; // a list of every line for each word, without spaces

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

				if ((a >> 28) == (b >> 28)) {
					sum += (a & 0xFFFFFFF);
					sum += (b & 0xFFFFFFF);
					break;
				}
			}
		}

		return sum;
	}

	public static void printLongArray(String word) {

		int wInd = Arrays.asList(words).indexOf(word);
		System.out.println("wInd = "+wInd);
		int start = starts[wInd]+1;
		int end = starts[wInd+1];

		//System.out.println(word+" starts at "+start+" ("+list[start]+") and ends at "+end+" ("+list[end]+")");

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

		List<String> l = Arrays.asList(words);

		long[] arr1 = getLongArray(w1);
		long[] arr2 = getLongArray(w2);

		double sum1 = totals[l.indexOf(w1)];
		double sum2 = totals[l.indexOf(w2)];

		long intersection = findIntersection(arr1, arr2);

		//System.out.println("sum1 = "+sum1+" sum2 = "+sum2+" intersection = "+intersection);

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
		
		while (true) {
			System.out.print("\nEnter [word1] [word2]: ");
			//System.out.print("\nEnter word: ");
			String input = in.nextLine();

			try {

				String s[] = input.split(" ");
				if (s[0].equalsIgnoreCase("print")) {
					printLongArray(s[1]);
					continue;
				}
				System.out.println(calcSim(s[0],s[1]));
				//printLongArray(input);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}