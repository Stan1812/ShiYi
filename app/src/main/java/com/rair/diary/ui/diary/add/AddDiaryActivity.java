package com.rair.diary.ui.diary.add;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rair.diary.R;
import com.rair.diary.base.RairApp;
import com.rair.diary.bean.Diary;
import com.rair.diary.bean.DiaryBean;
import com.rair.diary.constant.Constants;
import com.rair.diary.db.DiaryDao;
import com.rair.diary.ui.diary.detail.DiaryDetailActivity;
import com.rair.diary.utils.CommonUtils;
import com.rair.diary.utils.HttpUtils;
import com.rair.diary.utils.SPUtils;
import com.rair.diary.view.LinedEditText;
import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.cos.xml.transfer.TransferStateListener;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class AddDiaryActivity extends AppCompatActivity {

    @BindView(R.id.add_et_title)
    EditText addEtTitle;
    @BindView(R.id.add_et_content)
    LinedEditText addEtContent;
    @BindView(R.id.add_iv_back)
    ImageView addIvBack;
    @BindView(R.id.add_iv_save)
    ImageView addIvSave;
    @BindView(R.id.add_tv_title)
    TextView addTvTitle;
    @BindView(R.id.add_iv_qing)
    ImageView addIvQing;
    @BindView(R.id.add_iv_yin)
    ImageView addIvYin;
    @BindView(R.id.add_iv_yu)
    ImageView addIvYu;
    @BindView(R.id.add_iv_leiyu)
    ImageView addIvLeiyu;
    @BindView(R.id.add_iv_xue)
    ImageView addIvXue;
    @BindView(R.id.add_ll_weather)
    LinearLayout addLlWeather;
    @BindView(R.id.add_iv_photo)
    ImageView addIvPhoto;
    @BindView(R.id.add_iv_weather)
    ImageView addIvWeather;
    @BindView(R.id.add_iv_show)
    ImageView addIvShow;
    private Unbinder unbinder;
    public static String[] WEEK = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
    public static final int WEEKDAYS = 7;
    private String mDate;
    private String mWeek;
    private String weather;
    private String image;
    private  String  cosImagePath;
    private SPUtils spUtils;
    private  boolean hasUploadSuccess;
    private  boolean isDiaryAddSuccess;
    private  DiaryBean diary;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diary);
        unbinder = ButterKnife.bind(this);
        intView();
    }

    private void intView() {
        hasUploadSuccess = false;
        isDiaryAddSuccess = false;
        diary = new DiaryBean();
        spUtils = RairApp.getRairApp().getSpUtils();
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        mDate = dateFormat.format(date);
        mWeek = DateToWeek(date);
        addTvTitle.setText(String.format(Constants.FORMAT, mDate, mWeek, ""));
    }
//    public  void setResult(){
//
//    }
    public String DateToWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayIndex = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayIndex < 1 || dayIndex > WEEKDAYS) {
            return null;
        }
        return WEEK[dayIndex - 1];
    }

    public  void setIntentResult() {
        Intent intent = new Intent();
        intent.putExtra("diary",new Gson().toJson(diary));
        if(this.isDiaryAddSuccess){
            this.setResult(Constants.ADD_NEW_DIARY_SUCCESS,intent);
        }
    }
    @OnClick({R.id.add_iv_back, R.id.add_iv_save, R.id.add_iv_qing, R.id.add_iv_yin,
            R.id.add_iv_yu, R.id.add_iv_leiyu, R.id.add_iv_xue, R.id.add_iv_photo, R.id.add_iv_weather})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.add_iv_back:
                this.finish();
                break;
            case R.id.add_iv_save:
                doSave();
                break;
            case R.id.add_iv_qing:
                addLlWeather.setVisibility(View.GONE);
                addIvWeather.setImageResource(R.mipmap.ic_weather_qing);
                weather = "晴";
                break;
            case R.id.add_iv_yin:
                addLlWeather.setVisibility(View.GONE);
                addIvWeather.setImageResource(R.mipmap.ic_weather_qing);
                weather = "阴";
                break;
            case R.id.add_iv_yu:
                addLlWeather.setVisibility(View.GONE);
                addIvWeather.setImageResource(R.mipmap.ic_weather_yu);
                weather = "雨";
                break;
            case R.id.add_iv_leiyu:
                addLlWeather.setVisibility(View.GONE);
                addIvWeather.setImageResource(R.mipmap.ic_weather_leiyu);
                weather = "雷雨";
                break;
            case R.id.add_iv_xue:
                addLlWeather.setVisibility(View.GONE);
                addIvWeather.setImageResource(R.mipmap.ic_weather_xue);
                weather = "雪";
                break;
            case R.id.add_iv_photo:
                addLlWeather.setVisibility(View.GONE);
                checkSelfPermission();
                break;
            case R.id.add_iv_weather:
                addLlWeather.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void doSave() {
        String title = addEtTitle.getText().toString().trim();
        String content = addEtContent.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            title = "无题";
        }
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "内容不能为空哦，写点东西吧~", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(weather)) {
            weather = "晴";
        }
        if (TextUtils.isEmpty(image)) {
            image = "";
        }
        if (TextUtils.isEmpty(cosImagePath)){
            cosImagePath = "";
        }
        diary.setDate(mDate);
        diary.setWeek(mWeek);
        diary.setWeather(weather);
        diary.setTitle(title);
        diary.setContent(content);
        diary.setImage(cosImagePath);
//        如果用户已经登录，保存到线上数据库，并且设置
        if (spUtils.getBoolean("hasLogin", false)) {
            saveDiary2Server(diary);
        } else {
            DiaryDao diaryDao = new DiaryDao(this);
            diaryDao.insert(diary);
            this.finish();
        }
        CommonUtils.hideInput(this);
        CommonUtils.showSnackar(addEtContent, "保存成功");
    }

    private void saveDiary2Server(DiaryBean diary) {
        Gson gson = new Gson();
        String diaryJson = gson.toJson(diary);
        addNewDiary2Server(diaryJson);
    }

    private void formatString2Res(String response) {
        JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
        String status = jsonObject.get("status").toString();
        if (status.equals("0")) {
            JsonObject idInfo = jsonObject.get("data").getAsJsonObject();
            int diaryID = idInfo.get("id").getAsInt();
            diary.setId(diaryID);
            isDiaryAddSuccess = true;
            this.setIntentResult();
            Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
            this.finish();
        } else {
            Toast.makeText(this, "发布失败", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void addNewDiary2Server(String postData) {
        String RequestURL = "http://119.29.235.55:8000/api/v1/articles";
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String s = HttpUtils.PostHttp(RequestURL, postData);
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                if (s != null && !s.isEmpty()) {
                    formatString2Res(s);
                } else {
//                    Toast.makeText(, "发布失败", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    /**
     * 检查权限
     */
    private void checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                CommonUtils.showSnackar(addIvPhoto, "需要读写权限");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        } else {
            if (image == null) {
                MultiImageSelector.create()
                        .showCamera(true)
                        .single()
                        .start(this, 0);
            } else {
                CommonUtils.showSnackar(addIvPhoto, "你已经选择了一张图片");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MultiImageSelector.create()
                            .showCamera(false) // 是否显示相机. 默认为显示
                            .single() // 单选模式
                            .multi() // 多选模式, 默认模式;
                            .start(this, 0);
                } else {
                    CommonUtils.showSnackar(addIvPhoto, "没有授予读写权限，导出失败,请到设置中手动打开");
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (requestCode == 0) {
            List<String> selectPaths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            if (selectPaths.size() != 0) {
                String imagePath = selectPaths.get(0);
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                addIvShow.setVisibility(View.VISIBLE);
                addIvShow.setImageBitmap(bitmap);
                image = imagePath;
                initCosXmlService();
                System.out.println("IMAGE==========" + imagePath + "==== " + image);
            } else {
                addIvShow.setVisibility(View.GONE);
            }
        }
    }
    private void initCosXmlService(){
        String region = "ap-beijing";
        // 创建 CosXmlServiceConfig 对象，根据需要修改默认的配置参数
        CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                .setRegion(region)
                .builder();
        String secretId = "AKIDTp0zqmp6TWxIgyK7B7s6EQeIo8LQqATf"; //永久密钥 secretId
        String secretKey = "APjuvkNoRyKVUIuY4Pkutlx2ylMlO2aw"; //永久密钥 secretKey
        /**
         * 初始化 {@link QCloudCredentialProvider} 对象，来给 SDK 提供临时密钥
         * @parma secretId 永久密钥 secretId
         * @param secretKey 永久密钥 secretKey
         * @param keyDuration 密钥有效期，单位为秒
         */
        QCloudCredentialProvider credentialProvider = new ShortTimeCredentialProvider(secretId, secretKey, 300);
        CosXmlService cosXmlService = new CosXmlService(this, serviceConfig, credentialProvider);
        System.out.println("START========");
        uploadImage(cosXmlService);
    }
    private  void uploadImage( CosXmlService cosXmlService){
        // 初始化 TransferConfig
        TransferConfig transferConfig = new TransferConfig.Builder().build();
        /*若有特殊要求，则可以如下进行初始化定制。例如限定当对象 >= 2M 时，启用分块上传，且分块上传的分块大小为1M，当源对象大于5M时启用分块复制，且分块复制的大小为5M。*/
        transferConfig = new TransferConfig.Builder()
                .setDividsionForCopy(5 * 1024 * 1024) // 是否启用分块复制的最小对象大小
                .setSliceSizeForCopy(5 * 1024 * 1024) // 分块复制时的分块大小
                .setDivisionForUpload(2 * 1024 * 1024) // 是否启用分块上传的最小对象大小
                .setSliceSizeForUpload(1024 * 1024) // 分块上传时的分块大小
                .build();
        TransferManager transferManager = new TransferManager(cosXmlService, transferConfig);
        String extension = "";
        String cosPath = "";
        int i = image.lastIndexOf('.');
        if (i > 0) {
            extension = image.substring(i+1);
            cosPath =  "image/"+new Date().getTime() + "." + extension ; //对象在存储桶中的位置标识符，即称对象键
        }else{
            cosPath = image;
        }
        String bucket = "shiji-1253438335"; //存储桶，格式：BucketName-APPID
        String srcPath = new File(this.getExternalCacheDir(), image).toString(); //本地文件的绝对路径
        String uploadId = null; //若存在初始化分块上传的 UploadId，则赋值对应的 uploadId 值用于续传；否则，赋值 null
        COSXMLUploadTask cosxmlUploadTask = transferManager.upload(bucket, cosPath, image, uploadId);
        System.out.println("START========"+ cosxmlUploadTask);

//设置上传进度回调
        cosxmlUploadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                // todo Do something to update progress...
                System.out.println(complete+ target);
            }
        });
//设置返回结果回调
        cosxmlUploadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                COSXMLUploadTask.COSXMLUploadTaskResult cOSXMLUploadTaskResult = (COSXMLUploadTask.COSXMLUploadTaskResult) result;
                System.out.println("SUCCESS"+result.accessUrl+ cOSXMLUploadTaskResult);
//                Toast.makeText()
                cosImagePath =result.accessUrl;
                hasUploadSuccess = true;
            }
            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                // todo Upload failed because of CosXmlClientException or CosXmlServiceException...
                System.out.println("FAILED"+  exception + serviceException);
                hasUploadSuccess = false;
            }
        });
//设置任务状态回调, 可以查看任务过程
        cosxmlUploadTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(TransferState state) {
                // todo notify transfer state
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

}
