package com.imageToAscii.Image.to.Ascii.ImageProcessing;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

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
}
