package com.mycamera.lightsoo.mycamera;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mycamera.lightsoo.mycamera.Manager.NetworkManager;
import com.mycamera.lightsoo.mycamera.RestAPI.FileAPI;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;


/**
 * 카메라 사용법!
 *
 *  카메라인 경우! ACTION_IMAGE_CAPTURE사용!
 * 1. 카메라(INTENT)로 찍은 이후의 사진은 크롭(INTENT)으로 프로세싱과정을 거친다
 * 2. 다음 사진을 비트맵팩토리를 이용하여 decode()를 하고 이미지뷰에 setImageBitmap()한다
 *
 *  앨범의 경우! ACTION_PICK + EXTRENAL_CONTENT_URL사용!
 * 1. 앨덤으로 들어가 PICK된 이미지의 URL을 가져온다음 크롭(INTENT)으로 프로세싱 과정을 거친다.
 * 2. 다음 사진을 비트맵팩토리를 이용하여 decode()를 하고 이미지뷰에 setImageBitmap()한다
 */

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button btn_album, btn_camera;

    //인텐트 사용시 onActivityResult에서 사용될 reqCode
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_ALBUM = 1;
    private static final int REQUEST_CROP = 2;

    //카메라를 찍은 다음 사진을 임시로 저장해서 이후에 크롭 인텐트를 이용해서
    // THEMP_PHOTO_FILE로 명명해서 크롭된 이미지를 사용

    private static final String TEMP_CAMERA_FILE = "temp_camera.jpg";
    private static final String TEMP_PHOTO_FILE = "temp_album.jpg";
////////////////////////////////////////////////////////////////////////////////
    //파일 업로드 테스트
    private Button btn_upload;
    private EditText title, director, year;

    private Uri imageIri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();

        btn_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAlbumImage();
            }
        });

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCameraImage();
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileUpload();
            }
        });
    }

    public void fileUpload(){

        File file = new File(Environment.getExternalStorageDirectory(),
                TEMP_PHOTO_FILE);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        Call call = NetworkManager.getInstance()
                .getAPI(FileAPI.class)
                .upload(requestBody,
                        title.getText().toString(),
                        director.getText().toString(),
                        Integer.parseInt(year.getText().toString()));
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Response response, Retrofit retrofit) {
                if(response.isSuccess()){
                    Toast.makeText(MainActivity.this, "파일업로드 성공인경우code(200~300)",
                            Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "영화업로드 실패는 아닌데 다른 코드..",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(MainActivity.this, t.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }


    //카메라를 통한 사진 or 앨범에서 가져온 이미지를 crop을 통해 처리한다
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //액티비티 결과가 이상는경우
        if(resultCode != RESULT_OK){return;}

        switch (requestCode){
            case REQUEST_CAMERA :
                String imagePath = Environment.getExternalStorageDirectory() + "/" + TEMP_CAMERA_FILE;
                /**
                 * @param cr The content resolver to use
                 * @param imagePath The path to the image to insert
                 * @param name The name of the image
                 * @param description The description of the image
                 * @return The URL to the newly created image
                 */
                try {
                    String url = MediaStore.Images.Media.insertImage(getContentResolver(), imagePath, "카메라 이미지", "기존 이미지");
                    Uri photouri = Uri.parse(url);
                    //ContentResolver가 처리할수있는 value들을 저장하는데 사용
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.ORIENTATION, 90);
                   //뭔지 잘모르겟지만, URL의 value로 대체한다
                    getContentResolver().update(photouri, values, null, null);
                    cropImage(photouri);//이미지 프로세싱
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case REQUEST_ALBUM :
            case REQUEST_CROP :
                //파일에 직접 접근해서 사용
                String filePath = Environment.getExternalStorageDirectory() + "/"
                        + TEMP_PHOTO_FILE;
                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 4;
                Bitmap selectedImage = BitmapFactory.decodeFile(filePath, options);
                imageView.setImageBitmap(selectedImage);
                break;
        }

    }

    public void init(){
        imageView = (ImageView)findViewById(R.id.imageView);
        btn_album = (Button)findViewById(R.id.btn_album);
        btn_camera = (Button)findViewById(R.id.btn_camera);
        btn_upload = (Button)findViewById(R.id.btn_upload);


        title = (EditText)findViewById(R.id.title);
        director = (EditText)findViewById(R.id.director);
        year = (EditText)findViewById(R.id.year);



    }

    //카메라 사용
    private void getCameraImage(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri fileUri =getTempCameraUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        //카메라액티비티 종료류 결과값을 받기위한 reqCODE;
        startActivityForResult(intent, REQUEST_CAMERA);

    }
    //카메라로 찍은 해당 File객체의 절대경로 리턴
    private Uri getTempCameraUri(){
        return Uri.fromFile(getCameraFile());
    }

    //카메라로 찍은 파일의 경로로 파일을 만든다
    private File getCameraFile(){
        //파일을 읽고쓰기가 가능한 상태인지 확인
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            /**
             * @param dir : 파일이 저장된 경로
             * @param fileName : 파일의 이름
             */
            File file = new File(Environment.getExternalStorageDirectory(),
                    TEMP_CAMERA_FILE);
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return file;
        }else{
            return null;
        }
    }


    //카메라 인텐트이후 이미지처리하기 위해 새로운 인텐트를 만들어서
    //임시로 저장돈 이미지를 처리해줘야되
    private void cropImage(Uri uri) {
        if (uri != null) {
            Intent photoPickerIntent = new Intent(
                    "com.android.camera.action.CROP", uri);
            //crop한 이미지를 저장할때 X by Y 사이즈로 저장
            //크롭된 이미지를 이크기만큼 받아오도록 지정하는거야
            //아니면 사용자가 지정한 만큼 받아와와
//            photoPickerIntent.putExtra("outputX", 300);
//            photoPickerIntent.putExtra("outputY", 200);

            photoPickerIntent.putExtra("scale", false);

            //Bundle을 통해 bitmap아로 데이터를 받아오겠다.
//            photoPickerIntent.putExtra("return-data", true);

            //crop박스의 x,y축 비율
//            photoPickerIntent.putExtra("aspectX", 1);
//            photoPickerIntent.putExtra("aspectY", 1);

            photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    getTempUri());
            photoPickerIntent.putExtra("outputFormat",
                    Bitmap.CompressFormat.JPEG.toString());
            startActivityForResult(photoPickerIntent, REQUEST_CROP);
        }

    }
    //crop을 통해 이미지 처리하기위해 파일 경로를 찾는거야
    //사진을 찍고 임시 저장된 파일을 크롭 인테트를 실행하거나, 갤러리에서 가져온 이후 인텐트를 실행할수있다.
    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    private File getTempFile() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            //TEMP_PHOTO_FILE이걸로 임시파일을 만들어서 사용
            File file = new File(Environment.getExternalStorageDirectory(),
                    TEMP_PHOTO_FILE);
            try {
                file.createNewFile();
            } catch (IOException e) {
            }
            return file;
        } else {
            return null;
        }
    }

    private void getAlbumImage(){
        Intent photoPickerIntent = new Intent(
                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra("crop", "true");

        photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
        photoPickerIntent.putExtra("outputFormat",
                Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(photoPickerIntent, REQUEST_ALBUM);
    }
}