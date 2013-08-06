import time

dict = [[]]
parsed = 0

def init(num_docs):
	global parsed
	global dict

	dict = [[]]

	try: parse_file()
	except: pass
	
	populate(num_docs)

	condense()
	write_to_file()
	
	#print_dict()

def graph_string():
	with open('graph_string.txt','w') as f:
		for list in dict:
			for (a,b) in list:
				for i in range(b):
					f.write('%s ' % a)

def parse_file():
	global parsed

	index = 0
	with open('total_word_counts.txt','r') as f:
	
		for line in f:
			if line == '\n':
				index += 1
				dict.append([])
				continue
			array = line.split(' ')	
			dict[index].append((array[0],int(array[1])))

	parsed = index

def print_dict():
	for i in dict:
		for (a,b) in i:
			print a,b

def populate(x):
	global parsed

	init_time = time.time()
	for i in range(x):
		if parsed > i: continue
		print 'parsing document %02d...' % i
		t = time.time()

		with open('arcs/arcs.%02d-of-99.txt' % i,'r') as f:
			for line in f:
				l = line.split('\t')
				if dict[i] != []:
					(a,b) = dict[i][-1]
					if a == l[0]: dict[i][-1] = (a,b+1)
					else: dict[i].append((l[0],1))
				else: dict[i] = [(l[0],1)]

		t2 = time.time()
		print 'population time: %fs (total: %fs)' % (t2-t,t2-init_time)
		condense()
		print 'current range:',dict_range()
		parsed += 1
		dict.append([])
	return dict

def condense():
	global dict

	while True:
		if dict[-1] == []: dict = dict[:-1]
		else: break

	'''for i in range(len(dict)-1):
		if i >= len(dict)-1: return
		(a,b) = dict[i][-1]
		c,d) = dict[i+1][0]
		if a == c:
			dict[i][-1] = (a,b+d)
			dict[i+1] = dict[i+1][1:]'''

def dict_range():
	(a,b) = dict[0][0]
	(c,d) = dict[-1][-1]
	return '%s-%s' % (a,c)

def locate(word):
	for i in range(len(dict)):
		for j in range(len(dict[i])):
			(a,b) = dict[i][j]
			if a == word: return (i,j)
	return (-1,-1)

def word_count(word):
	(i1,i2) = locate(word)
	(w,c) = dict[i1][i2]
	return c

def write_to_file():
	with open('total_word_counts.txt','w') as f:
		for list in dict:
			for (a,b) in list:
				f.write('%s %s\n' % (a,b))

			f.write('\n')
	print 'written to file'

init(99)
