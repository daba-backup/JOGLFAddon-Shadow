package com.github.dabasan.joglfaddon.shadow;

import static com.github.dabasan.basis.vector.VectorFunctions.*;

import com.github.dabasan.basis.vector.Vector;

/**
 * Base class for light info
 * 
 * @author Daba
 *
 */
class LightInfoBase implements LightInfo {
	private ProjectionType type;

	private Vector position;
	private Vector target;
	private Vector up;

	private float near;
	private float far;

	public LightInfoBase() {
		this.type = ProjectionType.ORTHOGRAPHIC;

		position = VGet(100.0f, 100.0f, 100.0f);
		target = VGet(0.0f, 0.0f, 0.0f);
		up = VGet(0.0f, 1.0f, 0.0f);

		near = 1.0f;
		far = 100.0f;
	}

	protected void SetType(ProjectionType type) {
		this.type = type;
	}
	@Override
	public void SetPosition(Vector position) {
		this.position = position;
	}
	@Override
	public void SetTarget(Vector target) {
		this.target = target;
	}
	@Override
	public void SetUp(Vector up) {
		this.up = up;
	}
	@Override
	public void SetNearFar(float near, float far) {
		this.near = near;
		this.far = far;
	}

	@Override
	public ProjectionType GetProjectionType() {
		return type;
	}
	@Override
	public Vector GetPosition() {
		return position;
	}
	@Override
	public Vector GetTarget() {
		return target;
	}
	@Override
	public Vector GetUp() {
		return up;
	}
	@Override
	public float GetNear() {
		return near;
	}
	@Override
	public float GetFar() {
		return far;
	}
}
