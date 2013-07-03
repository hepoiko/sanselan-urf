package org.apache.sanselan.formats.urf;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.sanselan.ImageParser;
import org.apache.sanselan.formats.urf.UrfImageParser;
import org.junit.Assert;
import org.junit.Test;

public class UdfFileReadingTest {

	@Test
	public void test() throws FileNotFoundException, Exception {
		ImageParser parser = new UrfImageParser();
		Dimension dimension = parser.getImageSize(new File(
				"src/test/samples/sample.urf"));
		Assert.assertEquals(4960, dimension.width);
		
		try {
		    // retrieve image
		    BufferedImage bi = parser.getBufferedImage(new File(
					"src/test/samples/sample.urf"), null);
		    File outputfile = new File("target/saved.jpg");
		    ImageIO.write(bi, "jpg", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
