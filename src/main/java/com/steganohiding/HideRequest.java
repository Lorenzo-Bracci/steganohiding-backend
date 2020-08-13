package main.java.com.steganohiding;

import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.IOUtils;
import java.io.*;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet(name = "/HideData", urlPatterns = {"/HideData"})
@MultipartConfig                                               // specifies servlet takes multipart/form-data
public class HideRequest extends HttpServlet {
   
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        //response.setContentType("text/html;charset=UTF-8");
       // PrintWriter out = response.getWriter();
        try {
            // get access to file that is uploaded from client
            Part p1 = request.getPart("file");
            InputStream is = p1.getInputStream();
          //  InputStream is2 = is;
            File tempFile = File.createTempFile( "currentFile", ".png" );
            FileUtils.copyToFile( is, tempFile );
            // read filename which is sent as a part
            Part p2  = request.getPart("secretData");
            Scanner s = new Scanner(p2.getInputStream());
            String hiddenData = s.nextLine();
            while(s.hasNextLine())//add carriage return
            hiddenData = hiddenData + "\r\n" + s.nextLine();    // read filename from stream
//System.out.println(hiddenData);
File newFile = HideData.hide(tempFile, hiddenData);
tempFile.delete();
if(newFile == null || hiddenData == ""){
    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
}else{
InputStream in = new FileInputStream(newFile);
            // get filename to use on the server
            //String outputfile = this.getServletContext().getRealPath(filename);  // get path on the server
            //FileOutputStream os = new FileOutputStream (outputfile);
            OutputStream os = response.getOutputStream();
            response.setContentType("image/png");

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {

                    os.write(buffer, 0, bytesRead);
                }
                in.close();
                os.close();
                newFile.delete();
            // write bytes taken from uploaded file to target file
           /* int ch = is.read();
            while (ch != -1) {
                 os.write(ch);
                 ch = is.read();
            }
            os.close();
            out.println("<h3>File uploaded successfully!</h3>");*/
        }
        }
        catch(Exception ex) {
            System.out.println(ex);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
          //  response.setContentType("text/html;charset=UTF-8");
        //PrintWriter out = response.getWriter();
        //out.println("Exception -->" + ex.getMessage());
        }
    } // end of doPost()
 } // end of UploadServlet