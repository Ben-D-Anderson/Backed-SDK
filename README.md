# **Backed SDK**
###Getting Started
To start you will need an instance of the API, this can be achieved by creating an instance of the BackedAPI class. An url of a running backed site must be provided into the constructor as a string.
For example, if the site was running on localhost, you may use the following code to obtain an API instance:
```
BackedAPI api = new BackedAPI("http://localhost:8080");
```

Please bear in mind that an instance of the api is directly related to one user account and another instance should be created if you wish to use a different user account.
<hr>

###Logging In
Using your api instance, you can call the login method which takes a username and password as strings for parameters. The login method returns a Response object, and if successful, will return a LoginResponse instance which is a subclass Response.
```
Response response = api.login("username", "password");
if (response instanceof LoginResponse) {
    LoginResponse loginResponse = (LoginResponse) response;
    //Login Successful
} else {
    //Login Failed
}
```
If the login was successful, you can cast the Response to a LoginResponse object as seen above and gather data from the LoginResponse instance such as the cookie. However, this data isn't required for other methods as the BackedAPI instance will automatically attach the received cookie to the api instance.

The LoginResponse class has a method called getCookie which returns an instance of SessionCookie.
You can gather the name, value and expiry of the cookie using the code below.
```
String cookieName = loginResponse.getCookie().getName();
String cookieValue = loginResponse.getCookie().getValue();
long cookieExpiry = loginResponse.getCookie().getExpiry();
```
It is worth noting that the getExpiry method in SessionCookie will return the time in UTC milliseconds as a long.

If the login wasn't successful, then to understand what went wrong you can call the Response.getMessage() method which returns the response message from the server - this is present for every response.
<hr>

###Logging Out
Using your api instance, you can call the logout method which will invalidate the current cookie and logout the user. As always, a Response object is returned and you can check if the logout worked or not with Response.isSuccess() which returns a boolean.

Logging out will only work if the user is actually logged in, and their cookie is still valid. Here is an example of logging out:

```
Response response = api.logout()
```
Once logged out, other methods in the api such as listFiles will no longer work unless the login method or setSessionCookie method is successfully executed.
<hr>

###Listing Files
Using your api instance, you can call the listFiles method which returns a Response object, and if successful, will return a FilesResponse instance which is a subclass Response.
```
Response response = api.listFiles();
if (response instanceof FilesResponse) {
    FilesResponse filesResponse = (FilesResponse) response;
    //File Listing Successful
} else {
    //File Listing Failed
}
```
The FilesResponse class extends the Response class whilst also having the method getRootDirectory. This will return a Directory object which in itself can contain many Directory and File objects, last modified dates and further sub-directories.

The example below demonstrates one way that all directory names in the root folder of the user can be output from the FilesResponse object:
```
filesResponse.getRootDirectory().getDirectories().stream()
                                                    .map(x -> x.getName())
                                                    .forEach(System.out::println);
```
<hr>

###Deleting Files
Using your api instance, you can call the deleteFile method which takes the path of the file to delete relative to the root directory of the user as a string for parameters and returns a Response object.

The example below attempts to delete the file "filename.txt" in the user's root directory and checks if the operation was successful.
```
Response response = api.deleteFile("filename.txt");
if (response.isSuccess()) {
    //File was deleted
} else {
    //File couldn't be deleted
    //Debug using response.getMessage()
}
```
<hr>

###Downloading Files
Using your api instance, you can call the downloadFileToOutputStream method which takes three parameters:
- The path of the file to download relative to the user's root directory as a string.
- The OutputStream to write the data downloaded to, this will often be a FileOutputStream object.
- The buffer of how many bytes to read and write at once as a byte array.

The example below demonstrates downloading a file named "filename.txt" in the user's root directory onto the local machine at the path "D:\Downloads\downloaded.txt" with a byte buffer of 1000000 bytes (1000 x 1000):
```
String path = "filename.txt";
FileOutputStream fos = new FileOutputStream("D:\\Downloads\\downloaded.txt");
byte[] buffer = new byte[1000 * 1000];
Response response = api.downloadFileToOutputStream(path, fos, buffer);
if (response.isSuccess()) {
    //File was downloaded
} else {
    //File couldn't be downloaded
    //Debug using response.getMessage()
}
```
<hr>

###Uploading Files
Using your api instance, you can call the uploadFiles method which takes an array of FileUploadObject as the parameter to allow uploading of one or multiple files in one call. The method returns an array of Response objects which are linked to each FileUploadObject (with the same index) that was supplied as an argument. If the array is empty, the method will return null.

FileUploadObject is a class which can be initialised using the constructor FileUploadObject(java.io.File file, String path). The first parameter, file, is the java.io.File object associated with the file on the local machine. The second parameter, path, is the path for the file to be uploaded to which is relative to the user's root directory.

The example below demonstrates uploading a file from the local machine ("D:\Downloads\fileToUpload.txt") to the backed server with the path "uploaded.txt" relative to the user's root directory. The messages of all supplied Response objects are then printed.
```
File file = new File("D:\\Downloads\\fileToUpload.txt");
FileUploadObject fuo = new FileUploadObject(file, "uploaded.txt");
Response[] responses = api.uploadFiles(new FileUploadObject[] { fuo });
for (Response response : responses) {
    System.out.println(response.getMessage());
}
```
<hr>

###Manually Setting Session Cookie
For optimization within an application, it may be preferred to store and reuse a session cookie until it's expiry time therefore creating the least amount of login requests.

This is where the method setSessionCookie comes in as it takes a SessionCookie object as it's only parameter.

The SessionCookie class is serializable therefore if you ever wish to save a cookie into a file, you can do so and then read the file to an object later for use. From there, the setSessionCookie method comes into effect as you can send the restored object as the argument, and it will reuse the old cookie without creating a new login request.
<br><br>
<hr>
