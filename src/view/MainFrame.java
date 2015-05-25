package view;

import java.awt.EventQueue;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import model.BilateralFilter;
import model.Sobel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JScrollPane;
import javax.swing.JLabel;

public class MainFrame extends JFrame {
	private JPanel contentPane;
	private JLabel imgPanel;
	private BufferedImage bImg;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JButton btnChooseImage = new JButton("Choose Image");
		btnChooseImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();

				if (fileChooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
					try {
						int[] inPixels, outPixels, sobelPixels;
						int width, height;
						int filterTimes = 3;
						
						bImg = ImageIO.read(fileChooser.getSelectedFile());
						width = bImg.getWidth();
						height = bImg.getHeight();
						inPixels = new int[width * height];
						outPixels = new int[width * height];
						sobelPixels = new int[width * height];
						bImg.getRGB(0, 0, width, height, inPixels, 0, width);
						
						Sobel.process(inPixels, sobelPixels, width, height);
						for (int index = 0; index < inPixels.length; index++) {
							int red = (inPixels[index] >> 16) & 0xff;
							int green = (inPixels[index] >> 8) & 0xff;
							int blue = inPixels[index] & 0xff;
							
							red = colorSimplify(red);
							green = colorSimplify(green);
							blue = colorSimplify(blue);
							outPixels[index] = 0xff000000 | (red << 16) | (green << 8) | blue;
						}
						
						for (int times = 1; times <= filterTimes; times++) {
							BilateralFilter.filter(6, 16, inPixels, outPixels, width, height);
							if (times < filterTimes) {
								inPixels = outPixels;
								outPixels = new int[width * height];
							}
						}
						
						for (int index=0; index<outPixels.length; index++) {
							int gray = sobelPixels[index] & 0xff;
							int finalRed = (outPixels[index] >> 16) & 0xff;
							int finalGreen = (outPixels[index] >> 8) & 0xff;
							int finalBlue = outPixels[index] & 0xff;
							
							if (gray >= calColor(2 / 3.0)) {
								finalRed = 0;
								finalGreen = 0;
								finalBlue = 0;
							}
							outPixels[index] = 0xff000000 | (clamp(finalRed) << 16) | (clamp(finalGreen) << 8) | clamp(finalBlue);
						}
						
						bImg.setRGB(0, 0, width, height, outPixels, 0, width);
						imgPanel.setIcon(new ImageIcon(bImg));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
			
			private int clamp(int p) {
				return p < 0 ? 0 : ((p > 255) ? 255 : p);
			}
			
			private int colorSimplify(int color) {
				if (color <= calColor(1 / 9.0)) {
					return calColor(1 / 18.0);
				} else if (color > calColor(1 / 9.0) && color <= calColor(2 / 9.0)) {
					return calColor(3 / 18.0);
				} else if (color > calColor(2 / 9.0) && color <= calColor(3 / 9.0)) {
					return calColor(5 / 18.0);
				} else if (color > calColor(3 / 9.0) && color <= calColor(4 / 9.0)) {
					return calColor(7 / 18.0);
				} else if (color > calColor(4 / 9.0) && color <= calColor(5 / 9.0)) {
					return calColor(9 / 18.0);
				} else if (color > calColor(5 / 9.0) && color <= calColor(6 / 9.0)) {
					return calColor(11 / 18.0);
				} else if (color > calColor(6 / 9.0) && color <= calColor(7 / 9.0)) {
					return calColor(13 / 18.0);
				} else if (color > calColor(7 / 9.0) && color <= calColor(8 / 9.0)) {
					return calColor(15 / 18.0);
				} else {
					return calColor(17 / 18.0);
				}
			}
			
			private int calColor(double pos) {
				return (int)(256 * pos) - 1;
			}
		});
		btnChooseImage.setBounds(12, 12, 134, 29);
		contentPane.add(btnChooseImage);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 53, 774, 495);
		contentPane.add(scrollPane);

		imgPanel = new JLabel();
		scrollPane.setViewportView(imgPanel);

		JButton btnExportImage = new JButton("Export Image");
		btnExportImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();

				if (fileChooser.showSaveDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
					try {
						ImageIO.write(bImg, "JPEG", fileChooser.getSelectedFile());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnExportImage.setBounds(158, 12, 134, 29);
		contentPane.add(btnExportImage);
	}
}
