package com.example.quickvenduser.utils;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EncryptionUtils {

    // Encrypt Image to Base64
    public static String encryptImage(byte[] imageBytes) {
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    // Decrypt Image from Base64
    public static byte[] decryptImage(String encryptedImage) {
        return Base64.decode(encryptedImage, Base64.DEFAULT);
    }

    // Utility method to convert bitmap to byte array
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
