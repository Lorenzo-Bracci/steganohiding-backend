package main.java.com.steganohiding;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import javax.imageio.*;
import java.nio.file.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import org.w3c.dom.*;
import java.util.*;
import java.nio.charset.StandardCharsets;


public class RevealData {
private static byte[] maskingBytes = {1, 2, 4, 8, 16, 32, 64, (byte)128};

private static int[] readMetadata(File imageFile) throws Exception{//get the metadata needed to find the secret message (size in byes of the message and number of bits stored in every color level)
  ImageInputStream input = ImageIO.createImageInputStream(imageFile);
      Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
      ImageReader reader = readers.next();
      reader.setInput(input);
      IIOImage image = reader.readAll(0, null);//get image
      int[] result = {getMetadataEntry(image.getMetadata(), "size"), getMetadataEntry(image.getMetadata(), "k")};//metadata for both size and k in an array
return result;
}

private static int getMetadataEntry(IIOMetadata metadata, String key) throws Exception{//we returen an inte because we already know that it is the only datatype that we need in this application
    IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
    NodeList entries = root.getElementsByTagName("TextEntry");

    for (int i = 0; i < entries.getLength(); i++) {//we loop through all the textentries node in the metadata
        IIOMetadataNode node = (IIOMetadataNode) entries.item(i);
        if (node.getAttribute("keyword").equals(key)) {//check if we found the node that we were looking for
            return Integer.parseInt(node.getAttribute("value"));//return the value converted to int
        }
    }
    //System.out.println("Not all the metadata needed is present in the file, try generating the file again");
    return -1;//this statement is never executed and is only here for the compiler
}

private static byte[] getMsg(int[][] pixels, int size, int n){//n represents the number of pixels in every color level
byte[] concealedBits = new byte[size*8];//this is where we store all the secret bits
int pixelsNeeded = (size*8) / (3 * n);//computes the number of pixels needed to conceal the image (we deal with the reminder later)
  int pixelsOffest = (size*8) % (3 * n);
  int pixelUsed = 0;//counter to keep track of how many more pixels we need to conceal
  boolean done = false;
  boolean doneWithReminder = false;

  for(int i = 0; i < pixels.length; i++){
    for(int j = 0; j < pixels[0].length; j++){
  if(!done){//if we are not done we conceal the secret information in the pixel
  byte[] currentData = getData(pixels[i][j], n, 3*n);//get secret data in pixel
  for(int k = 0; k < currentData.length; k++)//store the bits that we found in the result
  concealedBits[pixelUsed*n*3 + k] = currentData[k];
   pixelUsed++;//increment the number of pixels used to conceal information
  if(pixelUsed*n*3 >= (size*8 - pixelsOffest))//check if we already hid all the secret data
  done = true;
  }else if(!doneWithReminder){
    if(pixelsOffest != 0){
    byte[] finalData = getData(pixels[i][j], n, pixelsOffest);//get secret data in pixel
    for(int k = 0; k < finalData.length; k++)//store the bits that we found in the result
    concealedBits[pixelUsed*n*3 + k] = finalData[k];
  }
    doneWithReminder = true;
  }
  else{//if we already wrote all secret information we simply break
      break;
  }
    }
  }
  byte[] result = new byte[size];
  for(int i = 0; i < concealedBits.length; i+=8){
    byte[] currentByte = new byte[8];
  for(int j = 0; j < 8; j++){
    currentByte[j] = concealedBits[i+j];
  }
  result[i/8] = bitsToByte(currentByte);
}
return result;
}

private static byte[] getData(int pixel, int k, int msgSize){
int masking = 0xfffffffe;//int used to mask bits that will be overwritten
  byte[] result = new byte[msgSize];//initialize the result to the correct size
  int color1 = (pixel >> 16) & 0xff;
    int color2 = (pixel >> 8) & 0xff;
    int color3 = (pixel) & 0xff;
    int[] colors = {color3, color2, color1};
    int bitsFound = 0;

    for(int i = 0; i < colors.length; i++){
  int current = colors[i] % ((int) Math.pow(2, k));//To extract the concealed data, you take the color level modulo 2^n
      for(int j = 0; j < k; j++){
if(bitsFound < msgSize){//make sure tha we havent already found everything
result[bitsFound++] = (byte) ((current >> j) & 1);//retrive the correct bit
}
      }
    }
    return result;
}

private static byte bitsToByte(byte[] bits){//the first bit in the array that represents the byte is the least significant bit
byte result = 0;
  for(int i = 0; i < 8; i++){
int current = (int) bits[i];
byte bit = (byte) (current << i);
  result = (byte) (result + bit);
}
return result;
}

/*public static void main(String[] args){
System.out.println(reveal(new File(args[0])));
}*/

public static String reveal(File imageFile) {
try{
//File imageFile = new File(args[0]);
BufferedImage image = ImageIO.read(imageFile);//reads the imgage
int width = image.getWidth();
int height = image.getHeight();
int[][] pixels = new int[width][height];
  for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
            pixels[i][j] = image.getRGB(i, j);//stores pixels of the image in matrix
            }}
            //System.out.println(Integer.toBinaryString(pixels[0][0]));
            int[] metadata = readMetadata(imageFile);//get the needed metadata(size at position 0, k at position 1)
            if((metadata[0] <= 0) || (metadata[1] <= 0))
            throw new Exception("Not all the metadata needed is present in the file, try generating the file again");
byte[] hiddenMsg = getMsg(pixels, metadata[0], metadata[1]);
return new String(hiddenMsg, StandardCharsets.UTF_8);
//File outputFile = new File("output.txt");
//Files.write(outputFile.toPath(), hiddenMsg);
}catch(Exception ex){
  System.out.println(ex);
return "";
  //System.exit(1);
}
}
}
