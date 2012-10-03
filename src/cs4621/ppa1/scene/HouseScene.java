package cs4621.ppa1.scene;

import java.io.IOException;

/**
 * A very silly scene that spins a house's window frame around.
 * 
 * @author ser99
 * 
 */
public class HouseScene extends Scene {

	private static float WindowSpeed = 1.0f;

	public HouseScene() {
		// Nothing.
	}

	public SceneNode buildInitialScene() {
		try {
			// Load the starting scene from a file.
			return SceneNode.load("data/scenes/house.txt");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	public void updateTransformationNode(TransformationNode node, float time) {
		// Branch on all animated nodes using a bunch of if/else if statements.
		if (node.getName().equals("Window Frame")) {
			node.setRotation(WindowSpeed * time, 0.0f, 0.0f);
		}
	}

}
