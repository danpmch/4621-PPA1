package cs4621.ppa1.shape;

import java.util.HashMap;
import java.util.Map;

public class Cylinder extends TriangleMesh
{
	
	//Cylinder vert/norm layout:
	//[0:+y origin ][3..3*points+3:+y cap]
	//  [3*points+6..6*points+6:+y side]
	//   [6*points+9:-y origin][6*points+12..9*points+12: -y cap]
	//	  [9*points+15..12*points+15:-y side]
	
	
	//cylinder tri layout:
	//[0..3*points:+y cap][3*points+3..6*points+3:-y cap]
	// [9*points+6..15*points+6: quads for sides]
	
	@Override
	public void buildMesh(float tolerance)
	{
		//subdivisions: at least 3
		int points = Math.max((int)((Math.PI*2)/tolerance),3);
		//init arrays
		vertices = new float[12*points+18];
		normals = new float[12*points+18];
		triangles = new int[15*points+9];
		
		//put in two origins
		//+y
		vertices[0] = 0;
		vertices[1] = 1;
		vertices[2] = 0;
		normals[0] = 0;
		normals[1] = 1;
		normals[2] = 0;

		// -y
		vertices[6 * points + 9] = 0;
		vertices[6 * points + 9 + 1] = -1;
		vertices[6 * points + 9 + 2] = 0;
		normals[6 * points + 9] = 0;
		normals[6 * points + 9 + 1] = -1;
		normals[6 * points + 9 + 2] = 0;

		// generate the circle
		float[] ycap = TriangleMesh.generateCircle(1f, points);

		// positive cap
		{
			int v_off = 3;
			for (int i = 0; i < ycap.length; i += 3) {
				vertices[i + v_off] = ycap[i];
				vertices[i + v_off + 1] = 1;
				vertices[i + v_off + 2] = ycap[i + 2];
				normals[i + v_off] = 0;
				normals[i + v_off + 1] = 1;
				normals[i + v_off + 2] = 0;
			}
		}
		
		// negative cap
		{
			int v_off = 6 * points + 12;
			for (int i = 0; i < ycap.length; i += 3) {
				vertices[i + v_off] = ycap[i];
				vertices[i + v_off + 1] = -1;
				vertices[i + v_off + 2] = ycap[i + 2];
				normals[i + v_off] = 0;
				normals[i + v_off + 1] = -1;
				normals[i + v_off + 2] = 0;
			}
		}
		
		// generate sides -> different normals
		{
			int v_off = 3 * points + 6;
			for (int i = 0; i < ycap.length; i += 3) {
				vertices[i + v_off] = ycap[i];
				vertices[i + v_off + 1] = 1;
				vertices[i + v_off + 2] = ycap[i + 2];
				normals[i + v_off] = ycap[i];
				normals[i + v_off + 1] = 0;
				normals[i + v_off + 2] = ycap[i + 2];
			}
		}
		{
			int v_off = 9 * points + 15;
			for (int i = 0; i < ycap.length; i += 3) {
				vertices[i + v_off] = ycap[i];
				vertices[i + v_off + 1] = -1;
				vertices[i + v_off + 2] = ycap[i + 2];
				normals[i + v_off] = ycap[i];
				normals[i + v_off + 1] = 0;
				normals[i + v_off + 2] = ycap[i + 2];
			}
		}
		
		// generate tris for top.
		{
			int v_off = 1; // vertex buffer offset
			int t_off = 0; // triangle buffer offset
			// point 0 is the middle. Start from 2, go mid+point behind
			// you+point.

			int CUTOFF = 10000;

			int v_ctr = v_off + 1;
			int t_ctr = t_off;
			while (CUTOFF > 0) {
				triangles[t_ctr] = v_off - 1;
				triangles[t_ctr + 1] = v_ctr - 1;
				triangles[t_ctr + 2] = v_ctr;

				if (t_ctr == t_off + 3 * (points - 2)) {
					triangles[t_ctr + 3] = v_off - 1;
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
			int v_off = (6 * points + 9) / 3 + 1;// vertex buffer offset
			int t_off = 3 * points + 3; // triangle buffer offset
			// point 0 is the middle. Start from 2, go mid+point behind
			// you+point.

			int CUTOFF = 10000;

			int v_ctr = v_off + 1;
			int t_ctr = t_off;
			while (CUTOFF > 0) {
				triangles[t_ctr] = v_off - 1;
				triangles[t_ctr + 1] = v_ctr - 1;
				triangles[t_ctr + 2] = v_ctr;

				if (t_ctr == t_off + 3 * (points - 2)) {
					triangles[t_ctr + 3] = v_off - 1;
					triangles[t_ctr + 4] = v_ctr;
					triangles[t_ctr + 5] = v_off;
					break;
				}
				t_ctr += 3;
				v_ctr++;
				CUTOFF--;
			}
		}

		//generate tris for sides: go around sides, make quads.
		{
			int v_off = (3*points+6)/3;	//vertex buffer offset
			int t_off = 9*points+6;	 //tri buffer offset
			
			int v_off_b = (2*points+3); //vertex buffer offset to bottom
			
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

	@Override
	public Object getYamlObjectRepresentation()
	{
		Map<Object,Object> result = new HashMap<Object, Object>();
		result.put("type", "Cylinder");
		return result;
	}
}
