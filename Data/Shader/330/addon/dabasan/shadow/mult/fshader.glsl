#version 330

uniform sampler2D texture_sampler;
uniform sampler2D shadow_factors;

in vec2 vs_out_uv;
out vec4 fs_out_color;

void main(){
    fs_out_color=texture(texture_sampler,vs_out_uv)*texture(shadow_factors,vs_out_uv).r;
}
