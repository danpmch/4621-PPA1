package cs4621.ppa1.scene;

public class SolarSystemScene extends Scene {

	public SolarSystemScene() {
		// Nothing.
	}

	public SceneNode buildInitialScene() {
		// TODO: (Problem 3) Construct the solar system.
		// The base code returns a new TransformationNode to prevent a null exception.
		return new TransformationNode();
	}

	public void updateTransformationNode(TransformationNode node, float time) {
		// TODO: (Problem 3) Animate the solar system.
	}

}
