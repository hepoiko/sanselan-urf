package org.apache.sanselan.formats.urf;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.apache.sanselan.FormatCompliance;
import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageParser;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.byteSources.ByteSource;

public class UrfImageParser extends ImageParser {

	@Override
	public IImageMetadata getMetadata(ByteSource paramByteSource, Map paramMap)
			throws ImageReadException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageInfo getImageInfo(ByteSource paramByteSource, Map paramMap)
			throws ImageReadException, IOException {

		InputStream imageStream = paramByteSource.getInputStream();
		FormatCompliance formatCompliance = FormatCompliance.getDefault();

		validateImageHeader(imageStream);
		int pages = getNumberOfPage(imageStream);
		UrfPageHeaderInfo pageInfo = readHeader(imageStream, formatCompliance,
				false);
		String formatDetails = "";
		int bitPerPixel = pageInfo.bpp;
		ArrayList comments = new ArrayList();
		String formatName = "unknown";
		int height = pageInfo.height;
		String mimeType = "image/urf";
		int numberOfImages = pages;
		float physicalHeightInch = pageInfo.height / pageInfo.dotPerInch * 1f;
		int physicalHeightDpi = pageInfo.dotPerInch;
		float physicalWidthInch = pageInfo.width / pageInfo.dotPerInch * 1f;
		int physicalWidthDpi = pageInfo.dotPerInch;;
		int width = pageInfo.width;
		boolean isProgressive = false;
		boolean isTransparent = false;
		boolean usesPalette = false;
		int colorType = 0;
		String compresionAlgroithm = null;
		ImageFormat format = null;
		ImageInfo info = new ImageInfo(formatDetails, bitPerPixel, comments,
				format, formatName, height, mimeType, numberOfImages,
				physicalHeightDpi, physicalHeightInch, physicalWidthDpi,
				physicalWidthInch, width, isProgressive, isTransparent,
				usesPalette, colorType, compresionAlgroithm);
		return info;
	}

	@Override
	public BufferedImage getBufferedImage(ByteSource paramByteSource, Map params)
			throws ImageReadException, IOException {
		FormatCompliance formatCompliance = FormatCompliance.getDefault();
		InputStream imageStream = paramByteSource.getInputStream();
		validateImageHeader(imageStream);
		getNumberOfPage(imageStream);
		UrfPageHeaderInfo info = readHeader(imageStream, formatCompliance,
				false);
		boolean hasAlpha = false;
		BufferedImage result = getBufferedImageFactory(params)
				.getColorBufferedImage(info.width, info.height, hasAlpha);
		byte[][] data = readImageData(info.width, info.height, info.bpp,
				imageStream);
		DataBuffer dataBuffer = result.getRaster().getDataBuffer();
		int pixelWidth = info.bpp / 8;
		byte[] pixel = new byte[pixelWidth];
		for (int y = 0; y < info.height; y++) {
			byte[] lineContainer = data[y];
			ByteArrayInputStream xStream = new ByteArrayInputStream(
					lineContainer);
			for (int x = 0; x < info.width; x++) {
				xStream.read(pixel);
				dataBuffer.setElem(y * info.width + x, pixelToInt(pixel));
			}
		}
		return result;
	}

	private int pixelToInt(byte[] pixel) {
		byte[] result = new byte[] { 0x00, 0x00, 0x00, 0x00 };
		int countIndex = 3;
		for (int i = (pixel.length - 1); i >= 0; i--) {
			result[countIndex] = pixel[i];
			countIndex--;
		}
		ByteBuffer buffer = ByteBuffer.wrap(result);
		return buffer.getInt();
	}

	@Override
	public Dimension getImageSize(ByteSource paramByteSource, Map paramMap)
			throws ImageReadException, IOException {
		FormatCompliance formatCompliance = FormatCompliance.getDefault();
		InputStream imageStream = paramByteSource.getInputStream();
		validateImageHeader(imageStream);
		getNumberOfPage(imageStream);
		UrfPageHeaderInfo info = readHeader(imageStream, formatCompliance,
				false);
		return new Dimension(info.width, info.height);
	}

	// TODO:
	private void validateImageHeader(InputStream imageStream)
			throws IOException, ImageReadException {
		readByteArray("UNIRAST Identifier", 8, imageStream);
	}

	private int getNumberOfPage(InputStream imageStream)
			throws ImageReadException, IOException {
		return read4Bytes("Number of Pages", imageStream, "Fail");
	}
	
	private UrfPageHeaderInfo readHeader(InputStream is,
			FormatCompliance formatCompliance, boolean verbose)
			throws IOException, ImageReadException {
		UrfPageHeaderInfo info = new UrfPageHeaderInfo();

		info.bpp = readByte("Bits per pixel", is, "Fail to retrieve");
		info.colorspace = readByte("Color Space", is, "Fail to retrieve");
		readByte("duplex", is, "Fail to retrieve");
		info.quality = readByte("Quality", is, "Fail");
		info.unknownValue0 = read4Bytes("Unknown Value 0", is, "Fail");
		info.unknownValue1 = read4Bytes("Unknown Value 1", is, "Fail");
		info.width = read4Bytes("Width", is, "Fail");
		info.height = read4Bytes("Height", is, "Fail");
		info.dotPerInch = read4Bytes("Dot per Inch", is, "Fail");
		info.unknownValue2 = read4Bytes("Unknown Value 2", is, "Fail");
		info.unknownValue3 = read4Bytes("Unknown Value 3", is, "Fail");

		return info;
	}

	private byte[][] readImageData(int width, int height, int bpp,
			InputStream imageStream) throws ImageReadException, IOException {
		int onLine = 0;
		int pixelSize = bpp / 8;
		byte[] pixel = new byte[pixelSize];
		byte[][] data = new byte[height][width * pixelSize];

		while (true) {
			byte lineRepeatByte = readByte("data", imageStream, "fail");

			int lineRepeat = (lineRepeatByte & 0xff) + 1;

			int position = 0;

			byte[] lineContainer = new byte[pixelSize * width];
			while (true) {
				byte code = readByte("packbit", imageStream, "fail");

				if (code == -128) {
					for (int i = position; i < width; i++) {
						for (int j = 0; j < pixelSize; j++) {
							lineContainer[i * pixelSize + j] = (byte) 0xff;
						}
					}
					position = width;

				} else if (code >= 0 && code <= 127) {
					int n = code + 1;
					pixel = readBytes(imageStream, pixelSize);
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < pixelSize; j++) {
							lineContainer[position * pixelSize + j] = pixel[j];
						}
						position++;
						if (position >= width) {
							break;
						}
					}
				} else if (code > -128 && code < 0) {
					int n = (code * -1) + 1;

					for (int i = 0; i < n; i++) {
						pixel = readBytes(imageStream, pixelSize);
						for (int j = 0; j < pixelSize; j++) {
							lineContainer[position * pixelSize + j] = pixel[j];
						}
						position++;
						if (position >= width) {
							break;
						}
					}
				}

				if (position >= width) {
					break;
				}
			}

			for (int i = 0; i < lineRepeat; i++) {
				data[onLine] = Arrays.copyOf(lineContainer,
						lineContainer.length);
				onLine++;
			}

			if (onLine >= height) {
				break;
			}
		}
		return data;
	}

	@Override
	public String getXmpXml(ByteSource paramByteSource, Map paramMap)
			throws ImageReadException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getICCProfileBytes(ByteSource paramByteSource, Map paramMap)
			throws ImageReadException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean embedICCProfile(File paramFile1, File paramFile2,
			byte[] paramArrayOfByte) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultExtension() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] getAcceptedExtensions() {
		return new String [] { ".urf" };
	}

	@Override
	protected ImageFormat[] getAcceptedTypes() {
		throw new UnsupportedOperationException("not yet implemented.");
	}

}
