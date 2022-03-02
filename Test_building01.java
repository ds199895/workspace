package archi;

import java.util.Random;

import peasy.PeasyCam;
import processing.core.PApplet;

public class Test_building01 extends PApplet{	
	PeasyCam cam;
	Render render;
	
	Building building;	
	public void setup() {
		size(1000, 1000, P3D);
		cam = new PeasyCam(this, 1000);
		cam.setMinimumDistance(50);
		cam.setMaximumDistance(500);

		render = new Render(this);			
		
		building = new Building(200,50,500);  		
		building.setSeed(100); //100,102,103,104,150,200,
		
		building.addSlice(50,50,50,50,50,50,50,50,50);				
		building.addSlice(30, 7000);	
	}

	public void draw() {
		background(255);
		directionalLight(255, 255, 255, 1, 1, -1);
		directionalLight(200, 200, 200, -1, -1, 1);
		
		building.draw(render);

		drawSystem();
	}

	public static void main(String[] args) {
		PApplet.main("archi.Test_building01");
	}
	
	private void drawSystem() {
		this.pushStyle();
		this.stroke(255, 0, 0);
		this.line(0, 0, 0, 10000, 0, 0);
		this.stroke(0, 255, 0);
		this.line(0, 0, 0, 0, 10000, 0);
		this.stroke(0, 0, 255);
		this.line(0, 0, 0, 0, 0, 10000);
		this.popStyle();
	}
}
