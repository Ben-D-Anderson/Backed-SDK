package backed.sdk.response;

import com.google.gson.JsonElement;

public class Response {

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    private boolean success;
    private String message;

    public Response(JsonElement jsonElement) {
        success = !jsonElement.getAsJsonObject().get("error").getAsBoolean();
        message = jsonElement.getAsJsonObject().get("message").getAsString();
    }

}
