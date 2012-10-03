package cs4621.ppa1.manip;

import javax.media.opengl.GL2;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import cs4621.ppa1.util.Util;

public class RotateManip extends Manip
{
	@Override
	public void dragged(Vector2f mousePosition, Vector2f mouseDelta)
	{
		// TODO: (Problem 1) Implement this manipulator.
	}

	private Vector3f xAxis = new Vector3f();
	private Vector3f yAxis = new Vector3f();
	private Vector3f zAxis = new Vector3f();
	boolean circleDepth = true;

	@Override
	public void glRender(GL2 gl, double scale, boolean pickingMode)
	{
		gl.glPushAttrib(GL2.GL_COLOR);

		gl.glPushAttrib(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_LIGHTING);

		gl.glPushAttrib(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_DEPTH_TEST);

		gl.glLineWidth(5);

		gl.glPushMatrix();

		transformationNode.glTranslateToOriginInWorldSpace(gl);

		gl.glScaled(scale, scale, scale);

		if (pickingMode)
			gl.glLoadName(PICK_CENTER);
		gl.glColor4d(0.8, 0.8, 0, 1);
		Util.glRenderBox(gl);

		if(circleDepth)
			gl.glEnable(GL2.GL_DEPTH_TEST);

		transformationNode.getLowestTransformationNodeAncestorBasisInWorldSpace(xAxis, yAxis, zAxis);

		gl.glPushMatrix();
		gl.glRotatef(transformationNode.rotation.z, zAxis.x, zAxis.y, zAxis.z);
		gl.glRotatef(transformationNode.rotation.y, yAxis.x, yAxis.y, yAxis.z);
		Util.glRotateYTo(gl,xAxis);
		gl.glColor4d(0.8, 0, 0, 1);
		if (pickingMode)
			gl.glLoadName(PICK_X);
		glRenderCircle(Y_AXIS, gl);
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glRotatef(transformationNode.rotation.z, zAxis.x, zAxis.y, zAxis.z);
		Util.glRotateYTo(gl,yAxis);
		gl.glColor4d(0, 0.8, 0, 1);
		if (pickingMode)
			gl.glLoadName(PICK_Y);
		glRenderCircle(Y_AXIS, gl);
		gl.glPopMatrix();

		gl.glPushMatrix();
		Util.glRotateYTo(gl,zAxis);
		gl.glColor4d(0, 0, 0.8, 1);
		if (pickingMode)
			gl.glLoadName(PICK_Z);
		glRenderCircle(Y_AXIS, gl);
		gl.glPopMatrix();

		gl.glPopMatrix();

		gl.glLineWidth(1);

		gl.glPopAttrib();
		gl.glPopAttrib();
		gl.glPopAttrib();
	}

	private static double circleDivs = 256;

	private static void glRenderCircle(byte axis, GL2 gl) {
		gl.glPushMatrix();
		switch (axis) {
		case X_AXIS:
			gl.glRotatef(90f, 0, 0, -1);
			break;
		case Z_AXIS:
			gl.glRotatef(90f, 1, 0, 0);
		}

		// neck ring
		gl.glBegin(GL2.GL_LINE_LOOP);
		for (double i = 0; i <= circleDivs; ++i) {
			double theta = (i / circleDivs) * Math.PI * 2;
			gl.glVertex3d(Math.cos(theta) * 2, 0, Math.sin(theta) * 2);
		}
		gl.glEnd();

		gl.glPopMatrix();
	}
}
