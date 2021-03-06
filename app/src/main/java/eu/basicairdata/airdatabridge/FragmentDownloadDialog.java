/**
 * FragmentDownloadDialog - Java Class for Android
 * Created by G.Capelli (BasicAirData) on 21/01/2018
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.basicairdata.airdatabridge;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class FragmentDownloadDialog extends DialogFragment {

    AirDataBridgeApplication ADBApplication = AirDataBridgeApplication.getInstance();
    LogFile dwFile = new LogFile();
    TextView TVDownloadDesc;
    TextView TVDownloadPercent;
    TextView TVDownloadkb;
    CheckBox CHKNotify;
    CheckBox CHKDeleteRemoteFile;
    ProgressBar PBProgress;

    SharedPreferences settings;

    //@SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder downloadAlert = new AlertDialog.Builder(getActivity());
        settings = PreferenceManager.getDefaultSharedPreferences(getContext());

        dwFile = ADBApplication.getCurrentRemoteDownload();
        Log.w("myApp", "[#] FragmentDownloadDialog: REQUEST_START_DOWNLOAD = " + dwFile.Name);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_downloaddialog, null);

        TVDownloadDesc = (TextView) view.findViewById(R.id.id_downloaddialog_desc);
        TVDownloadPercent = (TextView) view.findViewById(R.id.id_downloaddialog_percent);
        TVDownloadkb = (TextView) view.findViewById(R.id.id_downloaddialog_kb);
        PBProgress = (ProgressBar) view.findViewById(R.id.id_downloaddialog_progressBar);

        CHKNotify = (CheckBox) view.findViewById(R.id.id_downloaddialog_notify);
        CHKNotify.setChecked(settings.getBoolean("prefNotifyDownloadFinished", false));
        CHKNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                AirDataBridgeApplication.getInstance().setPrefNotifyDownloadFinished(isChecked);
                //SharedPreferences.Editor editor1 = settings.edit();
                //editor1.putBoolean("prefNotifyDownloadFinished", isChecked);
                //editor1.commit();
            }
        });

        CHKDeleteRemoteFile = (CheckBox) view.findViewById(R.id.id_downloaddialog_delete_remote_file);
        CHKDeleteRemoteFile.setChecked(settings.getBoolean("prefDeleteRemoteFileWhenDownloadFinished", false));
        CHKDeleteRemoteFile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                AirDataBridgeApplication.getInstance().setPrefDeleteRemoteFileWhenDownloadFinished(isChecked);
                //SharedPreferences.Editor editor1 = settings.edit();
                //editor1.putBoolean("prefDeleteRemoteFileWhenDownloadFinished", isChecked);
                //editor1.commit();
            }
        });

        TVDownloadDesc.setText(getResources().getString(R.string.dlg_download_the_file, dwFile.Name));

        downloadAlert.setView(view)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.w("myApp", "[#] FragmentDownloadDialog: REQUEST_STOP_DOWNLOAD");
                        EventBus.getDefault().post(EventBusMSG.REQUEST_STOP_DOWNLOAD);
                    }
                });
        return downloadAlert.create();
    }

    @Override
    public void onResume() {
        if (!ADBApplication.isDownloadDialogVisible()) this.dismiss();
        EventBus.getDefault().register(this);
        Update();
        super.onResume();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Subscribe
    public void onEvent(Short msg) {
        if (msg == EventBusMSG.END_DOWNLOAD) {
            this.dismiss();
        }
        if ((msg == EventBusMSG.UPDATE_DOWNLOAD_PROGRESS) && isAdded()) Update();
    }

    void Update() {
        final int p;
        p = (dwFile.lsize > 0) ? (int)(100 * ADBApplication.getDownloadedSize() / dwFile.lsize) : 0;
        (getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PBProgress.setProgress (p);
                TVDownloadPercent.setText(p + "%");
                TVDownloadkb.setText((int)(ADBApplication.getDownloadedSize() / 1024) + "/" + dwFile.Sizekb);
            }
        });
    }
}