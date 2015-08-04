import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

// By: Justin Spedding & Andrew Miller

public class ImagePanel extends JPanel {

	private static final long serialVersionUID = 4419850305952644041L;
	private BufferedImage image;
	private String text;

	public ImagePanel() {
		image = null;
		text = null;
	}

	public void loadImage(BufferedImage image) {
		this.image = image;
		this.text = null;
		this.repaint();
	}

	public void loadText(String text) {
		this.text = text;
		this.image = null;
		this.repaint();
	}

	public boolean isImage() {
		return image != null;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (text != null) {
			g.drawString(text, (getWidth() - new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics().getFontMetrics(new Font("Ariel", Font.PLAIN, 12)).stringWidth(text))/2, getHeight()/2);
		} else if (image != null) {
			// Scale it by width
			int scaledWidth = (int) ((image.getWidth() * getHeight() / image.getHeight()));
			// If the image is not off the screen horizontally...
			if (scaledWidth < getWidth()) {
				// Center the left and right destination x coordinates.
				int leftOffset = getWidth() / 2 - scaledWidth / 2;
				int rightOffset = getWidth() / 2 + scaledWidth / 2;
				g.drawImage(image, leftOffset, 0, rightOffset, getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
			}
			// Otherwise, the image width is too much, even scaled
			// So we need to center it the other direction
			else {
				int scaledHeight = (image.getHeight() * getWidth()) / image.getWidth();
				int topOffset = getHeight() / 2 - scaledHeight / 2;
				int bottomOffset = getHeight() / 2 + scaledHeight / 2;
				g.drawImage(image, 0, topOffset, getWidth(), bottomOffset, 0, 0, image.getWidth(), image.getHeight(), null);
			}
		}
	}
}