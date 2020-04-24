package ch.band.manko.tvdnumberreader;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import ch.band.manko.tvdnumberreader.adapters.ProposedTvdListAdapter;
import ch.band.manko.tvdnumberreader.data.TvdNumberRepository;
import ch.band.manko.tvdnumberreader.models.ProposedTvdNumber;
import ch.band.manko.tvdnumberreader.models.TvdNumber;

public class AnalysePhotoViewModel implements TextRecognizer.ResultListener, ProposedTvdListAdapter.ItemInteractions {
    private static final String TAG = AnalysePhotoViewModel.class.getSimpleName();

    private HashMap<String, ProposedTvdNumber> proposedTvds;
    private CommandExecutor executor;
    private TvdNumberRepository repository;

    public AnalysePhotoViewModel(CommandExecutor commandExecutor, Context context)
    {
        proposedTvds =  new HashMap<>();
        executor = commandExecutor;
        repository = new TvdNumberRepository(context);
    }

    private List<ProposedTvdNumber> proposedTvdsAsList(){
        ArrayList<ProposedTvdNumber> list = new ArrayList<>(proposedTvds.values());
        Collections.sort(list);
        return list;
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
            }else{
                Objects.requireNonNull(proposedTvds.get(text)).occurrence++;
            }
            executor.updateProposedList(proposedTvdsAsList());
        }
    }
    @Override
    public void onConfirm(ProposedTvdNumber tvd, int position) {
        ProposedTvdNumber removed = proposedTvds.remove(tvd.tvdNumber);
        assert removed != null;
        repository.addTvdNumber(new TvdNumber(tvd.tvdNumber));
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
