#version 330

struct Camera{
    vec3 position;
    vec3 target;
    mat4 projection;
    mat4 view_transformation;
    float near;
    float far;
};
struct LightInfo{
    int projection_type;
    mat4 depth_bias_mvp;
};

uniform Camera camera;
uniform float normal_offset;

const int MAX_LIGHT_NUM=16;
uniform LightInfo lights[MAX_LIGHT_NUM];
uniform int light_num;

layout(location=0) in vec3 vs_in_position;
layout(location=1) in vec2 vs_in_uv;
layout(location=2) in vec3 vs_in_normal;
out vec3 vs_out_position;
out vec2 vs_out_uv;
out vec4 shadow_coords[MAX_LIGHT_NUM];

void main(){
    mat4 camera_matrix=camera.projection*camera.view_transformation;
    gl_Position=camera_matrix*vec4(vs_in_position,1.0);
    vs_out_position=vs_in_position;
    vs_out_uv=vs_in_uv;

    int bound=min(light_num,MAX_LIGHT_NUM);
    for(int i=0;i<bound;i++){
        shadow_coords[i]=lights[i].depth_bias_mvp*vec4(vs_in_position+vs_in_normal*normal_offset,1.0);
    }
}
