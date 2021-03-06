/**
 * FragmentLogList_Local - Java Class for Android
 * Created by G.Capelli (BasicAirData) on 14/5/2017
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
 *
 */

package eu.basicairdata.airdatabridge;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentLogList_Local extends Fragment {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private View view;
    private TextView textViewLocalListEmpty;

    private RecyclerView.Adapter adapter;
    private List<LogFile> data = Collections.synchronizedList(new ArrayList<LogFile>());
    private LogFile SelectedLogFile;


    public FragmentLogList_Local() {
        // Required empty public constructor
    }

    @Subscribe
    public void onEvent(Short msg) {
        if (msg == EventBusMSG.LOCAL_UPDATE_LOGLIST) {
            (getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Update();
                }
            });
        }
    }

    @Subscribe
    public void onEvent(final EventBusMSGLogFile msg) {
        if (msg.MSGType == EventBusMSG.LOCAL_LOGLIST_SELECTION) {
            SelectedLogFile = msg.logFile;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    registerForContextMenu(view);
                    getActivity().openContextMenu(view);
                    unregisterForContextMenu(view);
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_loglist, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.getItemAnimator().setChangeDuration(0);
        adapter = new LogFileAdapter(data);
        recyclerView.setAdapter(adapter);
        textViewLocalListEmpty = (TextView) view.findViewById(R.id.id_textView_loglistEmpty);
        return view;
    }

    @Override
    public void onResume() {
        //Log.w("myApp", "[#] FragmentLogList_Remote: onResume()");
        EventBus.getDefault().register(this);
        Update();
        super.onResume();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        //Log.w("myApp", "[#] FragmentLogList_Remote: onPause()");
        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_card_local, menu);
        //menu.setHeaderTitle(SelectedLogFile.Name);

        //menu.findItem(R.id.cardmenu_local_start_recording).setVisible(true);
        menu.findItem(R.id.cardmenu_local_delete).setVisible(true);
        menu.findItem(R.id.cardmenu_local_share).setVisible(true);

    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.cardmenu_local_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getResources().getString(R.string.card_message_delete_local_file_confirmation));
                Log.w("myApp", "[#] FragmentLogList_Local: onContextItemSelected");
                builder.setIcon(android.R.drawable.ic_menu_info_details);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EventBus.getDefault().post(new EventBusMSGLogFile(EventBusMSG.LOCAL_FILE_DELETE, SelectedLogFile));
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.cardmenu_local_start_recording:
                //EventBus.getDefault().post(new EventBusMSGLogFile(EventBusMSG.REMOTE_REQUEST_START_RECORDING, SelectedLogFile));
                break;
            case R.id.cardmenu_local_stop_recording:
                //EventBus.getDefault().post(EventBusMSG.REMOTE_REQUEST_STOP_RECORDING);
                break;
            case R.id.cardmenu_local_share:

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Air Data Bridge - Recording " + SelectedLogFile.Name + "(" + SelectedLogFile.Sizekb + ")");

                //intent.putExtra(Intent.EXTRA_TEXT, (CharSequence) ("GPS Logger - Track " + track.getName()
                //            + "\n" + track.getNumberOfLocations() + " " + getString(R.string.trackpoints)
                //            + "\n" + track.getNumberOfPlacemarks() + " " + getString(R.string.placemarks)));

                intent.setType("text/csv");

                ArrayList<Uri> files = new ArrayList<>();
                String fname = SelectedLogFile.LocalName + ".CSV";
                File file = new File(Environment.getExternalStorageDirectory() + "/AirDataBridge/", fname);
                if (file.exists ()) {
                    Uri uri = Uri.fromFile(file);
                    files.add(uri);
                }

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);

                String title = getString(R.string.card_menu_share);
                // Create intent to show chooser
                Intent chooser = Intent.createChooser(intent, title);

                // Verify the intent will resolve to at least one activity
                if ((intent.resolveActivity(getContext().getPackageManager()) != null) && (!files.isEmpty())) {
                    startActivity(chooser);
                }

                break;
            default:
                return false;
        }
        return true;
    }

    public void Update() {
        if (isAdded()) {
            final List<LogFile> TI = AirDataBridgeApplication.getInstance().getLogfileList_Local();
            synchronized (data) {
                if (data != null) data.clear();
                if (!TI.isEmpty()) {
                    data.addAll(TI);
                    textViewLocalListEmpty.setVisibility(View.INVISIBLE);
                } else {
                    if (AirDataBridgeApplication.getInstance().isStoragePermissionGranted()) {
                        textViewLocalListEmpty.setText(R.string.local_archive_empty);
                    } else {
                        textViewLocalListEmpty.setText(R.string.local_archive_permission_denied);
                    }
                    //if (AirDataBridgeApplication.getInstance().getSD_Status() == AirDataBridgeApplication.SD_STATUS_NOT_PRESENT)
                    //    textViewLocalListEmpty.setText(R.string.local_archive_permission_denied);
                    //textViewLocalListEmpty.setVisibility(View.VISIBLE);
                    // LogFile List empty
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }
}