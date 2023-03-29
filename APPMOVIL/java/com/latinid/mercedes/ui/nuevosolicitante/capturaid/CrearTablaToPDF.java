package com.latinid.mercedes.ui.nuevosolicitante.capturaid;

import android.content.Context;


import com.latinid.mercedes.R;
import com.latinid.mercedes.model.local.IdentificacionModel;
import com.latinid.mercedes.model.local.PersonaModel;

import java.util.ArrayList;

public class CrearTablaToPDF {

    private static ArrayList<TablaPDFModel> nuevo = new ArrayList<>();
    private static TablaPDFModel n = new TablaPDFModel();

    public static ArrayList<TablaPDFModel> createArrays(Context context, PersonaModel personaModel, IdentificacionModel identificacionModel) {
         if(identificacionModel.getTipoDeIdentificacion().equals("Mexico (MEX) Voter Identification Card")) {
            n.setTitulo(context.getResources().getString(R.string.pdf_nombre));
            n.setValor(personaModel.getNombre() + " " + personaModel.getPaterno() + " " + personaModel.getMaterno());
            nuevo.add(n);
            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_apellidos));
            n.setValor(personaModel.getPaterno() + " " + personaModel.getMaterno());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_curp));
            n.setValor(personaModel.getCurp());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_nombre_full));
            n.setValor(personaModel.getNombre() + " " + personaModel.getPaterno() + " " + personaModel.getMaterno());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_birth));
            n.setValor(personaModel.getFechaDeNacimiento());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_address));
            n.setValor(personaModel.getCalle_Numero());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_viz));
            n.setValor(identificacionModel.getOCR());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_emision));
            n.setValor(identificacionModel.getFechaDeEmision());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_vencimiento));
            n.setValor(identificacionModel.getFechaDeVigencia());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_sexo));
            n.setValor(personaModel.getSexo());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_bnumber));
            n.setValor(identificacionModel.getOCR());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_ndocumento));
            n.setValor(identificacionModel.getCIC());
            nuevo.add(n);
        } else if(identificacionModel.getTipoDeIdentificacion().equals("PASAPORTE CON CHIP")){
            n.setTitulo(context.getResources().getString(R.string.pdf_nombre));
            n.setValor(personaModel.getNombre() + " " + personaModel.getPaterno() + " " + personaModel.getMaterno());
            nuevo.add(n);
            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_apellidos));
            n.setValor(personaModel.getPaterno() + " " + personaModel.getMaterno());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_ndocumento));
            n.setValor(identificacionModel.getNumeroDeDocumento());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_nombre_full));
            n.setValor(personaModel.getNombre() + " " + personaModel.getPaterno() + " " + personaModel.getMaterno());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_birth));
            n.setValor(personaModel.getFechaDeNacimiento());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_nacionalidad));
            n.setValor(personaModel.getEstado());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_vencimiento));
            n.setValor(identificacionModel.getFechaDeVigencia());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_sexo));
            n.setValor(personaModel.getSexo());
            nuevo.add(n);

        }else if(identificacionModel.getTipoDeIdentificacion().equals("Mexico (MEX) Professional License")){
             n.setTitulo(context.getResources().getString(R.string.pdf_nombre));
             n.setValor(personaModel.getNombre());
             nuevo.add(n);
             n = new TablaPDFModel();
             n.setTitulo(context.getResources().getString(R.string.pdf_apellidos));
             n.setValor(personaModel.getPaterno() + " " + personaModel.getMaterno());
             nuevo.add(n);

             n = new TablaPDFModel();
             n.setTitulo(context.getResources().getString(R.string.pdf_ndocumento));
             n.setValor(identificacionModel.getNumeroDeDocumento());
             nuevo.add(n);

             n = new TablaPDFModel();
             n.setTitulo(context.getResources().getString(R.string.pdf_nombre_full));
             n.setValor(personaModel.getNombre() + " " + personaModel.getPaterno() + " " + personaModel.getMaterno());
             nuevo.add(n);

             n = new TablaPDFModel();
             n.setTitulo(context.getResources().getString(R.string.pdf_birth));
             n.setValor(personaModel.getFechaDeNacimiento());
             nuevo.add(n);

             n = new TablaPDFModel();
             n.setTitulo(context.getResources().getString(R.string.pdf_nacionalidad));
             n.setValor(identificacionModel.getNacionalidad());
             nuevo.add(n);

             n = new TablaPDFModel();
             n.setTitulo(context.getResources().getString(R.string.pdf_sexo));
             n.setValor(personaModel.getSexo());
             nuevo.add(n);

         } else {
            n.setTitulo(context.getResources().getString(R.string.pdf_nombre));
            n.setValor(personaModel.getNombre() + " " + personaModel.getPaterno() + " " + personaModel.getMaterno());
            nuevo.add(n);
            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_apellidos));
            n.setValor(personaModel.getPaterno() + " " + personaModel.getMaterno());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_curp));
            n.setValor(personaModel.getCurp());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_nombre_full));
            n.setValor(personaModel.getNombre() + " " + personaModel.getPaterno() + " " + personaModel.getMaterno());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_birth));
            n.setValor(personaModel.getFechaDeNacimiento());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_emision));
            n.setValor(identificacionModel.getFechaDeEmision());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_vencimiento));
            n.setValor(identificacionModel.getFechaDeVigencia());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_sexo));
            n.setValor(personaModel.getSexo());
            nuevo.add(n);

            n = new TablaPDFModel();
            n.setTitulo(context.getResources().getString(R.string.pdf_ndocumento));
            n.setValor(identificacionModel.getNumeroDeDocumento());
            nuevo.add(n);
        }

        return nuevo;

    }
}
