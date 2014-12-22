from rmsdump import *

def read_log_entry (log_entry):
  return tuple(log_entry.val[i].val for i in range(0, 3))

def print_log (log_atom):
  print '%s> %s: %s' % (log_atom[0].strftime('%Y-%m-%d %H:%M:%S'), log_atom[1], log_atom[2])

if __name__ == "__main__":
  data = sys.stdin.read()
  stream = DataStream(data)
  (rmses, num_rms, err) = extract_rms(stream)

  log_rmses = [rms for rms in rmses if rms['name'].startswith('LOG_') and rms['name'] not in ('LOG_IX', 'LOG_PANIC')]
  log_entries = []
  for log_rms in log_rmses:
    log_entries.extend([rec['content'][1] for rec in log_rms['records']])

  panic_rms = [rms for rms in rmses if rms['name'] == 'LOG_PANIC']
  if len(panic_rms) > 0 and len(panic_rms[0]['records']) > 0:
    print 'PANIC entries detected!'

  log_digest = [read_log_entry(le) for le in log_entries]
  for la in sorted(log_digest, key=lambda la: la[0]):
    print_log(la)
