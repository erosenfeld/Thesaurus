'''removes all occurrences of annoying characters (i.e. '.', '\', '/', etc)'''

indices = {}
rels = {}

with open('total_word_counts.txt','r') as f:
	count = 0
	for line in f:
		i = line.find(' ')
		count += int(line[i+1:])
		indices[line[:i]] = count

with open('../rel-counts-total.txt','r') as f:
	count = 0
	for line in f:
		rels[line[:line.find('\t')]] = count
		count += 1
	rels['CD'] = count
	rels['JJ'] = count+1


for i in range(99):
	print 'opening %d'%i
	with open('p-arcs/p-arcs.top.%02d.txt'%i,'r') as f:
		f2 = open('p-arcs/p-arcs.%02d.txt'%i,'w')
		for line in f:
			
			t1 = line.find('\t')
			t2 = line.find('\t',t1+1)
			s = line.find(' ',t2+1)
			r = line[t1+1:t2]
			w = line[t2+1:s]

			if w in indices and r in rels:
				if not ('.' in w or '/' in w or '\\' in w or ',' in w):
					f2.write(line)
		f2.close()
