package cs4621.ppa1.manip;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import cs4621.ppa1.scene.TransformationNode;
import cs4621.ppa1.util.Util;

public class ScaleManip extends Manip {
	
	// converts a world space vector into a vector with only the local scale applied to it
	protected Vector3f toScaleSpace( Vector3f v )
	{
		Vector3f new_v = new Vector3f();
		transformationNode.toLocal(v, new_v);
		new_v.x = new_v.x * transformationNode.scaling.x;
		new_v.y = new_v.y * transformationNode.scaling.y;
		new_v.z = new_v.z * transformationNode.scaling.z;
		
		return new_v;
	}
	
	// Assumes that axis_local is normalized
	public Vector3f dragged_axis_manip( Vector2f mousePosition, Vector2f mousePosition_old, Vector3f axis_local )
	{
		//get current line through mouse
		Vector3f mouseCurrent_origin_w = new Vector3f();
		Vector3f mouseCurrent_axis_w = new Vector3f();
		
		// find closest point on axis to mouse line
		camera.getLineThroughNDC( mousePosition, mouseCurrent_origin_w, mouseCurrent_axis_w );
		
		double t1 = Util.lineNearLine( e0, 
				toScaleSpace( axis_local ), 
				toScaleSpace( mouseCurrent_origin_w ), 
				toScaleSpace( mouseCurrent_axis_w ) );
		
		//get old line through mouse
		Vector3f mouseOld_origin_w = new Vector3f();
		Vector3f mouseOld_axis_w = new Vector3f();
		
		// find closest point on axis to mouse line
		camera.getLineThroughNDC( mousePosition_old, mouseOld_origin_w, mouseOld_axis_w );
		
		double t0 = Util.lineNearLine( e0, 
				toScaleSpace( axis_local ), 
				toScaleSpace( mouseOld_origin_w ), 
				toScaleSpace( mouseOld_axis_w ) );
		
		// compute change in translation in local space
		double dt = t1 - t0;
		Vector3f translation_change = new Vector3f( axis_local );
		translation_change.scale( ( float ) dt );
		
		return translation_change;
	}
	
	protected void dragged_orig_manip( Vector2f mousePosition, Vector2f mousePosition_old )
	{
		Vector3f trans = get_translation_w(mousePosition, mousePosition_old);
		if( trans.x == 0 || Float.isNaN( trans.x ) ) return;
		
		float a1 = transformationNode.scaling.x + trans.x;
		float b1 = transformationNode.scaling.y + transformationNode.scaling.y / transformationNode.scaling.x * trans.x;
		float c1 = transformationNode.scaling.z + transformationNode.scaling.z / transformationNode.scaling.x * trans.x;
		
		transformationNode.scaling.set( a1, b1, c1 );
	}
	
	@Override
	public void dragged(Vector2f mousePosition, Vector2f mouseDelta)
	{
		Vector2f mousePosition_old = new Vector2f( mousePosition );
		mousePosition_old.sub( mouseDelta );
		
		Vector3f change_in_scale = null;
		switch(axisMode) {
		case PICK_X:
			change_in_scale = dragged_axis_manip( mousePosition, mousePosition_old, eX );
			break;
		case PICK_Y:
			change_in_scale = dragged_axis_manip( mousePosition, mousePosition_old, eY );
			break;
		
		case PICK_Z:
			change_in_scale = dragged_axis_manip( mousePosition, mousePosition_old, eZ );
			break;
			
		case PICK_CENTER:
			mouseDelta.y = 0;
			mousePosition_old.set( mousePosition );
			mousePosition_old.sub( mouseDelta );
			dragged_orig_manip( mousePosition, mousePosition_old );
			return;
		}
		
		transformationNode.scaling.add( change_in_scale );
		
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
