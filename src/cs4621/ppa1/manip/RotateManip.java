package cs4621.ppa1.manip;

import javax.media.opengl.GL2;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import cs4621.ppa1.util.Util;

public class RotateManip extends Manip
{
	static boolean dragLock = false;
	
	boolean firstDrag = true;
	Vector3f mousePointOffset = new Vector3f();
	
	Vector3f lastPlaneNormal = new Vector3f();
	
	@Override
	public void dragged(Vector2f mousePosition, Vector2f mouseDelta)
	{
		//return if another manip is active and has lock
		if(firstDrag) {
			if(dragLock) { 
				return;
			} else {
				dragLock = true; //acquire lock
			}
		}
		
		//init cam dir to local space
		Vector3f camDir = new Vector3f();
		Vector3f camDirGlobal = new Vector3f(camera.target.x-camera.eye.x,
				   camera.target.y-camera.eye.y, 
				   camera.target.z-camera.eye.z);
		toParentLocalDirection(camDirGlobal,camDir, transformationNode);
		camDir.normalize();
		
		//init mouse ray to local space
		Vector3f mr1 = new Vector3f();
		Vector3f mr2 = new Vector3f();
		camera.getLineThroughNDC(mousePosition, mr1, mr2);
		Vector3f mouseRayGlobal = new Vector3f(mr2.x-mr1.x,mr2.y-mr1.y,mr2.z-mr1.z);
		Vector3f mouseRay = new Vector3f();
		toParentLocalDirection(mouseRayGlobal, mouseRay, transformationNode);
		mouseRay.normalize();
		
		//init cam eye to local space
		Vector3f camEye = new Vector3f();
		toParentLocalDirection(new Vector3f(camera.eye),
				camEye, transformationNode);
		
		Vector3f planeNormal = new Vector3f();
		Vector3f axis = new Vector3f();
		
		//instead of special plane along axis like
		//Translate, Rotate needs plane with axis as normal.
		switch(axisMode) {
		case PICK_X:
			planeNormal = new Vector3f(1f,0f,0f);	
			break;
			
		case PICK_Y:
			planeNormal = new Vector3f(0f,1f,0f);	
			break;
		
		case PICK_Z:
			planeNormal = new Vector3f(0f,0f,1f);	
			break;
			
		case PICK_CENTER:
			planeNormal.add(camDir);
			//special case axis: points to mousepoint.
			//so, do nothing.
			break;
		}
		
		Vector3f orig = new Vector3f(transformationNode.translation);
		
		System.out.println(">>> Plane normal:("
				+planeNormal.x+","
				+planeNormal.y+","
				+planeNormal.z+")");
		
		//raycast camera onto origin/normal plane to get mouse point
		Vector3f mousePoint = PlaneRaycast(orig,planeNormal,
				camEye,mouseRay);
		
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
		
		Vector3f rot = new Vector3f();
		
		//compose rotation vector
		switch(axisMode) {
		case PICK_X:
			rot = new Vector3f(
					(float)Math.toDegrees(
							mousePoint.angle(mousePointOffset)
					),
					transformationNode.rotation.y,
					transformationNode.rotation.z);
			break;
			
		case PICK_Y:
			rot = new Vector3f(
					transformationNode.rotation.x,
					(float)Math.toDegrees(
							mousePoint.angle(mousePointOffset)
					),
					transformationNode.rotation.z);
			break;
		
		case PICK_Z:
			rot = new Vector3f(
					transformationNode.rotation.x,
					transformationNode.rotation.y,
					(float)Math.toDegrees(
							mousePoint.angle(mousePointOffset)
					));
			break;
			
		case PICK_CENTER:
			System.err.println(" Hell if I know... quats would be nice ");
			break;
		}
		
		System.out.println(">>> Rotation :("+rot.x+","+rot.y+","+rot.z+")");
		
		Vector3f dbgPlaneNorm = new Vector3f(transformationNode.translation);
		dbgPlaneNorm.add(planeNormal);
	
		transformationNode.rotation.set(rot);
		
	}

	public void released() {
		super.released();
		resetState();
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
