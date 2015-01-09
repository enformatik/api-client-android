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
package com.google.cloud.genomics.android.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.cloud.genomics.android.CredentialActivity;
import com.google.cloud.genomics.android.R;
import com.google.cloud.genomics.android.SettingsActivity;
import com.google.cloud.genomics.android.adapters.JobListAdapter;

public class JobListFragment extends ListFragment {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String projectString = SettingsActivity.getProjectNumber(getActivity());
    if (projectString == null) {
      Toast.makeText(getActivity(), "Set your project number in order to display Job status",
          Toast.LENGTH_LONG).show();
      launchSettings();

    } else {
      try {
        Long projectNumber = Long.valueOf(projectString);
        setListAdapter(new JobListAdapter((CredentialActivity) getActivity(), projectNumber));
      } catch (NumberFormatException e) {
        Toast.makeText(getActivity(), "Your project number isn't valid",
            Toast.LENGTH_SHORT).show();
        launchSettings();
      }
    }
  }

  private void launchSettings() {
    startActivity(new Intent(getActivity(), SettingsActivity.class));
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    activity.setTitle(R.string.job_section_title);
  }
}
