$input a_color0, a_position, a_texcoord0, a_texcoord1
#ifdef INSTANCING
    $input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0, v_fog, v_texcoord0, v_lightmapUV, wpos, cpos, fogplacement, worldcolor, worldtime, findcave, cunstructAO, nshd, removeAO, sunbloom, wflag

#include <bgfx_shader.sh>

uniform vec4 RenderChunkFogAlpha;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
uniform vec4 FogColor;

#include <azify/def.h>

#define nlit inv1(pow(a_texcoord1.x, 2.0))
#define wlit pow(a_texcoord1.x, 2.0)

vec3 ao(vec3 x, vec3 vcol) {
    vec3 ncol = normalize(vcol);
    return mix(x, x * ncol, step(0.001, length(ncol - vec3(ncol.y))));}

float drk(vec4 c, vec2 uv) {
    float shd = smoothstep(0.885, 0.71, c.y + 0.2 * (c.y - c.z));
    return mix(shd, 0.0, pow(uv.x, 3.0));}

void main() {
    mat4 model;
#ifdef INSTANCING
    model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
#else
    model = u_model[0];
#endif

    vec3 worldPos = mul(model, vec4(a_position, 1.0)).xyz;
    vec4 color;
#ifdef RENDER_AS_BILLBOARDS
    worldPos += vec3(0.5, 0.5, 0.5);
    vec3 viewDir = normalize(worldPos - ViewPositionAndTime.xyz);
    vec3 boardPlane = normalize(vec3(viewDir.z, 0.0, -viewDir.x));
    worldPos = (worldPos -
        ((((viewDir.yzx * boardPlane.zxy) - (viewDir.zxy * boardPlane.yzx)) *
        (a_color0.z - 0.5)) +
        (boardPlane * (a_color0.x - 0.5))));
    color = vec4(1.0, 1.0, 1.0, 1.0);
#else
    color = a_color0;
#endif

    vec3 modelCamPos = (ViewPositionAndTime.xyz - worldPos);
    float camDis = length(modelCamPos);
    vec4 fogColor;
    fogColor.rgb = FogColor.rgb;
    fogColor.a = clamp(((((camDis / FogAndDistanceControl.z) + RenderChunkFogAlpha.x) -
        FogAndDistanceControl.x) / (FogAndDistanceControl.y - FogAndDistanceControl.x)), 0.0, 1.0);
/*
#ifdef TRANSPARENT
    if(a_color0.a < 0.95) {
        color.a = mix(a_color0.a, 1.0, clamp((camDis / FogAndDistanceControl.w), 0.0, 1.0));
    };
#endif
*/
float day = pow(max(min(1.0-FogColor.r*1.2,1.),0.),.4);
float night = pow(max(min(1.-FogColor.r*1.5,1.),0.),1.2);
float dusk = max(FogColor.r-FogColor.b,0.);
float rain = mix(smoothstep(.66,.3, FogAndDistanceControl.x),0.,step(FogAndDistanceControl.x,0.))*inv1(smoothstep(.8,.2,a_texcoord1.y));
worldtime = vec4(day,dusk,night,rain);






float cve1 = smoothstep(.95,.9,a_texcoord1.y);
float cve2 = smoothstep(.7,.01,a_texcoord1.y);
findcave = vec2(cve1,cve2);

float findAO = inv1((color.g * 2.0 - min(color.r, color.b)) * 1.4);
vec3 newAO = mix(mix(vec3(1.,1.,1.), mix(mix(vec3(.8,.95,1.)*.25, vec3(.75,.8,1.)*.2, night), vec3(.88,.88,.88), rain), mix(0.5, 1.0, findAO)), vec3(.95,.9,.85), pow(a_texcoord1.x,5.));
cunstructAO = newAO;

removeAO = ao(vec3(1.,1.,1.), color.rgb);

nshd = drk(color, a_texcoord1);

vec3 wcc = mix(vec3(1.,.95,.89), vec3(.44,.25,.34), dusk);
  wcc = mix(wcc, vec3(.35,.4,.45), night);
  wcc = mix(wcc, mix(vec3(.6,.6,.6), vec3(.4,.4,.4), night), rain);
  wcc = mix(wcc, vec3(.66,.7,.8), inv1(pow(a_texcoord1.y, 1.3)));
  wcc = mix(wcc, vec3(.9,.9,.9), wlit);
worldcolor = wcc;


float dist1 = 1.3;
float fp0 = clamp((-worldPos.y * dist1) / FogAndDistanceControl.w, 0.0, 1.0);
float fp1 = clamp(length(worldPos.xyz * 0.6) / FogAndDistanceControl.w, 0.0, 1.0);
float fp3 = fp1*fp0;
fogplacement = max(fogColor.a,fp3);

float invWorldPosX = 1./worldPos.x;
float sunlength = length(worldPos.zy*5.*invWorldPosX);
float gaussian = clamp(1.-sunlength,.0,1.);
float bloomFactor = smoothstep(0.0,1.0,gaussian)*3.5*(1.-night)*(1.-rain)*(dusk*a_texcoord1.y)*fogColor.a;
vec3 bloomColor = vec3(1.,.65,.4);
sunbloom = vec4(bloomColor,bloomFactor);

wflag = 0.0;
#ifdef TRANSPARENT
if (a_color0.r != a_color0.g || a_color0.g != a_color0.b || a_color0.r != a_color0.b) {
	wflag = 1.0;
}
#endif

    v_texcoord0 = a_texcoord0;
    v_lightmapUV = a_texcoord1;
    v_color0 = color;
    v_fog = fogColor;
    wpos = worldPos;
    cpos = a_position;
    gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
}
