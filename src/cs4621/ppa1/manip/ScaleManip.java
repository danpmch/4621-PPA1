package cs4621.ppa1.manip;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import cs4621.ppa1.scene.TransformationNode;
import cs4621.ppa1.util.Util;

public class ScaleManip extends Manip {
	
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
		
		switch(axisMode) {
		case PICK_X:
			axis = new Vector3f(1f,0f,0f);	
			break;
			
		case PICK_Y:
			axis = new Vector3f(0f,1f,0f);	
			break;
		
		case PICK_Z:
			axis = new Vector3f(0f,0f,1f);	
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
		
		Vector3f trans = new Vector3f();
		
		//compose translation vector
		switch(axisMode) {
		case PICK_X:
			trans = new Vector3f(
					mousePoint.length()/mousePointOffset.length(),
					transformationNode.scaling.y,
					transformationNode.scaling.z);
			break;
			
		case PICK_Y:
			trans = new Vector3f(
					transformationNode.scaling.x,
					mousePoint.length()/mousePointOffset.length(),
					transformationNode.scaling.z);
			break;
		
		case PICK_Z:
			trans = new Vector3f(
					transformationNode.scaling.x,
					transformationNode.scaling.y,
					mousePoint.length()/mousePointOffset.length());
			break;
			
		case PICK_CENTER:
			trans = new Vector3f(mousePoint.x/mousePointOffset.x,
								 mousePoint.y/mousePointOffset.y,
								 mousePoint.z/mousePointOffset.z);
			break;
		}
		
		System.out.println(">>> Scale :("+trans.x+","+trans.y+","+trans.z+")");
		
		Vector3f dbgPlaneNorm = new Vector3f(transformationNode.translation);
		dbgPlaneNorm.add(planeNormal);
	
		transformationNode.scaling.set(trans);
		
	}

	public void released() {
		super.released();
		resetState();
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
