//package com.imageToAscii.Image.to.Ascii;
//
//import com.imageToAscii.Image.to.Ascii.Controller.ImageController;
//import com.imageToAscii.Image.to.Ascii.DataClasses.ColorMap;
//import com.imageToAscii.Image.to.Ascii.DataClasses.GIFCharMap;
//import com.imageToAscii.Image.to.Ascii.MethodClasses.ASCIIWriter;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.web.servlet.MockMvc;
//
//import javax.imageio.stream.ImageInputStream;
//import java.util.List;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPat h;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(ImageController.class)
//public class ImageHandlingTests {
//
////    @Autowired
////    private MockMvc mvc;
////    private static ASCIIWriter asciiWriter;
////    @Test
////    void testConvertGIF() throws Exception {
////        byte[]gifBytes=new byte[]{1,2,3};
////        MockMultipartFile multipartFile=new MockMultipartFile(
////          "file", "sample.gif", "image/gif",gifBytes
////        );
////        // expected output
////        GIFCharMap mockFrame = new GIFCharMap();
////        List<GIFCharMap> mockList = List.of(mockFrame);
////
////        Mockito.when(asciiWriter.buildGif(
////                Mockito.any(ImageInputStream.class),
////                Mockito.eq(ColorMap.ASCII),  // assume this is passed
////                Mockito.eq(100),               // width
////                Mockito.eq(50),                // height
////                Mockito.eq(2)                  // scale
////        )).thenReturn(mockList);
////
////        mvc.perform(multipart("/ascii/gif")
////                        .file(multipartFile)
////                        .param("mapping", "rainbow")
////                        .param("height", "50")
////                        .param("width", "100")
////                        .param("scale", "2")
////                )
////                .andExpect(status().isOk())
////                .andExpect(jsonPath("$").isArray())
////                .andExpect(jsonPath("$[0]").exists());
////    }
//
//}
