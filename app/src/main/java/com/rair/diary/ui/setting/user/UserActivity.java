package com.rair.diary.ui.setting.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rair.diary.R;
import com.rair.diary.base.RairApp;
import com.rair.diary.bean.User;
import com.rair.diary.utils.CommonUtils;
import com.rair.diary.utils.SPUtils;
import com.rair.diary.view.CircleImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class UserActivity extends AppCompatActivity {

    @BindView(R.id.user_iv_back)
    ImageView userIvBack;
    @BindView(R.id.user_civ_head)
    CircleImageView userCivHead;
    //    @BindView(R.id.user_rl_head)
//    RelativeLayout userRlHead;
    @BindView(R.id.user_tv_name)
    TextView userTvName;
    @BindView(R.id.user_rl_name)
    RelativeLayout userRlName;
    @BindView(R.id.user_tv_unlogin)
    TextView userTvUnlogin;
    private Unbinder unbinder;
    private SPUtils spUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        unbinder = ButterKnife.bind(this);
        spUtils = RairApp.getRairApp().getSpUtils();
        initView();
    }

    private void initView() {
        boolean hasLogin = spUtils.getBoolean("hasLogin", false);
        if (hasLogin) {
            userTvName.setText(spUtils.getString("current_username"));
        }

    }

    @OnClick({R.id.user_iv_back, R.id.user_civ_head, R.id.user_rl_head, R.id.user_rl_name, R.id.user_tv_unlogin})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.user_iv_back:
                this.finish();
                break;
            case R.id.user_civ_head:
                break;
            case R.id.user_rl_head:
                checkSelfPermission();
                break;
            case R.id.user_tv_unlogin:
                doLoginOut();
                break;
        }
    }

    /**
     * 检查权限
     */
    private void checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                CommonUtils.showSnackar(userRlName, "需要读写权限");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        } else {
            MultiImageSelector.create()
                    .showCamera(true) // 是否显示相机. 默认为显示
                    .single() // 单选模式
                    .multi() // 多选模式, 默认模式;
                    .start(this, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MultiImageSelector.create()
                            .showCamera(true) // 是否显示相机. 默认为显示
                            .single() // 单选模式
                            .multi() // 多选模式, 默认模式;
                            .start(this, 0);
                } else {
                    CommonUtils.showSnackar(userRlName, "没有授予读写权限，导出失败,请到设置中手动打开");
                }
        }
    }

    /**
     * 登出
     */
    private void doLoginOut() {
        spUtils.put("hasLogin", false);
        spUtils.put("current_token", "");
        spUtils.put("current_username", "");
        spUtils.put("current_password", "");
        spUtils.put("needRefresh", true);
        this.finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
//        if (requestCode == 0) {
//            List<String> selectPaths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
//            if (selectPaths.size() != 0) {
//                String imagePath = selectPaths.get(0);
//                final BmobFile headFile = new BmobFile(new File(imagePath));
//                headFile.uploadblock(new UploadFileListener() {
//
//                    @Override
//                    public void done(BmobException e) {
//                        if (e == null) {
////                            User user = BmobUser.getCurrentUser(User.class);
////                            user.setHeadFile(headFile);
////                            Picasso.with(UserActivity.this).load(headFile.getFileUrl()).into(userCivHead);
////                            user.update(new UpdateListener() {
////                                @Override
////                                public void done(BmobException e) {
////                                    if (e == null) {
////                                        CommonUtils.showSnackar(userRlSignature, "头像上传成功");
////                                    } else {
////                                        CommonUtils.showSnackar(userRlSignature, "头像上传失败");
////                                    }
////                                }
////                            });
//                        } else {
//                            CommonUtils.showSnackar(userRlSignature, "头像上传失败");
//                        }
//
//                    }
//
//                    @Override
//                    public void onProgress(Integer value) {
//                    }
//                });
//            }
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

}
