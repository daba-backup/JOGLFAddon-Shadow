package com.github.dabasan.joglfaddon.shadow;

public class TestMain {
	public static void main(String[] args) {
		new TestMain();
	}
	public TestMain() {
		var window = new ShadowMappingTestWindow();
		window.SetExitProcessWhenDestroyed();
	}
}
