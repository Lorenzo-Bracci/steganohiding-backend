package main.java.com.steganohiding;

import org.apache.commons.io.FileUtils;
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

@WebServlet(name = "/RevealData", urlPatterns = {"/RevealData"})
@MultipartConfig                                               // specifies servlet takes multipart/form-data
public class RevealRequest extends HttpServlet {
   
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
String secretMessage = RevealData.reveal(tempFile);
tempFile.delete();
if(secretMessage == ""){
    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
}else{
/*response.setContentType("text/xml");
PrintWriter writer=response.getWriter();
writer.append(secretMessage);
writer.flush();*/
PrintWriter out = response.getWriter();
response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
out.print(secretMessage);
out.flush();            
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