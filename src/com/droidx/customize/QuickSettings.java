/*
 * Copyright (C) 2020 Project-Awaken
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.droidx.customize;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment 
            implements Preference.OnPreferenceChangeListener {

    private static final String[] qsCustPreferences = { "qs_tile_shape",
            "qqs_num_columns", "qqs_num_columns_landscape",
            "qs_num_columns", "qs_num_columns_landscape" };
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.category_quicksettings);
        PreferenceScreen prefSet = getPreferenceScreen();

        boolean qsStyleRound = Settings.Secure.getIntForUser(getContext().getContentResolver(),
                Settings.Secure.QS_STYLE_ROUND, 1, UserHandle.USER_CURRENT) == 1;

        if (!qsStyleRound) {
            for (String key : qsCustPreferences) {
                Preference preference = prefSet.findPreference(key);
                if (preference != null) {
                    preference.setEnabled(false);
                }
            }
        }

    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }  
    
    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOMIZE;
    }

    
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.category_quicksettings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
