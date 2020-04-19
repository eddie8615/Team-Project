#version 150 core

precision mediump float;
uniform sampler2D Texture;

in vec4 pass_Color;
in vec2 pass_TextureCoord;

out vec4 out_Color;

void main(){
    out_Color = pass_Color * texture(Texture, pass_TextureCoord.st);
}
