import re
import sys

'''reads through the top 250000 words' data and prints out a parsed version
	in the form of (w, w POS, relation, w', w' POS, count)'''

for i in range(99):
	with open('../arcs/arcs.top.%02d.txt'%i,'r') as f:
		f2 = open('p-arcs/p-arcs.top.%02d.txt'%i,'w')
		print 'opening %02d' % i
		i = 0
		for line in f:
			i += 1
			if i > 10000:
				print >> sys.stderr,'.',
				sys.stderr.flush()
				i = 0
			x = line.find('\t')
			w0 = line[:x]
			w0pos = ''
			w1 = ''
			w1pos = ''
			rel = ''
			count = 0
			if '.' in w0 or '/' in w0 or '\\' in w0 or ',' in w0:
				continue
			x2 = line.find('\t',x+1)
			list = re.split(' ',line[x+1:x2])
			length = len(list)
			for entry in list:
				l = entry.split('/')
				if l[3] == '0':
					w0pos = l[1]
				else:
					if length == 3 and (l[2] == 'cc' or l[2] == 'prep'):
						continue
					w1 = l[0]
					w1pos = l[1]
					rel = l[2]
			l = re.split('\t',line[line.find('\t',x2+1)+1:].rstrip())
			for j in range(len(l)-1,0,-1):
				yn = l[j]
				if yn[:4] < 1950: break
				count += int(yn[5:])
			f2.write('%s %s\t%s\t%s %s\t%d\n'%(w0,w0pos,rel,w1,w1pos,count))
		f2.close()
		print 'finished\n'
