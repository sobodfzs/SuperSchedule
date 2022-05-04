package com.example.SuperSchedule;

import static com.firebase.ui.auth.AuthUI.EMAIL_LINK_PROVIDER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.SuperSchedule.databinding.ActivityLoginBinding;
import com.example.SuperSchedule.databinding.ActivityMainBinding;
import com.example.SuperSchedule.entity.Customer;
import com.example.SuperSchedule.viewmodel.CustomerViewModel;
import com.example.SuperSchedule.viewmodel.UserViewModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.firebase.ui.auth.util.ExtraConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.auth.UserInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private @NonNull ActivityLoginBinding binding;
    private com.example.SuperSchedule.viewmodel.UserViewModel userViewModel;
    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, LoginActivity.class);
    }
    // [START auth_fui_create_launcher]
    // See: https://developer.android.com/training/basics/intents/result
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );
    // [END auth_fui_create_launcher]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());

        setContentView(R.layout.activity_login);
        userViewModel =
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
                        .create(UserViewModel.class);
        userViewModel.getUser().observe(this, new
                Observer<FirebaseUser>() {
                    @Override
                    public void onChanged(@Nullable final FirebaseUser user) {
                        if (user == null) {
                            binding.signedState.setText("Unsigned (Limited functions)");
                            binding.signInOut.setText("SIGN IN");
                            binding.userEmail.setText("");
                            binding.userPhoneNumber.setText("");
                            binding.userDisplayName.setText("UNSIGNED");
                            //set sign in
                        } else {
                            binding.signedState.setText("You are signed in!");
                            binding.signInOut.setText("SIGN OUT");
                            binding.userEmail.setText(
                                    TextUtils.isEmpty(user.getEmail()) ? "No email" : "Email: "+user.getEmail());
                            binding.userPhoneNumber.setText(
                                    TextUtils.isEmpty(user.getPhoneNumber()) ? "No phone number" : "Phone: "+user.getPhoneNumber());
                            binding.userDisplayName.setText(
                                    TextUtils.isEmpty(user.getDisplayName()) ? "No display name" : "Name: "+user.getDisplayName());
                            //set sign out
                        }
                    }
                });
        @SuppressLint("RestrictedApi") IdpResponse response = getIntent().getParcelableExtra(ExtraConstants.IDP_RESPONSE);
        setContentView(binding.getRoot());
        //populateProfile();
        //populateIdpToken(response);

        binding.deleteAccount.setOnClickListener(view -> deleteAccountClicked());

        binding.signInOut.setOnClickListener(view -> signIn_Out());
        userViewModel.update();
    }


    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            // ...
        } else {
            response.getError().getErrorCode();
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            handleSignInResponse(result.getResultCode(), response);


        }
        userViewModel.update();
    }
    // [END auth_fui_result]

    private void handleSignInResponse(int resultCode, @Nullable IdpResponse response) {
        // Successfully signed in
        if (resultCode == RESULT_OK) {
            showSnackbar(R.string.signed_in_header);
            return;
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                showSnackbar(R.string.sign_in_cancelled);
                return;
            }

            if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;
            }


            if (response.getError().getErrorCode() == ErrorCodes.ERROR_USER_DISABLED) {
                showSnackbar(R.string.account_disabled);
                return;
            }

            showSnackbar(R.string.unknown_error);
            Log.e("LoginActivity", "Sign-in error: ", response.getError());
        }
    }
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(binding.getRoot(), errorMessageRes, Snackbar.LENGTH_LONG).show();
    }
    public void signIn_Out() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();//User
        boolean isSigned=(currentUser != null);
        if(isSigned==false){
            signIn();
        }
        else{
            signOut();
        }
    }

    public void deleteAccountClicked() {
        new MaterialAlertDialogBuilder(this)
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes, nuke it!", (dialogInterface, i) -> deleteAccount())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAccount() {
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        //startActivity(AuthUiActivity.createIntent(SignedInActivity.this));
                        //
                        finish();
                    } else {
                        showSnackbar(R.string.delete_account_failed);
                    }
                });
    }

    /*private void populateProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.getPhotoUrl() != null) {
            /*GlideApp.with(this)
                    .load(user.getPhotoUrl())
                    .fitCenter()
                    .into(binding.userProfilePicture);
        }

        binding.userEmail.setText(
                TextUtils.isEmpty(user.getEmail()) ? "No email" : "Email: "+user.getEmail());
        binding.userPhoneNumber.setText(
                TextUtils.isEmpty(user.getPhoneNumber()) ? "No phone number" : "Phone: "+user.getPhoneNumber());
        binding.userDisplayName.setText(
                TextUtils.isEmpty(user.getDisplayName()) ? "No display name" : "Name: "+user.getDisplayName());
        if (user == null) {
            binding.signedState.setText("Unsigned (Limited functions)");
            binding.signInOut.setText("SIGN IN");
            //set sign in
        }
        else{
            binding.signedState.setText("You are signed in!");
            binding.signInOut.setText("SIGN OUT");
            //set sign out
        }
        if (response == null) {
            binding.userIsNew.setVisibility(View.GONE);
        } else {
            binding.userIsNew.setVisibility(View.VISIBLE);
            binding.userIsNew.setText(response.isNewUser() ? "New user" : "Existing user");
        }*/

       /* List<String> providers = new ArrayList<>();
        if (user.getProviderData().isEmpty()) {
            providers.add(getString(R.string.providers_anonymous));
        } else {
            for (UserInfo info : user.getProviderData()) {
                switch (info.getProviderId()) {
                    case GoogleAuthProvider.PROVIDER_ID:
                        providers.add(getString(R.string.providers_google));
                        break;
                    case FacebookAuthProvider.PROVIDER_ID:
                        providers.add(getString(R.string.providers_facebook));
                        break;
                    case TwitterAuthProvider.PROVIDER_ID:
                        providers.add(getString(R.string.providers_twitter));
                        break;
                    case EmailAuthProvider.PROVIDER_ID:
                        providers.add(getString(R.string.providers_email));
                        break;
                    case PhoneAuthProvider.PROVIDER_ID:
                        providers.add(getString(R.string.providers_phone));
                        break;
                    case EMAIL_LINK_PROVIDER:
                        providers.add(getString(R.string.providers_email_link));
                        break;
                    case FirebaseAuthProvider.PROVIDER_ID:
                        // Ignore this provider, it's not very meaningful
                        break;
                    default:
                        providers.add(info.getProviderId());
                }
            }
        }

        mBinding.userEnabledProviders.setText(getString(R.string.used_providers, providers));}
        */

    /*private void populateIdpToken(@Nullable IdpResponse response) {
        String token = null;
        String secret = null;
        if (response != null) {
            token = response.getIdpToken();
            secret = response.getIdpSecret();
        }

        View idpTokenLayout = findViewById(R.id.idp_token_layout);
        if (token == null) {
            idpTokenLayout.setVisibility(View.GONE);
        } else {
            idpTokenLayout.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.idp_token)).setText(token);
        }

        View idpSecretLayout = findViewById(R.id.idp_secret_layout);
        if (secret == null) {
            idpSecretLayout.setVisibility(View.GONE);
        } else {
            idpSecretLayout.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.idp_secret)).setText(secret);
        }
    }*/
    public void signIn() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(

                new AuthUI.IdpConfig.EmailBuilder().build()
                //new AuthUI.IdpConfig.PhoneBuilder().build()
                //new AuthUI.IdpConfig.GoogleBuilder().build(),
                //new AuthUI.IdpConfig.FacebookBuilder().build(),
                //new AuthUI.IdpConfig.TwitterBuilder().build()
        );

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
        // [END auth_fui_create_intent]
    }
    // [START auth_fui_result]
    public void signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //startActivity(LoginActivity.createIntent(SignedInActivity.this));
                        //set string "SIGN IN"
                        showSnackbar(R.string.sign_out_success);
                    } else {
                        Log.w(TAG, "signOut:failure", task.getException());
                        showSnackbar(R.string.sign_out_failed);
                    }
                    userViewModel.update();
                });
        // [END auth_fui_signout]
    }

    public void delete() {
        // [START auth_fui_delete]
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        userViewModel.update();
                        // ...
                    }
                });
        // [END auth_fui_delete]
    }

    public void themeAndLogo() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();

        // [START auth_fui_theme_logo]
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.my_great_logo)      // Set logo drawable
                .setTheme(R.style.MySuperAppTheme)      // Set theme
                .build();
        signInLauncher.launch(signInIntent);
        // [END auth_fui_theme_logo]
    }

    public void privacyAndTerms() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();

        // [START auth_fui_pp_tos]
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTosAndPrivacyPolicyUrls(
                        "https://example.com/terms.html",
                        "https://example.com/privacy.html")
                .build();
        signInLauncher.launch(signInIntent);
        // [END auth_fui_pp_tos]
    }

    public void emailLink() {
        // [START auth_fui_email_link]
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName(
                        /* yourPackageName= */ "...",
                        /* installIfNotAvailable= */ true,
                        /* minimumVersion= */ null)
                .setHandleCodeInApp(true) // This must be set to true
                .setUrl("https://google.com") // This URL needs to be whitelisted
                .build();

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder()
                        .enableEmailLinkSignIn()
                        .setActionCodeSettings(actionCodeSettings)
                        .build()
        );
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
        // [END auth_fui_email_link]
    }
    @SuppressLint("RestrictedApi")
    public void catchEmailLink() {

        List<AuthUI.IdpConfig> providers = Collections.emptyList();

        // [START auth_fui_email_link_catch]
        if (AuthUI.canHandleIntent(getIntent())) {
            if (getIntent().getExtras() == null) {
                return;
            }
            String link = getIntent().getExtras().getString(ExtraConstants.EMAIL_LINK_SIGN_IN);
            if (link != null) {
                Intent signInIntent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setEmailLink(link)
                        .setAvailableProviders(providers)
                        .build();
                signInLauncher.launch(signInIntent);
            }
        }
        // [END auth_fui_email_link_catch]
    }

}

