rels = {}

for i in range(99):
	print 'opening %02d'%i
	with open('final/p-arcs.ps-final.%02d.txt'%i,'r') as f:
		for line in f:
			t1 = line.find('\t')
			t2 = line.find('\t',t1+1)
			t3 = line.find('\t',t2+1)

			rel = line[t1+1:t2]
			if rel in rels: rels[rel] += int(line[t3:])
			else: rels[rel] = int(line[t3:])

with open('relcounts.txt','w') as f:
	for key in sorted(rels.iterkeys()):
		f.write('%s\t%d\n'%(key,rels[key]))
