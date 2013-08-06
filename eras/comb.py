#cheap, inefficient script to remove duplicate entries

def comb_adj(inf,outf):
	with open(inf,'r') as f:
		table = []
		for line in f:
			table.append(line)
			for i in range(len(table)-1):
				l = table[i].split('\t')
				l2 = table[i+1].split('\t')
				if l[0] == l2[0]:
					table[i] = '%s\t%d' % (l[0],int(l[1])+int(l2[1]))
					table[i+1] = ''
					i += 1
	with open(outf,'w') as f:
		for l in table:
			if l != '': f.write(l)

def comb(inf, outf):
	with open(inf,'r') as f:
		dict = {}
		for line in f:
			s = line.split('\t')
			if s[0] in dict: dict[s[0]] += int(s[1])
			else: dict[s[0]] = int(s[1])
	
	with open(outf,'w') as f:
		for key in dict:
			f.write('%s\t%d\n' % (key,dict[key]))

#comb('rel-counts.txt','rel-counts-total.txt')
