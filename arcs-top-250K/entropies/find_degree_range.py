import sys

try:
	degree1 = int(sys.argv[1])
	degree2 = int(sys.argv[2])
except:
	print 'Input formatting error (Usage: python find_degree_range [degree1] [degree2])'
	exit(0)

found = False

for i in range(99):
	with open('rwIndexed/degree-sorted/entropies.degree-sorted.%02d.txt'%i,'r') as f:
		count = 0;
		for line in f:
			if line == '\n':
				x = 1 if i==0 else i
				if count > degree1 and not found:
					print '%02d'%(x-1)
					found = True
				if count > degree2:
					print '%02d'%(x-1)
					exit(0)
				break
			count += 1

	if i==98: print '98'
