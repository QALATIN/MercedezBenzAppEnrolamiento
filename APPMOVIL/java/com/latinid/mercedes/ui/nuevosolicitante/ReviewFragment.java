package com.latinid.mercedes.ui.nuevosolicitante;


import static com.latinid.mercedes.util.OperacionesUtiles.calculateAgeFromBirth;
import static com.latinid.mercedes.util.OperacionesUtiles.dateFormatFromCurp;
import static com.latinid.mercedes.util.OperacionesUtiles.getAge;
import static com.latinid.mercedes.util.OperacionesUtiles.readFile;
import static com.latinid.mercedes.util.OperacionesUtiles.writeToFile;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;


import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;
import com.latinid.mercedes.DatosRecolectados;

import com.latinid.mercedes.R;
import com.latinid.mercedes.databinding.FragmentReviewBinding;
import com.latinid.mercedes.db.DataBase;
import com.latinid.mercedes.db.model.ActiveEnrollment;
import com.latinid.mercedes.model.local.PersonaModel;
import com.latinid.mercedes.Main2Activity;
import com.latinid.mercedes.ui.applicants.InProcessFragment;
import com.latinid.mercedes.ui.nuevosolicitante.archivos.ModoCapturaFragment;
import com.latinid.mercedes.ui.nuevosolicitante.privacypolicy.SignatureBackWok;
import com.latinid.mercedes.util.GenericMessage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReviewFragment extends Fragment {

    private FragmentReviewBinding binding;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private AnimationDrawable frameAnimation;
    PersonaModel persona = new PersonaModel();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReviewBinding.inflate(inflater, container, false);
        saveChanges();
        fillFields();
        hideKeyBoard();
        return binding.getRoot();
    }

    private void saveChanges() {

        binding.siguiente.setOnClickListener(view -> {

            String email = binding.outlinedEmail.getEditText().getText().toString();
            String tel = binding.outlinedNumber.getEditText().getText().toString();

            executor.execute(() -> {
                if(binding.outlinedCurp.getEditText().getText().equals("")||binding.outlinedCurp.getEditText().getText().length()!=18){
                    requireActivity().runOnUiThread(() -> {
                        binding.outlinedCurp.setError("Curp inválio");
                    });
                    return;
                }
                String regex = "^([A-Z][AEIOUX][A-Z]{2}\\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\\d|3[01])[HM](?:AS|B[CS]|C[CLMSH]|D[FG]|G[TR]|HG|JC|M[CNS]|N[ETL]|OC|PL|Q[TR]|S[PLR]|T[CSL]|VZ|YN|ZS)[B-DF-HJ-NP-TV-Z]{3}[A-Z\\d])(\\d)$";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(binding.outlinedCurp.getEditText().getText().toString());
                boolean res = m.matches();
                System.out.println(binding.outlinedCurp.getEditText().getText());
                System.out.println(binding.outlinedCurp.getEditText().getText().length());
                if(!res){
                    requireActivity().runOnUiThread(() -> {
                        System.out.println("curp inválido");
                        binding.outlinedCurp.setError("Curp inválio");
                    });
                    return;
                }
                System.out.println(binding.outlinedDomicilio.getEditText().getText().length());
                System.out.println(binding.outlinedDomicilio.getEditText().getText().toString());
                if(binding.outlinedDomicilio.getEditText().getText().toString().equals("")||binding.outlinedDomicilio.getEditText().getText().length()==0){
                    requireActivity().runOnUiThread(() -> {
                        binding.outlinedDomicilio.setError("Domicilio inválio");
                    });
                    return;
                }
                Pattern pattern = Patterns.EMAIL_ADDRESS;
                Matcher mat = pattern.matcher(email);
                System.out.println(email);
                if (!mat.find()) {
                    requireActivity().runOnUiThread(() -> {
                        binding.outlinedEmail.setError("Correo inválido");
                    });
                    return;
                }
                if (tel.length() != 10) {
                    requireActivity().runOnUiThread(() -> {
                        binding.outlinedNumber.setError("Número inválido");
                    });
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    view.setVisibility(View.INVISIBLE);
                    binding.gifMercedes.setBackgroundResource(R.drawable.loadinggeneral);
                    frameAnimation = (AnimationDrawable) binding.gifMercedes.getBackground();
                    frameAnimation.start();
                    binding.gifMercedes.setVisibility(View.VISIBLE);
                });
                persona.setCorreo(email);
                persona.setTelefono(tel);
                persona.setNombre(binding.outlinedName.getEditText().getText().toString());
                persona.setPaterno(binding.outlinedFather.getEditText().getText().toString());
                persona.setMaterno(binding.outlinedMom.getEditText().getText().toString());
                persona.setCurp(binding.outlinedCurp.getEditText().getText().toString());
                persona.setFechaDeNacimiento(binding.outlinedNacimiento.getEditText().getText().toString());
                persona.setDomicilioCompleto(binding.outlinedDomicilio.getEditText().getText().toString());
                persona.setSexo(binding.outlinedGender.getEditText().getText().toString());
                persona.setEdad(binding.outlinedAge.getEditText().getText().toString());


                String path = writeToFile(requireActivity().getFilesDir(), new Gson().toJson(persona));
                ActiveEnrollment activeEnrollment = DatosRecolectados.activeEnrollment;
                activeEnrollment.setJson_pers(path);
                activeEnrollment.setIdent_id("10");
                activeEnrollment.setState_id("6");
                DataBase dataBase = new DataBase(requireContext());
                dataBase.open();
                dataBase.updateEnroll(activeEnrollment, activeEnrollment.getEnroll_id());
                dataBase.close();

                Data.Builder data = new Data.Builder();
                data.putString("nombre", persona.getNombre());
                data.putString("paterno", persona.getPaterno());
                data.putString("materno", persona.getMaterno());
                //data.putInt("solicitanteId", idSolicitante);
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SignatureBackWok.class).setInputData(data.build())
                        .build();
                WorkManager.getInstance(requireContext())
                        .beginWith(Collections.singletonList(workRequest))
                        .enqueue();

                while (true) {
                    try {
                        if (!DatosRecolectados.docSignatureFinish.equals("")) {
                            break;
                        }
                        Thread.sleep(1000);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
               /* requireActivity().runOnUiThread(() -> {
                    GenericMessage message = GenericMessage.newInstance("Aviso","Intermitencia en un servicio, intenté más tarde, si persiste el problema comuniquesé a mesa de ayuda.");
                    message.setCancelable(false);
                    message.show(getParentFragmentManager(), "Alerta");
                    ((Main2Activity) getActivity()).replaceFragments(SendInfoFragment.class);
                });*/
                switch (DatosRecolectados.docSignatureFinish){
                    case "signed":
                        requireActivity().runOnUiThread(() -> {
                            ((Main2Activity) getActivity()).replaceFragments(SendInfoFragment.class);
                        });
                        break;
                    case  "nosigned":
                        requireActivity().runOnUiThread(() -> {
                            GenericMessage message = GenericMessage.newInstance("Aviso","Intermitencia en un servicio, intenté más tarde, si persiste el problema comuniquesé a soporte.");
                            message.setCancelable(true);
                            message.show(getParentFragmentManager(), "Alerta");
                            ((Main2Activity) getActivity()).replaceFragments(InProcessFragment.class);
                        });
                        break;
                }
            });


        });
    }

    private void hideKeyGeneral(EditText editText) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });
    }

    private MaterialDatePicker<Long> picker;

    private void hideKeyBoard() {
        binding.outlinedGender.getEditText().setOnClickListener(view -> {
            showMenuGender(view);
        });
        binding.outlinedNacimiento.getEditText().setOnClickListener(view -> {
            try {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                cal.setTime(sdf.parse(binding.outlinedNacimiento.getEditText().getText().toString()));
                MaterialDatePicker.Builder<Long> builder =
                        MaterialDatePicker.Builder.datePicker();
                CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
                builder.setCalendarConstraints(constraintsBuilder.build());
                builder.setSelection(cal.getTimeInMillis());
                picker = builder.build();
                picker.show(requireActivity().getSupportFragmentManager(), picker.toString());
                updateBirth();
            } catch (Throwable e) {
                Log.e("TAG", "ERROR()", e);
            }
        });
        hideKeyGeneral(binding.outlinedName.getEditText());
        hideKeyGeneral(binding.outlinedFather.getEditText());
        hideKeyGeneral(binding.outlinedMom.getEditText());
        hideKeyGeneral(binding.outlinedCurp.getEditText());
        //hideKeyGeneral(binding.outlinedNacimiento.getEditText());
        hideKeyGeneral(binding.outlinedDomicilio.getEditText());
        hideKeyGeneral(binding.outlinedGender.getEditText());
        hideKeyGeneral(binding.outlinedAge.getEditText());
        hideKeyGeneral(binding.outlinedNumber.getEditText());
        hideKeyGeneral(binding.outlinedEmail.getEditText());
    }

    private void updateBirth() {
        picker.addOnPositiveButtonClickListener(selection -> {
            executor.execute(() -> {
                long firstDate = selection;
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(firstDate);
                calendar.add(java.util.Calendar.DATE, 1);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String formattedFirst = format.format(calendar.getTime());
                binding.outlinedNacimiento.getEditText().setText(formattedFirst);
            });
        });
    }

    private void showMenuGender(View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenuInflater().inflate(R.menu.menu_gener, popup.getMenu());
        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getTitle().toString().equals("Femenino")) {
                binding.outlinedGender.getEditText().setText("Femenino");
            }
            if (menuItem.getTitle().toString().equals("Masculino")) {
                binding.outlinedGender.getEditText().setText("Masculino");
            }
            return false;
        });
        popup.show();
    }

    private void fillFields() {
        ActiveEnrollment activeEnrollment = DatosRecolectados.activeEnrollment;
        persona = new Gson().fromJson(readFile(new File(activeEnrollment.getJson_pers())), PersonaModel.class);

        try {
            binding.outlinedName.getEditText().setText(persona.getNombre().replaceAll("[^a-zA-ZñÑ0-9]", " ").trim());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            binding.outlinedFather.getEditText().setText(persona.getPaterno().replaceAll("[^a-zA-ZñÑ0-9]", " ").trim());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            binding.outlinedMom.getEditText().setText(persona.getMaterno().replaceAll("[^a-zA-ZñÑ0-9]", " ").trim());

        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            binding.outlinedCurp.getEditText().setText(persona.getCurp().replaceAll("[^a-zA-Z0-9]", "").trim());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            if(persona.getFechaDeNacimiento().length() == 10){
                binding.outlinedNacimiento.getEditText().setText(persona.getFechaDeNacimiento().trim());
            }else{
                if(binding.outlinedCurp.getEditText().getText().length() != 0){
                    String sub1 = binding.outlinedCurp.getEditText().getText().toString().substring(4, 10);
                    String a = sub1.substring(0, 2);
                    String mm = sub1.substring(2, 4);
                    String d = sub1.substring(4, 6);
                    String preFecha = a + "-" + mm + "-" + d;
                    binding.outlinedNacimiento.getEditText().setText(dateFormatFromCurp(preFecha));
                }else{
                    Date myDate = new Date();
                    binding.outlinedNacimiento.getEditText().setText(new SimpleDateFormat("yyyy-MM-dd").format(myDate));
                }
            }

            /*if(binding.outlinedCurp.getEditText().getText().length() != 0){
                String sub1 = binding.outlinedCurp.getEditText().getText().toString().substring(4, 10);
                String a = sub1.substring(0, 2);
                String mm = sub1.substring(2, 4);
                String d = sub1.substring(4, 6);
                String preFecha = a + "-" + mm + "-" + d;
                binding.outlinedNacimiento.getEditText().setText(dateFormatFromCurp(preFecha));
            }*/
        } catch (Throwable e) {
            if(binding.outlinedCurp.getEditText().getText().length() != 0){
                String sub1 = binding.outlinedCurp.getEditText().getText().toString().substring(4, 10);
                String a = sub1.substring(0, 2);
                String mm = sub1.substring(2, 4);
                String d = sub1.substring(4, 6);
                String preFecha = a + "-" + mm + "-" + d;
                binding.outlinedNacimiento.getEditText().setText(dateFormatFromCurp(preFecha));
            }else{
                Date myDate = new Date();
                binding.outlinedNacimiento.getEditText().setText(new SimpleDateFormat("yyyy-MM-dd").format(myDate));
            }
            e.printStackTrace();
        }
        try {
            binding.outlinedDomicilio.getEditText().setText(persona.getDomicilioCompleto().replaceAll("[^a-zA-Z0-9]", " ").trim());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            binding.outlinedGender.getEditText().setText((persona.getSexo().replaceAll("[^a-zA-Z0-9]", "").trim().equals("M")) ? "Masculino" : "Femenino");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            if(persona.getEdad().length() == 2){
                binding.outlinedAge.getEditText().setText(persona.getEdad());
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
        try {
            if(binding.outlinedAge.getEditText().getText().length() == 2){

            }else{
                try {
                    if(binding.outlinedNacimiento.getEditText().getText().length()==10){
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate dob = LocalDate.parse(binding.outlinedNacimiento.getEditText().getText(), formatter);
                        binding.outlinedAge.getEditText().setText(String.valueOf(calculateAgeFromBirth(dob)));
                    }
                }catch (Throwable e2){
                    e2.printStackTrace();
                    try {
                        if(persona.getCurp().length() != 0) {
                            String sub1 = persona.getCurp().substring(4, 10);
                            String a = sub1.substring(0, 2);
                            String mm = sub1.substring(2, 4);
                            String d = sub1.substring(4, 6);
                            String preFecha = a + "-" + mm + "-" + d;
                            LocalDate.of(Integer.parseInt(a),Integer.parseInt(mm),Integer.parseInt(d));
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            LocalDate dob = LocalDate.parse(preFecha,formatter);
                            binding.outlinedAge.getEditText().setText(String.valueOf(calculateAgeFromBirth(dob)));
                        }
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            }
        } catch (Throwable e) {
           e.printStackTrace();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}