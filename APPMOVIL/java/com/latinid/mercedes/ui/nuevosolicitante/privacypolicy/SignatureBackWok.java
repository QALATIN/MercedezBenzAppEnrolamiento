package com.latinid.mercedes.ui.nuevosolicitante.privacypolicy;

import static com.latinid.mercedes.util.OperacionesUtiles.generarTXTJson;
import static com.latinid.mercedes.util.OperacionesUtiles.getBase64FromPath;
import static com.latinid.mercedes.util.OperacionesUtiles.readFile;
import static com.latinid.mercedes.util.OperacionesUtiles.saveFileAndGetPath;
import static com.latinid.mercedes.util.OperacionesUtiles.writeToFile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.latinid.mercedes.DatosRecolectados;
import com.latinid.mercedes.db.DataBase;
import com.latinid.mercedes.db.model.ActiveEnrollment;
import com.latinid.mercedes.model.local.SignatureModel;
import com.latinid.mercedes.ui.nuevosolicitante.capturaid.UtilsPDF;
import com.latinid.mercedes.util.Conexiones;
import com.latinid.mercedes.util.GetPost;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.io.image.ImageData;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class SignatureBackWok extends Worker {

    private static final String TAG = "SignatureBackWok";

    public SignatureBackWok(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        DataBase dataBase = new DataBase(getApplicationContext());
        ActiveEnrollment activeEnrollment = DatosRecolectados.activeEnrollment;
        try {
            dataBase.open();
            File file = new File(activeEnrollment.getJson_signature());
            String jsonSignatureEnroll = readFile(file);
            SignatureModel signatureModel = new Gson().fromJson(jsonSignatureEnroll, SignatureModel.class);
            String nombre = getInputData().getString("nombre");
            String paterno = getInputData().getString("paterno");
            String materno = getInputData().getString("materno");
            int solicitanteId = getInputData().getInt("solicitanteId", 1);
            String firmabase64 = signatureModel.getBase64Signature();
            String pdfToSign = createPdfPrivacy();
            DatosRecolectados.pdfToSignTemp =pdfToSign;
            generarTXTJson(getApplicationContext(), "frimab64.txt", firmabase64);
            generarTXTJson(getApplicationContext(), "aviso.txt", pdfToSign);
            if (pdfToSign.equals("")) {
                activeEnrollment.setSignature_id("2");
                activeEnrollment.setState_id("7");
                dataBase.updateEnroll(activeEnrollment, activeEnrollment.getEnroll_id());
                DatosRecolectados.docSignatureFinish = "nosigned";
                return null;
            }
            JSONObject jsonPdf = crearJSONPDFSeguriData(pdfToSign, nombre, paterno, materno);

            JSONObject docPreSignature = generarFirmaSeguriData(jsonPdf);
            if (docPreSignature == null) {
                activeEnrollment.setSignature_id("3");
                activeEnrollment.setState_id("7");
                dataBase.updateEnroll(activeEnrollment, activeEnrollment.getEnroll_id());
                DatosRecolectados.docSignatureFinish = "nosigned";
                return null;
            }
            boolean status = docPreSignature.getBoolean("status");
            if (!status) {
                activeEnrollment.setSignature_id("4");
                activeEnrollment.setState_id("7");
                dataBase.updateEnroll(activeEnrollment, activeEnrollment.getEnroll_id());
                DatosRecolectados.docSignatureFinish = "nosigned";
                return null;
            }
            String guid = docPreSignature.getString("guid");
            String referenceSignature = docPreSignature.getJSONArray("firmantes").getJSONObject(0).getString("referencia");
            JSONObject JsonAddSign = createJsonAddSign(referenceSignature, firmabase64);
            generarTXTJson(getApplicationContext(), "jsonFirma.json", JsonAddSign.toString());
            JSONObject responseAddSignature = GetPost.crearPost(Conexiones.webServiceSeguriData + "/AddFirma", JsonAddSign, getApplicationContext());
            if (responseAddSignature == null) {
                activeEnrollment.setSignature_id("5");
                activeEnrollment.setState_id("7");
                dataBase.updateEnroll(activeEnrollment, activeEnrollment.getEnroll_id());
                DatosRecolectados.docSignatureFinish = "nosigned";
                return null;
            }
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("Guid", guid);
            JSONObject responseGetDoc = GetPost.crearPost(Conexiones.webServiceSeguriData + "/DocumentoFirmado", jsonObject1, getApplicationContext());
            if (responseGetDoc == null || !responseGetDoc.getBoolean("firmado")) {
                activeEnrollment.setSignature_id("6");
                activeEnrollment.setState_id("7");
                dataBase.updateEnroll(activeEnrollment, activeEnrollment.getEnroll_id());
                DatosRecolectados.docSignatureFinish = "nosigned";
                return null;
            }
            String docSignature = responseGetDoc.getString("pdfBase64");
            generarTXTJson(getApplicationContext(), "docfirmadooo.txt", docSignature);
            //sendAviso(solicitanteId, docSignature,1,guid);
            signatureModel.setBase64DocSignature(docSignature);
            signatureModel.setReference(guid);
            String jsonSignature = new Gson().toJson(signatureModel);
            String signaturePath = writeToFile(
                    getApplicationContext().getFilesDir(),
                    jsonSignature);
            activeEnrollment.setSignature_id("10");
            activeEnrollment.setState_id("7");
            activeEnrollment.setJson_signature(signaturePath);
            dataBase.updateEnroll(activeEnrollment, activeEnrollment.getEnroll_id());
            DatosRecolectados.docSignatureFinish = "signed";
            return Result.success();
        } catch (Throwable e) {
            Log.e(TAG, "Error()", e);
            DatosRecolectados.docSignatureFinish = "nosigned";
            return Result.failure();
        } finally {
            dataBase.close();
        }
    }

    private JSONObject createJsonAddSign(String reference, String firma) {
        try {
            String ip = "10.10.10.10";
            String json ="{" +
                    "\"referencia\":\""+reference+"\","+
                    "\"firmaBase64\":\""+firma+"\","+
                    "\"Ip\":\""+ip+"\""+
                    "}";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("referencia", reference);
            jsonObject.put("firmaBase64", firma.replace("\\",""));
            System.out.println(json);
            jsonObject.put("Ip", "10.10.10.10");
            String p = jsonObject.toString();
            JSONObject p2 = new JSONObject(p.replace("\\",""));
            System.out.println(p2);
            return p2;
            //return  new JSONObject(json);
        } catch (Throwable e) {
            Log.e(TAG, "Error createJsonSign() ", e);
            return null;
        }
    }

    private JSONObject generarFirmaSeguriData(JSONObject json) {
        try {
            /*HashMap<String, String> params = new HashMap<>();
            params.put("Authorization", "bearer " + token);*/
            return GetPost.crearPost(Conexiones.webServiceSeguriData + "/FileSend", json, getApplicationContext());
        } catch (Throwable e) {
            Log.e(TAG, "Error generarFirmaSeguriData()", e);
            return null;
        }
    }

    private String tokenRequestSignature() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("apikey", "5VArG6eS1ysu33Gbmtghdfv13JaZCoNCu");
            JSONObject response = GetPost.crearPost(Conexiones.webServiceTokenBearer, jsonObject, getApplicationContext());
            return response.getString("token");
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }


    private JSONObject crearJSONPDFSeguriData(String privacyNotice, String nombre, String paterno, String materno) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject jsonSignatories = new JSONObject();
            JSONObject jsonPagsSignatures = new JSONObject();
            JSONArray jsonArraySginatures = new JSONArray();
            JSONArray jsonArrayPags = new JSONArray();
            jsonPagsSignatures.put("Pagina", 6);
            jsonPagsSignatures.put("CoordenadaX", 200);
            jsonPagsSignatures.put("CoordenadaY", 50);
            jsonSignatories.put("Nombre", nombre);
            jsonSignatories.put("ApePat", paterno);
            jsonSignatories.put("ApeMat", materno);
            jsonSignatories.put("Correo", "");
            jsonArraySginatures.put(jsonSignatories);
            jsonArrayPags.put(jsonPagsSignatures);
            jsonSignatories.put("Paginasfirma", jsonArrayPags);
            jsonObject.put("pdFbase64", privacyNotice);
            jsonObject.put("firmantes", jsonArraySginatures);
            return jsonObject;
        } catch (Throwable e) {
            Log.e(TAG, "Error crearJSONPDFSeguriData() ", e);
            return null;
        }
    }

    private String createPdfPrivacy() {
        try {
            File path = new File(getApplicationContext().getFilesDir(), "/shared_pdf");
            if (!path.exists()) {
                path.mkdir();
            }

            File dir = new File(path, "temp2.pdf");
            PdfWriter writer = new PdfWriter(dir);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            document.setMargins(50, 100, 50, 100);
            pdfDocument.setDefaultPageSize(PageSize.A4);
           Header headerHandler = new Header("TONY");
            pdfDocument.addEventHandler(PdfDocumentEvent.START_PAGE, headerHandler);
            String imageLogoTop = "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";
            byte[] decodedString;
            Bitmap decodedByte;
            ByteArrayOutputStream stream3;
            decodedString = Base64.decode(imageLogoTop, Base64.DEFAULT);
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            stream3 = new ByteArrayOutputStream();
            decodedByte.compress(Bitmap.CompressFormat.PNG, 100, stream3);
            ImageData imageData = ImageDataFactory.create(stream3.toByteArray());
            Image image = new Image(imageData);
            Paragraph titlenotice = new Paragraph("Aviso de privacidad integral solicitantes, clientes, avales y representaciones legales\n").setBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER);

            Paragraph textNotice = new Paragraph("El presente Aviso de Privacidad únicamente aplica para aquellas personas físicas que, no teniendo una calidad comercial o profesional, proporcionan sus datos"
                    + "personales a Mercedes-Benz Mobility México, S. de R.L. de C.V. Lo anterior significa que el presente Aviso de Privacidad no resulta aplicable para:"
                    + "\n\n.    1. Aquella información proporcionada por personas físicas con actividad empresarial."
                    + "\n.    2. Aquella información referente a personas morales."
                    + "\n\n.En el supuesto de que el presente Aviso de Privacidad le resulte aplicable, le informamos que el tratamiento de sus datos personales se realizará conforme a lo establecido en éste.")
                    .setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice2 = new Paragraph("Identidad y Domicilio del Responsable").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice3 = new Paragraph(
                    "Responsable El Responsable del tratamiento de sus datos personales será la sociedad Mercedes-Benz Mobility México, S. de R.L. de C.V. (en adelante denominada como “MBMOMX”) "
                            + "con domicilio en Avenida Santa Fe No. 428, Torre III, Piso 10, Colonia Santa Fe Cuajimalpa, Alcaldía Cuajimalpa de Morelos, C.P. 05348, Ciudad de México, México.")
                    .setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice4 = new Paragraph("Categorías de Datos Personales Sujetas a Tratamiento").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice5 = new Paragraph("Los datos personales que proporciona usted voluntariamente a través de la solicitud de crédito, contratos y demás documentos celebrados entre usted y MBMOMX "
                    + "pertenecen a las siguientes categorías de datos: (i) datos de identificación; (ii) datos de contacto; (iii) datos patrimoniales y/o financieros; (iv) datos laborales; (v) datos "
                    + "relacionados con el vehículo objeto del crédito, incluyendo su geolocalización."
                    + "\n\nAsimismo, para cumplir con las finalidades previstas en el presente Aviso de Privacidad se dará tratamiento a su huella digital, la cual es considerada como un dato personal sensible."
                    + "\n\nFinalmente, le informamos que los datos personales de terceras personas que usted nos proporcione (familiares, dependientes económicos y referencias personales) serán tratados para los fines del presente Aviso de Privacidad. En caso de que usted nos proporcione dicha información, se entenderá que ha obtenido del tercero de quien proporcione datos personales su consentimiento.")
                    .setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice6 = new Paragraph("Finalidades Primarias del Tratamiento").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice7 = new Paragraph("Solicitantes:").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice7.setFirstLineIndent(10f);
            Paragraph textNotice8 = new Paragraph("1. Identificarlo y autenticarlo mediante el tratamiento de su huella digital con la finalidad de evitar fraudes y robos de identidad.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice8.setFirstLineIndent(20f);
            Paragraph textNotice9 = new Paragraph("2. Evaluar la información que nos proporciona en su solicitud de crédito para analizar si usted es o no sujeto al crédito solicitado.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice9.setFirstLineIndent(20f);
            Paragraph textNotice10 = new Paragraph("3. Contactarlo en relación con la solicitud de crédito para validar su información o requerirle información adicional.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice10.setFirstLineIndent(20f);
            Paragraph textNotice11 = new Paragraph("4. Verificar su historial crediticio ante Sociedades de Información Crediticia con las que MBMOMX mantengan una relación jurídica.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice11.setFirstLineIndent(20f);
            Paragraph textNotice12 = new Paragraph("5. Realizar investigaciones para combatir el lavado de dinero, fraudes y cualquier otra actividad ilícita.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice12.setFirstLineIndent(20f);
            Paragraph textNotice13 = new Paragraph("Clientes:").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice13.setFirstLineIndent(10f);
            Paragraph textNotice14 = new Paragraph("1. Cumplir con las obligaciones contractuales contraídas con usted.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice14.setFirstLineIndent(20f);
            Paragraph textNotice15 = new Paragraph("2. Realizar actividades de validación y autenticación de su identidad durante el mantenimiento de la relación contractual con la finalidad de "
                    + "evitar fraudes y robos de identidad.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice15.setFirstLineIndent(20f);
            Paragraph textNotice16 = new Paragraph("3. Gestión de credenciales de acceso para otorgarle acceso a la plataforma web de clientes.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice16.setFirstLineIndent(20f);
            Paragraph textNotice17 = new Paragraph("4. La contratación de seguros propios o de terceros, así como la prestación de los servicios solicitados o contratados por usted con MBMOMX al "
                    + "respecto.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice17.setFirstLineIndent(20f);
            Paragraph textNotice18 = new Paragraph("5. Hacer consultas, investigaciones y revisiones en relación con sus quejas o reclamaciones presentadas a través de nuestro Centro de Atención a Clientes, así como a través de los medios electrónicos destinados para este fin.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice18.setFirstLineIndent(20f);
            Paragraph textNotice19 = new Paragraph("6. Hacer consultas, investigaciones y revisiones relacionadas con los servicios y/o productos contratados por usted con MBMOMX, mismos que pueden ser realizados por proveedores contratados directamente por MBMOMX, los cuales realizarán dichas consultas, investigaciones y revisiones apegándose al presente Aviso de Privacidad.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice19.setFirstLineIndent(20f);
            Paragraph textNotice20 = new Paragraph("7. Contactarlo para cualquier tema relacionado a los servicios y/o productos contratados.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice20.setFirstLineIndent(20f);
            Paragraph textNotice21 = new Paragraph("8. En caso de que usted incurra en mora, llevar a cabo gestiones de cobranza que incluyen llamadas, envío de correos electrónicos, visitas personales, envío de mensajes SMS, requerimientos y cartas, así como el procesamiento de solicitudes, aclaraciones, investigaciones y facturación de cargos relacionados con los productos y/o servicios ofrecidos y contratados por usted, los cuales podrán ser realizados a través de MBMOMX o a través de terceros contratados directamente por MBMOMX, los cuales realizarán las gestiones de cobranza apegados al presente Aviso de Privacidad, así como a las disposiciones vigentes aplicables a las gestiones de cobranza.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice21.setFirstLineIndent(20f);
            Paragraph textNotice22 = new Paragraph("9. En algunos casos, de conformidad con lo pactado en su contrato celebrado entre MBMOMX y usted, MBMOMX podrá llevar a cabo diversos trámites ante autoridades vehiculares para las cuales usted haya dado su consentimiento, solicitado o en su caso sean requeridos.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice22.setFirstLineIndent(20f);
            Paragraph textNotice23 = new Paragraph("10. Realizar investigaciones para combatir el lavado de dinero, fraudes y cualquier otra actividad ilícita.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice23.setFirstLineIndent(20f);
            Paragraph textNotice24 = new Paragraph("11. Realizar actividades tendientes a actualizar su información para cumplir con la calidad de los datos.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice24.setFirstLineIndent(20f);
            Paragraph textNotice25 = new Paragraph("12. En su caso, para realizar actividades de geolocalización del vehículo objeto del crédito para fines de conocer su ubicación cuando resulte necesario para efecto de dar cumplimiento y/o garantizar el cumplimiento de las obligaciones contractuales contraídas con usted, así como cuando resulte necesario para proteger los intereses y/o el ejercicio de los derechos de MBMOMX.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice25.setFirstLineIndent(20f);
            Paragraph textNotice26 = new Paragraph("13. Cumplir con aquellas legislaciones que resulten aplicables a MBMOMX; 14. Realizar la cesión o transmisión de los derechos y/u obligaciones derivados de la relación jurídica que mantiene con nosotros.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice26.setFirstLineIndent(20f);
            Paragraph textNotice27 = new Paragraph("14. Realizar la cesión o transmisión de los derechos y/u obligaciones derivados de la relación jurídica que mantiene con nosotros.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice27.setFirstLineIndent(20f);
            Paragraph textNotice28 = new Paragraph("Avales u Obligados Solidarios:").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice28.setFirstLineIndent(10f);
            Paragraph textNotice29 = new Paragraph("1. Gestionar todas las acciones derivadas de la relación jurídica como aval/avalista en la que se encuentra.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice29.setFirstLineIndent(20f);
            Paragraph textNotice30 = new Paragraph("2. Analizar su capacidad de crédito y, en su caso, darlo de alta como persona física que fungirá como aval/avalista para efectos de garantizar"
                    + "las obligaciones de determinado(s) cliente(s) respaldado(s) por usted de manera consciente y voluntaria.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice30.setFirstLineIndent(20f);
            Paragraph textNotice31 = new Paragraph("3. Dar seguimiento a solicitudes y/o consultas que usted nos haya planteado respecto a productos, servicios, cotizaciones, investigaciones y revisiones en relación con sus quejas o reclamaciones; y/o cualquier otro evento o circunstancia que nos solicite y se pueda derivar de la presente relación jurídica, para lo cual le estaremos contactando únicamente bajo los canales de comunicación proporcionados por usted.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice31.setFirstLineIndent(20f);
            Paragraph textNotice32 = new Paragraph("4. Gestiones de cobranza que incluyen llamadas, envío de correos electrónicos, visitas personales, envío de mensajes SMS, requerimientos y cartas, así como el procesamiento de solicitudes, aclaraciones, investigaciones y facturación de cargos relacionados con los productos y/o servicios ofrecidos y contratados por usted, los cuales podrán ser realizados a través de MBMOMX o a través de terceros contratados directamente por MBMOMX, los cuales realizarán las gestiones de cobranza apegados al presente Aviso de Privacidad, así como a las disposiciones vigentes aplicables a las gestiones de cobranza. Una vez que el deudor principal (o usted) haya cumplido con su obligación contractual, MBMOMX procederá a la cancelación de sus datos, mediante el bloqueo y posterior supresión de dicha información conforme a las disposiciones legales correspondientes.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice32.setFirstLineIndent(20f);
            Paragraph textNotice33 = new Paragraph("5. Realizar investigaciones para combatir el lavado de dinero, fraudes y cualquier otra actividad ilícita.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice33.setFirstLineIndent(20f);
            Paragraph textNotice34 = new Paragraph("6. Realizar actividades tendientes a actualizar su información para cumplir con la calidad de los datos.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice34.setFirstLineIndent(20f);
            Paragraph textNotice35 = new Paragraph("7. Cumplir con aquellas legislaciones que resulten aplicables a MBMOMX.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice35.setFirstLineIndent(20f);
            Paragraph textNotice36 = new Paragraph("Representantes Legales:").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice36.setFirstLineIndent(10f);
            Paragraph textNotice37 = new Paragraph("1. Validar su debida representación.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice37.setFirstLineIndent(20f);
            Paragraph textNotice38 = new Paragraph("2. Contactarlo para cualquier tema relacionado con los servicios y/o productos contratados.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice38.setFirstLineIndent(20f);
            Paragraph textNotice39 = new Paragraph("3. Realizar investigaciones para combatir el lavado de dinero, fraudes y cualquier otra actividad ilícita.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice39.setFirstLineIndent(20f);
            Paragraph textNotice40 = new Paragraph("4. Realizar actividades tendientes a actualizar su información para cumplir con la calidad de los datos.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice40.setFirstLineIndent(20f);
            Paragraph textNotice41 = new Paragraph("5. Cumplir con aquellas legislaciones que resulten aplicables a MBMOMX.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            textNotice41.setFirstLineIndent(20f);
            Paragraph textNotice42 = new Paragraph("Finalidades Secundarias del Tratamiento").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice43 = new Paragraph("Las presentes finalidades tienen como base de legitimación su consentimiento, lo que significa que usted en cualquier momento puede oponerse, o revocar su "
                    + "consentimiento para que sus datos personales sean tratados para cualquiera de las finalidades secundarias aquí señaladas. Las presentes finalidades aplican para "
                    + "solicitantes, clientes, y avales de MBMOMX. Asimismo, le informamos que en caso de que usted otorgue su consentimiento, las presentes finalidades subsistirán incluso "
                    + "cuando usted no mantenga una relación jurídica con nosotros.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice44 = new Paragraph("1. Contactarle para realizar encuestas de satisfacción.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice45 = new Paragraph("2. Contactarle para fines de prospección comercial incluyendo el envío de comunicaciones a través de correos electrónicos, llamadas telefónicas, mensajes cortos y demás medios de comunicación físicos y/o electrónicos.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice46 = new Paragraph("3. Llevar a cabo actividades necesarias para tendientes a promover, mantener, mejorar y evaluar los servicios de MBMOMX.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice47 = new Paragraph("Medio para Oponerse a los Tratamientos de Datos Personales Informados en el Presente Aviso").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice48 = new Paragraph("En caso de que no desee que sus datos personales sean tratados para alguna o todas las finalidades secundarias antes descritas, desde este momento usted nos puede comunicar lo anterior al correo datospersonales@mercedes-benz.com "
                    + "\n\nLa negativa para el uso de sus datos personales para fines adicionales no podrá ser un motivo para negarle los servicios solicitados o dar por terminada la relación establecida con nosotros.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice49 = new Paragraph("Transferencias y Remisiones de Datos Personales").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice50 = new Paragraph("1. Autoridades competentes en los casos legalmente previstos. Para cumplir con disposiciones aplicables. No sujeta a su consentimiento.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice51 = new Paragraph("2. Empresas que forman parte del mismo Grupo societario y/o comercial que MBMOMX, nacionales e internacionales y que tienen políticas vinculantes en materia de protección de datos personales y operan bajo las mismas políticas internas que MBMOMX, incluyendo a nuestra compañía controladora, empresas subsidiarias y afiliadas. Para llevar a cabo cualquiera de las finalidades previstas en el presente Aviso de Privacidad. No sujeta a su consentimiento.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice52 = new Paragraph("3. Sociedades de información crediticia. Para consultar su información, monitorear su comportamiento crediticio e informar sobre el mismo. No sujeta a su consentimiento.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice53 = new Paragraph("4. Terceros con los que se formalicen acuerdos relacionados con la cesión o transmisión de los derechos y/u obligaciones derivados de la relación jurídica que mantiene con nosotros. No sujeta a consentimiento.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice54 = new Paragraph("Remisiones").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice55 = new Paragraph("También, tendrán acceso, en su carácter de encargados del tratamiento, proveedores externos, nacionales e internacionales, para ayudarnos o darnos soporte en la gestión de las finalidades mencionadas en el presente Aviso de Privacidad. Todos nuestros encargados del tratamiento cumplen con lo exigido en la legislación aplicable.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice56 = new Paragraph("Medio y Procedimiento para ejercer cualquiera de sus Derechos ARCO y Revocar su Consentimiento").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice57 = new Paragraph("Usted, o su representante legal, podrá ejercer cualquiera de los derechos ARCO, así como revocar su consentimiento para el tratamiento de sus datos personales enviando un correo electrónico al Comité de Privacidad de MBMOMX a la dirección electrónica datospersonales@mercedes-benz.com Para que el Comité de Privacidad de MBMOMX pueda darle seguimiento a su solicitud, usted, o su representante legal, deberá acreditar correctamente su identidad para lo que es necesario que acompañe su solicitud con copia de alguna identificación oficial y vigente y en su caso, el poder que acredite la representación legal correspondiente.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice58 = new Paragraph("En caso de que la información proporcionada en la solicitud sea errónea o insuficiente, o bien, no se acompañen los documentos de acreditación correspondientes, el Comité de Privacidad de MBMOMX, dentro de los 05 (cinco) días hábiles siguientes a la recepción de la solicitud, podrá requerirle que aporte los elementos o documentos necesarios para dar trámite a la misma. Usted contará con 10 (diez) días hábiles para atender el requerimiento, contados a partir del día siguiente en que lo haya recibido. De no dar respuesta en dicho plazo, se tendrá por no presentada la solicitud correspondiente.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice59 = new Paragraph("El Comité de Privacidad de MBMOMX le comunicará la determinación adoptada, en un plazo máximo de 20 (veinte) días hábiles contados desde la fecha en que se recibió la solicitud, a efecto de que, si resulta procedente, haga efectiva la misma dentro de los 15 (quince) días hábiles siguientes a que se comunique la respuesta. La respuesta se dará vía electrónica a la dirección de correo que se especifique en la Solicitud.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice60 = new Paragraph("Medio y Procedimiento para Limitar el Uso y/o Divulgación de sus Datos Personales").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice61 = new Paragraph("Usted podrá limitar el uso o divulgación de sus datos personales enviando su Solicitud al Comité de Privacidad de MBMOMX al correo electrónico datospersonales@mercedes-benz.com").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice62 = new Paragraph("Los requisitos para acreditar su identidad, así como el procedimiento para atender su solicitud, se regirán por los mismos criterios señalados en el apartado anterior. En caso de que su solicitud resulte procedente, el Comité de Privacidad de MBMOMX lo registrará en el listado de exclusión interno de MBMOMX.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice63 = new Paragraph("Cookies y/o Web Beacons").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice64 = new Paragraph("Nuestras páginas de internet utilizan cookies y/o web beacons para facilitar su navegación.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice65 = new Paragraph("Para más información consulta las políticas de privacidad disponibles en nuestras páginas de internet.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice66 = new Paragraph("Cambios al Presente Aviso de Privacidad").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice67 = new Paragraph("Cualquier cambio o modificación al presente aviso podrá efectuarse por MBMOMX en cualquier momento, conforme lo permite la normatividad, y se dará a conocer previa solicitud al correo datospersonales@mercedes-benz.com").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice68 = new Paragraph("Comité de Privacidad de MBMOMX").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice69 = new Paragraph("Nuestro Comité de Privacidad queda a sus órdenes para proporcionarle cualquier información adicional que requiera o, en su caso, para resolver cualquier duda que pudiera surgirle en materia de privacidad y protección de datos personales, para lo que podrá contactarnos a través del correo electrónico datospersonales@mercedes-benz.com").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice70 = new Paragraph("Otra Información Relevante").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice71 = new Paragraph("MBMOMX trabaja juntamente con sus Distribuidores Autorizados para dar cumplimiento con la legislación aplicable y vigente en materia de protección de Datos Personales y ofrece a todos ellos información relativa a las obligaciones que de ella emanan. Sin embargo, es importante mencionar que los Distribuidores Autorizados son personas morales distintas e independientes a MBMOMX por lo que MBMOMX no es responsable del tratamiento que los Distribuidores Autorizados den a los Datos Personales recabados por éstos. En caso de que usted proporcione su información personal a un distribuidor, éste deberá proporcionarles su Aviso de Privacidad conforme a lo establecido en la legislación aplicable y vigente.").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice72 = new Paragraph("Autoridad en Materia de Protección de Datos Personales").setBold().setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);
            Paragraph textNotice73 = new Paragraph("Le informamos que la autoridad competente en la materia de protección de datos personales es el Instituto Nacional de Transparencia, Acceso a la Información y Protección de Datos Personales (INAI).").setFontSize(10).setTextAlignment(TextAlignment.JUSTIFIED);

            document.add(titlenotice);
            document.add(textNotice);
            document.add(textNotice2);
            document.add(textNotice3);
            document.add(textNotice4);
            document.add(textNotice5);
            document.add(textNotice6);
            document.add(textNotice7);

            document.add(textNotice8);
            document.add(textNotice9);
            document.add(textNotice10);
            document.add(textNotice11);
            document.add(textNotice12);
            document.add(textNotice13);
            document.add(textNotice14);
            document.add(textNotice15);
            document.add(textNotice16);
            document.add(textNotice17);
            document.add(textNotice18);
            document.add(textNotice19);
            document.add(textNotice20);
            document.add(textNotice21);
            document.add(textNotice22);
            document.add(textNotice23);
            document.add(textNotice24);
            document.add(textNotice25);
            document.add(textNotice26);
            document.add(textNotice27);
            document.add(textNotice28);
            document.add(textNotice29);
            document.add(textNotice30);
            document.add(textNotice31);
            document.add(textNotice32);
            document.add(textNotice33);
            document.add(textNotice34);
            document.add(textNotice35);
            document.add(textNotice36);
            document.add(textNotice37);
            document.add(textNotice38);
            document.add(textNotice39);
            document.add(textNotice40);
            document.add(textNotice41);
            document.add(textNotice42);
            document.add(textNotice43);
            document.add(textNotice44);
            document.add(textNotice45);
            document.add(textNotice46);
            document.add(textNotice47);
            document.add(textNotice48);
            document.add(textNotice49);
            document.add(textNotice50);
            document.add(textNotice51);
            document.add(textNotice52);
            document.add(textNotice53);
            document.add(textNotice54);
            document.add(textNotice55);
            document.add(textNotice56);
            document.add(textNotice57);
            document.add(textNotice58);
            document.add(textNotice59);
            document.add(textNotice60);
            document.add(textNotice61);
            document.add(textNotice62);
            document.add(textNotice63);
            document.add(textNotice64);
            document.add(textNotice65);
            document.add(textNotice66);
            document.add(textNotice67);
            document.add(textNotice68);
            document.add(textNotice69);
            document.add(textNotice70);
            document.add(textNotice71);
            document.add(textNotice72);
            document.add(textNotice73);
            document.add(titlenotice);
            document.add(textNotice);
            document.close();
            String pdf = getBase64FromPath(dir);
            dir.delete();
            return pdf;
        } catch (Throwable e) {
            Log.e(TAG, "Error createPdfPrivacy()", e);
            return "";
        }
    }

    protected static class Header implements IEventHandler {

        String header;

        public Header(String header) {
            this.header = header;
        }

        @Override
        public void handleEvent(Event event) {
            //Retrieve document and
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdf = docEvent.getDocument();

            PdfPage page = docEvent.getPage();

            Rectangle pageSize = page.getPageSize();
            pageSize.setX(100);
            pageSize.setY(680);
            pageSize.setWidth(400);
            pageSize.setHeight(150);
            PdfCanvas pdfCanvas = new PdfCanvas(
                    page.getLastContentStream(), page.getResources(), pdf);
            Canvas canvas = new Canvas(pdfCanvas, pdf, pageSize);
            canvas.setFixedPosition(110, 0, 100);

            String imageLogoTop = "iVBORw0KGgoAAAANSUhEUgAABdwAAACPCAYAAADgFxmaAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyhpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDcuMi1jMDAwIDc5LjU2NmViYzViNCwgMjAyMi8wNS8wOS0wODoyNTo1NSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIDIzLjQgKE1hY2ludG9zaCkiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6NDFCOTZBNzkzMTE4MTFFREIyNDZDNzQ5MEJGOEY5NEYiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6NDFCOTZBN0EzMTE4MTFFREIyNDZDNzQ5MEJGOEY5NEYiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo0MUI5NkE3NzMxMTgxMUVEQjI0NkM3NDkwQkY4Rjk0RiIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo0MUI5NkE3ODMxMTgxMUVEQjI0NkM3NDkwQkY4Rjk0RiIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PqJOWksAAGVqSURBVHja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";
            byte[] decodedString;
            Bitmap decodedByte;
            ByteArrayOutputStream stream3;
            decodedString = Base64.decode(imageLogoTop, Base64.DEFAULT);
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            stream3 = new ByteArrayOutputStream();
            decodedByte.compress(Bitmap.CompressFormat.PNG, 100, stream3);

            ImageData imageData = ImageDataFactory.create(stream3.toByteArray());
            Image image = new Image(imageData);
            canvas.add(image);
            //Write text at position
            //canvas.showTextAligned(header,pageSize.getWidth() / 2, pageSize.getTop() - 30, TextAlignment.CENTER);
        }

    }
}
