package ch.band.manko.tvdnumberreader.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import ch.band.manko.tvdnumberreader.R;
import ch.band.manko.tvdnumberreader.adapters.ConfirmedTvdNumberListAdapter;
import ch.band.manko.tvdnumberreader.data.TvdNumberRepository;
import ch.band.manko.tvdnumberreader.databinding.FragmentListBinding;
import ch.band.manko.tvdnumberreader.models.TvdNumber;

public class TextListFragment extends Fragment {
    FragmentListBinding binding;
    TvdNumberRepository repository;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new TvdNumberRepository(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(inflater,container,false);
        binding.rvTextlist.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTextlist.setAdapter(new ConfirmedTvdNumberListAdapter());

        binding.fabAdd.setOnClickListener(view->{
            NavDirections action = TextListFragmentDirections.actionAddText();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });
        return binding.getRoot();
    }
    public void updateProposedList(List<TvdNumber> list){
        ConfirmedTvdNumberListAdapter adapter = (ConfirmedTvdNumberListAdapter) binding.rvTextlist.getAdapter();
        adapter.submitList(list);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProposedList(repository.getAll());
    }
}
