package cs4621.ppa1.scene;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.yaml.snakeyaml.Yaml;

import cs4621.ppa1.scene.SceneNode;
import cs4621.ppa1.scene.TransformationNode;
import cs4621.ppa1.shape.Mesh;
import cs4621.ppa1.shape.Sphere;

public class Scene
{
	protected DefaultTreeModel treeModel;

	public Scene()
	{
		treeModel = new DefaultTreeModel(buildInitialScene());
	}

	public SceneNode buildInitialScene()
	{
		SceneNode root = new TransformationNode("Root");

		MeshNode sphereNode = new MeshNode("Sphere");
		sphereNode.setMesh(new Sphere());
		root.add(sphereNode);

		LightNode lightNode = new LightNode("Light");
		lightNode.setAmbient(1.0f, 1.0f, 1.0f);
		lightNode.setDiffuse(1.0f, 1.0f, 1.0f);
		lightNode.setSpecular(1.0f, 1.0f, 1.0f);
		lightNode.setPosition(5, 5, 5);
		root.add(lightNode);

		return root;
	}

	public SceneNode getSceneRoot()
	{
		return (SceneNode)treeModel.getRoot();
	}

	public void updateTransformationNode(TransformationNode node, float time)
	{
		// Nothing.
	}

	public void animateNodes(float time)
	{
		animateNodesHelper(getSceneRoot(), time);
	}

	private void animateNodesHelper(SceneNode node, float time)
	{
		if (node instanceof TransformationNode)
		{
			TransformationNode transNode = (TransformationNode)node;
			updateTransformationNode(transNode, time);
		}
		for (int i = 0; i < node.getChildCount(); i++)
			animateNodesHelper(node.getSceneNodeChild(i), time);
	}

	public void rebuildMeshes(float tolerance)
	{
		rebuildMeshesHelper(getSceneRoot(), tolerance);
	}

	private void rebuildMeshesHelper(SceneNode node, float tolerance)
	{
		if (node instanceof MeshNode)
		{
			MeshNode meshNode = (MeshNode)node;
			meshNode.getMesh().buildMesh(tolerance);
		}
		for (int i = 0; i < node.getChildCount(); i++)
			rebuildMeshesHelper(node.getSceneNodeChild(i), tolerance);
	}

	public SceneNode searchForMeshId(int meshId)
	{
		return searchForMeshIdHelper(getSceneRoot(), meshId);
	}

	private SceneNode searchForMeshIdHelper(SceneNode node, int meshId)
	{
		if (node instanceof MeshNode)
		{
			MeshNode meshNode = (MeshNode)node;
			if (meshNode.getMesh().getId() == meshId)
				return meshNode;
		}
		for (int i = 0; i < node.getChildCount(); i++)
		{
			SceneNode result = searchForMeshIdHelper(node.getSceneNodeChild(i), meshId);
			if (result != null)
				return result;
		}
		return null;
	}

	public void render(GL2 gl)
	{
		//gl.glMatrixMode(GL2.GL_MODELVIEW);
		//gl.glLoadIdentity();
		traverseDraw(gl, this.getSceneRoot(), false);
	}

	public void renderForPicking(GL2 gl)
	{		
		//gl.glMatrixMode(GL2.GL_MODELVIEW);
		//gl.glLoadIdentity();
		traverseDraw(gl, this.getSceneRoot(), true);
	}
	
	void traverseDraw(GL2 gl, SceneNode n, boolean pick) {
		//do transforming
		if(n instanceof TransformationNode) {
			
			TransformationNode t = (TransformationNode)n;
			
			//push matrix
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glPushMatrix();
			gl.glTranslatef(t.translation.x, t.translation.y, t.translation.z);
		
			//after messing with euler for ~1hr, I went there.
			//source = wikipedia.
			//the results are still pretty shoddy due to storing rotation as euler angles,
			//but it's better than plain glRotate()+transform rotation axes thing.
			float cx = (float)Math.cos(Math.toRadians(t.rotation.x)/2f);
			float cy = (float)Math.cos(Math.toRadians(t.rotation.y)/2f);
			float cz = (float)Math.cos(Math.toRadians(t.rotation.z)/2f);
			
			float sx = (float)Math.sin(Math.toRadians(t.rotation.x)/2f);
			float sy = (float)Math.sin(Math.toRadians(t.rotation.y)/2f);
			float sz = (float)Math.sin(Math.toRadians(t.rotation.z)/2f);
			
			float w = cx*cy*cz+sx*sy*sz; //w
			float x = sx*cy*cz-cx*sy*sz; //x
			float y = cx*sy*cz+sx*cy*sz; //y
			float z = cx*cy*sz-sx*sy*cz; //z
			
			float[] mat = new float[16];
			
			mat[0] = w*w + x*x - y*y - z*z;
			mat[1] = 2*x*y + 2*z*w;
			mat[2] = 2*x*z - 2*y*w;
			
			mat[4] = 2*x*y - 2*z*w;
			mat[5] = w*w - x*x + y*y - z*z;
			mat[6] = 2*y*z + 2*x*w;
			
			mat[8] = 2*x*z + 2*y*w;
			mat[9] = 2*y*z - 2*x*w;
		    mat[10] = w*w - x*x - y*y + z*z;
			
			mat[15] = 1;
			
			gl.glMultMatrixf(mat, 0);

			gl.glScalef(t.scaling.x, t.scaling.y, t.scaling.z);
			
			//draw
			n.draw(gl, pick);
			
			//traverse childs
			for(int i =0;i<n.getChildCount();i++) {
				
				traverseDraw(gl,((SceneNode)n.getChildAt(i)),pick);
				
			}
			
			//pop matrix
			gl.glPopMatrix();
		}
	}
	
	public void setupLighting(GL2 gl)
	{
		GLLightManager.startSettingUpLighting(gl);
		setupLightingHelper((SceneNode)treeModel.getRoot(), gl);
	}

	private void setupLightingHelper(SceneNode node, GL2 gl)
	{
		if (node instanceof LightNode)
		{
			LightNode light = (LightNode)node;
			if (GLLightManager.hasNextLight())
			{
				int lightId = GLLightManager.getNextLightId();

				gl.glEnable(lightId);
				gl.glLightfv(lightId, GL2.GL_AMBIENT, light.ambient, 0);
				gl.glLightfv(lightId, GL2.GL_DIFFUSE, light.diffuse, 0);
				gl.glLightfv(lightId, GL2.GL_SPECULAR, light.specular, 0);
				gl.glLightfv(lightId, GL2.GL_POSITION, light.position, 0);
			}
		}
		for (int i = 0; i < node.getChildCount(); i++)
			setupLightingHelper(node.getSceneNodeChild(i), gl);
	}


	/**
	 * Save the current scene to a file.
	 */
	public void save(String filename) throws IOException
	{
		Yaml yaml = new Yaml();
		Object rep = ((SceneNode)treeModel.getRoot()).getYamlObjectRepresentation();
		String output = yaml.dump(rep);

		FileWriter fstream = new FileWriter(filename);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(output);
		out.close();
	}

	public void load(String filename) throws java.io.IOException
	{
		treeModel.setRoot(SceneNode.load(filename));
	}

	private static String getBaseClassName(Class<? extends Object> c)
	{
		String classname = c.getName();
		String name = classname.substring(classname.lastIndexOf('.') + 1, classname.length());
		return name;
	}

	/**
	 * Add a new shape to the tree
	 */
	public void addNewShape(TreePath path, Class<? extends Mesh> c) throws Exception
	{
		//Get the node to insert into
		SceneNode selected = getSceneRoot();
		if (path != null && path.getLastPathComponent() instanceof TransformationNode)
			selected = (SceneNode) path.getLastPathComponent();

		Mesh newObject = c.newInstance();
		String nodeName = getBaseClassName(c);
		MeshNode node = new MeshNode(nodeName, newObject);
		treeModel.insertNodeInto(node, selected, selected.getChildCount());
	}

	/**
	 * Add a new light to the tree at the end of the given path.
	 */
	public void addNewLight(TreePath path)
	{
		//Create a light
		LightNode node = new LightNode("Light");

		//Get the node to insert into
		SceneNode selected = (SceneNode)treeModel.getRoot();
		if (path != null && path.getLastPathComponent() instanceof TransformationNode)
			selected = (SceneNode) path.getLastPathComponent();

		treeModel.insertNodeInto(node, selected, selected.getChildCount());
	}

	/**
	 * Filters out extraneous child nodes from the
	 * supplied array of Treeables.
	 */
	public Vector<SceneNode> filterChildren(SceneNode[] nodes)
	{
		// Trim extraneous child nodes
		Vector<SceneNode> filtered = new Vector<SceneNode>();
		for (int i = 0; i < nodes.length; i++)
			filtered.add(nodes[i]);

		//Find the redundant children
		for (Iterator<SceneNode> j = filtered.iterator(); j.hasNext();) {
			SceneNode tj = j.next();
			for (int i = 0; i < nodes.length; i++) {
				SceneNode ti = nodes[i];
				if (ti == tj) continue;
				if (tj.isNodeAncestor(ti)) j.remove();
			}
		}
		return filtered;
	}

	public void deleteNodes(SceneNode[] nodes)
	{
		Vector<SceneNode> filtered = filterChildren(nodes);
		for (int i=0; i<filtered.size(); i++) {
			SceneNode t = (SceneNode)filtered.get(i);
			if(t == treeModel.getRoot())
				continue;
			treeModel.removeNodeFromParent(t);
		}
		treeModel.reload();
	}

	/**
	 * Groups a set of selected nodes into a new parent. Return
	 * the new parent node.
	 * @param nodes
	 * @param groupName
	 */
	public SceneNode groupNodes(SceneNode[] nodes, String groupName)
	{
		Vector<SceneNode> filtered = filterChildren(nodes);

		if (filtered.size() == 0) return null;

		//Form the new group and add it to the tree
		SceneNode groupNode = new TransformationNode(groupName);
		SceneNode firstSelected = (SceneNode)filtered.get(0);
		SceneNode groupParent = (SceneNode)firstSelected.getParent();
		if (groupParent == null) return null;
		int groupIdx = groupParent.getIndex(firstSelected);

		treeModel.insertNodeInto(groupNode, groupParent, groupIdx);
		for (int i = 0; i < filtered.size(); i++)
		{
			SceneNode node = (SceneNode) filtered.get(i);
			treeModel.removeNodeFromParent(node);
			treeModel.insertNodeInto(node, groupNode, groupNode.getChildCount());
		}
		treeModel.reload();

		return groupNode;
	}

	/**
	 * Reparents the items in nodesToReparent underneath
	 * the currently selected node.
	 */
	public void reparent(SceneNode[] nodesToReparent, SceneNode parent)
	{
		// Invalid children selected?
		for (int i=0; i<nodesToReparent.length; i++) {
			if (parent.isNodeAncestor(nodesToReparent[i])) return;
		}

		Vector<SceneNode> filtered = filterChildren(nodesToReparent);

		//reparent the filtered children each seperately
		for (int i=0; i<filtered.size(); i++) {
			SceneNode t = (SceneNode)filtered.get(i);
			t.removeFromParent();
			parent.insert(t,0);
		}

		treeModel.reload();
	}

	public DefaultTreeModel getTreeModel()
	{
		return treeModel;
	}
}
