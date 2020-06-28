package backed.sdk.obj;

import java.io.Serializable;

public class SessionCookie implements Serializable {

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public long getExpiry() {
        return expiry;
    }

    private String name, value;
    private long expiry;

    public SessionCookie(String name, String value, long expiry) {
        this.name = name;
        this.value = value;
        this.expiry = expiry;
    }

}
