package com.latinid.mercedes.ui.applicants;

import static com.latinid.mercedes.util.OperacionesUtiles.generarBitmapFromBase64;
import static com.latinid.mercedes.util.OperacionesUtiles.readFile;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;
import com.latinid.mercedes.DatosRecolectados;

import com.latinid.mercedes.R;
import com.latinid.mercedes.databinding.FragmentInProcessBinding;
import com.latinid.mercedes.db.DataBase;
import com.latinid.mercedes.db.model.ActiveEnrollment;
import com.latinid.mercedes.model.local.IdentificacionModel;
import com.latinid.mercedes.model.local.PersonaModel;
import com.latinid.mercedes.model.local.SignatureModel;
import com.latinid.mercedes.ui.nuevosolicitante.ReviewFragment;
import com.latinid.mercedes.ui.nuevosolicitante.SendInfoFragment;
import com.latinid.mercedes.ui.nuevosolicitante.archivos.DocsFragment;
import com.latinid.mercedes.ui.nuevosolicitante.capturaid.IdentificacionFragment;
import com.latinid.mercedes.Main2Activity;
import com.latinid.mercedes.ui.nuevosolicitante.fingerprint.HuellasFragment;
import com.latinid.mercedes.ui.nuevosolicitante.selfie.fragments.SelfieAwareFragment;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class InProcessFragment extends Fragment {

    FragmentInProcessBinding binding;
    private ExecutorService executor
            = Executors.newFixedThreadPool(3);

    private static final String TAG = "InProcessFragment";

    private List<ActiveEnrollment> activeEnrollments;
    private List<ActiveEnrollment> activeEnrollmentsTemp;
    private MaterialDatePicker<Pair<Long, Long>> picker;
    private long firstTime;
    private long secondTime;
    MaterialDatePicker<Pair<Long, Long>> pickerRange;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInProcessBinding.inflate(inflater, container, false);
        requireActivity().runOnUiThread(() -> {
            binding.gifMercedes.setBackgroundResource(R.drawable.loadinggeneral);
            AnimationDrawable frameAnimation = (AnimationDrawable) binding.gifMercedes.getBackground();
            frameAnimation.start();
        });

        binding.buttonBack.setOnClickListener(view -> {
            ((Main2Activity) getActivity()).removerFragmets();
        });

        executor.execute(() -> {
            DataBase dataBase = new DataBase(requireContext());
            dataBase.open();
            activeEnrollments = dataBase.getEnrolls();
            activeEnrollmentsTemp = dataBase.getEnrolls();
            dataBase.close();
            setCalendar();
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String strDate = dateFormat.format(date);
            Calendar cal = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                cal.setTime(sdf.parse(strDate));
                cal2.setTime(sdf.parse(strDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            loadCalendars(cal.getTimeInMillis(), cal2.getTimeInMillis());
            createViews();
        });
        return binding.getRoot();
    }

    private void setCalendar() {
        binding.datePicker.setOnClickListener(view -> {
            executor.execute(() -> {
                MaterialDatePicker.Builder<Pair<Long, Long>> builder =
                        MaterialDatePicker.Builder.dateRangePicker();
                CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
                builder.setCalendarConstraints(constraintsBuilder.build());
                builder.setSelection(new Pair<>(firstTime, secondTime));
                picker = builder.build();
                requireActivity().runOnUiThread(() -> {
                    picker.show(requireActivity().getSupportFragmentManager(), picker.toString());
                    updateViews();
                });

            });
        });
    }

    private void updateViews() {
        picker.addOnPositiveButtonClickListener(selection -> {
            executor.execute(() -> {
                requireActivity().runOnUiThread(() -> {
                    binding.datePicker.setEnabled(false);
                    binding.content.removeAllViews();
                    binding.content.setEnabled(false);
                    binding.content.setClickable(false);
                    binding.gifMercedes.setVisibility(View.VISIBLE);
                });
                long firstDate = selection.first;
                long secondDate = selection.second;
                loadCalendars(firstDate, secondDate);
                createViews();
                requireActivity().runOnUiThread(() -> {
                    binding.datePicker.setEnabled(true);
                    binding.content.setEnabled(true);
                    binding.content.setClickable(true);
                    binding.gifMercedes.setVisibility(View.INVISIBLE);
                });
            });
        });
    }

    private boolean firsSelection= true;
    private void loadCalendars(long one, long two) {
        firstTime = one;
        secondTime = two;
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(one);
        Calendar calendar2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar2.setTimeInMillis(two);
        calendar2.set(Calendar.HOUR_OF_DAY, 23);
        calendar2.set(Calendar.MINUTE, 59);
        calendar2.set(Calendar.SECOND, 59);
        requireActivity().runOnUiThread(() -> {
            SimpleDateFormat format = new SimpleDateFormat("dd MMM");
            Calendar calendar1  = calendar;
            if(!firsSelection){
                calendar1.add(java.util.Calendar.DATE, 1);
            }
            firsSelection = false;
            String formattedFirst = format.format(calendar1.getTime());
            String formattedSecond = format.format(calendar2.getTime());
            requireActivity().runOnUiThread(() -> {
                binding.datePicker.setText(formattedFirst+" - "+formattedSecond);
            });
        });
        activeEnrollmentsTemp = new ArrayList<>();
        for (ActiveEnrollment activeEnrollment : activeEnrollments) {
            try {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                cal.setTime(sdf.parse(activeEnrollment.getDate()));
                if (cal.compareTo(calendar) >= 0 && cal.compareTo(calendar2) < 0) {
                    activeEnrollmentsTemp.add(activeEnrollment);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void createViews() {
        executor.execute(() -> {

            LinearLayout parent = new LinearLayout(requireContext());
            parent.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 5);
            parent.setLayoutParams(params);
            int contadorViews = 1;
            int contadorCompletos = 0;
            for (ActiveEnrollment activeEnrollment : activeEnrollmentsTemp) {
                int state = Integer.parseInt(activeEnrollment.getState_id());
                if (state < 8) {
                    contadorCompletos++;
                }
            }
            Collections.reverse(activeEnrollmentsTemp);
            for (ActiveEnrollment activeEnrollment : activeEnrollmentsTemp) {
                Log.d(TAG, activeEnrollment.toString());
                int state = Integer.parseInt(activeEnrollment.getState_id());
                if (state < 8) {
                    if (activeEnrollment.getState_id().equals("9")) {
                        return;
                    }
                    ArrayList<String> faltaporce = stringFaltante(activeEnrollment);
                    String name = "AÚN NO CUENTA CON NOMBRE Y APELLIDO";
                    String birth = "S/N";
                    String selfie = "";
                    try {
                        String idenId = activeEnrollment.getIdent_id();
                        if (!idenId.equals("null")) {
                            PersonaModel personaModel = new Gson().fromJson(readFile(new File(activeEnrollment.getJson_pers())), PersonaModel.class);
                            name = personaModel.getNombre() + " " + personaModel.getPaterno() + " " + personaModel.getMaterno();
                            birth = personaModel.getFechaDeNacimiento();
                            if (idenId.equals("2") || idenId.equals("10")) {
                                selfie = personaModel.getFotoSelfieB64();
                            } else if (idenId.equals("1")) {//
                                IdentificacionModel identificacionModel = new Gson().fromJson(readFile(new File(activeEnrollment.getJson_ident())), IdentificacionModel.class);
                                selfie = identificacionModel.getFotoRecorteB64();
                            }
                        }
                    } catch (Throwable e) {
                        Log.e(TAG, "ErrorControlate: ", e);
                        SignatureModel signatureModel = new Gson().fromJson(readFile(new File(activeEnrollment.getJson_signature())), SignatureModel.class);
                        selfie = signatureModel.getBase64Signature();
                    }

                    View view = createSubViews(faltaporce.get(0), faltaporce.get(1), name, birth, activeEnrollment.getEnroll_id(), activeEnrollment.getDate(), selfie, activeEnrollment);

                    if ((contadorViews % 2) != 0) {
                        parent = new LinearLayout(requireContext());
                        parent.setOrientation(LinearLayout.HORIZONTAL);
                        parent.setLayoutParams(params);
                        parent.addView(view);
                        if ((contadorViews) == contadorCompletos) {
                            LinearLayout finalParent1 = parent;
                            requireActivity().runOnUiThread(() -> {
                                binding.content.addView(finalParent1);
                            });
                        }
                    } else {
                        parent.addView(view);
                        LinearLayout finalParent = parent;
                        requireActivity().runOnUiThread(() -> {
                            binding.content.addView(finalParent);

                        });
                    }
                    contadorViews++;
                }
            }

        });
    }

    private void completeEnrollment(ActiveEnrollment activeEnrollment) {
        executor.execute(() -> {
            DatosRecolectados.activeEnrollment = activeEnrollment;
            switch (activeEnrollment.getState_id()) {
                case "1":
                    requireActivity().runOnUiThread(() -> {
                        ((Main2Activity) getActivity()).replaceFragments(IdentificacionFragment.class);
                    });
                    break;
                case "2":
                    requireActivity().runOnUiThread(() -> {
                        ((Main2Activity) getActivity()).replaceFragments(SelfieAwareFragment.class);
                    });
                    break;
                case "3":
                    requireActivity().runOnUiThread(() -> {
                        ((Main2Activity) getActivity()).replaceFragments(HuellasFragment.class);
                    });
                    break;
                case "4":
                    requireActivity().runOnUiThread(() -> {
                        ((Main2Activity) getActivity()).replaceFragments(DocsFragment.class);
                    });
                    break;
                case "5":
                case "6":
                    requireActivity().runOnUiThread(() -> {
                        ((Main2Activity) getActivity()).replaceFragments(ReviewFragment.class);
                    });
                    break;
                case "7":
                    if (activeEnrollment.getSignature_id().equals("10")) {
                        requireActivity().runOnUiThread(() -> {
                            ((Main2Activity) getActivity()).replaceFragments(SendInfoFragment.class);
                        });
                    } else {
                        requireActivity().runOnUiThread(() -> {
                            ((Main2Activity) getActivity()).replaceFragments(ReviewFragment.class);
                        });
                    }
                    break;
                case "8":
                    requireActivity().runOnUiThread(() -> {
                        ((Main2Activity) getActivity()).replaceFragments(SendInfoFragment.class);
                    });
                    break;
            }
        });
    }

    private ArrayList<String> stringFaltante(ActiveEnrollment activeEnrollment) {

        String falta = "";
        String porcentaje = "";
        String state_id = activeEnrollment.getState_id();
        String signature_id = activeEnrollment.getSignature_id();
        String ident_id = activeEnrollment.getIdent_id();
        String finger_id = activeEnrollment.getFinger_id();
        String docs_id = activeEnrollment.getDocs_id();
        if (!state_id.equals("0")) {
            if (state_id.equals("1")) {
                falta = "Registro con solo firma";
                porcentaje = "12.50%";
            }
            if (state_id.equals("2")) {
                falta = "Selfie, Huellas, Documentos, Revisión, Folio";
                porcentaje = "25%";
            }
            if (state_id.equals("3")) {
                falta = "Huellas, Documentos, Revisión, Folio";
                porcentaje = "37.50%";
            }
            if (state_id.equals("4")) {
                falta = "Documentos, Revisión, Folio";
                porcentaje = "50%";
            }
            if (state_id.equals("5")) {
                falta = "Revisión, Folio";
                porcentaje = "62.50%";
            }if (state_id.equals("6")) {
                falta = "Revisión, Folio";
                porcentaje = "62.50%";
            }
            if (state_id.equals("7")) {
                falta = "Folio";
                if (!signature_id.equals("10")) {
                    falta = "Folio y revisar servicio de firma, antes de enviar";
                    porcentaje = "75%";
                } else {
                    porcentaje = "87.50%";
                }
            }
        } else {

        }
        ArrayList<String> res = new ArrayList<>();
        res.add(falta);
        res.add(porcentaje);

        return res;
    }

    private View createSubViews(String missing, String porcentaje, String name, String birth, String id, String dateRegister, String selfie, ActiveEnrollment activeEnrollment) {
        LinearLayout childOneL = new LinearLayout(requireContext());
        childOneL.setOrientation(LinearLayout.VERTICAL);
        ViewGroup childOne = (ViewGroup) childOneL;
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5F);
        childOne.setPadding(10, 10, 10, 10);
        params2.setMargins(10, 0, 0, 10);
        childOne.setBackground(requireContext().getDrawable(R.drawable.troke_view));
        childOne.setLayoutParams(params2);

        /* TEXTVIEW: Faltan huellas, documentos*/
        TextView textFaltante = new TextView(requireContext());
        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textFaltante.setLayoutParams(params3);
        textFaltante.setTextSize(16);
        textFaltante.setTextColor(requireContext().getColor(R.color.app_naranja_texto));
        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.corpostextofficebold);
        textFaltante.setTypeface(typeface);
        textFaltante.setText("Falta: " + missing);

        /* VIEW:Linea separadora*/
        View view = new View(requireContext());
        LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 3);
        view.setLayoutParams(params4);
        view.setBackgroundColor(requireContext().getColor(R.color.app_background_color));

        /* VIEW:Linea separadora*/
        View view2 = new View(requireContext());
        view2.setLayoutParams(params4);
        view2.setBackgroundColor(requireContext().getColor(R.color.app_background_color));

        /* VIEW:Linea separadora*/
        View view3 = new View(requireContext());
        view3.setLayoutParams(params4);
        view3.setBackgroundColor(requireContext().getColor(R.color.app_background_color));

        /* VIEW:Linea separadora*/
        View view4 = new View(requireContext());
        view3.setLayoutParams(params4);
        view4.setBackgroundColor(requireContext().getColor(R.color.app_background_color));

        /* LINEARLAYOUT : Avance de solicitud: 80%       Incompleto */
        LinearLayout linearLayoutAvance = new LinearLayout(requireContext());
        linearLayoutAvance.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params5 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutAvance.setLayoutParams(params5);

        TextView textAvance = new TextView(requireContext());
        LinearLayout.LayoutParams params6 = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5F);
        textAvance.setLayoutParams(params6);
        textAvance.setTypeface(typeface);
        textAvance.setTextSize(16);
        textAvance.setText("Avance de solicitud: " + porcentaje);

        TextView textIncompleto = new TextView(requireContext());
        textIncompleto.setLayoutParams(params6);
        textIncompleto.setTypeface(typeface);
        textIncompleto.setTextSize(16);
        textIncompleto.setTextColor(requireContext().getColor(R.color.app_naranja_texto));
        textIncompleto.setText("Incompleto");
        linearLayoutAvance.addView(textAvance);
        linearLayoutAvance.addView(textIncompleto);

        /* SE AGREGA VIEW QUE YA SE DECLARO ARRIBA*/

        /* TEXTVIEW: Fecha de solicitud: 23/08/2022*/
        TextView textFecha = new TextView(requireContext());
        textFecha.setLayoutParams(params3);
        textFecha.setTextSize(16);
        textFecha.setTypeface(typeface);
        textFecha.setText("Fecha de solicitud: " + dateRegister);

        /* SE AGREGA VIEW QUE YA SE DECLARO ARRIBA*/

        /* LinearLayot: Foto FACE, nombre, nacimiento, button*/
        LinearLayout linearFoto = new LinearLayout(requireContext());
        linearFoto.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params7 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearFoto.setLayoutParams(params7);

        ImageView imageFace = new ImageView(requireContext());
        LinearLayout.LayoutParams params8 = new LinearLayout.LayoutParams(
                0, 200, 0.3F);
        imageFace.setLayoutParams(params8);
        try {
            if (selfie.equals("")) {
                imageFace.setImageResource(R.mipmap.solicitante_c);
            } else {
                Bitmap bitmap = generarBitmapFromBase64(selfie);
                imageFace.setImageBitmap(bitmap);
            }
        }catch (Throwable ignored){
            imageFace.setImageResource(R.mipmap.solicitante_c);
        }



        LinearLayout linearFotoDatos = new LinearLayout(requireContext());
        linearFotoDatos.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params9 = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7F);
        linearFotoDatos.setLayoutParams(params9);

        TextView textViewNombre = new TextView(requireContext());
        LinearLayout.LayoutParams params10 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params10.setMargins(10, 10, 10, 0);
        textViewNombre.setLayoutParams(params10);
        textViewNombre.setTextSize(23);
        textViewNombre.setTypeface(typeface);
        textViewNombre.setText("" + name);

        TextView textViewNacimiento = new TextView(requireContext());
        LinearLayout.LayoutParams params12 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params12.setMargins(10, 0, 10, 0);
        textViewNacimiento.setLayoutParams(params12);
        textViewNacimiento.setTextSize(16);
        textViewNacimiento.setTypeface(typeface);
        textViewNacimiento.setText("Fecha de nacimiento: " + birth);

        LinearLayout linearButon = new LinearLayout(requireContext());
        linearButon.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params13 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearButon.setLayoutParams(params13);
        MaterialButton materialButton = new MaterialButton(new ContextThemeWrapper(requireContext(), com.google.android.material.R.style.Widget_MaterialComponents_Button));
        LinearLayout.LayoutParams params11 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 50);
        params11.setMargins(50, 20, 50, 0);
        materialButton.setLayoutParams(params11);
        materialButton.setBackgroundTintList(requireContext().getColorStateList(R.color.app_blue_initial));
        materialButton.setTypeface(typeface);
        materialButton.setText("Completar");
        materialButton.setTextColor(requireContext().getColor(R.color.app_white_color));
        materialButton.setTextSize(16);
        materialButton.setCornerRadius(5);
        materialButton.setOnClickListener(view1 -> {
            completeEnrollment(activeEnrollment);
        });
        linearButon.addView(materialButton);

        linearFotoDatos.addView(linearButon);//textViewNombre
        linearFotoDatos.addView(textViewNombre);
        linearFotoDatos.addView(textViewNacimiento);

        linearFoto.addView(imageFace);
        linearFoto.addView(linearFotoDatos);

        childOne.addView(textFaltante);
        childOne.addView(view);
        childOne.addView(linearLayoutAvance);
        childOne.addView(view2);
        childOne.addView(textFecha);
        childOne.addView(view3);
        childOne.addView(linearFoto);

        return childOne;
    }


}