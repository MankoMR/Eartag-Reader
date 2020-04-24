package ch.band.manko.tvdnumberreader.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import ch.band.manko.tvdnumberreader.R;
import ch.band.manko.tvdnumberreader.adapters.ConfirmedTvdNumberListAdapter;
import ch.band.manko.tvdnumberreader.databinding.FragmentListBinding;
import ch.band.manko.tvdnumberreader.models.TvdNumber;

public class TextListFragment extends Fragment {
    FragmentListBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(inflater,container,false);
        binding.rvTextlist.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTextlist.setAdapter(new ConfirmedTvdNumberListAdapter());

        Bundle args = getArguments();
        try {
            updateProposedList(extractNumbers(args));
        } catch (NullPointerException e) {
        }

        binding.fabAdd.setOnClickListener(view->{
            //Todo: Setup proper navigation
            NavHostFragment.findNavController(TextListFragment.this)
                    .navigate(R.id.action_AddText);
        });
        return binding.getRoot();
    }
    public void updateProposedList(List<TvdNumber> list){
        ConfirmedTvdNumberListAdapter adapter = (ConfirmedTvdNumberListAdapter) binding.rvTextlist.getAdapter();
        adapter.submitList(list);
        adapter.notifyDataSetChanged();
    }
    private List<TvdNumber> extractNumbers(Bundle bundle) throws NullPointerException{
        String[] tvds = TextListFragmentArgs.fromBundle(bundle).getTvds();
        if (tvds == null)
            throw new NullPointerException();
        ArrayList<TvdNumber> list = new ArrayList<>();
        for (String num :tvds) {
            list.add(new TvdNumber(num));
        }
        return list;
    }
}
