package ru.scopato.dev.fileprovider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;

public class GalleryActivity extends AppCompatActivity {

    // Путь корневой внутренней внешнего хранилища
    private File mPrivateRootDir;
    // Путь до каталога images
    private File mImagesDir;
    // Массив файлов из директории images
    File[] mImageFiles;
    // Массив имён файлов соответствующих mImageFiles
    String[] mImageFilenames;
    // URI, получающая для стороннего приложения временное разрешение
    private Uri fileUri = null;

    //Инициализация явления
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Создание намерения для возврата запрошенного файла
        Intent mResultIntent = new Intent("ru.scopato.dev.fileprovider.ACTION_RETURN_FILE");
        //Получаем директорию cache/ внутреннего хранилища
        mPrivateRootDir = getCacheDir();
        // Получаем директорию cache/images
        mImagesDir = new File(mPrivateRootDir, "images");
        // Получаем файлы из директории images
        mImageFiles = mImagesDir.listFiles();
        // Устанавливаем начальный результат явления в null
        setResult(Activity.RESULT_CANCELED, null);

        mImageFilenames = new String[mImageFiles.length];
        for (int i = 0; i < mImageFiles.length; i++) {
            mImageFilenames[i] = mImageFiles[i].getName();

        }
        /*
         * Показываем имена файлов в компоненте ListView mFileListView
         * Возвращаем ListView с массивом mImageFilenames, который
         * мы создали перебирая mImageFiles и
         * вызываем метод File.getAbsolutePath() для каждого из файлов
         */
        ListView mFileListView = findViewById(android.R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mImageFilenames);
        mFileListView.setAdapter(adapter);

        // Создаём обработчик нажатия на элементы ListView
        mFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /*
             * Когда файл в ListView выбран,
             * полуачем его URI и передаём в вызвавшее приложение
             */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {
                /*
                 * Получаем объект File для выбранного файла
                 * Мы считаем, что имя файла содержится
                 * в массиве mImageFilename
                 */
                File requestFile = new File(mImagesDir, mImageFilenames[position]);
                /*
                 * Все файловые операции лучше производить в блоках try-catch
                 * Используем объект FileProvider для получения URI файла
                 */
                try {
                    fileUri = FileProvider.getUriForFile(GalleryActivity.this, "ru.scopato.dev.fileprovider.fileprovider", requestFile);
                } catch (IllegalArgumentException e) {
                    Log.e("File Selector", "The selected file can't be shared: " + requestFile);
                }
                /*
                 * Выдаём временные права на чтение по заданному URI
                 * Добавляем URI и MIME тип в намерение-результат
                 * Устанавливаем результат
                 */
                if (fileUri != null) {
                    mResultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    mResultIntent.setDataAndType(fileUri, getContentResolver().getType(fileUri));
                    GalleryActivity.this.setResult(Activity.RESULT_OK, mResultIntent);
                } else {
                    mResultIntent.setDataAndType(null, "");
                    GalleryActivity.this.setResult(Activity.RESULT_CANCELED, mResultIntent);
                }
                finish();
            }
        });
    }
    // Метод вызывается при нажатии кнопки
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}
