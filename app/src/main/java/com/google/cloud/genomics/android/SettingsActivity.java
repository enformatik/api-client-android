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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.genomics.GenomicsScopes;

import java.util.Collections;
public class SettingsActivity extends Activity {

  private static final String PREF_ACCOUNT_NAME = "account_name";
  private static final String PREF_PROJECT_ID = "project_id";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getFragmentManager().beginTransaction()
        .replace(android.R.id.content, new GeneralPreferenceFragment())
        .commit();
  }

  public static class GeneralPreferenceFragment extends PreferenceFragment {
    private static final int REQUEST_ACCOUNT_PICKER = 2;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      switch (requestCode) {
        case REQUEST_ACCOUNT_PICKER:
          if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
            String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
            if (accountName != null) {
              saveAccountName(getActivity(), accountName);
              findPreference(PREF_ACCOUNT_NAME).setSummary(accountName);
            }
          }
          break;
      }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.pref_general);

      // Display the current project id as the preference summary
      setSummaryToValue(findPreference(PREF_PROJECT_ID));

      // The account preference opens up the account chooser dialog
      Preference accountNamePreference = findPreference(PREF_ACCOUNT_NAME);
      setSummaryToValue(accountNamePreference);
      accountNamePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          startActivityForResult(GoogleAccountCredential.usingOAuth2(getActivity(),
              Collections.singleton(GenomicsScopes.GENOMICS)).newChooseAccountIntent(),
              REQUEST_ACCOUNT_PICKER);
          return true;
        }
      });
    }

    private void setSummaryToValue(Preference preference) {
      preference.setOnPreferenceChangeListener(
          new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
              if (newValue != null) {
                preference.setSummary(newValue.toString());
              }
              return true;
            }
          });
      preference.getOnPreferenceChangeListener().onPreferenceChange(
          preference, PreferenceManager.getDefaultSharedPreferences(getActivity())
              .getString(preference.getKey(), null));
    }
  }

  public static String getAccountName(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context)
        .getString(PREF_ACCOUNT_NAME, null);
  }

  public static void saveAccountName(Context context, String accountName) {
    PreferenceManager.getDefaultSharedPreferences(context).edit()
        .putString(PREF_ACCOUNT_NAME, accountName).commit();
  }

  public static String getProjectId(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context)
        .getString(PREF_PROJECT_ID, null);
  }
}
