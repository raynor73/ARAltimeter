attribute vec4 position;
attribute vec2 textureCoordinate;

varying vec2 vertextTextureCoordinate;

void main() {
	vertextTextureCoordinate = textureCoordinate;
	gl_Position = position;
}
