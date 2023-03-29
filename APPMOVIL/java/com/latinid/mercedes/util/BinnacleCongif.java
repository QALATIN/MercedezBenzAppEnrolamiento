package com.latinid.mercedes.util;

import static com.latinid.mercedes.util.OperacionesUtiles.dateEnrollment;
import static com.latinid.mercedes.util.OperacionesUtiles.generarTXTJson;
import static com.latinid.mercedes.util.OperacionesUtiles.readFile;

import android.content.Context;

import com.google.gson.Gson;
import com.latinid.mercedes.DatosRecolectados;
import com.latinid.mercedes.model.local.BinnacleModel;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class BinnacleCongif {

    public static void writeLog(String tag, int typeLog, String msg, String ref, Context context){
        new Thread(()->{
            try{
                BinnacleModel binnacle = new BinnacleModel();
                binnacle.setMensaje(tag+"|"+dateEnrollment()+"|"+msg);
                binnacle.setOrigenId(2);
                binnacle.setTipoLogId(typeLog);
                binnacle.setReferencia(ref);
                binnacle.setUsuarioId(DatosRecolectados.usuarioId);
                String json = new Gson().toJson(binnacle);
                writeInternalLog(context,json);
                String url = Conexiones.webServiceGeneral+"/Gateway/api/bitacora";
                HashMap<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + DatosRecolectados.token);
                JSONObject response = GetPost.crearPostHeaders(url,new JSONObject(json), params,context);
                writeInternalLog(context,response.toString());
            }catch (Throwable e){
                e.printStackTrace();
                writeInternalLog(context,e.getLocalizedMessage()+","+e.getMessage()+","+e.getCause());
            }
        }).start();
    }

    public static void writeInternalLog(Context context, String json){
        try {
            String log = readFile(new File(context.getFilesDir()+File.separator+"IDBIOMETRIC"+File.separator+"logInternal.log"));
            StringBuilder stringBuilder = new StringBuilder(log);
            stringBuilder.append("\n");
            stringBuilder.append(dateEnrollment()+"|"+json);
            generarTXTJson(context, "logInternal.log", stringBuilder.toString());
        }catch (Throwable e){
            e.printStackTrace();

        }

    }
}
