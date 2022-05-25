package ru.scopato.dev.fileprovider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class SendActivity extends AppCompatActivity {

    private Uri imageUri = null;

    private Uri shareUri = null;

    private ArrayList<Uri> imageUris= new ArrayList<Uri>();

    private Intent shareIntent = new Intent();

    private Integer index = 0;

    private String textToShare = "";

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);

        Button pickButton = (Button) findViewById(R.id.pickImageBtn);
        pickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    pickImage();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        Button shareBoth = (Button) findViewById(R.id.shareBothBtn);
        shareBoth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 TextView textEt = findViewById(R.id.textEt);
                 textToShare = textEt.getText().toString().trim();
                if (TextUtils.isEmpty(textToShare)){
                    showToast(("Enter text..."));
                }
                else if (imageUri == null) {
                    showToast ("Pick image... ");
                } else {
                    shareBoth();
                }
            }
        });

        Button shareImages = (Button) findViewById(R.id.shareImagesBtn);
        shareImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check if image is  picked or not
                if (imageUri == null) {
                    showToast ("Pick image... ");
                }
                else {
                    shareImageMultiple();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Создаем меню из файла ресурса меню
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Находим нужный пункт меню
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Сохраняем объект ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        return true;
    }

    private void pickImage() throws FileNotFoundException {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        showToast("Image Picked from gallery");
                        Intent data = result.getData();
                        imageUri = data.getData(); // Получаем URI файла из галлереи
                        shareUri = bitmapCoder(); // Получаем URI c разрешением из внутреннего хранилища
                        imageUris.add(shareUri); // Добавляем в список URI
                        setIntent();
                        setShareIntent(shareIntent);
                    } else {
                        showToast("Cancelled...");
                    }
                }
            }
    );

    // Вызываем для запуска намерения отправки файлов по частям
    private void setIntent() {
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.setType("image/*");
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    // Вызываем для связывания намреения с пунктом меню расшаривания
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private void shareImageMultiple() {
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
    }

    private void shareBoth() {
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); // Use for send Email's
        shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        startActivity(Intent.createChooser(shareIntent, "Share Via"));
    }

    // Создаём копию файла во внутренней директории images/
    private Uri bitmapCoder() {
        Bitmap bitmap = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            }
        } catch (Exception e) {
            showToast("" + e.getMessage());
        }

        // Генерируем название файла для внутренней директории
        index += 1;
        File imagesFolder = new File(getCacheDir(), "images");
        String path = "shared_image_" + index + ".png";
        Uri contentUri = null;

        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, path);
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            stream.flush();
            stream.close();
            contentUri = FileProvider.getUriForFile(this, "ru.scopato.dev.fileprovider.fileprovider", file);
        } catch (Exception e) {
            showToast("" + e.getMessage());
        }
        return contentUri;
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
