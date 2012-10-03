package cs4621.ppa1.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.media.opengl.GL2;
import javax.vecmath.Vector3f;

public class Util
{
	public static Vector3f getVector3ffromYamlObject(Object yamlObject)
	{
		if (!(yamlObject instanceof List))
			throw new RuntimeException("yamlObject not a List");
		List<?> yamlList = (List<?>)yamlObject;
		return new Vector3f(
				Float.valueOf(yamlList.get(0).toString()),
				Float.valueOf(yamlList.get(1).toString()),
				Float.valueOf(yamlList.get(2).toString()));
	}

	public static void assign4ElementArrayFromYamlObject(float[] output, Object yamlObject)
	{
		if (!(yamlObject instanceof List))
			throw new RuntimeException("yamlObject not a List");
		List<?> yamlList = (List<?>)yamlObject;

		output[0] = Float.valueOf(yamlList.get(0).toString());
		output[1] = Float.valueOf(yamlList.get(1).toString());
		output[2] = Float.valueOf(yamlList.get(2).toString());
		output[3] = Float.valueOf(yamlList.get(3).toString());
	}

	/**
	 * Get a vector not parallel to v.
	 */
	public static void nonParallelVector(Vector3f v, Vector3f nonParallel)
	{
		if (v.x <= v.y && v.x <= v.z) nonParallel.set(1,0,0);
		else if (v.y <= v.x && v.y <= v.z) nonParallel.set(0,1,0);
		else if (v.z <= v.x && v.z <= v.y) nonParallel.set(0,0,1);
	}


	/**
	 * gl-rotates the Y axis to the given vector using
	 * an arbitrary frame
	 */
	public static void glRotateYTo(GL2 gl, Vector3f v)
	{
		Vector3f ortho1 = new Vector3f();
		Vector3f ortho2 = new Vector3f();

		nonParallelVector(v, ortho1);
		ortho2.cross(v, ortho1);
		ortho1.cross(ortho2, v);
		ortho1.normalize();
		ortho2.normalize();

		gl.glMultMatrixf(new float[] {
				ortho1.x, ortho1.y, ortho1.z, 0,
				v.x, v.y, v.z, 0,
				ortho2.x, ortho2.y, ortho2.z, 0,
				0,0,0, 1 }, 0);
	}

	public static final double BOX_RADIUS = 0.1;

	public static void glRenderBox(GL2 gl)
	{
		double r = BOX_RADIUS;
		gl.glBegin(GL2.GL_QUADS);
		{
			gl.glNormal3d(1, 0, 0);
			gl.glVertex3d(r, r, r);
			gl.glVertex3d(r, -r, r);
			gl.glVertex3d(r, -r, -r);
			gl.glVertex3d(r, r, -r);

			gl.glNormal3d(-1, 0, 0);
			gl.glVertex3d(-r, r, -r);
			gl.glVertex3d(-r, -r, -r);
			gl.glVertex3d(-r, -r, r);
			gl.glVertex3d(-r, r, r);

			gl.glNormal3d(0, 1, 0);
			gl.glVertex3d(r, r, r);
			gl.glVertex3d(r, r, -r);
			gl.glVertex3d(-r, r, -r);
			gl.glVertex3d(-r, r, r);

			gl.glNormal3d(0, -1, 0);
			gl.glVertex3d(-r, -r, r);
			gl.glVertex3d(-r, -r, -r);
			gl.glVertex3d(r, -r, -r);
			gl.glVertex3d(r, -r, r);

			gl.glNormal3d(0, 0, 1);
			gl.glVertex3d(r, r, r);
			gl.glVertex3d(-r, r, r);
			gl.glVertex3d(-r, -r, r);
			gl.glVertex3d(r, -r, r);

			gl.glNormal3d(0, 0, -1);
			gl.glVertex3d(r, -r, -r);
			gl.glVertex3d(-r, -r, -r);
			gl.glVertex3d(-r, r, -r);
			gl.glVertex3d(r, r, -r);

		}
		gl.glEnd();
	}

	/**
	 * Given two lines, one through points a and b, the other through the
	 * points c and d, compute a value t that minimizes the distance from
	 * the point (tb + (1-t)a) to the line through c and d.
	 */
	public static float lineNearLine(Vector3f a, Vector3f b, Vector3f c, Vector3f d) {
		double t = 0;
		Vector3f ab = new Vector3f(b);
		ab.sub(a);

		Vector3f cd = new Vector3f(d);
		cd.sub(c);

		// compute edge vectors of a plane
		// e1 = d-c  and
		// e2 = e1 cross (b-a)
		Vector3f e1 = new Vector3f(cd);
		Vector3f e2 = new Vector3f();
		e2.cross(e1, ab);

		// intersect the line ab with the plane
		// passing through c with edge vectors e1, e2, to yield t
		Vector3f p = new Vector3f();
		Vector3f s = new Vector3f();
		Vector3f q = new Vector3f();

		p.cross(ab,e2);
		double r = e1.dot(p);

		if (r < 1e-5 && r > -1e-5) return 0;
		double f = (1.0 / r);
		s.set(a);
		s.sub(c);
		q.cross(s,e1);
		t = f * e2.dot(q);
		// jack's your uncle
		return (float)t;
	}
	
	/**
	 * Stolen from http://snippets.dzone.com/posts/show/1335
	 */
	public static String readFileAsString(String filePath) throws IOException{
	    byte[] buffer = new byte[(int) new File(filePath).length()];
	    BufferedInputStream f = null;
	    try {
	        f = new BufferedInputStream(new FileInputStream(filePath));
	        f.read(buffer);
	    } finally {
	        if (f != null) try { f.close(); } catch (IOException ignored) { }
	    }
	    return new String(buffer);
	}
}
