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

import android.os.AsyncTask;

import com.google.api.services.genomics.Genomics;

import java.io.IOException;

public abstract class GenomicsTask extends AsyncTask<Void, Void, Boolean> {
  protected final CredentialActivity activity;

  public GenomicsTask(CredentialActivity activity) {
    this.activity = activity;
  }

  @Override
  protected final Boolean doInBackground(Void... ignored) {
    try {
      performApiCall(activity.getGenomics());
      return true;
    } catch (IOException e) {
      activity.handleCredentialException(e);
      return false;
    }
  }

  protected abstract void performApiCall(Genomics client) throws IOException;
}