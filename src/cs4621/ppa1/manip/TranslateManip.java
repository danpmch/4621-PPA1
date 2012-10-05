package cs4621.ppa1.manip;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import cs4621.ppa1.util.Util;

public class TranslateManip extends Manip
{
	
	boolean firstDrag = true;
	Vector3f mousePointOffset = new Vector3f();
	
	// Assumes that axis_local is normalized
	public Vector3f dragged_axis_manip( Vector2f mousePosition, Vector2f mousePosition_old, Vector3f axis_local )
	{
		Vector3f axis_world = new Vector3f();
		transformationNode.toWorld( axis_local, axis_world );
		
		Vector3f origin_world = new Vector3f();
		transformationNode.toWorld( new Vector3f( 0f, 0f, 0f ), origin_world );
		
		//get current line through mouse
		Vector3f mouseCurrent_origin = new Vector3f();
		Vector3f mouseCurrent_axis = new Vector3f();
		
		// find closest point on axis to mouse line
		camera.getLineThroughNDC( mousePosition, mouseCurrent_origin, mouseCurrent_axis );
		double t1 = Util.lineNearLine( origin_world, axis_world, mouseCurrent_origin, mouseCurrent_axis );
		
		//get old line through mouse
		Vector3f mouseOld_origin = new Vector3f();
		Vector3f mouseOld_axis = new Vector3f();
		
		// find closest point on axis to mouse line
		camera.getLineThroughNDC( mousePosition_old, mouseOld_origin, mouseOld_axis );
		double t0 = Util.lineNearLine( origin_world, axis_world, mouseOld_origin, mouseOld_axis );
		
		// compute change in translation in local space
		double dt = t1 - t0;
		Vector3f translation_change = new Vector3f( axis_local );
		translation_change.scale( ( float ) dt );
		
		return translation_change;
	}
	
	public Vector3f dragged_orig_manip( Vector2f mousePosition, Vector2f mousePosition_old )
	{
		Vector3f trans = get_translation_w( mousePosition, mousePosition_old );
		
		// convert to object space
		Vector3f trans_local = new Vector3f();
		transformationNode.toLocal(trans, trans_local);
		
		// convert to local space
		Vector3f trans_t = new Vector3f();
		transformationNode.transform(trans_local, trans_t);
		
		return trans_t;
	}
	
	@Override
	public void dragged(Vector2f mousePosition, Vector2f mouseDelta)
	{
		System.out.println("Dragging translation:");
		System.out.println( "Mouse delta: " + mouseDelta );
		Vector2f mousePosition_old = new Vector2f( mousePosition );
		mousePosition_old.sub( mouseDelta );
		System.out.printf( "Mouse Position Current: %s\n", mousePosition );
		System.out.printf( "Mouse Position Old: %s\n", mousePosition_old );
		
		Vector3f change_in_translation = null;
		switch(axisMode) {
		case PICK_X:
			change_in_translation = dragged_axis_manip( mousePosition, mousePosition_old, eX );
			break;
		case PICK_Y:
			change_in_translation = dragged_axis_manip( mousePosition, mousePosition_old, eY );
			break;
		
		case PICK_Z:
			change_in_translation = dragged_axis_manip( mousePosition, mousePosition_old, eZ );
			break;
			
		case PICK_CENTER:
			change_in_translation = dragged_orig_manip( mousePosition, mousePosition_old );
			break;
		}
		
		// update translation
		transformationNode.translation.add( change_in_translation );
	}

	public void released() {
		super.released();
		resetState();
	}
	
	void resetState() {
		firstDrag = true;
	}
	
	private Vector3f xAxis = new Vector3f();
	private Vector3f yAxis = new Vector3f();
	private Vector3f zAxis = new Vector3f();

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

		transformationNode.getLowestTransformationNodeAncestorBasisInWorldSpace(xAxis, yAxis, zAxis);

		gl.glPushMatrix();
		Util.glRotateYTo(gl,xAxis);
		gl.glColor4d(0.8, 0, 0, 1);
		if (pickingMode)
			gl.glLoadName(PICK_X);
		glRenderArrow(gl);
		gl.glPopMatrix();

		gl.glPushMatrix();
		Util.glRotateYTo(gl,yAxis);
		gl.glColor4d(0, 0.8, 0, 1);
		if (pickingMode)
			gl.glLoadName(PICK_Y);
		glRenderArrow(gl);
		gl.glPopMatrix();

		gl.glPushMatrix();
		Util.glRotateYTo(gl,zAxis);
		gl.glColor4d(0, 0, 0.8, 1);
		if (pickingMode)
			gl.glLoadName(PICK_Z);
		glRenderArrow(gl);
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

	private static double arrowDivs = 32;
	private static double arrowTailRadius = 0.05;
	private static double arrowHeadRadius = 0.11;

	public static void glRenderArrow(GL2 gl) {
		glRenderArrow(Y_AXIS, gl);
	}

	public static void glRenderArrow(byte axis, GL2 gl) {

		gl.glPushMatrix();
		switch (axis) {
		case X_AXIS:
			gl.glRotatef(90f, 0, 0, -1);
			break;
		case Z_AXIS:
			gl.glRotatef(90f, 1, 0, 0);
		}
		// tail coney
		double theta = 0;
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex3d(0d, 0d, 0d);
		for (double i = 0; i <= arrowDivs; ++i) {
			theta = (i / arrowDivs) * Math.PI * 2;
			gl.glVertex3d(Math.cos(theta) * arrowTailRadius, 1.8, Math.sin(theta) * arrowTailRadius);
		}
		gl.glEnd();

		// neck ring
		gl.glBegin(GL2.GL_QUAD_STRIP);
		for (double i = 0; i <= arrowDivs; ++i) {
			theta = (i / arrowDivs) * Math.PI * 2;
			gl.glVertex3d(Math.cos(theta) * arrowTailRadius, 1.8, Math.sin(theta) * arrowTailRadius);
			gl.glVertex3d(Math.cos(theta) * arrowHeadRadius, 1.83, Math.sin(theta) * arrowHeadRadius);
		}
		gl.glEnd();

		// head coney
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex3d(0, 2, 0);
		for (double i = 0; i <= arrowDivs; ++i) {
			theta = (i / arrowDivs) * Math.PI * 2;
			gl.glVertex3d(Math.cos(theta) * arrowHeadRadius, 1.83, Math.sin(theta) * arrowHeadRadius);
		}
		gl.glEnd();

		gl.glPopMatrix();
	}
}
