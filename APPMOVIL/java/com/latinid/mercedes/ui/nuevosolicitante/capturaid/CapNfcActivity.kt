package com.latinid.mercedes.ui.nuevosolicitante.capturaid

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.acuant.acuantcamera.camera.AcuantCameraActivity
import com.acuant.acuantcamera.camera.AcuantCameraOptions
import com.acuant.acuantcamera.constant.ACUANT_EXTRA_CAMERA_OPTIONS
import com.acuant.acuantcamera.constant.ACUANT_EXTRA_MRZ_RESULT
import com.acuant.acuantcamera.helper.MrzResult
import com.acuant.acuantcamera.initializer.MrzCameraInitializer
import com.acuant.acuantcommon.background.AcuantAsync
import com.acuant.acuantcommon.exception.AcuantException
import com.acuant.acuantcommon.initializer.AcuantInitializer
import com.acuant.acuantcommon.initializer.IAcuantPackageCallback
import com.acuant.acuantcommon.model.AcuantError
import com.acuant.acuantechipreader.AcuantEchipReader
import com.acuant.acuantechipreader.echipreader.NfcTagReadingListener
import com.acuant.acuantechipreader.initializer.EchipInitializer
import com.acuant.acuantechipreader.model.NfcData
import com.acuant.acuantimagepreparation.initializer.ImageProcessorInitializer
import com.google.gson.Gson
import com.latinid.mercedes.DatosRecolectados
import com.latinid.mercedes.Main2Activity
import com.latinid.mercedes.R
import com.latinid.mercedes.databinding.ActivityCapNfcBinding
import com.latinid.mercedes.databinding.ActivityMain2Binding
import com.latinid.mercedes.db.DataBase
import com.latinid.mercedes.model.local.IdentificacionModel
import com.latinid.mercedes.model.local.PersonaModel
import com.latinid.mercedes.util.OperacionesUtiles
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

class CapNfcActivity : AppCompatActivity(), NfcTagReadingListener {

    private var nfcAdapter: NfcAdapter? = null
    private val executor2 = Executors.newSingleThreadExecutor()
    private var binding: ActivityCapNfcBinding? = null
    private var extras: Extras = Extras()
    private lateinit var loadingAnimation: AnimationDrawable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCapNfcBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())
        imagenMercedesActionBar()
        //setSupportActionBar(binding!!.toolbarr)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this.applicationContext)

        binding!!.gifMercedes.setBackgroundResource(R.drawable.loadinggeneral)
        loadingAnimation = binding!!.gifMercedes.background as AnimationDrawable
        loadingAnimation.start()
       // setSupportActionBar(binding!!.appBarMain.toolbar)
    }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter != null) {
            ensureSensorIsOn()
            AcuantEchipReader.listenNfc(this, nfcAdapter!!)
        }
    }

    private fun ensureSensorIsOn() {
        if (this.nfcAdapter != null && !this.nfcAdapter!!.isEnabled) {
            // Alert the user that NFC is off
            AlertDialog.Builder(this)
                .setTitle("NFC Sensor Turned Off")
                .setMessage("In order to use this application, the NFC sensor "
                        + "must be turned on. Do you wish to turn it on?")
                .setPositiveButton("Go to Settings") { _, _ ->
                    // Send the user to the settings page and hope they turn it on
                    startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                }
                .setNegativeButton("Do Nothing") { _, _ ->
                    // Do nothing
                }
                .show()
        } else if (this.nfcAdapter == null) {
            //DialogUtils.showDialog(this, "An NFC Reader is required for this step.")
            println("Apagado")
        }
    }

    private fun imagenMercedesActionBar() {
        val actionBar = supportActionBar
        actionBar!!.setDisplayShowCustomEnabled(true)
        val inflater = this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.image_custome_actionbar_nfc, null)
        actionBar.customView = view
    }

    override fun onError(error: AcuantError) {
        println(error.errorDescription)
        println(error.errorCode)
        println(error.additionalDetails)
        binding!!.gifMercedes.visibility = View.INVISIBLE
        binding!!.textoMensaje.text = "Retire el pasaporte y vuelva a colocarlo e intente no moverlo"

    }

    override fun tagReadStatus(status: String) {
        binding!!.gifMercedes.visibility = View.VISIBLE
        binding!!.textoMensaje.text = "Presione el pasaporte con la mano para que no se mueva"
    }


    override fun tagReadSucceeded(nfcData: NfcData) {
        println("Termine")
        executor2.execute{
            var identificacion: IdentificacionModel = IdentificacionModel()
            var persona: PersonaModel = PersonaModel()

            if (nfcData.passportDataValid) {
                identificacion.resultado = 1
            } else {
                identificacion.resultado = 2
            }

            if (nfcData.documentType.equals("P")) {
                identificacion.tipoDeIdentificacion = "PASAPORTE CON CHIP"
            }

            identificacion.numeroDeDocumento = nfcData.documentNumber
            identificacion.fechaDeVigencia = extras.formatoFechaPasaporteChip(nfcData.documentExpiryDate)
            persona.estado = nfcData.issuingAuthority
            persona.nombre = nfcData.firstName
            persona.paterno = extras.getApellidoPassport(nfcData.lastName, 0)
            persona.materno = extras.getApellidoPassport(nfcData.lastName, 1)
            persona.sexo = nfcData.gender
            persona.fechaDeNacimiento = extras.formatoFechaPasaporteChip(nfcData.dateOfBirth)

            var img = nfcData.image
            val byteArrayOutputStream = ByteArrayOutputStream()
            img!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            val foto: String = Base64.encodeToString(byteArray, Base64.DEFAULT)
            identificacion.fotoRecorteB64 = foto
            println(persona)
            println(identificacion)
            runOnUiThread{
                binding?.gifMercedes!!.visibility = View.INVISIBLE
            }
            var pdf: String = ""
            pdf = UtilsPDF.generarPDFIdentificacionConChip(filesDir,this,persona,identificacion)
            identificacion.documentoPdfBase64 = pdf

            OperacionesUtiles.generarTXTJson(
                this,
                "pdfResuladosChip.txt",
                pdf
            )
            identificacion.documentoPdfBase64 = pdf

            val jsonIdent: String = Gson().toJson(identificacion)
            val jsonPers: String = Gson().toJson(persona)
            val identPath = OperacionesUtiles.writeToFile(
               filesDir,
                jsonIdent
            )
            val persPath = OperacionesUtiles.writeToFile(
                filesDir,
                jsonPers
            )
            println(DatosRecolectados.activeEnrollment)
            var activeEnrollment = DatosRecolectados.activeEnrollment;
            activeEnrollment.ident_id = "1"
            activeEnrollment.state_id = "2"
            activeEnrollment.json_ident = identPath
            activeEnrollment.json_pers = persPath
            val dataBase = DataBase(this)
            dataBase.open()
            dataBase.updateEnroll(activeEnrollment, activeEnrollment.enroll_id)
            dataBase.close()
            DatosRecolectados.activeEnrollment = activeEnrollment
            DatosRecolectados.nfcFinish = true
            finish()
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        println("readNfcTag2")

        AcuantEchipReader.readNfcTag(this, intent, "N00027070".trim { it <= ' ' }, "900523",
            "241010", performOzoneAuthentication = true, tagListener = this)
       // DatosRecolectados.tempIntent = intent;
    }

}