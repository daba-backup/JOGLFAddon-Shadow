package com.github.dabasan.joglfaddon.shadow;

import static com.github.dabasan.basis.vector.VectorFunctions.*;
import static com.github.dabasan.joglf.gl.wrapper.GLWrapper.*;
import static com.jogamp.opengl.GL.*;

import java.util.ArrayList;
import java.util.List;

import com.github.dabasan.joglf.gl.input.keyboard.KeyboardEnum;
import com.github.dabasan.joglf.gl.input.mouse.MouseEnum;
import com.github.dabasan.joglf.gl.model.Model3DFunctions;
import com.github.dabasan.joglf.gl.util.camera.FreeCamera;
import com.github.dabasan.joglf.gl.util.screen.Screen;
import com.github.dabasan.joglf.gl.window.JOGLFWindow;

class ShadowMappingTestWindow extends JOGLFWindow {
	private int[] model_handles;
	private ShadowMapping shadow_mapping;
	private Screen screen;

	private FreeCamera camera;

	public ShadowMappingTestWindow() {
		super(1280, 720, "TestWindow", true);
	}

	@Override
	public void Init() {
		model_handles = new int[2];
		model_handles[0] = Model3DFunctions.LoadModel("./Data/Model/OBJ/Teapot/teapot.obj");
		model_handles[1] = Model3DFunctions.LoadModel("./Data/Model/OBJ/Plane/plane.obj");

		List<LightInfo> lights = new ArrayList<>();
		var light = new OrthographicLightInfo();
		light.SetPosition(VGet(-30.0f, 30.0f, -30.0f));
		light.SetTarget(VGet(0.0f, 0.0f, 0.0f));
		light.SetNearFar(5.0f, 200.0f);
		lights.add(light);

		shadow_mapping = new ShadowMapping(lights, 2048, 2048);
		shadow_mapping.AddDepthModel(model_handles[0]);
		shadow_mapping.AddShadowModel(model_handles[0]);
		shadow_mapping.AddShadowModel(model_handles[1]);

		camera = new FreeCamera();
		camera.SetPosition(VGet(35.0f, 35.0f, 35.0f));

		glDisable(GL_CULL_FACE);
	}

	@Override
	public void Reshape(int x, int y, int width, int height) {
		screen = new Screen(width, height);
	}

	@Override
	public void Update() {
		int front = this.GetKeyboardPressingCount(KeyboardEnum.KEY_W);
		int back = this.GetKeyboardPressingCount(KeyboardEnum.KEY_S);
		int right = this.GetKeyboardPressingCount(KeyboardEnum.KEY_D);
		int left = this.GetKeyboardPressingCount(KeyboardEnum.KEY_A);

		int diff_x;
		int diff_y;
		if (this.GetMousePressingCount(MouseEnum.MOUSE_MIDDLE) > 0) {
			diff_x = this.GetCursorDiffX();
			diff_y = this.GetCursorDiffY();
		} else {
			diff_x = 0;
			diff_y = 0;
		}

		camera.Translate(front, back, right, left);
		camera.Rotate(diff_x, diff_y);
		camera.Update();

		shadow_mapping.TransferCameraPropertiesToPrograms();
		shadow_mapping.Update();
	}

	@Override
	public void Draw() {
		// shadow_mapping.VisualizeDepthTexture(0, screen);
		shadow_mapping.CreateShadowedScene(screen, true);
		screen.Draw(0, 0, this.GetWidth(), this.GetHeight());
	}
}
