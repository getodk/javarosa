import struct
from datetime import datetime

#TODO: if there is an error when deserializing the record, would be VERY nice to return the partial
#deserialization of the record up to that point

class Datum:
  def __init__ (self, type, val):
    self.type = type
    self.val = val

  def __repr__ (self):
    return self.pretty_print(suppress_start_indent=True, suppress_end_newline=True)

  def pretty_print (self, indent=0, suppress_start_indent=False, suppress_end_newline=False):
    return self._pretty_print(indent, suppress_start_indent) + ('\n' if not suppress_end_newline else '')

  def _pretty_print (self, indent, suppress_start_indent=False):
    buf = ''

    IND = '  ' * indent
    if not suppress_start_indent:
      buf += IND
  
    if self.type in ('int', 'dbl', 'bool', 'str', 'date', 'bytes', 'generic', 'error'):
      prefix = {'int': 'i', 'dbl': 'f', 'bool': 'b', 'str': 's', 'date': 'd', 'bytes': 'x', 'generic': '?', 'error': '!'}[self.type]
      if self.val != None:
        if self.type == 'int':
          sval = '%d' % self.val
        elif self.type == 'dbl':
          sval = '%f' % self.val
        elif self.type == 'bool':
          sval = ('true' if self.val else 'false')
        elif self.type == 'str' or self.type == 'bytes':
          sval = repr(self.val)
        elif self.type == 'date':
          sval = self.val.strftime('%Y-%m-%d %H:%M:%S')
        elif self.type == 'error':
          sval = '#%d [%s]' % (len(self.val), tohex(self.val))
      else:
        sval = '<null>'
        
      buf += '%s %s' % (prefix, sval)
    elif self.type in ('seq', 'list', 'map') or self.type.startswith('obj:'):
      _print_element = lambda e: e._pretty_print(indent + 1)
      _print_mapping = lambda (k, v): k._pretty_print(indent + 1) + ' => ' + v._pretty_print(indent + 1, True)
    
      def _iteritems_sorted (map):
        for k in sorted(map.keys(), key=lambda datum: datum.val):
          yield (k, map[k])
      
      if self.type == 'seq':
        config = (True, '()', lambda x: x, _print_element)
      elif self.type.startswith('obj:'):
        config = (False, '()', lambda x: x, _print_element)
      elif self.type == 'list':
        config = (True, '[]', lambda x: x, _print_element)
      elif self.type == 'map':
        config = (True, '{}', _iteritems_sorted, _print_mapping)
      (show_count, brackets, iterator, print_elem) = config
        
      buf += self.type + ' '
      if self.val != None:
        if show_count:
          buf += '#%d ' % len(self.val)
        buf += brackets[0]
        if len(self.val) > 0:
          buf += '\n'
          for (i, e) in enumerate(iterator(self.val)):
            buf += print_elem(e)
            if i < len(self.val) - 1:
              buf += ','
            buf += '\n'
          buf += IND
        else:
          buf += ' '
        buf += brackets[1]
      else:
        buf += '<null>'
        
    return buf
    
class Type:
  def __init__ (self, base, params):
    if base.startswith('obj:'):
      self.custom = True
      self.base = base[4:]
      if self.base == '':
        raise ValueError('custom object type not specified')
    else:
      self.custom = False
      self.base = base
    self.params = params
    self.validate()
    
  def basename (self):
    return ('obj:' if self.custom else '') + self.base
    
  def validate (self):
    allowed = {
      'int': 0, 'bool': 0, 'dbl': 0, 'str': 0, 'date': 0, 'bytes': 0,
      'obj': 0, 'seq': None, 'null': 1, 'tagged': 0, 'list': 1, 'listp': 0, 'map': 2, 'mapp': 1
    }
    name = self.base if not self.custom else 'obj'

    if name in allowed:
      num_args = allowed[name]
      if num_args != None and len(self.params) != num_args:
        raise ValueError('wrong number of args for [%s]' % self.basename())
    else:    
      raise ValueError('unrecognized type [%s]' % self.base)

  def parse (self, stream):
    return self.parse_func(stream)(*self.params) 

  def parse_func (self, stream):
    builtin_types = {
      'int': stream.read_int,
      'bool': stream.read_bool,
      'dbl': stream.read_float,
      'str': stream.read_string,
      'date': stream.read_date,
      'bytes': stream.read_binary,
      'null': stream.read_null,
      'tagged': stream.read_tagged,
      'list': stream.read_list,
      'listp': stream.read_list_poly,
      'map': stream.read_map,
      'mapp': stream.read_map_poly,
      'seq': lambda *subtypes: Datum('seq', tuple([type.parse(stream) for type in subtypes]))
    }
  
    if not self.custom:
      return builtin_types[self.base]
    else:
      if self.base in custom_types:
        parse_obj_func = custom_types[self.base]
        return lambda: Datum(self.basename(), parse_obj_func(stream))
      else:
        raise ValueError('unknown object type [%s]' % self.base) #TODO: propogate partial deserialization
    
  def null_datum (self):
    if self.base in ['null', 'tagged']:
      basetype = 'generic'
    elif self.base == 'listp':
      basetype = 'list'
    elif self.base == 'mapp':
      basetype = 'map'
    else:
      basetype = self.base
    return Datum(basetype, None)

  def unwrap (self):
    if self.base != 'seq' or len(self.params) != 1:
      raise ValueError('not a single-item sequence')
    return self.params[0]

class Stream:
  def __init__ (self, bytes):
    self.stream = self.stream_gen(bytes)
    self.buffers = []

  def stream_gen (self, bytes):
    for b in bytes:
      yield b
    
  def read (self):
    try:
      b = self.stream.next()
      for buffer in self.buffers:
        buffer.append(b)
      return b  
    except StopIteration:
      raise self.EndOfStream([''.join(buff) for buff in reversed(self.buffers)])

  def mark (self):
    self.buffers.append([])
  
  def iter (self):
    try:
      while True:
        yield self.read()
    except self.EndOfStream:
      raise StopIteration
  
  class EndOfStream (Exception):
    bytes = 'not implemented'

    def __init__ (self, buffers):
      self.buffers = buffers
      
    def __str__ (self):
      return 'unexpected end of stream'

class DataStream (Stream):
  def __init__ (self, bytes):
    Stream.__init__(self, bytes)
      
  def read (self, n=1):
    return ''.join([Stream.read(self) for i in range(0, n)])

  def read_int (self, require_pos=False):
    (buff, c) = ([], None)
    while c == None or ord(c) >= 128:
      c = self.read()
      buff.append(ord(c) % 128)

    if buff[0] >= 64:
      buff[0] -= 128
    val = reduce(lambda x, y: 128 * x + y, buff)
  
    if require_pos and val < 0:
      raise ValueError('negative integer') #TODO: propogate partial deserialization
    elif len(buff) > 1:
      k = len(buff) - 1
      vmin = -(128**k / 2)
      vmax = 128**k / 2 - 1
      if val <= vmax and val >= vmin:
        raise ValueError('overlong integer encoding') #TODO: propogate partial deserialization

    return Datum('int', val)

  def read_string (self):
    n = reduce(lambda x, y: 256 * x + y, [ord(b) for b in self.read(2)])
    val = self.read(n)
    
    try:
      unicode(val, 'utf-8')
    except UnicodeDecodeError:
      raise #TODO: propogate partial deserialization
    
    return Datum('str', val)
    
  def read_bool (self):
    b = ord(self.read())
  
    if b != 0 and b != 1:
      raise ValueError('boolean not 0x00 or 0x01') #TODO: propogate partial deserialization
    return Datum('bool', b == 1)

  def read_float (self):
    return Datum('dbl', struct.unpack('!d', self.read(8))[0])
  
  def read_date (self):
    try:
      return Datum('date', datetime.utcfromtimestamp(self.read_int().val / 1000.))
    except ValueError: # out-of-range error
      raise ValueError('date ticks overflow') #TODO: propogate partial deserialization
      
  def read_binary (self):
    return Datum('bytes', self.read(self.read_int().val))

class CompoundDataStream (DataStream):
  def __init__ (self, bytes):
    DataStream.__init__(self, bytes)
    
  def read_null (self, type):
    if self.read_bool().val:
      return type.parse(self)
    else:
      return type.null_datum()    
    
  def read_list (self, type):
    return self._read_list(lambda: type.parse(self))

  def _read_list (self, get_elem):
    v = []
    n = self.read_int().val
    for i in range(0, n):
      v.append(get_elem())
    return Datum('list', v)
  
  def read_map (self, keytype, elemtype):
    return self._read_map(keytype, lambda: elemtype.parse(self))

  def _read_map (self, keytype, get_elem):
    m = {}
    n = self.read_int().val
    for i in range(0, n):
      k = keytype.parse(self)
      v = get_elem()
      m[k] = v
    return Datum('map', m)
  
  def read_tagged (self):
    return self.read_type().parse(self)

  def read_type (self):
    tag = self.read(4)
    basetype = basetype_from_tag(tag)
  
    if basetype == 'wrapper':
      (basetype, params) = self.unwrap_type()
    else:
      params = []

    return Type(basetype, params)

  def unwrap_type (self):
    subtype = self.read_int().val
    if subtype == 0:
      return ('null', [self.read_type()])
    elif subtype == 32:
      return ('list', [self.read_type()])
    elif subtype == 33:
      return ('listp', [])
    elif subtype == 34:
      self.read_bool() # 'ordered' flag
      return ('map', [self.read_type(), self.read_type()])
    elif subtype == 35:
      self.read_bool() # 'ordered' flag
      return ('mapp', [self.read_type()])
    else:
      raise ValueError('unrecognized wrapper code [%d]' % subtype) #TODO: propogate partial deserialization
    
  def read_list_poly (self):
    return self._read_list(lambda: self.read_tagged())
  
  def read_map_poly (self, keytype):
    return self._read_map(keytype, lambda: self.read_tagged())

  def read_compound (self, template):
    return type_from_template(template).parse(self)
    
  def read_template (self, template):
    return type_list_from_template(template).parse(self)  

def deserialize (bytes, template):
  stream = CompoundDataStream(bytes)
  obj = stream.read_compound(template)
  #handle botched parsing here?
  #handle extra data left over here?
  #return (status, obj)
  return obj

def type_from_template (template):
  return type_list_from_template(template).unwrap()
  
def type_list_from_template (template):
  return Type('seq', tuple([type_from_template_token(token) for token in tokenize(template, ',', '()')]))

def type_from_template_token (token):
  if '(' in token and token[-1] != ')':
    raise ValueError('extra crap after close paren')
    
  if '(' in token:
    name = token.split('(')[0]
    args = list(type_list_from_template(token[token.find('(')+1:-1]).params)
  else:
    name = token
    args = []
  
  if len(name) == 0:
    raise ValueError('empty token name')

  return Type(name, args)
  
def tokenize (str, sep, brackets):
  depth = 0
  tok_start = 0
  for i in range(0, len(str) + 1):
    new_token = False
    if i == len(str):
      if depth == 0:
        new_token = True
      else:
        raise ValueError('unbalanced brackets')
    elif str[i] == sep and depth == 0:
      new_token = True
    elif str[i] == brackets[0]:
      depth += 1
    elif str[i] == brackets[1]:
      depth -= 1
      if depth < 0:
        raise ValueError('unbalanced parens')

    if new_token:
      token = str[tok_start:i]
      tok_start = i + 1    
      yield token
  
def parse_custom (template):
  return lambda stream: stream.read_template(template).val

# relies on stream containing ONLY data for the record
def _parse_property (stream):
  return (Datum('str', ''.join(list(stream.iter()))),)

def _parse_tree_child (stream):
  if stream.read_bool().val:
    val = stream.read_compound('obj:treeelem')
  else:
    val = stream.read_tagged() # if this happens, which it almost certainly won't, we almost certainly won't have the prototype registered
  return (val,)
    
def _parse_xpath_num_lit (stream):
  if stream.read_bool().val:
    val = stream.read_float()
  else:
    val = stream.read_int()
  return (val,)

def _parse_xpath_path (stream):
  type = stream.read_int()
  filtexpr = stream.read_compound('obj:xpath-expr-filt') if type.val == 2 else None
  steps = stream.read_compound('list(obj:xpath-step)')
  return (type, filtexpr, steps) if filtexpr != None else (type, steps)

def _parse_xpath_step (stream):
  axis = stream.read_int()
  test = stream.read_int()
  if test.val == 0:
    detail = stream.read_compound('obj:qname')
  elif test.val == 2:
    detail = stream.read_string()
  elif test.val == 6:
    detail = stream.read_compound('null(str)')
  else:
    detail = None
  preds = stream.read_compound('listp')

  return (axis, test, detail, preds) if detail != None else (axis, test, preds)
  
custom_types = {
  'rmsinfo': parse_custom('int,int,int'),
  'recloc': parse_custom('int,int'),
  'user': parse_custom('str,str,int,str,bool,map(str,str)'),
  'case': parse_custom('str,str,str,str,bool,null(date),int,mapp(str)'),
  'patref': parse_custom('str,date,date,str,str,int,bool'),
  'formdef': parse_custom('int,str,null(str),listp,obj:forminst,null(obj:loclzr),list(obj:condition),list(obj:recalc),listp'),
  'qdef': parse_custom('int,null(str),null(str),null(str),null(str),null(str),null(str),null(str),int,list(obj:selchoice),null(tagged)'),
  'selchoice': parse_custom('bool,str,str'),
  'gdef': parse_custom('int,tagged,null(str),null(str),null(str),null(str),bool,listp,bool,null(tagged)'),
  'loclzr': parse_custom('bool,bool,map(str,listp),list(str),null(str),null(str)'),
  'resfiledatasrc': parse_custom('str'),
  'localedatasrc': parse_custom('map(str,str)'),
  'condition': parse_custom('tagged,obj:treeref,list(obj:treeref),int,int'),
  'recalc': parse_custom('tagged,obj:treeref,list(obj:treeref)'),
  'treeref': parse_custom('int,list(str),list(int)'),
  'forminst': parse_custom('int,int,null(str),null(str),null(date),map(str,str),obj:treeelem'),
#  'forminst-compact': ...,   oh boy...
  'treeelem': parse_custom('str,int,bool,null(tagged),null(list(obj:treechildpoly)),int,bool,bool,bool,bool,bool,null(obj:constraint),str,str,list(str)'),
  'treechildpoly': _parse_tree_child,
  'intdata': parse_custom('int'),
  'booldata': parse_custom('bool'),
  'strdata': parse_custom('str'),
  'selonedata': parse_custom('obj:sel'),
  'selmultidata': parse_custom('list(obj:sel)'),
  'sel': parse_custom('str,int'),
  'floatdata': parse_custom('dbl'),
  'datedata': parse_custom('date'),
  'datetimedata': parse_custom('date'),
  'timedata': parse_custom('date'),
  'constraint': parse_custom('tagged,str'),
  'xpathcond': parse_custom('tagged'),
  'xpathref': parse_custom('str,obj:treeref'),
  'xpath-expr-arith': parse_custom('int,tagged,tagged'),
  'xpath-expr-bool': parse_custom('int,tagged,tagged'),
  'xpath-expr-cmp': parse_custom('int,tagged,tagged'),
  'xpath-expr-eq': parse_custom('bool,tagged,tagged'),
  'xpath-expr-filt': parse_custom('tagged,listp'),
  'xpath-expr-func': parse_custom('obj:qname,listp'),
  'xpath-expr-numlit': _parse_xpath_num_lit,
  'xpath-expr-numneg': parse_custom('tagged'),
  'xpath-expr-path': _parse_xpath_path,
  'xpath-expr-strlit': parse_custom('str'),
  'xpath-expr-union': parse_custom('tagged,tagged'),
  'xpath-expr-varref': parse_custom('obj:qname'),
  'xpath-step': _parse_xpath_step,
  'qname': parse_custom('null(str),str'),
  'property': _parse_property,
  'txmsg': parse_custom('tagged'),
  'simplehttptxmsg': parse_custom('str,int,str,int,str,date,date,int,int,str,int,str,bytes'),
  'logentry': parse_custom('date,str,str'),
  'cc-recd-forms-mapping': parse_custom('list(int),map(int,int)')
}
 
def basetype_from_tag (tag):
  type_tags = {
    '\xff\xff\xff\xff': 'wrapper',
    '\xe5\xe9\xb5\x92': 'generic', #object -- should never be encountered
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
#   '\xed\x09\xe3\x8e': 'obj:forminst', #unused i think
#   '\xfb\x2c\xa2\x76': 'obj:txmsgserwrapper' #unused i think
  }

  if tag in type_tags:
    basetype = type_tags[tag]
    
    if basetype == 'generic':
      raise ValueError("'generic' type tag should never show up in practice")
    
    return basetype
  else:
    raise ValueError("no type known for tag %s" % tohex(tag)) #TODO: propogate partial deserialization
  
def hexinput (hexstr):
  return ''.join([chr(int(c, 16)) for c in hexstr.split()])
  
def tohex (bytes):
  return ' '.join(['%02x' % ord(b) for b in bytes])
