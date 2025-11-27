package com.imageToAscii.Image.to.Ascii.MethodClasses;

import com.imageToAscii.Image.to.Ascii.DataClasses.CharCountMap;
import com.imageToAscii.Image.to.Ascii.DataClasses.ColorMap;
import com.imageToAscii.Image.to.Ascii.DataClasses.GIFCharMap;
import com.imageToAscii.Image.to.Ascii.ImageProcessing.ImageManipulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ASCIIWriter {
    private static final Logger logger = LoggerFactory.getLogger(ASCIIWriter.class);
    char[] asciiMap = {
            '@', '8',
            '#', '&', '$',// Darkest
            'W', 'M',
            'H', 'N',
            'b', 'd', 'p', 'q',
            'Z',             // Dark Mid-tones
            'O', '0', 'Q', 'L',
            'C', 'J', 'E',   // Middle Values
            'z', 'c', 'v', 'u', 'n', 't', // Light Mid-tones
            ':', ';', ',',
            '`', '.' // Lightest
    };
    char[] blocks ={
            '█','▓','▒','░',' '
    };
    private int applyGamma(int gray, double gamma) {
        double normalized = gray / 255.0;
        normalized = Math.pow(normalized, gamma);
        return (int)(normalized * 255);
    }
    //builds the grayscale image before converting the image to ascii text and outputting to desired directories
    public List<CharCountMap> buildImage(BufferedImage image, ColorMap mapping,  Integer width,  Integer height,
                     Integer scale, String format){
        logger.info("Writing info of a {} file with dimensions: {}x{} and scale: {}", format, width, height, scale);
        image = resizeToDims(image, width, height);
        BufferedImage grayed=ImageManipulation.makeGray(image);
        grayed = ImageManipulation.gaussianBlur(grayed, 1); // apply mild gaussian blue
        logger.info("Image grayed successfully");
        return createString(mapping,grayed,scale);
    }
    public List<GIFCharMap> buildGif(ImageInputStream stream, ColorMap mapping,  Integer width, Integer height,
                   Integer scale) throws IOException {
        logger.info("Writing info of a GIF with dimensions: {}x{} and scale: {}", width, height, scale);
        logger.info("GIF resized to {}x{}", width, height);
        Iterator<ImageReader>readers= ImageIO.getImageReaders(stream);
        ImageReader reader=readers.next();
        reader.setInput(stream);
        List<GIFCharMap>frameList=new ArrayList<>();
        int frames=reader.getNumImages(true);
        logger.info("Number Of Frames: {}", frames);
        for(int i=0;i<frames;i++){
            BufferedImage image=reader.read(i);
            image=resizeToDims(image,width,height);
            int delay=getGifDelay(reader,i);
            frameList.add(new GIFCharMap(delay,createString(mapping,image,scale)));
            logger.info("Processed frame number: {}",i);
        }
        logger.info("Processed gif completely,Total characters: {}",getGIFMapTotalChars(frameList));
        reader.dispose();
        stream.close();
        return frameList;
    }
    public long getGIFMapTotalChars(List<GIFCharMap> gifMaps){
        long count=0;
        for(GIFCharMap charMap:gifMaps){for(CharCountMap map:charMap.charMaps){count+=map.getFreq();}}
        return count;
    }
    private int getGifDelay(ImageReader reader, int frameIndex) throws IOException {
        IIOMetadata metadata = reader.getImageMetadata(frameIndex);
        String metaFormat = metadata.getNativeMetadataFormatName();
        Node root = metadata.getAsTree(metaFormat);
        NodeList gceNodes = ((org.w3c.dom.Element) root)
                .getElementsByTagName("GraphicControlExtension");
        if (gceNodes.getLength() > 0) {
            org.w3c.dom.Node gce = gceNodes.item(0);
            NamedNodeMap attrs = gce.getAttributes();
            Node delayNode = attrs.getNamedItem("delayTime");
            if (delayNode != null) {
                int delay = Integer.parseInt(delayNode.getNodeValue());
                return delay * 10; // convert to milliseconds
            }
        }

        return 100; // fallback default = 100ms
    }
    private List<CharCountMap> createString(ColorMap mapping, BufferedImage image, int scale) {
        //builds the html file, grayscale output is text based while bw output is just the 4 black to white block characters
        int width = image.getWidth();
        int height = image.getHeight();
        List<CharCountMap> freqMap=new ArrayList<>();
        for (int j = 0; j < height; j+=scale) {
            for (int i = 0; i < width; i += scale) {
                int argb = image.getRGB(i, j);
                int gray = (argb >> 16 & 0xff) * 299 +
                        (argb >> 8 & 0xff) * 587 +
                        (argb & 0xff) * 114;
                gray = (gray + 500) / 1000;
                gray=applyGamma(gray,0.6);//adjust for natural light
                gray = Math.min(240, Math.max(15, gray));//reduce overstabilization from phones
                char mappedChar;
                if(mapping==ColorMap.TEXT) mappedChar= (asciiMap[(int) (Math.floor(gray * (asciiMap.length - 1) / 255.00))]);
                else mappedChar= ((blocks[(int) (Math.floor(gray * (blocks.length - 1) / 255.00))]));
                if(freqMap.isEmpty()||!(freqMap.getLast().ch==(mappedChar)))freqMap.add(new CharCountMap(mappedChar,1));
                else freqMap.getLast().incrementCount();
            }
            freqMap.add(new CharCountMap('\n',1));
        }
        logger.info("Image converted to a freqMap list successfully.FreqMap size: {}", freqMap.size());
        return freqMap;

    }

    private static BufferedImage resizeToDims(BufferedImage image, int width, int height) {
        logger.info("Dimensions passed into resizeToDims {}x{}", width, height);
        if (width == 0 && height == 0) {
            width = image.getWidth();
            height = image.getHeight();
            logger.warn("Proceeding with the default image dimensions of {}x{}",width,height);
        }
        if(height !=0&& width !=0){
            image = ImageManipulation.resize(image, width, height);
            logger.info("Image resized to {}x{}px", image.getWidth(), image.getHeight());
        }else if(height ==0){
            double aspectRatio = image.getWidth() * 1.000 / image.getHeight();
            height = (int) (width / aspectRatio);
            image = ImageManipulation.resize(image, width, height);
            logger.info("Image resized to {}x{}px", image.getWidth(), image.getHeight());
        }else{
            double aspectRatio = image.getWidth() * 1.000 / image.getHeight();
            width = (int) (height * aspectRatio);
            image = ImageManipulation.resize(image, width, height);
            logger.info("Image resized to {}x{}px", image.getWidth(), image.getHeight());
        }
        return image;
    }
    }
 /**   //EXTRACT THIS LOGIC TO FRONTEND
//    private static String escapeHTML(char c) {
//        //escapes special html characters with their escape values
//        return switch (c) {
//            case '&' -> "&amp;";
//            case '<' -> "&lt;";
//            case '>' -> "&gt;";
//            case '"' -> "&quot;";
//            case '\'' -> "&#39;";
//            default -> String.valueOf(c);
//        };
//    }
}
/***
 * const file = document.querySelector("input[type=file]").files[0];
 * const formData = new FormData();
 * formData.append("image", file);
 * fetch("/upload", {
 *     method: "POST",
 *     body: formData
 * });
 * @PostMapping("/upload")
 * public String upload(@RequestParam("image") MultipartFile file) throws Exception {
 *     byte[] bytes = file.getBytes(); // stays in memory
 *     BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
 *     // process img...
 *     return "ok";
 * }
 *
 */