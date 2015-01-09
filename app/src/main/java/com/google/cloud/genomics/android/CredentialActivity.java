/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.genomics.android;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.GenomicsScopes;

import java.io.IOException;
import java.util.Collections;

public abstract class CredentialActivity extends Activity {
  private static final int REQUEST_ACCOUNT_PICKER = 10070;
  private static final int REQUEST_GOOGLE_PLAY_SERVICES = 10080;
  private static final int REQUEST_AUTHORIZATION = 10090;

  protected boolean credentialAvailable = false;

  private static String getErrorMessage(Throwable t) {
    String message = t.getMessage();
    if (t instanceof GoogleJsonResponseException) {
      GoogleJsonError details = ((GoogleJsonResponseException) t).getDetails();
      if (details != null) {
        message = details.getMessage();
      }
    } else if (t.getCause() instanceof GoogleAuthException) {
      message = t.getCause().getMessage();
    }
    return message;
  }

  private static boolean isDeviceOnline(Context context) {
    ConnectivityManager connMgr = (ConnectivityManager)
        context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    return networkInfo != null && networkInfo.isConnected();
  }

  private static GoogleAccountCredential getCredential(Context context) {
    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context,
        Collections.singleton(GenomicsScopes.GENOMICS));
    credential.setSelectedAccountName(SettingsActivity.getAccountName(context));
    return credential;
  }

  protected void checkCredential() {
    GoogleAccountCredential credential = getCredential(this);

    if (credential.getSelectedAccountName() == null) {
      startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    } else if (!isDeviceOnline(this)) {
      Toast.makeText(this, "No network connection available, this app won't show any data.",
          Toast.LENGTH_SHORT).show();
    } else {
      credentialAvailable = true;
      handleCredential();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case REQUEST_AUTHORIZATION:
        if (resultCode != Activity.RESULT_OK) {
          Toast.makeText(this, "Authorization failed. Please try again.",
              Toast.LENGTH_SHORT).show();
        }
        checkCredential();
        break;
      case REQUEST_GOOGLE_PLAY_SERVICES:
        if (resultCode != Activity.RESULT_OK) {
          Toast.makeText(this, "Google Play Services must be updated to use this application.",
              Toast.LENGTH_SHORT).show();
        } else {
          checkCredential();
        }
        break;
      case REQUEST_ACCOUNT_PICKER:
        if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
          String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
          if (accountName != null) {
            SettingsActivity.saveAccountName(this, accountName);
          }
        } else {
          Toast.makeText(this, "You must select an account", Toast.LENGTH_SHORT).show();
        }
        checkCredential();
        break;
    }
  }

  public void handleCredentialException(final IOException e) {
    if (e instanceof GooglePlayServicesAvailabilityIOException) {
      // The Google Play services APK is old, disabled, or not present.
      // Show a dialog created by Google Play services that allows
      // the user to update the APK
      runOnUiThread(new Runnable() {
        public void run() {
          Dialog dialog =
              GooglePlayServicesUtil.getErrorDialog(
                  ((GooglePlayServicesAvailabilityIOException) e).getConnectionStatusCode(),
                  CredentialActivity.this,
                  REQUEST_GOOGLE_PLAY_SERVICES);
          dialog.show();
        }
      });

    } else if (e instanceof UserRecoverableAuthIOException) {
      // Unable to authenticate, such as when the user has not yet granted
      // the app access to the account, but the user can fix this.
      // Forward the user to an activity in Google Play services.
      startActivityForResult(
          ((UserRecoverableAuthIOException) e).getIntent(), REQUEST_AUTHORIZATION);

    } else {
      // We give up on all other IO exceptions
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(CredentialActivity.this, getErrorMessage(e),
              Toast.LENGTH_LONG).show();
        }
      });
    }
  }

  public Genomics getGenomics() {
    return new Genomics.Builder(AndroidHttp.newCompatibleTransport(),
        GsonFactory.getDefaultInstance(), getCredential(this))
        .setApplicationName("GoogleGenomics-AndroidClient").build();
  }

  protected abstract void handleCredential();
}
