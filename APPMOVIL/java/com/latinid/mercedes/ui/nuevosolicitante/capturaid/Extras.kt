package com.latinid.mercedes.ui.nuevosolicitante.capturaid

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.acuant.acuantcommon.helper.CredentialHelper
import com.acuant.acuantcommon.model.AcuantError
import com.acuant.acuantcommon.model.Credential
import com.acuant.acuantfacematchsdk.AcuantFaceMatch
import com.acuant.acuantfacematchsdk.model.FacialMatchData
import com.acuant.acuantfacematchsdk.model.FacialMatchResult
import com.acuant.acuantfacematchsdk.service.FacialMatchListener

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Extras {

    fun readFromFile(fileUri: String?): ByteArray {
        val file = File(fileUri)
        val bytes = ByteArray(file.length().toInt())
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bytes
    }

    fun formatoFechaPasaporteChip(string: String?): String? {
        return try {
            val date = SimpleDateFormat("yyMMdd").parse(string)
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            formatter.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    fun getComparacionFacial(identificacion: String?, selfie: String?) {
        val facialMatchData = FacialMatchData()
        val decodedString = Base64.decode(identificacion, Base64.NO_WRAP)
        val image1 = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        val decodedString2 = Base64.decode(selfie, Base64.NO_WRAP)
        val image2 = BitmapFactory.decodeByteArray(decodedString2, 0, decodedString2.size)
        facialMatchData.faceImageOne = image1;
        facialMatchData.faceImageTwo = image2;
        AcuantFaceMatch.processFacialMatch(facialMatchData, object : FacialMatchListener {
           /* override fun facialMatchFinished(p0: FacialMatchResult?) {
                println("resultado: " + p0!!.score)
                println("resultado: " + p0!!.isMatch)
                //p0.score Number

                if (p0!!.isMatch == true) {
                    DatosRecolectados.persona.comparacionFacial = "75.0"
                } else {
                    DatosRecolectados.persona.comparacionFacial = "0.0"
                }
            }*/

            override fun facialMatchFinished(result: FacialMatchResult) {
                TODO("Not yet implemented")
                println("resultado: " + result!!.score)
                println("resultado: " + result!!.isMatch)
                //p0.score Number

                if (result!!.isMatch == true) {
                    //DatosRecolectados.persona.comparacionFacial = "75.0"
                } else {
                    //DatosRecolectados.persona.comparacionFacial = "0.0"
                }
            }

            override fun onError(error: AcuantError) {
                TODO("Not yet implemented")

            }
        })
    }


    fun loadAssureIDImage(url: String?, credential: Credential?): Bitmap? {
        try {
            if (url != null && credential != null) {
                val c = URL(url).openConnection() as HttpURLConnection
                val auth = CredentialHelper.getAcuantAuthHeader(credential)
                c.setRequestProperty("Authorization", auth)
                c.useCaches = false
                c.connect()
                val img = BitmapFactory.decodeStream(c.inputStream)
                c.disconnect()
                return img
            }
            return null
        }catch (e: Exception){
            Log.e("Error : ", e.toString())
        }
        return null
    }

    fun dateFormatAccuant(fecha: String?): String? {
        var f = ""
        if (fecha == null) {
        } else {
            val array = fecha.split('(').toTypedArray()
            if(array.size >= 2) {
                f = array[1].substring(0, array[1].length - 2)
                var date = Date(f.toLong())
                val c = Calendar.getInstance()
                c.time = date
                c.add(Calendar.DATE, 1)
                date = c.time
                val format = SimpleDateFormat("yyyy-MM-dd", Locale("es", "MX"))
                //val format = SimpleDateFormat("dd-MM-yyyy", Locale("es", "MX"))
                f = format.format(date)
            }else{
                f = array[0]
            }
        }
        return f
    }

    fun getDate(fecha: String?, flag: Int?): String? {
        var f = ""
        if (fecha == null) {
        } else {
            val array = fecha.split(' ').toTypedArray()
            if (flag == 2){
                if(array.size > 2){
                    f = array[1]+' '+array[2]
                }else{
                    f = array[1]
                }
            }else if (flag == 1){
                f = array[1]
            }else{
                f = array[0]
            }
        }
        return  f;
    }

    fun getApellidoPassport(apellidos: String?, flag: Int?): String? {
        var f = ""
        if (apellidos == null) {
        } else {
            val array = apellidos.split(' ').toTypedArray()
            if (flag == 1){
                if(array.size == 3){
                    f = array[1] + " "+ array[2]
                }else if(array.size == 4){
                    f = array[1] + " "+ array[2]+ " "+ array[3]
                }else{
                    f = array[1]
                }
            }else{
                f = array[0]
            }
        }
        return  f;
    }

}