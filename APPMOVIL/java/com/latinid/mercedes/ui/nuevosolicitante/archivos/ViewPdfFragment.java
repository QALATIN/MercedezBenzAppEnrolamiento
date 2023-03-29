package com.latinid.mercedes.ui.nuevosolicitante.archivos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.latinid.mercedes.R;
import com.latinid.mercedes.databinding.FragmentViewPdfBinding;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class ViewPdfFragment extends DialogFragment {

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private FragmentViewPdfBinding binding;

    public ViewPdfFragment() {

    }


    public static ViewPdfFragment newInstance(String param1) {
        ViewPdfFragment fragment = new ViewPdfFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    private File saveImage(final Context context, final String imageData) {
        final byte[] imgBytesData = android.util.Base64.decode(imageData,
                android.util.Base64.DEFAULT);
        try {
            final File file = File.createTempFile("pdf", null, context.getCacheDir());
            final FileOutputStream fileOutputStream;

            fileOutputStream = new FileOutputStream(file);


            final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                    fileOutputStream);
            bufferedOutputStream.write(imgBytesData);
            bufferedOutputStream.close();
            return file;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViewPdfBinding.inflate(inflater,container,false);
        try {
            File file = saveImage(requireContext(),mParam1);
            ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(parcelFileDescriptor);
            Bitmap bitmap = Bitmap.createBitmap(900, 900,Bitmap.Config.ARGB_8888);
            PdfRenderer.Page page = pdfRenderer.openPage(0);
            page.render(bitmap,null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            binding.imageView10.setImageBitmap(bitmap);
            page.close();
            pdfRenderer.close();
        }catch (Throwable d){
            Log.e("DocsFragment","Error: ", d);
        }
        return binding.getRoot();
    }
}