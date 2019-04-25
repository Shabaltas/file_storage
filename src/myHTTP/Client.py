from http.client import HTTPConnection
import urllib
import time
headers = {
    'User-agent' : 'thisPython',
    'Accept-Charset' : 'utf-8'
}
print('Enter server address in format [IP:port] ')
#192.168.0.104:8080
url = input()
connection = HTTPConnection(url)
answer = ''
while (answer != '0'):
    print('What do you want to do?\n1 - get file, 2 - rewrite file, 3 - add to file, 4 - delete file, 5 - copy file, 6 - move file, 0 - exit')
    answer = input()
    if (answer == '1'):
        print('Enter filename to get')
        filename = input()
        print('Enter the path to save your file')
        path = input()
        try:
            connection.request('GET', '/' + filename, headers=headers)
            response = connection.getresponse()
            print('HTTP/1.0' if response.version == 10 else 'HTTP/1.1', response.status, response.reason)
            #print(response.msg)
            if (response.status == 200):
                f = open(path + "\\" + filename, "wb")
                content = response.read()
                f.write(content)
                f.close()
                #print(content.decode('cp1251'))
            else:
                response.read()
        except:
            print('Some mistake, try again')
            time.sleep(1)
    if (answer == '2'):
        print('Enter filename to rewrite')
        filename = input()
        print('Enter content')
        content = input()
        body = {'content' : content}
        try:
            connection.request('PUT', '/' + filename, headers=headers, body=urllib.parse.urlencode(body))
            response = connection.getresponse()
            response.read()
            print('HTTP/1.0' if response.version == 10 else 'HTTP/1.1', response.status, response.reason)
        except:
            print('Some mistake, try again')
            time.sleep(1)
    if (answer == '3'):
        print('Enter filename to add')
        filename = input()
        print('Enter content')
        content = input()
        body = {'content' : content}
        try:
            connection.request('POST', '/' + filename, headers=headers, body=urllib.parse.urlencode(body))
            response = connection.getresponse()
            response.read()
            print('HTTP/1.0' if response.version == 10 else 'HTTP/1.1', response.status, response.reason)
        except:
            print('Some mistake, try again')
            time.sleep(1)
    if (answer == '4'):
        print('Enter filename to delete')
        filename = input()
        try:
            connection.request('DELETE', '/' + filename, headers=headers)
            response = connection.getresponse()
            response.read()
            print('HTTP/1.0' if response.version == 10 else 'HTTP/1.1', response.status, response.reason)
        except:
            print('Some mistake, try again')
            time.sleep(1)
    if (answer == '5'):
        print('Enter filename to copy')
        filename = input()
        print('Enter new path')
        path = input()
        print('Enter new filename')
        newFilename = input()
        body = {'newPath' : path, 'newFilename' : newFilename}
        try:
            connection.request('COPY', '/' + filename, headers=headers, body=urllib.parse.urlencode(body))
            response = connection.getresponse()
            response.read()
            print('HTTP/1.0' if response.version == 10 else 'HTTP/1.1', response.status, response.reason)
        except:
            print('Some mistake, try again')
            time.sleep(1)
    if (answer == '6'):
        print('Enter filename to move')
        filename = input()
        print('Enter new path')
        path = input()
        body = {'newPath' : path}
        try:
            connection.request('MOVE', '/' + filename, headers=headers, body=urllib.parse.urlencode(body))
            response = connection.getresponse()
            response.read()
            print('HTTP/1.0' if response.version == 10 else 'HTTP/1.1', response.status, response.reason)
        except:
            print('Some mistake, try again')
            time.sleep(1)
connection.close()
