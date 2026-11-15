#!/usr/bin/env python3
"""Убирает из SVG служебные метаданные (<metadata>, xmlns:c2pa) — иконки становятся в разы легче."""
import re, sys
for p in sys.argv[1:]:
    s = open(p).read()
    t = re.sub(r'<metadata>.*?</metadata>', '', s, flags=re.S)
    t = re.sub(r'\s*xmlns:c2pa="[^"]*"', '', t)
    if t != s:
        open(p, 'w').write(t)
