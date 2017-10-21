# coding: UTF-8
import numpy as np
from PIL import Image
import matplotlib.pyplot as plt
from matplotlib import pyplot
from mpl_toolkits.mplot3d import Axes3D
from scipy import genfromtxt
from sklearn.cluster import KMeans
import csv
import sys
import math

#ロジスティック回帰用空配列作成
emptybox0 = np.array([[0,0]])
emptybox1 = np.array([[0,0]])
emptybox2 = np.array([[0,0]])

#画像の分割
count = 1
while(count <= 5):

	pic = Image.open(str(count)+'.jpg')
	w = pic.size[0]
	h = pic.size[1]

	h0 = h/3*1
	h1 = h/3*2

	pic.crop((0,0,w,h0)).save(str(count)+ '0.png')
	pic.crop((0,h0+1,w,h1)).save(str(count)+'1.png')
	pic.crop((0,h1+1,w,h)).save(str(count)+'2.png')



#k-means
	counter = 0
	while(counter <= 2):
		z=str(counter)
		gazou = str(count)+ str(counter) + '.png'
		#im = Image.open()
		imag = Image.open(gazou)
		#print imag
		#plt.imshow(imag)
		#plt.show()

		img = np.array(Image.open(gazou))
		g = imag.size
		##print 'g' + str(g)
		a = imag.size[0]
		r = imag.size[1]
		img_base = img.reshape(r*a,3)
		img_flat=img_base
		##print labels
		##print img_flat
		#RGB→XYZ変換
		red = np.delete(img_flat,[1,2],1)
		green = np.delete(img_flat,[0,2],1)
		blue = np.delete(img_flat,[0,1],1)


		##print red
		largex = 0.412391*red  +0.357584*green  +0.180481*blue
		largey = 0.212639*red  +0.715169*green  +0.072192*blue
		largez = 0.019331*red  +0.119195*green  +0.950532*blue
		#XYZ→u'v'変換
		cielu = 4*largex /(largex +15*largey +3*largez)
		cielv = 9*largey /(largex +15*largey +3*largez)
		##print 'cielu' + str(cielu)
		uv = np.c_[cielu,cielv]
		#np.savetxt( str(count) + z + 'uv.csv',uv,delimiter=',')
		lenuv = len(uv)
		##print 'len'+str(len(uv))
		#「NaN」（非数値）、「inf」（無限大）、「-inf」（負の無限大）除去
		uv[(np.isnan(uv)) | (a==float("inf")) | (a == float("-inf"))] = 0.3
		#クラスタリング
		kmeans_modeluv = KMeans(n_clusters=4).fit(uv)
		labelsuv = kmeans_modeluv.labels_
		#print 'center' + str(kmeans_modeluv.cluster_centers_)
		#代表色
		center = kmeans_modeluv.cluster_centers_
		#ロジスティック回帰場合分け
		#empty = np.array([[0],[0],[0],[0]])
		#addcenter = np.c_[center,empty]
		#print addcenter


		#配列の結合(u'v')
		uvlabel = np.c_[uv,labelsuv]
		##print uvlabel
		#行の削除
		rows, cols = np.where(uvlabel != 0)
		np.delete(uvlabel,rows[np.where(cols==2)],0)
		uv0 = np.delete(uvlabel,np.where(uvlabel != 0)[0][np.where(np.where(uvlabel != 0)[1] == 2)],0)

		rows, cols = np.where(uvlabel != 1)
		np.delete(uvlabel,rows[np.where(cols==2)],0)
		uv1 = np.delete(uvlabel,np.where(uvlabel != 1)[0][np.where(np.where(uvlabel != 1)[1] == 2)],0)

		rows, cols = np.where(uvlabel != 2)
		np.delete(uvlabel,rows[np.where(cols==2)],0)
		uv2 = np.delete(uvlabel,np.where(uvlabel != 2)[0][np.where(np.where(uvlabel != 2)[1] == 2)],0)

		rows, cols = np.where(uvlabel != 3)
		np.delete(uvlabel,rows[np.where(cols==2)],0)
		uv3 = np.delete(uvlabel,np.where(uvlabel != 3)[0][np.where(np.where(uvlabel != 3)[1] == 2)],0)
		#　列の削除
		uvzero = uv0[:,0:2]
		lenzero = len(uvzero)
		uvzerou = np.delete(uvzero,1,1)
		uvzerov = np.delete(uvzero,0,1)
		uvone = uv1[:,0:2]
		lenone = len(uvone)
		uvoneu = np.delete(uvone,1,1)
		uvonev = np.delete(uvone,0,1)
		uvtwo = uv2[:,0:2]
		#print uvtwo[0]
		lentwo = len(uvtwo)
		uvtwou = np.delete(uvtwo,1,1)
		uvtwov = np.delete(uvtwo,0,1)
		uvthree = uv3[:,0:2]
		lenthree = len(uvthree)
		uvthreeu = np.delete(uvthree,1,1)
		uvthreev = np.delete(uvthree,0,1)
		##print len(uvzero)

		#csvファイルに保存
		#np.savetxt(str(count) + z + 'testuv0.csv',uvzero,delimiter=',')
		#np.savetxt(str(count) + z + 'testuv1.csv',uvone,delimiter=',')
		#np.savetxt(str(count) + z + 'testuv2.csv',uvtwo,delimiter=',')
		#np.savetxt(str(count) + z + 'testuv3.csv',uvthree,delimiter=',')
		#u'v'元データ→代表色データへ置き換え
		img_compuv=kmeans_modeluv.cluster_centers_[kmeans_modeluv.labels_]
		##print 'img_compuv' + str(img_compuv)
		#代表色データ
		# 配列の結合
		uvlabel2 = np.c_[img_compuv,labelsuv]
		##print uvlabel2
		#行の削除
		rows, cols = np.where(uvlabel2 != 0)
		np.delete(uvlabel2,rows[np.where(cols==2)],0)
		uv20 = np.delete(uvlabel2,np.where(uvlabel2 != 0)[0][np.where(np.where(uvlabel2 != 0)[1] == 2)],0)

		rows, cols = np.where(uvlabel2 != 1)
		np.delete(uvlabel2,rows[np.where(cols==2)],0)
		uv21 = np.delete(uvlabel2,np.where(uvlabel2 != 1)[0][np.where(np.where(uvlabel2 != 1)[1] == 2)],0)

		rows, cols = np.where(uvlabel2 != 2)
		np.delete(uvlabel2,rows[np.where(cols==2)],0)
		uv22 = np.delete(uvlabel2,np.where(uvlabel2 != 2)[0][np.where(np.where(uvlabel2 != 2)[1] == 2)],0)

		rows, cols = np.where(uvlabel2 != 3)
		np.delete(uvlabel2,rows[np.where(cols==2)],0)
		uv23 = np.delete(uvlabel2,np.where(uvlabel2 != 3)[0][np.where(np.where(uvlabel2 != 3)[1] == 2)],0)

		#　列の削除
		uv2zero = uv20[:,0:2]
		##print 'uv2zero' + str(uv20)
		uv2one = uv21[:,0:2]
		uv2two = uv22[:,0:2]
		uv2three = uv23[:,0:2]
		##print 'uv2zero' + str(uv2zero)
		# パーセント
		perzero = len(uv20)*float(100)/float(a)/float(r)
		perone = len(uv21)*float(100)/float(a)/float(r)
		pertwo = len(uv22)*float(100)/float(a)/float(r)
		perthree = len(uv23)*float(100)/float(a)/float(r)
		perzero = int(perzero)
		perone = int(perone)
		pertwo = int(pertwo)
		perthree = int(perthree)
		percentzero = float(perzero)/float(100)
		percentone = float(perone)/float(100)
		percenttwo = float(pertwo)/float(100)
		percentthree = float(perthree)/float(100)
		percent0 = float(len(uv20))/float(w)/float(h)*100
		#print 'percent0' + str(percent0)
		percent1 = float(len(uv21))/float(w)/float(h)*100
		percent2 = float(len(uv22))/float(w)/float(h)*100
		percent3 = float(len(uv23))/float(w)/float(h)*100
		print perzero + perone + pertwo + perthree


		# 代表色
		daihyou0uv = uv2zero[0]
		#print 'daihyou0uv' + str(daihyou0uv)
		daihyou1uv = uv2one[0]
		#print 'daihyou1uv' + str(daihyou1uv)
		#print uv2two[0]
		daihyou2uv = uv2two[0]
		#print 'daihyou2uv' + str(daihyou2uv)
		daihyou3uv = uv2three[0]
		#print 'daihyou3uv' + str(daihyou3uv)
		daihyouuv = np.vstack((daihyou0uv,daihyou1uv,daihyou2uv,daihyou3uv))
		##print daihyou0uv

		'''
		# 平均値
		average0 = daihyou0uv * percentzero
		average1 = daihyou1uv * percentone
		average2 = daihyou2uv * percenttwo
		average3 = daihyou3uv * percentthree
		average = 0

		for cf in range(lenuv):
			averagef = uv[cf]/lenuv
			average += averagef
		#print 'average' + str(average)

		'''

		'''ロジスティック回帰用配列作成'''
		daihyou0 =  np.array([daihyou0uv])
		daihyou1 =  np.array([daihyou1uv])
		daihyou2 =  np.array([daihyou2uv])
		daihyou3 =  np.array([daihyou3uv])

		emptybox00 = np.array([[0,0]])
		emptybox01 = np.array([[0,0]])
		emptybox02 = np.array([[0,0]])
		emptybox03 = np.array([[0,0]])

		if (perzero == 0):
			perzero = 1

		if (perone == 0):
			perone = 1

		if (pertwo == 0):
			pertwo = 1

		if (perthree == 0):
			perthree = 1


		for i in range(perzero):
			arraydai0 = np.r_[emptybox00,daihyou0]
			emptybox00 = arraydai0
		#print arraydai0
		arraydai0 = np.delete(arraydai0, 0, 0)

		for i in range(perone):
			arraydai1 = np.r_[emptybox01,daihyou1]
			emptybox01 = arraydai1
		#print arraydai1
		arraydai1 = np.delete(arraydai1, 0, 0)


		print 'pertwo' + str(pertwo)
		for i in range(pertwo):
			arraydai2 = np.r_[emptybox02,daihyou2]
			emptybox02 = arraydai2
		#print arraydai2
		arraydai2 = np.delete(arraydai2, 0, 0)

		for i in range(perthree):
			arraydai3 = np.r_[emptybox03,daihyou3]
			emptybox03 = arraydai3
		#print arraydai0
		arraydai3 = np.delete(arraydai3, 0, 0)

		realcenter = np.r_[arraydai0,arraydai1,arraydai2,arraydai3]

		if (counter == 0):
			array0 = np.r_[emptybox0,realcenter]
			emptybox0 = array0
			#print 'emptybox0' + str(emptybox0)
		elif (counter ==1):
			array1 = np.r_[emptybox1,realcenter]
			emptybox1 = array1
			#print 'emptybox1' + str(emptybox1)
		else:
			array2 = np.r_[emptybox2,realcenter]
			emptybox2 = array2
			#print 'emptybox2' + str(emptybox2)

		counter = counter +1
	print count
	count = count +1

emptybox0 = np.delete(emptybox0,0,0)
emptybox1 = np.delete(emptybox1,0,0)
emptybox2 = np.delete(emptybox2,0,0)

np.savetxt( 'goodhigh.csv',emptybox0,delimiter=',')
np.savetxt( 'goodmedium.csv',emptybox1,delimiter=',')
np.savetxt( 'goodlow.csv',emptybox2,delimiter=',')
	##print np.unique(rgblszero)
print 'hello'
