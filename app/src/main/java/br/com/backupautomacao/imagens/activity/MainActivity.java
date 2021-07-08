package br.com.backupautomacao.imagens.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import br.com.backupautomacao.imagens.R;
import br.com.backupautomacao.imagens.models.ImageService;

public class MainActivity extends AppCompatActivity {

  String TAG = "MainActivity";
  ImageView imView;
  Button buttonTakePicture;
  Button buttonGetPicture;
  Button buttonDeletePictures;
  Button buttonSaveToCloud;
  Button buttonGetAllImages;
  CheckBox checkBoxStatus;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    imView = findViewById(R.id.imageView);
    buttonTakePicture = findViewById(R.id.btnTakePicture);
    buttonGetPicture = findViewById(R.id.btnGetPicture);
    buttonDeletePictures = findViewById(R.id.btnDeletePictures);
    buttonSaveToCloud = findViewById(R.id.btnSaveToCloud);
    buttonGetAllImages = findViewById(R.id.btnGetAllImages);
    checkBoxStatus = findViewById(R.id.cbxStatus);

    buttonSaveToCloud.setOnClickListener(v -> {
      boolean stageRoute = checkBoxStatus.isChecked();
      AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
      try {
        Bitmap bitmapFromFolder = ImageService.getImageFromFolder(stageRoute, MainActivity.this);
        builder
            .setTitle("Deseja salvar a imagem?")
            .setPositiveButton("Confirmar", (dialog, which) -> {
              byte[] byteImage = ImageService.transformImageIntoBytes(bitmapFromFolder);
              ImageService.saveImageToFirebase(byteImage, MainActivity.this);
            })
            .setNegativeButton("Cancelar", (dialog, which) -> {
              dialog.dismiss();
            });
        builder.show();
      } catch (IOException e) {
        e.printStackTrace();
      }

    });

    buttonGetAllImages.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          ArrayList<Bitmap> images = ImageService.getAllImageFromFolder(MainActivity.this);
          for( Bitmap image : images ){
            byte[] bitmapByte = ImageService.transformImageIntoBytes(image);
            ImageService.saveImageToFirebase(bitmapByte, MainActivity.this);
          }
        } catch (IOException e) {
          Log.i(TAG, "Erros ao recuperar imagens: " + e.getMessage());
        }
      }
    });

    buttonDeletePictures.setOnClickListener(v -> {
      try {
        ImageService.deleteAllImages(getApplicationContext());
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    buttonTakePicture.setOnClickListener(v -> CropImage
        .activity()
        .setFixAspectRatio(true)
        .setAspectRatio(2, 1)
        .start(MainActivity.this));

    buttonGetPicture.setOnClickListener(v -> {
      try {
        boolean stageRoute = checkBoxStatus.isChecked();
        Bitmap bitmapFromFolder = ImageService.getImageFromFolder(stageRoute, this);
        if (bitmapFromFolder != null) {
          imView.setImageBitmap(bitmapFromFolder);
        } else {
          Toast.makeText(this, "Imagem está vazia", Toast.LENGTH_SHORT).show();
        }
      } catch (IOException e) {
        Toast.makeText(this, "Erro ao acessar a imagem", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Erro ao acessar a imagem!!!");
        e.printStackTrace();
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
      CropImage.ActivityResult result = CropImage.getActivityResult(data);
      if (resultCode == RESULT_OK) {
        Uri resultUri = result.getUri();

        try {

          Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
          File imageFile = ImageService.createImageFile(checkBoxStatus.isChecked(), this);

          OutputStream outputStream = new FileOutputStream(imageFile, false);
          bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

          imView.setImageBitmap(bitmap);
          outputStream.flush();
          outputStream.close();

        } catch (FileNotFoundException e) {
          Log.i(TAG, "onActivityResult: Arquivo não encontrado");
        } catch (IOException e) {
          Log.i(TAG, "onActivityResult:" + e.getMessage());
          e.printStackTrace();
        }
      } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
        Exception error = result.getError();
        Log.i(TAG, "onActivityResult:" + error);
      }
    }
  }
}