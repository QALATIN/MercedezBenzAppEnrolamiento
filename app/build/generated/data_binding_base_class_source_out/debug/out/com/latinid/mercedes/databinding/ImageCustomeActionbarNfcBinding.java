// Generated by view binder compiler. Do not edit!
package com.latinid.mercedes.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import com.latinid.mercedes.R;
import java.lang.NullPointerException;
import java.lang.Override;

public final class ImageCustomeActionbarNfcBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  private ImageCustomeActionbarNfcBinding(@NonNull RelativeLayout rootView) {
    this.rootView = rootView;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ImageCustomeActionbarNfcBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ImageCustomeActionbarNfcBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.image_custome_actionbar_nfc, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ImageCustomeActionbarNfcBinding bind(@NonNull View rootView) {
    if (rootView == null) {
      throw new NullPointerException("rootView");
    }

    return new ImageCustomeActionbarNfcBinding((RelativeLayout) rootView);
  }
}