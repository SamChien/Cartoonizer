package model;
import java.lang.Math;

public class Sobel {
	public static void process(int[] input, int[] output, int width, int height) {
		int[][] kernelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
		int[][] kernelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
		
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = row * width + col;
				int gx, gy, total;
				
				gx = convolution(input, kernelX, row, col, width, height);
				gy = convolution(input, kernelY, row, col, width, height);
				total = (int) Math.sqrt(gx * gx + gy * gy);
				output[index] = 0xff000000 | clamp(total) << 16 | clamp(total) << 8 | clamp(total);
			}
		}
	}
	
	private static int convolution(int[] input, int[][] kernel, int row, int col, int width, int height) {
		int sum = 0;
		int radius = 1;
		
		for (int kerRow = -radius; kerRow <= radius; kerRow++) {
			for (int kerCol = -radius; kerCol <= radius; kerCol++) {
				if (row + kerRow >= 0 && row + kerRow < height && kerCol + col >= 0 && kerCol + col < width) {
					int rowOffset = row + kerRow;
					int colOffset = col + kerCol;
					
					sum += (input[rowOffset * width + colOffset] & 0xff) * kernel[kerRow + radius][kerCol + radius];
				}
			}
		}
		return sum;
	}

	private static int clamp(int p) {
		return p < 0 ? 0 : ((p > 255) ? 255 : p);
	}
}