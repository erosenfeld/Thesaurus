list = []

for i in range(99):
	print 'opening %02d'%i
	with open('final/p-arcs.ps-final.%02d.txt'%i,'r') as f:
		for line in f:
			
			t1 = line.find('\t')
			t2 = line.find('\t',t1+1)
			t3 = line.find('\t',t2+1)

			(w,r,w2,c) = (line[:t1],line[t1+1:t2],line[t2+1:t3],line[t3+1:])

			list.append('%s\t%s\t%s\t%s'%(w2,r,w,c))


list.sort()
index = len(list)
avg = index / 99

for i in range(98):
	f = open('final/reversed/p-arcs.reversed.%02d.txt'%i,'w')
	for j in range(i*avg,(i+1)*avg):
		f.write(list[j])
	f.close()

f = open('final/reversed/p-arcs.reversed.98.txt','w')
for i in range(98*avg,index):
	f.write(list[i])
f.close()
