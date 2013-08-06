'''formats the dictionary imported from Mathematica as dictionary.txt, and writes it to dict.txt'''

import re

with open("dictionary.txt","r") as fread:
	fwrite = open("dict.txt","w")
	first = True
	
	for line in fread:
		line = line.replace('\"','').strip().replace('{','').replace('}','').replace(',','')
		
		list = re.split(' ',line)

		for entry in list:
			if '[' not in entry: fwrite.write('%s\n' % entry)

	fwrite.close()
