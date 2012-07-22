package visualization;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class CircleGraph {


	private static LinkedList<Edge> edgeList;
	private static TreeSet<Vertex> verticesList;
	private static int[] maxWeights = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};

	public static void read() throws IOException {
		String path = "C:\\Users\\Felix\\Dropbox\\ProteinPrediction\\report\\transmembrane_2012-07-15.txt";

		verticesList = new TreeSet<Vertex>(new VertexComparator());
		edgeList = new LinkedList<Edge>();

		BufferedReader br = new BufferedReader(new FileReader(path));

		Pattern pattern = Pattern.compile("<edge.+id=\"([^\"]+)\".*source=\"([^\"]+)\".*target=\"([^\"]+)\"");

		String s;
		while ((s = br.readLine()) != null) {
			if (!s.startsWith("<edge")) {
				continue;
			}

			Matcher matcher = pattern.matcher(s);

			matcher.find();

			Vertex from = new Vertex(matcher.group(2));
			Vertex to = new Vertex(matcher.group(3));

			if (from.aa.length() > 1 || to.aa.length() > 1) {
				continue;
			}

			verticesList.add(from);
			verticesList.add(to);

			int[] weights = new int[3];
			String[] weightsStrs = matcher.group(1).split(":");
			for (int i = 0; i < 3; i++) {
				weights[i] = Integer.parseInt(weightsStrs[i]);
			}

			edgeList.add(new Edge(from, to, weights));
		}

		for (Vertex v : verticesList) {
			System.out.println(v);
		}

		//find maxweights

		for (Edge e : edgeList) {
			if (maxWeights[0] < e.weights[0]) {
				maxWeights[0] = e.weights[0];
			}
			if (maxWeights[1] < e.weights[1]) {
				maxWeights[1] = e.weights[1];
			}
			if (maxWeights[2] < e.weights[1]) {
				maxWeights[2] = e.weights[2];
			}
		}

	}

	public static void main(String[] args) throws IOException {

		read();

//		if(true) System.exit(0);

		BufferedImage image = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = (Graphics2D) image.getGraphics();

		g.setColor(Color.white);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());

		RenderingHints rhints = g.getRenderingHints();

		// Enable antialiasing for shapes
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

		// Enable antialiasing for text
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


		g.setColor(Color.black);


		int radius = 800;

		int originX = 1000;
		int originY = 1000;

		double labelDist = 40;
		int dotRadius = 10;
		int fontSize = 20;
		int minStrokeThickness = 1;
		int maxStrokeThickness = 3;
		int lightestColor = 200;

		//g.drawOval(50, 50, 900, 900);

		g.setStroke(new BasicStroke(minStrokeThickness));

		HashMap<Vertex, Point> vertices = new HashMap<Vertex, Point>();

		double angleInDegrees = 0;
		double increment = 360 / (float) verticesList.size();

		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));

		for (Vertex v : verticesList) {
			int x = Math.round((float) (radius * Math.cos(angleInDegrees * Math.PI / 180F)) + originX);
			int y = Math.round((float) (radius * Math.sin(angleInDegrees * Math.PI / 180F)) + originY);

			g.fillOval((int) x - dotRadius / 2, (int) y - dotRadius / 2, dotRadius, dotRadius);
			System.out.println(v);
			vertices.put(v, new Point(x, y));

			int xLabel = Math.round((float) ((radius + labelDist) * Math.cos(angleInDegrees * Math.PI / 180F)) + originX);
			int yLabel = Math.round((float) ((radius + labelDist) * Math.sin(angleInDegrees * Math.PI / 180F)) + originY);

			String label = v.aa + ":" + v.sse;

			int labelW = g.getFontMetrics().stringWidth(label);
			int labelH = g.getFontMetrics().getHeight();

			double ascend = (-y + yLabel) / labelDist;
			double hdiff = (x - xLabel) / labelDist;
			System.out.println("ASCEND " + ascend);

			xLabel -= labelW * hdiff + labelW / 2;
			yLabel += labelH * ascend;

			g.drawString(label, xLabel, yLabel);

			angleInDegrees += increment;
		}

		Random ruediger = new Random();

		Collections.sort(edgeList, new Comparator<Edge>() {
			@Override
			public int compare(Edge o1, Edge o2) {
				return -(o2.weights[1] - o1.weights[1]);
			}
		});

		for (Edge e : edgeList) {
//			System.out.println(e);

			System.out.println(e.weights[1]);

			Point from = vertices.get(e.from);
			Point to = vertices.get(e.to);

//			System.out.println(from);
//			System.out.println(to);

			g.setComposite(makeComposite(0.5f));
			
			int color = (lightestColor- (int) (e.weights[1] / (float)(maxWeights[1]/(float)lightestColor)));
			
//			color = (int) (lightestColor - ( (lightestColor)* ( Math.pow(e.weights[1], 2) / Math.pow(maxWeights[1], 2) ) ));
			
			int thickness = (int) Math.round( minStrokeThickness + 
					(maxStrokeThickness-minStrokeThickness) 
						* ( Math.pow(e.weights[1], 2) / Math.pow(maxWeights[1], 2) ) );
			
			
			

			g.setStroke(new BasicStroke(thickness));

			g.setColor(new Color(color, color, color));

			QuadCurve2D.Float curve = new QuadCurve2D.Float(from.x, from.y, originX, originY, to.x, to.y);
			g.draw(curve);
		}

//		for(Vertex k : vertices.keySet()) {
//			for(Vertex l : vertices.keySet()) {
//				if(ruediger.nextInt(50000)>49958) {
//					Point from = vertices.get(k);
//					Point to = vertices.get(l);
//
//					g.setComposite(makeComposite(0.5f));
//
//					System.out.println("draw");
//					g.setColor(Color.GRAY);
//
////					int color = ruediger.nextInt(25)*10;
//					int color = 180;
//
//					if(ruediger.nextInt(20)==0)
//						color-=100;
//
//					if(ruediger.nextInt(50)==0)
//						color = 0;
//
//					g.setColor(new Color(color, color, color));
//
//					QuadCurve2D.Float curve = new QuadCurve2D.Float(from.x, from.y, 500, 500, to.x, to.y);
//					g.draw(curve);
//				}
//			}
//		}

		File outputfile = new File("n:\\temp\\temp.png");
		ImageIO.write(image, "png", outputfile);

		new Viewer(image);
	}

	private static AlphaComposite makeComposite(float alpha) {
		int type = AlphaComposite.SRC_OVER;
		return (AlphaComposite.getInstance(type, alpha));
	}
}
