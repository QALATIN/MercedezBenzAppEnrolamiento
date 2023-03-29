package com.latinid.mercedes.ui.nuevosolicitante.capturaid

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.acuant.acuantcamera.camera.AcuantCameraActivity
import com.acuant.acuantcamera.camera.AcuantCameraOptions
import com.acuant.acuantcamera.constant.ACUANT_EXTRA_CAMERA_OPTIONS
import com.acuant.acuantcamera.constant.ACUANT_EXTRA_ERROR
import com.acuant.acuantcamera.constant.ACUANT_EXTRA_IMAGE_URL
import com.acuant.acuantcamera.constant.ACUANT_EXTRA_MRZ_RESULT
import com.acuant.acuantcamera.helper.MrzResult
import com.acuant.acuantcamera.initializer.MrzCameraInitializer
import com.acuant.acuantcommon.background.AcuantAsync
import com.acuant.acuantcommon.exception.AcuantException
import com.acuant.acuantcommon.initializer.AcuantInitializer
import com.acuant.acuantcommon.initializer.IAcuantPackageCallback
import com.acuant.acuantcommon.model.AcuantError
import com.acuant.acuantcommon.model.Credential
import com.acuant.acuantdocumentprocessing.AcuantDocumentProcessor
import com.acuant.acuantdocumentprocessing.model.AcuantIdDocumentInstance
import com.acuant.acuantdocumentprocessing.model.EvaluatedImageData
import com.acuant.acuantdocumentprocessing.model.IdInstanceOptions
import com.acuant.acuantdocumentprocessing.resultmodel.Classification
import com.acuant.acuantdocumentprocessing.resultmodel.IDResult
import com.acuant.acuantdocumentprocessing.service.listener.ClassificationListener
import com.acuant.acuantdocumentprocessing.service.listener.CreateIdInstanceListener
import com.acuant.acuantdocumentprocessing.service.listener.GetIdDataListener
import com.acuant.acuantdocumentprocessing.service.listener.UploadImageListener
import com.acuant.acuantechipreader.initializer.EchipInitializer
import com.acuant.acuantimagepreparation.AcuantImagePreparation
import com.acuant.acuantimagepreparation.background.EvaluateImageListener
import com.acuant.acuantimagepreparation.initializer.ImageProcessorInitializer
import com.acuant.acuantimagepreparation.model.AcuantImage
import com.acuant.acuantimagepreparation.model.CroppingData
import com.google.gson.Gson
import com.latinid.mercedes.DatosRecolectados
import com.latinid.mercedes.Main2Activity
import com.latinid.mercedes.R
import com.latinid.mercedes.databinding.FragmentIdentificacionBinding
import com.latinid.mercedes.db.DataBase
import com.latinid.mercedes.model.local.IdentificacionModel
import com.latinid.mercedes.model.local.PersonaModel
import com.latinid.mercedes.ui.nuevosolicitante.selfie.fragments.SelfieAwareFragment
import com.latinid.mercedes.util.OperacionesUtiles
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

class IdentificacionFragment : Fragment(), View.OnClickListener {

    private val TAG = "IdentificacionFrag"
    private var binding: FragmentIdentificacionBinding? = null
    private var isFrontCredential: Boolean = true
    private var documentIdInstance: AcuantIdDocumentInstance? = null
    private var capturedFrontImage: AcuantImage? = null
    private var capturedBackImage: AcuantImage? = null
    private val backgroundTasks = mutableListOf<AcuantAsync>()
    private lateinit var loadingAnimation2: AnimationDrawable
    private lateinit var loadingAnimation: AnimationDrawable
    private val executor = Executors.newFixedThreadPool(5)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIdentificacionBinding.inflate(inflater, container, false)
        cargarComponentes()
        binding!!.textoMensaje.text =
            HtmlCompat.fromHtml(getString(R.string.cap_id_text), HtmlCompat.FROM_HTML_MODE_COMPACT)
        return binding!!.getRoot()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iniciarAcuant()
    }

    override fun onResume() {
        super.onResume()
        if (DatosRecolectados.nfcFinish) {
            DatosRecolectados.nfcFinish = false
            siguienteFregment()
        }
        binding?.iniciarCaptura?.setOnClickListener { view ->

            /* activity!!.runOnUiThread {
                 view.visibility = View.INVISIBLE
             }*/

            //iniciarCamara()
            val menuidFragment = MenuIDFragment()

            menuidFragment.show(parentFragmentManager, "Omisión")
            //iniciarCamara()

            parentFragmentManager.setFragmentResultListener(
                "captura", this
            ) { requestKey: String?, bundle: Bundle? ->
                val result = bundle!!.getString("action")
                activity!!.runOnUiThread {
                    binding!!.iniciarCaptura.visibility = View.GONE
                }
                when (result) {
                    "normal" -> iniciarCamara()
                    "chip" -> {
                        iniciarChip()
                    }
                }
            }
        }

        binding?.buttonBack?.setOnClickListener { view ->
            activity!!.runOnUiThread {
                (activity as Main2Activity?)!!.removerFragment()
            }
        }
    }

    fun openMenu() {
        Thread {
            activity!!.runOnUiThread {
                binding!!.iniciarCaptura.visibility = View.INVISIBLE
                binding!!.gifMercedes.visibility = View.VISIBLE
            }
            iniciarSDKAcuant(object : IAcuantPackageCallback {
                override fun onInitializeSuccess() {
                    activity!!.runOnUiThread {
                        binding!!.gifMercedes.visibility = View.GONE
                        //binding!!.iniciarCaptura.visibility = View.VISIBLE
                        //iniciarCamara()
                        menu()
                    }

                }

                override fun onInitializeFailed(error: List<AcuantError>) {
                    val alert = AlertDialog.Builder(requireContext())
                    alert.setTitle("Error")
                    alert.setCancelable(false)
                    alert.setMessage("No se pudo iniciar" + "\n" + error[0].errorDescription)
                    alert.setPositiveButton("Aceptar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    alert.show()
                }
            })
        }.start()

    }

    fun menu(){
        activity!!.runOnUiThread {
            val menuidFragment = MenuIDFragment()

            menuidFragment.show(parentFragmentManager, "Omisión")
            //iniciarCamara()

            parentFragmentManager.setFragmentResultListener(
                "captura", this
            ) { requestKey: String?, bundle: Bundle? ->
                val result = bundle!!.getString("action")
                activity!!.runOnUiThread {
                    binding!!.iniciarCaptura.visibility = View.GONE
                }
                when (result) {
                    "normal" -> iniciarCamara()
                    "chip" -> {
                        iniciarChip()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        stopBackgroundTasks()
        try {
            loadingAnimation2.stop()
            loadingAnimation.stop()
        } catch (e: Throwable) {
            Log.e(TAG, "onDestroy", e)
        }

        super.onDestroy()
    }

    fun iniciarCamara() {

        val cameraIntent = Intent(
            requireContext(),
            AcuantCameraActivity::class.java
        )
        cameraIntent.putExtra(
            ACUANT_EXTRA_CAMERA_OPTIONS,
            AcuantCameraOptions
                .DocumentCameraOptionsBuilder()
                .setAllowBox(true)
                .setAutoCapture(true)
                .build()
        )
        docCameraLauncher.launch(cameraIntent)

    }

    fun iniciarChip() {
        val cameraIntent = Intent(
            requireContext(),
            AcuantCameraActivity::class.java
        )
        cameraIntent.putExtra(
            ACUANT_EXTRA_CAMERA_OPTIONS,
            AcuantCameraOptions
                .MrzCameraOptionsBuilder()
                .build()
        )
        mrzCameraLauncher.launch(cameraIntent)
        binding!!.iniciarCaptura.visibility = View.VISIBLE
    }

    private var mrzCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
                val mrzResult = data?.getSerializableExtra(ACUANT_EXTRA_MRZ_RESULT) as MrzResult?

                DatosRecolectados.dob = mrzResult!!.dob
                DatosRecolectados.doe = mrzResult!!.passportExpiration
                DatosRecolectados.docNumber = mrzResult!!.passportNumber
                DatosRecolectados.country = mrzResult!!.country

                //  (activity as Main2Activity?)!!.replaceFragments(NfcFragment::class.java)
                val confirmNFCDataActivity = Intent(requireContext(), CapNfcActivity::class.java)
                this.startActivity(confirmNFCDataActivity)

            } else if (result.resultCode == AppCompatActivity.RESULT_CANCELED) {
                Log.d(TAG, "User canceled mrz capture")
            }
        }

    private var docCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data: Intent? = result.data
                val url = data?.getStringExtra(ACUANT_EXTRA_IMAGE_URL)
                if (url != null) {
                    activity!!.runOnUiThread {
                        binding!!.iniciarCaptura.visibility = View.INVISIBLE
                        binding!!.frontImage.layoutParams.width =
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        binding!!.frontImage.layoutParams.height =
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        binding!!.frontImage.setBackgroundResource(R.drawable.analizando_id)
                        loadingAnimation2 = binding!!.frontImage.background as AnimationDrawable
                        loadingAnimation2.start()
                        binding!!.textoMensaje.setText("Recortando . . .")
                        binding!!.textoMensaje.setVisibility(View.VISIBLE)
                        binding!!.frontImage.setImageBitmap(null)
                    }
                    var task = AcuantImagePreparation.evaluateImage(
                        requireContext(),
                        CroppingData(url),
                        object :
                            EvaluateImageListener {

                            override fun onSuccess(image: AcuantImage) {

                                if (isFrontCredential) {
                                    capturedFrontImage = image
                                    activity!!.runOnUiThread {
                                        binding!!.frontImage.layoutParams.height = 500
                                        binding!!.frontImage.layoutParams.height = 300
                                        binding!!.frontImage.setImageBitmap(image.image)
                                        binding!!.frontImage.setBackgroundResource(0)
                                    }
                                    //Obtener datos ID
                                    uploadIdFront()
                                } else {
                                    ///loadingFinalClose()
                                    capturedBackImage = image
                                    activity!!.runOnUiThread {
                                        binding!!.frontImage.layoutParams.height = 500
                                        binding!!.frontImage.layoutParams.height = 300
                                        binding!!.frontImage.setImageBitmap(image.image)
                                        binding!!.frontImage.setBackgroundResource(0)
                                    }
                                    //Obtener datos ID
                                    uploadIdBack()
                                }
                            }

                            override fun onError(error: AcuantError) {
                                Log.e(TAG, "AcuantError ->$error")
                                val alert = AlertDialog.Builder(requireContext())
                                alert.setTitle("Recorte")
                                alert.setCancelable(false)
                                alert.setMessage("Imposible hacer un recorte fino, por favor mantenga más firme la identificación")
                                alert.setPositiveButton("Aceptar") { dialog, _ ->
                                    dialog.dismiss()
                                    isFrontCredential = true
                                    iniciarCamara()
                                }
                                alert.setNegativeButton("Menú") { dialog, _ ->
                                    dialog.dismiss()
                                    isFrontCredential = true;
                                    openMenu()
                                }
                                /* alert.setNegativeButton("Cancelar") { dialog, _ ->
                                     dialog.dismiss()
                                     (activity as MainActivity?)!!.removerFragment()
                                 }
                                 */
                                alert.show()
                            }
                        })
                    backgroundTasks.add(task)
                } else {
                    Log.e(
                        TAG,
                        "ERROR INUSUAL: ACUANT, devolvió 'RESULT_OK' pero ninguna imagen en la captura"
                    )
                    val alert = AlertDialog.Builder(requireContext())
                    alert.setTitle("Recorte")
                    alert.setCancelable(false)
                    alert.setMessage("Imposible hacer un recorte fino, por favor mantenga más firme la identificación")
                    alert.setPositiveButton("Aceptar") { dilog, _ ->
                        dilog.dismiss()
                        iniciarCamara()
                    }
                    alert.setNegativeButton("Menú") { dialog, _ ->
                        dialog.dismiss()
                        isFrontCredential = true;
                        openMenu()
                    }
                    alert.show()
                }
            } else if (result.resultCode == AppCompatActivity.RESULT_CANCELED) {
                Log.w(TAG, "SE CANCELO LA CAPTURA MANUALMENTE")
                val alert = AlertDialog.Builder(requireContext())
                alert.setTitle("Captura cancelada")
                alert.setMessage("Eligé una opción")
                alert.setCancelable(false)
                alert.setPositiveButton("Recapturar") { dialog, _ ->
                    dialog.dismiss()
                    iniciarCamara()
                }
                alert.setNegativeButton("Menú") { dialog, _ ->
                    dialog.dismiss()
                    isFrontCredential = true;
                    openMenu()
                }
                alert.show()
            } else {
                val data: Intent? = result.data
                val error = data?.getSerializableExtra(ACUANT_EXTRA_ERROR)
                if (error is AcuantError) {
                    Log.e(TAG, "ERROR INUSUAL: ACUANT, en la captura de identificación: ->$error")
                } else {
                    Log.e(
                        TAG,
                        "ERROR INUSUAL: ACUANT, en la captura de identificación: ->${data.toString()}"
                    )
                }
                val alert = AlertDialog.Builder(requireContext())
                alert.setTitle("Aviso")
                alert.setCancelable(false)
                alert.setMessage("Volver a capturar")
                alert.setPositiveButton("Aceptar") { dialog, _ ->
                    dialog.dismiss()
                    iniciarCamara()
                }
                alert.setNegativeButton("Menú") { dialog, _ ->
                    dialog.dismiss()
                    isFrontCredential = true;
                    openMenu()
                }
                alert.show()
            }
        }

    private fun uploadIdFront() {
        activity!!.runOnUiThread {
            binding!!.textoMensaje.setText("Procesando frente . . .")
        }
        val instance = documentIdInstance
        if (instance != null) {
            val frontData = if (capturedFrontImage?.rawBytes != null) {
                EvaluatedImageData(capturedFrontImage!!.rawBytes)
            } else {
                alertaSinprocesar()
                return
            }
            val task = instance.uploadFrontImage(frontData, object : UploadImageListener {
                override fun imageUploaded() {
                    //setProgress(true, "Classifying...")
                    val task = instance.getClassification(object : ClassificationListener {
                        override fun documentClassified(
                            classified: Boolean,
                            classification: Classification
                        ) {
                            //setProgress(false)
                            if (classified) {
                                if (isBackSideRequired(classification)) {
                                    loadingreverso()
                                } else {
                                    getData()
                                }
                            } else {
                                alertaSinprocesar()
                            }
                        }

                        override fun onError(error: AcuantError) {
                            alertaSinprocesar()
                        }
                    })
                    backgroundTasks.add(task)
                }

                override fun onError(error: AcuantError) {
                    alertaSinprocesar()
                }
            })
            backgroundTasks.add(task)
        } else {
            createIdInstance(NextStep.Front)
        }
    }

    private fun alertaSinprocesar() {
        val alert = AlertDialog.Builder(requireContext())
        alert.setTitle("Sin procesar")
        alert.setMessage("Por favor aseguresé de que sea un documento válido para este proceso")
        alert.setCancelable(false)
        alert.setPositiveButton("Recapturar") { dialog, _ ->
            dialog.dismiss()
            iniciarCamara()
        }
        alert.setNegativeButton("Menú") { dialog, _ ->
            dialog.dismiss()
            isFrontCredential = true;
            openMenu()
        }
        alert.show()
    }

    private fun uploadIdBack() {
        activity!!.runOnUiThread {
            binding!!.textoMensaje.setText("Analizando reverso")
        }

        val instance = documentIdInstance
        if (instance != null) {
            val backData = if (capturedBackImage?.rawBytes != null) {
                EvaluatedImageData(capturedBackImage!!.rawBytes)
            } else {
                alertaSinprocesar()
                return
            }

            val task = instance.uploadBackImage(backData, object : UploadImageListener {
                override fun imageUploaded() {
                    getData()
                }

                override fun onError(error: AcuantError) {
                    alertaSinprocesar()
                }
            })
            backgroundTasks.add(task)
        } else {
            createIdInstance(NextStep.Back)
        }
    }

    fun getData() {
        activity!!.runOnUiThread {
            binding!!.textoMensaje.setText("Procesando identificación . . .")
            binding!!.frontImage.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            binding!!.frontImage.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            binding!!.frontImage.setBackgroundResource(R.drawable.analizando_id)
            //binding!!.frontImage.background.setTint(0)
            binding!!.frontImage.setImageBitmap(null)
            loadingAnimation2 = binding!!.frontImage.background as AnimationDrawable
            loadingAnimation2.start()
        }
        val instance = documentIdInstance
        if (instance != null) {
            val task = instance.getData(object : GetIdDataListener {
                override fun processingResultReceived(result: IDResult) {
                    executor.execute {
                        if (result.fields == null || result.fields.dataFieldReferences == null) {
                            showAcuDialog("Unknown error happened.\nCould not extract data")

                        }
                        if ((result as IDResult) != null || result.fields != null) {

                            when (result.classification.type.name) {
                                /*INE/IFE*/
                                "Mexico (MEX) Voter Identification Card" -> {
                                    llenarINE(result)
                                    getPhotosCommon(result)
                                }
                                /*PASAPORTE*/
                                "Mexico (MEX) Passport" -> {
                                    llenarPassport(result)
                                    getPhotoPassport(result)
                                }
                                /*CEDULA PROFESIONAL*/
                                "Mexico (MEX) Professional License" -> {
                                    llenarProfesionalLicence(result)
                                    getPhotosCommon(result)
                                }
                                else -> {
                                    activity!!.runOnUiThread {
                                        val alert = AlertDialog.Builder(requireContext())
                                        alert.setTitle("Clasificación")
                                        alert.setMessage("Sin clasificar, se volvera a capturar")
                                        alert.setCancelable(false)
                                        alert.setPositiveButton("Aceptar") { dialog, _ ->
                                            dialog.dismiss()
                                            isFrontCredential = true
                                            iniciarCamara()
                                        }
                                        alert.setNegativeButton("Menú") { dialog, _ ->
                                            dialog.dismiss()
                                            isFrontCredential = true;
                                            openMenu()
                                        }
                                        alert.show()
                                    }
                                }
                            }

                            if (result.classification.type.name.equals("Mexico (MEX) Voter Identification Card") || result.classification.type.name.equals(
                                    "Mexico (MEX) Passport"
                                )
                            ) {
                                try {
                                    val anio = identificacion.fechaDeEmision.split("-")
                                    if (Integer.valueOf(anio[0]) > 2012) {
                                        if (identificacion.mrz.equals("") || identificacion.mrz == null) {
                                            activity!!.runOnUiThread {
                                                val alert = AlertDialog.Builder(requireContext())
                                                alert.setTitle("Clasificación")
                                                if (result.classification.type.name.equals("Mexico (MEX) Voter Identification Card")) {
                                                    alert.setMessage("Datos inconsistentes, se volvera a capturar el reverso")
                                                } else {
                                                    alert.setMessage("Datos inconsistentes, se volvera a capturar")
                                                }
                                                alert.setNegativeButton("Menú") { dialog, _ ->
                                                    dialog.dismiss()
                                                    isFrontCredential = true;
                                                    openMenu()
                                                }
                                                alert.setCancelable(false)
                                                alert.setPositiveButton("Aceptar") { dialog, _ ->
                                                    dialog.dismiss()
                                                    isFrontCredential = false
                                                    iniciarCamara()
                                                }
                                                alert.show()
                                            }
                                            return@execute
                                        }
                                    }
                                } catch (e: Throwable) {
                                    e.printStackTrace()
                                    activity!!.runOnUiThread {
                                        val alert = AlertDialog.Builder(requireContext())
                                        alert.setTitle("Clasificación")
                                        alert.setMessage("Datos inconsistentes, se volvera a capturar")
                                        alert.setCancelable(false)
                                        alert.setPositiveButton("Aceptar") { dialog, _ ->
                                            dialog.dismiss()
                                            isFrontCredential = false
                                            iniciarCamara()
                                        }
                                        alert.setNegativeButton("Menú") { dialog, _ ->
                                            dialog.dismiss()
                                            isFrontCredential = true;
                                            openMenu()
                                        }
                                        alert.show()
                                    }
                                    return@execute
                                }
                            }

                            var pdf: String = ""
                            if (identificacion.tipoDeIdentificacion.equals("PASAPORTE CON CHIP")) {
                                pdf = UtilsPDF.generarPDFIdentificacionConChip(
                                    requireContext().filesDir,
                                    requireContext(),
                                    persona,
                                    identificacion
                                )
                            } else {
                                pdf = UtilsPDF.generarPDFIdentificacion(
                                    requireContext().filesDir,
                                    requireContext(),
                                    persona,
                                    identificacion
                                )
                            }

                            OperacionesUtiles.generarTXTJson(
                                requireContext(),
                                "pdfResulados.txt",
                                pdf
                            )
                            identificacion.documentoPdfBase64 = pdf


                            val jsonIdent: String = Gson().toJson(identificacion)
                            val jsonPers: String = Gson().toJson(persona)
                            val identPath = OperacionesUtiles.writeToFile(
                                requireActivity().filesDir,
                                jsonIdent
                            )
                            val persPath = OperacionesUtiles.writeToFile(
                                requireActivity().filesDir,
                                jsonPers
                            )
                            println(DatosRecolectados.activeEnrollment)
                            var activeEnrollment = DatosRecolectados.activeEnrollment;
                            activeEnrollment.ident_id = "1"
                            activeEnrollment.state_id = "2"
                            activeEnrollment.json_ident = identPath
                            activeEnrollment.json_pers = persPath
                            val dataBase = DataBase(requireContext())
                            dataBase.open()
                            dataBase.updateEnroll(activeEnrollment, activeEnrollment.enroll_id)
                            dataBase.close()
                            DatosRecolectados.activeEnrollment = activeEnrollment


                            activity!!.runOnUiThread {
                                binding!!.textoMensaje.setText("Finalizado")
                                binding!!.gifMercedes.visibility = View.INVISIBLE
                                binding!!.frontImage.visibility = View.INVISIBLE
                            }

                            siguienteFregment()
                        } else {
                            val alert = AlertDialog.Builder(requireContext())
                            alert.setTitle("Sin clasificar")
                            alert.setMessage("Se volvera a capturar")
                            alert.setCancelable(false)
                            alert.setPositiveButton("Aceptar") { dialog, _ ->
                                dialog.dismiss()
                                isFrontCredential = true
                                iniciarCamara()
                            }
                            alert.setNegativeButton("Menú") { dialog, _ ->
                                dialog.dismiss()
                                isFrontCredential = true;
                                openMenu()
                            }
                            alert.show()
                        }
                    }
                }

                override fun onError(error: AcuantError) {
                    val alert = AlertDialog.Builder(requireContext())
                    alert.setTitle("Sin clasificar")
                    alert.setMessage("Se volvera a capturar")
                    alert.setCancelable(false)
                    alert.setPositiveButton("Aceptar") { dialog, _ ->
                        dialog.dismiss()
                        isFrontCredential = true
                        iniciarCamara()
                    }
                    alert.setNegativeButton("Menú") { dialog, _ ->
                        dialog.dismiss()
                        isFrontCredential = true;
                        openMenu()
                    }
                    alert.show()
                }
            })
            backgroundTasks.add(task)
        } else {
            createIdInstance(NextStep.Data)
        }
    }

    private fun siguienteFregment() {
        (activity as Main2Activity?)!!.replaceFragments(SelfieAwareFragment::class.java)
    }

    var identificacion: IdentificacionModel = IdentificacionModel()
    var persona: PersonaModel = PersonaModel()
    private fun llenarINE(result: IDResult) {
        identificacion.result = result
        identificacion.serie = result.classification.type.issue.trim()
        println(identificacion.serie)
        identificacion.tipoDeIdentificacion = result.classification.type.name
        identificacion.resultado = result.result.toInt()
        identificacion.resultadoAssure = result.result.toInt()
        persona.edad = result.biographic.age.toString();
        val fieldsResults = result.fields?.dataFieldReferences
        val datafieldsResults = result.dataFields.dataFields
        for (field in fieldsResults!!) {
            println(field.key + " : " + field.value)
            when (field.key) {
                "Address" -> {
                    persona.domicilioCompleto = field.value
                    try {
                        val jsonObject = OperacionesUtiles.generarDireccion(field.value)
                        val Cp = jsonObject.getString("Cp")
                        val CalleCol = jsonObject.getString("CalleCol")
                        val Estado = jsonObject.getString("Estado")
                        val Municipio = jsonObject.getString("Municipio")
                        persona.calle_Numero = CalleCol
                        persona.codigoPostal = Cp
                        persona.estado = Estado
                        persona.municipio = Municipio
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
                "Birth Date" -> {
                    persona.fechaDeNacimiento = Extras().dateFormatAccuant(field.value)
                    //persona.fechaDeNacimiento = dateFormatAcuant(field.value)
                }
                "CURP" -> {
                    persona.curp = field.value
                }
                "Document Class Name" -> {
                }
                "Expiration Date" -> {
                    identificacion.fechaDeVigencia = Extras().dateFormatAccuant(field.value)
                    //identificacion.fechaDeVigencia = dateFormatAcuant(field.value)
                }
                "Father's Surname" -> {
                    persona.paterno = field.value
                }
                "Full Name" -> {
                }
                "Given Name" -> {
                    persona.nombre = field.value
                }
                "Issue Date" -> {
                    identificacion.fechaDeEmision = Extras().dateFormatAccuant(field.value)
                }
                "Issuing State Code" -> {
                }
                "Issuing State Name" -> {
                }
                "Mother's Surname" -> {
                    persona.materno = field.value
                }
                "Photo" -> {
                }
                "Registration Year and Verification Number" -> {
                    try {
                        identificacion.fechaDeRegistro = Extras().getDate(field.value, 1)
                        identificacion.numeroDeEmision = field.value
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                "Sex" -> {
                    persona.sexo = field.value
                }
                "Surname" -> {
                }
                "Back Number" -> {
                    if(!identificacion.serie.equals("2004")){
                        identificacion.ocr = field.value
                    }else{
                        identificacion.cic = field.value
                    }
                }
                "Document Class Code" -> {
                }
                "Document Number" -> {
                    if(!identificacion.serie.equals("2004")){
                        identificacion.cic = field.value
                    }else{
                        identificacion.ocr = field.value
                    }
                }
                "Fingerprint" -> {
                }
                "MRZ" -> {
                    identificacion.mrz = field.value
                }
                "Nationality Code" -> {
                }
                "Nationality Name" -> {
                    identificacion.nacionalidad = field.value
                }
                "Personal Number" -> {
                    if(!identificacion.serie.equals("2004")){
                        identificacion.ocr = field.value
                    }else{
                        identificacion.electorNumber = field.value
                    }
                }
                "QR Barcode" -> {
                }
                "Signature" -> {
                }
                "Elector Number" -> {
                    identificacion.electorNumber = field.value

                }
            }
        }
        println("-------------------------------------------------------------------------------------")
        for (datafield in datafieldsResults) {
            println(datafield.key + " : " + datafield.value)
            when (datafield.key) {
                "VIZ Given Name" -> {
                    persona.nombre = datafield.value
                }
            }
        }
    }

    private fun llenarPassport(result: IDResult) {
        identificacion.result = result
        identificacion.serie = result.classification.type.issue
        identificacion.tipoDeIdentificacion = result.classification.type.name
        identificacion.resultado = result.result.toInt()
        identificacion.resultadoAssure = result.result.toInt()
        val fieldsResults = result.fields?.dataFieldReferences
        val datafieldsResults = result.dataFields.dataFields
        for (field in fieldsResults!!) {
            println(field.key + " : " + field.value)
            when (field.key) {
                "Document Number" -> {
                    identificacion.numeroDeDocumento = field.value
                }
                "Personal Number" -> {
                    persona.curp = field.value
                }
                "Surname" -> {
                    persona.paterno = Extras().getApellidoPassport(field.value, 0)
                    persona.materno = Extras().getApellidoPassport(field.value, 1)
                }
                "Given Name" -> {
                    persona.nombre = field.value
                }
                "MRZ" -> {
                    identificacion.mrz = field.value
                }
                "Registration Year" -> {
                    identificacion.fechaDeRegistro = Extras().dateFormatAccuant(field.value)
                }
                "Sex" -> {
                    persona.sexo = field.value
                }
                "Birth Date" -> {
                    persona.fechaDeNacimiento = Extras().dateFormatAccuant(field.value)
                }
                "Birth Place" -> {
                }
                "Document Class Code" -> {
                }
                "Document Class Name" -> {
                }
                "Expiration Date" -> {
                    identificacion.fechaDeVigencia = Extras().dateFormatAccuant(field.value)
                }
                "Full Name" -> {
                }
                "Issue Date" -> {
                    identificacion.fechaDeEmision = Extras().dateFormatAccuant(field.value)
                }
                "Issuing State Code" -> {
                }
                "Issuing State Name" -> {
                }
                "Nationality Code" -> {
                }
                "Nationality Name" -> {
                    identificacion.nacionalidad = field.value
                }
                "Signature" -> {
                }
            }
        }
        for (datafield in datafieldsResults) {
            when (datafield.key) {
                "VIZ Given Name" -> {
                    persona.nombre = datafield.value
                }
            }
        }
    }

    private fun llenarProfesionalLicence(result: IDResult) {
        identificacion.serie = result.classification.type.issue
        identificacion.tipoDeIdentificacion = result.classification.type.name
        identificacion.resultado = result.result.toInt()
        identificacion.resultadoAssure = result.result.toInt()
        identificacion.result = result
        val fieldsResults = result.fields?.dataFieldReferences
        val datafieldsResults = result.dataFields.dataFields
        for (field in fieldsResults!!) {
            println(field.key + " : " + field.value)
            when (field.key) {
                "Document Number" -> {
                    identificacion.numeroDeDocumento = field.value
                }
                "CURP" -> {
                    persona.curp = field.value
                }
                "Surname" -> {
                    persona.paterno = Extras().getApellidoPassport(field.value, 0)
                    persona.materno = Extras().getApellidoPassport(field.value, 1)
                }
                "Given Name" -> {
                    persona.nombre = field.value
                }
                "Sex" -> {
                    persona.sexo = field.value
                }
                "Issue Date" -> {
                    identificacion.fechaDeEmision = Extras().dateFormatAccuant(field.value)
                }
                "Issuing State Code" -> {
                    identificacion.nacionalidad = field.value
                }
                "MRZ" -> {
                    identificacion.mrz = field.value
                }
                "Signature" -> {
                }
                "Birth Date" -> {
                    persona.fechaDeNacimiento = Extras().dateFormatAccuant(field.value)
                }
                "Document Class Name" -> {
                }
                "Full Name" -> {
                }
                "Issuing State Name" -> {
                    identificacion.nacionalidad = field.value
                }
                "Personal Number" -> {
                    persona.curp = field.value
                }
            }
        }
        println("-------------------------------------------------------------------------------------")
        for (datafield in datafieldsResults) {
            println(datafield.key + " : " + datafield.value)
            //VIZ Personal Number Full
            when (datafield.key) {
                "VIZ Personal Number Full" -> {
                    persona.curp = datafield.value
                }
            }
        }
    }

    private fun getPhotosCommon(result: IDResult) {
        var frontImageUri: String? = null
        var backImageUri: String? = null
        var faceImageUri: String? = null
        var fieldReferences = result.fields.dataFieldReferences
        for (reference in fieldReferences) {
            if (reference.key.equals("Photo") && reference.type.equals("uri")) {
                faceImageUri = reference.value;
            }
        }
        for (image in result.images.images) {
            if (image.side == 0) {
                frontImageUri = image.uri
            } else if (image.side == 1) {
                backImageUri = image.uri
            }
        }

        val faceImage = Extras().loadAssureIDImage(faceImageUri, Credential.get())
        val stream = ByteArrayOutputStream()
        faceImage?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val image = stream.toByteArray()
        val encodePhoto: String = Base64.encodeToString(image, 0)
        identificacion.fotoRecorteB64 = encodePhoto
        DatosRecolectados.recorte = encodePhoto
        //FOTO FRENTE
        val frontImage = Extras().loadAssureIDImage(frontImageUri, Credential.get())
        val stream2 = ByteArrayOutputStream()
        frontImage?.compress(Bitmap.CompressFormat.PNG, 100, stream2)
        val image2 = stream2.toByteArray()
        val encodePhoto2: String = Base64.encodeToString(image2, 0)
        identificacion.capturaIdentificacionFrente = encodePhoto2
        //FOTO REVERSO
        val backImage = Extras().loadAssureIDImage(backImageUri, Credential.get())
        val stream3 = ByteArrayOutputStream()
        backImage?.compress(Bitmap.CompressFormat.PNG, 100, stream3)
        val image3 = stream3.toByteArray()
        val encodePhoto3: String = Base64.encodeToString(image3, 0)
        identificacion.capturaIdentificacionReverso = encodePhoto3


    }

    private fun getPhotoPassport(result: IDResult) {
        var frontImageUri: String? = null
        var faceImageUri: String? = null
        var fieldReferences = result.fields.dataFieldReferences
        for (reference in fieldReferences) {
            if (reference.key.equals("Photo") && reference.type.equals("uri")) {
                faceImageUri = reference.value;
            }
        }
        for (image in result.images.images) {
            if (image.side == 0) {
                frontImageUri = image.uri
            }
        }


        val faceImage = Extras().loadAssureIDImage(faceImageUri, Credential.get())
        val stream = ByteArrayOutputStream()
        faceImage?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val image = stream.toByteArray()
        val encodePhoto: String = Base64.encodeToString(image, 0)
        identificacion.fotoRecorteB64 = encodePhoto
        //FOTO FRENTE
        val frontImage = Extras().loadAssureIDImage(frontImageUri, Credential.get())
        val stream2 = ByteArrayOutputStream()
        frontImage?.compress(Bitmap.CompressFormat.PNG, 100, stream2)
        val image2 = stream2.toByteArray()
        val encodePhoto2: String = Base64.encodeToString(image2, 0)
        identificacion.capturaIdentificacionFrente = encodePhoto2

        //FOTO 1

        //FOTO REVERSO

    }

    private fun loadingreverso() {
        activity!!.runOnUiThread {
            binding!!.frontImage.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            binding!!.frontImage.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            binding!!.frontImage.setBackgroundResource(0)
            binding!!.frontImage.setImageResource(R.mipmap.captar_reverso)
            binding!!.textoMensaje.setText("Captura una fotografía del reverso de tú identificación")
            binding!!.textoMensaje.setVisibility(View.VISIBLE)
        }
        Handler(Looper.getMainLooper()).postDelayed({
            isFrontCredential = false
            iniciarCamara()
        }, 2500)
    }

    private fun cargarComponentes() {
        executor.execute {
            activity!!.runOnUiThread {
                binding!!.gifMercedes.setBackgroundResource(R.drawable.loadinggeneral)
                loadingAnimation = binding!!.gifMercedes.background as AnimationDrawable
                loadingAnimation.start()
            }
        }

    }

    enum class NextStep { Front, Back, Data }

    private fun createIdInstance(next: NextStep) {
        //setProgress(true, "Creating Instance...")

        val idOptions = IdInstanceOptions()
        val task = AcuantDocumentProcessor.createInstance(idOptions, object :
            CreateIdInstanceListener {
            override fun instanceCreated(instance: AcuantIdDocumentInstance) {
                documentIdInstance = instance
                when (next) {
                    NextStep.Front -> uploadIdFront()
                    NextStep.Back -> uploadIdBack()
                    NextStep.Data -> getData()
                }
            }

            override fun onError(error: AcuantError) {
                showAcuDialog(error)
            }
        })
        backgroundTasks.add(task)
    }

    fun isBackSideRequired(classification: Classification?): Boolean {
        var isBackSideScanRequired = false
        if (classification?.type != null && classification.type.supportedImages != null) {
            val list = classification.type.supportedImages as ArrayList<HashMap<String, Int>>
            for (i in list.indices) {
                val map = list[i]
                if (map["Light"] == 0) {
                    if (map["Side"] == 1) {
                        isBackSideScanRequired = true
                    }
                }
            }
        }
        return isBackSideScanRequired
    }

    private fun stopBackgroundTasks() {
        backgroundTasks.forEach {
            it.cancel()
        }
        backgroundTasks.clear()
    }

    fun iniciarAcuant() {


        iniciarSDKAcuant(object : IAcuantPackageCallback {
            override fun onInitializeSuccess() {
                activity!!.runOnUiThread {
                    binding!!.gifMercedes.visibility = View.GONE
                    binding!!.iniciarCaptura.visibility = View.VISIBLE

                }

            }

            override fun onInitializeFailed(error: List<AcuantError>) {
                val alert = AlertDialog.Builder(requireContext())
                alert.setTitle("Error")
                alert.setCancelable(false)
                alert.setMessage("No se pudo iniciar" + "\n" + error[0].errorDescription)
                alert.setPositiveButton("Aceptar") { dialog, _ ->
                    dialog.dismiss()
                }
                alert.show()
            }
        })


    }

    private fun iniciarSDKAcuant(callback: IAcuantPackageCallback) {

        try {
            val task = AcuantInitializer.initialize(
                "AcuantConfig.xml",
                requireContext(),
                listOf(ImageProcessorInitializer(), EchipInitializer(), MrzCameraInitializer()),
                callback
            )
            if (task != null)
                backgroundTasks.add(task)
        } catch (e: AcuantException) {
            Log.e("Acuant Error", e.toString())
        }


    }


    override fun onClick(p0: View?) {

    }

    private fun showAcuDialog(
        message: String, title: String = "Error",
        yesOnClick: DialogInterface.OnClickListener? = null,
        noOnClick: DialogInterface.OnClickListener? = null
    ) {
        val code = {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(title)
            alert.setCancelable(false)
            alert.setMessage(message)
            if (yesOnClick != null) {
                alert.setPositiveButton("YES", yesOnClick)
                if (noOnClick != null) {
                    alert.setNegativeButton("NO", noOnClick)
                }
            } else {
                alert.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
            }
            alert.show()
        }
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            code()
        } else {
            activity!!.runOnUiThread {
                code()
            }
        }
    }

    private fun showAcuDialog(
        message: Int, @Suppress("SameParameterValue") title: String = "Error",
        yesOnClick: DialogInterface.OnClickListener? = null,
        noOnClick: DialogInterface.OnClickListener? = null
    ) {
        showAcuDialog(getString(message), title, yesOnClick, noOnClick)
    }

    private fun showAcuDialog(
        message: AcuantError, @Suppress("SameParameterValue") title: String = "Error",
        yesOnClick: DialogInterface.OnClickListener? = null,
        noOnClick: DialogInterface.OnClickListener? = null
    ) {
        if (message.additionalDetails == null) {
            showAcuDialog(message.errorDescription, title, yesOnClick, noOnClick)
        } else {
            showAcuDialog(
                "${message.errorDescription} - ${message.additionalDetails}",
                title,
                yesOnClick,
                noOnClick
            )
        }
    }

}