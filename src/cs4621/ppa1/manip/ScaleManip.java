package cs4621.ppa1.manip;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import cs4621.ppa1.scene.TransformationNode;
import cs4621.ppa1.util.Util;

public class ScaleManip extends Manip {
	
	@Override
	public void dragged(Vector2f mousePosition, Vector2f mouseDelta)
	{
		// TODO: (Problem 1) Implement this manipulator.
	}

	Vector3f xManipBasis = new Vector3f();
	Vector3f yManipBasis = new Vector3f();
	Vector3f zManipBasis = new Vector3f();
	Vector3f manipOrigin = new Vector3f();

	private void initManipBasis()
	{
		// and apply this rotation, since scale happens before rotation
		xManipBasis.set(eX);
		yManipBasis.set(eY);
		zManipBasis.set(eZ);

		transformationNode.rotate(eX, xManipBasis);
		transformationNode.rotate(eY, yManipBasis);
		transformationNode.rotate(eZ, zManipBasis);

		xManipBasis.add(transformationNode.translation);
		yManipBasis.add(transformationNode.translation);
		zManipBasis.add(transformationNode.translation);

		// get origin
		transformationNode.toWorld(e0, manipOrigin);

		if (transformationNode.getLowestTransformationNodeAncestor() != null)
		{
			TransformationNode parent = transformationNode.getLowestTransformationNodeAncestor();

			parent.toWorld(xManipBasis, xManipBasis);
			parent.toWorld(yManipBasis, yManipBasis);
			parent.toWorld(zManipBasis, zManipBasis);
		}

		xManipBasis.sub(manipOrigin);
		yManipBasis.sub(manipOrigin);
		zManipBasis.sub(manipOrigin);

		xManipBasis.normalize();
		yManipBasis.normalize();
		zManipBasis.normalize();
	}

	public void glRender(GL2 gl, double scale, boolean pickingMode)
	{
		gl.glPushAttrib(GL2.GL_COLOR);

		gl.glPushAttrib(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_LIGHTING);

		gl.glPushAttrib(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_DEPTH_TEST);

		gl.glPushMatrix();

		transformationNode.glTranslateToOriginInWorldSpace(gl);

		gl.glScaled(scale, scale, scale);

		initManipBasis();

		gl.glPushMatrix();
		Util.glRotateYTo(gl,xManipBasis);
		gl.glColor4d(0.8, 0, 0, 1);
		if (pickingMode)
			gl.glLoadName(PICK_X);
		glRenderBoxOnAStick(gl);
		gl.glPopMatrix();

		gl.glPushMatrix();
		Util.glRotateYTo(gl,yManipBasis);
		gl.glColor4d(0, 0.8, 0, 1);
		if (pickingMode)
			gl.glLoadName(PICK_Y);
		glRenderBoxOnAStick(gl);
		gl.glPopMatrix();

		gl.glPushMatrix();
		Util.glRotateYTo(gl,zManipBasis);
		gl.glColor4d(0, 0, 0.8, 1);
		if (pickingMode)
			gl.glLoadName(PICK_Z);
		glRenderBoxOnAStick(gl);
		gl.glPopMatrix();

		if (pickingMode)
			gl.glLoadName(PICK_CENTER);
		gl.glColor4d(0.8, 0.8, 0, 1);
		Util.glRenderBox(gl);

		gl.glPopMatrix();

		gl.glPopAttrib();
		gl.glPopAttrib();
		gl.glPopAttrib();
	}

	private static void glRenderBoxOnAStick(GL2 gl) {
		glRenderBoxOnAStick(Y_AXIS, gl);
	}

	private static void glRenderBoxOnAStick(byte axis, GL2 gl) {
		gl.glPushMatrix();
		switch (axis) {
		case X_AXIS:
			gl.glRotatef(90f, 0, 0, -1);
			break;
		case Z_AXIS:
			gl.glRotatef(90f, 1, 0, 0);
		}

		gl.glPushAttrib(GL2.GL_CURRENT_BIT);
		gl.glColor4f(1,1,1,1);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(0,0,0);
		gl.glVertex3f(0,2,0);
		gl.glEnd();
		gl.glPopAttrib();

		gl.glTranslatef(0,2,0);
		Util.glRenderBox(gl);

		gl.glPopMatrix();
	}
}
