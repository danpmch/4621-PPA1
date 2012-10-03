package cs4621.ppa1.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.swing.tree.DefaultMutableTreeNode;

import org.yaml.snakeyaml.Yaml;

import cs4621.ppa1.util.Util;

public abstract class SceneNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;

	protected String name;

	public SceneNode()
	{
		// NOP
	}
	
	public abstract void draw(GL2 gl, boolean picking);

	public SceneNode getSceneNodeChild(int i)
	{
		return (SceneNode)getChildAt(i);
	}
	
	public SceneNode getSceneNodeParent()
	{
		return (SceneNode)getParent();
	}

	public SceneNode(String name)
	{
		setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public Object getYamlObjectRepresentation()
	{
		Map<Object, Object> result = new HashMap<Object, Object>();
		result.put("name", getName());
		List<Object> children = new ArrayList<Object>();
		for (int ctr = 0; ctr < getChildCount(); ctr++)
			  children.add(((SceneNode)getChildAt(ctr)).getYamlObjectRepresentation());
		result.put("children", children);
		return result;
	}

	public static SceneNode fromYamlObject(Object yamlObject)
	{
		if (!(yamlObject instanceof Map))
			throw new RuntimeException("yamlObject not a Map");
		Map<?, ?> yamlMap = (Map<?, ?>)yamlObject;

		if (yamlMap.get("type").equals("TransformationNode"))
			return TransformationNode.fromYamlObject(yamlObject);
		else if (yamlMap.get("type").equals("MeshNode"))
			return MeshNode.fromYamlObject(yamlObject);
		else if (yamlMap.get("type").equals("LightNode"))
			return LightNode.fromYamlObject(yamlObject);
		else
			throw new RuntimeException("invalid SceneNode type: " + yamlMap.get("type").toString());
	}
	
	public static SceneNode load(String filename) throws java.io.IOException
	{
		String fileContent = Util.readFileAsString(filename);
		Yaml yaml = new Yaml();
		Object yamlObject = yaml.load(fileContent);

		return SceneNode.fromYamlObject(yamlObject);
	}
}
