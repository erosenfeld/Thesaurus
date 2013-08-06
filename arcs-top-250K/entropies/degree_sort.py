import sys, math

word_vals = []
dict = []

with open('../../Thesaurus/arcs-top-250K/dict.txt','r') as f:
	for line in f:
		dict.append(line.lower().rstrip())

with open('../../Thesaurus/arcs-top-250K/word-values.txt','r') as f:
	for line in f:
		word_vals.append(line.rstrip())


def rwIndex():
	
	list = []

	w1_index = 0
	first = True
	remove = False

	print 'Reading:',
	for i in range(99):
		with open('entropies.%02d.txt'%i,'r') as f:

			if i%10==0: print ''
			print ' %02d /'%i,
			sys.stdout.flush()
		
			for s in f:
	
				if first:
					remove = word_vals[w1_index][:-2] not in dict
					if s=='X\n': remove = True
					first = False

				if s=='\n':
					first = True
					w1_index += 1
					continue

				if not remove:
					test = 2**19-1
					l = long(s)

					r_index = l >> 57	#r is 6 bits
					w2_index = (l >> 28) & 0x1FFFFFFF	#index is 19 bits
					count = l & 0xFFFFFFF	#count is 19 bits

					count /= 1000

					if w1_index > test: print 'w1 > 19 bits'
					if w2_index > test: print 'w2 > 19 bits'
					if count > test: print 'count > 19 bits'

					val = (r_index<<57) + (w2_index<<38) + (w1_index<<19) + count
					list.append(val)
	
	list.sort()
	print ''

	binsize = len(list) / 99.0
	cur = list[0]
	curNum = 1;

	for i in range(99):
		with open('rwIndexed/entropies.rwIndexed.%02d.txt'%i,'w') as f:
			
			print 'writing %02d'%i,
			sys.stdout.flush()

			f.write('%d\n'%cur)

			while(True):

				if curNum%100000==0:
					print '.',
					sys.stdout.flush()
				
				if curNum >= len(list): break
				l = list[curNum]
				curNum += 1

				if (l>>38) == (cur>>38):
					f.write('%d\n'%l)

				else:
					cur = l
					if curNum > (i+1)*binsize: break
					f.write('\n%d\n'%l)
		print ''

def binary(a, x, lo=0, hi=None):

	if hi is None:
		hi = len(a)
	while lo < hi:
		mid = (lo+hi)//2
		midval = a[mid]
		if midval < x:
			lo = mid+1
		elif midval > x:
			hi = mid
		else:
			return mid
	return -1

def remove_non_alpha():

	print 'Parsing:',
	sys.stdout.flush()

	for i in range(99):

		start = True

		with open('rwIndexed/entropies.rwIndexed.%02d.txt'%i,'r') as f:
	
			if i%10==0: print ''
			print ' %02d /'%i,
			sys.stdout.flush()
			
			out = open('rwIndexed/entropies.alpha.%02d.txt'%i,'w')

			temp_list = []
			
			for line in f:
				
				if line == '\n':
					if not start:
						start = True
						for e in temp_list:
							out.write('%s'%e)
						out.write('\n')
						temp_list = []
				else:
					l = long(line)
					w1 = (l >> 19) & 0x7FFFF
					w2 = (l >> 38) & 0x7FFFF

					word1 = word_vals[w1][:-2]
					word2 = word_vals[w2][:-2]

					if binary(dict, word1) > -1 and binary(dict, word2) > -1:
						temp_list.append(line)
						start = False

			out.close()

def degree_sort():
	
	list = []
	total = 0
	word_count = 0

	print 'Reading:',
	sys.stdout.flush()

	for i in range(99):
		with open('rwIndexed/entropies.alpha.%02d.txt'%i,'r') as f:
			
			if i%10==0: print ''
			print ' %02d /'%i,
			sys.stdout.flush()

			count = 0
			temp = []
			
			for s in f:

				if s == '\n':
					list.append((count,temp))
					count = 0
					word_count += 1
					temp = []
					continue

				l = long(s)
				temp.append(l)
				count += 1
				total += 1

	list.sort()
	print '\nword_count = %d\ntotal = %d'%(word_count,total)

	binsize = total / 99.0
	count = 0
	index = 0

	for i in range(99):
		with open('rwIndexed/degree-sorted/entropies.degree-sorted.%02d.txt'%i,'w') as f:
			
			print 'writing %02d'%i,
			sys.stdout.flush()

			temp = index
			
			for (num, vals) in list[temp:]:
				count += num
				index += 1

				if index % 250 == 0:
					print '.',
					sys.stdout.flush()

				for val in vals:
					f.write('%d\n'%val)

				if count > (i+1)*binsize: break
				else: f.write('\n')
		print ''

rwIndex()
remove_non_alpha()
degree_sort()
