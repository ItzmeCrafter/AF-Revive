$input v_color0, v_fog, v_light, v_texcoord0, wpos, worldtime, worldcolor, fogplacement

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/ActorUtil.dragonh>
#include <MinecraftRenderer.Materials/FogUtil.dragonh>

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
uniform vec4 LightDiffuseColorAndIlluminance;
uniform vec4 LightWorldSpaceDirection;
uniform vec4 HudOpacity;
uniform vec4 UVAnimation;
uniform mat4 Bones[8];
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

SAMPLER2D_AUTOREG(s_MatTexture);
SAMPLER2D_AUTOREG(s_MatTexture1);

#include <azify/def.h>
#include <azify/noise.h>
#include <azify/tone.h>
#include <azify/sky.h>

#define nlit inv1(pow(v_texcoord0.x, 2.0))
#define wlit pow(v_texcoord0.x, 2.0)

void main() {
    #if DEPTH_ONLY
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
        return;
    #elif DEPTH_ONLY_OPAQUE
        gl_FragColor = vec4(applyFog(vec3(1.0, 1.0, 1.0), v_fog.rgb, v_fog.a), 1.0);
        return;
    #endif

    vec4 albedo = getActorAlbedoNoColorChange(v_texcoord0, s_MatTexture, s_MatTexture1, MatColor);

    #if ALPHA_TEST
        float alpha = mix(albedo.a, (albedo.a * OverlayColor.a), TintedAlphaTestEnabled.x);
        if(shouldDiscard(albedo.rgb, alpha, ActorFPEpsilon.x))
            discard;
    #endif // ALPHA_TEST

    #if CHANGE_COLOR_MULTI
        albedo = applyMultiColorChange(albedo, ChangeColor.rgb, MultiplicativeTintColor.rgb);
    #elif CHANGE_COLOR
        albedo = applyColorChange(albedo, ChangeColor, albedo.a);
        albedo.a *= ChangeColor.a;
    #endif // CHANGE_COLOR_MULTI

    #if ALPHA_TEST
        albedo.a = max(UseAlphaRewrite.r, albedo.a);
    #endif

float day = worldtime.x;
float night = worldtime.z;
float dusk = worldtime.y;
float rain = worldtime.w;

float yq = lum(v_light.rgb);
albedo = applyActorDiffuse(albedo, v_color0.rgb, vec4(n_fix(sat(v_light.rgb,0.0)),v_light.a), ColorBased.x, OverlayColor);
albedo.rgb *= worldcolor;

//diffuse.rgb *= night_fix(sat(v_light.rgb,0.0));

albedo.rgb = tone(albedo.rgb);
albedo.rgb = sat(albedo.rgb, mix(1.1, 0.58, rain));

    #if TRANSPARENT
        albedo = applyHudOpacity(albedo, HudOpacity.x);
    #endif

if (FogAndDistanceControl.x < 0.01) {
// F_FogWater
  albedo.rgb = mix(albedo.rgb,vec3(.35,.7,.8) * 0.75, v_fog.a);
} else {
// F_FogWorld
  albedo.rgb = mix(albedo.rgb, dS(albedo.rgb, normalize(wpos), vec3(smoothstep(.66,.3, FogAndDistanceControl.x),dusk,night)), fogplacement);
}

    //albedo.rgb = applyFog(albedo.rgb, v_fog.rgb, v_fog.a);
    
    gl_FragColor = albedo;
}
