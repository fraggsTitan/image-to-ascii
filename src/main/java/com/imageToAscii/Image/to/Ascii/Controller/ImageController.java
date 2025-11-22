package com.imageToAscii.Image.to.Ascii.Controller;

import com.imageToAscii.Image.to.Ascii.DataClasses.CharCountMap;
import com.imageToAscii.Image.to.Ascii.DataClasses.ColorMap;
import com.imageToAscii.Image.to.Ascii.DataClasses.GIFCharMap;
import com.imageToAscii.Image.to.Ascii.ImageProcessing.ImageManipulation;
import com.imageToAscii.Image.to.Ascii.MethodClasses.ASCIIWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

@RestController
public class ImageController {
    private static final Logger logger =  LoggerFactory.getLogger(ImageController.class.getName());
    private static final ASCIIWriter asciiWriter=new ASCIIWriter();
    @PostMapping("/ascii")
    public List<CharCountMap> convertImage(@RequestParam MultipartFile file, @RequestParam String mapping, @RequestParam(defaultValue = "0") Integer height,
                                           @RequestParam(defaultValue = "0") Integer width,@RequestParam(defaultValue = "1") Integer scale,@RequestParam String format) throws IOException {
       if(format.equalsIgnoreCase("GIF"))throw new UnexpectedFormatException("Received GIF Format image in method that doesn't handle it. Try /ascii/gif instead");
       BufferedImage img= ImageIO.read(file.getInputStream());
       return asciiWriter.buildImage(img, ColorMap.valueOf(mapping.toUpperCase()),width,height,scale,format);

    }
    @PostMapping("/ascii/gif")
    public List<GIFCharMap>convertGIF(@RequestParam MultipartFile file, @RequestParam String mapping, @RequestParam(defaultValue = "0") Integer height,
                                      @RequestParam(defaultValue = "0") Integer width,@RequestParam(defaultValue = "1")Integer scale) throws IOException {
        ImageInputStream stream=ImageIO.createImageInputStream(file.getInputStream());
        return asciiWriter.buildGif(stream, ColorMap.valueOf(mapping.toUpperCase()),width,height,scale);
    }
    @PostMapping("/ascii/crop_and_build")
    public List<CharCountMap>cropAndBuildImage(@RequestParam MultipartFile file, @RequestParam String mapping, @RequestParam(defaultValue = "0") Integer height,
                                               @RequestParam(defaultValue = "0") Integer width,
                                               @RequestParam(defaultValue = "1") Integer scale,@RequestParam String format,
                                             @RequestParam int x, @RequestParam int y,@RequestParam int cropWidth,@RequestParam int cropHeight) throws IOException {
        if(format.equalsIgnoreCase("GIF")){
            throw new UnexpectedFormatException("Received GIF Format image in method that doesn't handle it. Try /ascii/gif instead. If you wanted to crop a GIF, it isnt supported yet.");
        }
        BufferedImage img=ImageIO.read(file.getInputStream());
        try {
            img = ImageManipulation.cropImage(img, x, y, cropWidth, cropHeight);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return asciiWriter.buildImage(img,ColorMap.valueOf(mapping.toUpperCase()),width,height,scale,format);
    }
}
