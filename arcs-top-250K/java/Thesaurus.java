import java.io.*;
import java.util.*;

public class Thesaurus {

	static final int NUMWORDS = 296964;
	static final int ARRAYSIZE = 400;

	static long[][] data = new long[NUMWORDS][ARRAYSIZE+1]; // first element is individual sum of Is
	static String[] words = new String[NUMWORDS]; // list of words, use indexOf to find order

	public static int indexOf(double[] list, double val) {
		for(int i=0; i<list.length; i++) {
			if (list[i] == val) return i;
		}
		return -1;
	}

	public static void init(int start, int end) {

		int count = 0;
		String s;

		try {
			BufferedReader in = new BufferedReader(new FileReader("../word-values.txt"));

			while ((s = in.readLine()) != null) {

				words[count] = s.trim();
				count++;

			}
			in.close();

			System.out.print("\nReading");

			in = new BufferedReader(new FileReader("../thesaurus_data/thesaurus."+start+"-"+end+".txt"));

			boolean first = true;
			int wordInd = 0, c = 0;
			count = -2;

			while ((s = in.readLine()) != null) {

				if (first) {

					wordInd = Integer.parseInt(s);
					first = false;
					c++;
					if (c % 1500 == 0) System.out.print(".");

				} else if (s.equals("")) {

					first = true;
					count = -2;
					continue;

				} else {

					count++;

					if (count == -1) continue;

					data[wordInd][count] = Long.parseLong(s);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("\n");

	}
	
	public static void main(String args[]) {

		int start = Integer.parseInt(args[0]);
		int end = Integer.parseInt(args[1]);
		int fortyfour = (int) Math.pow(2,44)-1;

		init(start,end);

		Scanner in = new Scanner(System.in);

		while(true) {

			int numToShow = 20;
			String w1 = "";

			try {

				System.out.print("Enter word: ");
				w1 = in.nextLine();
				System.out.println("");

				int temp = w1.indexOf(' ');
				if (temp != -1) {
					numToShow = Integer.parseInt(w1.substring(temp+1));
					w1 = w1.substring(0,temp);
				}

				int w1Index = Arrays.asList(words).indexOf(w1);

				long[] w1Data = data[w1Index];
				long w1Sum = w1Data[0];

				double[] valList = new double[ARRAYSIZE];
				String[] stringList = new String[ARRAYSIZE];
				int listInd = 0;

				for(int i=1; i<ARRAYSIZE+1; i++) {

					long l = w1Data[i];

					int w2Index = (int)(l >> 44);
					long[] w2Data = data[w2Index];
					long w2Sum = w2Data[0];
					String w2 = words[w2Index];

					long totalSum = (l & fortyfour);

					double sim = totalSum * 1.0 / (w1Sum + w2Sum);

					valList[listInd] = sim;
					stringList[listInd] = w2 + " - " + sim;
					listInd++;

				}

				double[] valClone = valList.clone(); // crude way of sorting by word similarity

				Arrays.sort(valList);

				for(int i=ARRAYSIZE-1; i>=ARRAYSIZE-numToShow; i--) {

					int index = indexOf(valClone,valList[i]);
					if (index == -1) continue;
					System.out.println(stringList[index]); // theoretically could fail if two words have identical similarity

				}

			} catch (Exception e) {
				System.out.println("Word not found: "+w1);
			}
			System.out.println("");
		}
	}
}