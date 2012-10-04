package cs4621.ppa1.scene;

import java.io.IOException;

import cs4621.ppa1.scene.*;
import java.util.HashMap;

import javax.vecmath.Matrix3f;
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
		planet_data.put( "Earth", new PlanetData( 0.1f, 2.0f ) );
		planet_data.put( "Moon", new PlanetData( 0.1f, 5.0f ) );
		planet_data.put( "Mars", new PlanetData( 0.05f, 5.0f ) );
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
		
		// cancel out parent revolution, since that shouldn't affect position of satellites
		TransformationNode parent = node.getLowestTransformationNodeAncestor();
		float parent_rev = 0f;
		if( parent != null )
			parent_rev = parent.rotation.y;
			
		Vector3f translation = node.translation;
		Vector3f previous_translation = new Vector3f( node.translation );
		
		translation.set( data.initial_translation );
		tmp_rotation.rotY( rotation_angle - parent_rev );
	    tmp_rotation.transform( translation );
		
	    if( name.equals("Moon"))
	    {
	    	// a bunch of debug calculations
	    	Vector3f world_translation = new Vector3f( node.translation );
	    	float cos_angle = world_translation.dot(previous_world_translation) / ( world_translation.length() * previous_world_translation.length() );
	    	float angle = ( float )( Math.acos( cos_angle ) * 180.0 / Math.PI );
	    	tmp_rotation.rotY( parent_rev );
	    	tmp_rotation.transform( world_translation );
	    	previous_world_translation.set( world_translation );
	    	
	    	System.out.printf( "Rotation: %f, Parent Revolution: %f, Net Rotation: %f\n", rotation_angle, 
	    			parent_rev, rotation_angle - parent_rev );
	    	System.out.printf( "Original translation: %s, New Translation: %s, Fully Rotated Translation: %s\n", 
	    			previous_translation, node.translation, world_translation );
	    	System.out.println( "World space update angle: " + angle );
	    }
	    
	    time_prev = time;
	}

}
