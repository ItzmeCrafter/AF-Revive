$input v_color0, v_fog, v_texcoord0, v_lightmapUV, wpos, cpos, fogplacement, worldcolor, worldtime, findcave, cunstructAO, nshd, removeAO, sunbloom, wflag

#include <bgfx_shader.sh>

uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

SAMPLER2D_AUTOREG(s_MatTexture);
SAMPLER2D_AUTOREG(s_SeasonsTexture);
SAMPLER2D_AUTOREG(s_LightMapTexture);

#include <azify/def.h>
#include <azify/noise.h>
#include <azify/tone.h>
#include <azify/sky.h>

#define nlit inv1(v_lightmapUV.x * v_lightmapUV.x)
#define wlit v_lightmapUV.x * v_lightmapUV.x
#define pbr_Strength 0.0004

vec3 getTangentVector(vec3 n) {
    vec3 t = vec3(1, 0, 0);
    if (abs(n.x) > 0.5) t = vec3(0, 0, sign(n.x));
    else if (abs(n.y) > 0.5) t = vec3(1, 0, 0);
    else if (abs(n.z) > 0.5) t = vec3(sign(n.z), 0, 0);
    return t;}

vec4 roughsun(vec4 c, float s, vec3 P, vec3 wp, vec3 wt, float cv) {
  vec3 SC = mix(vec3(.9,.7,.3), vec3(.0,.05,.1), wt.z);
  vec3 bp = normalize(-wp);
  vec3 sp1 = vec3(.89,.259,.0);
  float s1 = max(.0, dot(P, normalize(bp + sp1)));
  vec3 sp2 = vec3(-.968,.295,.0);
  float s2 = max(.0, dot(P, normalize(bp + sp2)));
  vec3 sp3 = vec3(.968,.295,.0);
  float s3 = max(0.0, dot(P, normalize(bp + sp3)));
  vec4 SC4 = vec4(SC,1.);
  float p1 = cl(pow(s1, s)) * (1.0 - wt.y) * inv1(cv) * (1.0 - wt.x);
  float p2 = cl(pow(s2, s)) * wt.y * inv1(cv) * (1.0 - wt.x);
  float p3 = cl(pow(s3, s)) * wt.y * inv1(cv) * (1.0 - wt.x);
  c += SC4 * p1 + SC4 * 0.5 * p2 + SC4 * inv1(cv) * p3;
  return c;
}

vec4 sunRef(vec4 c, float s, vec3 P, vec3 wp, vec3 cp, vec3 wt, vec2 l) {
  vec3 SC = mix(vec3(1.,0.8, 0.5)+0.4, vec3(.9,.9,.9), wt.z);
  vec3 bp = normalize(-wp);
  vec3 sp1 = vec3(0.89, 0.159, 0.0);
  float s1 = max(0.0, dot(P, normalize(bp + sp1)));
  vec3 sp2 = vec3(-0.968, 0.295, 0.0);
  float s2 = max(0.0, dot(P, normalize(bp + sp2)));
  vec3 sp3 = vec3(0.968, 0.295, 0.0);
  float s3 = max(0.0, dot(P, normalize(bp + sp3)));
  vec4 SC4 = vec4(SC, 1.0);
  float p1 = cl(pow(s1, s)) * (1.0 - wt.y) * l.y * (1.0 - wt.x);
  float p2 = cl(pow(s2, s)) * wt.y * l.y * (1.0 - wt.x);
  float p3 = cl(pow(s3, s)) * wt.y * l.y * (1.0 - wt.x);
  float wave = sin(wp.x * 0.2 + ViewPositionAndTime.x * 2.0) * 0.9 +  
             sin(wp.z * 0.2 + ViewPositionAndTime.x * 2.0) * 0.7;  
  float groundNoise = noise(vec2(cp.x * 0.8 +ViewPositionAndTime.w * 2.0, cp.z * 0.8)+wave);
  c += (SC4 * p1 + SC4 * 1.9 * p2 + SC4 * 1.9 * p3)*groundNoise;
  return c;
}


vec4 wFunc(vec4 c, const float w, const vec3 P, const vec3 Cp, const vec3 Wp, const vec2 l, const vec3 wt, sampler2D t, vec2 u, float iT) {
  vec3 wn = normalize(Wp);
  vec3 vD = normalize(-Wp);
  float NoV = max(0.0, dot(P, vD));
  float F = smoothstep(0.38, 0.0, NoV);
  vec3 Rp = reflect(normalize(Wp), P);
  float L = pow(1.0 - abs(Rp.y), 3.0);

    vec3 wc = dS(c.rgb, vD, wt);
    //wc = mix(wc, vec3(1.0, 0.35, 0.26), wt.y);
    //wc = mix(wc, mix(vec3(0.45), vec3(0.15), wt.z), wt.x);
    vec3 wb = mix(c.rgb, wc, clamp(L, 0.0, 1.0));
    c = vec4(mix(c.rgb, wc, (1.0 - abs(dot(wn, P))) * l.y), mix(c.w * 0.35, 1.0, F));
  
  return c;
}

vec3 normals(in sampler2D tex, in vec2 coord0) {
	vec2 dl_uv = vec2(pbr_Strength, 0.);
  float d0 = lum(texture2D(tex, coord0 + dl_uv.yy).rgb);
  float d1 = lum(texture2D(tex, coord0 + dl_uv.xy).rgb);
  float d2 = lum(texture2D(tex, coord0 + dl_uv.yx).rgb);

	return normalize(vec3((d0 - d1) * 2.5, (d0 - d2) * 2.5, 1.0));
}

void main() {
    vec4 diffuse;
		//float worldtime.x = worldtime.x;
		//float worldtime.z = worldtime.z;
		//float worldtime.y = worldtime.y;
		//float worldtime.w = worldtime.w;
		//float findcave.x = findcave.x;
    //float findcave.y = findcave.y;

float alpha = texture2DLod(s_MatTexture, v_texcoord0, 0.0).a;
float Metals = (alpha > 0.95 && alpha < 1.0) ? 1.0 : 0.0;
float Roughs = (alpha > 0.92 && alpha < 0.94) ? 1.0 : 0.0;

/*----------------------------------------------------*/

vec3 norml = normalize(cross(dFdx(cpos), dFdy(cpos)));

mat3 TBN = mat3(
    abs(norml.y) + norml.z, 0.0, norml.x,
    0.0, 0.0, norml.y,
    -norml.x, norml.y, norml.z);

vec3 normap = normals(s_MatTexture,v_texcoord0);
norml.xy = normap.xy;
norml.z = inversesqrt(dot(norml.xy, norml.xy));
norml = mul(normap, TBN);

vec3 rfpos = reflect(normalize(wpos), norml);

#if defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY)
    diffuse.rgb = vec3(1.0, 1.0, 1.0);
#else
    diffuse = texture2D(s_MatTexture, v_texcoord0);

#if defined(ALPHA_TEST)
    if (diffuse.a < 0.5) {
        discard;
    }
#endif
/*
#if defined(SEASONS) && (defined(OPAQUE) || defined(ALPHA_TEST))
    diffuse.rgb *=
        mix(vec3(1.0, 1.0, 1.0),
            texture2D(s_SeasonsTexture, v_color0.xy).rgb * 2.0, v_color0.b);
    diffuse.rgb *= v_color0.aaa;
#else
*/
    diffuse.rgb *= removeAO * cunstructAO;
//#endif
#endif

if (alpha > 0.03 && alpha < 0.06) {
	diffuse.rgb *= max(diffuse.rgb, ((3.5 * (0.06 - alpha)) / 0.007499993));
	diffuse.rgb *= diffuse.rgb;
}

#ifndef TRANSPARENT
    diffuse.a = 1.0;
#endif

  if (Metals < 1.0) {
  float lightx = pow(v_lightmapUV.x, 1.5);
  vec4 light = texture2D(s_LightMapTexture,vec2(lightx,v_lightmapUV.y));
  diffuse.rgb *= n_fix(sat(light.rgb,0.0));
  diffuse.rgb *= worldcolor;
  }

  vec3 shadC = mix(vec3(.8,.95,1.)*.7, vec3(.75,.8,1.)*.9, worldtime.z);
  diffuse.rgb *= mix(mix(vec3(1.,1.,1.), shadC, findcave.x * inv1(findcave.y)), vec3(1.,1.,1.), wlit);








  float dp1 = cl(abs(norml.x));
	vec3 cc1 = mix(vec3(1., .95, .85) + .7, vec3(1., .5, .67) + 0.95, worldtime.y);
	cc1 = mix(cc1, vec3(.8, .8, .8), worldtime.z);
	cc1 = mix(cc1, vec3(1.,1.,1.), worldtime.w);
	diffuse.rgb *= mix(vec3(1.,1.,1.), cc1, dp1 * v_lightmapUV.y * nlit);

  vec3 tcc1 = mix(vec3(1.0, 0.8, 0.45), vec3(1.0, 0.5, 0.16), inv1(v_lightmapUV.y));
  diffuse.rgb += mix((diffuse.rgb * tcc1 * wlit), diffuse.rgb * tcc1 * pow(v_lightmapUV.x*1.1, 15.0), (nshd));


// F_Rays
  vec3 raycol = mix(vec3(1.,1.,1.),vec3(1.,.25,.25), worldtime.y);
  float nse = noise(vec2(atan(wpos.z/wpos.y)*6.5, atan(wpos.z/wpos.y)*6.5));
  float raypos = smoothstep(.1,3.4, nse*length(wpos)/17.);
  float raycl = clamp(length(wpos.zy)*.08,0.,1.);
  diffuse.rgb = mix(diffuse.rgb,raycol,raypos*.3*raycl*worldtime.y*inv1(worldtime.z)*v_lightmapUV.y);

// F_Bloom
  diffuse.rgb = mix(diffuse.rgb, sunbloom.rgb, sunbloom.a);

	diffuse.rgb = tone(diffuse.rgb);
	diffuse.rgb = sat(diffuse.rgb, mix(1.1, 0.58, worldtime.w));

float stars = step(length(fract(rfpos.xz * 109.) - 0.5), 0.24) * step((fract(sin(dot(floor(rfpos.xz * 109.), vec2(15, 55))) * 15.0)), 0.00008);
if (Metals > 0.5) {
// B_Gradient
  diffuse.rgb *= mix(diffuse.rgb, dS(diffuse.rgb, rfpos, vec3(worldtime.w,worldtime.y,worldtime.z))*1.15, v_lightmapUV.y);

// B_Clouds
  diffuse.rgb = generateCloud1(diffuse.rgb, rfpos, vec3(worldtime.w,worldtime.y,worldtime.z));

// B_Stars
  diffuse.rgb += mix(0.0, stars, max(worldtime.z, worldtime.y) * (1.0-worldtime.w));
}

// R_Sun
if (Roughs > 0.5) {
  float cve3 = smoothstep(0.88, 0.8, v_lightmapUV.y);
  diffuse = roughsun(diffuse, 50.0, norml, wpos, vec3(worldtime.w,worldtime.y,worldtime.z), cve3);
}

#ifdef TRANSPARENT
if (wflag > 0.0) {
// W_Gradient
diffuse = wFunc(diffuse, wflag, norml, cpos, wpos, v_lightmapUV, vec3(worldtime.w,worldtime.y,worldtime.z), s_MatTexture, v_texcoord0, ViewPositionAndTime.w);

// W_SunReflect
diffuse = sunRef(diffuse, 190.0, norml, wpos, cpos, vec3(worldtime.w,worldtime.y,worldtime.z), v_lightmapUV);

// W_Clouds
diffuse.rgb = generateCloud1(diffuse.rgb, rfpos, vec3(worldtime.w,worldtime.y,worldtime.z));

// W_Stars
diffuse.rgb += mix(0.0, stars, max(worldtime.z, worldtime.y) * (1.0-worldtime.w));

}
#endif

if (FogAndDistanceControl.x < 0.01) {
	diffuse.rgb = mix(diffuse.rgb, vec3(.35, .7, .8) * 0.75, v_fog.a);
} else {
	diffuse.rgb = mix(diffuse.rgb, dS(diffuse.rgb, normalize(wpos), vec3(smoothstep(.66, .3, FogAndDistanceControl.x), worldtime.y, worldtime.z)), fogplacement);
}
    gl_FragColor = diffuse;
}