#version 330

uniform mat4 depth_mvp;

layout(location=0) in vec3 vs_in_position;
//layout(location=1) in vec2 vs_in_uv;
//layout(location=2) in vec3 vs_in_normal;

void main(){
    gl_Position=depth_mvp*vec4(vs_in_position,1.0);
}
