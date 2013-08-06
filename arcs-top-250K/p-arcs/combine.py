for i in range(99):
	print 'opening %02d' % i
	with open('purged/p-arcs.purged-sorted.%02d.txt'%i,'r') as fr:
		fw = open('final/p-arcs.ps-final.%02d.txt'%i,'w')
		
		c = 0
		s = ''

		for line in fr:
			t1 = line.find('\t')
			t2 = line.find('\t',t1+1)
			t3 = line.find('\t',t2+1)

			if s == '': s = line[:t3]
	
			if t3 <= len(s) and s == line[:t3]:
				c += int(line[t3+1:])
			else:
				if c != 0: fw.write('%s\t%d\n' % (s,c))
				s = line[:t3]
				c = int(line[t3+1:])
