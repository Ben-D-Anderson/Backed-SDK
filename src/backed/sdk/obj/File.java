package backed.sdk.obj;

public class File {

    public File(String name, String path, Directory directory) {
        this.name = name;
        this.path = path;
        this.directory = directory;
    }

    public Directory getDirectory() {
        return directory;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    private final String name, path;
    private final Directory directory;

}
