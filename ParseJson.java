package com.steganohiding;
import java.io.*;
import java.lang.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import org.json.*;
/*<servlet>
<servlet-name>HideRequest</servlet-name>
<servlet-class>HideRequest</servlet-class>
</servlet>
<servlet-mapping>
<servlet-name>HideRequest</servlet-name>
<url-pattern>/HideData</url-pattern>
</servlet-mapping> */
@WebServlet(name = "/HideRequest", urlPatterns = {"/HideRequest"})
public class HideRequest extends HttpServlet{
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException  {
//String secretData = request.getParameter("hello");
String secretData = "";
StringBuffer jb = new StringBuffer();
String line = null;
JSONObject jsonObject = null;
try {
    BufferedReader reader = request.getReader();
    while ((line = reader.readLine()) != null) {
        jb.append(line);
    }
} catch (Exception e) { System.out.println(e); }

try {
    System.out.println(jb.toString());
    jsonObject = new JSONObject(jb.toString());
} catch (JSONException e) {
    System.out.println("Error parsing JSON");
}  

try {
 secretData = (jsonObject.get("hiddenData")).toString(); 
} catch (JSONException e) {
    System.out.println(e);
}  


//response.setContentType("text/html");
response.setContentType("text/html");
PrintWriter out = response.getWriter();
out.println(secretData);
/*out.println("<html>");
out.println("<head>");
out.println("<title>" + secretData + "</title>");
out.println("</head>");
out.println("<body>");
out.println("<h1>hello</h1>");
out.println("</body>");
out.println("</html>");*/
    }
}