#version 330

uniform sampler2D texture_sampler;

in vec2 vs_out_uv;
out vec4 fs_out_color;

void main(){
    float depth=texture(texture_sampler,vs_out_uv).r;
    fs_out_color=vec4(vec3(depth),1.0);
}
