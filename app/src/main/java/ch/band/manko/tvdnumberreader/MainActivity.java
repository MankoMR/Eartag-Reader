package ch.band.manko.tvdnumberreader;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;

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

import ch.band.manko.tvdnumberreader.data.TvdNumberRepository;
import ch.band.manko.tvdnumberreader.databinding.ActivityMainBinding;
import ch.band.manko.tvdnumberreader.models.TvdNumber;

import static androidx.core.content.FileProvider.getUriForFile;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private TvdNumberRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        repository = new TvdNumberRepository(getApplicationContext());

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
        repository.getAll().observe(this, new Observer<List<TvdNumber>>() {
            @Override
            public void onChanged(List<TvdNumber> tvdNumbers) {
                boolean showShareButton = tvdNumbers.size() > 0;
                MenuItem share = binding.toolbar.getMenu().findItem(R.id.share);
                share.setEnabled(showShareButton);
                share.setVisible(showShareButton);
                MenuItem delete = binding.toolbar.getMenu().findItem(R.id.clear_list);
                delete.setEnabled(showShareButton);
                delete.setVisible(showShareButton);
            }
        });
        return true;
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
                share(file,"text/csv");
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
     * @param repository: The TvdNumberRepository from which the content for the file can be accessed.
     * @return an Uri to share with other Apps, pointing to the location of the file.
     *
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private Uri createFile(String name, TvdNumberRepository repository) throws IOException, ExecutionException, InterruptedException {
        //gets the storagelocation to store the file and creates the folders if necessary.
        File path = new File(getFilesDir(),getString(R.string.storagelocation));
        path.mkdirs();
        //creates the file
        File csv = new File(path,name+".csv");
        Log.d(TAG,"Filepath: "+csv.getAbsolutePath());
        csv.createNewFile();
        //writes the String from AllTvdNUmbersasCSV to the file
        FileWriter writer = new FileWriter(csv);
        writer.append(repository.allTvdNUmbersAsCSV(getApplicationContext()).get());
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
     */
    private void share(Uri uri,String mimetype){
        Intent toshare = new Intent(Intent.ACTION_SEND);
        //Grants temporary read permission
        toshare.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        toshare.putExtra(Intent.EXTRA_STREAM,uri.normalizeScheme());
        toshare.setType(mimetype);
        startActivity(Intent.createChooser(toshare,getString(R.string.share_title)));
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
