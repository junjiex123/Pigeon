package com.monsterlin.pigeon.ui.family;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.monsterlin.pigeon.R;
import com.monsterlin.pigeon.adapter.FamilyNumberAdapter;
import com.monsterlin.pigeon.base.BaseActivity;
import com.monsterlin.pigeon.bean.Family;
import com.monsterlin.pigeon.bean.User;
import com.monsterlin.pigeon.utils.ToastUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author : monsterLin
 * @version : 1.0
 * @email : monster941025@gmail.com
 * @github : https://github.com/monsterLin
 * @time : 2017/8/5
 * @desc : 我的家庭
 */
public class MyFamilyActivity extends BaseActivity {

    private Toolbar mToolBar;
    private RecyclerView mRvFamily;
    private FamilyNumberAdapter familyNumberAdapter;
    private User mCurrentUser;
    private BmobQuery<User> queryFamilyNumber;
    private BmobQuery<Family> queryFamilyInfo;

    private TextView mTvFamilyName, mTvCreator, mTvCreateTime;
    private CircleImageView mFamilyCivPhoto ;

    @Override
    public int getLayoutId() {
        return R.layout.activity_family;
    }

    @Override
    public void initViews() {
        mToolBar = findView(R.id.common_toolbar);
        initToolBar(mToolBar, "我的家庭", true);
        mRvFamily = findView(R.id.family_rv_numbers);
        mTvFamilyName = findView(R.id.family_tv_name);
        mTvCreator = findView(R.id.family_tv_creator);
        mTvCreateTime = findView(R.id.family_tv_createTime);
        mFamilyCivPhoto=findView(R.id.family_civ_userPhoto);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        dialog.showDialog();
        mCurrentUser = BmobUser.getCurrentUser(User.class);
        queryFamilyInfo = new BmobQuery<>();
        queryFamilyInfo.include("familyCreator");
        queryFamilyInfo.getObject(mCurrentUser.getFamily().getObjectId(), new QueryListener<Family>() {
            @Override
            public void done(Family family, BmobException e) {
                if (e == null) {
                    if (family != null) {

                        BmobFile photoFile = family.getFamilyIcon();
                        if (photoFile!=null){
                            Picasso.with(MyFamilyActivity.this).load(photoFile.getFileUrl()).into(mFamilyCivPhoto);
                        }else {
                            mFamilyCivPhoto.setImageResource(R.drawable.ic_default);
                        }

                        mTvFamilyName.setText("家庭名：" + family.getFamilyName());

                        if (!TextUtils.isEmpty(family.getFamilyCreator().getNick())){
                            mTvCreator.setText(family.getFamilyCreator().getNick());
                        }

                        mTvCreateTime.setText("创建时间：" + family.getCreatedAt());
                    }
                } else {
                    dialog.dismissDialog();
                    ToastUtils.showToast(MyFamilyActivity.this, "GetCurrentFamily："+e.getMessage());
                }
            }
        });

        queryFamilyNumber = new BmobQuery<>();
        queryFamilyNumber.addWhereEqualTo("family", mCurrentUser.getFamily());
        queryFamilyNumber.include("family");
        queryFamilyNumber.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    if (list.size() != 0) {
                        familyNumberAdapter = new FamilyNumberAdapter(MyFamilyActivity.this, list);
                        mRvFamily.setAdapter(familyNumberAdapter);
                        mRvFamily.setLayoutManager(new LinearLayoutManager(MyFamilyActivity.this, LinearLayoutManager.VERTICAL, false));
                        dialog.dismissDialog();
                    }
                } else {
                    dialog.dismissDialog();
                    ToastUtils.showToast(MyFamilyActivity.this, "QueryNumber："+e.getMessage());
                }
            }
        });

    }

    @Override
    public void processClick(View v) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_family_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit:
                //判断是否为创建者
                queryFamilyInfo = new BmobQuery<>();
                queryFamilyInfo.include("familyCreator");
                queryFamilyInfo.getObject(mCurrentUser.getFamily().getObjectId(), new QueryListener<Family>() {

                    @Override
                    public void done(Family family, BmobException e) {
                        if (e == null) {
                            String currentFamilyCreatorId = family.getFamilyCreator().getObjectId();
                            if (currentFamilyCreatorId.equals(mCurrentUser.getObjectId())) {
                                startActivity(new Intent(MyFamilyActivity.this, EditFamilyActivity.class));
                            } else {
                                ToastUtils.showToast(MyFamilyActivity.this, "抱歉，你不是创建者，无法进行编辑！");
                            }
                        }else {
                            ToastUtils.showToast(MyFamilyActivity.this, "isCreator ："+e.getMessage());
                        }


                    }
                });

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();

    }
}
