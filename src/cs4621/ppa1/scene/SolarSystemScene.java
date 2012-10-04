package cs4621.ppa1.scene;

import java.io.IOException;

import cs4621.ppa1.scene.*;
import java.util.HashMap;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public class SolarSystemScene extends Scene {
	
	// simple class to hold data on planetary motion
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
	
	// maps planet names to planetary data
	protected HashMap<String, PlanetData> planet_data;
	
    Matrix3f tmp_rotation;
    
	public SolarSystemScene() {
		planet_data = new HashMap< String, PlanetData >();
		planet_data.put( "Sun", new PlanetData( 0f, 0f ) );
		planet_data.put( "Mars", new PlanetData( 0.025f, 15.0f ) );
		
		// defines orbit of earth and moon around sun
		planet_data.put( "EarthGroup", new PlanetData( 0.05f, 0f ) );
		
		// defines earth revolution
		planet_data.put( "Earth", new PlanetData( 0f, 2.0f ) );
		
		// defines moon revolution and orbit
		planet_data.put( "Moon", new PlanetData( 0.2f, 5.0f ) );
		
		tmp_rotation = new Matrix3f();
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
		
		// make sure node is actually a planet
		if( data == null ) return;
		
		// revolution
		node.rotation.y = time * data.revolution_degrees_per_step;
		
		// rotation
		
		// save initial translation for orbit computation
		if( data.initial_translation == null )
			data.initial_translation = new Vector3f( node.translation );
		
		float rotation_angle = time * data.rotation_degrees_per_step;
		Vector3f translation = node.translation;
		translation.set( data.initial_translation );
		tmp_rotation.rotY( rotation_angle );
	    tmp_rotation.transform( translation );
	}
		
}
