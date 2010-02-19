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
from recparse import *

#parse the data stream from the dump file into a structure representing all RMSes and records, accounting for any errors in the stream
#returns tuple (
#    list of rms -- in order encountered in stream,
#    number of expected rms -- None if couldn't be read,
#    whether error was encountered in the stream
#  ) 
#  where 'rms' is dict {
#    'name': RMS name -- None if unreadable,
#    'size': expected number of records -- None if unreadable,
#    'records': list of records -- in order encountered in stream -- empty list if no records or could not
#               read any records
#  }
#  where 'record' is dict {
#    'id': record ID -- None if unreadable,
#    'len': expected length of data -- None if unreadable,
#    'data': raw content of record as a string -- None if could not read ID or data length, partial data
#            if stream terminated while reading -- may be empty string,
#    'status': status of record
#  }
#  where 'status' is one of:
#    'ok' if record is intact
#    None if ID not readable
#    'data not read' if ID was readable but stream terminated before getting to this record's data (error
#        occured in a different record)
#    'corrupt length <list of 'length' bytes read>' if could not read data length
#    'partial data' if stream terminated before expected length of data was read
def extract_rms (dstr):
  rmses = []
  num_rms = None
  err = False

  try:
    num_rms = get(read_int(dstr, True))
    for i in range(0, num_rms):
      rms = {'name': None, 'size': None, 'records': []}
      rmses.append(rms)

      rmsname = get(read_string(dstr))
      rms['name'] = rmsname

      num_recs = get(read_int(dstr, True))
      rms['size'] = num_recs

      for j in range(0, num_recs):
        rec = {'id': None, 'len': None, 'data': None, 'status': None}
        rms['records'].append(rec)

        id = get(read_int(dstr))
        rec['id'] = id
        rec['status'] = 'data not read'

      for rec in rms['records']:
        try:
          rec['len'] = get(read_int(dstr, True))
        except EndOfStream, eos:
          rec['status'] = 'corrupt length %s' % eos.bytes
          raise
        except ValueError:
          rec['status'] = 'corrupt length'
          raise

        try:
          rec['data'] = read_bytes(dstr, rec['len'])
          rec['status'] = 'ok'
          rec['content'] = get_record_content(rec['data'], rec['id'], rms['name'])
        except EndOfStream, eos:
          rec['status'] = 'partial data'
          rec['data'] = ''.join(eos.bytes)
          raise
  except (EndOfStream, ValueError):
    err = True

  return (rmses, num_rms, err)
  
def get_unique (l, key, val, type):
  matches = filter(lambda x: x[key] == val, l)
  if not matches:
    print 'No %s %s' % (type, val)
    return None
  if len(matches) > 1:
    print 'Warning: more than one %s %s' % (type, val)
  return matches[0]

rms_types = {
  'USER': 'user',
  'CASE': 'case',
  'PAT_REFERRAL': 'patref',
  'PROPERTY': 'property',
  'LOG': 'logentry',
  'FORMS_RECD': 'cc-recd-forms-mapping'
}
  
def get_record_content (bytes, rec_id, rms_name):
  type = None
  ignore = False
  if rms_name.endswith('_IX'):
    if rec_id == 1:
      type = 'bool'
    elif rec_id == 2:
      type = 'obj:rmsinfo'
    elif rec_id == 3:
      type = 'map(int,obj:recloc)'
    elif rec_id == 4:
      ignore = True
  elif len(rms_name) > 3 and rms_name[-3] == '_':
    basename = rms_name[:-3]
    if basename in rms_types:
      type = 'obj:' + rms_types[basename]
      
  if ignore:
    return ('ignore', None)
  elif type != None:
    try:
      return ('ok', parse_data(stream(bytes), type))
    except:
      return ('error', None)
  else:
    return ('unknown', None)
  
#return the raw byte data content for a given RMS record; pass in the parsed RMS structure, RMS name, and record ID
def get_record (rmses, name, id):
  rms = get_unique(rmses, 'name', name, 'RMS')
  if not rms:
    return None

  rec = get_unique(rms['records'], 'id', id, 'record with ID')
  if not rec:
    return None

  if rec['status'] != 'ok':
    print 'Warning: record is corrupt'
  return rec['data']

def format_bytes (data, indent=4):
  strs = [' ' * indent]
  for (i, c) in enumerate(data):
    hx = '%02x' % ord(c)
    if i > 0:
      if i % 30 == 0:
        if (i + 15) % 1024 < 30:
          strs.append('\n')
        strs.append('\n' + ' ' * indent)
      elif i % 10 == 0:
        strs.append('   ')
      else:
        strs.append(' ')
    strs.append(hx)
  return ''.join(strs)

def coalesce (val, fallback):
  return val if val != None else fallback

#pretty print the contents of all RMSes
def print_contents (dlen, rmses, num_rms, err):
  print 'Data length: %d' % dlen
  if num_rms != None:
    print '%d RMSes %s' % (num_rms, '(only %d recovered)' % len(rmses) if len(rmses) < num_rms else '')
  else:
    print '? RMSes'

  for rms in rmses:
    print '=== %s ===' % coalesce(rms['name'], '[no name]')
    if rms['size'] != None:
      print 'Records: %d %s' % (rms['size'], '(only %d recovered)' % len(rms['records']) if len(rms['records']) < rms['size'] else '')
    else:
      print 'Records: [no size]'

    for rec in rms['records']:
      print '  ID: %s' % coalesce(rec['id'], '[no id]')
      if rec['status']:
        if rec['status'] != 'ok':
          print '  Status: %s' % rec['status']
        if rec['len'] != None:
          print '  Data: %d bytes %s' % (len(rec['data']), '(expected %d)' % rec['len'] if len(rec['data']) < rec['len'] else '')
          if rec['data']:
            print format_bytes(rec['data'])
          if rec['content'][0] == 'ok':
            print '  Content:\n' + print_data(rec['content'][1], 2, False, True)
          elif rec['content'][0] == 'error':
            print '  Content: error deserializing'
          elif rec['content'][0] == 'unknown':
            print '  Content: don\'t know record format'          
          print
        else:
          print '  Data: [no data]'
          print

    print

  if err:
    print 'ERROR in dump'

if __name__ == "__main__":

  data = sys.stdin.read()
  dstr = stream(data)

  (rmses, num_rms, err) = extract_rms(dstr)

  print_contents(len(data), rmses, num_rms, err)
