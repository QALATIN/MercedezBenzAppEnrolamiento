package com.latinid.mercedes.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.latinid.mercedes.DatosRecolectados;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperacionesUtiles {

    public static String dateFormatFromCurp(String string) {
        try {
            Date date = new SimpleDateFormat("yy-MM-dd").parse(string);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            return formatter.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String dateFormatAcuant(String date){
        try {
            String dateMiliseconds = date.replace("/Date(","").replace(")/","");
            long currentDateTime = Long.parseLong(dateMiliseconds);
            Date currentDate = new Date(currentDateTime);
            System.out.println("current Date: " + currentDate);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            System.out.println("Milliseconds to Date: " + df.format(currentDate));
            return df.format(currentDate);
        }catch (Throwable e){
            Log.e("OperacionesUtiles", "ERROR()", e);
            return null;
        }
    }

    public static void saveCoors(Context context){
        GpsTracker gpsTracker = new GpsTracker(context);
        if (gpsTracker.canGetLocation()) {
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            String coors = latitude + "," + longitude;
            DatosRecolectados.coors = coors;
        } else {
            gpsTracker.showSettingsAlert();
        }
    }


    public static String saveFileAndGetPath(File storageDir, String base64, String suffix) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "FILE_" + timeStamp + "_";
        File file = File.createTempFile(
                imageFileName,
                suffix,
                storageDir
        );
        file.mkdir();
        byte[] pdfAsBytes = Base64.decode(base64, 0);
        FileOutputStream os;
        os = new FileOutputStream(file, false);
        os.write(pdfAsBytes);
        os.flush();
        os.close();
        return file.getAbsolutePath();
    }

    public static String readFile(File file){
        try {
            int length = (int) file.length();

            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }

            return new String(bytes);
        }catch (Throwable e){
            e.printStackTrace();
            return "";
        }
    }

    public static String writeToFile(File storageDir, String data) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "FILE_" + timeStamp + "_";
            File file = File.createTempFile(
                    imageFileName,
                    ".txt",
                    storageDir
            );
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(data.getBytes());
            } finally {
                stream.close();
            }
            return file.getAbsolutePath();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            return "";
        }
    }

    public static String dateFormat(String string) {
        try {
            Date date = new SimpleDateFormat("dd-MM-yyyy").parse(string);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            return formatter.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String dateFormat2(String string) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(string);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            return formatter.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String fechaFormada() {
        Date myDate = new Date();
        //Aquí obtienes el formato que deseas
        return (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(myDate));
    }

    public static String dateEnrollment() {
        Date myDate = new Date();
        //Aquí obtienes el formato que deseas
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(myDate));
    }

    public static void generarTXTJson(Context context, String sFileName, String sBody) {
        try {
            File root = new File(context.getFilesDir() +
                    File.separator + "IDBIOMETRIC"+File.separator);
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            System.out.println(gpxfile.getPath());
            System.out.println(gpxfile.getAbsolutePath());
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            //Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap generarBitmapFromBase64(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @SuppressLint("SimpleDateFormat")
    public static String stringCustomDateToday() {
        return (new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    public static int calculateAgeFromBirth(LocalDate dob) {

        LocalDate curDate = LocalDate.now();

        if ((dob != null) && (curDate != null)) {
            return Period.between(dob, curDate).getYears();
        } else {
            return 0;
        }
    }

    public static String getAge(int year, int month, int day){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return ageS;
    }

    public static String getBase64FromPath(File path) {
        String base64 = "";
        try {
            File file = path;
            byte[] buffer = new byte[(int) file.length() + 100];
            int length = new FileInputStream(file).read(buffer);
            base64 = Base64.encodeToString(buffer, 0, length,
                    Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64;
    }


    public static JSONObject generarDireccion(String direccion) {
        String Cp = "";
        String CalleCol = "";
        String Estado = "";
        String Municipio = "";
        try {

            String domiciliocompleto = direccion.trim();

            Pattern p = Pattern.compile("[0-9]{5}");
            Matcher m = p.matcher(domiciliocompleto);
            ArrayList<String> codigospos = new ArrayList<String>();

            while (m.find()) {
                codigospos.add(m.group());
            }

            if (codigospos.size() > 0) {
                Cp = codigospos.get(codigospos.size() - 1).replaceAll("[^\\dA-Za-z ]", "").replaceAll("[^a-zA-Z0-9 ]", "");
            }

            String cllecol = "";
            String muni = "";
            String estad = "";
            int interseccion = domiciliocompleto.indexOf(" ");
            int espacio = domiciliocompleto.lastIndexOf(" ");
            if (interseccion == -1) {
                interseccion = domiciliocompleto.lastIndexOf(".");
                if (interseccion != -1) {
                    estad = domiciliocompleto.substring(espacio + 2, interseccion).replace(",", "");
                    if (codigospos.size() > 0) {
                        muni = domiciliocompleto.substring(domiciliocompleto.indexOf(codigospos.get(codigospos.size() - 1)) + 5, domiciliocompleto.indexOf(estad) - 1);
                        cllecol = domiciliocompleto.substring(0, domiciliocompleto.indexOf(codigospos.get(codigospos.size() - 1)) - 1);
                    }
                } else {
                    interseccion = domiciliocompleto.lastIndexOf(",");
                    if (interseccion != -1) {
                        estad = domiciliocompleto.substring(interseccion + 1).replace(",", "");
                        if (codigospos.size() > 0) {
                            muni = domiciliocompleto.substring(domiciliocompleto.indexOf(codigospos.get(codigospos.size() - 1)) + 5, domiciliocompleto.indexOf(estad) - 1);
                            cllecol = domiciliocompleto.substring(0, domiciliocompleto.indexOf(codigospos.get(codigospos.size() - 1)) - 1);
                        }
                    }
                }
            } else {
                estad = domiciliocompleto.substring(espacio).replace(",", "");
                if (codigospos.size() > 0) {
                    muni = domiciliocompleto.substring(domiciliocompleto.indexOf(codigospos.get(codigospos.size() - 1)) + 5, espacio);
                    cllecol = domiciliocompleto.substring(0, domiciliocompleto.indexOf(codigospos.get(codigospos.size() - 1)) - 1);
                }
            }
            CalleCol = cllecol.replaceAll("[^\\dA-Za-z ]", "").replaceAll("[^a-zA-Z0-9 ]", "");
            Municipio = muni.replace(".", "").replace(",", "").replaceAll("[^\\dA-Za-z ]", "").replaceAll("[^a-zA-Z0-9 ]", "");
            Estado = estad.replace(",", "").replaceAll("[^\\dA-Za-z ]", "").replaceAll("[^a-zA-Z0-9 ]", "");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("CalleCol", CalleCol);
            jsonObject.put("Estado", Estado);
            jsonObject.put("Municipio", Municipio);
            jsonObject.put("Cp", Cp);
            return jsonObject;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
