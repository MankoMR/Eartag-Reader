package ch.band.manko.tvdnumberreader.fragments;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ch.band.manko.tvdnumberreader.TextRecognizer;
import ch.band.manko.tvdnumberreader.adapters.ProposedTvdListAdapter;
import ch.band.manko.tvdnumberreader.models.ProposedTvdNumber;

public class AnalysePhotoViewModel implements TextRecognizer.ResultListener, ProposedTvdListAdapter.ItemInteractions {
    private static final String TAG = AnalysePhotoViewModel.class.getSimpleName();
    private HashMap<String, ProposedTvdNumber> proposedTvds;
    private ArrayList<String> confirmedTvds = new ArrayList<>();
    private CommandExecutor executor;

    public AnalysePhotoViewModel(CommandExecutor commandExecutor)
    {
        proposedTvds =  new HashMap<>();
        executor = commandExecutor;
    }

    private List<ProposedTvdNumber> proposedTvdsAsList(){
        ArrayList<ProposedTvdNumber> list = new ArrayList<>(proposedTvds.values());
        Collections.sort(list);
        return list;
    }

    public String[] getConfirmedTvds(){
        return confirmedTvds.toArray(new String[]{});
    }
    @Override
    public void onSuccess(String text) {
        if(text != null){
            boolean isduplicate = false;
            ProposedTvdNumber newItem = new ProposedTvdNumber(text,1);

            if(!proposedTvds.containsKey(text)){
                executor.playSound();
                proposedTvds.put(text,newItem);
                Log.w(TAG,text);
            }else {
                proposedTvds.get(text).occurrence++;
            }
            executor.updateProposedList(proposedTvdsAsList());
        }
    }
    @Override
    public void onConfirm(ProposedTvdNumber tvd, int position) {
        ProposedTvdNumber removed = proposedTvds.remove(tvd.tvdNumber);
        assert removed != null;
        confirmedTvds.add(tvd.tvdNumber);
        executor.updateProposedList(proposedTvdsAsList());
    }
    @Override
    public void onRemove(ProposedTvdNumber tvd, int position) {
        ProposedTvdNumber removed = proposedTvds.remove(tvd.tvdNumber);
        assert removed != null;
        executor.updateProposedList(proposedTvdsAsList());
    }

    public interface CommandExecutor{
        void playSound();
        void updateProposedList(List<ProposedTvdNumber> list);
    }
}
