package com.github.dabasan.joglfaddon.shadow;

import com.github.dabasan.basis.vector.Vector;

/**
 * Light info
 * 
 * @author Daba
 *
 */
public interface LightInfo {
	public void SetPosition(Vector position);
	public void SetTarget(Vector target);
	public void SetUp(Vector up);
	public void SetNearFar(float near, float far);

	public ProjectionType GetProjectionType();
	public Vector GetPosition();
	public Vector GetTarget();
	public Vector GetUp();
	public float GetNear();
	public float GetFar();
}
