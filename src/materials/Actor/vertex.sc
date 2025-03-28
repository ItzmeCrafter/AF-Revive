$input a_position, a_color0, a_texcoord0, a_indices, a_normal
#ifdef INSTANCING
    $input i_data0, i_data1, i_data2
#endif

$output v_color0, v_fog, v_light, v_texcoord0, wpos, worldtime, worldcolor, fogplacement

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/FogUtil.dragonh>
#include <MinecraftRenderer.Materials/DynamicUtil.dragonh>
#include <MinecraftRenderer.Materials/TAAUtil.dragonh>

uniform vec4 ColorBased;
uniform vec4 ChangeColor;
uniform vec4 UseAlphaRewrite;
uniform vec4 TintedAlphaTestEnabled;
uniform vec4 MatColor;
uniform vec4 OverlayColor;
uniform vec4 TileLightColor;
uniform vec4 MultiplicativeTintColor;
uniform vec4 FogColor;
uniform vec4 FogControl;
uniform vec4 ActorFPEpsilon;
uniform vec4 LightDiffuseColorAndIntensity;
uniform vec4 LightWorldSpaceDirection;
uniform vec4 HudOpacity;
uniform vec4 UVAnimation;
uniform mat4 Bones[8];
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

#define nlit inv1(pow(a_texcoord0.x, 2.0))
//#define wlit pow(a_texcoord0.x, 2.0)

void main() {
    mat4 World = u_model[0];
    
    //StandardTemplate_InvokeVertexPreprocessFunction
    World = mul(World, Bones[int(a_indices)]);

    vec2 texcoord0 = a_texcoord0;
    texcoord0 = applyUvAnimation(texcoord0, UVAnimation);

    float lightIntensity = calculateLightIntensity(World, vec4(a_normal.xyz, 0.0), TileLightColor);
    lightIntensity += OverlayColor.a * 0.35;
    vec4 light = vec4(lightIntensity * TileLightColor.rgb, 1.0);
    
    //StandardTemplate_VertSharedTransform
    vec3 worldPosition;
    #ifdef INSTANCING
        mat4 model;
        model[0] = vec4(i_data0.x, i_data1.x, i_data2.x, 0);
        model[1] = vec4(i_data0.y, i_data1.y, i_data2.y, 0);
        model[2] = vec4(i_data0.z, i_data1.z, i_data2.z, 0);
        model[3] = vec4(i_data0.w, i_data1.w, i_data2.w, 1);
        worldPosition = instMul(model, vec4(a_position, 1.0)).xyz;
    #else
        worldPosition = mul(World, vec4(a_position, 1.0)).xyz;
    #endif
    
    vec4 position;// = mul(u_viewProj, vec4(worldPosition, 1.0));

    //StandardTemplate_InvokeVertexOverrideFunction
    position = jitterVertexPosition(worldPosition);
    float cameraDepth = position.z;
    float fogIntensity = calculateFogIntensity(cameraDepth, FogControl.z, FogControl.x, FogControl.y);
    vec4 fog = vec4(FogColor.rgb, fogIntensity);

    #if defined(DEPTH_ONLY)
        v_texcoord0 = vec2(0.0, 0.0);
        v_color0 = vec4(0.0, 0.0, 0.0, 0.0);
    #else
        v_texcoord0 = texcoord0;
        v_color0 = a_color0;
    #endif

float day = pow(max(min(1.0-FogColor.r*1.2,1.),0.),.4);
float night = pow(max(min(1.-FogColor.r*1.5,1.),0.),1.2);
float dusk = max(FogColor.r-FogColor.b,0.);
float rain = mix(smoothstep(.66,.3, FogAndDistanceControl.x),0.,step(FogAndDistanceControl.x,0.));
worldtime = vec4(day,dusk,night,rain);

vec3 wcc = mix(vec3(1.,.95,.89), vec3(.44,.25,.34), dusk);
  wcc = mix(wcc, vec3(.35,.4,.45), night);
  wcc = mix(wcc, mix(vec3(.6,.6,.6), vec3(.4,.4,.4), night), rain);
  //wcc = mix(wcc, vec3(.66,.7,.8), inv1(pow(a_texcoord1.y, 1.3)));
  wcc = mix(wcc, vec3(1.,.95,.9), pow(light.x, 2.0));
worldcolor = wcc;

float dist1 = 1.3;
float fp0 = clamp((-worldPosition.y * dist1) / FogAndDistanceControl.w, 0.0, 1.0);
float fp1 = clamp(length(worldPosition.xyz * 0.6) / FogAndDistanceControl.w, 0.0, 1.0);
float fp3 = fp1*fp0;
fogplacement = max(fog.a,fp3);

    v_fog = fog; 
    v_light = light;
    wpos = worldPosition;
    gl_Position = position;
}
