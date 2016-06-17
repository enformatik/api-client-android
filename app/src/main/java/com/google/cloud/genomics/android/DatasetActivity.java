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

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.api.services.genomics.Genomics;
import com.google.api.services.genomics.model.Dataset;

import java.io.IOException;

public class DatasetActivity extends CredentialActivity {
  public static String EXTRA_ID = "datasetId";

  private String datasetId;
  private TextView projectView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dataset);

    datasetId = getIntent().getStringExtra(EXTRA_ID);
    ((TextView) findViewById(R.id.dataset_id)).setText(datasetId);

    projectView = (TextView) findViewById(R.id.dataset_project);
    checkCredential();
  }

  @Override
  protected void handleCredential() {
    new GetDataset(this).execute();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finishAfterTransition();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public class GetDataset extends GenomicsTask {

    public GetDataset(CredentialActivity activity) {
      super(activity);
    }

    @Override
    protected void performApiCall(Genomics client) throws IOException {
      final Dataset dataset = client.datasets().get(datasetId).execute();

      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          setTitle(dataset.getName());
          projectView.setText("Project: " + dataset.getProjectId());
        }
      });
    }
  }
}
