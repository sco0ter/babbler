# HTTP Pre-Binding
---

```java
BoshConnectionConfiguration boshConnectionConfiguration = BoshConnectionConfiguration.builder()
        .hostname("domain")
        .port(5280)
        .file("/http-bind/")
        .build();
        
        
try (XmppClient xmppClient = new XmppClient("domain", boshConnectionConfiguration)) {
    // Connect
    xmppClient.connect();
    // Login
    xmppClient.login("username", "password", "resource");
    
    BoshConnection boshConnection = (BoshConnection) xmppClient.getActiveConnection();
    
    // Gets the session id (sid) of the BOSH connection.
    String sessionId = boshConnection.getSessionId();
    
    // Detaches the BOSH session, without terminating it.
    long rid = boshConnection.detach();
    
    System.out.println("JID: " + xmppClient.getConnectedResource());
    System.out.println("SID: " + sessionId);
    System.out.println("RID: " + rid);
} catch (XmppException e) {
    e.printStackTrace();
}
```
