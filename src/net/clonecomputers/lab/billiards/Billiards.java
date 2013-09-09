package net.clonecomputers.lab.billiards;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static java.lang.Math.*;

public class Billiards extends JPanel{
	private BufferedImage canvas = new BufferedImage(300,300,BufferedImage.TYPE_INT_RGB);
	public static void main(String[] args) {
		final Billiards b = new Billiards();
		EventQueue.invokeLater(new Runnable(){

			@Override
			public void run() {
				JFrame window = new JFrame("Billiards");
				window.add(b);
				b.setPreferredSize(new Dimension(300,300));
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
	
	public Billiards(){
		
	}
	
	public void drawBackground(){
		Graphics g = canvas.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, canvas.getWidth()-1, canvas.getHeight()-1);
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, canvas.getWidth()-2, canvas.getHeight()-2);
		g.drawOval(canvas.getWidth()/3, canvas.getHeight()/3, canvas.getWidth()/3, canvas.getHeight()/3);
	}
	
	public void start(){
		drawBackground();
		double ballAngle = Double.parseDouble(ask("Input initial angle"))*PI/180.0;
		double[] pos = { 0, -3, };
		double[] oldpos = pos;
		double sideAngle = Double.NaN;
		int nBounces = Integer.parseInt(ask("Input number of bounces"));
		for(int i = 0; i < nBounces; i++){
			oldpos = pos;
			pos = findSide(pos, ballAngle);
			sideAngle = findSideAngle(pos);
			ballAngle = findBallAngle(ballAngle,sideAngle);
			draw(pos, oldpos);
		}
	}

	private synchronized void draw(double[] pos, double[] oldpos) {
		Graphics g = canvas.getGraphics();
		g.setColor(Color.BLACK);
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
		g.drawImage(canvas, 0, 0, this);
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
		double dist = (pos[0]*pos[0]) + (pos[1]*pos[1]);
		if(dist < 2){
			return atan2(pos[1],pos[0]) + (PI/2);
		}else if(pos[0] == -3 || pos[0] == 3){
//			if(pos[1] == -3 || pos[1] == 3){
//				return Double.NaN;
//			}else{
				return PI/2;
//			}
		}else{
			return 0;
		}
	}

	private double[] findSide(double[] pos, double ballAngle) {
		double a = ballAngle;
		a=((a%(PI*2))+(PI*10))%(PI*2);
		double d2 = (pos[0]*pos[0])+(pos[1]*pos[1]);
		if(d2>2){
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
		// TODO Fix Method
		double x1 = pos[0], y1 = pos[1];
		double x2 = pos[0]+cos(ballAngle), y2 = pos[1]+sin(ballAngle);

		double dx = x2 - x1;
		double dy = y2 - y1;
		double dr = sqrt(dx*dx + dy*dy);
		double D = x1*y2 - x2*y1;
		double s = dr*dr - D*D;
		if(!(s<=0)){
			double px1 = (D*dy + signum(dy)*dx*sqrt(s)) / (dr*dr);
			double px2 = (D*dx - signum(dy)*dx*sqrt(s)) / (dr*dr);
			double py1 = (-D*dy + abs(dy)*sqrt(s)) / (dr*dr);
			double py2 = (-D*dx - abs(dy)*sqrt(s)) / (dr*dr);
			double[] pos1 = {px1, py1};
			double[] pos2 = {px2, py2};
			double d1 = hypot(px1 - pos[0], py1 - pos[1]);
			double d2 = hypot(px2 - pos[0], py2 - pos[1]);
			if(d1 < d2) return pos1;
			else return pos2;
		}
		return null;
	}
	
	private String p(double[] pos){
		return "(" + pos[0] + "," + pos[1] + ")";
	}
}
