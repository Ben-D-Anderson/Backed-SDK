package backed.sdk.obj;

import com.google.gson.JsonArray;

import java.util.ArrayList;

public class Directory {

    private final ArrayList<Directory> directories = new ArrayList<>();
    private final ArrayList<File> files = new ArrayList<>();
    private final Directory parentDirectory;
    private final String name, path;

    public Directory(String name, String path, JsonArray jsonArray, Directory parentDirectory) {
        this.name = name;
        this.path = path;
        this.parentDirectory = parentDirectory;
        for (int i = 0; i < jsonArray.size(); i++) {
            if (jsonArray.get(i).getAsJsonObject().get("type").getAsString().equals("directory")) {
                String directoryName = jsonArray.get(i).getAsJsonObject().get("name").getAsString();
                String directoryPath = path + directoryName + "/";
                JsonArray files = jsonArray.get(i).getAsJsonObject().get("files").getAsJsonArray();
                directories.add(new Directory(directoryName, directoryPath, files, this));
            } else {
                String fileName = jsonArray.get(i).getAsJsonObject().get("name").getAsString();
                String filePath = path + fileName;
                files.add(new File(fileName, filePath, this));
            }
        }
    }

    public ArrayList<Directory> getDirectories() {
        return directories;
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public Directory getParentDirectory() {
        return parentDirectory;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public Directory getChildDirectory(String directoryName) {
        for (Directory directory : directories) {
            if (directory.getName().equals(directoryName))
                return directory;
        }
        return null;
    }

}
