package backed.sdk.obj;

public class File {

    public File(String name, String path, Directory directory, long lastModified) {
        this.name = name;
        this.path = path;
        this.directory = directory;
        this.lastModified = lastModified;
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

    public long getLastModified() {
        return lastModified;
    }

    private final String name, path;
    private final Directory directory;
    private final long lastModified;

}
