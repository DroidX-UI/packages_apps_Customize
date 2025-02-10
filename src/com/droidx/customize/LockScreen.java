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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.text.TextUtils;
import android.provider.SearchIndexableResource;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
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

import com.droidx.support.preferences.SecureSettingSwitchPreference;
import com.droidx.support.utils.ImageUtils;

@SearchIndexable
public class LockScreen extends SettingsPreferenceFragment 
            implements Preference.OnPreferenceChangeListener {

    private static final String KEY_FINGERPRINT_CATEGORY = "lock_screen_fingerprint_category";
    private static final String KEY_SCREEN_OFF_UDFPS = "screen_off_udfps_enabled";
    private static final String CUSTOM_IMAGE_REQUEST_CODE_KEY = "lockscreen_custom_image";
    private static final int CUSTOM_IMAGE_REQUEST_CODE = 1001;

    private Preference mCustomImagePreference;
    private PreferenceCategory mFingerprintCategory;
    private SecureSettingSwitchPreference mScreenOffUdfps;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.category_lockscreen);
        PreferenceScreen prefSet = getPreferenceScreen();

        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final Resources res = context.getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mCustomImagePreference = findPreference(CUSTOM_IMAGE_REQUEST_CODE_KEY);
        int clockStyle = Settings.Secure.getIntForUser(getContext().getContentResolver(), "clock_style", 0, UserHandle.USER_CURRENT);
        String imagePath = Settings.System.getString(getContext().getContentResolver(), "custom_aod_image_uri");
        if (imagePath != null && clockStyle > 0) {
            mCustomImagePreference.setSummary(imagePath);
            mCustomImagePreference.setEnabled(true);
        } else if (clockStyle == 0) {
            mCustomImagePreference.setSummary(getContext().getString(R.string.custom_aod_image_not_supported));
            mCustomImagePreference.setEnabled(false);
        }

        mFingerprintCategory = (PreferenceCategory) findPreference(KEY_FINGERPRINT_CATEGORY);
        mScreenOffUdfps = (SecureSettingSwitchPreference) findPreference(KEY_SCREEN_OFF_UDFPS);

        FingerprintManager fingerprintManager = (FingerprintManager)
                getActivity().getSystemService(Context.FINGERPRINT_SERVICE);

        if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
            prefScreen.removePreference(mFingerprintCategory);
        } else {
            boolean screenOffUdfpsAvailable = res.getBoolean(
                    com.android.internal.R.bool.config_supportScreenOffUdfps) ||
                    !TextUtils.isEmpty(res.getString(
                            com.android.internal.R.string.config_dozeUdfpsLongPressSensorType));

            if (!screenOffUdfpsAvailable) {
                mFingerprintCategory.removePreference(mScreenOffUdfps);
            }
        }
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }  

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mCustomImagePreference) {
            try {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, CUSTOM_IMAGE_REQUEST_CODE);
            } catch(Exception e) {
                Toast.makeText(getContext(), R.string.quick_settings_header_needs_gallery, Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == CUSTOM_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && result != null) {
            Uri imgUri = result.getData();
            if (imgUri != null) {
                String savedImagePath = ImageUtils.saveImageToInternalStorage(getContext(), imgUri, "lockscreen_aod_image", "LOCKSCREEN_CUSTOM_AOD_IMAGE");
                if (savedImagePath != null) {
                    ContentResolver resolver = getContext().getContentResolver();
                    Settings.System.putStringForUser(resolver, "custom_aod_image_uri", savedImagePath, UserHandle.USER_CURRENT);
                    mCustomImagePreference.setSummary(savedImagePath);
                }
            }
        }
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
                    sir.xmlResId = R.xml.category_lockscreen;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    final Resources res = context.getResources();

                FingerprintManager fingerprintManager = (FingerprintManager)
                    context.getSystemService(Context.FINGERPRINT_SERVICE);

                if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
                    keys.add(KEY_SCREEN_OFF_UDFPS);
                } else {
                    boolean screenOffUdfpsAvailable = res.getBoolean(
                        com.android.internal.R.bool.config_supportScreenOffUdfps) ||
                        !TextUtils.isEmpty(res.getString(
                            com.android.internal.R.string.config_dozeUdfpsLongPressSensorType));
                    if (!screenOffUdfpsAvailable) {
                        keys.add(KEY_SCREEN_OFF_UDFPS);
                    }
                }
                return keys;
                }
            };
}