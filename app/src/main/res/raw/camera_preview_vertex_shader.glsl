attribute vec4 position;
attribute vec2 textureCoordinate;

uniform mat4 projection;

varying vec2 vertextTextureCoordinate;

void main() {
	vertextTextureCoordinate = textureCoordinate;
	gl_Position = projection * position;
}
