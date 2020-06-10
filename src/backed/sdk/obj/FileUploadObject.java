package backed.sdk.obj;

public class FileUploadObject {

    private final java.io.File file;

    private final String path;

    public FileUploadObject(java.io.File file, String path) {
        this.file = file;
        this.path = path;
    }

    public java.io.File getFile() {
        return file;
    }

    public String getPath() {
        return path;
    }

}
