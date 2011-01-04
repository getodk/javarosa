import sys

# a utility script to generate xform markup for select choices
# the utility reads the raw choice information from stdin, one choice per line,
# each line of the format [label]|[value]. the markup for <item> and <itext> is
# printed on stdout

lines = [l.strip() for l in sys.stdin.readlines() if l.strip()]
pairs = [(p[0], p[1]) for p in [l.split('|') for l in lines]]

for (_, v) in pairs:
  print """<item><label ref="jr:itext('ch-%s')" /><value>%s</value></item>""" % (v, v)
  
for (l, v) in pairs:
  print """<text id="ch-%s"><value>%s</value></text>""" % (v, l)