package org.xmpp.extension.avatar;

/**
 * Represents an avatar image.
 *
 * @author Christian Schudt
 */
public final class Avatar {
    private final String type;

    private final byte[] imageData;

    Avatar(String type, byte[] imageData) {
        this.type = type;
        this.imageData = imageData;

    }

    /**
     * Gets the image type, e.g. "image/jpeg"
     *
     * @return The type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the binary image data.
     *
     * @return The image data.
     */
    public byte[] getImageData() {
        return imageData;
    }
}
