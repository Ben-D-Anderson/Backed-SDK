package backed.sdk.response;

import backed.sdk.obj.SessionCookie;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LoginResponse extends Response {

    public LoginResponse(JsonElement jsonObject) {
        super(jsonObject);
        JsonObject cookieObject = jsonObject.getAsJsonObject().getAsJsonObject("cookie");
        this.cookie = new SessionCookie(cookieObject.get("name").getAsString(), cookieObject.get("value").getAsString(), cookieObject.get("expiry").getAsLong());
    }

    private final SessionCookie cookie;

    public SessionCookie getCookie() {
        return cookie;
    }

}
