package ch.band.manko.tvdnumberreader;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ch.band.manko.tvdnumberreader.data.TvdNumberRepository;
import ch.band.manko.tvdnumberreader.databinding.ActivityMainBinding;

import static androidx.core.content.FileProvider.getUriForFile;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        TvdNumberRepository repository = new TvdNumberRepository(getApplicationContext());
        //noinspection SimplifiableIfStatement
        if (id == R.id.share) {

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
    private Uri createFile(String name, TvdNumberRepository repository) throws IOException {
        File path = new File(getFilesDir(),getString(R.string.tvdnumberlists));
        File csv = new File(path,name+".csv");
        FileWriter writer = new FileWriter(csv);
        writer.append(repository.AllTvdNUmbersasCSV(getApplicationContext()));
        writer.close();
        return getUriForFile(getApplicationContext(), getText(R.string.package_).toString(), csv);
    }
    private AlertDialog buildDialog(DialogInterface.OnClickListener ok){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.confirm_delete_list);
        builder.setPositiveButton(R.string.confirm,ok);
        builder.setNegativeButton(R.string.abort,null);

        return builder.create();
    }
}
