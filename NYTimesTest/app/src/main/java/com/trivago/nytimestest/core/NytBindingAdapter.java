package com.trivago.nytimestest.core;

import android.databinding.BindingAdapter;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.trivago.nytimestest.R;

/**
 * Created by Michael Dontsov on 09.04.2017.
 */

public class NytBindingAdapter {

    @BindingAdapter({"imageUrl"})
    public static void loadImage(final ImageView imageView, String url) {
        if (url == null || !Patterns.WEB_URL.matcher(url).matches()) {
//            imageView.setVisibility(View.GONE);
            imageView.setImageBitmap(null);
            return;
        }

//        imageView.setVisibility(View.VISIBLE);
        int size = imageView.getContext().getResources().getDimensionPixelSize(R.dimen.thumb_size);
        Glide.with(imageView.getContext())
                .load(url)
                .override(size, size)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }
}
