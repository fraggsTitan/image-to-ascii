package com.imageToAscii.Image.to.Ascii.ImageProcessing;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.imageio.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.*;
public class BuildASCIIFromImage {
    char[] asciiMap = {
            '@', '8', '#', '&', '$', 'W',        // Darkest
            'W', 'M', 'H', 'N', 'm', 'w', 'k',   // Very Dark
            'b', 'd', 'p', 'q', 'Z',             // Dark Mid-tones
            'O', '0', 'Q', 'L', 'C', 'J', 'E',   // Middle Values
            'U', 'Y', 'X', 'z', 'c', 'v', 'u', 'n', 't', // Light Mid-tones
            'i', 'l', 'I', ':', ';', ',', '"', '^', '`', '.' // Lightest
    };
    char[] map2={
      '█','▓','▒','░',' '
    };
//    private static String format(String in){
//        if(!in.contains("."))throw new  IllegalArgumentException("String doesnt contain a .");
//        return in.substring(in.lastIndexOf(".")+1);
//    }
    void buildImage(String in, String grayscaleOutput, String bwOutput, @DefaultValue("0") int width, @DefaultValue("0") int height){
        String format=in.substring(in.lastIndexOf(".")+1);
        String out;
        IO.println("Format: "+format);
        out="resources/results/res."+format;
        try{
            BufferedImage image=ImageIO.read(new File(in));
            BufferedImage grayed=makeGray(image);
            if(format.equals("jfif"))format="jpg";
            ImageIO.write(grayed,format,new File(out));
        }catch(IOException e){
            System.err.println("Error In copying data: "+e.getMessage());
        }
        //IO.println("Image copied to "+out);
        try{
            BufferedImage image=ImageIO.read(new File(out));
            if(height!=0&&width!=0){
                image=resize(image,width,height);
                IO.println("Image resized to "+width+"x"+height+"px");
            }else if(height==0){
                double aspectRatio = image.getWidth() * 1.000 / image.getHeight();
                image=resize(image,width,(int)(height*aspectRatio));
            }else{
                double aspectRatio = image.getWidth() * 1.000 / image.getHeight();
                image=resize(image,(int)(width/aspectRatio),height);
            }
            ImageIO.write(image,"png",new File(out));
            writeHTML(grayscaleOutput,bwOutput,image);
        }catch(IOException e){
            System.err.println("Error In manipulating data: "+e.getMessage());
        }
    }
    void writeHTML(String grayscaleOutput,String bwOutput,BufferedImage image) throws IOException {
        try(BufferedWriter output=new BufferedWriter(new FileWriter(grayscaleOutput));
            BufferedWriter output2=new BufferedWriter(new FileWriter(bwOutput))) {
            int width = image.getWidth();
            int height = image.getHeight();
            String str = String.format("""
                    <pre style="font-size:%dpx; font-family:consolas;">
                    """, 1);
            output.write(str);
            output2.write(str);
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int argb = image.getRGB(i, j);
                    int a = (argb >> 24) & 0xFF;
                    int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = (argb) & 0xFF;
                    double alpha = a / 255.0;
                    int gray = (int) (alpha * (0.299 * r + 0.587 * g + 0.114 * b) + (1 - alpha) * 255);
                    output2.write(map2[(int) (Math.floor(gray * (map2.length - 1) / 255.00))]);
                    output.write(escapeHTML(asciiMap[(int) (Math.floor(gray * (asciiMap.length - 1) / 255.00))]));
                }
                output.write("\n");
                output2.write("\n");
            }
            output.write("</pre>");
            output.write("<input type='number' min=0 max=3 value=1 step=0.1 id='val' style='position:absolute;bottom:5%;'>");
            output.write("""
                    <script>
                            const pre = document.querySelector('pre');
                            document.getElementById("val").addEventListener("input", function() {
                                pre.style.fontSize = this.value + 'px';
                            });
                    </script>""");
            output2.write("</pre>");
            output2.write("<input type='number' min=0 max=3 value=1 step=0.1 id='val' style='position:absolute;bottom:5%;'>");
            output2.write("""
                    <script>
                            const pre = document.querySelector('pre');
                            document.getElementById("val").addEventListener("input", function() {
                                pre.style.fontSize = this.value + 'px';
                            });
                    </script>""");
        }
        IO.println("Files Successfully written to: "+new File(grayscaleOutput)+" and "+new File(bwOutput)+".");
    }
    void main() {

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
        BufferedImage newImage=new BufferedImage(image.getWidth(),image.getHeight(),image.getType());
        ColorConvertOp convertOp=new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY),null);
        convertOp.filter(image,newImage);
        return newImage;
    }
    private static String escapeHTML(char c) {
        return switch (c) {
            case '&' -> "&amp;";
            case '<' -> "&lt;";
            case '>' -> "&gt;";
            case '"' -> "&quot;";
            case '\'' -> "&#39;";
            default -> String.valueOf(c);
        };
    }
}


