import sys

temp = []
table = []

for i in range(99):
	c = 0
	print 'opening %02d' % i
	with open('purged/p-arcs.f-purged.%02d.txt'%i,'r') as fr:
		word = ''
		for line in fr:
			c += 1
			if c > 25000:
				print >> sys.stderr,'.',
				sys.stderr.flush()
				c = 0
			test = line[:line.find('/')]
			if word == test:
				temp.append(line)
			else:
				word = test
				temp.sort()
				for entry in temp:
					table.append(entry)
				temp = []
		temp.sort()
		for entry in temp:
			table.append(entry)

	print 'writing to %02d'%i
	fw = open('purged/p-arcs.purged-sorted.%02d.txt'%i,'w')
	for entry in table:
		fw.write(entry)
	fw.close()
	print
	temp = []
	table = []
