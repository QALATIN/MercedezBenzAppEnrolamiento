package com.latinid.mercedes.ui.nuevosolicitante.selfie

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.acuant.acuantcamera.initializer.MrzCameraInitializer
import com.acuant.acuantcommon.background.AcuantAsync
import com.acuant.acuantcommon.exception.AcuantException
import com.acuant.acuantcommon.initializer.AcuantInitializer
import com.acuant.acuantcommon.initializer.IAcuantPackageCallback
import com.acuant.acuantcommon.model.AcuantError
import com.acuant.acuantechipreader.initializer.EchipInitializer
import com.acuant.acuantfacematchsdk.AcuantFaceMatch
import com.acuant.acuantfacematchsdk.model.FacialMatchData
import com.acuant.acuantfacematchsdk.model.FacialMatchResult
import com.acuant.acuantfacematchsdk.service.FacialMatchListener
import com.acuant.acuantimagepreparation.initializer.ImageProcessorInitializer
import com.acuant.acuantipliveness.AcuantIPLiveness
import com.acuant.acuantipliveness.IPLivenessListener
import com.acuant.acuantipliveness.facialcapture.model.FacialCaptureResult
import com.acuant.acuantipliveness.facialcapture.model.FacialSetupResult
import com.acuant.acuantipliveness.facialcapture.service.FacialCaptureListener
import com.acuant.acuantipliveness.facialcapture.service.FacialSetupListener

import com.latinid.mercedes.R
import com.latinid.mercedes.databinding.FragmentSelfieBinding
import com.latinid.mercedes.model.local.IdentificacionModel
import com.latinid.mercedes.model.local.PersonaModel
import com.latinid.mercedes.ui.home.HomeFragment
import com.latinid.mercedes.Main2Activity


class SelfieFragment : Fragment(), View.OnClickListener {

    private val TAG = "SelfieFragment"
    private val backgroundTasks = mutableListOf<AcuantAsync>()
    private var binding: FragmentSelfieBinding? = null


    private lateinit var loadingAnimation: AnimationDrawable
    private var capturedSelfieImage: Bitmap? = null

    var identificacion: IdentificacionModel = IdentificacionModel()
    var persona: PersonaModel = PersonaModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelfieBinding.inflate(inflater, container, false)
        cargarComponentes()
        return binding!!.getRoot()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iniciarAcuant()
    }

    override fun onResume() {
        super.onResume()
        binding?.iniciarCaptura?.setOnClickListener { view ->
            activity!!.runOnUiThread {
                view.visibility = View.INVISIBLE
                binding!!.gifMercedes.visibility =View.VISIBLE
                binding!!.textoMensaje.text = "Iniciando camara . . ."
            }
                val task = AcuantIPLiveness.getFacialSetup(object : FacialSetupListener {

                    override fun onDataReceived(result: FacialSetupResult) {
                        //setProgress(false)
                        result.allowScreenshots = true
                        if (result != null) {
                            //start face capture activity
                            result.allowScreenshots = true //Set to false by default; set to true to enable allowScreenshots

                            AcuantIPLiveness.runFacialCapture(requireContext(), result, object :
                                IPLivenessListener {
                                override fun onConnecting() {

                                }

                                override fun onConnected() {

                                }

                                override fun onProgress(status: String, progress: Int) {

                                }

                                override fun onSuccess(userId: String, token: String, frame: Bitmap?) {
                                    activity!!.runOnUiThread {
                                        binding!!.textoMensaje.text = "Obteniendo cara . . ."
                                    }

                                    startFacialLivelinessRequest(token, userId)
                                }

                                override fun onFail(error: AcuantError) {
                                    val alert = AlertDialog.Builder(requireContext())
                                    alert.setTitle("Imposible capturar")
                                    alert.setMessage("Se volvera a intentar capturar")
                                    alert.setPositiveButton("Aceptar") { dialog, _ ->
                                        dialog.dismiss()
                                        binding?.iniciarCaptura?.setOnClickListener{this}
                                    }
                                    alert.show()
                                }

                                override fun onCancel() {
                                    val alert = AlertDialog.Builder(requireContext())
                                    alert.setTitle("Captura cancelada")
                                    alert.setMessage("¿Capturar de nuevo?")
                                    alert.setPositiveButton("Aceptar") { dialog, _ ->
                                        dialog.dismiss()
                                        binding?.iniciarCaptura?.setOnClickListener{this}
                                    }
                                    alert.show()
                                }

                                override fun onError(error: AcuantError) {
                                    val alert = AlertDialog.Builder(requireContext())
                                    alert.setTitle("Imposible capturar")
                                    alert.setMessage("Se volvera a intentar capturar")
                                    alert.setPositiveButton("Aceptar") { dialog, _ ->
                                        dialog.dismiss()
                                        binding?.iniciarCaptura?.setOnClickListener{this}
                                    }
                                    alert.show()
                                }
                            })
                        }

                    }

                    override fun onError(error: AcuantError) {
                        val alert = AlertDialog.Builder(requireContext())
                        alert.setTitle("Imposible capturar")
                        alert.setMessage("Se volvera a intentar capturar")
                        alert.setPositiveButton("Aceptar") { dialog, _ ->
                            dialog.dismiss()
                            binding?.iniciarCaptura?.setOnClickListener{this}
                        }
                        alert.show()
                    }
                })
                backgroundTasks.add(task)

        }
    }

    private fun startFacialLivelinessRequest(token: String, userId: String) {

        AcuantIPLiveness.getFacialLiveness(
            token,
            userId,
            object : FacialCaptureListener {
                override fun onDataReceived(result: FacialCaptureResult) {
                    persona.pruebaDeVida = result.isPassed.toString()
                    if (result.isPassed) {
                        //DatosRecolectados.faceB64 = result.frame
                        persona.fotoSelfieB64 = result.frame
                        val decodedString = Base64.decode(result.frame, Base64.NO_WRAP)
                        capturedSelfieImage =
                            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                       activity!!.runOnUiThread {
                            binding!!.image.setImageBitmap(capturedSelfieImage)
                        }
                        facialMatch()
                    } else {
                        val alert = AlertDialog.Builder(requireContext())
                        alert.setTitle("Sin comprobación de vida")
                        alert.setMessage("No se pudo validar la prueba de vida, vuelva a capturar, procure no mover tanto la tableta")
                        alert.setPositiveButton("Aceptar") { dialog, _ ->
                            dialog.dismiss()
                            binding?.iniciarCaptura?.setOnClickListener{this}
                        }
                        alert.show()
                    }
                }

                override fun onError(error: AcuantError) {
                    val alert = AlertDialog.Builder(requireContext())
                    alert.setTitle("Oooops!")
                    alert.setMessage("Detalle en el servidor de selfie, se volvera a capturar, si el problema perdura, comuniquesé con soporte técnico")
                    alert.setPositiveButton("Aceptar") { dialog, _ ->
                        dialog.dismiss()
                        binding?.iniciarCaptura?.setOnClickListener{this}
                    }
                    alert.show()
                }
            }
        )

    }

    private fun facialMatch() {
        activity!!.runOnUiThread {
            binding!!.textoMensaje.text = "Analizando . . ."
        }

        val facialMatchData = FacialMatchData()
        val decodedString = Base64.decode(identificacion.fotoRecorteB64, Base64.NO_WRAP)
        val image1 = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        val decodedString2 = Base64.decode(persona.fotoSelfieB64, Base64.NO_WRAP)
        val image2 = BitmapFactory.decodeByteArray(decodedString2, 0, decodedString2.size)
        facialMatchData.faceImageOne = image1;
        facialMatchData.faceImageTwo = image2;
        AcuantFaceMatch.processFacialMatch(facialMatchData, object : FacialMatchListener {
            override fun facialMatchFinished(p0: FacialMatchResult) {
                println("resultado: " + p0!!.score)
                println("resultado: " + p0!!.isMatch)
                //p0.score Number
                if (p0!!.isMatch == true) {
                    persona.comparacionFacial = "75.0"
                } else {
                    persona.comparacionFacial = "0.0"
                }
                siguienteFregment()
            }

            override fun onError(error: AcuantError) {
                val alert = AlertDialog.Builder(requireContext())
                alert.setTitle("Oooops!")
                alert.setMessage("Detalle en el servidor de selfie, se volvera a capturar, si el problema perdura, comuniquesé con soporte técnico")
                alert.setPositiveButton("Aceptar") { dialog, _ ->
                    dialog.dismiss()
                    binding?.iniciarCaptura?.setOnClickListener{this}
                }
                alert.show()
            }
        })
    }

    private fun siguienteFregment(){
        (activity as Main2Activity?)!!.replaceFragments(HomeFragment::class.java)
    }

    private fun cargarComponentes() {
        binding!!.gifMercedes.setBackgroundResource(R.drawable.loadinggeneral)
        loadingAnimation = binding!!.gifMercedes.background as AnimationDrawable
        loadingAnimation.start()
    }

    override fun onDestroy() {
        stopBackgroundTasks()
        loadingAnimation.stop()

        super.onDestroy()
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
            AcuantInitializer.initialize(
                "AcuantConfig.xml",
                requireContext(),
                listOf(ImageProcessorInitializer(), EchipInitializer(), MrzCameraInitializer()),
                callback
            )
        } catch (e: AcuantException) {
            Log.e("Acuant Error", e.toString())
        }
    }

    override fun onClick(p0: View?) {

    }



}