package cn.jcyh.peephole.ui.dialog;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.StringSignature;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jcyh.peephole.R;
import cn.jcyh.peephole.constant.Constant;
import cn.jcyh.peephole.utils.ScreenUtil;

/**
 * Created by jogger on 2018/11/14.
 */
public class ADPopupDialog extends BaseDialogFragment {
    @BindView(R.id.iv_ad)
    ImageView ivAd;

    @Override
    int getLayoutId() {
        return R.layout.dialog_ad_popup;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        Bundle arguments = getArguments();
        String url = arguments.getString(Constant.URL);
        if (TextUtils.isEmpty(url)) return;
        loadImg(url);
    }

    private void loadImg(String url) {
        Glide.with(this)
                .load(url)
                .asBitmap()
                .signature(new StringSignature(System.currentTimeMillis() + ""))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        int width = resource.getWidth();
                        int height = resource.getHeight();
                        try {
                            Bitmap bitmap;
                            Matrix m = new Matrix();
                            m.postScale((float) ScreenUtil.dip2px(263) / height,
                                    (float) ScreenUtil.dip2px(263) / height);
                            if (width > height) {
                                bitmap = Bitmap.createBitmap(resource, 0, 0, width, height, m, true);
                            } else {
                                bitmap = Bitmap.createBitmap(resource, 0, 0, width, height, m, true);
                            }
                            ivAd.setImageBitmap(bitmap);
                        } catch (Exception ignore) {
                            try {
                                Bitmap bitmap = Bitmap.createBitmap(resource, 0, 0, ScreenUtil.dip2px(263) * width / height, ScreenUtil.dip2px(263));
                                ivAd.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
    }

    public void updateAD(String url) {
        if (ivAd == null) return;
        loadImg(url);
    }

    @OnClick({R.id.ibtn_close})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibtn_close:
                dismiss();
//                Glide.with(mActivity)
//                        .load("http://www.9cyh.cn/templates/default/images/S70-pro_03.png")
//                        .error(R.mipmap.ic_launcher)
//                .signature(new StringSignature(System.currentTimeMillis() + ""))
//                        .into(ivAd);
                break;
        }
    }
}
