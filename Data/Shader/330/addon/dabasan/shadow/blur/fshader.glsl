#version 330

uniform sampler2D texture_sampler;
uniform ivec2 texture_size;
uniform int direction;
const int DIRECTION_HORIZONTAL=0;
const int DIRECTION_VERTICAL=1;

const int WEIGHT_NUM=3;
const float[WEIGHT_NUM] weights=float[](0.250301,0.221461,0.153388);

in vec2 vs_out_uv;
out vec4 fs_out_color;

float HorizontalBlur(){
    float ret=0.0;
    vec2 texel_size=1.0/texture_size;
    
    for(int i=1;i<WEIGHT_NUM;i++){
        ret+=texture(texture_sampler,vs_out_uv+vec2(texel_size.x*float(-i),0.0)).r*weights[i];
    }
    ret+=texture(texture_sampler,vs_out_uv).r*weights[0];
    for(int i=1;i<WEIGHT_NUM;i++){
        ret+=texture(texture_sampler,vs_out_uv+vec2(texel_size.x*float(i),0.0)).r*weights[i];
    }

    return ret;
}
float VerticalBlur(){
    float ret=0.0;
    vec2 texel_size=1.0/texture_size;
    
    for(int i=1;i<WEIGHT_NUM;i++){
        ret+=texture(texture_sampler,vs_out_uv+vec2(0.0,texel_size.y*float(-i))).r*weights[i];
    }
    ret+=texture(texture_sampler,vs_out_uv).r*weights[0];
    for(int i=1;i<WEIGHT_NUM;i++){
        ret+=texture(texture_sampler,vs_out_uv+vec2(0.0,texel_size.y*float(i))).r*weights[i];
    }

    return ret;
}

void main(){
    float factor=(direction==DIRECTION_HORIZONTAL)?HorizontalBlur():VerticalBlur();
    fs_out_color=vec4(factor,0.0,0.0,1.0);
}
