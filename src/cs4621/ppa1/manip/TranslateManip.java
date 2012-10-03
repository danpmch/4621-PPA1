package cs4621.ppa1.manip;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import cs4621.ppa1.util.Util;

public class TranslateManip extends Manip
{
	
	boolean firstDrag = true;
	Vector3f mousePointOffset = new Vector3f();
	
	@Override
	public void dragged(Vector2f mousePosition, Vector2f mouseDelta)
	{
		System.out.println("Dragging translation:");
		
		Vector3f camDir = new Vector3f(camera.target.x-camera.eye.x,
				   camera.target.y-camera.eye.y, 
				   camera.target.z-camera.eye.z);
		camDir.normalize();
		
		//get mouse ray
		Vector3f mr1 = new Vector3f();
		Vector3f mr2 = new Vector3f();
		camera.getLineThroughNDC(mousePosition, mr1, mr2);
		Vector3f mouseRay = new Vector3f(mr2.x-mr1.x,mr2.y-mr1.y,mr2.z-mr1.z);
		
		Vector3f planeNormal = new Vector3f();
		Vector3f axis = new Vector3f();
		
		switch(axisMode) {
		case PICK_X:
			if(transformationNode.getLowestTransformationNodeAncestor()==null) {
				axis = new Vector3f(1f,0f,0f);	
			} else {
			transformationNode.getLowestTransformationNodeAncestor().
				toWorld(new Vector3f(1f,0f,0f), axis);
			}
			break;
			
		case PICK_Y:
			if(transformationNode.getLowestTransformationNodeAncestor()==null) {
				axis = new Vector3f(0f,1f,0f);	
			} else {
			transformationNode.getLowestTransformationNodeAncestor().
				toWorld(new Vector3f(0f,1f,0f), axis);
			}
			break;
		
		case PICK_Z:
			if(transformationNode.getLowestTransformationNodeAncestor()==null) {
				axis = new Vector3f(0f,0f,1f);	
			} else {
			transformationNode.getLowestTransformationNodeAncestor().
				toWorld(new Vector3f(0f,0f,1f), axis);
			}
			break;
			
		case PICK_CENTER:
			planeNormal.add(camDir);
			//special case axis: points to mousepoint.
			//so, do nothing.
			break;
		}
		
		//do plane normal

		//do not need axis for pickcenter, because
		//plane normal will subtract the projection,
		//which will be 0, and norm will point at cam
		if(!(axis.x==0&&axis.y==0&&axis.z==0))
			axis.normalize();
		
		float mag = axis.dot(camDir);
		if(Float.isNaN(mag)) mag = 0;
		
		
		Vector3f planeProj = new Vector3f(
			axis.x*mag,
			axis.y*mag,
			axis.z*mag
		);
		
		//perpendicularize plane normal to axis
		planeNormal.set(camDir);
		planeNormal.sub(planeProj);
		
		planeNormal.normalize();
		Vector3f orig = new Vector3f();
		
		System.out.println(">>> Plane normal:("
				+planeNormal.x+","
				+planeNormal.y+","
				+planeNormal.z+")");
		
		//convert object origin to world space.
		if(transformationNode.getLowestTransformationNodeAncestor()!=null) {
			transformationNode.getLowestTransformationNodeAncestor().
				toWorld(transformationNode.translation, orig);
		}
		
		//raycast camera onto origin/normal plane to get mouse point
		Vector3f mousePoint = PlaneRaycast(orig,planeNormal,
				new Vector3f(camera.eye),mouseRay);
		
		if(firstDrag) {
			firstDrag = false;
			mousePointOffset.set(mousePoint);
		}
		
		System.out.println(">>> Mouse point:("
				+mousePoint.x+","
				+mousePoint.y+","
				+mousePoint.z+")");
		
		//now that mousepoint is known, can do special case
		if(axisMode==PICK_CENTER) {
			axis = new Vector3f(mousePoint.x-orig.x,
							    mousePoint.y-orig.y,
							    mousePoint.z-orig.z);
		}
		
		System.out.println(">>> Axis Vector: ("+axis.x+","+axis.y+","+axis.z+")");
		
		Vector3f trans = new Vector3f();
		
		//compose translation vector
		switch(axisMode) {
		case PICK_X:
			trans = new Vector3f(mousePoint.x-mousePointOffset.x,
					transformationNode.translation.y,
					transformationNode.translation.z);
			break;
			
		case PICK_Y:
			trans = new Vector3f(
					transformationNode.translation.x,
					mousePoint.y-mousePointOffset.y,
					transformationNode.translation.z);
			break;
		
		case PICK_Z:
			trans = new Vector3f(
					transformationNode.translation.x,
					transformationNode.translation.y,
					mousePoint.z-mousePointOffset.z);
			break;
			
		case PICK_CENTER:
			trans = new Vector3f(mousePoint);
			trans.sub(mousePointOffset);
			break;
		}
		
		System.out.println(">>> Raw Translation :("+trans.x+","+trans.y+","+trans.z+")");
		
		//transform the translation to localspace
		
		Vector3f lossyScale = transformationNode.
			getLowestTransformationNodeAncestor().lossyScale();
		
		Vector3f rtrans = new Vector3f(trans);
		//	trans.x/lossyScale.x,
		//	trans.y/lossyScale.y,
		//	trans.z/lossyScale.z
		//);
		
		if(transformationNode.getLowestTransformationNodeAncestor()!=null)
			transformationNode.getLowestTransformationNodeAncestor().toLocal(trans,rtrans);
		//transformationNode.toLocal(trans,rtrans);
			
		System.out.println(">>> Translation :("+rtrans.x+","+rtrans.y+","+rtrans.z+")");
		
		transformationNode.translation.set(rtrans);
		
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
