package net.clonecomputers.lab.billiards;

import static java.lang.Math.*;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.apache.commons.csv.*;
//import javax.imageio.ImageIO;

@SuppressWarnings("serial")
public class Billiards extends JPanel{
	private BufferedImage canvas;
	private double circleRadius;
	private int hitCircle;
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
		b.run();
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
	
	public void run(){
		circleRadius = Double.parseDouble(ask("Input circle radius"));
		drawBackground();
		double ballAngle = Double.parseDouble(ask("Input initial angle"))*PI/180.0;
		double maxDistance = Double.parseDouble(ask("Input distance to travel"));
		int howMany = Integer.parseInt(ask("How many?"));
		/*if(howMany < 2) {
			run(new double[]{0,-3}, ballAngle,maxDistance, true, true);
			return;
		}*/
		//double[][] finalPoints = new double[howMany][];
		double dx = Double.parseDouble(ask("Input x error"));
		double dtheta = Double.parseDouble(ask("Input theta error"))*PI/180.0;
		/*
		for(int n = 0; n < howMany; n++){
			double x = (random()-.5)*dx;
			double theta = ballAngle + (random()-.5)*dtheta;
			finalPoints[n] = run(new double[]{x,-3}, theta, maxDistance, n<10, false);
		}
		System.out.println("Average distance to midpoint: "+avgDistToMidpoint(finalPoints));
		*/
		
		int numSteps = Integer.parseInt(ask("Input number of steps"));
		double[][] points = new double[howMany][]; // arr of {x,y,theta}
		double[][] interestingStuff = new double[numSteps][2];
		for(int i = 0; i < points.length; i++){
			points[i] = new double[3];
			points[i][0] = (random()-.5)*dx;
			points[i][1] = -3;
			points[i][2] = ballAngle + (random()-.5)*dtheta;
		}
		for(int i = 0; i < numSteps; i++){
			for(int n = 0; n < howMany; n++){
				points[n] = run(new double[]{points[n][0],points[n][1]},points[n][2],maxDistance/numSteps,n<10,false);
			}
			repaint();
			interestingStuff[i] = interestingProperties(points);
		}
		
		//System.out.println(Arrays.deepToString(interestingStuff));
		/*Double[][] printableStuff = new Double[interestingStuff[0].length][interestingStuff.length];
		for(int i = 0; i < interestingStuff.length; i++){
			for(int j = 0; j < interestingStuff[i].length; j++){
				printableStuff[j][i] = interestingStuff[i][j];
			}
		}*/
		
		Object[][] stuffToPrint = new Object[interestingStuff.length+4][];
		int i = 0;
		stuffToPrint[i++] = new String[]{
				"radius",
				"angle",
				"distance",
				"how many",
				"x error",
				"theta error",
				"steps",
		};
		stuffToPrint[i++] = new Double[]{
				circleRadius,
				ballAngle*180/PI,
				maxDistance,
				(double) howMany,
				dx,
				dtheta*180/PI,
				(double) numSteps,
		};
		stuffToPrint[i++] = new String[]{
				"average distance from midpoint",
				"maximum minimum distance",
				"what fraction hit the circle",
				"maximum minimum distance of velocity vectors (angle)"
		};
		for(int l = 0; l < interestingStuff.length; l++){
			stuffToPrint[l+i] = new Object[interestingStuff[l].length];
			for(int j = 0; j < interestingStuff[l].length; j++){
				stuffToPrint[l+i][j] = interestingStuff[l][j];
			}
		}
		//System.out.println(Arrays.deepToString(stuffToPrint));
		JFileChooser chooser = new JFileChooser();
		chooser.showSaveDialog(this);
		File f = chooser.getSelectedFile();
		CSVPrinter csv;
		try {
			csv = new CSVPrinter(new FileWriter(f), CSVFormat.EXCEL);
		} catch (IOException e2) {
			throw new RuntimeException(e2);
		}
		try {
			csv.printRecords(stuffToPrint);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}finally{
			try {
				csv.close();
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}
		
		Graphics2D g = canvas.createGraphics();
		g.setColor(Color.RED);
		for(double[] p: points){
			g.fillOval(xgp(p[0])-4, ygp(p[1])-4, 8, 8);
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		repaint();

	}

	public double averageDistanceFromMidpoint(double[][] points){
		double[] sum = {0,0};
		for(double[] p: points){
			sum[0]+=p[0];
			sum[1]+=p[1];
		}
		double[] average = {
				sum[0]/points.length,
				sum[1]/points.length,
		};
		double dSum = 0;
		for(double[] p: points){
			dSum += sqrt(dist2(p,average));
		}
		return dSum/points.length;
	}
	
	public double maximumMinimumDistance(double[][] points){
		double maxMin2 = 0;
		for(int i = 0; i < points.length; i++){
			double min2 = 10000;
			for(int j = 0; j < points.length; j++){
				double d2 = dist2(points[i],points[j]);
				if(j != i && d2 < min2) min2 = d2;
			}
			if(min2 > maxMin2) maxMin2 = min2;
		}
		return sqrt(maxMin2);
	}
	
	public double[] interestingProperties(double[][] points){
		double cTmp = hitCircle;
		hitCircle = 0;
		return new double[]{
				averageDistanceFromMidpoint(points),
				maximumMinimumDistance(points),
				cTmp/(double)points.length, // no int division
				maximumMinimumVelocityDistance(points),
		};
	}

	private double maximumMinimumVelocityDistance(double[][] points) {
		double maxMin2 = 0;
		for(int i = 0; i < points.length; i++){
			double min2 = 10000;
			for(int j = 0; j < points.length; j++){
				double d2 = dist2(velocity(points[i]),velocity(points[j]));
				if(j != i && d2 < min2) min2 = d2;
			}
			if(min2 > maxMin2) maxMin2 = min2;
		}
		return sqrt(maxMin2);
	}

	private double[] velocity(double[] point) {
		return new double[]{
				cos(point[2]),
				sin(point[2]),
		};
	}

	public double[] run(double[] pos, double ballAngle, double maxDistance, boolean outputGUI, boolean outputSteps){
		double[] oldpos = pos;
		double sideAngle = Double.NaN;
		double curDist = 0;
		for(int i = 0; curDist < maxDistance; i++){
			int hitCircleStart = hitCircle;
			if(outputSteps) System.out.println("#"+i+": "+p(pos));
			oldpos = pos;
			pos = findSide(pos, ballAngle);
			double oldDist = curDist;
			curDist += sqrt(dist2(pos,oldpos));
			if(curDist > maxDistance){
				pos = moveBackwards(oldpos, pos, maxDistance - oldDist);
				hitCircle = hitCircleStart;
			}else{
				sideAngle = findSideAngle(pos);
				ballAngle = findBallAngle(ballAngle,sideAngle);
			}
			if(outputGUI) draw(pos, oldpos);
		}
		if(false){
			Graphics g = canvas.getGraphics();
			g.setColor(Color.RED);
			g.fillOval(xgp(pos[0])-4, ygp(pos[1])-4, 8, 8);
			repaint();
		}
		return new double[]{pos[0],pos[1],ballAngle};
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
		double circleNormal = atan2(pos[1],pos[0]);
		double ca = (circleNormal - a);
		ca=((ca%(PI*2))+(PI*10))%(PI*2);
		boolean facingCircle = ca > PI/2 && ca < 3*PI/2;
		if(facingCircle){
			double[] circlePos = findCircle(pos, a);
			if(circlePos != null){
				hitCircle++;
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
