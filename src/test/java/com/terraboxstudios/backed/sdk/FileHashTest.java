package com.terraboxstudios.backed.sdk;

import com.terraboxstudios.backed.sdk.response.LoginResponse;
import com.terraboxstudios.backed.sdk.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class FileHashTest {

    private final BackedAPI api = new BackedAPI("http://localhost:8080");

    @Test
    public void getFileHash() throws IOException {
        String path = "filename.txt";
        Response response = api.getFileHash(path);
        Assert.assertTrue("Get File Hash Succeeded", response.isSuccess());
    }

    @Before
    public void login() throws IOException {
        Response response = api.login("username", "password");
        if (response instanceof LoginResponse) {
            Assert.assertTrue("Login Succeeded", response.isSuccess());
        } else {
            Assert.assertFalse("Login Failed", response.isSuccess());
        }
    }

    @After
    public void logout() throws IOException {
        Response response = api.logout();
        Assert.assertTrue("Logout Succeeded", response.isSuccess());
    }

}
