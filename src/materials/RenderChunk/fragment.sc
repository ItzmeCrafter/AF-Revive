$input v_color0, v_fog, v_texcoord0, v_lightmapUV, wpos, cpos, fogplacement, worldcolor, worldtime, findcave, cunstructAO, nshd, removeAO, sunbloom, wflag, endflag, mainSky, endfog

#include <bgfx_shader.sh>

uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

SAMPLER2D_AUTOREG(s_MatTexture);
SAMPLER2D_AUTOREG(s_SeasonsTexture);
SAMPLER2D_AUTOREG(s_LightMapTexture);

#include <azify/func.sh>

#define nlit inv1(v_lightmapUV.x * v_lightmapUV.x)
#define wlit v_lightmapUV.x * v_lightmapUV.x
#define pbr_Strength 0.0004

vec3 getTangentVector(vec3 n) {
vec3 t = vec3(0, 0, 0);
if(n.x > 0.0) {
t = vec3(0, 0, -1);
} else if(n.x < -0.5) {
t = vec3(0, 0, 1);
} else if(n.y > 0.0) {
t = vec3(1, 0, 0);
} else if(n.y < -0.5) {
t = vec3(1, 0, 0);
} else if(n.z > 0.0) {
t = vec3(1, 0, 0);
} else if(n.z < -0.5) {
t = vec3(-1, 0, 0);
} return t;
}

vec4 roughsun(vec4 c, float s, vec3 P, vec3 wp, vec3 wt, float cv) {
    vec3 SC = mix(vec3(0.9, 0.7, 0.3), vec3(0.0, 0.05, 0.1), wt.z);
    vec3 bp = normalize(-wp);
    vec3 sp[3] = vec3[3](
        vec3(0.89, 0.259, 0.0),
        vec3(-0.968, 0.295, 0.0),
        vec3(0.968, 0.295, 0.0)
    );
    float sFactors[3];
    for (int i = 0; i < 3; i++) {
        sFactors[i] = cl(pow(max(0.0, dot(P, normalize(bp + sp[i]))), s));
    }
    float invCv = inv1(cv);
    float factor = (1.0 - wt.x) * invCv;
    vec4 SC4 = vec4(SC, 1.0);
    c += SC4 * (sFactors[0] * (1.0 - wt.y) * factor);
    c += SC4 * 0.5 * (sFactors[1] * wt.y * factor);
    c += SC4 * (sFactors[2] * wt.y * factor);
    return c;
}

vec4 sunRef(vec4 c, float s, vec3 P, vec3 wp, vec3 cp, vec3 wt, vec2 l) {
    vec3 SC = mix(vec3(1.4, 1.2, 0.9), vec3(0.9, 0.9, 0.9), wt.z);
    vec3 bp = normalize(-wp);
    vec3 sp[3] = vec3[3](
        vec3(0.89, 0.159, 0.0),
        vec3(-0.968, 0.295, 0.0),
        vec3(0.968, 0.295, 0.0)
    );
    float sFactors[3];
    for (int i = 0; i < 3; i++) {
        sFactors[i] = cl(pow(max(0.0, dot(P, normalize(bp + sp[i]))), s));
    }
    float wave = sin(wp.x * 0.2 + ViewPositionAndTime.x * 2.0) * 0.9 +  
                 sin(wp.z * 0.2 + ViewPositionAndTime.x * 2.0) * 0.7;  
    float groundNoise = noise(vec2(cp.x * 0.8 + ViewPositionAndTime.w * 2.0, cp.z * 0.8) + wave);
    float lightFactor = (1.0 - wt.x) * l.y;
    vec4 SC4 = vec4(SC, 1.0);
    c += SC4 * (sFactors[0] * (1.0 - wt.y) * lightFactor) * groundNoise;
    c += SC4 * 1.9 * (sFactors[1] + sFactors[2]) * wt.y * lightFactor * groundNoise;
    return c;
}


lowp vec4 wFunc(lowp vec4 c, lowp float w, lowp vec3 P, lowp vec3 Cp, lowp vec3 Wp, lowp vec2 l, lowp vec3 wt, lowp vec3 sky) {
  lowp vec3 wn = normalize(Wp);
  lowp vec3 vD = normalize(-Wp);
  lowp float NoV = max(0.0, dot(P, vD));
  lowp float F = smoothstep(0.38, 0.0, NoV);
  lowp vec3 Rp = reflect(normalize(Wp), P);
  lowp float L = pow(1.0 - abs(Rp.y), 3.0);

	lowp vec3 wc = sky;
	lowp vec3 wb = mix(c.rgb, wc, clamp(L, 0.0, 1.0));
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

float alpha = texture2DLod(s_MatTexture, v_texcoord0, 0.0).a;
float Metals = (alpha > 0.95 && alpha < 1.0) ? 1.0 : 0.0;
float Roughs = (alpha > 0.92 && alpha < 0.94) ? 1.0 : 0.0;

vec3 norml = normalize(cross(dFdx(cpos), dFdy(cpos)));

mat3 TBN = mat3(abs(norml.y) + norml.z, 0.0, norml.x, 0.0, 0.0, norml.y, -norml.x, norml.y, norml.z);

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

	diffuse.rgb *= removeAO * cunstructAO;
#endif

if (alpha > 0.03 && alpha < 0.06) {
	diffuse.rgb *= max(diffuse.rgb, ((3.5 * (0.06 - alpha)) / 0.007499993));
	diffuse.rgb *= diffuse.rgb;
}

#ifndef TRANSPARENT
    diffuse.a = 1.0;
#endif

  float lightx = pow(v_lightmapUV.x, 1.5);
  vec4 light = texture2D(s_LightMapTexture,vec2(lightx,v_lightmapUV.y));
  diffuse.rgb *= n_fix(sat(light.rgb,0.0));
  diffuse.rgb *= worldcolor;
  

  vec3 shadC = mix(vec3(.8,.95,1.)*.7, vec3(.75,.8,1.)*.9, worldtime.z);
  diffuse.rgb *= mix(mix(vec3(1.,1.,1.), shadC, findcave.x * inv1(findcave.y)), vec3(1.,1.,1.), wlit);

  vec3 tcc1 = mix(vec3(1.0, 0.8, 0.45), vec3(1.0, 0.5, 0.16), inv1(v_lightmapUV.y));
  diffuse.rgb += mix((diffuse.rgb * tcc1 * wlit), diffuse.rgb * tcc1 * pow(v_lightmapUV.x*1.1, 15.0), nshd);


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
  diffuse.rgb *= mix(diffuse.rgb, mainSky.rgb*1.15, v_lightmapUV.y);

// B_Clouds
  diffuse.rgb = generateCloud1(diffuse.rgb, rfpos, worldtime.wyz);

// B_Stars
  diffuse.rgb += mix(0.0, stars, max(worldtime.z, worldtime.y) * (1.0-worldtime.w));
}

// R_Sun
if (Roughs > 0.5) {
  float cve3 = smoothstep(0.88, 0.8, v_lightmapUV.y);
  diffuse = roughsun(diffuse, 50.0, norml, wpos, worldtime.wyz, cve3);
}


if (wflag > 0.5) {
// W_Gradient
diffuse = wFunc(diffuse, wflag, norml, cpos, wpos, v_lightmapUV, worldtime.wyz, mainSky);

// W_SunReflect
diffuse = sunRef(diffuse, 190.0, norml, wpos, cpos, worldtime.wyz, v_lightmapUV);

// W_Clouds
diffuse.rgb = mix(diffuse.rgb, generateCloud1(diffuse.rgb, rfpos, worldtime.wyz), v_lightmapUV.y);

// W_Stars
diffuse.rgb += mix(0.0, stars, max(worldtime.z, worldtime.y) * (1.0-worldtime.w));

}



if (endflag > 0.5) {
	diffuse.rgb *= mix(vec3(0.28,0.27,0.28), vec3(1.,1.,1.), pow(v_lightmapUV.x,5.));
	diffuse.rgb = sat(diffuse.rgb, 0.6);
	diffuse.rgb = mix(diffuse.rgb, endfog.rgb, endfog.a);
}else{
if (FogAndDistanceControl.x < 0.01) {
	diffuse.rgb = mix(diffuse.rgb, vec3(.35, .7, .8) * 0.75, v_fog.a);
} else {
	diffuse.rgb = mix(diffuse.rgb, mainSky, fogplacement);
}}
    gl_FragColor = diffuse;
}