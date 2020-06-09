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
        connectionBuilder.getOutputStream().write(("username=" + username + "&password=" + password).getBytes());
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
        JsonElement jsonElement = connectionBuilder.readJson();
        this.sessionCookie = null;
        return new Response(jsonElement);
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
        connectionBuilder.getOutputStream().write(("filename=" + path).getBytes());
        JsonElement jsonElement = connectionBuilder.readJson();
        return new Response(jsonElement);
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
        private String url, method = "POST";
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

        public OutputStream getOutputStream() throws IOException {
            return connection.getOutputStream();
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

        public JsonElement readJson() throws IOException {
            connection.connect();
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
