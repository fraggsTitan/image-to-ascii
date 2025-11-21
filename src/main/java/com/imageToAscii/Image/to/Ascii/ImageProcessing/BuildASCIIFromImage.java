package com.imageToAscii.Image.to.Ascii.ImageProcessing;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.imageio.*;
import java.awt.image.BufferedImage;
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
    //builds the grayscale image before converting the image to ascii text and outputting to desired directories
    void buildImage(String in, String grayscaleOutput, String bwOutput, @DefaultValue("0") int width, @DefaultValue("0") int height,
                    @DefaultValue("1") int scale){
        String format=in.substring(in.lastIndexOf(".")+1);
        String out;
        IO.println("Format: "+format);
        out="resources/results/res."+format;
        try{
            //handles making image to gray
            BufferedImage image=ImageIO.read(new File(in));
            BufferedImage grayed= ImageManipulation.makeGray(image);
            if(format.equals("jfif"))format="jpg";
            ImageIO.write(grayed,format,new File(out));
        }catch(IOException e){
            System.err.println("Error In copying data: "+e.getMessage());
        }
        try{
            //if any param is not specified,then this function will try to adjust for them by using the aspect ratio to fill in values
            BufferedImage image=ImageIO.read(new File(out));
            if (width == 0 && height == 0) {
                width = image.getWidth();
                height = image.getHeight();
            }
            if(height!=0&&width!=0){
                image= ImageManipulation.resize(image,width,height);
                IO.println("Image resized to "+width+"x"+height+"px");
            }else if(height==0){
                double aspectRatio = image.getWidth() * 1.000 / image.getHeight();
                height = (int) (width / aspectRatio);
                image= ImageManipulation.resize(image,width,height);
            }else{
                double aspectRatio = image.getWidth() * 1.000 / image.getHeight();
                width = (int) (height * aspectRatio);
                image= ImageManipulation.resize(image,width,height);
            }
            ImageIO.write(image,format,new File(out));
            writeHTML(grayscaleOutput,bwOutput,image,scale);
        }catch(IOException e){
            System.err.println("Error In manipulating data: "+e.getMessage());
        }
    }
    void writeHTML(String grayscaleOutput,String bwOutput,BufferedImage image,int scale) throws IOException {
        //builds the html file, grayscale output is text based while bwoutput is just the 4 black to white block characters
        //TODO make this cleaner by using the html library of java
        try(BufferedWriter output=new BufferedWriter(new FileWriter(grayscaleOutput));
            BufferedWriter output2=new BufferedWriter(new FileWriter(bwOutput))) {
            int width = image.getWidth();
            int height = image.getHeight();
            String str = String.format("""
                    <pre style="font-size:%dpx; font-family:consolas;">
                    """, 1);
            output.write(str);
            output2.write(str);
            //iterate over every pixel in image, get its a,r,g,b by doing right shift on the bits, then write to the bw/graysclae files
            for (int j = 0; j < height; j+=scale) {
                for (int i = 0; i < width; i+=scale) {
                    int argb = image.getRGB(i, j);
//                    int a = (argb >> 24) & 0xFF;
//                    int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = (argb) & 0xFF;
//                    double alpha = a / 255.0;
//                    int gray = (int) (alpha * (0.299 * r + 0.587 * g + 0.114 * b) + (1 - alpha) * 255);
                    int gray = (argb >> 16 & 0xff) * 299 +
                            (argb >> 8  & 0xff) * 587 +
                            (argb       & 0xff) * 114;
                    gray = (gray + 500) / 1000;
                    output2.write(map2[(int) (Math.floor(gray * (map2.length - 1) / 255.00))]);
                    output.write(escapeHTML(asciiMap[(int) (Math.floor(gray * (asciiMap.length - 1) / 255.00))]));
                }
                output.write("\n");
                output2.write("\n");
            }
            //javascript to allow for file to allow text resizing
            //TODO Abstract this javascript to another real js file
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

    private static String escapeHTML(char c) {
        //escapes special html characters with their escape values
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


