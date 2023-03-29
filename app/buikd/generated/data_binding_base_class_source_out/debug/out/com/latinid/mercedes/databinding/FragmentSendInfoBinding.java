// Generated by view binder compiler. Do not edit!
package com.latinid.mercedes.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.latinid.mercedes.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentSendInfoBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final MaterialButton buttonAval;

  @NonNull
  public final MaterialButton buttonCoa;

  @NonNull
  public final MaterialButton buttonfinalizar;

  @NonNull
  public final ImageView gifMercedes;

  @NonNull
  public final ImageView imageView11;

  @NonNull
  public final ImageView imageView4;

  @NonNull
  public final ConstraintLayout layoutEnviado;

  @NonNull
  public final ConstraintLayout layoutEnvio;

  @NonNull
  public final TextInputLayout outlinedFol;

  @NonNull
  public final MaterialButton saveFolio;

  @NonNull
  public final MaterialButton sendPack;

  @NonNull
  public final TextView textView11;

  @NonNull
  public final TextView textView4;

  @NonNull
  public final TextView textoMensaje;

  @NonNull
  public final TextView textoMensaje2;

  @NonNull
  public final TextView textoPregunta;

  @NonNull
  public final View view1;

  @NonNull
  public final View view2;

  private FragmentSendInfoBinding(@NonNull ConstraintLayout rootView,
      @NonNull MaterialButton buttonAval, @NonNull MaterialButton buttonCoa,
      @NonNull MaterialButton buttonfinalizar, @NonNull ImageView gifMercedes,
      @NonNull ImageView imageView11, @NonNull ImageView imageView4,
      @NonNull ConstraintLayout layoutEnviado, @NonNull ConstraintLayout layoutEnvio,
      @NonNull TextInputLayout outlinedFol, @NonNull MaterialButton saveFolio,
      @NonNull MaterialButton sendPack, @NonNull TextView textView11, @NonNull TextView textView4,
      @NonNull TextView textoMensaje, @NonNull TextView textoMensaje2,
      @NonNull TextView textoPregunta, @NonNull View view1, @NonNull View view2) {
    this.rootView = rootView;
    this.buttonAval = buttonAval;
    this.buttonCoa = buttonCoa;
    this.buttonfinalizar = buttonfinalizar;
    this.gifMercedes = gifMercedes;
    this.imageView11 = imageView11;
    this.imageView4 = imageView4;
    this.layoutEnviado = layoutEnviado;
    this.layoutEnvio = layoutEnvio;
    this.outlinedFol = outlinedFol;
    this.saveFolio = saveFolio;
    this.sendPack = sendPack;
    this.textView11 = textView11;
    this.textView4 = textView4;
    this.textoMensaje = textoMensaje;
    this.textoMensaje2 = textoMensaje2;
    this.textoPregunta = textoPregunta;
    this.view1 = view1;
    this.view2 = view2;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentSendInfoBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentSendInfoBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_send_info, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentSendInfoBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.buttonAval;
      MaterialButton buttonAval = ViewBindings.findChildViewById(rootView, id);
      if (buttonAval == null) {
        break missingId;
      }

      id = R.id.buttonCoa;
      MaterialButton buttonCoa = ViewBindings.findChildViewById(rootView, id);
      if (buttonCoa == null) {
        break missingId;
      }

      id = R.id.buttonfinalizar;
      MaterialButton buttonfinalizar = ViewBindings.findChildViewById(rootView, id);
      if (buttonfinalizar == null) {
        break missingId;
      }

      id = R.id.gif_mercedes;
      ImageView gifMercedes = ViewBindings.findChildViewById(rootView, id);
      if (gifMercedes == null) {
        break missingId;
      }

      id = R.id.imageView11;
      ImageView imageView11 = ViewBindings.findChildViewById(rootView, id);
      if (imageView11 == null) {
        break missingId;
      }

      id = R.id.imageView4;
      ImageView imageView4 = ViewBindings.findChildViewById(rootView, id);
      if (imageView4 == null) {
        break missingId;
      }

      id = R.id.layout_enviado;
      ConstraintLayout layoutEnviado = ViewBindings.findChildViewById(rootView, id);
      if (layoutEnviado == null) {
        break missingId;
      }

      id = R.id.layout_envio;
      ConstraintLayout layoutEnvio = ViewBindings.findChildViewById(rootView, id);
      if (layoutEnvio == null) {
        break missingId;
      }

      id = R.id.outlinedFol;
      TextInputLayout outlinedFol = ViewBindings.findChildViewById(rootView, id);
      if (outlinedFol == null) {
        break missingId;
      }

      id = R.id.saveFolio;
      MaterialButton saveFolio = ViewBindings.findChildViewById(rootView, id);
      if (saveFolio == null) {
        break missingId;
      }

      id = R.id.sendPack;
      MaterialButton sendPack = ViewBindings.findChildViewById(rootView, id);
      if (sendPack == null) {
        break missingId;
      }

      id = R.id.textView11;
      TextView textView11 = ViewBindings.findChildViewById(rootView, id);
      if (textView11 == null) {
        break missingId;
      }

      id = R.id.textView4;
      TextView textView4 = ViewBindings.findChildViewById(rootView, id);
      if (textView4 == null) {
        break missingId;
      }

      id = R.id.texto_mensaje;
      TextView textoMensaje = ViewBindings.findChildViewById(rootView, id);
      if (textoMensaje == null) {
        break missingId;
      }

      id = R.id.texto_mensaje2;
      TextView textoMensaje2 = ViewBindings.findChildViewById(rootView, id);
      if (textoMensaje2 == null) {
        break missingId;
      }

      id = R.id.texto_pregunta;
      TextView textoPregunta = ViewBindings.findChildViewById(rootView, id);
      if (textoPregunta == null) {
        break missingId;
      }

      id = R.id.view1;
      View view1 = ViewBindings.findChildViewById(rootView, id);
      if (view1 == null) {
        break missingId;
      }

      id = R.id.view2;
      View view2 = ViewBindings.findChildViewById(rootView, id);
      if (view2 == null) {
        break missingId;
      }

      return new FragmentSendInfoBinding((ConstraintLayout) rootView, buttonAval, buttonCoa,
          buttonfinalizar, gifMercedes, imageView11, imageView4, layoutEnviado, layoutEnvio,
          outlinedFol, saveFolio, sendPack, textView11, textView4, textoMensaje, textoMensaje2,
          textoPregunta, view1, view2);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
