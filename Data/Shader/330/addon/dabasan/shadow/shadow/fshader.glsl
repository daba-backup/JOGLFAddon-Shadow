#version 330

struct LightInfo{
    int projection_type;
    mat4 depth_bias_mvp;
};

const int MAX_LIGHT_NUM=16;
uniform LightInfo lights[MAX_LIGHT_NUM];
uniform int light_num;

const int PROJECTION_TYPE_ORTHOGRAPHIC=0;
const int PROJECTION_TYPE_PERSPECTIVE=1;

uniform sampler2D texture_sampler;
uniform sampler2D depth_textures[MAX_LIGHT_NUM];
uniform ivec2 depth_texture_size;
uniform float bias;
uniform float in_shadow_visibility;

uniform int integration_method;
const int INTEGRATION_MUL=0;
const int INTEGRATION_MIN=1;

in vec3 vs_out_position;
in vec2 vs_out_uv;
in vec4 shadow_coords[MAX_LIGHT_NUM];
out vec4 fs_out_color;

float PCFSampling(int index){
    float visibility=1.0;
    vec2 texel_size=1.0/depth_texture_size;

    if(lights[index].projection_type==PROJECTION_TYPE_ORTHOGRAPHIC){
        float visibility_sum=0.0;

        for(int x=-1;x<=1;x++){
            for(int y=-1;y<=1;y++){
                float pcf_depth=texture(depth_textures[index],shadow_coords[index].xy+vec2(x,y)*texel_size).r;
                if(pcf_depth<shadow_coords[index].z-bias){
                    visibility_sum+=in_shadow_visibility;
                }
                else{
                    visibility_sum+=1.0;
                }
            }
        }

        visibility=visibility_sum/9.0;
    }
    else if(lights[index].projection_type==PROJECTION_TYPE_PERSPECTIVE){
        float visibility_sum=0.0;

        for(int x=-1;x<=1;x++){
            for(int y=-1;y<=1;y++){
                float pcf_depth=texture(depth_textures[index],shadow_coords[index].xy/shadow_coords[index].w+vec2(x,y)*texel_size).r;
                if(pcf_depth<(shadow_coords[index].z-bias)/shadow_coords[index].w){
                    visibility_sum+=in_shadow_visibility;
                }
                else{
                    visibility_sum+=1.0;
                }
            }
        }

        visibility=visibility_sum/9.0;
    }

    return visibility;
}

void main(){
    float final_factor=1.0;

    int bound=min(light_num,MAX_LIGHT_NUM);
    for(int i=0;i<bound;i++){
        float factor=PCFSampling(i);
        if(integration_method==INTEGRATION_MUL){
            final_factor*=factor;
        }
        else{
            final_factor=min(final_factor,factor);
        }
    }

    float x_adj_final_factor=final_factor+dFdx(final_factor);
    float y_adj_final_factor=final_factor+dFdy(final_factor);
    float mean_final_factor=(final_factor+x_adj_final_factor+y_adj_final_factor)/3.0;

    fs_out_color=texture(texture_sampler,vs_out_uv)*mean_final_factor;
    fs_out_color.a=1.0;
}
