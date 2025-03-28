#ifndef DEF_H
#define DEF_H

#define inv1(a) (1.0 - a)
#define inv2(a, b) (1.0 - max (a, b))
#define inv3(a, b, c) (1.0 - max ( max (a, b), c))
#define cl(x) clamp(x, 0.0, 1.0)

vec3 n_fix(vec3 l) {
  	float y = 0.4;
    vec3 x = vec3(y,y,y);
  	return pow(l,1.0-x);}

#endif