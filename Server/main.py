import time
import BaseHTTPServer

from urlparse import urlparse
import json
import urllib2 
import urllib
from getColor import getColor

HOST_NAME = '140.113.235.154' # !!!REMEMBER TO CHANGE THIS!!!
PORT_NUMBER = 9000 # Maybe set this to 9000.


class MyHandler(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_HEAD(s):
        s.send_response(200)
        s.send_header("Content-type", "text/html")
        s.end_headers()
    def do_GET(s):
        query = urlparse(s.path)[4]
        if query != '':
            input_data = {}
            output_data = {}
            for list_query in query.split('&'):
                tmp = list_query.split('=')
                input_data[tmp[0]] = tmp[1]
            # lat long

            url_obs = 'https://bde75dae-f931-4098-8c54-1113ac61bc24:pTdEQXXoOS@twcservice.mybluemix.net:443/api/weather/v1/geocode/' + input_data['lat'] + '/' + input_data['long'] + '/observations.json?units=m&language=en-US'
            url_for = 'https://bde75dae-f931-4098-8c54-1113ac61bc24:pTdEQXXoOS@twcservice.mybluemix.net:443/api/weather/v1/geocode/' + input_data['lat'] + '/' + input_data['long'] + '/forecast/hourly/48hour.json?units=m&language=en-US'
            print url_obs
            print url_for
            response = urllib.urlopen(url_obs)
            data = json.loads(response.read())
            output_data['uv_desc'] = data['observation']['uv_desc']
            output_data['temp'] = data['observation']['temp']
            output_data['wx_phrase'] = data['observation']['wx_phrase']
            response = urllib.urlopen(url_for)
            data = json.loads(response.read())
            output_data['phrase_32char'] = data['forecasts'][0]['phrase_32char']

            output_data['Chlorophyll'] = getColor(float(input_data['lat']),float(input_data['long']))

            print '=============='
            print output_data
            print '=============='
            """Respond to a GET request."""
            s.send_response(200)
            s.send_header("Content-type", "application/json")
            s.end_headers()
            s.wfile.write(json.dumps(output_data, file, indent=4))
        else: 
            """Respond to a GET request."""
            s.send_response(200)
            s.send_header("Content-type", "text/html")
            s.end_headers()
            s.wfile.write("<html><head><title>Title goes here.</title></head>")
            s.wfile.write("<body><p>This is a test.</p>")
            # If someone went to "http://something.somewhere.net/foo/bar/",
            # then s.path equals "/foo/bar/".
            s.wfile.write("<p>You accessed path: %s</p>" % s.path)
            s.wfile.write("</body></html>")

if __name__ == '__main__':
    server_class = BaseHTTPServer.HTTPServer
    httpd = server_class((HOST_NAME, PORT_NUMBER), MyHandler)
    print time.asctime(), "Server Starts - %s:%s" % (HOST_NAME, PORT_NUMBER)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    print time.asctime(), "Server Stops - %s:%s" % (HOST_NAME, PORT_NUMBER)
