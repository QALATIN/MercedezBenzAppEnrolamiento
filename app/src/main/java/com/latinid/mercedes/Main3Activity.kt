package com.latinid.mercedes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.latinid.mercedes.databinding.ActivityMain2Binding
import com.latinid.mercedes.ui.applicants.SubMenuFragment
import com.latinid.mercedes.ui.home.LoginFragment
import com.latinid.mercedes.ui.nuevosolicitante.facecapture.api.FaceCaptureApi
import com.latinid.mercedes.ui.nuevosolicitante.facecapture.fragments.FaceAwareFragment
import com.latinid.mercedes.ui.nuevosolicitante.facecapture.fragments.LivenessFragment
import com.latinid.mercedes.ui.nuevosolicitante.privacypolicy.AvisoFragment
import com.latinid.mercedes.ui.nuevosolicitante.selfie.rest.RestClientTask
import com.latinid.mercedes.util.*
import java.io.IOException

/**
 * Clase Base del Proyecto, Contenedora de DrawerLayout
 * <p>
 * Control Dinamico de las Actividades-Clases del menu Navigation</p>
 * <p>
 * Controla el diseño de la barra superior ActionBar - Imagen y titulo mercedes</p>
 * <p>
 */
class Main3Activity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, FaceAwareFragment.FaceListener {

    private val TAG: String = "Main3Activity.class"

    private var mAppBarConfiguration: AppBarConfiguration? = null
    private var binding: ActivityMain2Binding? = null
    private var tvHeaderName: TextView? = null
    private var tvCloseSesion: TextView? = null
    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable
    private var mTime: Long = 600000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Mercedes_NoActionBar)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())
        setSupportActionBar(binding!!.appBarMain.toolbar)
        loadNavigationUI()
        drawVersion()
        imagenMercedesActionBar()
        iniciarTemporizadorSesion()
        btnCerrarSesion()
        OperacionesUtiles.saveCoors(this)//Save Coors To use in Signature Services

    }

    private fun loadNavigationUI(){
        val drawer: DrawerLayout = binding!!.drawerLayout
        val navigationView: NavigationView = binding!!.navView
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_home, R.id.nav_aviso, R.id.nav_consulta, R.id.nav_version, R.id.nav_extra
        )
            .setOpenableLayout(drawer)
            .build()
        val navController = findNavController(this, R.id.nav_host_fragment_content_main)
        setupActionBarWithNavController(this, navController, mAppBarConfiguration!!)
        setupWithNavController(navigationView, navController)
        navigationView.setNavigationItemSelectedListener(this)

    }

    private fun drawVersion(){
        try {
            val item:MenuItem  = binding!!.navView.menu.findItem(R.id.nav_version)
            if(Conexiones.webServiceGeneral.equals("https://mbfs.latinid.com.mx:9582")){
                item.setTitle("Versión "+BuildConfig.VERSION_NAME +" (DEV)")
            }else if(Conexiones.webServiceGeneral.equals("https://mbfs.latinid.com.mx:9583")){
                item.setTitle("Versión "+BuildConfig.VERSION_NAME +" (QA)")
            }else{
                item.setTitle("Versión "+BuildConfig.VERSION_NAME)
            }
        }catch (e:Throwable){
            BinnacleCongif.writeLog(TAG,1,"Hubo error asignando la versión en la tableta","Error: "+e.message+"|"+e.localizedMessage.toString(),this)
            e.printStackTrace()
        }
    }

    private fun btnCerrarSesion(){
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)//Indicador de ventana: siempre que esta ventana sea visible para el usuario, mantenga la pantalla del dispositivo encendida y brillante.
        val header: View = binding!!.navView.getHeaderView(0)
        tvHeaderName = header.findViewById<View>(R.id.nameEnrollment) as TextView
        tvCloseSesion = header.findViewById<View>(R.id.closeSesion) as TextView
        tvCloseSesion!!.setOnClickListener { view: View? ->
            if (!DatosRecolectados.inSesion) {
            } else {
                tvHeaderName!!.text = ""
                DatosRecolectados.inSesion = false
                removerFragmets()
                replaceFragments(LoginFragment::class.java)
                val drawe2r: DrawerLayout = binding!!.drawerLayout
                drawe2r.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun iniciarTemporizadorSesion(){
        mHandler = Handler(Looper.getMainLooper())
        mRunnable = Runnable {
            try {
                if (DatosRecolectados.inSesion) {
                    DatosRecolectados.inSesion = false
                    val i = baseContext.packageManager
                        .getLaunchIntentForPackage(baseContext.packageName)
                    //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(i)
                    System.exit(1)
                }
            } catch (e: Throwable) {
                BinnacleCongif.writeLog(TAG,1,"Error saliendo de la sesión despues del tiempo asignado 5 minutos de inactividad -> function(): iniciarTemporizadorSesion()","Error: "+e.message+"|"+e.localizedMessage.toString()+"|"+e.stackTraceToString(),this)
                val i = baseContext.packageManager
                    .getLaunchIntentForPackage(baseContext.packageName)
                //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(i)
                System.exit(1)
            }
        }
        startHandler()
    }


    override fun onUserInteraction() {
        super.onUserInteraction()
        if (DatosRecolectados.inSesion) {
            stopHandler()
            startHandler()
        } else {
            stopHandler()
        }
    }

    private fun startHandler() {
        mHandler.postDelayed(mRunnable, mTime)
    }

    private fun stopHandler() {
        mHandler.removeCallbacks(mRunnable)
    }

    fun insertNameComplete(name: String?) {
        tvHeaderName!!.text = name
    }

    private fun imagenMercedesActionBar() {
        val actionBar = supportActionBar
        actionBar!!.setDisplayShowCustomEnabled(true)
        val inflater = this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.image_custome_actionbar, null)
        actionBar.customView = view
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun verifyVersion(){
        Thread {
            try {
                val jsonObject1 = GetPost.crearGet(
                    Conexiones.webServiceGeneral + "/Gateway/api/recuperacion/apkVersion",
                    this
                )
                val fecha = jsonObject1.getString("fecha")
                val version = jsonObject1.getString("version")
                val actualizar = jsonObject1.getBoolean("actualizar")
                val versionLocal = BuildConfig.VERSION_NAME
                if (actualizar) {
                    if (!versionLocal.equals(version)) {
                        val upFragment = UpdateFragment()
                        upFragment.isCancelable = false
                        upFragment.show(supportFragmentManager, "update")
                        //upFragment.show(supportFragmentManager, "update");
                    }
                }
            } catch (e: Throwable) {
                BinnacleCongif.writeLog(TAG,1,"Error consultando la versión de la App en Servidor -> function(): verifyVersion() "+Conexiones.webServiceGeneral + "/Gateway/api/recuperacion/apkVersion","Error: "+e.message+"|"+e.localizedMessage.toString()+"|"+e.stackTraceToString(),this)
                e.printStackTrace()
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        val navigationView: NavigationView = binding!!.navView
        navigationView.setNavigationItemSelectedListener(this)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        verifyVersion()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.nav_host_fragment_content_main)
        return (navigateUp(navController, mAppBarConfiguration!!)
                || super.onSupportNavigateUp())
    }

    fun replaceFragments(fragmentClass: Class<*>) {
        removerFragmets()
        var fragment: Fragment? = null
        try {
            fragment = fragmentClass.newInstance() as Fragment
            if (fragmentClass == AvisoFragment::class.java) {
                val navigationView: NavigationView = binding!!.navView
                navigationView.setCheckedItem(R.id.nav_aviso)
            }
            if (fragmentClass == SubMenuFragment::class.java) {
                val navigationView: NavigationView = binding!!.navView
                navigationView.setCheckedItem(R.id.nav_consulta)
            }
        } catch (e: Exception) {
            BinnacleCongif.writeLog(TAG,1,"Error interno en el intercambio de Fragmentos -> function():replaceFragments()","Error: "+e.message+"|"+e.localizedMessage.toString()+"|"+e.stackTraceToString(),this)
            e.printStackTrace()
        }
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().setReorderingAllowed(true).addToBackStack(null).replace(
            R.id.nav_host_fragment_content_main,
            fragment!!
        )
            .commitAllowingStateLoss()
    }

    fun removerFragment() {
        supportFragmentManager.popBackStack()
    }

    fun removerFragmets() {
        val fm = supportFragmentManager
        for (i in 0 until fm.getBackStackEntryCount()) {
            fm.popBackStack()
        }
    }

    fun activateBar() {
        supportActionBar!!.show()
    }

    fun desactivateBar() {
        supportActionBar!!.hide()
    }


    override fun onBackPressed() {}

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navigationView: NavigationView = binding!!.navView
        val id = item.itemId
        if (!DatosRecolectados.inSesion) {
            if(id==R.id.nav_extra){
                val logFragment = SendLogFragment()
                logFragment.isCancelable = true
                logFragment.show(supportFragmentManager, "log")
            }
            return false
        }
        navigationView.setCheckedItem(id)
        val fragment: Fragment
        val ft = supportFragmentManager.beginTransaction()
        when (id) {
            R.id.nav_aviso -> {
                //removerFragmets()
                fragment = AvisoFragment()
                ft.replace(R.id.nav_host_fragment_content_main, fragment)
                ft.addToBackStack(null)
                ft.commitAllowingStateLoss()
            }
            R.id.nav_home -> {
                removerFragmets()

            }
            R.id.nav_consulta -> {
                //removerFragmets()
                fragment = SubMenuFragment()
                ft.replace(R.id.nav_host_fragment_content_main, fragment)
                ft.addToBackStack(null)
                ft.commitAllowingStateLoss()
            }
            R.id.nav_extra -> {
                //removerFragmets()
                val logFragment = SendLogFragment()
                logFragment.isCancelable = true
                logFragment.show(supportFragmentManager, "log")
            }
            else -> {}
        }
        val drawer: DrawerLayout = binding!!.drawerLayout
        drawer.closeDrawer(GravityCompat.START)
        navigationView.setNavigationItemSelectedListener(this)
        return false
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        DatosRecolectados.tempIntent = intent;
    }


    /**
     * **LÓGICA COMPLEMENTARIA AL FRAGMENTO SELFIEAWAREFRAGMENT**
     *
     * Metodos correspondientes para activar el SDK de Knomi Aware
     *
     * Metodos finales para el termino de la captura facial
     *
     * Tiene su propia metodo de inflar fragmento para que solo afecto el container de la selfie y no el general
     *
     * Metodo workflow, metodo importante para lanzar la capura de la selfie
     */
    private var mFaceCaptureApi: FaceCaptureApi? = null
    private val mMatchThresholdValue = 3
    private var mErrorCreatingSession = false

    override fun onWorkflowSelected(workflowName: String?, id: String?) {


        //Create an instance.
        try {
            mFaceCaptureApi = FaceCaptureApi.getInstance(this@Main3Activity)
            mErrorCreatingSession = false
            Log.e("ERROR_TEST", "onWorkflowSelected after allocate lib obj: ")
        } catch (e: Throwable) {
            BinnacleCongif.writeLog(TAG,1,"Error Iniciando API DE AWARE -> function():onWorkflowSelected():","Error: "+e.message+"|"+e.localizedMessage.toString()+"|"+e.stackTraceToString(),this)
            e.printStackTrace()
            onCaptureAbort()
        }

        val mUsername = "Latin"
        var mCaptureTimeout = "0"
        val mCameraPosition = "Back"
        val mCameraOrientation = "Portrait"
        val mPackageType = "High Usability"
        val mProfileName = "face_capture_foxtrot_client.xml"



        if (mCaptureTimeout == "") mCaptureTimeout = "0.0"

        // Note the following calls must be in the correct order.
        // Must set properties before calling selectWorkflow.

        // Note the following calls must be in the correct order.
        // Must set properties before calling selectWorkflow.
        try {
            //Create an ApiData class and fill in appropriate values.

            //Create an ApiData class and fill in appropriate values.
            val sessionData = FaceCaptureApi.ApiData()
            sessionData.workflow = "Foxtrot"
            sessionData.userName = mUsername
            sessionData.captureTimeout = mCaptureTimeout.toDouble()
            sessionData.profileData = getProfileData("profiles/$mProfileName")
            sessionData.cameraOrientation = mCameraOrientation
            sessionData.cameraPosition = mCameraPosition
            sessionData.packageType = mPackageType
            //mIsPortrait = mCameraOrientation.lowercase(Locale.getDefault()) == "portrait"
            mFaceCaptureApi!!.setupSessionData(sessionData)
        } catch (e: Throwable) {
            BinnacleCongif.writeLog(TAG,1,"Error Configurando API DE AWARE -> function():val sessionData = FaceCaptureApi.ApiData():","Error: "+e.message+"|"+e.localizedMessage.toString()+"|"+e.stackTraceToString(),this)
            mErrorCreatingSession = true
            e.printStackTrace()
            runOnUiThread {
                val message = "onWorkflowSelected Invalid property setting: " + e.message
                val t = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
                t.setGravity(Gravity.CENTER, 0, 0)
                t.show()
            }
        }

        //Foxtrot

    }

    //  @Override
    //  public void onSaveInstanceState(Bundle outState) {
    //      super.onSaveInstanceState(outState);
    // }
    fun getProfileData(profile_name: String?): ByteArray? {
        val profile_data: ByteArray?
        profile_data = try {
            readAsset(this@Main3Activity as Context, profile_name)
        } catch (e: java.lang.Exception) {
            null
        }
        return profile_data
    }

    fun readAsset(context: Context, filename: String?): ByteArray? {
        var outBuffer: ByteArray? = null
        try {
            val inn = context.applicationContext.assets.open(filename!!)
            outBuffer = ByteArray(inn.available())
            inn.read(outBuffer)
            inn.close()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return outBuffer
    }

    fun sessionSetupComplete() {
        selectFragment()
    }

    fun selectFragment() {
        val fm = supportFragmentManager
        var fragment2: Fragment? = null
        fragment2 = LivenessFragment.newInstance(this,true)
        val ft = fm.beginTransaction()
        ft.replace(R.id.fragment_containerr, fragment2, "live")
        ft.addToBackStack("execute")
        ft.commitAllowingStateLoss()
    }

    fun onCaptureEnd() {
        runOnUiThread {
            removerFragment()
        }
        try {
            var serverPackage: String? = mFaceCaptureApi!!.getServerPackage()
            val postTask = RestClientTask(this)
            postTask.executeLiveness(serverPackage, mMatchThresholdValue)
            serverPackage = null
        } catch (e: Throwable) {
            BinnacleCongif.writeLog(TAG,1,"Error trayendo el paquete de Aware getServerPackage() -> function():onCaptureEnd() ","Error: "+e.message+"|"+e.localizedMessage.toString()+"|"+e.stackTraceToString(),this)
            e.printStackTrace()
        }
    }

    fun onCaptureTimedout() {
        runOnUiThread {
            supportFragmentManager.beginTransaction()
                .remove(supportFragmentManager.findFragmentByTag("live")!!).commit()
        }
    }

    fun onCaptureAbort() {
        mFaceCaptureApi!!.DestroyWorkflow()
        runOnUiThread {
            supportFragmentManager.beginTransaction()
                .remove(supportFragmentManager.findFragmentByTag("live")!!).commit()
        }
    }

    fun onCaptureStopped() {
        runOnUiThread {
            supportFragmentManager.beginTransaction()
                .remove(supportFragmentManager.findFragmentByTag("live")!!).commit()
        }
        mFaceCaptureApi!!.DestroyWorkflow()
    }
}