package it.eternitywall.eternitywall;

public class IdenticonGenerator {
	public static int height = 5;
	public static int width = 5;
	public static int mul = 13;


	/*
	* Convert to Bitmap instead of BufferedImage or
	* temporary use web service http://identicon-1132.appspot.com/data
	* in any case this algo must produce the same identicon for the same alias address
	* */

	/*
	public static BufferedImage generate(String string) {
		return generate(Sha256Hash.create(string.getBytes()).getBytes());
	}



	public static BufferedImage generate(byte[] hash) {


		BufferedImage identicon = new BufferedImage(width*mul, height*mul, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = identicon.getRaster();

		//get byte values as unsigned ints
		int r = hash[0] & 255;
		int g = hash[1] & 255;
		int b = hash[2] & 255;
		System.out.println("r " + r + " g " + g + " b " + b);

		int [] background = new int [] {255,255,255, 0};
		int [] foreground = new int [] {r, g, b, 255};

		for(int x=0 ; x < width ; x++) {

			int i = x < 3 ? x : width - 1 - x;
			for(int y=0 ; y < height; y++) {
				int [] pixelColor;


				if((hash[i] >> y & 1) == 1)
					pixelColor = foreground;
				else
					pixelColor = background;

                System.out.println("i " + i + " y " + y + " " + (hash[i] >> y & 1) );

                for(int a=0;a<mul;a++) {
					for(int c=0;c<mul;c++) {
						raster.setPixel(x * mul + a, y * mul + c, pixelColor);
					}
				}
			}
		}

		return identicon;
	}
	*/
	/**
	 * Encode image to string
	 * @param image The image to encode
	 * @param type jpeg, bmp, ...
	 * @return encoded string
	 */
	/*
	public static String encodeToString(BufferedImage image, String type) {
		String imageString = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			ImageIO.write(image, type, bos);
			byte[] imageBytes = bos.toByteArray();

			BASE64Encoder encoder = new BASE64Encoder();
			imageString = encoder.encode(imageBytes);

			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imageString;
	}
	*/
}