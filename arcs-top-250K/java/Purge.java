import java.io.*;
import java.util.*;

public class Purge {
	
	public static void main(String args[]) {

		String arg = args[0]; // bleh

		for(int i=0;;i++) {

			Hashtable<Long, Integer> itemsets = new Hashtable<Long, Integer>();
		
			int count = 0;
			String n = String.format("%03d", i);

			try {

				String s;

				BufferedReader in = new BufferedReader(new FileReader("../../../../mnt/SPACE/elan/pair-counts/"+
																			arg+"/pair-counts-"+n+".txt"));

				System.out.print("\nreading "+arg+"/"+n);

				while ((s = in.readLine()) != null) {

					count++;
					if (count % 1000000 == 0) System.out.print(".");

					int tab = s.indexOf('\t');

					long key = Long.parseLong(s.substring(0,tab));
					int val = Integer.parseInt(s.substring(tab+1));

					if (itemsets.containsKey(key)) val += itemsets.get(key);

					itemsets.put(key,val);

				}
				in.close();
				if (i<0) break;

			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

			try {
				System.out.println("\nwriting "+arg+"/"+n);

				BufferedWriter out = new BufferedWriter(new FileWriter("../../../../mnt/SPACE/elan/pair-counts/"+
																			arg+"/pair-counts-"+n+".txt"));

				ArrayList<Long> keys = Collections.list(itemsets.keys());

				for(long key : keys) {
					Integer val = itemsets.get(key);
					if (val != null && val >= 25) out.write(key+"\t"+val+"\n");
				}
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}