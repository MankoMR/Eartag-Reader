/*
 * Copyright (c) 2020. Manuel Koloska, Band Genossenschaft. All rights reserved.
 */

package ch.band.manko.eartagreader;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.band.manko.eartagreader.data.EarTagRepository;
import ch.band.manko.eartagreader.databinding.ActivityMainBinding;
import ch.band.manko.eartagreader.models.EarTag;

import static androidx.core.content.FileProvider.getUriForFile;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private EarTagRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        repository = new EarTagRepository(getApplicationContext());
        NavController navController = Navigation.findNavController(this,R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(destination.getId() == R.layout.fragment_analyse_photo){
                    setMenuVisibility(false);
                }else {
                    setMenuVisibility(true);
                }
            }
        });
    }
    /**
     * @See <a href="https://developer.android.com/training/appbar">Appbar</a>
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //it also needs to be inflated before it can be used
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //with the following statement it observes the tvd-number list and if the list is empty it hides the
        //share and delete button, since there nothing to share or delete.
        repository.getAll().observe(this, new Observer<List<EarTag>>() {
            @Override
            public void onChanged(List<EarTag> earTags) {
                boolean showShareButton = earTags.size() > 0;
                setMenuVisibility(showShareButton);
            }
        });
        return true;
    }
    public void setMenuVisibility(boolean isVisible){
        MenuItem share = binding.toolbar.getMenu().findItem(R.id.share);
        if(share == null)
            return;
        share.setEnabled(isVisible);
        share.setVisible(isVisible);
        MenuItem delete = binding.toolbar.getMenu().findItem(R.id.clear_list);
        delete.setEnabled(isVisible);
        delete.setVisible(isVisible);
    }

    /**
     * Gets called when item in menu of the app gets tipped.
     *
     * @See <a href="https://developer.android.com/training/appbar">Appbar</a>
     *
     * @param item: the Item that got tipped.
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.share) {
            //creates file name.
            Date now = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            format.setTimeZone(Calendar.getInstance().getTimeZone());
            String name = getString(R.string.share_title)+"_"+ format.format(now);
            //Log.d(TAG,name);
            //create and share file
            try {
                Uri file = createFile(name,repository);
                share(file,"text/csv",name);
            } catch (IOException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }
        if (id == R.id.clear_list) {
            //ask user for confirmation before deleting the tvd-number list
            AlertDialog dialog = buildDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    repository.deleteAll();
                }
            });
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *  Creates a .csv file at the position specified in res/values/strings/storagelocation.
     *  The Location of the file enables the Fileprovider to grant temporary access to the file to
     *  other apps.
     *
     * @See <a href="https://developer.android.com/reference/androidx/core/content/FileProvider">File Provider</a>
     * @See <a href="https://developer.android.com/training/secure-file-sharing">Sharing Files</a>
     *
     * @param name: The name the file should have.
     * @param repository: The EarTagRepository from which the content for the file can be accessed.
     * @return an Uri to share with other Apps, pointing to the location of the file.
     *
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private Uri createFile(String name, EarTagRepository repository) throws IOException, ExecutionException, InterruptedException {
        //gets the storagelocation to store the file and creates the folders if necessary.
        File path = new File(getFilesDir(),getString(R.string.storagelocation));
        path.mkdirs();
        //creates the file
        File csv = new File(path,name+".csv");
        Log.d(TAG,"Filepath: "+csv.getAbsolutePath());
        csv.createNewFile();
        //writes the String from AllTvdNUmbersasCSV to the file
        FileWriter writer = new FileWriter(csv);
        writer.append(repository.allEarTagsAsCSV(getApplicationContext()).get());
        writer.close();
        //get the Uri to the file from the fileprovider with the authority described in the manifest
        //(res/values/strings/authority should be the string describing the authority in the manifest
        // the value needs to match the value in the manifest.)
        Uri uri = getUriForFile(getApplicationContext(), getText(R.string.authority).toString(), csv);
        Log.d(TAG,"Uri: "+uri.toString());
        return uri;
    }

    /**
     * Shares to Uri to possibly open the file specified in Uri with an compatible app.
     * Therefore it grants
     *
     * @See <a href="https://developer.android.com/guide/components/intents-filters">Intents</a>
     * @See <a href="https://developer.android.com/training/secure-file-sharing">Sharing Files</a>
     *
     * @param uri: The Uri which points to a file.
     * @param mimetype: The type of content of the file.
     * @param title: Title of the share dialog.
     */
    private void share(Uri uri,String mimetype,String  title){
        Intent toshare = new Intent(Intent.ACTION_SEND);
        //Grants temporary read permission
        toshare.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        toshare.putExtra(Intent.EXTRA_STREAM,uri.normalizeScheme());
        toshare.setType(mimetype);
        //toshare.setDataAndTypeAndNormalize(uri,mimetype);
        startActivity(Intent.createChooser(toshare,title));
    }

    /**
     * Builds the AlertDialog to show when the user wants to delete the list.
     * This Dialog is necessary to provide the user with the opportunity to consider aborting the deletion
     * in case he accidentally tipped or other reasons.
     *
     * @param ok: An OnClickListener which will be executed the user clicks on OK on the AlertDialog.
     * @return the finished AlertDialog.
     */
    private AlertDialog buildDialog(DialogInterface.OnClickListener ok){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.confirm_delete_list);
        builder.setPositiveButton(R.string.confirm,ok);
        builder.setNegativeButton(R.string.abort,null);

        return builder.create();
    }
}
