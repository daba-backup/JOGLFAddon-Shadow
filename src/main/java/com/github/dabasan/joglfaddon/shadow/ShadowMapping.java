package com.github.dabasan.joglfaddon.shadow;

import static com.github.dabasan.basis.matrix.MatrixFunctions.*;
import static com.github.dabasan.joglf.gl.wrapper.GLWrapper.*;
import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dabasan.basis.matrix.Matrix;
import com.github.dabasan.basis.vector.Vector;
import com.github.dabasan.joglf.gl.front.CameraFront;
import com.github.dabasan.joglf.gl.model.Model3DFunctions;
import com.github.dabasan.joglf.gl.shader.ShaderProgram;
import com.github.dabasan.joglf.gl.tool.matrix.ProjectionMatrixFunctions;
import com.github.dabasan.joglf.gl.tool.matrix.TransformationMatrixFunctions;
import com.github.dabasan.joglf.gl.transferrer.FullscreenQuadTransferrerWithUV;
import com.github.dabasan.joglf.gl.util.screen.ScreenBase;
import com.jogamp.common.nio.Buffers;

/**
 * Shadow mapping
 * 
 * @author Daba
 *
 */
public class ShadowMapping {
	private Logger logger = LoggerFactory.getLogger(ShadowMapping.class);

	private int depth_texture_width;
	private int depth_texture_height;

	public static int MAX_LIGHT_NUM = 16;
	private List<LightInfo> lights;

	private List<Integer> depth_model_handles;
	private List<Integer> shadow_model_handles;

	private IntBuffer depth_fbo_ids;
	private IntBuffer depth_texture_ids;

	private ShaderProgram depth_program;
	private ShaderProgram shadow_program;
	private ShaderProgram visualize_program;
	private FullscreenQuadTransferrerWithUV transferrer;

	private Matrix bias_matrix;

	public ShadowMapping(List<LightInfo> lights, int depth_texture_width, int depth_texture_height)
			throws IllegalArgumentException {
		if (lights.size() > MAX_LIGHT_NUM) {
			throw new IllegalArgumentException("Too many lights in the list.");
		}

		this.lights = lights;

		this.depth_texture_width = depth_texture_width;
		this.depth_texture_height = depth_texture_height;

		this.SetupDepthTextures();
		this.SetupDepthFramebuffers();
		this.SetupPrograms();
		this.SetDefaultValues();

		depth_model_handles = new ArrayList<>();
		shadow_model_handles = new ArrayList<>();

		transferrer = new FullscreenQuadTransferrerWithUV(true);

		bias_matrix = new Matrix();
		bias_matrix.SetValue(0, 0, 0.5f);
		bias_matrix.SetValue(0, 3, 0.5f);
		bias_matrix.SetValue(1, 1, 0.5f);
		bias_matrix.SetValue(1, 3, 0.5f);
		bias_matrix.SetValue(2, 2, 0.5f);
		bias_matrix.SetValue(2, 3, 0.5f);
		bias_matrix.SetValue(3, 3, 1.0f);
	}
	private void SetupDepthTextures() {
		depth_texture_ids = Buffers.newDirectIntBuffer(lights.size());
		glGenTextures(depth_texture_ids.capacity(), depth_texture_ids);

		for (int i = 0; i < depth_texture_ids.capacity(); i++) {
			glBindTexture(GL_TEXTURE_2D, depth_texture_ids.get(i));
			glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT16, depth_texture_width,
					depth_texture_height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			glBindTexture(GL_TEXTURE_2D, 0);
		}
	}
	private void SetupDepthFramebuffers() {
		depth_fbo_ids = Buffers.newDirectIntBuffer(lights.size());
		glGenFramebuffers(depth_fbo_ids.capacity(), depth_fbo_ids);

		for (int i = 0; i < depth_fbo_ids.capacity(); i++) {
			glBindFramebuffer(GL_FRAMEBUFFER, depth_fbo_ids.get(i));
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D,
					depth_texture_ids.get(i), 0);
			int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
			if (status != GL_FRAMEBUFFER_COMPLETE) {
				logger.error("Incomplete framebuffer for depth. status={}", status);
			}
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
		}
	}
	private void SetupPrograms() {
		depth_program = new ShaderProgram("dabasan/shadow/depth",
				"./Data/Shader/330/addon/dabasan/shadow/depth/vshader.glsl",
				"./Data/Shader/330/addon/dabasan/shadow/depth/fshader.glsl");
		shadow_program = new ShaderProgram("dabasan/shadow/shadow",
				"./Data/Shader/330/addon/dabasan/shadow/shadow/vshader.glsl",
				"./Data/Shader/330/addon/dabasan/shadow/shadow/fshader.glsl");
		visualize_program = new ShaderProgram("dabasan/shadow/visualize",
				"./Data/Shader/330/addon/dabasan/shadow/visualize/vshader.glsl",
				"./Data/Shader/330/addon/dabasan/shadow/visualize/fshader.glsl");

		CameraFront.AddProgram(shadow_program);
	}
	private void SetDefaultValues() {
		this.SetNormalOffset(0.01f);
		this.SetBias(0.001f);
		this.SetInShadowVisibility(0.5f);
		this.SetIntegrationMethod(IntegrationMethod.MUL);

		shadow_program.Enable();
		shadow_program.SetUniform("light_num", lights.size());
		shadow_program.SetUniform("depth_texture_size", depth_texture_width, depth_texture_height);
		shadow_program.Disable();
	}

	public void Dispose() {
		glDeleteFramebuffers(depth_fbo_ids.capacity(), depth_fbo_ids);
		glDeleteTextures(depth_texture_ids.capacity(), depth_texture_ids);

		CameraFront.RemoveProgram(shadow_program);
	}

	public void SetNormalOffset(float normal_offset) {
		shadow_program.Enable();
		shadow_program.SetUniform("normal_offset", normal_offset);
		shadow_program.Disable();
	}
	public void SetBias(float bias) {
		shadow_program.Enable();
		shadow_program.SetUniform("bias", bias);
		shadow_program.Disable();
	}
	public void SetInShadowVisibility(float in_shadow_visibility) {
		shadow_program.Enable();
		shadow_program.SetUniform("in_shadow_visibility", in_shadow_visibility);
		shadow_program.Disable();
	}
	public void SetIntegrationMethod(IntegrationMethod integration_method) {
		shadow_program.Enable();
		shadow_program.SetUniform("integration_method", integration_method.ordinal());
		shadow_program.Disable();
	}

	public void AddDepthModel(int model_handle) {
		depth_model_handles.add(model_handle);
	}
	public void RemoveDepthModel(int model_handle) {
		depth_model_handles.remove(model_handle);
	}
	public void RemoveAllDepthModels() {
		depth_model_handles.clear();
	}
	public void AddShadowModel(int model_handle) {
		shadow_model_handles.add(model_handle);
		Model3DFunctions.RemoveAllPrograms(model_handle);
		Model3DFunctions.AddProgram(model_handle, shadow_program);
	}
	public void RemoveShadowModel(int model_handle) {
		shadow_model_handles.remove(model_handle);
	}
	public void RemoveAllShadowModels() {
		shadow_model_handles.clear();
	}

	public void TransferCameraPropertiesToPrograms() {
		CameraFront.Update(shadow_program);
	}

	public void Update() {
		this.TransferLightProperties();
		this.GenerateDepthTextures();
	}
	private void TransferLightProperties() {
		for (int i = 0; i < lights.size(); i++) {
			var light = lights.get(i);

			Vector position = light.GetPosition();
			Vector target = light.GetTarget();
			Vector up = light.GetUp();
			float near = light.GetNear();
			float far = light.GetFar();

			Matrix projection;
			Matrix view_transformation = TransformationMatrixFunctions
					.GetViewTransformationMatrix(position, target, up);

			ProjectionType type = light.GetProjectionType();
			if (type == ProjectionType.ORTHOGRAPHIC) {
				float size = ((OrthographicLightInfo) light).GetSize();
				projection = ProjectionMatrixFunctions.GetOrthogonalMatrix(-size, size, -size, size,
						near, far);
			} else {
				float fov = ((PerspectiveLightInfo) light).GetFOV();
				float aspect = ((PerspectiveLightInfo) light).GetAspect();
				projection = ProjectionMatrixFunctions.GetPerspectiveMatrix(fov, aspect, near, far);
			}

			Matrix depth_mvp = MMult(projection, view_transformation);
			Matrix depth_bias_mvp = MMult(bias_matrix, depth_mvp);

			depth_program.Enable();
			depth_program.SetUniform("depth_mvp", true, depth_mvp);
			depth_program.Disable();

			String uname = "lights" + "[" + i + "]";
			shadow_program.Enable();
			shadow_program.SetUniform(uname + ".projection_type", type.ordinal());
			shadow_program.SetUniform(uname + ".depth_bias_mvp", true, depth_bias_mvp);
			shadow_program.Disable();
		}
	}
	private void GenerateDepthTextures() {
		depth_program.Enable();
		for (int i = 0; i < lights.size(); i++) {
			glBindFramebuffer(GL_FRAMEBUFFER, depth_fbo_ids.get(i));
			glViewport(0, 0, depth_texture_width, depth_texture_height);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			for (int depth_model_handle : depth_model_handles) {
				Model3DFunctions.TransferModel(depth_model_handle);
			}
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
		}
		depth_program.Disable();
	}

	public void CreateShadowedScene(ScreenBase screen, boolean clear_screen) {
		shadow_program.Enable();
		for (int i = 0; i < lights.size(); i++) {
			String uname = "depth_textures" + "[" + i + "]";
			glActiveTexture(GL_TEXTURE0 + (i + 1));
			glBindTexture(GL_TEXTURE_2D, depth_texture_ids.get(i));
			shadow_program.SetUniform(uname, i + 1);
		}
		screen.Enable();
		if (clear_screen == true) {
			screen.Clear();
		}
		for (int shadow_model_handle : shadow_model_handles) {
			Model3DFunctions.DrawModel(shadow_model_handle, "texture_sampler", 0);
		}
		screen.Disable();
		shadow_program.Disable();
	}

	public int VisualizeDepthTexture(int index, ScreenBase screen) {
		if (!(0 <= index && index < lights.size())) {
			logger.trace("Index out of bounds. index={}", index);
			return -1;
		}

		visualize_program.Enable();
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, depth_texture_ids.get(index));
		visualize_program.SetUniform("texture_sampler", 0);
		screen.Enable();
		screen.Clear();
		transferrer.Transfer();
		screen.Disable();
		visualize_program.Disable();

		return 0;
	}
}
