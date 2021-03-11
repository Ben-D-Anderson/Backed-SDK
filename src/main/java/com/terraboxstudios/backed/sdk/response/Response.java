package com.terraboxstudios.backed.sdk.response;

import com.google.gson.JsonElement;

public class Response {

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    private final boolean success;
    private final String message;

    public Response(JsonElement jsonElement) {
        success = !jsonElement.getAsJsonObject().get("error").getAsBoolean();
        message = jsonElement.getAsJsonObject().get("message").getAsString();
    }

}
