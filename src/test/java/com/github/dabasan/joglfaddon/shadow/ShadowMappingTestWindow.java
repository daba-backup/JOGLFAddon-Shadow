package com.github.dabasan.joglfaddon.shadow;

import static com.github.dabasan.basis.vector.VectorFunctions.*;

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
	private Screen screen_src;
	private Screen screen_dst;

	private FreeCamera camera;

	public ShadowMappingTestWindow() {
		super(1280, 720, "TestWindow", true);
	}

	@Override
	public void Init() {
		model_handles = new int[2];
		model_handles[0] = Model3DFunctions.LoadModel("./Data/Model/BD1/Cube/cube.bd1");
		model_handles[1] = Model3DFunctions.LoadModel("./Data/Model/OBJ/Plane/plane.obj");
		Model3DFunctions.TranslateModel(model_handles[0], VGet(0.0f, 10.0f, 0.0f));

		List<LightInfo> lights = new ArrayList<>();
		var light = new OrthographicLightInfo();
		light.SetPosition(VGet(-30.0f, 30.0f, -30.0f));
		light.SetTarget(VGet(0.0f, 0.0f, 0.0f));
		light.SetNearFar(5.0f, 100.0f);
		lights.add(light);

		shadow_mapping = new ShadowMapping(lights, 2048, 2048, this.GetWidth(), this.GetHeight());
		shadow_mapping.AddDepthModel(model_handles[0]);
		shadow_mapping.AddShadowModel(model_handles[0]);
		shadow_mapping.AddShadowModel(model_handles[1]);
		shadow_mapping.SetNormalOffset(0.01f);
		shadow_mapping.SetBiasCoefficient(0.0001f);

		camera = new FreeCamera();
		camera.SetPosition(VGet(35.0f, 35.0f, 35.0f));

		this.GetWindow().setResizable(false);
	}

	@Override
	public void Reshape(int x, int y, int width, int height) {
		if (screen_src != null) {
			screen_src.Dispose();
		}
		if (screen_dst != null) {
			screen_dst.Dispose();
		}

		screen_src = new Screen(width, height);
		screen_dst = new Screen(width, height);
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

		shadow_mapping.Update();
	}

	@Override
	public void Draw() {
		screen_src.Enable();
		screen_src.Clear();
		Model3DFunctions.DrawModel(model_handles[0]);
		Model3DFunctions.DrawModel(model_handles[1]);
		screen_src.Disable();

		// shadow_mapping.VisualizeShadowFactors(screen_dst);
		shadow_mapping.ApplyShadow(screen_src, screen_dst);
		screen_dst.Draw(0, 0, this.GetWidth(), this.GetHeight());

		if (this.GetKeyboardPressingCount(KeyboardEnum.KEY_ENTER) == 1) {
			screen_dst.TakeScreenshot("screenshot.png");
		}
	}
}
