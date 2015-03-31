<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.io.*,java.util.*, javax.servlet.*" %>
<%@ page import="javax.servlet.http.*" %>
<%@ page import="org.apache.commons.fileupload.*" %>
<%@ page import="org.apache.commons.fileupload.disk.*" %>
<%@ page import="org.apache.commons.fileupload.servlet.*" %>
<%@ page import="org.apache.commons.io.output.*" %>

<%
   File file ;
   int maxFileSize = 100000000 * 1024;
   int maxMemSize = 5000 * 1024;
   ServletContext context = pageContext.getServletContext();
   String filePath = "C:\\Users\\azt0018\\git\\FileStorage\\";//context.getInitParameter("file-upload");
   String experimentName = "";
   // Verify the content type
   String contentType = request.getContentType();
   if ((contentType.indexOf("multipart/form-data") >= 0)) {

      DiskFileItemFactory factory = new DiskFileItemFactory();
      // maximum size that will be stored in memory
      factory.setSizeThreshold(maxMemSize);
      // Location to save data that is larger than maxMemSize.
      factory.setRepository(new File("C:\\azt0018\\git\\FileStorage\\Temp\\"));

      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);
      // maximum file size to be uploaded.
      upload.setSizeMax( maxFileSize );
      try{ 
         // Parse the request to get file items.
         List<FileItem> fileItems = upload.parseRequest(request);

         // Process the uploaded file items
         Iterator<FileItem> i = fileItems.iterator();
         String fileName = "";
         while ( i.hasNext () ) 
         {
            FileItem fi = (FileItem)i.next();
            if ( !fi.isFormField () ){
            	// Get the uploaded file parameters
            	String fieldName = fi.getFieldName();
            	//out.println("Field name: " + fieldName);
            	fileName = fi.getName();
            	boolean isInMemory = fi.isInMemory();
            	long sizeInBytes = fi.getSize();
            	//Create the folder for the uploaded files
            	new File(filePath + experimentName).mkdir();
            	filePath = filePath + experimentName + "\\";
            	// Write the file
            	if( fileName.lastIndexOf("\\") >= 0 ){
            		fileName = fileName.substring(fileName.lastIndexOf("\\"));
            		file = new File( filePath + fileName.substring( fileName.lastIndexOf("\\"))) ;
            	}else{
            		fileName = fileName.substring(fileName.lastIndexOf("\\")+1);
            		file = new File( filePath + fileName.substring(fileName.lastIndexOf("\\")+1)) ;
            	}
            	fi.write( file ) ;
            }else{
            	String fieldName = fi.getFieldName();
                String fieldValue = fi.getString();
                experimentName = fieldValue;
            }
         }
         response.sendRedirect("main.jsp?experimentName=" + experimentName + "&modelFileName=" + filePath + fileName + "&modelFolder=" + filePath);
      }catch(Exception ex) {
         System.out.println(ex);
      }
   }else{
      out.println("<html>");
      out.println("<head>");
      out.println("<title>Simulation model upload</title>");  
      out.println("</head>");
      out.println("<body>");
      out.println("<p>An error ocurred while uploading your simulation model file!</p>"); 
      out.println("</body>");
      out.println("</html>");
   }
%>