package com.imageToAscii.Image.to.Ascii;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImageToAsciiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageToAsciiApplication.class, args);
	}
    //TODO create a crop endpoint that takes 4 params and crops the image to those dimensions
}
