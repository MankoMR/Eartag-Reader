package ch.manko.usingocrinapps.googlemlkitwithcamerax.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import ch.manko.usingocrinapps.googlemlkitwithcamerax.R;
import ch.manko.usingocrinapps.googlemlkitwithcamerax.models.TvdNumber;
import ch.manko.usingocrinapps.googlemlkitwithcamerax.adapters.ConfirmedTvdNumberListAdapter;
import ch.manko.usingocrinapps.googlemlkitwithcamerax.databinding.FragmentListBinding;

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
