import re
import sys

rels = {}

def parse(file):
	global rels

	with open(file,'r') as f:
		n = 0
		for line in f:
			n += 1
			if n>10000:
				print >> sys.stderr, '.', 
				sys.stderr.flush()
				n = 0
			l = re.split('[\t]',line.rstrip())
			
			if l[1].count('/') != 6: continue

			list = re.split(' |/',l[1])

			for i in range(2,len(list),4):
				if list[i] != 'ROOT' and list[i+1] != 0:
					total = 0
					for j in range(len(l)-1,3,-1):
						if l[j][:4] < '2000': break
						total += int(l[j][5:])

					if list[i] in rels: rels[list[i]] += total
					else: rels[list[i]] = total
					break
	print >> sys.stderr, 'finished %s\n' % file
	sys.stderr.flush()

def output():
	for key in rels:
		if key != '': print '%s\t%d' % (key, rels[key])

for i in range(99):
	parse('arcs/arcs.%02d-of-99.txt' % i)

output()
