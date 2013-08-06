'''reads in words in alphabetical order from p-arcs.ps-final.**.txt and writes them to word-values.txt'''

fw = open('word-values.txt','w')
for i in range(99):
	print 'opening %02d'%i
	with open('p-arcs/final/p-arcs.ps-final.%02d.txt'%i,'r') as f:
		word = ''
		for line in f:
			s = line[:line.find('\t')]
			if s != word:
				word = s
				fw.write('%s\n'%s)
fw.close()
