package cs4621.ppa1.shape;

import java.util.HashMap;
import java.util.Map;

public class Sphere extends TriangleMesh 
{
	//sphere vert/norm layout
	//[0: top origin]
	//[3: bottom origin]
	//[6..3*points+6 ..3*points*rings+6: latitudes]
	
	//sphere tri layout:
	//[0..3*points: top cap][3*points+3..6*points+3: bottom cap]
	//[6*points+6..6*points*(rings+1)+6: latitudes]
	
	@Override
	public void buildMesh(float tolerance)
	{
		//points = longitude
		int points = Math.max((int)((Math.PI*2)/tolerance),3);
		//rings = latitude - spans pi instead of 2pi so /2
		int rings = points/2+points%2-1; //-1 due to top/bot
		
		vertices = new float[3*points*rings+9];
		normals = new float[3*points*rings+9];
		triangles = new int[6*points*(rings+1)+9];
		
		//put in origins
		//+y
		vertices[0] = 0;
		vertices[1] = 1;
		vertices[2] = 0;
		normals[0] = 0;
		normals[1] = 1;
		normals[2] = 0;

		// -y
		vertices[3] = 0;
		vertices[4] = -1;
		vertices[5] = 0;
		normals[3] = 0;
		normals[4] = -1;
		normals[5] = 0;

		//draw some circles
		float[] ycap = TriangleMesh.generateCircle(1f, points);
		
		//draw the rest of the fucking owl.
		{
			float theta = (float)(-Math.PI/2);
			float delta_theta = (float)Math.PI/(rings+1);
		
			int v_off = 6;
		
			for(int ring = 0;ring<rings;ring++) {
				theta+=delta_theta;
			
				float dr = (float)Math.cos(theta);
				float dy = (float)Math.sin(theta);
				for(int vert=0;vert<points;vert++) {
					vertices[v_off+(3*vert)+(3*ring*points)  ] = ycap[3*vert  ]*dr;
					vertices[v_off+(3*vert)+(3*ring*points)+1] = ycap[3*vert+1]-dy;
					vertices[v_off+(3*vert)+(3*ring*points)+2] = ycap[3*vert+2]*dr;
					normals[v_off+(3*vert)+(3*ring*points)  ] = ycap[3*vert  ]*dr;
					normals[v_off+(3*vert)+(3*ring*points)+1] = ycap[3*vert+1]-dy;
					normals[v_off+(3*vert)+(3*ring*points)+2] = ycap[3*vert+2]*dr;
				}
			}
		}
		
		// generate tris for top.
		{
			int v_off = 2; // vertex buffer offset
			int t_off = 0; // triangle buffer offset
			int v_orig = 0; //origin target
			// point 0 is the middle. Start from 2, go mid+point behind
			// you+point.

			int CUTOFF = 10000;

			int v_ctr = v_off+1;
			int t_ctr = t_off;
			while (CUTOFF > 0) {
				triangles[t_ctr] = v_orig;
				triangles[t_ctr + 1] = v_ctr - 1;
				triangles[t_ctr + 2] = v_ctr;

				if (t_ctr == t_off + 3 * (points - 2)) {
					triangles[t_ctr + 3] = v_orig;
					triangles[t_ctr + 4] = v_ctr;
					triangles[t_ctr + 5] = v_off;
					break;
				}
				t_ctr += 3;
				v_ctr++;
				CUTOFF--;
			}
		}
		// generate tris for bot.
		{
			int v_off = (3*points*rings+6)/3-points; // vertex buffer offset
			int t_off = 3*points+3; // triangle buffer offset
			int v_orig = 1; //origin target
			// point 0 is the middle. Start from 2, go mid+point behind
			// you+point.

			int CUTOFF = 10000;

			int v_ctr = v_off+1;
			int t_ctr = t_off;
			while (CUTOFF > 0) {
				triangles[t_ctr] = v_orig;
				triangles[t_ctr + 1] = v_ctr - 1;
				triangles[t_ctr + 2] = v_ctr;

				if (t_ctr == t_off + 3 * (points - 2)) {
					triangles[t_ctr + 3] = v_orig;
					triangles[t_ctr + 4] = v_ctr;
					triangles[t_ctr + 5] = v_off;
					break;
				}
				t_ctr += 3;
				v_ctr++;
				CUTOFF--;
			}
		}
		
		//go through rings
		{
			//join ring 0 to ring 1, ring 1 to ring 2, etc.
			for(int ring = 0;ring<rings-1;ring++) {
				int v_off = (3*points*ring+6)/3;	//vertex buffer offset
				int t_off = 6*points*(ring+1)+6;	 //tri buffer offset
				
				int v_off_b = points; //vertex buffer offset to bottom
				
				int CUTOFF = 10000;

				int v_ctr = v_off;
				int t_ctr = t_off;
				while (CUTOFF > 0) {
					//generate quad
					triangles[t_ctr    ] = v_ctr;
					triangles[t_ctr + 1] = v_ctr+v_off_b;
					triangles[t_ctr + 2] = v_ctr+v_off_b+1;

					triangles[t_ctr + 3] = v_ctr+v_off_b+1;
					triangles[t_ctr + 4] = v_ctr+1;
					triangles[t_ctr + 5] = v_ctr;
					
					if (t_ctr == t_off + 6 *(points - 1)) {
						triangles[t_ctr    ] = v_ctr;
						triangles[t_ctr + 1] = v_ctr+v_off_b;
						triangles[t_ctr + 2] = v_off+v_off_b;

						triangles[t_ctr + 3] = v_off+v_off_b;
						triangles[t_ctr + 4] = v_off;
						triangles[t_ctr + 5] = v_ctr;
						break;
					}
					t_ctr += 6;
					v_ctr++;
					CUTOFF--;
				}
			}
		}

	}

	@Override
	public Object getYamlObjectRepresentation()
	{
		Map<Object,Object> result = new HashMap<Object, Object>();
		result.put("type", "Sphere");
		return result;
	}
}
