/*
 * Copyright (c) 2020. Manuel Koloska, Band Genossenschaft. All rights reserved.
 */

package ch.band.manko.eartagreader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.util.FileUtil;

import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ch.band.manko.eartagreader.adapters.ConfirmedEarTagListAdapter;
import ch.band.manko.eartagreader.adapters.ProposedEarTagListAdapter;
import ch.band.manko.eartagreader.data.EarTagRepository;
import ch.band.manko.eartagreader.databinding.FragmentListBinding;
import ch.band.manko.eartagreader.models.EarTag;
import ch.band.manko.eartagreader.models.ProposedEarTag;

public class ImportCsvActivity extends AppCompatActivity {
	private static final String TAG = ImportCsvActivity.class.getName();
	FragmentListBinding binding;
	EarTagRepository repository;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = FragmentListBinding.inflate(getLayoutInflater());
		binding.fabAdd.setImageResource(R.drawable.ic_baseline_save_24);
		setContentView(binding.getRoot());
		repository = new EarTagRepository(getApplicationContext());
		Intent intent = getIntent();
		Uri uri = intent.getData();
		if(uri == null){
			uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
		}
		binding.rvTextlist.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
		ProposedEarTagListAdapter adapter = new ProposedEarTagListAdapter(new ProposedEarTagListAdapter.ItemInteractions() {
			@Override
			public void onConfirm(ProposedEarTag tvd, int position) {
				repository.addEarTag(new EarTag(tvd.number));
			}

			@Override
			public void onRemove(ProposedEarTag tvd, int position) {
			}
		});
		binding.rvTextlist.setAdapter(adapter);
		adapter.submitList(open(uri));
		adapter.notifyDataSetChanged();
		binding.fabAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				for(ProposedEarTag tag: adapter.getCurrentList()){
					try {
						EarTag earTag = new EarTag(tag.number);
						if(!repository.containsEarTag(earTag).get()){
							repository.addEarTag(earTag);
							tag.isRegistered = true;
						}
					} catch (ExecutionException | InterruptedException e) {
						e.printStackTrace();
					}
				}
				adapter.notifyDataSetChanged();
			}
		});
	}
	private List<ProposedEarTag> open(Uri uri){
		List<ProposedEarTag> list = new ArrayList<>();
		try {
			InputStream stream = getContentResolver().openInputStream(uri);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			reader.readLine();
			String line = reader.readLine();
			 do {
			 	String[] fields = line.split(",");
				 for (String field : fields) {
					 if (EarTag.isEartagNumber(line)) {
						 line = EarTag.formatNumber(line);
					 }
					 try {
						 list.add(new ProposedEarTag(line,1,repository.containsEarTag(new EarTag(line)).get()));
					 } catch (InterruptedException | ExecutionException e) {
						 e.printStackTrace();
					 }
				 }
				line = reader.readLine();
			} while (line != null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "open: Listsize: "+list.size());
		return list;
	}
}