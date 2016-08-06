package org.ilapin.araltimeter;

import org.ilapin.araltimeter.graphics.Camera;
import org.ilapin.araltimeter.graphics.WithShaders;

public interface Scene extends WithShaders {

	Camera getActiveCamera();

	void setViewportSize(final int width, final int height);

	void render();
}
