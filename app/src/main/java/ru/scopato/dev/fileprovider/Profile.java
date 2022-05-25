package ru.scopato.dev.fileprovider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class Profile extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);

        // Получаем намерение, действие и MIME тип
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Если передан текст, обрабатываем его
            } else if (type.startsWith("image/")) {
                if (intent.getStringExtra(Intent.EXTRA_TEXT) != null) {
                    handleSendImageAndText(intent); // Если передано одно изображение и текст, обрабатываем его
                } else {
                    handleSendImage(intent); // Если передано одно изображение, обрабатываем его
                }
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Функция для намерения с несколькими изображениями
            }
        } else {} // Обработка других намерений, возможно полученных из списка программы

        // Кнопка, запускающая  явление GalleryActivity
        Button galleryCheck = (Button) findViewById(R.id.galleryImageBtn);
        galleryCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Profile.this, GalleryActivity.class);
                startActivity(galleryIntent);
            }
        });

        // Кнопка, запускающая  явление SendActivity
        Button editProfile = (Button) findViewById(R.id.editProfileBtn);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editProfileIntent = new Intent(Profile.this, SendActivity.class);
                startActivity(editProfileIntent);
            }
        });
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        TextView textView = (TextView) findViewById(R.id.textTv);
        if (sharedText != null) {
            textView.setText(sharedText);
            // Что-то ещё делаем с полученным текстом
        }
    }

    private void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        ImageView imageView = (ImageView) findViewById(R.id.imageIv);
        if (imageUri != null) {
            imageView.setImageURI(imageUri);
            // Что-то ещё делаем с полученным изображением
        }
    }

    private void handleSendImageAndText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        TextView textView = (TextView) findViewById(R.id.textTv);
        if (sharedText != null) {
            textView.setText(sharedText);
        }
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        ImageView imageView = (ImageView) findViewById(R.id.imageIv);
        if (imageUri != null) {
            imageView.setImageURI(imageUri);
        }
    }

    private void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imagesUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imagesUris != null) {
            // Что-то делаем с набором полученных картинок
        }
    }
}
