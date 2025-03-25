$input a_position, a_texcoord0
$output v_texcoord0

#include <bgfx_shader.sh>

void main() {
    v_texcoord0 = a_texcoord0;
    vec3 pos = a_position;
    pos.xz *= .8;
    float angle = radians(45.0);
    float s = sin(angle);
    float c = cos(angle);
    pos.xz = vec2(c*pos.x-s*pos.z,s*pos.x+c*pos.z);
    gl_Position = mul(u_modelViewProj, vec4(pos, 1.0));
}