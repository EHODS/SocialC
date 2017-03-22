package apps.emajjaas.socialconexion;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.view.View.OnClickListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class MainActivity extends AppCompatActivity implements OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {

    private AdView adView;

    private CallbackManager callbackManager;
    private LoginButton loginButton;

    private static final int RC_SIGN_IN = 0;
    private GoogleApiClient mGoogleApiClient;

    private boolean progreso;
    private ConnectionResult connectionResult;
    private boolean firmaUsuario;
    private TextView usuario;
    private TextView email_usuario;
    private LinearLayout perfilFinalizado;
    private RelativeLayout perfilIniciado;
    private SignInButton signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        getFbKeyHash("qbVxOyyMZIx6QIq4XGpeX0WxgbE=");

        setContentView(R.layout.activity_main);

        loginButton = (LoginButton)findViewById(R.id.login_button);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Toast.makeText(MainActivity.this, "Inicio de sesion exitoso", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {

                Toast.makeText(MainActivity.this, "Inicio de sesion cancelado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {

                Toast.makeText(MainActivity.this, "Inicio de sesion no exitoso", Toast.LENGTH_SHORT).show();
            }
        });


        signInButton = (SignInButton) findViewById(R.id.signin);
        signInButton.setOnClickListener(this);

        usuario = (TextView) findViewById(R.id.usuario);
        email_usuario = (TextView) findViewById(R.id.email);

        perfilFinalizado = (LinearLayout) findViewById(R.id.perfil_finalizado);
        perfilIniciado = (RelativeLayout) findViewById(R.id.perfil_iniciado);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();

        adView = (AdView)findViewById(R.id.ad_view);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        adView.loadAd(adRequest);
    }

    public void getFbKeyHash(String packageName) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures){
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash :", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                System.out.println("KeyHash :" +Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        }catch (PackageManager.NameNotFoundException e){

        }catch (NoSuchAlgorithmException e){

        }
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void resultSingInError() {
        if (connectionResult.hasResolution()) {
            try {
                progreso = true;
                connectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                progreso = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
        }
        if (!progreso) {
            connectionResult = result;

            if (firmaUsuario) {
                resultSingInError();
            }
        }
    }


    protected void onActivityResult(int reqCode,int resCode, Intent intent){
        callbackManager.onActivityResult(reqCode, resCode, intent);

        switch (reqCode) {
            case RC_SIGN_IN:
                if (resCode == RESULT_OK) {
                    firmaUsuario = false;
                }
                progreso = false;
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        firmaUsuario = false;
        Toast.makeText(this, "!Conexion Exitosa¡", Toast.LENGTH_LONG).show();
        informacionPerfil();
    }

    private void actualizarPerfil(boolean iniciado) {
        if (iniciado) {
            perfilFinalizado.setVisibility(View.GONE);
            perfilIniciado.setVisibility(View.VISIBLE);
        } else {
            perfilFinalizado.setVisibility(View.VISIBLE);
            perfilIniciado.setVisibility(View.GONE);
        }
    }

    private void informacionPerfil() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                String nombreUsuario = person.getDisplayName();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                usuario.setText(nombreUsuario);
                email_usuario.setText(email);
                actualizarPerfil(true);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        actualizarPerfil(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signin:
                googlePlusLogin();
                break;
        }
    }

    public void signIn(View view) {
        googlePlusLogin();
    }

    public void logOut(View view) {
        googlePlusLogout();
    }

    private void googlePlusLogin() {
        if (!mGoogleApiClient.isConnected()) {
            firmaUsuario = true;
            resultSingInError();
        }
    }

    private void googlePlusLogout() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            actualizarPerfil(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.youtube:
                Intent youtubeActivity = new Intent(this, YouTube.class);
                startActivity(youtubeActivity);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (adView != null){
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (adView != null){
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (adView != null){
            adView.resume();
        }
        super.onResume();
    }
}

