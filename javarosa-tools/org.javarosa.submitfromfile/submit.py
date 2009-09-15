# Copyright (C) 2009 JavaRosa
# 
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

import sys
from optparse import OptionParser
from xml.etree import ElementTree
import httplib
from urlparse import urlparse
import socket

default_path = '/'
default_url = 'http://test.commcarehq.org/receiver/submit/pathfinder'

opt_parser = OptionParser(usage="""usage: %prog [options] [file]

Extract saved forms from xml dump and submit them. Read from stdin if no file given.""")
opt_parser.add_option('-p', '--path', dest='path', help="""xpath expression that identifies the container
nodes of instances to submit (that is, the parent of the top-level node of the instance itself); default:
'/', meaning the entire document is a single instance""")
opt_parser.add_option('-u', '--url', dest='url', help="""url to submit to, or 'file://' to write each form
to a separate file in the local directory; default: %default""")
opt_parser.add_option('-n', '--namespace-path', dest='nspath', help="""xpath expression that identifies
the namespace to use for the associated instance. if absent, it is assumed the namespace is tagged in the
'xmlns' attribute of the instance itself, or there is no namespace. cannot be used if PATH is '/'""")
opt_parser.set_defaults(path=default_path, url=default_url)

(options, args) = opt_parser.parse_args()

path = options.path
url = options.url
nspath = options.nspath
fin = open(args[0]) if args else sys.stdin

if nspath and path == default_path:
  print 'Ignoring namespace path...'
  nspath = None

if url.startswith('file://'):
  url = None

#fix kinda incorrect handling of 'document root' element
#'/' should select the abstract 'document root', not the top-level element
docRoot = ElementTree.Element('')
doc = ElementTree.parse(fin)
docRoot.append(doc.getroot())
doc._setroot(docRoot)

def get_instance (node):
  children = node.getchildren()
  instance = children[0] if children else None
  if instance == None:
    print 'Instance not well formed! Exiting...'
    sys.exit()
  return instance

instances = [get_instance(node) for node in doc.findall(path)]

if nspath:
  namespaces = [node.text for node in doc.findall(nspath)]
  if len(namespaces) != len(instances):
    print 'Number of namespaces (%d) and forms (%d) don\'t match up! Exiting...' % (len(namespaces), len(instances))
    sys.exit()
else:
  namespaces = [None] * len(instances)

def apply_namespace (inst, ns):
  name = inst.tag
  name = name[name.find('}')+1:] #strip out existing namespace
  inst.tag = '{%s}%s' % (ns, name)

  for child in inst.getchildren():
    apply_namespace(child, ns)

def send (data, url):
  up = urlparse(url)

  try:
    conn = httplib.HTTPConnection(up.netloc)
    conn.request('POST', up.path, data, {'Content-Type': 'text/xml', 'User-Agent': 'CCHQ-submitfromfile-python-v0.1'})
    resp = conn.getresponse()
    return resp if resp.status == httplib.OK else None
  except (httplib.HTTPException, socket.error):
    return None

def save (data, filename):
  try:
    f = open(filename, 'w')
    f.write(data)
    f.close()
    return True
  except IOError:
    return False

print '%d instances to send' % len(instances)
for (i, (ns, inst)) in enumerate(zip(namespaces, instances)):
  if ns:
    apply_namespace(inst, ns)

  xml = ElementTree.tostring(inst, 'UTF-8')

  if url:
    result = send(xml, url)
    if result:
      print 'Sent instance %d' % (i + 1)
    else:
      print 'Send failed for instance %d' % (i + 1)
  else:
    result = save(xml, 'form%0*d.xml' % (len(str(len(instances))), i + 1))
    if result:
      print 'Saved instance %d to file' % (i + 1)
    else:
      print 'Failed to save instance %d' % (i + 1)


