def get_type(pos):
	if pos in ['NN','NNS','NNP','NNPS']: return 'N'
	if pos in ['JJ','JJR','JJS','RB','RBR','RBS']: return 'A'
	if pos in ['VB','VBD','VBG','VBN','VBP','VBZ']: return 'V'

f2 = open('final/final-indices.txt','w')

count = 0
for i in range(99):
	word = ''
	tag = ''
	print 'opening %d'%i
	with open('final/p-arcs.ps-final.%02d.txt'%i,'r') as f:
		for line in f:
			w = line[:line.find('\t')]
			#pos = get_type(line[line.find(' ')+1:line.find('\t')])
			if word != w:
				word = w
				#tag = pos
				#f2.write('%s/%s\t%d\n'%(word,tag,count+extra))
				f2.write('%s\t%d\n'%(word,count))
			count += 1

f2.close()
