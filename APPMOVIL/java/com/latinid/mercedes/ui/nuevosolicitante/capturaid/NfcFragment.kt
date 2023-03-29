package com.latinid.mercedes.ui.nuevosolicitante.capturaid

import android.content.Intent
import android.graphics.Bitmap
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.acuant.acuantcommon.model.AcuantError
import com.acuant.acuantechipreader.AcuantEchipReader
import com.acuant.acuantechipreader.echipreader.NfcTagReadingListener
import com.acuant.acuantechipreader.model.NfcData
import com.latinid.mercedes.DatosRecolectados
import com.latinid.mercedes.Main2Activity
import com.latinid.mercedes.databinding.FragmentNfcBinding
import com.latinid.mercedes.model.local.IdentificacionModel
import com.latinid.mercedes.model.local.PersonaModel
import com.latinid.mercedes.ui.home.LoginFragment
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors


class NfcFragment : Fragment(), NfcTagReadingListener {

    private var binding: FragmentNfcBinding? = null
    private var error: Boolean = true
    private var extras: Extras = Extras()
    private val executor = Executors.newSingleThreadExecutor()
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNfcBinding.inflate(inflater, container, false)
        //tagReadSuccedMain()

        return binding!!.getRoot()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
    }

    override fun onResume() {
        super.onResume()
        DatosRecolectados.capNfc = true
        nfcPressed()
        onNewIntentt()
    }


    override fun tagReadStatus(s: String) {
        println(s)
        println("leyendo")
    }

    override fun tagReadSucceeded(nfcData: NfcData) {
        executor.execute { DatosRecolectados.cardDetails = nfcData }
        println(nfcData.firstName)
        tagReadSuccedMain()
    }

    fun onNewIntentt(){
        Thread{
            do{
                if(DatosRecolectados.tempIntent != null){
                    val docNumber = DatosRecolectados.docNumber.toString().trim { it <= ' ' }
                    val dateOfBirth = DatosRecolectados.dob
                    val dateOfExpiry = DatosRecolectados.doe
                    AcuantEchipReader.readNfcTag(requireActivity(), DatosRecolectados.tempIntent, docNumber, dateOfBirth,
                        dateOfExpiry, performOzoneAuthentication = true, tagListener = this)
                    break
                }
            }while (true)
        }.start()

    }

    fun nfcPressed() {
        try {
            if (nfcAdapter != null) {
                ensureSensorIsOn()
                AcuantEchipReader.listenNfc(requireActivity(), nfcAdapter!!)
                var instString = ""

                //setProgress(true, HelpState.Locate, instString)

            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("NFC error!")
                    .setMessage("NFC is not available for this device")
                    .setPositiveButton("OK") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .show()
            }
        }catch (e: Throwable){
            e.printStackTrace()
        }

    }


    override fun onError(acuantError: AcuantError) {

        println("falle "+acuantError.errorDescription+" "+acuantError.errorCode+" "+acuantError.additionalDetails)


        if (nfcAdapter != null) {
            try {
                // nfcAdapter!!.disableForegroundDispatch(this)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    private fun ensureSensorIsOn() {
        if (this.nfcAdapter != null && !this.nfcAdapter!!.isEnabled) {
            // Alert the user that NFC is off
            AlertDialog.Builder(requireContext())
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

    fun tagReadSuccedMain(){
        executor.execute {
            error = false
            do{
                if(DatosRecolectados.cardDetails != null){
                    break
                }
            }while (true)
            var nfcData = DatosRecolectados.cardDetails;
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
            identificacion.fechaDeVigencia =
                extras.formatoFechaPasaporteChip(nfcData.documentExpiryDate)
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
            //DatosRecolectados.identificacion = identificacion
            //DatosRecolectados.persona = persona



        }
    }










}