package com.latinid.mercedes.ui.nuevosolicitante.capturaid;

import static com.latinid.mercedes.util.OperacionesUtiles.dateEnrollment;
import static com.latinid.mercedes.util.OperacionesUtiles.getBase64FromPath;
import static java.lang.Integer.valueOf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;

import com.acuant.acuantdocumentprocessing.resultmodel.Action;
import com.acuant.acuantdocumentprocessing.resultmodel.IDResult;
import com.latinid.mercedes.DatosRecolectados;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import com.itextpdf.kernel.*;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.styledxmlparser.jsoup.helper.StringUtil;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.Event;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
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
import com.latinid.mercedes.model.local.IdentificacionModel;
import com.latinid.mercedes.model.local.PersonaModel;


public class UtilsPDF {

    public static String generarPDFIdentificacion(File filesDir, Context context, PersonaModel personaModel, IdentificacionModel identificacionModel) {
        try {
            IDResult result = identificacionModel.getResult();
            String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
            //File file = new File(pdfPath, "myPDF.pdf");
            File path = new File(filesDir, "/shared_pdf");
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
            //String imageLogoTop = "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";
            String imageLogoTop = "iVBORw0KGgoAAAANSUhEUgAAAeIAAABVCAYAAACVUefaAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyhpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDcuMi1jMDAwIDc5LjU2NmViYzViNCwgMjAyMi8wNS8wOS0wODoyNTo1NSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIDIzLjQgKE1hY2ludG9zaCkiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6MzUwM0IwOEQxRkU0MTFFREFEQkJGNDg4QkQ5MDk1QUMiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6MzUwM0IwOEUxRkU0MTFFREFEQkJGNDg4QkQ5MDk1QUMiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDozNTAzQjA4QjFGRTQxMUVEQURCQkY0ODhCRDkwOTVBQyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDozNTAzQjA4QzFGRTQxMUVEQURCQkY0ODhCRDkwOTVBQyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Pjp5DowAABLvSURBVHja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";
            byte[] decodedString;
            Bitmap decodedByte;
            ByteArrayOutputStream stream3;
            decodedString = Base64.decode(imageLogoTop, Base64.DEFAULT);
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            stream3 = new ByteArrayOutputStream();
            decodedByte.compress(Bitmap.CompressFormat.PNG, 100, stream3);

            ImageData imageData = ImageDataFactory.create(stream3.toByteArray());
            Image image = new Image(imageData);
            Paragraph reporteIdentificacion = new Paragraph("Reporte Identificación").setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER);

            Paragraph origen = new Paragraph("Origen: Tableta"+" " + Build.MODEL).setTextAlignment(TextAlignment.RIGHT).setFontSize(10);
            Paragraph fechaCaptura = new Paragraph("Fecha de captura:"+" " + dateEnrollment()).setFontSize(10).setTextAlignment(TextAlignment.RIGHT);
            Paragraph resultadoGeneral = new Paragraph();
            Paragraph nota = new Paragraph();
            switch (result.result) {
                case "1":
                    resultadoGeneral = new Paragraph("Resultado General:"+" Passed\n").setBold().setFontSize(12).setTextAlignment(TextAlignment.LEFT);
                    nota = new Paragraph("Nota:  Probabilidad de falsificación MUY BAJA, requiere una revisión visual rápida").setBold().setFontSize(10).setTextAlignment(TextAlignment.LEFT);
                    break;
                case "2":
                case "3":
                    if (result.result.equals("2")) {
                        resultadoGeneral = new Paragraph("Resultado General:"+" Failed\n").setBold().setFontSize(12).setTextAlignment(TextAlignment.LEFT);
                    } else {
                        resultadoGeneral = new Paragraph("Resultado General:"+" Skipped\n").setBold().setFontSize(12).setTextAlignment(TextAlignment.LEFT);
                    }
                    nota = new Paragraph("Nota:  Probabilidad de falsificación ALTA, requiere una revisión detallada de la identificación").setBold().setFontSize(10).setTextAlignment(TextAlignment.LEFT);
                    break;
                case "4":
                case "5":
                    if (result.result.equals("4")) {
                        resultadoGeneral = new Paragraph("Resultado General:"+" Caution\n").setBold().setFontSize(12).setTextAlignment(TextAlignment.LEFT);
                    } else {
                        resultadoGeneral = new Paragraph("Resultado General:"+" Attention\n").setBold().setFontSize(12).setTextAlignment(TextAlignment.LEFT);
                    }
                    nota = new Paragraph("Nota:  Probabilidad de falsificación ALTA, requiere una revisión detallada de la identificación").setBold().setFontSize(10).setTextAlignment(TextAlignment.LEFT);
                    break;
                default:
            }
            Color colorBlack = new DeviceRgb(0x00, 0x00, 0x00);
            Color colorWhite = new DeviceRgb(0xFF, 0xFF, 0xFF);
            resultadoGeneral.setBackgroundColor(colorBlack);
            resultadoGeneral.setFontColor(colorWhite);
            Paragraph tipoDocumento = new Paragraph("Tipo de documento:"+" " + result.classification.type.name + "\n").setTextAlignment(TextAlignment.LEFT).setFontSize(10);
            Paragraph serie = new Paragraph("Serie:"+" " + result.classification.type.issue + "\n").setTextAlignment(TextAlignment.LEFT).setFontSize(10);
            Paragraph tituloDatos = new Paragraph("Datos del documento\n").setBold().setFontSize(15).setTextAlignment(TextAlignment.CENTER);
            ArrayList<TablaPDFModel> infotabla = CrearTablaToPDF.createArrays(context,personaModel,identificacionModel);
            float[] widthTablaInfo = {100f, 100f};
            Table table = new Table(widthTablaInfo);
            table.setHorizontalAlignment(HorizontalAlignment.CENTER);
            Color lineColor = new DeviceRgb(0xEB, 0xEB, 0xEB);
            int contTables = 1;
            for (TablaPDFModel tablaPDFModel : infotabla) {
                System.out.println(tablaPDFModel.getTitulo()+" "+tablaPDFModel.getValor());
                try {
                    if((contTables %2)!=0){
                        table.addCell(new Cell().add(new Paragraph(tablaPDFModel.getTitulo() + ":")).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f)));
                        table.addCell(new Cell().add(new Paragraph(tablaPDFModel.getValor())).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f)));
                    }else{
                        table.addCell(new Cell().add(new Paragraph(tablaPDFModel.getTitulo() + ":")).setBackgroundColor(lineColor).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f)));
                        table.addCell(new Cell().add(new Paragraph(tablaPDFModel.getValor())).setBackgroundColor(lineColor).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f)));
                    }
                }catch (Throwable e){
                    e.printStackTrace();
                }
                contTables++;
            }

            Paragraph tituloAnalisis = new Paragraph("\n\n\n\n\nAnálisis del documento:\n").setBold().setFontSize(15).setTextAlignment(TextAlignment.CENTER);
            float[] widthTablaAlertas = {400f, 200f, 400f};
            Table tableAlertas = new Table(widthTablaAlertas);
            tableAlertas.setHorizontalAlignment(HorizontalAlignment.CENTER);
            contTables = 1;
            for (Action action : result.alerts.actions) {
                if((contTables%2)!=0){
                    tableAlertas.addCell(new Cell().add(new Paragraph(action.description)).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                    int r = valueOf(action.result);
                    switch (r) {
                        case 1:
                            tableAlertas.addCell(new Cell().add(new Paragraph("Passed")).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                            break;
                        case 2:
                            tableAlertas.addCell(new Cell().add(new Paragraph("Failed")).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                            break;
                        case 3:
                            tableAlertas.addCell(new Cell().add(new Paragraph("Skipped")).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                            break;
                        case 4:
                            tableAlertas.addCell(new Cell().add(new Paragraph("Caution")).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                            break;
                        case 5:
                            tableAlertas.addCell(new Cell().add(new Paragraph("Attention")).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                            break;
                    }
                    tableAlertas.addCell(new Cell().add(new Paragraph(action.actions)).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                }else{
                    tableAlertas.addCell(new Cell().add(new Paragraph(action.description)).setBackgroundColor(lineColor).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                    int r = valueOf(action.result);
                    switch (r) {
                        case 1:
                            tableAlertas.addCell(new Cell().add(new Paragraph("Passed")).setBackgroundColor(lineColor).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                            break;
                        case 2:
                            tableAlertas.addCell(new Cell().add(new Paragraph("Failed")).setBackgroundColor(lineColor).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                            break;
                        case 3:
                            tableAlertas.addCell(new Cell().add(new Paragraph("Skipped")).setBackgroundColor(lineColor).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                            break;
                        case 4:
                            tableAlertas.addCell(new Cell().add(new Paragraph("Caution")).setBackgroundColor(lineColor).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                            break;
                        case 5:
                            tableAlertas.addCell(new Cell().add(new Paragraph("Attention")).setBackgroundColor(lineColor).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                            break;
                    }
                    tableAlertas.addCell(new Cell().add(new Paragraph(action.actions)).setBackgroundColor(lineColor).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                }
                contTables++;
            }

            //document.add(image);
            document.add(reporteIdentificacion);
            document.add(origen);
            document.add(fechaCaptura);
            document.add(resultadoGeneral);
            document.add(nota);
            document.add(tipoDocumento);
            document.add(serie);
            document.add(tituloDatos);
            document.add(table);
            document.add(tituloAnalisis);
            document.add(tableAlertas);
            document.close();
            String pdf = getBase64FromPath(dir);
            dir.delete();
            return pdf;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generarPDFIdentificacionConChip(File filesDir, Context context, PersonaModel personaModel, IdentificacionModel identificacionModel) {
        try {
            //IDResult result = DatosRecolectados.identificacion.getResult();
            String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
            //File file = new File(pdfPath, "myPDF.pdf");
            File path = new File(filesDir, "/shared_pdf");
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
            String imageLogoTop = "iVBORw0KGgoAAAANSUhEUgAAAeIAAABVCAYAAACVUefaAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAADCpJREFUeNrsnU+IXVcdx0+0BRO0DElBtKm+UKK0i3SqSGot5D3qyiCZ0GI3lc4sImgWk8GNCDozdKELJTOLVGgWeaFuLJYZkbhqeTcQa7qoGbKwaCh5oSkomDBUaBa68HzfnDtz5+b+f/fO3Hff5wOXmby595zzzr253/M7v9/5HWMAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARok9dEEzOXXpdM/+aCedc/74uT2B8yftj7M1aHrfHrfc75491mw717mjANBUHqALwDGRJty7wLwbJEicV+1x0YryGrcKAJrEp+gCGAFa9jhjj2uy9J31DgCAEAPsAm0nyGfoCgBAiAF2D/zGAIAQA+wSS+ePn+vSDQCAEAPsPKtWhOfoBgBoCkRNQx4UsVz1lHDLHXH1z3AbAAAhhnFlzlqjXtWVnLp0WkuppuzxstlaUqUBwAxrigEAIQaoGCe2XR1WlKftzwtOhFlDDACNAx8x1F2UJcgd+3OV3gAAhBhgd8TYoxcAACEGAACA0sFHDLvCY5PPtuyP6QqrkJ957YO1K15KO9pmY7MLP22mpsDn7HV97lLtnplexMcX7b3qVlyvnhHlPW8HnpFFWy8xC4AQw0jTci+3ql/e64EXZz/0N4lv+OWuaO22/dshez4R2vWiHfHZ5R0YMPKMQKUwNQ1NZ8JZ3tfsi3Mq4oUadw0bSwDPCCDEACUL8oqzgn2SrBksHeA5AIQYoAJWAr93zUa2rjBd/H8gnP856lnw0uIPALKCjxjqhl56w+SS9qcMj5lon2JLwTd6icq/Z39ftJ+dMFtpNfv2WEyrxAXwtAt+v7W8wWDOkp808ek/1wNlrye0uZVQRt9dv5azbX6fF+mPdSdqazFlL6RcfyzlnEjBzNJme51frp7Hl7M+I86v7N+vsmEAgBADVM56CS+a1cBLPCogTC9fL8JC9rllj4UYIZs18X7DPOIl4VlOivh1YnEmJAJZytZ3G0QTF2mzvabvrl9IOW/alT1ZQn9IkJftsRQaSKQF9GUZEHkF76H//XtZnpEy+yPr9wGEGKDueKaEyGwniBfKEOAAellfsGVLZE+GrVhnAa/kEeCwONky5gter2vm7fWaKejEtO1CyYIz4e7VrC2/U4VrwJarNk9X8aDZsldKfj5gjMBHDJAuwr0KX7JtJ2rhOouKcFhQhx0srESIcK9Cq2/Q3256t8z7uFChCJ9FhAEhBojmRMzneaytM6b6qcapUDT3VAkiWtpAIdS2s04sq0Tlz5Zc5mwVDQ24DwAKw9Q01I1WhgCdNL7sLM1WCUKc9AKXL/pyxvIkZk8mWGVTgXJejjlHU8QKEtoW3OSsx7bZvm1kFJ49LtqjLz98IGBp0g1a2kltC9QT1a6u64s8y31art6pmP4SncBnUb7arvtOcfSdX3iixHZHtfO+el27vJL/f/R5RSDEAJULsak245aXM2J5IkbIOzmzKnlONBWQdK3gC/ipqDrd95GgdBN8lYvh4CtXlueOJSdYUWL3ZODeRDFjyyq6O1Y3yXcbDNyz50WdcitDGtMq2m1S+qRDmlTIClPTME7ohX2yhHL+UDS14RBBSF7GOpeTBgIpbfNirPu0qehhk17c2qXnoYxkHa2YvkSEAYsYoICg1JmdEqsi4nTWLT8q26oEQIgBGob8eQo2mqErSu9XAECIoSF4ZvgddZKCtaat9TbHrjkAgBADRHM5LaNTVhKCgCYN2YkAoCYQrAVNJm5ZS5uuAQAsYhhFeqcunc57zeL54+cW6LpGo/zQc3QDABYx1I/V3RJht3b0bMyfh/UPnyiSglEJNEpIVlJHzuh7uQQh40Y/5l6TbQuwiGHX0XrUItHJx4YUKyWfkEgmRfJ6OcqTaIcFRmXfdDso6biVsV1tE7+Ean1E7mkcSsIy73Z+6sf0SbeB62vjvo+WdM26Z63MpWdsg4gQA2QWr5PWGi4iLm1TrQ/Xy5lUY9nEZ/oqc8/Z1brfVLd/c9ckb57QThkANUqIXarQ9ZgBVstUs9EEQtwwmJqGKpAI1/GFu17ASl8y+XJTF2FmhCzFuR3oj1FjkS4AhBhq9aK2IlzHEbvadCiv4Ln1xh0nyJUMDGwd3RGyAP3+wCrb6pMlxBgQYqgLXSvCSzVqj7+7jhLwd4bID73uooIPOUEe1npdcy/uQ6MkwqH+6DhB7hp2BDJu7XtZzweMGfiIm0ve6cN+CaP6pR2uL+57S3DXM/qCF2Os56iXrdosQZ4LbCEo2hkt8kH7MgwItP75cpY25ejXrOIQVXc/pj88s7WrVLA/gr8XacPQ96rkPslUb8Lz0TLl5dNmJgIAAAAAAAAAGsIeugAAojh16bSmV68Ztincbfr2uEiGuuZCsBYAxLGCCNcC3YN5OzC6QFcgxAAwPtaw0oO26YlaMW3vyzTd0DyImgaAsAjrZR+bK/nRhw6afQ/uNR9+fNt88t97pdVbVbkqU2X7DFO+yvrmI08Pft65d9e8c/uq+eqBw+Yr+w9vnvPO7Xft3+5UdXuU5a3LU4oQA0BzRVhLbuI2yzDfPfydwSEkZr9+d3kgbEObeke+b545eHSz3Feu/LI0MZMI//jo7Oa/1ea/37lRuJ2Tnz+y7bMDe/dv9on4x90bVQpxi6e0eTA1DQC+CCs4S37h2F2UZP0FrUMJXNDaLMJzrc6mCPvlPrxvfy37SG0LizAAFjEAlEUvr8UlYZo+8tLAyiwy3Svr8sUnns98/oG9B2JFWvWXYZ0n8dbNnjn4uY0pdFm+moYODiKKtl2UPSUPCDEAjJY1rIjcQjtJySL+0dd/YH51dTn3dZrqTUNWuKxm+WHDFmmcoL3d9wb+2yiC09Rp5Uhs37rpbU41+35sofakTUHnbbuE+C8fXTV/vPEnRBkhBoAxEuFpM+R2fRIaiWr3+uuZLWmJd5o4BX3S+QT+pUGbsrYnrhwdCs76zV9fy+1XDvq988wwSLhV50+9nyPGYwI+YoDxFmFZwaWsT5XoSESyIBFO86/KmswrwuH2DOu/9sUxi+UebnteEQ7XOcx3ByxiABgNEVZQVm+YMjSFGhQM+Xs1Xbv2r+uJlmJwuY+sPpUT9hUHzxEqU+fF+YHlg/32ofa2wYB80JpeDpIWNa1yHn3okcH38oVcgwZ9nmeGIMir772W2Ce+eP/wa1uzBGUMIgAhBoD6i/DEsEIswQgu6ZHQxi1reubg0/dZim+8/2am5T4SsyT8AUBWqzypHB3+1LTPMJHc9/53b1vEeVLd+x5EgBFiABgHtFZ4soyC5IcNLmPy/b+vXPnFNh+nhEi+2yBv93ubSTGaTNYAMRhP8BEDjJ81rKxZ02WVJ7F99b3z20RXU7kSn+A0q6Zdg8h6/d3f3uSGAEJMFwCMlQi3TULmrKJoSlXT0UEkvt97/IXNtcbBCGlNWw8T0QzQJB54bPJbLUPaNIDG88VvHJA/uLIdfDbE9bfbpp/lD5b/OCjCspx1Xt6lORL2tIQdWQKq1J4sS5HKDJZS4o+yBh72nd3maW6YEJuNKap5ugKgwf/RP/Npc/Dow5XXI3+vIobDKSuDSJCyZMAKC/XPnv3J4LoPP/7ovuAuP9I5LJ46/9+f3N32mb9O9/Z/bkdms/KDtMLLqyTe4Wjo+BmC7XX6AxLVqfb47S+4QUSPJ7p5Qty3h0dXADSXx5//UvuzX9i7I3VJaBVhHCVa8gmnLePxUYap8FrgcCRzmoXu16WgsGA0tQYHal9WYdX1eQckz7Xa29q6VefWeQU3iOB93TQh/mDtz13DtloAjeXUpdMLZof3FtZSo/CGELL+8giav7uT/Mx5k2NI4ILLnTQAkEX74hMv5N6oQW0uElRWtO1p2Hd2h6e6eRYxADRXhKdMia6nrJsq+CLqJ8W49s/riSKs84OJN/zp4g1/8uvmjfd/P7Am06xhTQlLcKOsTFnHOlRGltzPfq7p4NS1/q210+E6i7Y9PG0O48keugCgsSKsdcJDJ+2AWrF2/vi5p+iGZsHyJYBmirAfIY0IN4tFugAhBoDRoPC2hlBbutYaXqUbEGIAqL81rIQdU/REY/DscdKK8AxdAQAAAAAAAAAAAAAAAAAAAAAAAAAAAACjx/8FGAAaN5bDymcO4QAAAABJRU5ErkJggg==";
            byte[] decodedString;
            Bitmap decodedByte;
            ByteArrayOutputStream stream3;
            decodedString = Base64.decode(imageLogoTop, Base64.DEFAULT);
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            stream3 = new ByteArrayOutputStream();
            decodedByte.compress(Bitmap.CompressFormat.PNG, 100, stream3);

            ImageData imageData = ImageDataFactory.create(stream3.toByteArray());
            Image image = new Image(imageData);
            Paragraph reporteIdentificacion = new Paragraph("Reporte Identificación").setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER);

            Paragraph origen = new Paragraph("Origen: Tableta"+" " + Build.MODEL).setTextAlignment(TextAlignment.RIGHT).setFontSize(10);
            Paragraph fechaCaptura = new Paragraph("Fecha de captura:"+" " + dateEnrollment()).setFontSize(10).setTextAlignment(TextAlignment.RIGHT);
            Paragraph resultadoGeneral = new Paragraph();
            Paragraph nota = new Paragraph();
            switch (String.valueOf(identificacionModel.getResultado())) {
                case "1":
                    resultadoGeneral = new Paragraph("Resultado General:"+" Passed\n").setBold().setFontSize(12).setTextAlignment(TextAlignment.LEFT);
                    nota = new Paragraph("Nota:  Probabilidad de falsificación MUY BAJA, requiere una revisión visual rápida").setBold().setFontSize(10).setTextAlignment(TextAlignment.LEFT);
                    break;
                case "2":
                case "3":
                    if (String.valueOf(identificacionModel.getResultado()).equals("2")) {
                        resultadoGeneral = new Paragraph("Resultado General:"+" Failed\n").setBold().setFontSize(12).setTextAlignment(TextAlignment.LEFT);
                    } else {
                        resultadoGeneral = new Paragraph("Resultado General:"+" Skipped\n").setBold().setFontSize(12).setTextAlignment(TextAlignment.LEFT);
                    }
                    nota = new Paragraph("Nota:  Probabilidad de falsificación ALTA, requiere una revisión detallada de la identificación").setBold().setFontSize(10).setTextAlignment(TextAlignment.LEFT);
                    break;
                case "4":
                case "5":
                    if (String.valueOf(identificacionModel.getResultado()).equals("4")) {
                        resultadoGeneral = new Paragraph("Resultado General:"+" Caution\n").setBold().setFontSize(12).setTextAlignment(TextAlignment.LEFT);
                    } else {
                        resultadoGeneral = new Paragraph("Resultado General:"+" Attention\n").setBold().setFontSize(12).setTextAlignment(TextAlignment.LEFT);
                    }
                    nota = new Paragraph("Nota:  Probabilidad de falsificación ALTA, requiere una revisión detallada de la identificación").setBold().setFontSize(10).setTextAlignment(TextAlignment.LEFT);
                    break;
                default:
            }
            Paragraph tipoDocumento = new Paragraph("Tipo de documento: "+ "PASAPORTE" + "\n").setTextAlignment(TextAlignment.LEFT).setFontSize(10);
            Paragraph serie = new Paragraph("Serie:"+" " + identificacionModel.getTipoDeIdentificacion() + "\n").setTextAlignment(TextAlignment.LEFT).setFontSize(10);
            Paragraph tituloDatos = new Paragraph("Datos del documento\n").setBold().setFontSize(15).setTextAlignment(TextAlignment.CENTER);
            ArrayList<TablaPDFModel> infotabla = CrearTablaToPDF.createArrays(context,personaModel,identificacionModel);
            float[] widthTablaInfo = {100f, 100f};
            Table table = new Table(widthTablaInfo);
            table.setHorizontalAlignment(HorizontalAlignment.CENTER);

            for (TablaPDFModel tablaPDFModel : infotabla) {
                table.addCell(new Cell().add(new Paragraph(tablaPDFModel.getTitulo() + ":")).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f)));
                table.addCell(new Cell().add(new Paragraph(tablaPDFModel.getValor())).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f)));
            }

            /*Paragraph tituloAnalisis = new Paragraph("\n\n\n\n\nAnálisis del documento:\n").setBold().setFontSize(15).setTextAlignment(TextAlignment.CENTER);
            float[] widthTablaAlertas = {400f, 200f, 400f};
            Table tableAlertas = new Table(widthTablaAlertas);
            tableAlertas.setHorizontalAlignment(HorizontalAlignment.CENTER);
            for (Action action : result.alerts.actions) {
                tableAlertas.addCell(new Cell().add(new Paragraph(action.description)).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
                int r = valueOf(action.result);
                switch (r) {
                    case 1:
                        tableAlertas.addCell(new Cell().add(new Paragraph("Passed")));
                        break;
                    case 2:
                        tableAlertas.addCell(new Cell().add(new Paragraph("Failed")));
                        break;
                    case 3:
                        tableAlertas.addCell(new Cell().add(new Paragraph("Skipped")));
                        break;
                    case 4:
                        tableAlertas.addCell(new Cell().add(new Paragraph("Caution")));
                        break;
                    case 5:
                        tableAlertas.addCell(new Cell().add(new Paragraph("Attention")));
                        break;
                }
                tableAlertas.addCell(new Cell().add(new Paragraph(action.actions)).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1f)));
            }*/

            //document.add(image);
            document.add(reporteIdentificacion);
            document.add(origen);
            document.add(fechaCaptura);
            document.add(resultadoGeneral);
            document.add(nota);
            document.add(tipoDocumento);
            document.add(serie);
            document.add(tituloDatos);
            document.add(table);
            //document.add(tituloAnalisis);
            //document.add(tableAlertas);
            document.close();
            String pdf = getBase64FromPath(dir);
            dir.delete();
            return pdf;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
