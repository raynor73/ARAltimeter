attribute vec4 position;
attribute vec2 textureCoordinate;

uniform mat4 projection;
uniform mat4 modelView;

varying vec2 vertextTextureCoordinate;

void main() {
	vertextTextureCoordinate = textureCoordinate;
	gl_Position = projection * modelView * position;
}
