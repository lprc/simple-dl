from __future__ import unicode_literals
#from java import *
import os
import youtube_dl
import json

def download_youtube (url, path, opts):
	#print(os.environ["HOME"])
	os.chdir(os.environ["HOME"])
	os.chdir('/storage/emulated/0/Download')

	import sys
	sys.stdout = open('logger.txt', 'w')
	sys.stderr = open('logger.txt', 'w')

	os.chdir(path)

	#ydl_opts = {
		#'listformats':True
		#'cachedir': False,
		#'simulate': True,
		#'outtmpl': 'Internal shared storage/Download/%(title)s.%(ext)s'
	#}

	print('used options: ' + json.dumps(opts))
	with youtube_dl.YoutubeDL(opts) as ydl:
		try:
			ydl.download([str(url)])
		except:
			print('error')