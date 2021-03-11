package com.terraboxstudios.backed.sdk;

import com.terraboxstudios.backed.sdk.obj.FileUploadObject;
import com.terraboxstudios.backed.sdk.obj.SessionCookie;
import com.terraboxstudios.backed.sdk.response.FilesResponse;
import com.terraboxstudios.backed.sdk.response.LoginResponse;
import com.terraboxstudios.backed.sdk.response.Response;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class BackedAPI {

    private final String url;
    private SessionCookie sessionCookie;

    /**
     * Initiate an instance of the backed api
     *
     * @param url the url of backed site, e.g. "http://localhost:8080/"
     */
    public BackedAPI(String url) {
        if (!url.endsWith("/")) url += "/";
        this.url = url + "api/";
    }

    /**
     * Login user to create a session
     *
     * @param username username of user to login
     * @param password password of user to login
     * @return Response object if login failed,
     * LoginResponse object if login was successful
     * (Response is superclass of LoginResponse)
     */
    public Response login(String username, String password) throws IOException {
        ConnectionBuilder connectionBuilder = new ConnectionBuilder();
        connectionBuilder.setUrl(url + "login");
        connectionBuilder.setMethod("POST");
        connectionBuilder.openConnection();
        connectionBuilder.getHttpUrlConnection().getOutputStream().write(("username=" + username + "&password=" + password).getBytes());
        connectionBuilder.connect();
        JsonElement jsonElement = connectionBuilder.readJson();
        Response response = new Response(jsonElement);
        if (response.isSuccess()) {
            LoginResponse loginResponse = new LoginResponse(jsonElement);
            this.sessionCookie = loginResponse.getCookie();
            return loginResponse;
        }
        return response;
    }

    /**
     * Logout the current user and invalidate the session cookie
     *
     * @return Response object of response.
     */
    public Response logout() throws IOException {
        ConnectionBuilder connectionBuilder = new ConnectionBuilder();
        connectionBuilder.setUrl(url + "logout");
        connectionBuilder.setMethod("GET");
        connectionBuilder.setCookie(this.sessionCookie);
        connectionBuilder.openConnection();
        connectionBuilder.connect();
        this.sessionCookie = null;
        return new Response(connectionBuilder.readJson());
    }

    /**
     * Upload a file to the storage of current user.
     *
     * @param fileUploadObjects The FileUploadObject of the file to upload.
     * @return Response object of json response.
     */
    public Response[] uploadFiles(FileUploadObject[] fileUploadObjects) throws IOException {
        if (fileUploadObjects.length == 0) {
            return null;
        }

        ConnectionBuilder connectionBuilder = new ConnectionBuilder();
        connectionBuilder.setUrl(url + "upload");
        connectionBuilder.setMethod("POST");
        connectionBuilder.setCookie(this.sessionCookie);
        connectionBuilder.openConnection();

        String boundary = Long.toHexString(System.currentTimeMillis());
        String CRLF = "\r\n";
        connectionBuilder.getHttpUrlConnection().setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(connectionBuilder.getHttpUrlConnection().getOutputStream(), StandardCharsets.UTF_8), true);

        for (int i = 0; i < fileUploadObjects.length; i++) {
            FileUploadObject f = fileUploadObjects[i];
            writer.append("--").append(boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"file").append(String.valueOf(i)).append("\"; filename=\"").append(f.getPath()).append("\"").append(CRLF);
            writer.append("Content-Type: text/plain; charset=").append("UTF-8").append(CRLF);
            writer.append(CRLF).flush();
            Files.copy(f.getFile().toPath(), connectionBuilder.getHttpUrlConnection().getOutputStream());
            connectionBuilder.getHttpUrlConnection().getOutputStream().flush();
            writer.append(CRLF).flush();
        }
        writer.append("--").append(boundary).append("--").append(CRLF).flush();

        connectionBuilder.connect();
        JsonElement jsonElement = connectionBuilder.readJson();
        Response[] responses = new Response[jsonElement.getAsJsonArray().size()];
        for (int i = 0; i < jsonElement.getAsJsonArray().size(); i++) {
            responses[i] = new Response(jsonElement.getAsJsonArray().get(i));
        }
        return responses;
    }

    /**
     * Download specific file belonging to current user to output stream
     *
     * @param path path to download file from on web server (includes file name)
     * @param outputStream output stream to write downloaded file to
     * @param buffer byte array to act as a buffer when downloading file
     * @return Response object for the json response
     */
    public Response downloadFileToOutputStream(String path, OutputStream outputStream, byte[] buffer) throws IOException {
        ConnectionBuilder connectionBuilder = new ConnectionBuilder();
        connectionBuilder.setUrl(url + "download");
        connectionBuilder.setMethod("POST");
        connectionBuilder.setCookie(this.sessionCookie);
        connectionBuilder.openConnection();
        connectionBuilder.getHttpUrlConnection().getOutputStream().write(("filename=" + path).getBytes());
        connectionBuilder.connect();

        if (connectionBuilder.getHttpUrlConnection().getContentType().equalsIgnoreCase("application/json")) {
            return new Response(connectionBuilder.readJson());
        }

        int len;
        while ((len = connectionBuilder.getHttpUrlConnection().getInputStream().read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }

        return new Response(new JsonParser().parse("{\"error\": \"false\", \"message\": \"file successfully written to output stream\"}"));
    }

    /**
     * Delete a specified file belonging to the current user
     *
     * @param path path of file to delete (relative to root directory of the user)
     * @return Response object of response.
     */
    public Response deleteFile(String path) throws IOException {
        ConnectionBuilder connectionBuilder = new ConnectionBuilder();
        connectionBuilder.setUrl(url + "delete");
        connectionBuilder.setMethod("POST");
        connectionBuilder.setCookie(this.sessionCookie);
        connectionBuilder.openConnection();
        connectionBuilder.getHttpUrlConnection().getOutputStream().write(("filename=" + path).getBytes());
        connectionBuilder.connect();
        return new Response(connectionBuilder.readJson());
    }

    /**
     * List files of current user
     *
     * @return Response object if list files failed,
     * FilesResponse object if list files was successful
     * (Response is superclass of FilesResponse)
     */
    public Response listFiles() throws IOException {
        ConnectionBuilder connectionBuilder = new ConnectionBuilder();
        connectionBuilder.setUrl(url + "files");
        connectionBuilder.setMethod("GET");
        connectionBuilder.setCookie(this.sessionCookie);
        connectionBuilder.openConnection();
        connectionBuilder.connect();
        JsonElement jsonElement = connectionBuilder.readJson();
        Response response = new Response(jsonElement);
        if (response.isSuccess()) return new FilesResponse(jsonElement);
        return response;
    }

    /**
     * Get SHA256 hash of a decrypted file
     *
     * @param path path of file to get the hash of (relative to root directory of the user)
     * @return Response object of the response
     */
    public Response getFileHash(String path) throws IOException {
        ConnectionBuilder connectionBuilder = new ConnectionBuilder();
        connectionBuilder.setUrl(url + "hash");
        connectionBuilder.setMethod("POST");
        connectionBuilder.setCookie(this.sessionCookie);
        connectionBuilder.openConnection();
        connectionBuilder.getHttpUrlConnection().getOutputStream().write(("filename=" + path).getBytes());
        connectionBuilder.connect();
        return new Response(connectionBuilder.readJson());
    }

    /**
     * Reuse an active session cookie instead of logging in
     *
     * @param sessionCookie SessionCookie object of cookie
     */
    public void setSessionCookie(SessionCookie sessionCookie) {
        this.sessionCookie = sessionCookie;
    }

    private static class ConnectionBuilder {
        private String url, method;
        private SessionCookie sessionCookie;
        private HttpURLConnection connection;

        public void setUrl(String url) {
            this.url = url;
        }

        public void setCookie(SessionCookie sessionCookie) {
            this.sessionCookie = sessionCookie;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public HttpURLConnection getHttpUrlConnection() {
            return connection;
        }

        public void openConnection() throws IOException {
            URLConnection con = new URL(url).openConnection();
            HttpURLConnection connection = (HttpURLConnection) con;
            connection.setRequestMethod(method);
            if (sessionCookie != null) {
                connection.setRequestProperty("Cookie", sessionCookie.getName() + "=" + sessionCookie.getValue());
            }
            connection.setRequestProperty("User-Agent", "backed/api");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            this.connection = connection;
        }

        public void connect() throws IOException {
            connection.connect();
        }

        public JsonElement readJson() throws IOException {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                return new JsonParser().parse(response.toString());
            }
        }

    }

}
