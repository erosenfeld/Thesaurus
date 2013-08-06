import java.io.*;
import java.util.*;

public class Itemsets {

	static final int COUNTSIZE = 10;
	static final int COUNTONES = (int)Math.pow(2,COUNTSIZE)-1;
	static final int OLDNUMWORDS = 296964;
	static final int GROUPSIZE = 29697;
	static final int NUMWORDS = 135057;
	static final int DICTSIZE = 92318;
	static final int MIN_FREQ = 25;
	
	static String words[] = new String[OLDNUMWORDS]; // list of words, use indexOf to find order
	static Hashtable itemsets[] = new Hashtable[OLDNUMWORDS]; //condensed list of itemsets
	static String rels[] = new String[38]; 	// list of relations
	static String dictionary[] = new String[DICTSIZE]; // English dictionary as provided by Mathematica
	static int dictIndices[] = new int[NUMWORDS]; // records the indices in words[] that are 
											// contained in dictionary[]

	@SuppressWarnings("unchecked")
	public static void init() {

		int count = 0;
		String s;

		try {

			BufferedReader in = new BufferedReader(new FileReader("../dict.txt"));

			while ((s = in.readLine()) != null) {
				dictionary[count] = s.toLowerCase();
				count++;
			}
			in.close();


			in = new BufferedReader(new FileReader("../word-values.txt"));
			count = 0;
			int other = 0;

			while ((s = in.readLine()) != null) {

				s = s.trim();

				if (Arrays.binarySearch(dictionary,s.substring(0,s.length()-2)) >= 0) {
					dictIndices[other] = count;
					other++;
				}

				words[count] = s;
				count++;

			}
			in.close();

			in = new BufferedReader(new FileReader("../p-arcs/relcounts.txt"));
			count = 0;

			while ((s = in.readLine()) != null) {
				rels[count] = s.substring(0,s.indexOf('\t'));
				count++;
			}
			in.close();

			for(int i=0; i<OLDNUMWORDS; i++) {
				itemsets[i] = new Hashtable<Integer, Integer>();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		int index = 0;


		System.out.print("\nReading");
		for (int i=0; i<99; i++) {

			String n = String.format("%02d",i);
			String filename = "../long-arcs/final/long-arcs.purged."+n+".txt";

			if (i%10==0) System.out.println();
			System.out.print(String.format(" %02d /", i));

			boolean inDict = false;

			try {
				BufferedReader reader = new BufferedReader(new FileReader(filename));

				while ((s = reader.readLine()) != null) {

					if (s.equals("")) {

						index++;
						inDict = Arrays.binarySearch(dictIndices, index) >= 0;

					} else if (!s.equals("X")) {

						if (inDict) {

							int ind = ((int)(Long.parseLong(s) >> 28) & 0x1FFFFFFF);
							if (Arrays.binarySearch(dictIndices,ind) < 0) continue;

							Hashtable<Integer, Integer> hash = itemsets[ind];
							if (hash.containsKey(index)) {
								hash.put(index, hash.get(index)+1);
							} else {
								hash.put(index, 1);
							}
						}
					}
				}
				reader.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
			index++;
		}
		System.out.println("\nDatabase created.\n");
	}

	public static void printArray(Hashtable<Integer, Integer> p) {
		if (p.size() == 0) return;
		System.out.print("{");

		ArrayList<Integer> list = Collections.list(p.keys());
		for(int i=0; i<list.size(); i++) {

			int key = list.get(i);
			int x = 0;

			try {
				x = p.get(key);
			} catch (Exception e) {}

			for(int j=0; j<x; j++) {
				System.out.print(words[key]+",");
			}
		}
		System.out.println("}");
	}

	@SuppressWarnings("unchecked")
	public static int populate(int start, int end, int fileNum, int extension, BufferedWriter out) {

		Hashtable<Long, Integer> pairCounts = new Hashtable<Long, Integer>(); // counts of pairs
		int total = 0; // keep track of scale of memory used

		for(int index=start; index<end; index++) {

			if (Arrays.binarySearch(dictIndices, index) < 0) {
				//System.out.println("skipping "+words[index]);
				continue;
			}

			/* Skipping goes here */

			System.out.print("populating "+words[index]);
			Hashtable<Integer, Integer> a = itemsets[index];
			ArrayList<Integer> list = Collections.list(a.keys());

			int num = 0;

			for(int i=0; i<list.size(); i++) {

				int k = list.get(i);

				int c1;
				try {
					c1 = a.get(k);
				} catch (Exception e) {
					continue;
				}

				if (i%100 == 0) {
					num++;
					System.out.print(".");
					if (num >= 80) {
						try { out.write(words[index]+"\n"); } catch (Exception e) {e.printStackTrace();}

						write(pairCounts, fileNum, extension);
						return index+1;
					}
				}

				for(int j=i+1; j<list.size(); j++) {

					int k2 = list.get(j);

					int c2;
					try {
						c2 = a.get(k2);
					} catch (Exception e) {
						continue;
					}

					long key = ((long) k * (long) Math.pow(2,32)) + k2;

					int val = c1 * c2;
					if (pairCounts.containsKey(key)) val += pairCounts.get(key);
					
					if (val > 0) pairCounts.put(key, val);
				}
			}
			System.out.print("\t("+num+")\n");

			total += Math.pow(list.size(),2);

			if (total > (int)Math.pow(2,28)) {
				write(pairCounts, fileNum, extension);
				return index+1;
			}
		}
		System.out.print("(final) ");
		write(pairCounts, fileNum, extension);
		return -1; // signaling it's done
	}

	public static void write(Hashtable<Long, Integer> pairCounts, int fileNum, int extension) {

		try {

			String n = String.format("%03d",fileNum);
			System.out.println("\nwriting "+extension+"/"+n+"\n");

			BufferedWriter writer = new BufferedWriter(new FileWriter("../../../mnt/SPACE/elan/pair-counts/"+
															extension+"/pair-counts-"+n+".txt"));
			ArrayList<Long> keys = Collections.list(pairCounts.keys());

			for(long key : keys) {
				Integer val = pairCounts.get(key);
				if (val != null && val >= MIN_FREQ) writer.write(key+"\t"+val+"\n");
			}
			writer.close();

		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Error with writing");
			System.exit(0);
		}
	}

	public static void main(String args[]) {

		int x = Integer.parseInt(args[0]); // bleh

		init();

		int start = x*GROUPSIZE;
		int end = (x == 9) ? 296964 : ((x+1)*GROUPSIZE);

		int list[] = {0,0,0,0,0,0,0,0,0,0}; // to pick up where was left off in case of interruption
		int fileNum = list[x];

		BufferedWriter out = null;

		try {
			out = new BufferedWriter(new FileWriter("../skipped-"+x+".txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (start != -1) {

			start = populate(start,end,fileNum,x,out);
			try { out.flush(); } catch (Exception e) { e.printStackTrace();}
			fileNum++;
			System.gc();

		}

		try {out.close();} catch (Exception e) { e.printStackTrace();};
		System.out.println("done");
	}
}