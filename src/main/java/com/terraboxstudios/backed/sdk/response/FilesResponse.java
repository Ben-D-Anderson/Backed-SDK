package com.terraboxstudios.backed.sdk.response;

import com.terraboxstudios.backed.sdk.obj.Directory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class FilesResponse extends Response {

    private final Directory rootDirectory;

    public FilesResponse(JsonElement jsonElement) {
        super(jsonElement);
        JsonArray filesJsonArray = jsonElement.getAsJsonObject().getAsJsonArray("files");
        rootDirectory = new Directory("", "", filesJsonArray, null, 0L);
    }

    public Directory getRootDirectory() {
        return rootDirectory;
    }

}
