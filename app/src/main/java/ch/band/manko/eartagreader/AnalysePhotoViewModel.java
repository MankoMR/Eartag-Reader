package ch.band.manko.eartagreader;

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

import ch.band.manko.eartagreader.adapters.ProposedEarTagListAdapter;
import ch.band.manko.eartagreader.data.EarTagRepository;
import ch.band.manko.eartagreader.models.EarTag;
import ch.band.manko.eartagreader.models.ProposedEarTag;

/**
 * The AnalysePhotoViewModel got created to separate the business Logic from UI management logic since
 * the AnalysePhotoFragment class got a little to big.
 *
 * This class implements OnSuccessListener<String> because it needs to listen for tvd-number recognized
 * from the Camera. It also needs to implement ProposedEarTagListAdapter.ItemInteractions to react to
 * interactions with ProposedTvdNumbers.
 *
 */
public class AnalysePhotoViewModel implements OnSuccessListener<String>, ProposedEarTagListAdapter.ItemInteractions {
    private static final String TAG = AnalysePhotoViewModel.class.getSimpleName();

    private HashMap<String, ProposedEarTag> proposedEarTags;
    private ICommandExecutor executor;
    private EarTagRepository repository;

    /**
     * Creates a new AnalysePhotoViewModel
     * @param commandExecutor is an object, which implements the commands defined in the interface.
     * @param context the AppContext (to create a EarTagRepository)
     */
    public AnalysePhotoViewModel(ICommandExecutor commandExecutor, Context context)
    {
        proposedEarTags =  new HashMap<>();
        executor = commandExecutor;
        repository = new EarTagRepository(context);
    }

    /**
     * Helperfunction to convert the @Link{#proposedTvds} to a List.
     * It also sorts the list with the interface Comparable<ProposedEarTag> implemented on ProposedEarTag.
     * @return a sorted list of the current ProposedTvdNumbers.
     */
    private List<ProposedEarTag> proposedEarTagsAsList(){
        ArrayList<ProposedEarTag> list = new ArrayList<>(proposedEarTags.values());
        Collections.sort(list);
        return list;
    }

    /**
     * If TextRecognizer recognized a new tvd-number it checks whether the number is already on the list
     * and if the proposal already exists. if it exists the occurrence-counter gets 1 added or a new
     * ProposedEarTag gets created. If necessary, the UI gets an update.
     *
     * @param text
     */
    @Override
    public void onSuccess(String text) {
        if(text != null){

            Future<Boolean> isRegistered = repository.containsEarTag(new EarTag(text));
            try {
                ProposedEarTag newItem = new ProposedEarTag(text, 1, isRegistered.get());

                if(!proposedEarTags.containsKey(text)){
                    executor.playSoundTextRecognized();
                    proposedEarTags.put(text,newItem);
                    Log.w(TAG,text);
                }else{
                    Objects.requireNonNull(proposedEarTags.get(text)).occurrence++;
                }
                executor.updateProposedList(proposedEarTagsAsList());

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets called when the user tips the check mark on a ProposedEarTag
     *
     * @param tvd the ProposedEarTag the user tipped.
     * @param position of the ProposedEarTag on the screen (not needed, refactor?)
     */
    @Override
    public void onConfirm(ProposedEarTag tvd, int position) {
        ProposedEarTag removed = proposedEarTags.remove(tvd.number);
        assert removed != null;
        if(!tvd.isRegistered){
            repository.addEarTag(new EarTag(tvd.number));
            executor.playSoundTouch();
        }
        executor.updateProposedList(proposedEarTagsAsList());
    }

    /**
     * Gets called when the user tips the x mark on a ProposedEarTag
     * @param tvd the ProposedEarTag the user tipped.
     * @param position of the ProposedEarTag on the screen (not needed, refactor?)
     */
    @Override
    public void onRemove(ProposedEarTag tvd, int position) {
        ProposedEarTag removed = proposedEarTags.remove(tvd.number);
        assert removed != null;
        executor.playSoundTouch();
        executor.updateProposedList(proposedEarTagsAsList());
    }

    public interface ICommandExecutor {
        void playSoundTextRecognized();
        void playSoundTouch();
        void updateProposedList(List<ProposedEarTag> list);
    }
}
