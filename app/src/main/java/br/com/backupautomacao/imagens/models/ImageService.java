package br.com.backupautomacao.imagens.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import br.com.backupautomacao.imagens.firebase.Firebase;
import br.com.backupautomacao.imagens.helpers.DateHelper;

public class ImageService {
  private static String TAG = "ImageService";

  public static void saveImageToFirebase(byte[] image, Context context) {
    String imageFormat = ".JPEG";
    FirebaseStorage storage = Firebase.getFirebaseStorage();
    String now = DateHelper.formatDate(System.currentTimeMillis());
    String[] timeSplit = now.split("-");
    String year = timeSplit[0];
    String month = timeSplit[1];
    String day = timeSplit[2];

    UploadTask taskToSaveImage = storage.getReference()
        .child("motoristas")
        .child("gabriel")
        .child(year)
        .child(month)
        .child(day)
        .child(now + imageFormat)
        .putBytes(image);

    taskToSaveImage.addOnSuccessListener(taskSnapshot -> {
      taskSnapshot
          .getStorage()
          .getDownloadUrl()
          .addOnSuccessListener(uri -> {
            Toast.makeText(context, "Imagem salva com sucesso", Toast.LENGTH_SHORT).show();

          })
          .addOnFailureListener(e -> Log.i(TAG, "saveImageToFirebase: Falha ao salvar imagem " + e.getMessage()));
    });
  }

  public static File createImageFile(boolean stageOfRoute, Context context) {

    String imageFileNameStartRoute = "pictureStartRoute.jpg";
    String imageFileNameEndRoute = "pictureEndRoute.jpg";
    String imageFileName = stageOfRoute ? imageFileNameStartRoute : imageFileNameEndRoute;
    File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    Log.i(TAG, "createImageFile: " + storageDir + imageFileName);
    File image = new File(storageDir, imageFileName);

    return image;
  }

  public static Bitmap getImageFromFolder(boolean stageRoute, Context context) throws IOException {

    String fileName = stageRoute ? "pictureEndRoute.jpg" : "pictureStartRoute.jpg";
    File imageFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
    Uri imageUri = FileProvider.getUriForFile(context, "br.com.backupautomacao.imagens.fileprovider", imageFile);
    Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
    return bitmap;
  }

  public static ArrayList<Bitmap> getAllImageFromFolder(Context context)throws IOException {

    ArrayList<Bitmap> images = new ArrayList<>();
    File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    if (storageDir.isDirectory()) {
      String[] files = storageDir.list();
      for (String file : files) {
        images.add(getImageFromFolder(true, context));
      }
    }

    return images;
  }

  public static byte[] transformImageIntoBytes(Bitmap bitmap) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
    return baos.toByteArray();
  }

  public static void deleteAllImages(Context context) throws IOException{
    File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    if (storageDir.isDirectory()) {
      String[] files = storageDir.list();
      for (String file : files) {
        Log.i(TAG, "deleteAllImages: " + file);
        new File(storageDir, file).delete();
      }
    }
  }
}
