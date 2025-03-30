#ifndef INSTANCING
$input v_texcoord0, v_posTime
#endif

#include <bgfx_shader.sh>

uniform vec4 ViewPositionAndTime;

#ifndef INSTANCING
#include <azify/sky.h>

  SAMPLER2D_AUTOREG(s_SkyTexture);
#endif


void main() {
  #ifndef INSTANCING
    vec4 diffuse = texture2D(s_SkyTexture, v_texcoord0);
    vec3 color = diffuse.rgb;


vec3 spos = normalize(v_posTime.xyz);
color = gradientCircle(spos);

    gl_FragColor = vec4(color, 1.0);
  #else
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
  #endif
}
