package archi;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import wblut.geom.*;
import wblut.geom.WB_Coord;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HEM_Lattice;
import wblut.hemesh.HEM_MultiSliceSurface;
import wblut.hemesh.HEM_Noise;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Mesh;
import wblut.hemesh.HE_Vertex;
import wblut.processing.WB_Render;

public class Face {
	HE_Face he_face;
	
	//WB_Polygon[]polygons;	
	HE_Mesh fMesh = null;
	public Face(HE_Face face) {
		this.he_face = face;
	}
	
	public void simpleOffset() {
		List<HE_Vertex>vs = he_face.getFaceVertices();
		
//		if(he_face.getUserLabel()==0) {
//			fMesh = setPolygon0(vs);
//		}
		if(he_face.getUserLabel()==1) {
			fMesh = setPolygon1(vs,2);
		}		
//		if(he_face.getUserLabel()==2) {
//			fMesh = setPolygon2(vs);
//		}
	}
	
	private HE_Mesh setPolygon0(List<HE_Vertex> vs) {		
		return rawShape(vs, 0.15, 1);
	}
	
	private HE_Mesh setPolygon1(List<HE_Vertex> vs, double roughGap) {		
		HE_Mesh raw_mesh = rawShape(vs, 0, 15);	
		
		WB_AABB aabb = raw_mesh.getAABB();
		double minZ = aabb.getMinZ();
		int gapNum = (int)(aabb.getDepth()/roughGap);
		double realGap = aabb.getDepth()/gapNum;
		
		WB_Plane[] planes = new WB_Plane[gapNum];
		
		double c_depth = minZ;
		for(int i=0;i<gapNum;i++) {
			c_depth += realGap*2;
			planes[i] = new WB_Plane(0, 0, c_depth, 0, 0, 1);
		}
		
		HEM_MultiSliceSurface modifier0 = new HEM_MultiSliceSurface();
		modifier0.setPlanes(planes);
		modifier0.setOffset(0);
		raw_mesh.modify(modifier0);
				
		HEM_Lattice modifier1 = new HEM_Lattice();
		modifier1.setWidth(1);     	// desired width of struts
		modifier1.setDepth(3);     	// depth of struts
		modifier1.setThresholdAngle(1.5);      	// treat edges sharper than this angle as hard edges
		modifier1.setFuse(true);      			// try to fuse planar adjacent planar faces created by the extrude
		modifier1.setFuseAngle(0.1);      		// threshold angle to be considered coplanar
		raw_mesh.modify(modifier1);
		
		return raw_mesh;
	}

	
	private HE_Mesh setPolygon2(List<HE_Vertex> vs) {		
		
		HE_Mesh raw_mesh = rawShape(vs, 0, 20);	
		
		HEM_Noise modifier = new HEM_Noise();
		modifier.setDistance(5);
		raw_mesh.modify(modifier);
		
		HE_Mesh raw_mesh2 = raw_mesh.get();
		HEM_Lattice modifier1 = new HEM_Lattice();
		modifier1.setWidth(2);     	// desired width of struts
		modifier1.setDepth(2);     	// depth of struts
		modifier1.setThresholdAngle(1.5);      	// treat edges sharper than this angle as hard edges
		modifier1.setFuse(true);      			// try to fuse planar adjacent planar faces created by the extrude
		modifier1.setFuseAngle(0.1);      		// threshold angle to be considered coplanar		            
		raw_mesh2.modify(modifier1);		
		raw_mesh.add(raw_mesh2);
		
//		HEM_Noise modifier = new HEM_Noise();
//		modifier.setDistance(20);
//		raw_mesh.modify(modifier);
//		raw_mesh.smooth();
//		raw_mesh.smooth();
		
		return raw_mesh;				
	}
	
	private HE_Mesh rawShape(List<HE_Vertex> vs, double r, double extrude) {
		WB_Point center = he_face.getFaceCenter();
		WB_Vector nor = he_face.getFaceNormal();	
		nor.scaleSelf(extrude);
		
		List<WB_Polygon>polygons = new ArrayList<WB_Polygon>();

		List<WB_Coord>ori_cs = new ArrayList<WB_Coord>();
		for(HE_Vertex v:vs) {
			WB_Point cp = new WB_Point(v);
			ori_cs.add(cp);
		}
		List<WB_Coord>off_cs = new ArrayList<WB_Coord>();
		for(HE_Vertex v:vs) {
			WB_Point cp = new WB_Point(v);
			WB_Point offc = seg_ratio(cp,center,r);
			offc.addSelf(nor);
			off_cs.add(offc);
		}

		for(int i=0;i<ori_cs.size();i++) {
			int n = (i+1) % ori_cs.size();
			
			List<WB_Coord>coords = new ArrayList<WB_Coord>();
			coords.add(ori_cs.get(i));
			coords.add(ori_cs.get(n));
			coords.add(off_cs.get(n));
			coords.add(off_cs.get(i));
			
			WB_Polygon poly = Building.gf.createSimplePolygon(coords);
			polygons.add(poly);			
		}
		
		WB_Polygon poly = Building.gf.createSimplePolygon(off_cs);
		polygons.add(poly);
		
		HE_Mesh rawMesh = new HE_Mesh(new HEC_FromPolygons().setPolygons(polygons));
		return rawMesh;
	}
	
	private WB_Point seg_ratio(WB_Point sp, WB_Point ep, double r) {
		WB_Point del = ep.sub(sp);
		del.scaleSelf(r);
		return sp.add(del);
	}
	
	public void draw(Render render) {
		PApplet app = render.getApp();
		WB_Render wb_render = render.getWB_Render();
				
		app.pushStyle();		
		app.stroke(255,0,0);
		app.fill(255);
		if(fMesh!=null) {
			app.stroke(0);
			wb_render.drawEdges(fMesh);
			app.noStroke();
			wb_render.drawFaces(fMesh);
		}				
		app.popStyle();
	}	
}

