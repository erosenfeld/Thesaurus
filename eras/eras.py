import re
import sys

w = ''
counts = []
total = []

def parse(file):

	word = ''
	c = 0

	with open(file,'r') as f:

		print 'opening %s...' % file
		
		for line in f:
			c += 1
			if (c>10000):
				print >> sys.stderr, '.',
				sys.stderr.flush()
				c = 0
			tab = line.find('\t')

			word = line[:tab]
			tab = line.find('\t', tab+1)
			tab = line.find('\t', tab+1)

			data = re.split('[\t]|,',line[tab+1:].rstrip())

			inc(word, count(data))

def count(data):
	d0 = d1 = d2 = d3 = 0

	for i in range(0, len(data), 2):
		s = data[i]
		if s < '1920': d0 += int(data[i+1])
		elif s < '1950': d1 += int(data[i+1])
		elif s < '2000': d2 += int(data[i+1])
		else: d3 += int(data[i+1])

	return [d0,d1,d2,d3]

def inc(word, data):
	
	global w
	global counts

	if word != w:
		if w != '': total.append((w,data))
		w = word
		counts = [0,0,0,0]
	
	counts = map(lambda x,y: x+y, counts, data)

def write(file):
	
	with open(file,'w') as f:

		for (w,(d0,d1,d2,d3)) in total:
			f.write('%s %d %d %d %d %d\n' % (w,(d0+d1+d2+d3),d0,d1,d2,d3))

for i in range(0,99):
	parse('arcs.%02d-of-99.txt' % i)
	print 'writing to file'
	write('eras.%02d.txt' % i)
