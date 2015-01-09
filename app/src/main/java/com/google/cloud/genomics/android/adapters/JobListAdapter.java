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
import com.google.api.services.genomics.model.Job;
import com.google.api.services.genomics.model.JobRequest;
import com.google.api.services.genomics.model.SearchJobsRequest;
import com.google.api.services.genomics.model.SearchJobsResponse;
import com.google.cloud.genomics.android.CredentialActivity;
import com.google.cloud.genomics.android.GenomicsTask;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

public class JobListAdapter extends ArrayAdapter<Job> {
  private static final String JOB_FIELDS = "jobs(request,created)";

  private static Map<String, String> JOB_TYPES = Maps.newHashMap();
  static {
    JOB_TYPES.put("IMPORT_VARIANTS", "Import variants");
    JOB_TYPES.put("IMPORT_READSETS", "Import reads");
    JOB_TYPES.put("EXPORT_VARIANTS", "Export variants");
    JOB_TYPES.put("EXPORT_READSETS", "Export reads");
  }

  private LayoutInflater layoutInflater;

  public class GetJobs extends GenomicsTask {
    private Long projectNumber;

    public GetJobs(CredentialActivity activity, Long projectNumber) {
      super(activity);
      this.projectNumber = projectNumber;
    }

    @Override
    protected void performApiCall(Genomics client) throws IOException {
      // TODO: Use paginator
      String pageToken = null;
      do {
        final SearchJobsResponse response = client.jobs().search(
            new SearchJobsRequest().setProjectNumber(projectNumber).setPageToken(pageToken))
            .setFields(JOB_FIELDS)
            .execute();

        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            addAll(response.getJobs());
          }
        });

        pageToken = response.getNextPageToken();
      } while (pageToken != null);
    }
  }

  private static class ViewHolder {
    TextView title;
    TextView subtitle;
  }

  public JobListAdapter(CredentialActivity activity, Long projectNumber) {
    super(activity, android.R.layout.simple_list_item_1);
    layoutInflater = (LayoutInflater) activity.getSystemService(
        Context.LAYOUT_INFLATER_SERVICE);
    new GetJobs(activity, projectNumber).execute();
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

    Job job = getItem(position);
    holder.title.setText(getJobDescription(job));
    holder.subtitle.setText(getCreatedDate(job));
    return convertView;
  }

  private String getCreatedDate(Job job) {
    Date date = new Date(job.getCreated());
    DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
    return dateFormat.format(date);
  }

  private String getJobDescription(Job job) {
    JobRequest request = job.getRequest();
    if (request == null) {
      return "Unknown job";
    }

    String requestType = request.getType();
    if (JOB_TYPES.containsKey(requestType)) {
      requestType = JOB_TYPES.get(request.getType());
    }

    String source = Joiner.on(',').join(request.getSource());
    String destination = Joiner.on(',').join(request.getDestination());

    return requestType + " from " + source + " to " + destination;
  }
}
