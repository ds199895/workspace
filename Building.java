package archi;

import java.util.ArrayList;
import java.util.Random;

import processing.core.PApplet;
import wblut.geom.WB_AABB;
import wblut.geom.WB_GeometryFactory;
import wblut.geom.WB_Plane;
import wblut.geom.WB_Point;
import wblut.geom.WB_Vector;
import wblut.hemesh.HEC_Box;
import wblut.hemesh.HEM_MultiSliceSurface;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render;

public class Building {
	public static WB_GeometryFactory gf = WB_GeometryFactory.instance ();

	HE_Mesh mesh;
	double minArea = -1;
	double maxArea = -1;

	private Random random;
	public Building(double width, double height, double depth) {
		HEC_Box box = new HEC_Box().setWidth(width).setHeight(height).setDepth(depth);
		mesh = new HE_Mesh(box);
		
		random = new Random();
	}	
	
	public void setSeed(int seed) {
		random.setSeed(seed);
	}
	
	public void addSlice(double...depths) {
		WB_AABB aabb = mesh.getAABB();
		double minZ = aabb.getMinZ();
		
		int num = depths.length;
		WB_Plane[] planes = new WB_Plane[num];
		
		double c_depth = minZ;
		for(int i=0;i<num;i++) {
			c_depth += depths[i];
			System.out.println(c_depth);
			planes[i] = new WB_Plane(0, 0, c_depth, 0, 0, 1);
		}
		
		HEM_MultiSliceSurface modifier = new HEM_MultiSliceSurface();
		modifier.setPlanes(planes);
		modifier.setOffset(0);

		mesh.modify(modifier);		
	}

	public void addSlice(double minArea, double maxArea) {
		boolean tooBig = true;
		while (tooBig) {
			HE_Mesh mesgCPY = mesh.get();

			WB_AABB aabb = mesgCPY.getAABB();
			WB_Point rp = randomAABB(aabb);

			WB_Plane[] plane = new WB_Plane[1];
			
			plane[0] = new WB_Plane(rp.xd(), rp.yd(), rp.zd(), random.nextDouble() - 0.5, random.nextDouble() - 0.5,
					random.nextDouble() - 0.5);

			HEM_MultiSliceSurface modifier = new HEM_MultiSliceSurface();
			modifier.setPlanes(plane);
			modifier.setOffset(0);
			mesgCPY.modify(modifier);
			//�ж��Ƿ�������С���
			boolean tooSmall = false;
			for (HE_Face f : mesgCPY.getFaces()) {
				double curArea = f.getFaceArea();
				if (curArea < minArea) {
					tooSmall = true;
				}
			}
			if (!tooSmall) {
				this.mesh = mesgCPY;
			}			
			//����������Ƿ�����ж��Ƿ���Ҫ�����ʷ�
			tooBig = false;
			for (HE_Face f : this.mesh.getFaces()) {
				double curArea = f.getFaceArea();
				if (curArea > maxArea) {
					tooBig = true;
				}
			}			
			System.out.println(this.mesh.getEdges().size());
		}
		this.minArea = minArea;
		this.maxArea = maxArea;
		
		this.setFaces();
	}	

	ArrayList<Face>faces = null;
	private void setFaces() {
		faces = new ArrayList<Face>();
		
		int classify_num = 3;
		double area_step = (this.maxArea-this.minArea)/(float)classify_num;
		for(HE_Face he_f:mesh.getFaces()) {			
			Face f = new Face(he_f);
			//��f���ʵ�Ԥ����
			//��face���������С����Ϊ��������
			double f_area = f.he_face.getFaceArea();
			for(int i=0;i<classify_num;i++) {
				if((f_area>=this.minArea + area_step*i) && (f_area<=this.minArea + area_step*(i+1))) {
					f.he_face.setUserLabel(i);
				}
			}
			f.simpleOffset();
//			System.out.println(f.he_face.getLabel());
			faces.add(f);
		}
		
//		for(Face fc:faces) {
//			System.out.println(fc.he_face.getLabel());
//		}
	}

	private WB_Point randomAABB(WB_AABB aabb) {
		WB_Point minPoint = aabb.getMin();
		WB_Point maxPoint = aabb.getMax();
		WB_Point delta = maxPoint.sub(minPoint);
		delta.set(delta.xd() * random.nextDouble(), delta.yd() * random.nextDouble(), delta.zd() * random.nextDouble());
		return minPoint.add(delta);
	}

	public void draw(Render render) {
		PApplet app = render.getApp();
		WB_Render wb_render = render.getWB_Render();
		
		app.pushStyle();
		//draw mesh
		app.stroke(0);
		wb_render.drawEdges(mesh);
		app.noStroke();
		wb_render.drawFaces(mesh);
		
		//draw normal of faces
		app.stroke(255,255,0);
		for(HE_Face f: mesh.getFaces()) {
			WB_Point cnt = f.getFaceCenter();
			WB_Vector nor = f.getFaceNormal();
			nor.scaleSelf(10);

			app.line(cnt.xf(),cnt.yf(),cnt.zf(),nor.xf()+cnt.xf(),nor.yf()+cnt.yf(),nor.zf()+cnt.zf());
		}
		app.popStyle();

		//draw offset faces
		if(faces != null) {
			for(Face f:faces) {
				f.draw(render);
			}
		}
	}
}
