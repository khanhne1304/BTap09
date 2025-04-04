package com.example.bt09;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadActivity extends AppCompatActivity {

    Button btnChooseFile, btnUpdateImage;
    ImageView ivProfile;
    private Uri mUri;
    public static final int MY_REQUEST_CODE = 100;

    public static String[] storge_permissions = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storge_permissions_33 = {
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.READ_MEDIA_VIDEO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload);

        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnUpdateImage = findViewById(R.id.btnUpdateImages);
        ivProfile = findViewById(R.id.imgProfile);

        btnUpdateImage.setOnClickListener(v -> updateImage());

        // Bắt sự kiện nút chọn ảnh
        btnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckPermission(); // checks quyền
                // chooseImage();
            }
        });
    }

    public static String[] permissions() {
        String[] p;
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            p = storge_permissions_33;
        } else {
            p = storge_permissions;
        }
        return p;
    }

    private void CheckPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();
            return;
        }

        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            // ActivityCompat.requestPermissions(UploadImageActivity.this, permissions(), MY_REQUEST_CODE);
            requestPermissions(permissions(), MY_REQUEST_CODE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // request code
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }
                        Uri uri = data.getData();
                        mUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            ivProfile.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    // Gửi yêu cầu cập nhật ảnh
    private void updateImage() {
        // ID người dùng
        RequestBody id = RequestBody.create(MediaType.parse("text/plain"), "5");

        // Create RequestBody instance from file
        String IMAGE_PATH = RealPathUtil.getRealPath(this, mUri);

        File file = new File(IMAGE_PATH);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData(Const.MY_IMAGES, file.getName(), requestFile);

        // Gọi API
        ServiceAPI.serviceapi.updateImage(id, imagePart).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Parse the JSON response
                        String responseString = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseString);

                        JSONArray resultArray = jsonResponse.getJSONArray("result");
                        if (resultArray.length() > 0) {
                            JSONObject userObject = resultArray.getJSONObject(0);
                            String imageUrl = userObject.getString("images");

                            // Navigate to ProfileActivity with the image URL
                            Intent intent = new Intent(UploadActivity.this, ProfileActivity.class);
                            intent.putExtra("imageUrl", imageUrl);
                            startActivity(intent);
                        }

                    } catch (Exception e) {
                        Log.e("UploadActivity", "Error parsing response: " + e.getMessage());
                        Toast.makeText(UploadActivity.this, "Error parsing response", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("UploadActivity", "Upload failed: " + response.message());
                    Toast.makeText(UploadActivity.this, "Upload failed: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Xử lý lỗi kết nối
                Log.e("API", t.getMessage());
            }
        });
    }
}