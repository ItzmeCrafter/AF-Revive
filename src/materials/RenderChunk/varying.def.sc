vec4 a_color0     : COLOR0;
vec3 a_position   : POSITION;
vec2 a_texcoord0  : TEXCOORD0;
vec2 a_texcoord1  : TEXCOORD1;

vec4 i_data0      : TEXCOORD8;
vec4 i_data1      : TEXCOORD7;
vec4 i_data2      : TEXCOORD6;
vec4 i_data3      : TEXCOORD5;

vec4          v_color0     : COLOR0;
vec4          v_fog        : COLOR2;
centroid vec2 v_texcoord0  : TEXCOORD0;
vec2          v_lightmapUV : TEXCOORD1;

vec3 wpos : POSITION1;
vec3 cpos : POSITION2;

float fogplacement : COLOR3;
vec3 worldcolor : COLOR4;
vec4 worldtime : COLOR5;
vec2 findcave : COLOR6;
vec3 cunstructAO : COLOR7;
float nshd : COLOR8;
vec3 removeAO : COLOR9;
vec4 sunbloom : COLOR10;
float wflag : COLOR11;
