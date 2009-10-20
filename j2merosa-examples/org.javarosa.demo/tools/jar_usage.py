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
import re
import zipfile

BYTE_KB = 1024.0
default_jarfile = 'JavaRosaDemo.jar'
default_obfusmapfile = 'obfuscation_mapping.txt'
default_outfile = 'jar_contents.csv'

#determine sort order of a file
suffix_order = ['class', 'xhtml']
def sort_filename(s):
  suffix = s.rsplit('.', 1)[-1]
  try:
    ordinal = suffix_order.index(suffix)
  except ValueError:
    ordinal = len(suffix_order)
  return (ordinal, s)

#initialize and parse parameters
if len(sys.argv) == 1: #no args, use defaults
  jarfile = default_jarfile
  obfusmapfile = default_obfusmapfile
elif len(sys.argv) == 2: #one arg, assume path
  path = sys.argv[1]
  if path[-1] != '\\':
    path = path + '\\'
  jarfile = path + default_jarfile
  obfusmapfile = path + default_obfusmapfile
else: #assume arg1 = jar file, arg2 = obfuscation mapping
  jarfile = sys.argv[1]
  obfusmapfile = sys.argv[2]
outfile = default_outfile

print 'JAR file: %s' % jarfile
print 'Obfuscation mapping: %s' % obfusmapfile
print 'Output written to: %s' % outfile

#read jar info
zipf = zipfile.ZipFile(jarfile, 'r')
contents = dict()
for i in zipf.infolist():
  contents[i.filename] = (i.file_size, i.compress_size)

#build de-obfuscation mapping
maptxt = open(obfusmapfile, 'r').readlines()
obfusmap = dict()
for line in filter(lambda s: re.compile('^ ').match(s) == None, maptxt): #classname mappings have no leading whitespace
  mapmatch = re.compile('^(?P<class>[A-Za-z0-9._$]+) -> (?P<tag>[a-z]+):$').search(line)
  if mapmatch != None: #None means class was not obfuscated
    mapentry = mapmatch.groupdict()
    obfusmap['%s.class' % mapentry['tag']] = '%s.class' % mapentry['class'].replace('.', '/')

#de-obfuscate
for file, info in contents.copy().iteritems():
  if file in obfusmap:
    del contents[file]
    contents[obfusmap[file]] = info

#write output
fout = open(outfile, 'w')
fout.write('File,Uncompressed Size (KB),Size in JAR (KB)\n')
for _, file in sorted(map(sort_filename, contents.iterkeys())):
  (size, compr_size) = contents[file]
  fout.write('%s,%s,%s\n' % (file, size/BYTE_KB, compr_size/BYTE_KB))
fout.close()