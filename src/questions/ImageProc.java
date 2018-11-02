package questions;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.io.*;
import java.nio.ByteBuffer;

public class ImageProc extends JFrame {

	JPanel comboPanel;
	DrawingPanel dp;
	JTextArea txtArea;
	JFileChooser chooser;
	JComboBox<String> comboBox;
	String directory;

	ImageProc() {
		setSize(1024, 768);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		dp = new DrawingPanel();
		comboPanel = new JPanel();
		
		directory = ".";
		comboBox = new JComboBox<>();
		JButton btnDirectory = new JButton("Open a Directory..");
		JButton btnFile = new JButton("Open a File...");
		
		txtArea = new JTextArea(4,0);
		txtArea.setEditable(false);
		txtArea.setBackground(Color.LIGHT_GRAY);
		
		fillComboBox(comboBox, directory);
		
		comboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(comboBox.getSelectedItem() != null && !String.valueOf(comboBox.getSelectedItem()).equals("Empty directory."))
				{
					dp.showImage(directory+"\\"+String.valueOf(comboBox.getSelectedItem()));
					txtArea.setText(dp.magicNumber+"\n"+(dp.commentLine.isEmpty() ? "" : dp.commentLine+"\n") +dp.width+ " " + dp.height + (dp.maxValue !=0 ? "\n"+dp.maxValue : ""));
				}
			}
		});
		
		btnDirectory.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				chooser = new JFileChooser();
				chooser.setDialogTitle("Select a Directory");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false); // disables all files option
				
				if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				{
					directory = chooser.getSelectedFile().getPath();
					fillComboBox(comboBox, directory);
				}
				
			}
		});
		
		btnFile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				chooser = new JFileChooser("Select a file.");
				chooser.setAcceptAllFileFilterUsed(false);
				FileNameExtensionFilter fnef = new FileNameExtensionFilter("Image Files", "pgm","ppm","pbm");
				chooser.setFileFilter(fnef);
				
				if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				{
					dp.showImage(chooser.getSelectedFile().getAbsolutePath());
					txtArea.setText(dp.magicNumber+"\n"+(dp.commentLine.isEmpty() ? "" : dp.commentLine+"\n") +dp.width+ " " + dp.height + (dp.maxValue !=0 ? "\n"+dp.maxValue : ""));
				}
			}
		});
		
		comboBox.setSelectedIndex(0);
		
		comboPanel.add(comboBox);
		comboPanel.add(btnDirectory);
		comboPanel.add(btnFile);
		add(comboPanel, BorderLayout.NORTH);
		add(new JScrollPane(txtArea), BorderLayout.SOUTH);
		add(dp, BorderLayout.CENTER);

		setVisible(true);
		
	}
	
	public void fillComboBox(JComboBox<String> comboBox,String directory)
	{
		comboBox.removeAllItems();;
		File file = new File(directory);
		String[] files = file.list();
		for (String e : files) {
			if (e.contains(".") && !e.startsWith(".")) {

				String fileExtension = e.split("\\.")[e.split("\\.").length - 1];
				if (fileExtension.equals("pgm") || fileExtension.equals("ppm") || fileExtension.equals("pbm")) {
					comboBox.addItem(e);
				}
			}
		}
		files = null;
		
		if(comboBox.getItemCount() != 0)
		{
			comboBox.setEnabled(true);
			comboBox.setSelectedIndex(0);
		}
		else {
			comboBox.setEnabled(false);
			dp.clear();
			dp.repaint();
			txtArea.setText("");
			comboBox.addItem("Empty directory.");
		}
	}

	public static void main(String[] args) {

		new ImageProc();
	}
	
}

class DrawingPanel extends JPanel {

	int width, height, maxValue;
	String magicNumber;
	String commentLine;
	int[] pixels;
	int[][] coloredPixels;

	public void clear()
	{
		width = height = maxValue = 0;
		commentLine="";
		magicNumber = "";
		pixels = null;
	}
	
	DrawingPanel() {
		clear();
	}
	
	public void prepareDataStructure(String fileName) {

		try {
			clear();
			
			FileInputStream fis = new FileInputStream(fileName);
			byte[] magicNumberArray = new byte[2];
			fis.read(magicNumberArray);
			String magicNumber = new String(magicNumberArray);
			this.magicNumber = magicNumber;
			
			byte chByte;
			do {
				chByte = (byte) fis.read();
			} while (Character.isWhitespace(chByte));

			String widthString="";
			String heightString="";
			
			if(chByte == '#')
			{
				do
				{
					commentLine += (char) chByte;
					chByte = (byte) fis.read();
				}while(chByte != 10); // 10 for newLine
				
				do {
					chByte = (byte) fis.read();
				} while (Character.isWhitespace(chByte));
			}
			
			do {
				widthString += (char) chByte;
				chByte = (byte)fis.read();
			} while (!Character.isWhitespace(chByte));

			do {
				chByte = (byte) fis.read();
			} while (Character.isWhitespace(chByte));

			
			do {
				heightString+= (char) chByte;
				chByte = (byte)fis.read();
			} while (!Character.isWhitespace(chByte));


			this.width = Integer.parseInt(widthString);
			this.height = Integer.parseInt(heightString);

			if (!magicNumber.equals("P1") && !magicNumber.equals("P4")) {
				do {
					chByte = (byte) fis.read();
				} while (Character.isWhitespace(chByte));

				String maxString="";

				do {
					maxString += (char) chByte;
					chByte = (byte)fis.read();
				} while (!Character.isWhitespace(chByte));

				this.maxValue = Integer.parseInt(maxString);
			}
			
			if (magicNumber.equals("P1") || magicNumber.equals("P2") || magicNumber.equals("P3")) {
				Scanner scn = new Scanner(new File(fileName));
				for (int i = 0; i < (magicNumber.equals("P1") ? 2 : 3) + (commentLine.isEmpty() ? 0 : 1); i++)
					scn.nextLine();

				pixels = new int[width * height];
				for (int i = 0; i < width * height; i++)
					pixels[i] = Integer.parseInt(scn.next());
				
			}
			else if(magicNumber.equals("P4"))
			{
				int it=0;
				pixels = new int[width*height];
				int[] abyte = new int[8];
				int a=0;
				while((a = fis.read()) != -1)
				{		
					for(int i = 0; i < abyte.length; i++)
					{
						abyte[abyte.length-1-i] = a & (int)Math.pow(2,i);
					}
					
					for(int x : abyte)
					{
						pixels[it++] = x==0 ? 1:0;
						if(it % width==0) 
							break;
					}
				}
			}
			else if(magicNumber.equals("P5"))
			{
				pixels = new int[width * height];
				for(int i = 0 ; i < width*height ; i++)
					pixels[i] = fis.read();
			}
			
			else if(magicNumber.equals("P6"))
			{
				coloredPixels = new int[width*height][3];
				for(int i = 0 ; i < width*height ; i++)
				{
					for(int j = 0 ; j < 3 ; j++)
					{
						coloredPixels[i][j] = fis.read();
					}
				}
			}
		} catch (IOException e) {e.printStackTrace();}
	}

	public void showImage(String fileName) {
		prepareDataStructure(fileName);
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (pixels != null || coloredPixels != null) {
			
				for (int row = 0; row < height; row++) {
					for (int column = 0; column < width; column++) {
						
						if(!magicNumber.equals("P6"))
						{
							int intColor = pixels[row * width + column];
							int multiplier = ((magicNumber.equals("P1") || magicNumber.equals("P4")) ? 255 : 1);
							g.setColor(new Color(multiplier * intColor, multiplier * intColor, multiplier * intColor));
						}
						else
						g.setColor(new Color(coloredPixels[row * width + column][0] , coloredPixels[row * width + column][1] , coloredPixels[row * width + column][2]));
						
						g.drawLine(column +(getWidth()/2-width/2), row+(getHeight()/2-height/2), column+(getWidth()/2-width/2), row+(getHeight()/2-height/2));

					}
				}
		}		
	}
}