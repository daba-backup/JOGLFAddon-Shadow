package com.github.dabasan.joglfaddon.shadow;

import com.github.dabasan.tool.MathFunctions;

/**
 * Perspective light info
 * 
 * @author Daba
 *
 */
public class PerspectiveLightInfo extends LightInfoBase {
	private float fov;
	private float aspect;

	public PerspectiveLightInfo() {
		this.SetType(ProjectionType.PERSPECTIVE);
		fov = MathFunctions.DegToRad(60.0f);
		aspect = 1.0f;
	}

	public void SetFOV(float fov) {
		this.fov = fov;
	}
	public void SetAspect(float aspect) {
		this.aspect = aspect;
	}

	public float GetFOV() {
		return fov;
	}
	public float GetAspect() {
		return aspect;
	}
}
