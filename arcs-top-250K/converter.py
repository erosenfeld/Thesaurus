'''reads in p-arcs and converts to longs with bit shifting'''

indices = {}
rels = {}

with open('word-values.txt','r') as f:
	count = 0
	for line in f:
		indices[line.rstrip()] = count
		count += 1

with open('p-arcs/relcounts.txt','r') as f:
	count = 0
	for line in f:
		rels[line[:line.find('\t')]] = count
		count += 1

def convert(r,w,c):
	global indices
	global rels
	
	if w not in indices or r not in rels: return 'X\n'
	#							rel is 	6 bits
	rel = rels[r]*(2**57) #		word is 29 bits
	word = indices[w]*(2**28) #	c is 	28 bits

	return '%d\n'%(rel + word + c)

def write_longs():
	for i in range(99):
		cur_word = ''
		print 'opening %d' % i
		with open('p-arcs/final/p-arcs.ps-final.%02d.txt'%i,'r') as f:
			longs = open('long-arcs/final/long-arcs.purged.%02d.txt'%i,'w')
			for line in f:
				
				t1 = line.find('\t')
				t2 = line.find('\t',t1+1)
				t3 = line.find('\t',t2+1)
			
				w1 = line[:t1]
				w2 = line[t2+1:t3]

				if cur_word != w1:
					if cur_word != '': longs.write('\n')
					cur_word = w1

				r = line[t1+1:t2]
				c = int(line[t3+1:])
				
				longs.write(convert(r,w2,c))
			longs.close()

write_longs()
