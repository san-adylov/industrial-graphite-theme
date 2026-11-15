#!/usr/bin/env python3
"""Склеивает базовую иконку и бейдж модификатора (правый нижний квадрант) с вырезом под бейдж,
как в спеке «04 — Модификаторы на class». Без цвета фона — вырез прозрачный, работает на любом фоне."""
import re, sys, os
D = sys.argv[1]

def parts(path):
    s = open(path).read()
    s = re.sub(r'<metadata>.*?</metadata>', '', s, flags=re.S)
    m = re.match(r'\s*<svg([^>]*)>(.*)</svg>\s*$', s, flags=re.S)
    attrs, body = m.group(1), m.group(2)
    keep = ' '.join(re.findall(r'(?:fill|stroke-width|stroke-linecap|stroke-linejoin)="[^"]*"', attrs))
    return keep, body

def compose(base, badge, out, scale):
    s = 16 * scale; q = 8 * scale; r = 2 * scale
    bk, bb = parts(os.path.join(D, base))
    gk, gb = parts(os.path.join(D, badge))
    hole = (f"M0 0H{s}V{s}H0Z M{q+r} {q}H{s-r}A{r} {r} 0 0 1 {s} {q+r}V{s-r}A{r} {r} 0 0 1 {s-r} {s}"
            f"H{q+r}A{r} {r} 0 0 1 {q} {s-r}V{q+r}A{r} {r} 0 0 1 {q+r} {q}Z")
    svg = (f'<svg xmlns="http://www.w3.org/2000/svg" width="{s}" height="{s}" viewBox="0 0 {s} {s}">'
           f'<defs><clipPath id="cut"><path d="{hole}" clip-rule="evenodd"/></clipPath></defs>'
           f'<g clip-path="url(#cut)" {bk}>{bb}</g><g {gk}>{gb}</g></svg>\n')
    open(os.path.join(D, out), 'w').write(svg)

for base in ["class", "abstractClass", "interface", "record"]:
    compose(f"{base}.svg", "springBean.svg", f"{base}_bean.svg", 1)
print("ok")
