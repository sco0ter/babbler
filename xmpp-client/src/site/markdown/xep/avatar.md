# Avatars
---

## Publishing your Avatar

```java
try {
    // Get the avatar manager
    AvatarManager avatarManager = xmppSession.getExtensionManager(AvatarManager.class);

    // Choose a file with JavaFX file dialog.
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(null);

    // If the user has chosen a file
    if (file != null) {
        // Read the file as image.
        BufferedImage bufferedImage = ImageIO.read(file);

        // Publish the image as your avatar.
        avatarManager.publishAvatar(bufferedImage);
    }
} catch (IOException | XmppException e) {
    // Deal with is. Chosen file was probably no image file.
}
```

[vCard-Based Avatars]: http://xmpp.org/extensions/xep-0153.html "XEP-0153: vCard-Based Avatars"
