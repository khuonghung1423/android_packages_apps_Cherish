/*
 * Copyright (C) 2017 AospExtended ROM Project
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

package com.cherish.settings.fragments;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.Utils;

import com.cherish.settings.preferences.CustomSeekBarPreference;
import com.cherish.settings.preferences.SystemSettingSwitchPreference;

public class Traffic extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private ListPreference mNetTrafficLocation;
    private CustomSeekBarPreference mThreshold;
    private SystemSettingSwitchPreference mShowArrows;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.traffic);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

        mNetTrafficLocation = (ListPreference) findPreference("network_traffic_location");
        int location = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_LOCATION, 0, UserHandle.USER_CURRENT);
        mNetTrafficLocation.setValue(String.valueOf(location));
        mNetTrafficLocation.setSummary(mNetTrafficLocation.getEntry());
        mNetTrafficLocation.setOnPreferenceChangeListener(this);

        int trafvalue = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 1, UserHandle.USER_CURRENT);
        mThreshold = (CustomSeekBarPreference) findPreference("network_traffic_autohide_threshold");
        mThreshold.setValue(trafvalue);
        mThreshold.setOnPreferenceChangeListener(this);
        mShowArrows = (SystemSettingSwitchPreference) findPreference("network_traffic_arrow");

        updateTrafficLocation(location);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CHERISH_SETTINGS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mNetTrafficLocation) {
            int location = Integer.valueOf((String) objValue);
            int index = mNetTrafficLocation.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_LOCATION, location, UserHandle.USER_CURRENT);
            mNetTrafficLocation.setSummary(mNetTrafficLocation.getEntries()[index]);
            updateTrafficLocation(location);
            return true;
        } else if (preference == mThreshold) {
            int val = (Integer) objValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, val,
                    UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    public void updateTrafficLocation(int location) {
        switch(location){
            case 0:
                mThreshold.setEnabled(false);
                mShowArrows.setEnabled(false);
                Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_STATE, 0);
                Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_EXPANDED_STATUS_BAR_STATE, 0);
                break;
            case 1:
                mThreshold.setEnabled(true);
                mShowArrows.setEnabled(true);
                Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_STATE, 1);
                Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_EXPANDED_STATUS_BAR_STATE, 0);
                break;
            case 2:
                mThreshold.setEnabled(true);
                mShowArrows.setEnabled(true);
                Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_STATE, 0);
                Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_EXPANDED_STATUS_BAR_STATE, 1);
                break;
            default:
                break;
        }
    }
}
