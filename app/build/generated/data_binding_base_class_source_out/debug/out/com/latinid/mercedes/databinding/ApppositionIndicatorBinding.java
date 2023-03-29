// Generated by view binder compiler. Do not edit!
package com.latinid.mercedes.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import com.latinid.mercedes.R;
import java.lang.NullPointerException;
import java.lang.Override;

public final class ApppositionIndicatorBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final LinearLayout apppositionIndicator;

  private ApppositionIndicatorBinding(@NonNull LinearLayout rootView,
      @NonNull LinearLayout apppositionIndicator) {
    this.rootView = rootView;
    this.apppositionIndicator = apppositionIndicator;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ApppositionIndicatorBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ApppositionIndicatorBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.appposition_indicator, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ApppositionIndicatorBinding bind(@NonNull View rootView) {
    if (rootView == null) {
      throw new NullPointerException("rootView");
    }

    LinearLayout apppositionIndicator = (LinearLayout) rootView;

    return new ApppositionIndicatorBinding((LinearLayout) rootView, apppositionIndicator);
  }
}
