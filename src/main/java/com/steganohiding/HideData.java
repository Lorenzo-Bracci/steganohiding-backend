package main.java.com.steganohiding;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.nio.file.*;
import java.util.*;


public class HideData {
private static byte[] maskingBytes = {1, 2, 4, 8, 16, 32, 64, (byte)128};
private static int[] masking = {0xfffffffe, 0xfffffffd, 0xfffffffb, 0xfffffff7, 0xffffffef, 0xffffffdf, 0xffffffbf, 0xffffff7f};

private static void writeMetadata(File imageFile, int size, int k) throws Exception{//k represents the bits stored in every color level
  ImageInputStream input = ImageIO.createImageInputStream(imageFile);
      Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
      ImageReader reader = readers.next();
      reader.setInput(input);
      IIOImage image = reader.readAll(0, null);//get image
      addSteganographicMetadata(image.getMetadata(), size, k);//add needed metadata to the xml metadata structure
      ImageWriter writer = ImageIO.getImageWriter(reader);
      ImageOutputStream output = ImageIO.createImageOutputStream(imageFile);
      writer.setOutput(output);
      writer.write(image);//write image with the new metadata
}

private static void addSteganographicMetadata(IIOMetadata metadata, int size, int k) throws Exception {//k represents the number of bits stored in every color level
    IIOMetadataNode sizeEntry = new IIOMetadataNode("TextEntry");
    sizeEntry.setAttribute("keyword", "size");
    sizeEntry.setAttribute("value", Integer.toString(size));
    IIOMetadataNode kEntry = new IIOMetadataNode("TextEntry");
    kEntry.setAttribute("keyword", "k");
    kEntry.setAttribute("value", Integer.toString(k));
    IIOMetadataNode newMetadata = new IIOMetadataNode("Text");//Link together the new metedata
    newMetadata.appendChild(sizeEntry);
    newMetadata.appendChild(kEntry);
    IIOMetadataNode root = new IIOMetadataNode(IIOMetadataFormatImpl.standardMetadataFormatName);
    root.appendChild(newMetadata);//append the new metadata to the file metadata
    metadata.mergeTree(IIOMetadataFormatImpl.standardMetadataFormatName, root);
}

private static int concealData(byte[] secretBits, int pixel, int k){
  /*System.out.println("Bits to conceal");
  for(int i = 0; i < secretBits.length; i++)
  System.out.println(secretBits[i]);
  System.out.println("Pixel before: " + Integer.toBinaryString(pixel));*/
int result = pixel & 0xff000000;//keep only the alpha value as we will change everything else
  int color1 = (pixel >> 16) & 0xff;
    int color2 = (pixel >> 8) & 0xff;
    int color3 = (pixel) & 0xff;
    int[] colors = {color3, color2, color1};
    int msgSize = secretBits.length;
    int bitsConcealed = 0;
byte comparisonByte = 1;//used for comparison (making sure that type is correct)
    for(int i = 0; i < colors.length; i++){
      for(int j = 0; j < k; j++){
if(bitsConcealed < msgSize){//make sure tha we havent already concealed everything
        if(secretBits[i*k + j] == comparisonByte){
colors[i] = colors[i] | (1 << j);//set the bit that we want to overwrite to 1

}else{
colors[i] = colors[i] & masking[j];//set the bit that we want to overwrite to 0

}
bitsConcealed++;
}
      }
      result = result | (colors[i] << (8*i));//write the new color values to the result
    }
    //System.out.println("Pixel after: " + Integer.toBinaryString(result));
    return result;
}

private static byte[] byteToBit(byte current){
  byte[] result = new byte[8];
  for(int i = 0; i < 8; i++)
  result[i] = (byte)Math.abs((((int)(current & maskingBytes[i])) >> i));
  return result;
}

  private static byte[] readSecretMsg(File secret) throws Exception{
    InputStream fis = null;
    try{
      fis = new FileInputStream(secret);
    }catch(Exception ex){
      System.out.println("An error has occurred while reading the secret file");
     // System.exit(1);
    }
    byte[] result = new byte[(int) secret.length()];
    fis.read(result);//read all the bytes of the file in an array
    fis.close();
    //System.out.println("The size of the secret message is: " + result.length);
  return result;
  }

private static int[][] transformPixels(int[][] pixels, byte[] secretData, int n){// n represents the amount of data to conceal in every pixel
//public static int[][] transformPixels(int[][] pixels){
if(n > 8){
  System.out.println("N is too large, rgb 8 bit is used therefore no more than 8 bits can be concealed on every color level");
  //System.exit(1);
}
  int[][] result = new int[pixels.length][pixels[0].length];//initialize the matrix which stores the result
  byte[] bits = new byte[secretData.length * 8];//create the array where every single bit will be stored

  for(int i = 0; i < secretData.length; i++){
    byte[] currentByte = byteToBit(secretData[i]);//get evry bit out of the byte
    for(int j = 0; j < currentByte.length; j++)//copy the byte in the bits array
    bits[i*8 + j] = currentByte[j];
  }


  int availableBits = result.length * result[0].length * 3 * n;//total number of bits that can be concealed in the image
  if(bits.length > availableBits){//if we are trying to conceal too much data we exit the program with a useful error message
    System.out.println("Too much data is trying to be concealed in the image");
    System.exit(1);
  }

  int pixelsNeeded = bits.length / (3 * n);//computes the number of pixels needed to conceal the image (we deal with the reminder later)
int pixelsOffest = bits.length % (3 * n);
int pixelUsed = 0;//counter to keep track of how many more pixels we need to conceal
boolean done = false;
boolean doneWithReminder = false;
for(int i = 0; i < pixels.length; i++){
  for(int j = 0; j < pixels[0].length; j++){
if(!done){//if we are not done we conceal the secret information in the pixel
byte[] currentData = new byte[3*n];
for(int k = 0; k < (3*n); k++)//wrire array of bits needed to conceal data
currentData[k] = bits[pixelUsed*n*3 + k];
 result[i][j] = concealData(currentData, pixels[i][j], n);//conceal secret data in pixel
 pixelUsed++;//increment the number of pixels used to conceal information
if(pixelUsed*n*3 >= (bits.length - pixelsOffest))//check if we already hid all the secret data
done = true;
}else if(!doneWithReminder){
  if(pixelsOffest != 0){
  byte[] finalData = new byte[pixelsOffest];
  for(int k = 0; k < pixelsOffest; k++)//wrire array of bits needed to conceal data
  finalData[k] = bits[pixelUsed*n*3 + k];
  result[i][j] = concealData(finalData, pixels[i][j], n);//conceal secret data in pixel
}else{
result[i][j] = pixels[i][j];
}
  doneWithReminder = true;
}
else{//if we already wrote all secret information we simply copy the other pixels
    result[i][j] = pixels[i][j];
}
  }
}
return result;
}

/*public static void main(String[] args) throws Exception{
try{
File output = hide(new File(args[0]), args[1], Integer.parseInt(args[2]));
}catch(Exception ex){
  System.out.println(ex);
}
}*/

public static File hide(File imageFile, String secret) throws Exception{
  //args[1] = secret message file
  //args[0] = image File
  //args[2] = number of bits to store in every color level
/*byte three = 3;
byte[] result = byteToBit(three);
for(int i = 0; i < result.length; i++)
System.out.println(result[i]);*/
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
            byte[] secretMsg = secret.getBytes();//store all the bytes that we want to hide in an array
            //int k = Integer.parseInt(args[2]);//number of bits that we want to store in every color level (maximum 8)
            
            int k = 1;
            int totPixels = width * height;
            while(k <= 8){ //compute appropriate k to make image as smooth as possible
                if((secretMsg.length * 8) < (totPixels*3*k))
                break;
                k++;
            }
          

            if(k >= 8){
                throw new Exception("No more than 8 bits can be stored in a color level because 8 bits rgb is used");
              }

int[][] steganographicPixels = transformPixels(pixels, secretMsg, k);
BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);//initialize buffer where we write the output image
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                outputImage.setRGB(i, j, steganographicPixels[i][j]);//set bitmap in output image
            }
        }

        File outputFile = new File("output.png");
        try {
            ImageIO.write(outputImage, "png", outputFile);//write output image in a file
            writeMetadata(outputFile, secretMsg.length, k);//write the needed metadata into the png file (size in bytes of the hidden file and number of bits stored in every color level)
return outputFile;
        } catch (Exception ex) {
System.out.println("An error has occurred while writing the image to the output file");
return null;
        }
}catch(Exception ex){
  System.out.println("An error has occured while trying to upload the data in the image file, make sure that the data file is smaller than the image file or try another image");
return null;
}
//return null;
}
}
