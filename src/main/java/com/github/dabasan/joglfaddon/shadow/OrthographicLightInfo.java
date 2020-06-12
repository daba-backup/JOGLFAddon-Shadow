package com.github.dabasan.joglfaddon.shadow;

/**
 * Orthographic light info
 * 
 * @author Daba
 *
 */
public class OrthographicLightInfo extends LightInfoBase {
	private float size;

	public OrthographicLightInfo() {
		this.SetType(ProjectionType.ORTHOGRAPHIC);
		size = 40.0f;
	}

	public void SetSize(float size) {
		this.size = size;
	}

	public float GetSize() {
		return size;
	}
}
