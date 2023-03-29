package com.latinid.mercedes.ui.nuevosolicitante.privacypolicy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;



import com.latinid.mercedes.R;
import com.latinid.mercedes.databinding.FragmentAvisoBinding;
import com.latinid.mercedes.Main2Activity;


public class AvisoFragment extends Fragment {

    private FragmentAvisoBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAvisoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.textoAviso.setText(HtmlCompat.fromHtml(getString(R.string.htmlFormattedText), HtmlCompat.FROM_HTML_MODE_LEGACY));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.buttonAceptarAviso.setOnClickListener(view -> {
            ((Main2Activity) getActivity()).replaceFragments(SignaturePolicyFragment.class);
        });
        binding.buttonCancelarAviso.setOnClickListener(view -> {
            ((Main2Activity) getActivity()).removerFragmets();
        });
        binding.buttonBack.setOnClickListener(view -> {
            ((Main2Activity) getActivity()).removerFragmets();
        });
        //binding.textoAviso.setText(HtmlCompat.fromHtml(getString(R.string.aviso_texto_lorem),0));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }




}