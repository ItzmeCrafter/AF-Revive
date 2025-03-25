#ifndef INSTANCING
$input a_color0, a_position

$output v_color0, skypos

#include <bgfx_shader.sh>

uniform vec4 SkyColor;
uniform vec4 FogColor;
#endif

void main() {
#ifndef INSTANCING
    //Opaque
  vec4 pos = vec4(a_position,1.);
  pos.xzw = pos.xzw;
  pos.y = (a_position.y - sqrt(dot (a_position.xz, a_position.xz) * 17.5));

  skypos = (pos.xyz + vec3(0.0, 0.128, 0.0));
    v_color0 = mix(SkyColor, FogColor, a_color0.x);
    gl_Position = mul(u_modelViewProj, vec4(pos));
#else
  gl_FragColor = vec4(0.0,0.0,0.0,0.0);
#endif
}