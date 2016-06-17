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
import com.google.api.services.genomics.model.Operation;
import com.google.api.services.genomics.model.ListOperationsResponse;
import com.google.cloud.genomics.android.CredentialActivity;
import com.google.cloud.genomics.android.GenomicsTask;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

public class JobListAdapter extends ArrayAdapter<Operation> {
  private static final String OPERATIONS_FIELDS = "operations(done,name,response)";
  private static final int PAGE_SIZE = 10;

  private static Map<String, String> JOB_TYPES = Maps.newHashMap();
  static {
    JOB_TYPES.put("IMPORT_VARIANTS", "Import variants");
    JOB_TYPES.put("IMPORT_READSETS", "Import reads");
    JOB_TYPES.put("EXPORT_VARIANTS", "Export variants");
    JOB_TYPES.put("EXPORT_READSETS", "Export reads");
  }

  private LayoutInflater layoutInflater;

  public class GetOperations extends GenomicsTask {
    private String projectId;

    public GetOperations(CredentialActivity activity, String projectId) {
      super(activity);
      this.projectId = projectId;
    }

    @Override
    protected void performApiCall(Genomics client) throws IOException {
      // TODO: Use paginator
      String pageToken = null;
      do {
        final ListOperationsResponse response = client.operations().list("operations")
          .setName("operations")
          .setFilter("projectId=" + projectId)
          .setPageToken(pageToken)
          .setFields(OPERATIONS_FIELDS)
          .setPageSize(PAGE_SIZE)
          .execute();

        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            addAll(response.getOperations());
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

  public JobListAdapter(CredentialActivity activity, String projectId) {
    super(activity, android.R.layout.simple_list_item_1);
    layoutInflater = (LayoutInflater) activity.getSystemService(
        Context.LAYOUT_INFLATER_SERVICE);
    new GetOperations(activity, projectId).execute();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;

    if (convertView == null) {
      convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null);
      holder = new ViewHolder();
      holder.title = (TextView) convertView.findViewById(android.R.id.text1);
      holder.subtitle = (TextView) convertView.findViewById(android.R.id.text2);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    // TODO: Add current status (cancelled, error, etc)
    // TODO: Allow cancellation
    // TODO: Allow retrying

    Operation operation = getItem(position);
    holder.title.setText(operation.getName());
    holder.subtitle.setText("Done: " + operation.getDone().toString());
    return convertView;
  }
}
