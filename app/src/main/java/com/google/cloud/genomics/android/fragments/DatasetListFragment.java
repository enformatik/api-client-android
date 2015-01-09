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
import android.app.ActivityOptions;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.api.services.genomics.model.Dataset;
import com.google.cloud.genomics.android.CredentialActivity;
import com.google.cloud.genomics.android.DatasetActivity;
import com.google.cloud.genomics.android.R;
import com.google.cloud.genomics.android.adapters.DatasetListAdapter;

public class DatasetListFragment extends ListFragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setListAdapter(new DatasetListAdapter((CredentialActivity) getActivity()));
  }

  @Override
  public void onListItemClick(ListView listView, View view, int position, long id) {
    super.onListItemClick(listView, view, position, id);

    Dataset dataset = (Dataset) getListAdapter().getItem(position);
    Intent intent = new Intent(getActivity(), DatasetActivity.class);
    intent.putExtra(DatasetActivity.EXTRA_ID, dataset.getId());

    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
        view, getString(R.string.main_transition_name));
    startActivity(intent, options.toBundle());
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    activity.setTitle(R.string.dataset_section_title);
  }
}
