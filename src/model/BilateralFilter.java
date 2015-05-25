package model;

public class BilateralFilter {
	public static void filter(double ds, double rs, int[] inPixels, int[] outPixels, int width, int height) {
		int radius = (int) Math.max(ds, rs);
		int size = 2 * radius + 1;
		double[][] cWeightTable = new double[size][size];
		double[] sWeightTable = new double[256];

		buildDistanceWeightTable(ds, cWeightTable, radius);
		buildSimilarityWeightTable(rs, sWeightTable);
		
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				int index = row * width + col;
				int red = (inPixels[index] >> 16) & 0xff;
				int green = (inPixels[index] >> 8) & 0xff;
				int blue = inPixels[index] & 0xff;
				double redSum = 0;
				double greenSum = 0;
				double blueSum = 0;
				double csSumRedWeight = 0;
				double csSumGreenWeight = 0;
				double csSumBlueWeight = 0;

				for (int semirow = -radius; semirow <= radius; semirow++) {
					for (int semicol = -radius; semicol <= radius; semicol++) {
						if (row + semirow >= 0 && row + semirow < height && semicol + col >= 0 && semicol + col < width) {
							int rowOffset = row + semirow;
							int colOffset = col + semicol;
							int index2, red2, green2, blue2;
							double csRedWeight, csGreenWeight, csBlueWeight;
							
							index2 = rowOffset * width + colOffset;
							red2 = (inPixels[index2] >> 16) & 0xff;
							green2 = (inPixels[index2] >> 8) & 0xff;
							blue2 = inPixels[index2] & 0xff;

							csRedWeight = cWeightTable[semirow + radius][semicol + radius] * sWeightTable[(Math.abs(red2 - red))];
							csGreenWeight = cWeightTable[semirow + radius][semicol + radius] * sWeightTable[(Math.abs(green2 - green))];
							csBlueWeight = cWeightTable[semirow + radius][semicol + radius] * sWeightTable[(Math.abs(blue2 - blue))];

							csSumRedWeight += csRedWeight;
							csSumGreenWeight += csGreenWeight;
							csSumBlueWeight += csBlueWeight;
							redSum += (csRedWeight * (double) red2);
							greenSum += (csGreenWeight * (double) green2);
							blueSum += (csBlueWeight * (double) blue2);
						}
					}
				}

				red = (int) (redSum / csSumRedWeight);
				green = (int) (greenSum / csSumGreenWeight);
				blue = (int) (blueSum / csSumBlueWeight);
				outPixels[index] = 0xff000000 | (clamp(red) << 16) | (clamp(green) << 8) | clamp(blue);
			}
		}
	}

	private static void buildDistanceWeightTable(double ds, double[][] cWeightTable, int radius) {
		for (int rowIndex = -radius; rowIndex <= radius; rowIndex++) {
			for (int colIndex = -radius; colIndex <= radius; colIndex++) {
				double distance = Math.sqrt(rowIndex * rowIndex + colIndex * colIndex);
				double delta = distance / ds;

				cWeightTable[rowIndex + radius][colIndex + radius] = Math.exp(delta * delta * -0.5);
			}
		}
	}

	private static void buildSimilarityWeightTable(double rs, double[] sWeightTable) {
		for (int index = 0; index < 256; index++) {
			double delta = index / rs;

			sWeightTable[index] = Math.exp(delta * delta * -0.5);
		}
	}

	private static int clamp(int p) {
		return p < 0 ? 0 : ((p > 255) ? 255 : p);
	}
}