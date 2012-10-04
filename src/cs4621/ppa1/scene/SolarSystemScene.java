package cs4621.ppa1.scene;

import java.io.IOException;

import cs4621.ppa1.scene.*;
import java.util.HashMap;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public class SolarSystemScene extends Scene {
	
	private class PlanetData {
		public float rotation_degrees_per_step;
		public float revolution_degrees_per_step;
		public Vector3f initial_translation;
		
		public PlanetData( float rot, float rev )
		{
			rotation_degrees_per_step = rot;
			revolution_degrees_per_step = rev;
			initial_translation = null;
		}
	}
	
	protected HashMap<String, PlanetData> planet_data;
    Matrix3f tmp_rotation;
    float time_prev;
    
	public SolarSystemScene() {
		planet_data = new HashMap< String, PlanetData >();
		planet_data.put( "Sun", new PlanetData( 0f, 0f ) );
		planet_data.put( "EarthGroup", new PlanetData( 0.05f, 0f ) );
		planet_data.put( "Earth", new PlanetData( 0f, 2.0f ) );
		planet_data.put( "Moon", new PlanetData( 0.2f, 5.0f ) );
		planet_data.put( "Mars", new PlanetData( 0.025f, 15.0f ) );
		tmp_rotation = new Matrix3f();
		time_prev = 0;
	}

	public SceneNode buildInitialScene() {
		try {
			// Load the starting scene from a file.
			return SceneNode.load("data/scenes/solar_system.txt");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	
	// cancel out parent revolution, since that shouldn't affect position of satellites
    // sets v = (S^-1)(R^-1)Sv, converting to parent frame then gives TRS(S^-1)(R^-1)Sv = TSv
	protected void cancelParentRotation( TransformationNode node, float time, Vector3f v )
	{
		TransformationNode parent = node.getLowestTransformationNodeAncestor();
		if( parent != null )
		{
			Matrix4f m = new Matrix4f();
//			m.setScale(arg0)
			
			// transform to parent space
			v.x *= parent.scaling.x;
			v.y *= parent.scaling.y;
			v.z *= parent.scaling.z;
			
			// undo parent revolution
			float parent_rev = parent.rotation.y;
			tmp_rotation.rotZ( -parent.rotation.z );
		    tmp_rotation.transform( v );
			tmp_rotation.rotY( -time * parent_rev );
		    tmp_rotation.transform( v );
			tmp_rotation.rotX( -parent.rotation.x );
		    tmp_rotation.transform( v );
		    
		    // transform back to object frame
			v.x /= parent.scaling.x;
			v.y /= parent.scaling.y;
			v.z /= parent.scaling.z;
		}
	}

   	Vector3f previous_world_translation = new Vector3f();
	public void updateTransformationNode(TransformationNode node, float time) {
		String name = node.name;
		PlanetData data = planet_data.get( name );
		
		if( data == null ) return;
		
		// save initial translation for computing orbits later
		if( data.initial_translation == null )
			data.initial_translation = new Vector3f( node.translation );
		
		// revolution
		node.rotation.y = time * data.revolution_degrees_per_step;
		
		// rotation
		float rotation_angle = time * data.rotation_degrees_per_step;
		Vector3f translation = node.translation;
		translation.set( data.initial_translation );
		tmp_rotation.rotY( rotation_angle );
	    tmp_rotation.transform( translation );
		
//		cancelParentRotation( node, time, translation );
	}
		
}
