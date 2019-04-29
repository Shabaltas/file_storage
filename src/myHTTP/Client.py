from http.client import HTTPConnection
import urllib

headers = {
	'User-agent' : 'thisPython',
	'Accept-Charset' : 'utf-8'
}
print('Enter server address in format [IP:port] ')
#192.168.0.104:8080
url = input()
answer = ''
while (answer != '0'):
	print('What do you want to do?\n1 - get file, 2 - rewrite file, 3 - add to file, 4 - delete file, 5 - copy file, 6 - move file, 0 - exit')
	answer = input()
	try:
		connection = HTTPConnection(url)
		response = ''
		if (answer == '1'):
			print('Enter filename to get')
			filename = input()
			print('Enter the path to save your file')
			path = input()
			connection.request('GET', '/' + filename, headers=headers)
			response = connection.getresponse()
			content = response.read()
			print('HTTP/1.0' if response.version == 10 else 'HTTP/1.1', response.status, response.reason)
			if (response.status == 200):
				f = open(path + "\\" + filename, "wb")
				f.write(content)
				f.close()
		if (answer == '2'):
			print('Enter filename to rewrite')
			filename = input()
			print('Enter content')
			body = {'content' : input()}
			connection.request('PUT', '/' + filename, headers=headers, body=urllib.parse.urlencode(body))
		if (answer == '3'):
			print('Enter filename to add')
			filename = input()
			print('Enter content')
			body = {'content' : input()}
			connection.request('POST', '/' + filename, headers=headers, body=urllib.parse.urlencode(body))
		if (answer == '4'):
			print('Enter filename to delete')
			connection.request('DELETE', '/' + input(), headers=headers)
		if (answer == '5'):
			print('Enter filename to copy')
			filename = input()
			print('Enter new path')
			path = input()
			print('Enter new filename')
			newFilename = input()
			body = {'newPath' : path, 'newFilename' : newFilename}
			connection.request('COPY', '/' + filename, headers=headers, body=urllib.parse.urlencode(body))
		if (answer == '6'):
			print('Enter filename to move')
			filename = input()
			print('Enter new path')
			body = {'newPath' : input()}
			connection.request('MOVE', '/' + filename, headers=headers, body=urllib.parse.urlencode(body))
		if (answer in {'2', '3', '4', '5', '6'}):
			response = connection.getresponse()
			print('HTTP/1.0' if response.version == 10 else 'HTTP/1.1', response.status, response.reason)
			print(response.getheader('message'))
			response.read()
		connection.close()
	except ConnectionRefusedError:
		print('Failed to connect')
		answer = '0'