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

    public Response(JsonElement jsonObject) {
        success = !jsonObject.getAsJsonObject().get("error").getAsBoolean();
        message = jsonObject.getAsJsonObject().get("message").getAsString();
    }

}
