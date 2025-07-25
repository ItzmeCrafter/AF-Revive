#ifndef TONE_H
#define TONE_H

float lum(vec3 col) {
    return dot(col, vec3(0.2125, 0.7154, 0.0721));}

float flmC(float x) {
    return (x * (1.51 * x + 0.14 * 0.15) + 0.6 * 0.05) / (x * (1.51 * x + 0.15) + 0.6 * 0.35) - 0.05 / 0.35;}

vec3 tone(vec3 c){
    float w = 1.3 / 1.;
    float l = dot(c, vec3(.0, .3, .3));
    vec3 m = c - l;
    c = (m * 1.5) + l * 1.09;
    c = vec3(flmC(c.r), flmC(c.g), flmC(c.b)) / flmC(w);
    return c;}

vec3 sat(vec3 c, float a) {
    const vec3 w = vec3(0.2125, 0.7154, 0.0721);
    float x = dot(c, w);
    vec3 i = vec3(x,x,x);
    return min(mix(i, c, a), vec3(1.,1.,1.));}

vec3 tonemap(vec3 c) {
    float exposure = -1.0; // Adjust for brightness
    c *= exposure;
    return c / (c + vec3(1.,1.,1.)); // Reinhard tone mapping
}

#endif
