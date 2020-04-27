package ch.band.manko.tvdnumberreader;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //Menu needs to be inflated before it can get called
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.share) {
            Date now = Calendar.getInstance().getTime();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            format.setTimeZone(Calendar.getInstance().getTimeZone());
            String name = getString(R.string.share_title)+"_"+ format.format(now);
            Log.d(TAG,name);
            try {
                Uri file = createFile(name,repository);
                share(file,"text/csv");
            } catch (IOException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }
        if (id == R.id.clear_list) {
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

    private Uri createFile(String name, TvdNumberRepository repository) throws IOException, ExecutionException, InterruptedException {
        File path = new File(getFilesDir(),getString(R.string.storagelocation));
        path.mkdirs();
        File csv = new File(path,name+".csv");
        Log.d(TAG,"Filepath: "+csv.getAbsolutePath());
        csv.createNewFile();
        //FileOutputStream stream = getBaseContext().openFileOutput(getFilesDir().getName() + getString(R.string.storagelocation), MODE_PRIVATE);
        FileWriter writer = new FileWriter(csv);
        writer.append(repository.AllTvdNUmbersasCSV(getApplicationContext()).get());
        writer.close();
        Uri uri = getUriForFile(getApplicationContext(), getText(R.string.authority).toString(), csv);
        Log.d(TAG,"Uri: "+uri.toString());
        return uri;
    }

    private void share(Uri uri,String mimetype){
        Intent toshare = new Intent(Intent.ACTION_SEND);
        toshare.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        toshare.putExtra(Intent.EXTRA_STREAM,uri.normalizeScheme());
        toshare.setType(mimetype);
        startActivity(Intent.createChooser(toshare,getString(R.string.share_title)));
    }

    private AlertDialog buildDialog(DialogInterface.OnClickListener ok){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.confirm_delete_list);
        builder.setPositiveButton(R.string.confirm,ok);
        builder.setNegativeButton(R.string.abort,null);

        return builder.create();
    }
}
