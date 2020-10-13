/*
 * Copyright (c) 2020. Manuel Koloska, Band Genossenschaft. All rights reserved.
 */

package ch.band.manko.eartagreader;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import ch.band.manko.eartagreader.adapters.ProposedEarTagListAdapter;
import ch.band.manko.eartagreader.data.EarTagRepository;
import ch.band.manko.eartagreader.databinding.FragmentListBinding;
import ch.band.manko.eartagreader.models.EarTag;
import ch.band.manko.eartagreader.models.ProposedEarTag;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

public class ImportCsvActivity extends AppCompatActivity {
	private static final String TAG = ImportCsvActivity.class.getName();
	FragmentListBinding binding;
	EarTagRepository repository;
	public List<ProposedEarTag> proposedEarTagList = new ArrayList<>();
	private static int tv_error_id = View.generateViewId();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = FragmentListBinding.inflate(getLayoutInflater());
		binding.fabAdd.setImageResource(R.drawable.ic_check_black_24dp);
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
				ProposedEarTagListAdapter _adapter = (ProposedEarTagListAdapter) binding.rvTextlist.getAdapter();
				repository.addEarTag(new EarTag(tvd.number));
				proposedEarTagList.get(position).isRegistered = true;
				_adapter.submitList(proposedEarTagList);
				_adapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(),R.string.import_save_entry,Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onRemove(ProposedEarTag tvd, int position) {
				proposedEarTagList.remove(position);
				ProposedEarTagListAdapter _adapter = (ProposedEarTagListAdapter) binding.rvTextlist.getAdapter();
				_adapter.submitList(proposedEarTagList);
				_adapter.notifyDataSetChanged();
			}
		});
		binding.rvTextlist.setAdapter(adapter);
		proposedEarTagList = open(uri);
		adapter.submitList(proposedEarTagList);
		adapter.notifyDataSetChanged();
		ImportCsvActivity activity = this;
		binding.fabAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Toast.makeText(getApplicationContext(),R.string.import_sucessful,Toast.LENGTH_LONG).show();
				activity.finishAfterTransition();
			}
		});
	}
	private List<ProposedEarTag> open(Uri uri){
		List<ProposedEarTag> list = new ArrayList<>();
		try {
			InputStream stream = getContentResolver().openInputStream(uri);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			CSVParser parser = new CSVParser();
			parser.setErrorLocale(Locale.getDefault());
			reader.readLine();
			String line = reader.readLine();
			 do {
			 	String[] fields = parser.parseLine(line);
				 for (String field : fields) {
				 	if (EarTag.isEartagNumber(field)) {
				 		field = EarTag.formatNumber(field);
				 		list.add(new ProposedEarTag(field,-1,repository.containsEarTag(new EarTag(field)).get()));
						break;
				 	}
				 }
				 line = reader.readLine();
			} while (line != null);
		} catch (IOException | ExecutionException | InterruptedException e) {
			e.printStackTrace();
			addErrorText(R.string.import_parse_error);
		}
		if(list.isEmpty()){
			addErrorText(R.string.import_file_incompatible);
		}
		Log.d(TAG, "open: Listsize: "+list.size());
		return list;
	}
	private void addErrorText(@StringRes int description){
		TextView textView = (TextView) binding.getRoot().getViewById(tv_error_id);
		if(textView != null){
			textView.setText(description);
			return;
		}
		ConstraintLayout.LayoutParams layoutparams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,ConstraintLayout.LayoutParams.WRAP_CONTENT);
		layoutparams.topToTop = R.id.fragListRoot;
		layoutparams.bottomToBottom = R.id.fragListRoot;
		layoutparams.leftToLeft = R.id.fragListRoot;
		layoutparams.rightToRight = R.id.fragListRoot;
		textView = new TextView(getApplicationContext());
		textView.setId(tv_error_id);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			textView.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
		}else {
			textView.setTextSize(COMPLEX_UNIT_SP,22);
		}
		textView.setText(description);
		binding.getRoot().addView(textView,layoutparams);
	}
}