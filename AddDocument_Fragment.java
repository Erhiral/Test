package com.membership.Fragment;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.membership.Activity.HomeActivity;
import com.membership.ApiClient;
import com.membership.ApiInterface;
import com.membership.DataStorage;
import com.membership.R;
import com.membership.Response.DocumentResponse;
import com.membership.Response.ImageUpload;
import com.membership.Response.Register;
import com.membership.ResponseModel.DocumentListResponse;
import com.membership.Utils.CheckNetwork;
import com.membership.Utils.Const;
import com.membership.Utils.CustomHeaderWithRelative;
import com.membership.Utils.FilePathHelper;
import com.membership.Utils.Prefs;
import com.membership.Utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.github.florent37.runtimepermission.RuntimePermission.askPermission;

@SuppressLint("ValidFragment")
public class AddDocument_Fragment extends Fragment {
    Activity context;
    FragmentManager fragmentManager;
    RelativeLayout upload_btn;
    ApiInterface apiInterface;
    AppCompatDialog progressDialog;
    RelativeLayout header;
    View rootview;
    DataStorage dataStorage;
    DrawerLayout drawer;
    List<DocumentListResponse> documentListResponses= new ArrayList<>();
    TextView SaveData;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private static final int REQUEST_CAMERA_CODE = 100;
    private static final int REQUEST_GALLERY_CODE = 200;
    private static final int REQUEST_FILE_CODE = 300;
    Uri uri;
    String image;
    TextInputEditText file_name;
    EditText doc_description;
    TextView uploadtext;
    public AddDocument_Fragment(Activity context, FragmentManager fragmentManager, RelativeLayout header, DrawerLayout drawer){
        this.context=context;
        this.fragmentManager=fragmentManager;
        this.header=header;
        this.drawer=drawer;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootview = inflater.inflate(R.layout.document_add_fragment, container, false);
        CustomHeaderWithRelative.setInnerToolbar(getActivity(), header, " Add Documents");
        SaveData = (TextView) header.findViewById(R.id.text);
        SaveData.setText(R.string.Save);
        SaveData.setVisibility(View.VISIBLE);
        progressDialog = new AppCompatDialog(getContext());
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.progress_dialog);
        file_name=rootview.findViewById(R.id.file_name);
        doc_description=rootview.findViewById(R.id.doc_description);
        uploadtext=rootview.findViewById(R.id.uploadtext);
        upload_btn=rootview.findViewById(R.id.upload_btn);
        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23) {
                    Utils.getInstance().d("In check permission");
                    CheckPermission();
                } else {
                    Utils.getInstance().d("In image upload");
                    if (CheckNetwork.isInternetAvailable(getContext())) {
                        SelectImage();
                    } else
                    {
                        Toast.makeText(getContext(),"No Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        SaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name= file_name.getText().toString();
                String des=doc_description.getText().toString();
                if(!name.isEmpty()&&!des.isEmpty()&&image!=null){
                    if(CheckNetwork.isInternetAvailable(context)){
                      SaveDocument(name,des,image);
                    }else {
                        Toast.makeText(context,"No Internet Connection",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if(name.isEmpty()){
                        file_name.setError("Please Enter Your FileName");
                        file_name.requestFocus();
                    }else if(des.isEmpty()){
                        doc_description.setError("Please Enter Your Description");
                        doc_description.requestFocus();
                    }else if(image==null){
                        Toast.makeText(getContext(),"Please Uplaod Your data",Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        return rootview;
    }
    private void CheckPermission(){
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.CAMERA))
            permissionsNeeded.add("Camera Access");
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Access Storage");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Modify Storage");
        if (permissionsList.size() > 0){
            askpermissionthis();
//            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
//                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }
        SelectImage();

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
                if (!shouldShowRequestPermissionRationale(permission))
                    return false;
            }
        }
        return true;
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
//                Map<String, Integer> perms = new HashMap<String, Integer>();
//                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
//                for (int i = 0; i < permissions.length; i++)
//                    perms.put(permissions[i], grantResults[i]);
//                if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//                    SelectImage();
//                } else {
//                    Toast.makeText(getContext(), "Some Permission is Denied", Toast.LENGTH_SHORT).show();
//                }
//            }
//            break;
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }
    public void SelectImage(){
        final CharSequence[] options = { "Capture from Camera","Choose from Gallery","File Manger","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if(options[item].equals("Capture from Camera")){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    startActivityForResult(intent, REQUEST_CAMERA_CODE);
                }
                if (options[item].equals("Choose from Gallery")){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_GALLERY_CODE);
                }
                if(options[item].equals("File Manger")){
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    intent.setType("file/*");
                    startActivityForResult(intent,REQUEST_FILE_CODE);
                }
                else if (options[item].equals("Cancel")){
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Log.d("mytag","uricropping "+result.getUri());
                if(CheckNetwork.isInternetAvailable(context)){

                    ApiImage(new File(resultUri.getPath()), resultUri);
                }else {
                    Toast.makeText(context,"No Internet Connection",Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }if(resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA_CODE) {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                Log.d("mytag", "f"+f.getAbsoluteFile()+" "+f.getParentFile());
                for (File temp : f.listFiles()) {
                    if(temp.getName().equals("temp.jpg")){
                        f = temp;
                        Log.d("mytag", "f=temp");
                        break;
                    }
                }
                CropImage.activity(Uri.fromFile(f)).start(getContext(), this);
            } else if (requestCode == REQUEST_GALLERY_CODE) {
                uri = data.getData();
                String filePath = getRealPathFromURIPath(uri, getContext());
                File file = new File(filePath);
                Log.d("mytag", "Filename " + file.getName());
                CropImage.activity(uri).start(getContext(), this);
//                uploadimagetoserver(file, uri);
            }else if (requestCode == REQUEST_FILE_CODE) {
                uri = data.getData();
                Log.d("mytag","uri filemanger "+uri);
//                String filePath = getRealPathFromURIPathPdf(uri, getContext());
                FilePathHelper filePathHelper = new FilePathHelper();
                String path = "";
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    if (filePathHelper.getPathnew(uri, getContext()) != null) {
                        path = filePathHelper.getPathnew(uri, getContext()).toLowerCase();
                        Log.d("mytag","path Kitket 1"+ path);
                    } else {
//                        path = filePathHelper.getPathnew(uri, getContext()).toLowerCase();
                        path = filePathHelper.getFilePathFromURI(uri, getContext()).toLowerCase();
                        Log.d("mytag","path Kitket else"+ path);
                    }
                } else {
                    path = filePathHelper.getPath(uri, getContext()).toLowerCase();
                    Log.d("mytag","path normal  else"+ path);
                }

                File file = new File(path);

                if(CheckNetwork.isInternetAvailable(context)){
                    ApiImage(file, uri);
                }else {
                    Toast.makeText(context,"No Internet Connection",Toast.LENGTH_SHORT).show();
                }
                Log.d("mytag", "Filename filemanger  " + file.getName()+"file" + file+"filePath"+file.getAbsolutePath());
//                CropImage.activity(uri).start(getContext(), this);
//                uploadimagetoserver(file, uri);
            }
        }
    }

    private void ApiImage(File file, final Uri mImageCaptureUri) {
        progressDialog.show();
        uploadtext.setText("Uploading File.....");
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        RequestBody reqFile = RequestBody.create(MediaType.parse("*/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);
        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload_test");
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ImageUpload> req = apiInterface.uplaod_image(body);
        req.enqueue(new Callback<ImageUpload>() {
            @Override
            public void onResponse(Call<ImageUpload> call, Response<ImageUpload> response) {
                progressDialog.dismiss();

                if(response.body().getStatus()==1){
                    Log.d("mytag",response.body().getMsg());
                    uploadtext.setText("Your File Uploaded successfully.");
//                    try {
//                        profile_image.setImageBitmap(MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), mImageCaptureUri));
////                            Prefs.getPrefInstance().setValue(activity, Const.PROFILE_IMAGE,profileImage);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    image=response.body().getData().toString();

                    Log.d("myatg","Document server "+image);
//                    Toast.makeText(context,response.body().getData().toString(),Toast.LENGTH_SHORT).show();
                }else {
                    Log.d("mytag",response.body().getMsg());
//                    profile_image.setImageResource(R.drawable.logo_my_nation);
                    Toast.makeText(getContext(),response.body().getMsg().toString(),Toast.LENGTH_SHORT).show();
                }
                // Do Something
            }
            @Override
            public void onFailure(Call<ImageUpload> call, Throwable t){
                t.printStackTrace();
                Log.d("mytag", "Filemanger Fail");
            }
        });
    }
    private String getRealPathFromURIPath(Uri contentURI, Context activity) {
        Cursor cursor = ((HomeActivity)activity).getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    private String getRealPathFromURIPathPdf(Uri contentURI, Context activity) {
//        Cursor cursor = ((HomeActivity)activity).getContentResolver().query(contentURI, null, null, null, null);
//        if (cursor == null) {
//            return contentURI.getPath();
//        } else {
//            cursor.moveToFirst();
//            int idx = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
////            int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
//            return cursor.getString(idx);
//        }
        String[] projection = { MediaStore.Files.FileColumns.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = ((HomeActivity)context).managedQuery(contentURI, projection, null, null, null);

        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return contentURI.getPath();
    }



    public  void SaveDocument(String name,String des,String url){
        progressDialog.show();
        documentListResponses= new ArrayList<>();
        ApiInterface  apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Register> registerCall=apiInterface.SaveDocument(Integer.parseInt(String.valueOf(DataStorage.read("id",DataStorage.INTEGER))),des,name,url);
        registerCall.enqueue(new Callback<Register>() {
            @Override
            public void onResponse(Call<Register> call, Response<Register> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    int status = response.body().getStatus();

                    if(status==1){
                        new AlertDialog.Builder(getContext())
                                .setCancelable(false)
                                .setMessage(response.body().getMsg())
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        getActivity().onBackPressed();
//                                                        finish();
                                    }
                                })
//
                                .show();

                    }else {
                        new AlertDialog.Builder(getContext())
                                .setCancelable(false)
                                .setMessage(response.body().getMsg())
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
//                                                        finish();
                                    }
                                })
//
                                .show();
                    }

                }
            }
            @Override
            public void onFailure(Call<Register> call, Throwable t){
                if(progressDialog!=null){
                    progressDialog.dismiss();
                }
            }
        });

    }



    public void pushFragment(Fragment frag, String tag, Boolean addtobackstack) {
//            FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.rl_content, frag, tag);
        if (addtobackstack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }


    public void askpermissionthis(){
        // if (f instanceof Location_Fragment) {
        askPermission(this,Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onAccepted((result) -> {
                    //all permissions already granted or just granted
                    for (int i = 0; i <result.getAccepted().size() ; i++) {
                        Log.d("thistag", "Accepted : " + result.getAccepted().get(i));
                    }
                    SelectImage();

                    Integer  permi= Integer.parseInt(Prefs.getPrefInstance().getValue(getContext(), Const.Status,""));


                })
                .onDenied((result) -> {
                    //the list of denied permissions
                    for (String permission : result.getDenied()) {
                        Log.d("thistag", "Denied : " + permission);

                    }
                    //permission denied, but you can ask again, eg:

                    new AlertDialog.Builder(getContext())
                            .setCancelable(false)
                            .setMessage("We need to access your location services in order to show you other members on the map. Please allow the same.")
                            .setPositiveButton("yes", (dialog, which) -> {
                                result.askAgain();
                            }) // ask again
                            .show();

                })
                .onForeverDenied((result) -> {
                    //the list of forever denied permissions, user has check 'never ask again'
                    for (String permission : result.getForeverDenied()) {
                        Log.d("thistag", "Forever Denied : " + permission);
                    }

                    new AlertDialog.Builder(getContext())
                            .setCancelable(false)
                            .setMessage("We need to access your location services in order to show you other members on the map. Please allow the same.")
                            .setPositiveButton("yes", (dialog, which) -> {
                                result.goToSettings();
                            }) // ask again

                            .show();

                    // you need to open setting manually if you really need it



                })
                .ask();
    }
}
