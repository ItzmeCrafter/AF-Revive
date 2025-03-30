#ifndef SKY_H
#define SKY_H

vec3 dS(vec3 tmp, vec3 sP, vec3 wt) {
 float c = dot(sP, vec3(0.0, 1.6, 0.0));
 float s = pow(smoothstep(0.0, 1.0, c), 0.5);
//main
  vec3 c1 = mix(mix(mix(
 	vec3(.4,.63,.8),
 	vec3(.4,.5,.6)*.1,wt.z),mix(
 	vec3(.8,.8,.8),
 	vec3(.1,.1,.1),wt.z),wt.x),
 	vec3(.9,.8,1.04),wt.y);
//upper grad
 vec3 c2 = mix(mix(mix(
 	vec3(.9,.95,1.),
 	vec3(0.2,.38,.65),wt.z),mix(
 	vec3(.43,.43,.43),
 	vec3(.05,.05,.05),wt.z),wt.x), 
 	vec3(1.,.3,.25)+.16,wt.y);
//bottom
 vec3 d1 = mix(mix(mix(
 	vec3(.8,.85,.95)*.76,
 	vec3(.0,.0,.1), wt.z),mix(
 	vec3(.43,.43,.43),
 	vec3(.05,.05,.05), wt.z), wt.x),
 	vec3(.66,.1,.0),wt.y);
//upper bottom grad
 vec3 d2 = mix(mix(mix(
 	vec3(.84,.8,.9)*.95,
	vec3(.0,.1,.3),wt.z),mix(
	vec3(.43,.43,.43),
	vec3(.05,.05,.05),wt.z),wt.x),
	vec3(.98,.2,.08),wt.y);

 vec3 col;
 col = mix(c2, c1, s);
 col = mix(col, d2, clamp(((c - 0.0)/(-0.3 - 0.0)), 0.0, 1.0));
 col = mix(col, d1, clamp(((c - -0.15)/(-0.65 - -0.15)), 0.0, 1.0));
 tmp = (col);
return tmp;
}

vec3 mCC(vec3 wt) {
return mix(mix(mix(vec3(0.9,0.95,1.0)+0.45, vec3(0.1,0.4,0.5), wt.z), vec3(1.0, 0.8, 0.75), wt.y), mix(vec3(.7,.7,.7), vec3(.25,.25,.25), wt.z), wt.x);}
vec3 sCC(vec3 wt) {
return mix(mix(mix(vec3(0.4,0.25,0.15
), vec3(0.06,0.11,0.18), wt.z), vec3(1.1, 0.3, 0.2), wt.y), mix(vec3(.35,.35,.35), vec3(.09,.09,.09), wt.z), wt.x);}

float calculateHash(highp vec3 p5) {
    return fract((p5.x * p5.y) + p5.z);
}

vec3 generateCloud1(vec3 col, vec3 pos, vec3 wT) {
 float dens, opac;
  for (int lIdx = 0; lIdx < 5; lIdx++) {
    vec2 dPos = (pos.xz / pos.y * (1.0 + float(lIdx) * 0.016)) * 4.0;
    vec2 dPosD = floor(dPos + ViewPositionAndTime.w * 0.195);
    vec3 hash = fract(vec3(dPosD.xyx) * 0.1031);
               hash += dot(hash, hash.yzx + 33.33);
    float hVal = calculateHash(hash);
    dens += (hVal > 0.78) ? 1.0 : 0.0;
    opac = mix(opac, 1.0, dens / ((float(10) * float(10) * float(10)) * 0.016));
  }

 dens = clamp(dens * 5.0, 0.0, 1.0);
 vec2 combFactors = (mix(0.5, 0.0, smoothstep(0.0, 1.0, length(pos.xz / (pos.y * 9.0)))) * vec2(dens, opac));

  if (pos.y > 0.0) {
    col = mix(mix(col, mCC(wT), combFactors.x), sCC(wT), combFactors.y);
  }
    return col;
}

vec3 gradientCircle(vec3 pos) {
    return mix(vec3(0.25,0.2,0.35), vec3(0.1,0.05,0.2), smoothstep(0.0, 1., pos.y+1.));
}

#endif