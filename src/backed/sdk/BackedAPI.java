package backed.sdk;

import backed.sdk.obj.SessionCookie;
import backed.sdk.response.FilesResponse;
import backed.sdk.response.LoginResponse;
import backed.sdk.response.Response;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class BackedAPI {

    private String url;
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
     * @param path path of file to delete
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

        public HttpURLConnection getHttpUrlConnection() throws IOException {
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
