package com.latinid.mercedes;


import static com.latinid.mercedes.util.OperacionesUtiles.saveCoors;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.acuant.acuantcommon.model.AcuantError;
import com.acuant.acuantcommon.model.Credential;
import com.acuant.acuantechipreader.AcuantEchipReader;
import com.acuant.acuantechipreader.echipreader.NfcTagReadingListener;
import com.acuant.acuantechipreader.model.NfcData;
import com.aware.face_liveness.api.FaceLiveness;
import com.aware.face_liveness.api.exceptions.FaceLivenessException;
import com.aware.face_liveness.api.interfaces.ErrorReporterCallback;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.latinid.mercedes.databinding.ActivityMainBinding;
import com.latinid.mercedes.model.local.IdentificacionModel;
import com.latinid.mercedes.model.local.PersonaModel;
import com.latinid.mercedes.ui.applicants.SubMenuFragment;
import com.latinid.mercedes.ui.home.HomeFragment;
import com.latinid.mercedes.ui.home.LoginFragment;
import com.latinid.mercedes.ui.nuevosolicitante.privacypolicy.AvisoFragment;
import com.latinid.mercedes.ui.nuevosolicitante.selfie.fragments.LivenessFragment;
import com.latinid.mercedes.ui.nuevosolicitante.selfie.fragments.SelfieAwareFragment;
import com.latinid.mercedes.ui.nuevosolicitante.selfie.rest.RestClientTask;
import com.latinid.mercedes.util.GpsTracker;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/*

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FaceLiveness.LivenessActivityPresenter, ErrorReporterCallback, SelfieAwareFragment.SelfieListener, NfcTagReadingListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FaceLiveness mLivenessApi;
    private InitializeBackgroundTask mInitializeBackgroundTask;
    private boolean mInitComplete = false;
    private FaceLiveness.InitializationError mInitializationError = FaceLiveness.InitializationError.NO_ERROR;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextView tvHeaderName;
    private TextView tvCloseSesion;
    private NfcAdapter nfcAdapter = null;
    private ExecutorService executor2 = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Mercedes_NoActionBar);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_aviso, R.id.nav_consulta)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        imagenMercedesActionBar();
        new Thread(() -> {
            mInitializeBackgroundTask = new InitializeBackgroundTask(mInitListener);
            mInitializeBackgroundTask.execute("FaceModelStandardv2.dat");
        }).start();
        saveCoors(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        View header = binding.navView.getHeaderView(0);
        tvHeaderName = (TextView) header.findViewById(R.id.nameEnrollment);
        tvCloseSesion = (TextView) header.findViewById(R.id.closeSesion);
        tvCloseSesion.setOnClickListener(
                view -> {
            tvHeaderName.setText("");
            DatosRecolectados.inSesion = false;
            replaceFragments(LoginFragment.class);
                    DrawerLayout drawe2r = binding.drawerLayout;
                    drawe2r.closeDrawer(GravityCompat.START);
        });
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }



    public void insertNameComplete(String name){
        tvHeaderName.setText(name);
    }

    private void imagenMercedesActionBar(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater =(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.image_custome_actionbar,null);
        actionBar.setCustomView(view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void replaceFragments(Class fragmentClass) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
            if(fragmentClass == AvisoFragment.class){
                NavigationView navigationView = binding.navView;
                navigationView.setCheckedItem(R.id.nav_aviso);
            }
            if(fragmentClass == SubMenuFragment.class){
                NavigationView navigationView = binding.navView;
                navigationView.setCheckedItem(R.id.nav_consulta);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.nav_host_fragment_content_main, fragment)
                .commit();


    }

    public void removerFragment(){

        getSupportFragmentManager().popBackStack();
        //getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        */
/*for (Fragment fragmento : getSupportFragmentManager().getFragments()) {
            if(fragment.getClass() == fragmento.class){

                break;
            }
        }*//*

    }

    public void activateBar(){
        getSupportActionBar().show();
    }

    public void desactivateBar(){
        getSupportActionBar().hide();
    }


    @Override
    public void onBackPressed() {


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(!DatosRecolectados.inSesion){
            return false;
        }
        NavigationView navigationView = binding.navView;
        int id = item.getItemId();
        navigationView.setCheckedItem(id);
        Fragment fragment;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (id){
            case R.id.nav_aviso:
                fragment = new AvisoFragment();
                ft.replace(R.id.nav_host_fragment_content_main, fragment);
                ft.addToBackStack(null);
                ft.commit();
                break;
            case R.id.nav_home:
                fragment = new HomeFragment();
                ft.replace(R.id.nav_host_fragment_content_main, fragment);
                ft.addToBackStack(null);
                ft.commit();
                break;
            case R.id.nav_consulta:
                fragment = new SubMenuFragment();
                ft.replace(R.id.nav_host_fragment_content_main, fragment);
                ft.addToBackStack(null);
                ft.commit();
                break;
            default:
        }
        DrawerLayout drawer = binding.drawerLayout;
        drawer.closeDrawer(GravityCompat.START);
        navigationView.setNavigationItemSelectedListener(this);

        return false;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String country = DatosRecolectados.country;
        String docNumber = DatosRecolectados.docNumber;
        String dateOfBirth = DatosRecolectados.dob;
        String dateOfExpiry = DatosRecolectados.doe;

        executor2.execute(() -> {
            if(DatosRecolectados.capNfc){
                AcuantEchipReader.readNfcTag(this, intent, docNumber, dateOfBirth,
                        dateOfExpiry,  true,  this);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.nfcAdapter != null) {
            this.nfcAdapter.disableForegroundDispatch(this);
        }

    }

    @Override
    public void tagReadStatus(@NonNull String s) {

    }

    @Override
    public void tagReadSucceeded(@NonNull NfcData nfcData) {
        executor.execute(() -> {
            DatosRecolectados.cardDetails = nfcData;
        });
    }


    @Override
    public void onError(@NonNull AcuantError acuantError) {
        if (this.nfcAdapter != null) {
            try {
                this.nfcAdapter.disableForegroundDispatch(this);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }
    }

    */
/**
     * <b>LÓGICA COMPLEMENTARIA AL FRAGMENTO SELFIEAWAREFRAGMENT</b>
     * <p>Metodos correspondientes para activar el SDK de Knomi Aware</p>
     * <p>Metodos finales para el termino de la captura facial</p>
     * <p>Tiene su propia metodo de inflar fragmento para que solo afecto el container de la selfie y no el general</p>
     * <p>Metodo workflow, metodo importante para lanzar la capura de la selfie</p>
     *//*

    @Override
    public void onWorkflowSelected(String workflowName, String id, String overrideJson) {

        boolean isRunning = false;
        String mUsername = "Latin";
        String mCaptureTimeout = "0";
        boolean mImageCaptureProperty = true;
        boolean mCaptureOnDevice = true;
        boolean mCheckPhonePosition = false;

        if (mCaptureTimeout.equals(""))
            mCaptureTimeout = "0";

        // Note the following calls must be in the correct order.
        // Must set properties before calling selectWorkflow.
        try {

            mLivenessApi.setProperty(FaceLiveness.PropertyTag.USERNAME, mUsername);
            mLivenessApi.setProperty(FaceLiveness.PropertyTag.CONSTRUCT_IMAGE, mImageCaptureProperty);
            mLivenessApi.setProperty(FaceLiveness.PropertyTag.TIMEOUT, Double.parseDouble(mCaptureTimeout));
            mLivenessApi.setProperty(FaceLiveness.PropertyTag.CAPTURE_ON_DEVICE, mCaptureOnDevice);
            mLivenessApi.setProperty(FaceLiveness.PropertyTag.CHECK_PHONE_POSITION, mCheckPhonePosition);
        } catch (FaceLivenessException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = "Invalid property setting!!!";
                    Toast t = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
            });
            e.printStackTrace();
        }

        // select a workflow
        try {
            mLivenessApi.selectWorkflow(getApplicationContext(), workflowName, overrideJson);
        } catch (FaceLivenessException e) {
            if (e.getErrorCode() == FaceLivenessException.ErrorCode.InsufficientCameraPermission) {
                //clearMarkAsAsked(CAMERA);
            } else if (e.getErrorCode() == FaceLivenessException.ErrorCode.InsufficientWriteSettingsPermission) {
                // clearMarkAsAsked(WRITE_SETTINGS);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String message = "Invalid workflow setting or incorrect model specified!!!";
                        Toast t = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                    }
                });
            }
            e.printStackTrace();
            return;
        }

        selectFragment();
    }

    public void selectFragment() {

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment2 = null;
        fragment2 = LivenessFragment.newInstance(this);

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_containerr, fragment2, "live");
        ft.addToBackStack("execute");
        ft.commit();

    }

    public void popUpToBaseFragment() {
        FragmentManager fm = getSupportFragmentManager();

        int count = fm.getBackStackEntryCount();
        if (count > 1) {
            FragmentManager.BackStackEntry backStackEntry = fm.getBackStackEntryAt(count - 2);
            fm.popBackStack("execute", 0);
        }
    }

    public void onCaptureEnd() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag("live")).commit();
                System.out.println("Si llegue");
                //popUpToBaseFragment();
                removerFragment();
                //ShowHourglassDirect();
            }
        });

        try {
            String serverPackage = mLivenessApi.getServerPackage();
            RestClientTask postTask = new RestClientTask(this);

            postTask.executeLiveness(serverPackage, 3);

        } catch (FaceLivenessException e) {
            //getServerPackage can throw an exception on an error
            e.printStackTrace();
        }
    }

    public void onCaptureTimedout() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager().beginTransaction().remove( getSupportFragmentManager().findFragmentByTag("live")).commit();

            }
        });
    }

    public void onCaptureAbort() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager().beginTransaction().remove( getSupportFragmentManager().findFragmentByTag("live")).commit();

            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }



    public interface ModelInitializationListener {
        public void onInitializationComplete(boolean success, String modelName);
    }

    private ModelInitializationListener mInitListener = new ModelInitializationListener() {
        @Override
        public void onInitializationComplete(final boolean success, final String modelName) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!success) {
                        String message = "Could not initialize model " + modelName + " not found!" ;
                        Toast t = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                    }else{
                        Log.i("SELFIE AWARE", "AWARE INICIADO CORRECTAMENTE ");
                    }
                }
            });
        }
    };

    public class InitializeBackgroundTask extends AsyncTask<String, Void, Void> {
        private boolean mCouldNotOpenModel = false;
        private String mModelName = "";
        private ModelInitializationListener mInitializationListener;

        InitializeBackgroundTask(ModelInitializationListener listener) {
            mInitializationListener = listener;
            mCouldNotOpenModel = false;
            mModelName = "";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(final String[] params) {
            mModelName = params[0];
            try {
                FaceLiveness.setStaticProperty(FaceLiveness.StaticPropertyTag.FACE_MODEL, params[0]);
                mLivenessApi = new FaceLiveness( getApplicationContext() );
            } catch (FaceLivenessException e) {
                e.printStackTrace();
            }

            try {
                // initialize the library, wait for callback...
                mLivenessApi.initializeFaceLivenessLibrary(MainActivity.this);
            }
            catch (FaceLivenessException e) {
                mCouldNotOpenModel = true;
                return null;
            }

            synchronized (InitializeBackgroundTask.this) {
                while (!mInitComplete && !isCancelled()) {
                    try {
                        Log.i("LIVENESS_SAMPLE_TAG", "Launch starting WAIT");
                        InitializeBackgroundTask.this.wait(3000);
                        Log.i("LIVENESS_SAMPLE_TAG", "Launch completing WAIT");
                    } catch (InterruptedException e) {
                        Log.e("LIVENESS_SAMPLE_TAG", "Launch INTERRUPTED: " + e.getMessage());
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void o) {
            super.onPostExecute(o);
            mInitializationListener.onInitializationComplete(!mCouldNotOpenModel, mModelName);
        }
    }

    @Override
    public void onErrorReporter(int errorCode, String info, Context context) {
        Log.d("LIVENESS_SAMPLE_TAG",
                "  ---------------------------------       onErrorReporter: " + info + " " + errorCode);
    }

    @Override
    public void onInitializationComplete(FaceLiveness.InitializationError success) {
        if (success != FaceLiveness.InitializationError.NO_ERROR) {
            runOnUiThread(() -> {
                String msg = success.toString() + " contact Aware, Inc.";
                mInitComplete = true;
            });
        } else {
            runOnUiThread(() -> mInitComplete = true);
        }
    }

    @Override
    public WeakReference<FaceLiveness> getLivenessComponentApi() {
        if (mLivenessApi == null) {
            return null;
        }
        return new WeakReference<FaceLiveness>(mLivenessApi);
    }
}*/
