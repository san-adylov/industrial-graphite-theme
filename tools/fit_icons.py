#!/usr/bin/env python3
"""Ужимает рисунок иконки в рамку 14x14 по центру холста 16x16 — как у стандартных иконок New UI."""
import re, sys, io
import cairosvg
from PIL import Image
LIMIT = 14.0
for path in sys.argv[1:]:
    if '@20x20' in path:
        continue  # иконки панелей 20x20 — свой холст, не трогаем
    if path.endswith('_bean.svg'):
        continue  # склейки с бейджем собирает compose_badges.py
    s = open(path).read()
    im = Image.open(io.BytesIO(cairosvg.svg2png(bytestring=s.encode(), output_width=160, output_height=160))).convert("RGBA")
    bb = im.getchannel("A").point(lambda v: 255 if v > 40 else 0).getbbox()
    size = max(bb[2] - bb[0], bb[3] - bb[1]) / 10
    if size <= LIMIT:
        continue
    k = round(LIMIT / size, 4)
    s = re.sub(r'<metadata>.*?</metadata>', '', s, flags=re.S)
    s = re.sub(r'\s*xmlns:c2pa="[^"]*"', '', s)
    m = re.match(r'(\s*<svg[^>]*>)(.*)(</svg>\s*)$', s, flags=re.S)
    s = f'{m.group(1)}<g transform="translate(8 8) scale({k}) translate(-8 -8)">{m.group(2)}</g>{m.group(3)}'
    open(path, 'w').write(s)
    print(f"{path}: {size}px -> {LIMIT}px (scale {k})")
