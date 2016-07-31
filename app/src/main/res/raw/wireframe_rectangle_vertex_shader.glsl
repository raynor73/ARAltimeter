attribute vec4 position;

uniform mat4 projection;
uniform mat4 modelView;

void main(void) {
	gl_Position = projection * modelView * position;
}
