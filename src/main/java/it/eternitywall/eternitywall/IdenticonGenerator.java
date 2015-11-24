package it.eternitywall.eternitywall;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import org.bitcoinj.core.Sha256Hash;

public class IdenticonGenerator {
    private static final String TAG = "IdenticonGenerator";

    public static int SIDE = 7;
    public static int MUL = 9;

    public static Bitmap generate(String string, int side, int mul) {
        return generate(Sha256Hash.create(string.getBytes()).getBytes(), side, mul);
    }

	public static Bitmap generate(String string) {
		return generate(Sha256Hash.create(string.getBytes()).getBytes(), SIDE, MUL);
	}

	public static Bitmap generate(byte[] hash, int side, int mul) {
		final Bitmap identicon = Bitmap.createBitmap(side*mul, side*mul, Bitmap.Config.ARGB_8888);

		//get byte values as unsigned ints
		int r = hash[0] & 255;
		int g = hash[1] & 255;
		int b = hash[2] & 255;
		Log.i(TAG, "r " + r + " g " + g + " b " + b);

		int background = Color.argb(0,255,255,255);
		int foreground = Color.argb(255,r,g,b);

		for(int x=0 ; x < side ; x++) {
			int i = x < side/2 ? x : side - 1 - x;
            final int i1 = x * mul;
			for(int y=0 ; y < side; y++) {
                final int i2 = y * mul;
                int currentColor;

				if((hash[i] >> y & 1) == 1)
                    currentColor=foreground;
				else
                    currentColor=background;

                for (int a = 0; a < mul; a++) {
                    final int x1 = i1 + a;
                    for (int c = 0; c < mul; c++) {
                        final int y1 = i2 + c;
                        identicon.setPixel(x1, y1, currentColor);
                    }
                }
            }
		}

		return identicon;
	}

}