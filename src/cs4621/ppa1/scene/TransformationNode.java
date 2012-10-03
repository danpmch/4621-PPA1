package cs4621.ppa1.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import cs4621.ppa1.util.Util;

public class TransformationNode extends SceneNode
{
	private static final long serialVersionUID = 1L;

	/**
	 * Rotation component of this transformation.
	 * Stored in degrees.
	 */
	public final Vector3f rotation = new Vector3f();

	/**
	 * Scaling component of this transformation.
	 */
	public final Vector3f scaling = new Vector3f();

	/**
	 * Translation component of this transformation.
	 */
	public final Vector3f translation = new Vector3f();

	Matrix3f tempMatrix = new Matrix3f();

	/**
	 * Required for I/O.
	 */
	public TransformationNode()
	{
		super();
	}

	public TransformationNode(String name)
	{
		super(name);
		resetTransformation();

	}

	protected void resetTransformation()
	{
		scaling.set(1,1,1);
		rotation.set(0, 0, 0);
		translation.set(0, 0, 0);
	}

	public void setRotation(float xAngle, float yAngle, float zAngle)
	{
		this.rotation.set(xAngle, yAngle, zAngle);
	}

	public void setScaling(float x, float y, float z)
	{
		this.scaling.set(x, y, z);
	}

	public void setTranslation(float x, float y, float z)
	{
		this.translation.set(x, y, z);
	}

	private Object convertVector3ToIntList(Vector3f v)
	{
		List<Object> result = new ArrayList<Object>();
		result.add(v.x);
		result.add(v.y);
		result.add(v.z);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getYamlObjectRepresentation()
	{
		Map<String, Object> result = (Map<String, Object>)super.getYamlObjectRepresentation();
		result.put("type", "TransformationNode");
		result.put("translation", convertVector3ToIntList(translation));
		result.put("rotation", convertVector3ToIntList(rotation));
		result.put("scaling", convertVector3ToIntList(scaling));
		return result;
	}

	public void extractTransformationFromYamlObject(Object yamlObject)
	{
		if (!(yamlObject instanceof Map))
			throw new RuntimeException("yamlObject not a Map");
		Map<?, ?> yamlMap = (Map<?, ?>)yamlObject;
		translation.set(Util.getVector3ffromYamlObject(yamlMap.get("translation")));
		rotation.set(Util.getVector3ffromYamlObject(yamlMap.get("rotation")));
		scaling.set(Util.getVector3ffromYamlObject(yamlMap.get("scaling")));
	}

	public void addChildrenFromYamlObject(Object yamlObject)
	{
		if (!(yamlObject instanceof Map))
			throw new RuntimeException("yamlObject not a Map");
		Map<?, ?> yamlMap = (Map<?, ?>)yamlObject;
		List<?> childrenList = (List<?>)yamlMap.get("children");
		for(Object o : childrenList)
			insert(SceneNode.fromYamlObject(o),getChildCount());
	}

	public static SceneNode fromYamlObject(Object yamlObject)
	{
		if (!(yamlObject instanceof Map))
			throw new RuntimeException("yamlObject not a Map");
		Map<?, ?> yamlMap = (Map<?, ?>)yamlObject;

		TransformationNode result = new TransformationNode();
		result.setName((String)yamlMap.get("name"));
		result.extractTransformationFromYamlObject(yamlObject);
		result.addChildrenFromYamlObject(yamlObject);

		return result;
	}

	@Override
	public void draw(GL2 gl, boolean picking) {
		//nop
		
	}
	
	public TransformationNode getLowestTransformationNodeAncestor()
	{
		SceneNode parent = getSceneNodeParent();
		while (parent != null)
		{
			if (parent instanceof TransformationNode)
				return (TransformationNode)parent;
			else
				parent = getSceneNodeParent();
		}
		return null;
	}
	
	public Vector3f getRotation()
	{
		return rotation;
	}

	public Vector3f getScaling()
	{
		return scaling;
	}

	public Vector3f getTranslation()
	{
		return translation;
	}

	public void glTranslateToOriginInWorldSpace(GL2 gl)
	{
		Vector3f worldPositionOfOrigin = new Vector3f();
		toWorld(new Vector3f(0,0,0), worldPositionOfOrigin);
		gl.glTranslatef(
				worldPositionOfOrigin.x,
				worldPositionOfOrigin.y,
				worldPositionOfOrigin.z);
	}
	
	/**
	 * Converts the given point v from local space to world space
	 */
	public void toWorld(Vector3f v, Vector3f outv)
	{
		Vector3f result = new Vector3f();
		transform(v, result);
		if (getLowestTransformationNodeAncestor() == null)
		{
			outv.set(result);
		}
		else
		{
			getLowestTransformationNodeAncestor().toWorld(result, outv);
		}
	}
	
	/**
	 * Converts the given point v from world space to local space
	 */
	public void toLocal(Vector3f v, Vector3f outv)
	{
		Vector3f result = new Vector3f();
		inverseTransform(v, result);
		if (getLowestTransformationNodeAncestor() == null)
		{
			outv.set(result);
		}
		else
		{
			getLowestTransformationNodeAncestor().toWorld(result, outv);
		}
	}

	public Vector3f lossyScale() {
		Vector3f v = new Vector3f(1,1,1);
		return lsh(v);
	}
	
	private Vector3f lsh(Vector3f v) {
		v.x=v.x*scaling.x;
		v.y=v.y*scaling.y;
		v.z=v.z*scaling.z;
		return 
		(getLowestTransformationNodeAncestor()==null?
				v :
				getLowestTransformationNodeAncestor().lsh(v));
	}
	
	
	/**
	 * Transforms the given point by this transformation
	 */
	public void inverseTransform(Vector3f v, Vector3f outv)
	{
		outv.set(v);

		// Translate
		outv.sub(translation);
		
		// Rotate
		tempMatrix.rotZ((float)Math.toRadians(-rotation.z));
		tempMatrix.transform(outv);
		tempMatrix.rotY((float)Math.toRadians(-rotation.y));
		tempMatrix.transform(outv);
		tempMatrix.rotX((float)Math.toRadians(-rotation.x));
		tempMatrix.transform(outv);

		// Scale
		outv.set(outv.x / scaling.x, outv.y / scaling.y, outv.z / scaling.z);

	}
	
	/**
	 * Transforms the given point by this transformation
	 */
	public void transform(Vector3f v, Vector3f outv)
	{
		outv.set(v);
		// Scale
		outv.set(outv.x * scaling.x, outv.y * scaling.y, outv.z * scaling.z);

		// Rotate
		tempMatrix.rotX((float)Math.toRadians(rotation.x));
		tempMatrix.transform(outv);
		tempMatrix.rotY((float)Math.toRadians(rotation.y));
		tempMatrix.transform(outv);
		tempMatrix.rotZ((float)Math.toRadians(rotation.z));
		tempMatrix.transform(outv);

		// Translate
		outv.add(translation);
	}
	
	public void rotate(Vector3f v, Vector3f outv)
	{
		outv.set(v);

		// Rotate
		tempMatrix.rotX((float)Math.toRadians(rotation.x));
		tempMatrix.transform(outv);
		tempMatrix.rotY((float)Math.toRadians(rotation.y));
		tempMatrix.transform(outv);
		tempMatrix.rotZ((float)Math.toRadians(rotation.z));
		tempMatrix.transform(outv);
	}

	/**
	 * Compute an orthonormal basis of the object space expressed
	 * as vectors in world space.
	 */
	public void getBasisInWorldSpace(Vector3f outX, Vector3f outY, Vector3f outZ)
	{
		Vector3f origin = new Vector3f();

		toWorld(new Vector3f(0,0,0), origin);
		toWorld(new Vector3f(1,0,0), outX);
		toWorld(new Vector3f(0,1,0), outY);
		toWorld(new Vector3f(0,0,1), outZ);

		outX.sub(origin);
		outY.sub(origin);
		outZ.sub(origin);

		outX.normalize();
		outY.normalize();
		outZ.normalize();
	}

	/**
	 * Compute an orthonormal basis of the lowest transformation node's object space expressed
	 * as vectors in world space.
	 */
	public void getLowestTransformationNodeAncestorBasisInWorldSpace(Vector3f outX, Vector3f outY, Vector3f outZ)
	{
		if (getLowestTransformationNodeAncestor() == null)
		{
			outX.set(1, 0, 0);
			outY.set(0, 1, 0);
			outZ.set(0, 0, 1);
		}
		else
		{
			TransformationNode ancestor = getLowestTransformationNodeAncestor();
			ancestor.getBasisInWorldSpace(outX, outY, outZ);
		}
	}
}
