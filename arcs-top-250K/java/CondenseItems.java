import java.io.*;
import java.util.*;

public class CondenseItems {

	static Hashtable<Long, Integer> hash = new Hashtable<Long, Integer>(); //condensed list of itemsets

	public static void main(String args[]) {

		try {

			String i = args[0];

			BufferedWriter out = new BufferedWriter(new FileWriter("../../../../mnt/SPACE/elan/pair-counts/"+i+"/"+
																"pair-counts.infrequent.condensed.txt"));

			for(int j=0; ; j++) {

				String n = String.format("%03d",j);
				String s;

				try {

					BufferedReader in = new BufferedReader(new FileReader("../../../../mnt/SPACE/elan/pair-counts/"+i+
																	"/pair-counts.infrequent-"+n+".txt"));

					System.out.print("reading "+i+"/"+n);
					int count = 0;

					while ((s = in.readLine()) != null) {

						count++;
						if (count % 250000 == 0) System.out.print(".");

						int tab = s.indexOf('\t');
						long key = Long.parseLong(s.substring(0,tab));
						int val = Integer.parseInt(s.substring(tab+1));

						if (hash.containsKey(key)) val += hash.get(key);

						hash.put(key, val);
					}
					in.close();
					System.out.println("");
				} catch (Exception e) {
					break;
				}

				if (j<0) break;
			}

			System.out.println("\nwriting "+i+"/***\n");

			ArrayList<Long> keys = Collections.list(hash.keys());

			for(long key : keys) {

				Integer val = hash.get(key);
				if (val != null) out.write(key+"\t"+val+"\n");

			}
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("done (size = "+hash.size()+")\n");
	}	
}