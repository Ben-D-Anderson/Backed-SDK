package backed.sdk;

import backed.sdk.obj.SessionCookie;
import backed.sdk.response.LoginResponse;
import backed.sdk.response.Response;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        if (!url.endsWith("/")) url += "/api/";
        this.url = url;
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
        connectionBuilder.getConnection().getOutputStream().write(("username=" + username + "&password=" + password).getBytes());
        JsonElement jsonElement = connectionBuilder.readJson();
        Response response = new Response(jsonElement);
        if (response.isSuccess())
            return new LoginResponse(jsonElement);
        return response;
    }

    private class ConnectionBuilder {
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

        public HttpURLConnection getConnection() {
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
