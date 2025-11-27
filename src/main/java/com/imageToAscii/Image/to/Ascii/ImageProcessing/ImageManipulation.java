package com.imageToAscii.Image.to.Ascii.ImageProcessing;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class ImageManipulation {
    public static BufferedImage cropImage(BufferedImage image, int x, int y, int width, int height){
        return image.getSubimage(x, y, width, height);
    }
    public static BufferedImage resize(BufferedImage original,int width,int height){
        BufferedImage resizedImage=new BufferedImage(width,height,original.getType());
        Graphics2D g2d=resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(original, 0, 0, width, height, null);
        g2d.dispose();

        return resizedImage;
    }

    public static BufferedImage makeGray(BufferedImage image){
        // just makes image gray and writes to the new file
        BufferedImage newImage=new BufferedImage(image.getWidth(),image.getHeight(),image.getType());
        ColorConvertOp convertOp=new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY),null);
        convertOp.filter(image,newImage);
        return newImage;
    }
    public static BufferedImage gaussianBlur(BufferedImage src, int radius) {
        if (radius < 1) return src;

        int size = radius * 2 + 1;
        float[] data = new float[size * size];

        // Build Gaussian kernel
        float sigma = radius / 2f;
        float twoSigmaSquare = 2f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0;

        int index = 0;
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                float distance = x * x + y * y;
                data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
                total += data[index];
                index++;
            }
        }

        // Normalize so kernel sums to 1
        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }

        Kernel kernel = new Kernel(size, size, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        op.filter(src, dest);
        return dest;
    }

}
