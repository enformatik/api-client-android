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
package com.google.cloud.genomics.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.model.Dataset;
import com.google.api.services.genomics.model.ListDatasetsResponse;
import com.google.cloud.genomics.android.CredentialActivity;
import com.google.cloud.genomics.android.GenomicsTask;
import com.google.cloud.genomics.android.R;
import com.google.common.base.Strings;

import java.io.IOException;

public class DatasetListAdapter extends ArrayAdapter<Dataset> {
  private static final String DATASET_FIELDS = "datasets(name,id)";

  private LayoutInflater layoutInflater;

  public class GetDatasets extends GenomicsTask {
    private String projectId;

    public GetDatasets(CredentialActivity activity, String projectId) {
        super(activity);
        this.projectId = projectId;
    }

    @Override
    protected void performApiCall(Genomics client) throws IOException {
      // TODO: Use paginator
      String pageToken = null;
      do {
        final ListDatasetsResponse response = client.datasets().list()
            .setProjectId(projectId)
            .setFields(DATASET_FIELDS)
            .setPageToken(pageToken).execute();

        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            addAll(response.getDatasets());
          }
        });

        pageToken = response.getNextPageToken();
      } while (!Strings.isNullOrEmpty(pageToken));
    }
  }

  private static class ViewHolder {
    TextView title;
    TextView subtitle;
  }

  public DatasetListAdapter(CredentialActivity activity, String projectId) {
    super(activity, android.R.layout.simple_list_item_1);
    layoutInflater = (LayoutInflater) getContext().getSystemService(
        Context.LAYOUT_INFLATER_SERVICE);

    new GetDatasets(activity, projectId).execute();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;

    if (convertView == null) {
      convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null);
      convertView.setTransitionName(getContext().getString(R.string.main_transition_name));
      holder = new ViewHolder();
      holder.title = (TextView) convertView.findViewById(android.R.id.text1);
      holder.subtitle = (TextView) convertView.findViewById(android.R.id.text2);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    final Dataset dataset = getItem(position);
    holder.title.setText(dataset.getName());
    holder.subtitle.setText(dataset.getId());
    return convertView;
  }
}
