word = ''
list = []

for i in range(99):
	print 'opening %02d'%i
	with open('purged/p-arcs.purged.%02d.txt'%i,'r') as fr:
		fw = open('purged/p-arcs.f-purged.%02d.txt'%i,'w')

		for line in fr:
			test = line[:line.find('/')]
			if word == test:
				list.append(line)
			else:
				word = test
				for entry in list:
					fw.write(entry)
				list = []
		if i == 98:
			for entry in list:
				fw.write(entry)
		fw.close()
		
