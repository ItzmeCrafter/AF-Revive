#ifndef NOISE_H
#define NOISE_H

float hash(highp float n) {
    return fract(sin(n)*43758.5453);}
float hash(highp vec2 p) {
    return fract(cos(p.x + p.y * 332.) * 335.552);}
float noise( in highp vec2 x ) {
  vec2 p = floor(x);
  vec2 f = fract(x);
  f = f*f*(3.-2.*f);
   float n = p.x + p.y*57.;
   float res = mix(mix( hash(n+  0.), hash(n+  1.),f.x), mix( hash(n+ 57.), hash(n+ 58.),f.x),f.y);
  return res;}

#endif