# Save as dump.py and run: python3 dump.py
from http.server import HTTPServer, BaseHTTPRequestHandler

class Dumper(BaseHTTPRequestHandler):
    def do_ANY(self):
        print(f"\n--- {self.command} {self.path} ---")
        print(self.headers)
        content_length = int(self.headers.get('Content-Length', 0))
        if content_length > 0:
            print(self.rfile.read(content_length).decode('utf-8'))
        self.send_response(200)
        self.end_headers()
        self.wfile.write(b"Logged")

    do_GET = do_POST = do_PUT = do_DELETE = do_ANY

HTTPServer(('0.0.0.0', 8090), Dumper).serve_forever()
