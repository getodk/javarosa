import struct
from datetime import datetime

class EndOfStream (Exception):
  def __init__ (self, bytes):
    self.bytes = bytes
    
def stream (data):
  for c in data:
    yield c

def read_bytes (dstr, n):
  bytes = []
  try:
    for i in range(0, n):
      bytes.append(dstr.next())
  except StopIteration:
    raise EndOfStream(bytes)
  
  return ''.join(bytes)

def read_int (dstr, require_pos=False):
  (nb, c) = ([], None)
  try:
    while c == None or ord(c) >= 128:
      c = dstr.next()
      nb.append(c)
  except StopIteration:
    raise EndOfStream(nb)

  nv = [ord(c) % 128 for c in nb]
  if nv[0] >= 64:
    nv[0] -= 128
  val = reduce(lambda x, y: 128 * x + y, nv, 0)

  if val < 0 and require_pos:
    raise ValueError
  return val

def read_string (dstr):
  lb = [ord(i) for i in read_bytes(dstr, 2)]
  ln = 256 * lb[0] + lb[1]

  try:
    return read_bytes(dstr, ln)
  except EndOfStream, eos:
    raise EndOfStream(lb + eos.bytes)

def read_bool (dstr):
  try:
    b = ord(dstr.next())
  except StopIteration:
    raise EndOfStream([])

  if b != 0 and b != 1:
    raise ValueError
  return (b == 1)

def read_float (dstr):
  raw = read_bytes(dstr, 8)
  return struct.unpack('!d', raw)[0]
  
def read_date (dstr):
  return datetime.utcfromtimestamp(read_int(dstr) / 1000.)

#todo: the parse functions for compound objects don't retain the whole bytestream during unexpected end-of-stream  

def read_null (dstr, type):
  if read_bool(dstr):
    return parse(dstr, type)
  else:
    return None
    
def read_list (dstr, type):
  return read_list_helper(dstr, lambda dstr: parse(dstr, type))

def read_list_helper(dstr, get_elem):
  v = []
  n = read_int(dstr)
  for i in range(0, n):
    v.append(get_elem(dstr))
  return v
  
def read_map (dstr, keytype, elemtype):
  return read_map_helper(dstr, keytype, lambda dstr: parse(dstr, elemtype))

def read_map_helper (dstr, keytype, get_elem):
  m = {}
  n = read_int(dstr)
  for i in range(0, n):
    k = parse(dstr, keytype)
    v = get_elem(dstr)
    m[k] = v
  return m
  
def read_tagged (dstr):
  tag = read_bytes(dstr, 4)
  if tag in type_tags:
    type = type_tags[tag]
    
    if type == 'wrapper':
      raise ValueError("don't support wrapper tags currently")
    elif type == 'generic':
      raise ValueError("don't know how to handle generic")
    
    return parse(dstr, [type])
  else:
    raise ValueError("don't know tag [%s]" % repr(tag))
  
def read_list_poly (dstr):
  return read_list_helper(dstr, lambda dstr: read_tagged(dstr))
  
def read_map_poly (dstr, keytype):
  return read_map_helper(dstr, keytype, lambda dstr: read_tagged(dstr))
  
def get_parse_func (name):
  if name in builtin_types:
    return builtin_types[name]
  elif name.startswith('obj:'):
    name = name[4:]
    if name not in custom_types:
      raise ValueError('unknown type [%s]' % name)
      
    return custom_types[name]
  
def parse (dstr, type):
  name = type[0]
  args = type[1:]
  
  return get_parse_func(name)(*([dstr] + args))
  
def parse_template (dstr, template):
  types = parse_type_template(template)
  data = []
  
  for type in types:
    data.append(parse(dstr, type))
  
  return tuple(data)
  
def parse_custom (template):
  return lambda dstr: parse_template(dstr, template)
  
def parse_type_template (template):
  return parse_token_list(template)

def parse_token_list (toklist):
  tokens = []

  depth = 0
  tok_start = 0
  for tok_end in range(0, len(toklist) + 1):
    new_token = False
    if tok_end == len(toklist):
      if depth == 0:
        new_token = True
      else:
        raise ValueError('unbalanced parens')
    elif toklist[tok_end] == ',' and depth == 0:
      new_token = True
    elif toklist[tok_end] == '(':
      depth += 1
    elif toklist[tok_end] == ')':
      depth -= 1
      if depth < 0:
        raise ValueError('unbalanced parens')

    if new_token:
      tokens.append(parse_token(toklist[tok_start:tok_end]))
      tok_start = tok_end + 1    

  return tokens

def parse_token (tokstr):
  token = []

  if '(' in tokstr and tokstr[-1] != ')':
    raise ValueError('extra crap after close paren')
    
  if '(' in tokstr:
    name = tokstr[:tokstr.find('(')]
    args = parse_token_list(tokstr[tokstr.find('(')+1:-1])
  else:
    name = tokstr
    args = []
  
  if len(name) == 0:
    raise ValueError('empty token name')
  
  validate_token(name, args)
  
  token.append(name)
  token.extend(args)    
  return token

def validate_token (name, args):
  allowed = {'int': 0, 'bool': 0, 'dbl': 0, 'str': 0, 'date': 0, 'obj:': 0, 'null': 1, 'tagged': 0, 'list': 1, 'listp': 0, 'map': 2, 'mapp': 1}
  
  if name == 'obj:':
    raise ValueError('custom object not specified')
  
  if name not in allowed:
    if name.startswith('obj:'):
      name = 'obj:'
    else:
      raise ValueError('unrecognized type [%s]' % name)

  if len(args) != allowed[name]:
    raise ValueError('wrong number of args for [%s]' % name)


builtin_types = {
  'int': read_int,
  'bool': read_bool,
  'dbl': read_float,
  'str': read_string,
  'date': read_date,
  'null': read_null,
  'tagged': read_tagged,
  'list': read_list,
  'listp': read_list_poly,
  'map': read_map,
  'mapp': read_map_poly
}

custom_types = {
  'test': parse_custom('int,str,bool')
}
  
type_tags = {
  '\xff\xff\xff\xff': 'wrapper',
  '\xe5\xe9\xb5\x92': 'generic',
  '\x7c\xa1\x6f\xdb': 'int',
  '\x8a\xc5\x87\x0b': 'int', #long
  '\xb5\xdc\x2e\x41': 'int', #short
  '\x03\x3e\xb3\x91': 'int', #byte
  '\x58\x4b\x12\x84': 'char',
  '\xe4\xf9\xf9\xae': 'bool',
  '\xc9\x83\xee\x7b': 'dbl', #float
  '\x8e\xa8\x96\x89': 'dbl',
  '\x42\xc2\x5b\xe3': 'str',
  '\xc5\x1d\xfd\xa6': 'date',
  '\x27\x51\x2e\xc9': 'obj:qdef',
  '\xb3\xc4\x9b\xbd': 'obj:gdef',
  '\xed\x09\xe3\x8e': 'obj:forminst',
  '\x68\xc2\xaf\xad': 'obj:intdata',
  '\x8f\x4b\x45\xfe': 'obj:booldata',
  '\xed\xce\xd1\xce': 'obj:geodata',
  '\x02\x6f\x56\x15': 'obj:strdata',
  '\x29\xd7\x1a\x40': 'obj:selonedata',
  '\xf7\x30\xcc\x7d': 'obj:selmultidata',
  '\x4e\x52\xe2\x15': 'obj:floatdata',
  '\x51\x0e\x1e\x6e': 'obj:datedata',
  '\x6f\x87\x88\xa7': 'obj:datetimedata',
  '\x68\x4e\x4e\x2e': 'obj:timedata',
  '\x2b\xf7\x1a\xcb': 'obj:ptrdata',
  '\xec\xa8\xec\xde': 'obj:multiptrdata',
  '\xef\x74\x56\x54': 'obj:basicdataptr',
  '\xf3\x06\x34\x28': 'obj:xpath-expr-arith',
  '\xf6\xe4\xb9\xaf': 'obj:xpath-expr-bool',
  '\x91\x2e\xfc\xee': 'obj:xpath-expr-cmp',
  '\x65\x71\x6e\x97': 'obj:xpath-expr-eq',
  '\xe7\x68\xb3\x6d': 'obj:xpath-expr-filt',
  '\x67\x44\xc2\x7e': 'obj:xpath-expr-func',
  '\x17\xe0\x31\x27': 'obj:xpath-expr-numlit',
  '\x35\x60\xa2\x3b': 'obj:xpath-expr-numneg',
  '\xfc\x87\x51\x53': 'obj:xpath-expr-path',
  '\xef\x45\x98\x8f': 'obj:xpath-expr-strlit',
  '\xff\x82\x5b\x62': 'obj:xpath-expr-union',
  '\xf9\x4b\xf7\xa8': 'obj:xpath-expr-varref',
  '\x5c\x57\xbb\x5e': 'obj:xpathref',
  '\x5e\x88\x11\xfe': 'obj:xpathcond',
  '\xf4\xaa\xb2\xe9': 'obj:resfiledatasrc',
  '\xf6\xc7\x83\x5c': 'obj:localedatasrc',
  '\x27\x53\xac\x23': 'obj:simplehttptxmsg',
  '\x01\x12\x89\x43': 'obj:smstxmsg',
  '\x21\x71\xd6\x5d': 'obj:binsmstxmsg',
  '\xfb\x2c\xa2\x76': 'obj:txmsgserwrapper'
}
  
  
def hex_to_stream (hexstr):
  return stream(''.join([chr(int(c, 16)) for c in hexstr.split()]))
