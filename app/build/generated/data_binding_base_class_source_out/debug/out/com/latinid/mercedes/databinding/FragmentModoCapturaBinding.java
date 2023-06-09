// Generated by view binder compiler. Do not edit!
package com.latinid.mercedes.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.button.MaterialButton;
import com.latinid.mercedes.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentModoCapturaBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final MaterialButton adjuntar;

  @NonNull
  public final MaterialButton camara;

  @NonNull
  public final ImageView cerrar;

  @NonNull
  public final TextView subtitulo;

  @NonNull
  public final TextView titulo;

  private FragmentModoCapturaBinding(@NonNull RelativeLayout rootView,
      @NonNull MaterialButton adjuntar, @NonNull MaterialButton camara, @NonNull ImageView cerrar,
      @NonNull TextView subtitulo, @NonNull TextView titulo) {
    this.rootView = rootView;
    this.adjuntar = adjuntar;
    this.camara = camara;
    this.cerrar = cerrar;
    this.subtitulo = subtitulo;
    this.titulo = titulo;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentModoCapturaBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentModoCapturaBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_modo_captura, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentModoCapturaBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.adjuntar;
      MaterialButton adjuntar = ViewBindings.findChildViewById(rootView, id);
      if (adjuntar == null) {
        break missingId;
      }

      id = R.id.camara;
      MaterialButton camara = ViewBindings.findChildViewById(rootView, id);
      if (camara == null) {
        break missingId;
      }

      id = R.id.cerrar;
      ImageView cerrar = ViewBindings.findChildViewById(rootView, id);
      if (cerrar == null) {
        break missingId;
      }

      id = R.id.subtitulo;
      TextView subtitulo = ViewBindings.findChildViewById(rootView, id);
      if (subtitulo == null) {
        break missingId;
      }

      id = R.id.titulo;
      TextView titulo = ViewBindings.findChildViewById(rootView, id);
      if (titulo == null) {
        break missingId;
      }

      return new FragmentModoCapturaBinding((RelativeLayout) rootView, adjuntar, camara, cerrar,
          subtitulo, titulo);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
