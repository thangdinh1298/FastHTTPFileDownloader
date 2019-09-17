package Server;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.*;

public class RequestProcessor implements Runnable {
    private final static Logger logger = Logger.getLogger(
            RequestProcessor.class.getCanonicalName());
    private File rootDirectory;
    private String indexFileName = "index.html";
    private Socket connection;

    public RequestProcessor(File rootDirectory,
                            String indexFileName, Socket connection) {
        if (rootDirectory.isFile()) {
            throw new IllegalArgumentException(
                    "rootDirectory must be a directory, not a file");
        }
        try {
            rootDirectory = rootDirectory.getCanonicalFile();
        } catch (IOException ex) {
        }
        this.rootDirectory = rootDirectory;
        if (indexFileName != null) this.indexFileName = indexFileName;
        this.connection = connection;
    }

    @Override
    public void run() {
        // for security checks
        String root = rootDirectory.getPath();
        try {
            OutputStream raw = new BufferedOutputStream(
                    connection.getOutputStream()
            );
            Writer out = new OutputStreamWriter(raw);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder requestLine = new StringBuilder();
            String c;
            while ((c = in.readLine()) != null) {
                if (c.length() == 0)
                    break;
                requestLine.append(c + "\r\n");
            }

            String get = requestLine.toString();
            logger.info(connection.getRemoteSocketAddress() + " " + get);
            String[] tokens = get.split("\\s+");
            String method = tokens[0];
            String version = "";
            String contentRange = "";
            int rangeStart = 0;
            int rangeEnd = 0;
            if (method.equals("GET")) {
                String fileName = tokens[1];
                if (fileName.endsWith("/")) fileName += indexFileName;
                String contentType =
                        URLConnection.getFileNameMap().getContentTypeFor(fileName);
                if (tokens.length > 2) {
                    version = tokens[2];
                }

                for (int i = 0; i < tokens.length; i++) {
                    String tok = tokens[i];
                    if (tok.equals("Range:")) {
                        contentRange = tokens[i + 1].split("=")[1];
                        rangeStart = Integer.parseInt(contentRange.split("-")[0]);
                        rangeEnd = Integer.parseInt(contentRange.split("-")[1]);
                        break;
                    }
                }

                File theFile = new File(rootDirectory,
                        fileName.substring(1, fileName.length()));
                if (theFile.canRead()

                        // Don't let clients outside the document root
                        && theFile.getCanonicalPath().startsWith(root)) {
                    byte[] theData = Files.readAllBytes(theFile.toPath());
                    if (version.startsWith("HTTP/")) { // send a MIME header
                        if (contentRange.isEmpty()) {
                            sendHeader(out, "HTTP/1.0 200 OK", contentType, theData.length);
                            raw.write(theData);
                        } else {
                            int contentLength = rangeEnd - rangeStart + 1;
                            sendHeader(out, "HTTP/1.0 206 Partial Content", contentType, contentLength, contentRange);
                            for (int i = rangeStart; i <= rangeEnd; i++) {
                                raw.write(theData[i]);
                            }
                        }
                    }
                    // send the file; it may be an image or other binary data
                    // so use the underlying output stream
                    // instead of the writer
                    raw.flush();
                } else { // can't find the file
                    String body = new StringBuilder("<HTML>\r\n")
                            .append("<HEAD><TITLE>File Not Found</TITLE>\r\n")
                            .append("</HEAD>\r\n")
                            .append("<BODY>")
                            .append("<H1>HTTP Error 404: File Not Found</H1>\r\n")
                            .append("</BODY></HTML>\r\n").toString();
                    if (version.startsWith("HTTP/")) { // send a MIME header
                        sendHeader(out, "HTTP/1.0 404 File Not Found",
                                "text/html; charset=utf-8", body.length());
                    }
                    out.write(body);
                    out.flush();
                }
            } else { // method does not equal "GET"
                String body = new StringBuilder("<HTML>\r\n")
                        .append("<HEAD><TITLE>Not Implemented</TITLE>\r\n")
                        .append("</HEAD>\r\n")
                        .append("<BODY>")
                        .append("<H1>HTTP Error 501: Not Implemented</H1>\r\n")
                        .append("</BODY></HTML>\r\n").toString();
                if (version.startsWith("HTTP/")) { // send a MIME header
                    sendHeader(out, "HTTP/1.0 501 Not Implemented",
                            "text/html; charset=utf-8", body.length());
                }
                out.write(body);
//                out.flush();
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING,
                    "Error talking to " + connection.getRemoteSocketAddress(), ex);
        } finally {
            try {
                connection.close();
            } catch (IOException ex) {
                logger.info(ex.toString());
            }
        }
    }

    private void sendHeader(Writer out, String responseCode,
                            String contentType, int length) throws IOException {
        out.write(responseCode + "\r\n");
        Date now = new Date();
        out.write("Date: " + now + "\r\n");
        out.write("Server: JHTTP 2.0\r\n");
        out.write("Content-Length: " + length + "\r\n");
        out.write("Content-Type: " + contentType + "\r\n");
        out.write("Content-Disposition: attachment; filename=\"" + indexFileName + "\"" + "\r\n\r\n");
        out.flush();
    }

    private void sendHeader(Writer out, String responseCode,
                            String contentType, int length, String contentRange)
            throws IOException {
        out.write(responseCode + "\r\n");

        Date now = new Date();
        out.write("Date: " + now + "\r\n");
        out.write("Server: JHTTP 2.0\r\n");
        out.write("Content-Range: bytes " + contentRange + "/" + length + "\r\n");
        out.write("Content-Type: " + contentType + "\r\n");
        out.write("Accept-Ranges: bytes" + "\r\n");
        out.write("Content-Length: "     + length + "\r\n");
        out.write("Content-Disposition: attachment; filename=\"" + indexFileName + "\"" + "\r\n\r\n");
        out.flush();
    }
}
