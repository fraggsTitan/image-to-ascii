package com.imageToAscii.Image.to.Ascii.Controller;

import com.imageToAscii.Image.to.Ascii.DataClasses.CharCountMap;
import com.imageToAscii.Image.to.Ascii.DataClasses.ColorMap;
import com.imageToAscii.Image.to.Ascii.DataClasses.GIFCharMap;
import com.imageToAscii.Image.to.Ascii.MethodClasses.ASCIIWriter;
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
    private static final ASCIIWriter asciiWriter=new ASCIIWriter();
    @PostMapping("/ascii")
    public List<CharCountMap> convertImage(@RequestParam MultipartFile file, @RequestParam String mapping, @RequestParam(required = false) int height,
                                           @RequestParam(required = false) int width,@RequestParam(required = false) int scale,@RequestParam String format) throws IOException {
        BufferedImage img= ImageIO.read(file.getInputStream());
        return asciiWriter.buildImage(img, ColorMap.valueOf(mapping.toUpperCase()),width,height,scale,format);
    }
    @PostMapping("/ascii/gif")
    public List<GIFCharMap>convertGIF(@RequestParam MultipartFile file, @RequestParam String mapping, @RequestParam(required = false) int height,
                                      @RequestParam(required = false) int width,@RequestParam(required = false) int scale) throws IOException {
        ImageInputStream stream=ImageIO.createImageInputStream(file.getInputStream());
        return asciiWriter.buildGif(stream, ColorMap.valueOf(mapping.toUpperCase()),width,height,scale);
    }

}
