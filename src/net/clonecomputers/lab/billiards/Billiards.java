package net.clonecomputers.lab.billiards;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static java.lang.Math.*;

public class Billiards extends JPanel{
	private BufferedImage canvas;
	private double circleRadius;
	public static void main(String[] args) {
		final int w = 600, h = 600;
		final Billiards b = new Billiards(w,h);
		EventQueue.invokeLater(new Runnable(){

			@Override
			public void run() {
				JFrame window = new JFrame("Billiards");
				window.add(b);
				b.setPreferredSize(new Dimension(w+10,h+10));
				window.pack();
				window.setResizable(false);
				window.setVisible(true);
				window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		});
		b.start();
	}
	
	public String ask(String what){
		return JOptionPane.showInputDialog(this, what);
	}
	
	public Billiards(int width, int height){
		canvas = new BufferedImage(width+1,height+1,BufferedImage.TYPE_INT_ARGB);
	}
	
	public void drawBackground(){
		Graphics g = canvas.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.setColor(Color.BLACK);
		//g.drawRect(0, 0, canvas.getWidth()-1, canvas.getHeight()-1);
		int wdiameter = (int)(circleRadius*canvas.getWidth()/3);
		int hdiameter = (int)(circleRadius*canvas.getHeight()/3);
		g.drawOval(canvas.getWidth()/2 - wdiameter/2, canvas.getHeight()/2 - hdiameter/2, wdiameter, hdiameter);
	}
	
	public void start(){
		circleRadius = Double.parseDouble(ask("Input circle radius"));
		drawBackground();
		double ballAngle = Double.parseDouble(ask("Input initial angle"))*PI/180.0;
		double maxDistance = Double.parseDouble(ask("Input distance to travel"));
		int howMany = Integer.parseInt(ask("How many?"));
		if(howMany < 2) {
			start(new double[]{0,-3}, ballAngle,maxDistance, true, true);
			return;
		}
		double[][] finalPoints = new double[howMany][];
		double dx = Double.parseDouble(ask("Input x error"));
		double dtheta = Double.parseDouble(ask("Input theta error"))*PI/180.0;
		for(int n = 0; n < howMany; n++){
			double x = (random()*2-1)*dx;
			double theta = ballAngle + (random()*2-1)*dtheta;
			finalPoints[n] = start(new double[]{x,-3}, theta, maxDistance, n<10, false);
		}
		
		double[] sum = {0,0};
		for(double[] p: finalPoints){
			sum[0]+=p[0];
			sum[1]+=p[1];
		}
		double[] average = {
				sum[0]/finalPoints.length,
				sum[1]/finalPoints.length,
		};
		double dSum = 0;
		for(double[] p: finalPoints){
			dSum += sqrt(dist2(p,average));
		}
		double dAvg = dSum/finalPoints.length;
		System.out.println("Average distance to midpoint: "+dAvg);
		
		Graphics g = canvas.getGraphics();
		g.setColor(Color.RED);
		for(double[] p: finalPoints){
			g.fillOval(xgp(p[0])-4, ygp(p[1])-4, 8, 8);
		}
		repaint();
	}
	
	public double[] start(double[] pos, double ballAngle, double maxDistance, boolean outputGUI, boolean outputSteps){
		double[] oldpos = pos;
		double sideAngle = Double.NaN;
		double curDist = 0;
		for(int i = 0; curDist < maxDistance; i++){
			if(outputSteps) System.out.println("#"+i+": "+p(pos));
			oldpos = pos;
			pos = findSide(pos, ballAngle);
			double oldDist = curDist;
			curDist += sqrt(dist2(pos,oldpos));
			if(curDist > maxDistance) pos = moveBackwards(oldpos, pos, maxDistance - oldDist);
			sideAngle = findSideAngle(pos);
			ballAngle = findBallAngle(ballAngle,sideAngle);
			if(outputGUI) draw(pos, oldpos);
		}
		if(true){
			Graphics g = canvas.getGraphics();
			g.setColor(Color.RED);
			g.fillOval(xgp(pos[0])-4, ygp(pos[1])-4, 8, 8);
			repaint();
		}
		return pos;
	}

	private double[] moveBackwards(double[] pos, double[] newPos, double d) {
		double r = sqrt(dist2(pos,newPos));
		double dx = newPos[0] - pos[0];
		double dy = newPos[1] - pos[1];
		return new double[]{pos[0]+dx*(d/r), pos[1]+dy*(d/r)};
	}

	private synchronized void draw(double[] pos, double[] oldpos) {
		Graphics g = canvas.getGraphics();
		g.setColor(new Color(0,0,255,200));
		int gx1 = xgp(pos[0]), gy1 = ygp(pos[1]), gx2 = xgp(oldpos[0]), gy2 = ygp(oldpos[1]);
		g.drawLine(gx1, gy1, gx2, gy2);
		this.repaint();
	}
	
	private int xgp(double a){
		return (int)(canvas.getWidth()*(3+a)/6);
	}
	
	private int ygp(double a){
		return (int)(canvas.getHeight()*(3-a)/6);
	}
	
	@Override
	public synchronized void paintComponent(Graphics g){
		super.paintComponent(g);
		g.drawImage(canvas, 5, 5, this);
		g.drawRect(4, 4, canvas.getWidth()+1, canvas.getHeight()+1);
	}

	private double findBallAngle(double ballAngle, double sideAngle) {
		double a = ballAngle;
		a=((a%(PI*2))+(PI*10))%(PI*2);
//		if(Double.isNaN(sideAngle)){
//			return ballAngle + PI;
//		}
//		double shiftedBallAngle = ballAngle - sideAngle;
//		double shiftedNewBallAngle = PI - shiftedBallAngle;
//		double newBallAngle = shiftedNewBallAngle + sideAngle;
		//return PI - ballAngle + (2*sideAngle);
		return (2*sideAngle) - a;
	}

	private double findSideAngle(double[] pos) {
		if(pos[0] == -3 || pos[0] == 3){
			return PI/2;
		}else if(pos[1] == -3 || pos[1] == 3){
			return 0;
		}else{
			return atan2(pos[1],pos[0]) + (PI/2);
		}
	}

	private double[] findSide(double[] pos, double ballAngle) {
		double a = ballAngle;
		a=((a%(PI*2))+(PI*10))%(PI*2);
		if(onEdge(pos)){
			double[] circlePos = findCircle(pos, a);
			if(circlePos != null){
				return circlePos;
			}
		}
		return findEdge(pos, a);
	}

	private double[] findEdge(double[] pos, double a){
		//System.out.println("Angle = " + a*180/PI);
		//System.out.println("Currently at ("+pos[0]+","+pos[1]+")");
		boolean hitsTop = a>0 && a<PI,
				hitsRight = !(a>PI/2 && a<3*PI/2);
		double tbInt,lrInt;
		if(hitsTop){
			tbInt = 3*(cos(a)/sin(a)) + pos[0] - pos[1]*(cos(a)/sin(a));
		}else{
			tbInt = -3*(cos(a)/sin(a)) + pos[0] - pos[1]*(cos(a)/sin(a));
		}
		if(hitsRight){
			lrInt = 3*(sin(a)/cos(a)) + pos[1] - pos[0]*(sin(a)/cos(a));
		}else{
			lrInt = -3*(sin(a)/cos(a)) + pos[1] - pos[0]*(sin(a)/cos(a));
		}
		if(tbInt <= 3 && tbInt >= -3){
			if(hitsTop){
				return new double[]{tbInt, 3};
			}else{
				return new double[]{tbInt, -3};
			}
		}else{
			if(hitsRight){
				return new double[]{3, lrInt};
			}else{
				return new double[]{-3, lrInt};
			}
		}
	}
	
	private double[] findCircle(double[] pos, double ballAngle){
		// Polar Cordinates FTW!
		double rc = circleRadius;
		double x=pos[0],y=pos[1],theta=ballAngle,gamma=theta+PI/2;
		double r0 = y*cos(theta) - x*sin(theta);
		if(r0 > rc || r0 < -rc) return null;
		double phi1 = gamma + acos(r0/rc);
		double phi2 = gamma - acos(r0/rc);
		double[] p1 = {rc*cos(phi1), rc*sin(phi1)};
		double[] p2 = {rc*cos(phi2), rc*sin(phi2)};
		
		if(dist2(p1,pos) < dist2(p2,pos)){
			return p1;
		}else{
			return p2;
		}
	}
	
	public double dist2(double[] p1, double[] p2){
		return ((p1[0] - p2[0]) * (p1[0] - p2[0])) + ((p1[1] - p2[1]) * (p1[1] - p2[1]));
	}
	
	@SuppressWarnings("unused")
	private String p(double[] pos){
		return "(" + pos[0] + "," + pos[1] + ")";
	}
	
	private boolean onEdge(double[] pos){
		return pos[0] == -3 ||
				pos[0] == 3 ||
				pos[1] == -3 ||
				pos[1] == 3;
	}
}
