import sys

list = ["JJ","JJR","JJS","NN","NNS","NNP","NNPS","RB","RBR","RBS","VB","VBD","VBG","VBN","VBP","VBZ"]
ads = ["JJ","JJR","JJS","RB","RBR","RBS"]
nouns = ["NN","NNS","NNP","NNPS"]
verbs = ["VB","VBD","VBG","VBN","VBP","VBZ"]

def get_type(tag):
	if tag in ads: return "A"
	if tag in nouns: return "N"
	if tag in verbs: return "V"
	return "X" #should never happen

def tag(purge):
	for i in range(99):
		n = 0
		print 'opening %02d' % i
		with open('top/p-arcs.%02d.txt'%i,'r') as fr:
			fw = open('tagged/p-arcs.tagged.%02d.txt'%i,'w')
	
			for line in fr:
				
				n += 1
				if n > 25000:
					print >> sys.stderr,'.',
					sys.stderr.flush()
					n = 0

				tab1 = line.find('\t')
				tab2 = line.find('\t',tab1+1)

				w1pos = line[line.find(' ')+1:tab1]
				w2pos = line[line.find(' ',tab2)+1:line.find('\t',tab2+1)]

				if (w1pos in list and w2pos in list) or not purge:
					w1 = line[:line.find(' ')]
					rel = line[tab1+1:tab2]
					w2 = line[tab2+1:line.find(' ',tab2)]
					c = line[line.find('\t',tab2+1)+1:]

					tag1 = get_type(w1pos)
					tag2 = get_type(w2pos)

					fw.write('%s/%s\t%s\t%s/%s\t%s' % (w1,tag1,rel,w2,tag2,c))
		print

tag(False)
