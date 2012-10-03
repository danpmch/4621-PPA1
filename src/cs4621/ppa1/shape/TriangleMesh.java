package cs4621.ppa1.shape;

import javax.media.opengl.GL2;

public abstract class TriangleMesh extends Mesh {
	protected float[] vertices = null;
	protected float[] normals = null;
	protected int[] triangles = null;

	public TriangleMesh()
	{
		super();
	}

	protected void setVertex(int i, float x, float y, float z)
	{
		vertices[3*i]   = x;
		vertices[3*i+1] = y;
		vertices[3*i+2] = z;
	}

	protected void setNormal(int i, float x, float y, float z)
	{
		normals[3*i]   = x;
		normals[3*i+1] = y;
		normals[3*i+2] = z;
	}

	protected void setTriangle(int i, int i0, int i1, int i2)
	{
		triangles[3*i]   = i0;
		triangles[3*i+1] = i1;
		triangles[3*i+2] = i2;
	}
	
	public static int DEBUG_MESH = 0;
	//1. draw axes
	//2: draw verts
	//3: color-code first 6 drawn tris
	//4: ~show vertex norms~ -NOT IMPLEMENTED
	//6: print each drawn vert to console (SLOW!)
	
	public final void render(GL2 gl)
	{
		//assuming here that gl has already been init'd and all that is needed is to draw the shape.
		if(DEBUG_MESH>0) {
			gl.glColor3f(1,0,0);
			gl.glBegin(GL2.GL_LINES);
			{
				gl.glVertex3i(0, 0, 0);
				gl.glVertex3i(1, 0, 0);
			}
			gl.glEnd();
			gl.glColor3f(0,1,0);
			gl.glBegin(GL2.GL_LINES);
			{
				gl.glVertex3i(0, 0, 0);
				gl.glVertex3i(0, 1, 0);
			}
			gl.glEnd();
			gl.glColor3f(0,0,1);
			gl.glBegin(GL2.GL_LINES);
			{
				gl.glVertex3i(0, 0, 0);
				gl.glVertex3i(0, 0, 1);
			}
			gl.glEnd();
		}
		if(DEBUG_MESH>1) {
			gl.glPointSize(3f);
			gl.glColor3f(1,1,1);
			for(int i = 0;i<vertices.length;i+=3) {
				gl.glBegin(GL2.GL_POINTS);

				gl.glVertex3f(vertices[3*triangles[i]], 
					  vertices[3*triangles[i]+1], 
					  vertices[3*triangles[i]+2]);
				gl.glEnd();

			}
		}
		
		//POOSH TRAINGILS
		for(int i = 0; i<triangles.length;i+=3) {
			if(DEBUG_MESH>5) {
				System.out.println("Tri "+(i/3)+
					": Drawing verts "+triangles[i]+", "+triangles[i+1]+", "+triangles[i+2]);
			}
			if(DEBUG_MESH>1) {
				float[] col = new float[3];
				switch(i) {
					case 0: col[0]=1;col[1]=0;col[2]=0; break; //tri 0 = red
					case 3: col[0]=0;col[1]=1;col[2]=0; break; //tri 1 = green
					case 6: col[0]=0;col[1]=0;col[2]=1; break; //tri 2 = blue
					case 9: col[0]=1;col[1]=0;col[2]=1; break; //tri 3 = magenta
					case 12: col[0]=1;col[1]=1;col[2]=0; break; //tri 4 = yellow
					case 15: col[0]=0;col[1]=1;col[2]=1; break; //tri 5 = cyan
					case 18: col[0]=1;col[1]=1;col[2]=1; break; //tri 6 = white
					default: col[0]=0.5f;col[1]=0.5f;col[2]=0.5f; break; //all others = grey
				}
				gl.glColor3f(col[0],col[1],col[2]);
			}
			gl.glBegin(GL2.GL_TRIANGLES);
			{
				//push first vertex
				gl.glNormal3f(normals[3*triangles[i]], 
							  normals[3*triangles[i]+1], 
							  normals[3*triangles[i]+2]);
				
				gl.glVertex3f(vertices[3*triangles[i]], 
							  vertices[3*triangles[i]+1], 
							  vertices[3*triangles[i]+2]);
				
				//push second vertex
				gl.glNormal3f(normals[3*triangles[i+1]], 
						  normals[3*triangles[i+1]+1], 
						  normals[3*triangles[i+1]+2]);
				
				gl.glVertex3f(vertices[3*triangles[i+1]], 
						      vertices[3*triangles[i+1]+1], 
						      vertices[3*triangles[i+1]+2]);
				
				//push third vertex
				gl.glNormal3f(normals[3*triangles[i+2]], 
						  normals[3*triangles[i+2]+1], 
						  normals[3*triangles[i+2]+2]);
				
				gl.glVertex3f(vertices[3*triangles[i+2]], 
						      vertices[3*triangles[i+2]+1], 
						      vertices[3*triangles[i+2]+2]);
				
			}
			gl.glEnd();
		}
	}

	public final void setMeshData(float[] vertices, float[] normals, int[] triangles)
	{
		if (vertices.length % 3 != 0)
			throw new Error("Vertex array's length is not a multiple of 3.");
		if (normals.length % 3 != 0)
			throw new Error("Normal array's length is not a multiple of 3");
		if (vertices.length != normals.length)
			throw new Error("Vertex and normal array are not equal in size.");
	    if (triangles.length % 3 != 0)
	        throw new Error("Triangle array's length is not a multiple of 3.");

	    this.vertices = vertices;
	    this.normals = normals;
	    this.triangles = triangles;
	}
	
	/**
	 * Generates a Y-oriented (y = up) circle of vertices 
	 * @param origin Origin of circle
	 * @param radius radius of circle
	 * @param points # points to generate
	 * @return array of vertices 3*i=x, 3*i+1=y, 3*i+2=z, with point 0 lying on X axis
	 */
	public static float[] generateCircle(float radius, int points) {
		float[] res = new float[3*points];
		
		double deltaTheta = (Math.PI*2)/(double)points;
		
		double theta = 0;
		
		//y = 0, x = cos(theta), y = sin(theta)
		for(int i = 0;i<points;i++) {
			res[3*i]=radius*(float)(Math.cos(theta));
			//y init'd to 0, can ignore
			res[3*i+2]=radius*(float)(Math.sin(theta));
			theta+=deltaTheta;
		}
		return res;
	}
}
