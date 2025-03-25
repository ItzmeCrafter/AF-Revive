#ifndef INSTANCING
$input v_color0, skypos
#endif

#include <bgfx_shader.sh>

uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform vec4 ViewPositionAndTime;
uniform highp vec4 FogAndDistanceControl;

#include <azify/sky.h>

void main() {
#ifndef INSTANCING
    //Opaque
  vec3 basepos_1;
    basepos_1 = normalize(skypos);
    float day = pow(max(min(1.0 - FogColor.r * 1.2, 1.0), 0.0), 0.4);
		float night = pow(max(min(1.0 - FogColor.r * 1.5, 1.0), 0.0), 1.2);
		float dusk = max(FogColor.r - FogColor.b, 0.0);
		float rain = mix(smoothstep(0.66, 0.3, FogAndDistanceControl.x), 0.0, step(FogAndDistanceControl.x, 0.0));


  float stars;
  stars = step(length(fract(basepos_1.xz * 109.) - 0.5), 0.24) * step((fract(sin(dot(floor(basepos_1.xz * 109.), vec2(15, 55))) * 15.0)), 0.00008);
  vec3 color;
  color = vec3(0,0,0);
  color += dS(color.rgb, basepos_1, vec3(rain,dusk,night));
  vec3 cl2 = generateCloud1(color, basepos_1, vec3(rain,dusk,night));
  color = clamp(cl2, 0.,1.);
  if ((basepos_1.y > 0.0)) {
  color.rgb += mix(0.0, stars, max(night, dusk) * (1.0-rain));
  }
  if(FogAndDistanceControl.x <= 0.0 && FogColor.b > FogColor.r){
    color.rgb = vec3(.35,.7,.8);
  }

    gl_FragColor = vec4(color,1.);
#else
  gl_FragColor = vec4(0.0,0.0,0.0,0.0);
#endif
}