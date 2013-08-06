word_list = []

with open('word-values.txt','r') as f:
	for line in f:
		word_list.append(line.rstrip())

print 'Word List created. Length:',len(word_list)

word_index = 0

for i in range(99):
	print 'opening %02d' % i
	with open('long-arcs/final/replaced/long-arcs.replaced.%02d.txt'%i,'r') as fr:

		fw = open('entropies/entropies.%02d.txt'%i,'w')

		sum = 0
		lines = []
		start = True
		double = False

		for line in fr:

			if line == '\n':

				if start:
					start = False
					continue
				if double: continue

				fw.write('%d\n' % sum)
				for entry in lines:
					fw.write(entry)
				fw.write('\n')
				word_index += 1

				sum = 0
				lines = []
				double = True

			elif line == 'X\n':

				start = False
				double = True
				word_index += 1
				fw.write('X\n\n')

			else:
				
				start = False
				double = False

				lines.append(line)
				num = int(line)
				sum += (num & 0xFFFFFFF)

		if not double:
			fw.write('%d\n' % sum)
			for entry in lines:
				fw.write(entry)
			fw.write('\n')
			word_index += 1

		fw.close()
