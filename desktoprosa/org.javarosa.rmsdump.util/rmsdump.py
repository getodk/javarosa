import sys

class EndOfStream (Exception):
  def __init__ (self, bytes):
    self.bytes = bytes

def stream (data):
  for c in data:
    yield c

def read_int (dstr):
  (nb, c) = ([], None)
  try:
    while c == None or ord(c) >= 128:
      c = dstr.next()
      nb.append(c)
  except StopIteration:
    raise EndOfStream(nb)

  nb = [ord(c) % 128 for c in nb]
  if nb[0] >= 64:
    nb[0] -= 128
  return reduce(lambda x, y: 128 * x + y, nb, 0)

def read_string (dstr):
  lb = [ord(i) for i in read_bytes(dstr, 2)]
  ln = 256 * lb[0] + lb[1]

  try:
    return read_bytes(dstr, ln)
  except EndOfStream, eos:
    raise EndOfStream(lb + eos.bytes)

def read_bytes (dstr, n):
  bytes = []
  try:
    for i in range(0, n):
      bytes.append(dstr.next())
  except StopIteration:
    raise EndOfStream(bytes)
  
  return ''.join(bytes)

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
    num_rms = read_int(dstr)
    for i in range(0, num_rms):
      rms = {'name': None, 'size': None, 'records': []}
      rmses.append(rms)

      rmsname = read_string(dstr)
      rms['name'] = rmsname

      num_recs = read_int(dstr)
      rms['size'] = num_recs

      for j in range(0, num_recs):
        rec = {'id': None, 'data': None, 'status': None}
        rms['records'].append(rec)

        id = read_int(dstr)
        rec['id'] = id
        rec['status'] = 'data not read'

      for rec in rms['records']:
        try:
          ln = read_int(dstr)
        except EndOfStream, eos:
          rec['status'] = 'corrupt length %s' % eos.bytes
          raise

        try:
          rec['data'] = read_bytes(dstr, ln)
          rec['status'] = 'ok'
        except EndOfStream, eos:
          rec['status'] = 'partial data'
          rec['data'] = ''.join(eos.bytes)
          raise
  except EndOfStream:
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
  print '%d RMSes' % num_rms
  print

  for rms in rmses:
    print '=== %s ===' % coalesce(rms['name'], '[no name]')
    print 'Records: %s' % coalesce(rms['size'], '[no size]')

    for rec in rms['records']:
      print '  ID: %s%s' % (coalesce(rec['id'], '[no id]'), '   (%d bytes)' % len(rec['data']) if rec['data'] or rec['status'] == 'ok' else '')
      if rec['status'] != 'ok':
        print '  Status: %s' % rec['status']
      print format_bytes(rec['data']) if rec['data'] else '    [no data]'

    print

  if err:
    print 'ERROR in dump'

if __name__ == "__main__":

  data = sys.stdin.read()
  dstr = stream(data)

  (rmses, num_rms, err) = extract_rms(dstr)

  print_contents(len(data), rmses, num_rms, err)
