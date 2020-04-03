package com.luck.picture.lib.widget.instagram;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.ScreenUtils;
import com.luck.picture.lib.tools.SdkVersionUtils;
import com.yalantis.ucrop.util.FileUtils;
import com.yalantis.ucrop.util.MimeType;
import com.yalantis.ucrop.view.GestureCropImageView;
import com.yalantis.ucrop.view.OverlayView;
import com.yalantis.ucrop.view.TransformImageView;
import com.yalantis.ucrop.view.UCropView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ================================================
 * Created by JessYan on 2020/3/30 16:33
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class PreviewContainer extends FrameLayout {
    public static final int PLAY_IMAGE_MODE = 0;
    public static final int PLAY_VIDEO_MODE = 1;
    private UCropView mUCropView;
    private VideoView mVideoView;
    private GestureCropImageView mGestureCropImageView;
    private OverlayView mOverlayView;
    private ImageView mRatioView;
    private ImageView mMultiView;
    private boolean mCropGridShowing;
    private Handler mHandler;
    private boolean isAspectRatio;
    private boolean isMulti;
    private onSelectionModeChangedListener mListener;
    private int PlayMode;

    private TransformImageView.TransformImageListener mImageListener = new TransformImageView.TransformImageListener() {
        @Override
        public void onRotate(float currentAngle) {
        }

        @Override
        public void onScale(float currentScale) {

        }

        @Override
        public void onBitmapLoadComplete(@NonNull Bitmap bitmap) {
            resetAspectRatio();
        }

        @Override
        public void onLoadComplete() {

        }

        @Override
        public void onLoadFailure(@NonNull Exception e) {
        }

    };

    public PreviewContainer(@NonNull Context context) {
        super(context);
        mHandler = new Handler(context.getMainLooper());

        mVideoView = new VideoView(context);
        addView(mVideoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mVideoView.setVisibility(View.GONE);
        mVideoView.setOnClickListener((v -> {
            if (PlayMode != PLAY_VIDEO_MODE) {
                return;
            }
        }));
        mVideoView.setOnPreparedListener(mp -> {
            mp.setOnVideoSizeChangedListener((mp1, width, height) -> {

            });
        });

        mUCropView = new UCropView(getContext(), null);
        mGestureCropImageView = mUCropView.getCropImageView();
        mOverlayView = mUCropView.getOverlayView();

        mGestureCropImageView.setPadding(0, 0, 0, 0);
        mGestureCropImageView.setTargetAspectRatio(1.0f);
        mGestureCropImageView.setRotateEnabled(false);
        mGestureCropImageView.setTransformImageListener(mImageListener);
        mGestureCropImageView.setMaxScaleMultiplier(15.0f);

        mOverlayView.setPadding(0, 0, 0, 0);
        mOverlayView.setShowCropGrid(false);

        mGestureCropImageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!mCropGridShowing) {
                    mOverlayView.setShowCropGrid(true);
                    mOverlayView.invalidate();
                    mCropGridShowing = true;
                } else {
                    mHandler.removeCallbacksAndMessages(null);
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mHandler.postDelayed(() -> {
                    if (mCropGridShowing) {
                        mOverlayView.setShowCropGrid(false);
                        mOverlayView.invalidate();
                        mCropGridShowing = false;
                    }
                }, 800);
            }
            return false;
        });

        addView(mUCropView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        mRatioView = new ImageView(context);

        CombinedDrawable ratiodDrawable = new CombinedDrawable(DrawableUtils.createSimpleSelectorCircleDrawable(ScreenUtils.dip2px(context, 30), 0x88000000, Color.BLACK),
                context.getResources().getDrawable(R.drawable.discover_telescopic).mutate());
        ratiodDrawable.setCustomSize(ScreenUtils.dip2px(context, 30), ScreenUtils.dip2px(context, 30));

        mRatioView.setImageDrawable(ratiodDrawable);
        FrameLayout.LayoutParams ratioLayoutParams = new LayoutParams(ScreenUtils.dip2px(context, 30), ScreenUtils.dip2px(context, 30), Gravity.BOTTOM | Gravity.LEFT);
        ratioLayoutParams.leftMargin = ScreenUtils.dip2px(context, 15);
        ratioLayoutParams.bottomMargin = ScreenUtils.dip2px(context, 12);
        addView(mRatioView, ratioLayoutParams);
        mRatioView.setOnClickListener((v) -> {
            if (PlayMode != PLAY_IMAGE_MODE) {
                return;
            }
            isAspectRatio = !isAspectRatio;
            resetAspectRatio();
        });

        mMultiView = new ImageView(context);

        CombinedDrawable multiDrawable = new CombinedDrawable(DrawableUtils.createSimpleSelectorCircleDrawable(ScreenUtils.dip2px(context, 30), 0x88000000, Color.BLACK),
                context.getResources().getDrawable(R.drawable.discover_many).mutate());
        multiDrawable.setCustomSize(ScreenUtils.dip2px(context, 30), ScreenUtils.dip2px(context, 30));

        mMultiView.setImageDrawable(multiDrawable);
        FrameLayout.LayoutParams multiLayoutParams = new LayoutParams(ScreenUtils.dip2px(context, 30), ScreenUtils.dip2px(context, 30), Gravity.BOTTOM | Gravity.RIGHT);
        multiLayoutParams.rightMargin = ScreenUtils.dip2px(context, 15);
        multiLayoutParams.bottomMargin = ScreenUtils.dip2px(context, 12);
        addView(mMultiView, multiLayoutParams);
        mMultiView.setOnClickListener(v -> {
            isMulti = !isMulti;
            if (isMulti) {
                mRatioView.setVisibility(View.GONE);
            } else {
                mRatioView.setVisibility(View.VISIBLE);
            }
            if (mListener != null) {
                mListener.onModeChange(isMulti);
            }
        });

        View divider = new View(getContext());
        divider.setBackgroundColor(Color.WHITE);
        FrameLayout.LayoutParams dividerParms = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(getContext(), 2), Gravity.BOTTOM);
        addView(divider, dividerParms);
    }

    private void resetAspectRatio() {
        mGestureCropImageView.setTargetAspectRatio(isAspectRatio ? 0 : 1.0f);
        mGestureCropImageView.onImageLaidOut();
    }

    public void setImageUri(@NonNull Uri inputUri, @Nullable Uri outputUri) {
        if (PlayMode != PLAY_IMAGE_MODE) {
            return;
        }
        if (inputUri != null && outputUri != null) {
            try {
                boolean isOnTouch = isOnTouch(inputUri);
                mGestureCropImageView.setScaleEnabled(isOnTouch ? true : isOnTouch);
                mGestureCropImageView.setImageUri(inputUri, outputUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void playVideo(LocalMedia media) {
        if (PlayMode != PLAY_VIDEO_MODE) {
            return;
        }
        if (SdkVersionUtils.checkedAndroid_Q() && PictureMimeType.isContent(media.getPath())) {
            mVideoView.setVideoURI(Uri.parse(media.getPath()));
        } else {
            mVideoView.setVideoPath(media.getPath());
        }
        mVideoView.start();
    }

    public void checkModel(int mode) {
        PlayMode = mode;

        AnimatorSet set = new AnimatorSet();
        List<Animator> animators = new ArrayList<>();
        if (mode == PLAY_IMAGE_MODE) {
            setViewVisibility(mUCropView, View.VISIBLE);
            setViewVisibility(mVideoView, View.GONE);
            animators.add(ObjectAnimator.ofFloat(mUCropView, "alpha", 0.1f,1.0f));
        } else if (mode == PLAY_VIDEO_MODE) {
            setViewVisibility(mVideoView, View.VISIBLE);
            setViewVisibility(mUCropView, View.GONE);
            animators.add(ObjectAnimator.ofFloat(mVideoView, "alpha", 0f, 1.0f));
        }
        set.setDuration(800);
        set.playTogether(animators);
        set.start();
    }

    private void setViewVisibility(View view, int visibility) {
        if (view != null) {
            if (view.getVisibility() != visibility) {
                view.setVisibility(visibility);
            }
        }
    }

    /**
     * 是否可以触摸
     *
     * @param inputUri
     * @return
     */
    private boolean isOnTouch(Uri inputUri) {
        if (inputUri == null) {
            return true;
        }
        boolean isHttp = MimeType.isHttp(inputUri.toString());
        if (isHttp) {
            // 网络图片
            String lastImgType = MimeType.getLastImgType(inputUri.toString());
            return !MimeType.isGifForSuffix(lastImgType);
        } else {
            String mimeType = MimeType.getMimeTypeFromMediaContentUri(getContext(), inputUri);
            if (mimeType.endsWith("image/*")) {
                String path = FileUtils.getPath(getContext(), inputUri);
                mimeType = MimeType.getImageMimeType(path);
            }
            return !MimeType.isGif(mimeType);
        }
    }

    public void setListener(onSelectionModeChangedListener listener) {
        mListener = listener;
    }

    public interface onSelectionModeChangedListener {
        void onModeChange(boolean isMulti);
    }
}