package ch.band.manko.tvdnumberreader;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import ch.band.manko.tvdnumberreader.adapters.ProposedTvdListAdapter;
import ch.band.manko.tvdnumberreader.data.TvdNumberRepository;
import ch.band.manko.tvdnumberreader.models.ProposedTvdNumber;
import ch.band.manko.tvdnumberreader.models.TvdNumber;

/**
 * The AnalysePhotoViewModel got created to separate the business Logic from UI management logic since
 * the AnalysePhotoFragment class got a little to big.
 *
 * This class implements OnSuccessListener<String> because it needs to listen for tvd-number recognized
 * from the Camera. It also needs to implement ProposedTvdListAdapter.ItemInteractions to react to
 * interactions with ProposedTvdNumbers.
 *
 */
public class AnalysePhotoViewModel implements OnSuccessListener<String>, ProposedTvdListAdapter.ItemInteractions {
    private static final String TAG = AnalysePhotoViewModel.class.getSimpleName();

    private HashMap<String, ProposedTvdNumber> proposedTvds;
    private ICommandExecutor executor;
    private TvdNumberRepository repository;

    /**
     * Creates a new AnalysePhotoViewModel
     * @param commandExecutor is an object, which implements the commands defined in the interface.
     * @param context the AppContext (to create a TvdNumberRepository)
     */
    public AnalysePhotoViewModel(ICommandExecutor commandExecutor, Context context)
    {
        proposedTvds =  new HashMap<>();
        executor = commandExecutor;
        repository = new TvdNumberRepository(context);
    }

    /**
     * Helperfunction to convert the @Link{#proposedTvds} to a List.
     * It also sorts the list with the interface Comparable<ProposedTvdNumber> implemented on ProposedTvdNumber.
     * @return a sorted list of the current ProposedTvdNumbers.
     */
    private List<ProposedTvdNumber> proposedTvdsAsList(){
        ArrayList<ProposedTvdNumber> list = new ArrayList<>(proposedTvds.values());
        Collections.sort(list);
        return list;
    }

    /**
     * If TextRecognizer recognized a new tvd-number it checks whether the number is already on the list
     * and if the proposal already exists. if it exists the occurrence-counter gets 1 added or a new
     * ProposedTvdNumber gets created. If necessary, the UI gets an update.
     *
     * @param text
     */
    @Override
    public void onSuccess(String text) {
        if(text != null){

            Future<Boolean> isRegistered = repository.containsTvdNumber(new TvdNumber(text));
            try {
                ProposedTvdNumber newItem = new ProposedTvdNumber(text, 1, isRegistered.get());

                if(!proposedTvds.containsKey(text)){
                    executor.playSoundTextRecognized();
                    proposedTvds.put(text,newItem);
                    Log.w(TAG,text);
                }else{
                    Objects.requireNonNull(proposedTvds.get(text)).occurrence++;
                }
                executor.updateProposedList(proposedTvdsAsList());

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets called when the user tips the check mark on a ProposedTvdNumber
     *
     * @param tvd the ProposedTvdNumber the user tipped.
     * @param position of the ProposedTvdNumber on the screen (not needed, refactor?)
     */
    @Override
    public void onConfirm(ProposedTvdNumber tvd, int position) {
        ProposedTvdNumber removed = proposedTvds.remove(tvd.tvdNumber);
        assert removed != null;
        if(!tvd.isRegistered){
            repository.addTvdNumber(new TvdNumber(tvd.tvdNumber));
            executor.playSoundTouch();
        }
        executor.updateProposedList(proposedTvdsAsList());
    }

    /**
     * Gets called when the user tips the x mark on a ProposedTvdNumber
     * @param tvd the ProposedTvdNumber the user tipped.
     * @param position of the ProposedTvdNumber on the screen (not needed, refactor?)
     */
    @Override
    public void onRemove(ProposedTvdNumber tvd, int position) {
        ProposedTvdNumber removed = proposedTvds.remove(tvd.tvdNumber);
        assert removed != null;
        executor.playSoundTouch();
        executor.updateProposedList(proposedTvdsAsList());
    }

    public interface ICommandExecutor {
        void playSoundTextRecognized();
        void playSoundTouch();
        void updateProposedList(List<ProposedTvdNumber> list);
    }
}
