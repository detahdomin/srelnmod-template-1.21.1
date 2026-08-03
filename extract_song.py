import sys, types, json
m = types.ModuleType('playsound')
m.playsound = lambda *a: None
sys.modules['playsound'] = m
content = open(r'C:\Users\deped\Desktop\Portal_StillAlive_Python-master\still_alive_credit.py', encoding='utf-8').read()
content = content.replace('if enable_sound:\n    import playsound', 'if False:\n    pass')
exec(content.split('################# Main')[0])

arts = [a1, a2, a3, a4, a5, a6, a7, a8, a9, a10]
print(f'// {len(arts)} ASCII art sets')
print('private static final String[][][] ASCII_ART = {')
for i, art in enumerate(arts):
    print('    { // ' + str(i))
    for line in art:
        escaped = line.replace('\\', '\\\\').replace('"', '\\"')
        print(f'        "{escaped}",')
    print('    },')
print('};')

print()
print(f'// {len(lyrics)} lyrics')
print('private static final SongLyric[] LYRICS = {')
for l in lyrics:
    words = repr(l.words) if isinstance(l.words, str) else str(l.words)
    print(f'    new SongLyric({words}, {l.time}, {l.interval}, {l.mode}),')
print('};')