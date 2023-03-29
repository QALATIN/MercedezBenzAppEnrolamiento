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
import com.aware.face_liveness.api.FaceLiveness
import com.aware.face_liveness.api.exceptions.FaceLivenessException
import com.aware.face_liveness.api.interfaces.ErrorReporterCallback
import com.google.android.material.navigation.NavigationView
import com.latinid.mercedes.databinding.ActivityMain2Binding
import com.latinid.mercedes.ui.applicants.SubMenuFragment
import com.latinid.mercedes.ui.home.LoginFragment
import com.latinid.mercedes.ui.nuevosolicitante.privacypolicy.AvisoFragment
import com.latinid.mercedes.ui.nuevosolicitante.selfie.fragments.LivenessFragment
import com.latinid.mercedes.ui.nuevosolicitante.selfie.fragments.SelfieAwareFragment
import com.latinid.mercedes.ui.nuevosolicitante.selfie.rest.RestClientTask
import com.latinid.mercedes.util.Conexiones
import com.latinid.mercedes.util.GetPost
import com.latinid.mercedes.util.OperacionesUtiles
import com.latinid.mercedes.util.UpdateFragment
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

class Main2Activity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    FaceLiveness.LivenessActivityPresenter,
    ErrorReporterCallback, SelfieAwareFragment.SelfieListener {
    private var mAppBarConfiguration: AppBarConfiguration? = null
    private var binding: ActivityMain2Binding? = null
    private var mLivenessApi: FaceLiveness? = null

    //private var mInitializeBackgroundTask: InitializeBackgroundTask? = null
    private var mInitComplete = false
    private val mInitializationError = FaceLiveness.InitializationError.NO_ERROR
    private val executor = Executors.newSingleThreadExecutor()
    private var tvHeaderName: TextView? = null
    private var tvCloseSesion: TextView? = null
    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable
    private var mTime: Long = 600000


    private val executor2 = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Mercedes_NoActionBar)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())
        setSupportActionBar(binding!!.appBarMain.toolbar)
        val drawer: DrawerLayout = binding!!.drawerLayout
        val navigationView: NavigationView = binding!!.navView
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_home, R.id.nav_aviso, R.id.nav_consulta
        )
            .setOpenableLayout(drawer)
            .build()
        val navController = findNavController(this, R.id.nav_host_fragment_content_main)
        setupActionBarWithNavController(this, navController, mAppBarConfiguration!!)
        setupWithNavController(navigationView, navController)
        navigationView.setNavigationItemSelectedListener(this)
        imagenMercedesActionBar()
        Thread {
            iniciarBack(mInitListener);
        }.start()
        OperacionesUtiles.saveCoors(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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

        mHandler = Handler(Looper.getMainLooper())
        mRunnable = Runnable {
            try {
                println("Verificando sesión")
                if (DatosRecolectados.inSesion) {
                    DatosRecolectados.inSesion = false
                    /*tvHeaderName!!.text = ""
                    DatosRecolectados.inSesion = false
                    replaceFragments(LoginFragment::class.java)
                    val drawe2r: DrawerLayout = binding!!.drawerLayout
                    drawe2r.closeDrawer(GravityCompat.START)*/
                    val i = baseContext.packageManager
                        .getLaunchIntentForPackage(baseContext.packageName)
                    //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(i)
                    System.exit(1)
                }
            } catch (e: Throwable) {
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

    // stop handler function
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

    override fun onResume() {
        super.onResume()
        val navigationView: NavigationView = binding!!.navView
        navigationView.setNavigationItemSelectedListener(this)
        supportActionBar!!.setDisplayShowTitleEnabled(false)


         Thread {
            try {

                val jsonObject1 = GetPost.crearGet(
                    Conexiones.webServiceGeneral + "/Gateway/api/recuperacion/apkVersion",
                   this
                )
                println(jsonObject1)
                val fecha = jsonObject1.getString("fecha")
                val version = jsonObject1.getString("version")
                val actualizar = jsonObject1.getBoolean("actualizar")
                val versionLocal = DatosRecolectados.versionApp
                println(versionLocal)
                println(version)
                if(actualizar){
                    if(!versionLocal.equals(version)){
                        val upFragment = UpdateFragment()
                        upFragment.isCancelable = false
                        upFragment.show(supportFragmentManager, "update")
                        //upFragment.show(supportFragmentManager, "update");
                    }
                }
            } catch (e: Throwable) {
                println("Error version")
                e.printStackTrace()
            }
        }.start()

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
        //getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        /*for (Fragment fragmento : getSupportFragmentManager().getFragments()) {
            if(fragment.getClass() == fragmento.class){

                break;
            }
        }*/
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
        if (!DatosRecolectados.inSesion) {
            return false
        }
        val navigationView: NavigationView = binding!!.navView
        val id = item.itemId
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
            else -> {}
        }
        val drawer: DrawerLayout = binding!!.drawerLayout
        drawer.closeDrawer(GravityCompat.START)
        navigationView.setNavigationItemSelectedListener(this)
        return false
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        println("readNfcTag2")

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
    override fun onWorkflowSelected(workflowName: String?, id: String?, overrideJson: String?) {
        val isRunning = false
        val mUsername = "Latin"
        var mCaptureTimeout = "0"
        val mImageCaptureProperty = true
        val mCaptureOnDevice = true
        val mCheckPhonePosition = false
        if (mCaptureTimeout == "") mCaptureTimeout = "0"

        // Note the following calls must be in the correct order.
        // Must set properties before calling selectWorkflow.
        try {
            mLivenessApi!!.setProperty(FaceLiveness.PropertyTag.USERNAME, mUsername)
            mLivenessApi!!.setProperty(
                FaceLiveness.PropertyTag.CONSTRUCT_IMAGE,
                mImageCaptureProperty
            )
            mLivenessApi!!.setProperty(FaceLiveness.PropertyTag.TIMEOUT, mCaptureTimeout.toDouble())
            mLivenessApi!!.setProperty(FaceLiveness.PropertyTag.CAPTURE_ON_DEVICE, mCaptureOnDevice)
            mLivenessApi!!.setProperty(
                FaceLiveness.PropertyTag.CHECK_PHONE_POSITION,
                mCheckPhonePosition
            )
        } catch (e: FaceLivenessException) {
            runOnUiThread {
                val message = "Invalid property setting!!!"
                val t = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
                t.setGravity(Gravity.CENTER, 0, 0)
                t.show()
            }
            e.printStackTrace()
        }

        // select a workflow
        try {
            mLivenessApi!!.selectWorkflow(applicationContext, workflowName, overrideJson)
        } catch (e: FaceLivenessException) {
            if (e.errorCode == FaceLivenessException.ErrorCode.InsufficientCameraPermission) {
                //clearMarkAsAsked(CAMERA);
            } else if (e.errorCode == FaceLivenessException.ErrorCode.InsufficientWriteSettingsPermission) {
                // clearMarkAsAsked(WRITE_SETTINGS);
            } else {
                runOnUiThread {
                    val message = "Invalid workflow setting or incorrect model specified!!!"
                    val t = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
                    t.setGravity(Gravity.CENTER, 0, 0)
                    t.show()
                }
            }
            e.printStackTrace()
            return
        }
        selectFragment()
    }


    fun selectFragment() {
        val fm = supportFragmentManager
        var fragment2: Fragment? = null
        fragment2 = LivenessFragment.newInstance(this)
        val ft = fm.beginTransaction()
        ft.replace(R.id.fragment_containerr, fragment2, "live")
        ft.addToBackStack("execute")
        ft.commitAllowingStateLoss()
    }

    fun onCaptureEnd() {
        runOnUiThread {
            //getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("live")).commit();
            println("Si llegue")
            //popUpToBaseFragment();
            removerFragment()
            //ShowHourglassDirect();
        }
        try {
            val serverPackage = mLivenessApi!!.serverPackage
            val postTask = RestClientTask(this)
            postTask.executeLiveness(serverPackage, 3)
        } catch (e: FaceLivenessException) {
            //getServerPackage can throw an exception on an error
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
        runOnUiThread {
            supportFragmentManager.beginTransaction()
                .remove(supportFragmentManager.findFragmentByTag("live")!!).commit()
        }
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {
        super.onPointerCaptureChanged(hasCapture)
    }


    interface ModelInitializationListener {
        fun onInitializationComplete(success: Boolean, modelName: String?)
    }

    private val mInitListener: ModelInitializationListener = object : ModelInitializationListener {
        override fun onInitializationComplete(success: Boolean, modelName: String?) {
            runOnUiThread {
                if (!success) {
                    val message =
                        "Could not initialize model $modelName not found!"
                    val t = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
                    t.setGravity(Gravity.CENTER, 0, 0)
                    t.show()
                } else {
                    Log.i("SELFIE AWARE", "AWARE INICIADO CORRECTAMENTE ")
                }
            }
        }

    }


    fun iniciarBack(mInitializationListener: ModelInitializationListener) {

        try {
            FaceLiveness.setStaticProperty(
                FaceLiveness.StaticPropertyTag.FACE_MODEL,
                "FaceModelStandardv2.dat"
            )
            mLivenessApi = FaceLiveness(getApplicationContext())
        } catch (e: FaceLivenessException) {
            e.printStackTrace()
        }
        try {
            // initialize the library, wait for callback...
            mLivenessApi!!.initializeFaceLivenessLibrary(this@Main2Activity)
        } catch (e: FaceLivenessException) {
            // mCouldNotOpenModel = true

        }
        runOnUiThread {
            mInitializationListener.onInitializationComplete(true, "FaceModelStandardv2.dat")
        }


    }


    override fun onErrorReporter(errorCode: Int, info: String, context: Context?) {
        Log.d(
            "LIVENESS_SAMPLE_TAG",
            "  ---------------------------------       onErrorReporter: $info $errorCode"
        )
    }

    override fun onInitializationComplete(success: FaceLiveness.InitializationError) {
        if (success != FaceLiveness.InitializationError.NO_ERROR) {
            runOnUiThread {
                val msg = "$success contact Aware, Inc."
                mInitComplete = true
            }
        } else {
            runOnUiThread { mInitComplete = true }
        }
    }

    override fun getLivenessComponentApi(): WeakReference<FaceLiveness>? {
        return if (mLivenessApi == null) {
            null
        } else WeakReference(mLivenessApi)
    }
}