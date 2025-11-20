package com.imageToAscii.Image.to.Ascii.ImageProcessing;
import java.io.*;
public class ReadingImages {
	void main() {
		String imageName="images",format="jpg";
		String path="resources/images/"+imageName+"."+format;
		String opPath="resources/results/"+imageName+format+".txt";
		System.out.println(System.getProperty("user.dir"));
		try(BufferedInputStream fs=new BufferedInputStream(new FileInputStream(path))) {
			var writeFile=new BufferedWriter(new FileWriter(opPath));
			int b;int count=0;
			while((b=fs.read())!=-1) {
                if(count%4==0)writeFile.write("(");
                writeFile.write(Integer.toString(b));
                count++;
				if(count%4==0) {
                    writeFile.write(") ");
                }else writeFile.write(", ");
                if(count%16==0)writeFile.write("\n");
			}
		}catch(IOException e) {
			System.out.println("Error while operating on file:"+e.getMessage());
		}
	}
}
