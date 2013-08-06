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

			double threshold = 0.0;
			boolean set = false;
			String w1 = "";

			try {

				System.out.print("Enter word: ");
				w1 = in.nextLine();
				System.out.println("");

				int temp = w1.indexOf(' ');
				if (temp != -1) {
					set = true;
					threshold = Double.parseDouble(w1.substring(temp+1));
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

					char type = w2.charAt(w2.length()-1);
					int numFiles = end-start;

					/*

						calculate threshold using polynomial regression based on numFiles

							N:
								0 - 0.0018    UPDATE THIS
								1 - 0.0018
								20 - 0.003
								98 - 0.1

							V/A:
								0 - 0.0027
								1 - 0.0027
								20 - 0.0031
								98 - 0.15
					*/

					if (!set) {
						if (type == 'N') {
							threshold = 1.8e-003 - 1.3188e-006*numFiles + 1.22686e-006*Math.pow(numFiles,2) + 9.1954e-008*Math.pow(numFiles,3);
						} else {
							threshold = 2.7e-003 + 2.651e-006*numFiles - 2.8358e-006*Math.pow(numFiles,2) + 1.8516e-007*Math.pow(numFiles,3);
						}
					}

					if (sim > threshold) {
						valList[listInd] = sim;
						stringList[listInd] = w2 + " - " + sim;
						listInd++;
					}

				}

				double[] valClone = valList.clone(); // crude way of sorting by word similarity

				Arrays.sort(valList);

				for(int i=ARRAYSIZE-1; i>=ARRAYSIZE-listInd; i--) {

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