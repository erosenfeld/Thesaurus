import re
import sys

'''simple counter of certain occurrences for statistical analysis
	purposes'''

total = two = three = 0
deps = {}

def output():
	print '\ntotal = %d\ntwo   = %d\nthree = %d\n' % (total,two,three)
	print 'among those with three, here are the occurrences of dependency types:'
	for key in deps:
		print key,':',deps[key]

for i in range(0,15):
	print '\nopening %02d' % i
	with open('arcs.top.%02d.txt'%i,'r') as f:
		i = 0
		for line in f:
			i += 1
			if i > 10000:
				print >> sys.stderr,'.',
				sys.stderr.flush()
				i = 0
			total += 1
			num = line.find('\t')
			num2 = line.find('\t',num+1)
			list = re.split(' ',line[num+1:num2])
			
			if len(list) == 2: two += 1
			elif len(list) == 3:
				three += 1
				for entry in list:
					l = entry.split('/')
					if l[-1] == '1':
						key = l[-2]
						if key in deps: deps[key] += 1
						else: deps[key] = 1
	output()
