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

vec3 ACESFilmToneMapping(vec3 c) {
    const mat3 ACESInMat = mat3(
        0.59719, 0.07600, 0.02840,
        0.35458, 0.90834, 0.13383,
        0.04823, 0.01566, 0.83777
    );
    const mat3 ACESOutMat = mat3(
        1.60475, -0.10208, -0.00327,
        -0.53108,  1.10813, -0.07276,
        -0.07367, -0.00605,  1.07602
    );
    c = ACESInMat * c;
    c = (c * (c + 0.0245786)) / (c * (0.983729 * c + 0.4329510) + 0.238081);
    c = ACESOutMat * c;
    return clamp(c, 0.0, 1.0);}

#endif