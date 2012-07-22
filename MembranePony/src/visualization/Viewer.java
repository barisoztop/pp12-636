package visualization;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class Viewer extends JFrame {
	public Viewer(Image img) {
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		add(new JScrollPane(new JLabel(new ImageIcon(img))));

		pack();
		setVisible(true);
	}
}