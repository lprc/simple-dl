from __future__ import unicode_literals
#from java import *
import os
import youtube_dl
import json

progress = 0

def download_youtube (url, path, opts):
	#print(os.environ["HOME"])
	os.chdir(os.environ["HOME"])
	os.chdir('/storage/emulated/0/Download')

	global progress
	progress = 0 # reset progress

	import sys
	sys.stdout = open('logger.txt', 'w')
	sys.stderr = open('logger.txt', 'w')

	os.chdir(path)

	# print supplied options to log file
	print('used options: ' + json.dumps(opts))

	# add progress hook
	def my_hook(d):
		if d['status'] == 'downloading':
			global progress
			progress = 100 * (d['downloaded_bytes'] / d['total_bytes'])
	opts.update(progress_hooks = [my_hook])

	with youtube_dl.YoutubeDL(opts) as ydl:
		try:
			ydl.download([str(url)])
		except:
			print('error')