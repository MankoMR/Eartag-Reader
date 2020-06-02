package ch.band.manko.tvdnumberreader;

import android.annotation.SuppressLint;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Documentation: https://developer.android.com/training/camerax/analyze
 * Tutorial: https://codelabs.developers.google.com/codelabs/camerax-getting-started/#0
 */
public class TextRecognizer implements ImageAnalysis.Analyzer {
    private static final String TAG = TextRecognizer.class.getSimpleName();
    //Matches tvd-number like patterns within a string.
    //Examples: Dd53453456, CH 2345 6547, ad 2356\t2356, 2352 2345
    private static final Pattern numberPattern = Pattern.compile("(([a-zA-Z]{2}\\s{0,3})?[0-9]{4}\\s{0,3}[0-9]{4})");
    //Matches the first two Letters of a tvd-number
    private static final Pattern langPattern = Pattern.compile("[A-Z]{2}");

    private Degrees currentRotation = Degrees._0;
    private boolean success = false;
    private OnSuccessListener<String> listener;

    /**
     * Create a new Textrecognizer
     *
     * @param resultListener: Is a OnSuccessListener<String> which gets invocated each time the text
     *                        recognized, could possibly be a tvd-number.
     */
    public TextRecognizer(OnSuccessListener<String> resultListener){
        this.listener = resultListener;
    }

    /**
     * Searchs for text in the imageProxy which could possibly be a tvd-number.
     *
     * @param imageProxy: The image which should be analyzed.
     */
    @SuppressLint("UnsafeExperimentalUsageError")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {

        Image mediaImage = imageProxy.getImage();
        //May stop crash through NullPointerExeption.
        if (mediaImage == null || listener == null) {
            imageProxy.close();
            return;
        }
        //If in previous call to analyse, text could be recognized, it keeps the current rotation of
        //the image. Otherwise it sets a new rotation until some text could successfully recognized.
        if(success){
            Degrees successfullRotation = currentRotation;
            processImage(imageProxy, successfullRotation);
        } else {
            switch (currentRotation){
                case _0:
                    currentRotation = Degrees._180;
                    break;
                case _180:
                    currentRotation = Degrees._90;
                    break;
                case _90:
                    currentRotation = Degrees._270;
                    break;
                case _270:
                    currentRotation = Degrees._0;
                    break;
            }
            processImage(imageProxy,currentRotation);
        }

    }

    /**
     * Tries to extract text from an image using the ML Kit Vision API from Google. Its using machine
     * learning. If it finds text matching the @numberPattern it formats the tvd-number and calls
     * listener.onSuccess(result).
     *
     * Overview: https://developers.google.com/ml-kit/vision/
     * Documentation: https://firebase.google.com/docs/ml-kit/android/recognize-text
     * Tutorial: https://codelabs.developers.google.com/codelabs/mlkit-android/#0
     *
     * @param imageProxy: The image which should be analyzed.
     * @param rotation: The rotation the image should be interpreted in. (The model can't recognize
     *                  text which isn't approximately parallel to a horizontal line. It also doesn't
     *                  recognizes Text which is rotated 180°.)
     */
    @SuppressLint("UnsafeExperimentalUsageError")
    private void processImage(ImageProxy imageProxy, Degrees rotation){
        FirebaseVisionImage image;
        try {
            image =
                    FirebaseVisionImage.fromMediaImage(Objects.requireNonNull(imageProxy.getImage()), rotation.rotation);
        }catch (IllegalStateException e){
            Log.d(TAG,"couldn't open image",e);
            imageProxy.close();
            return;
        }
        // Pass image to an ML Kit Vision API
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        detector.processImage(image)
                //If it finds text this part will be executed.
            .addOnSuccessListener(firebaseVisionText -> {
                closeImageProxySimple(imageProxy);
                String text = filterText(firebaseVisionText);
                success = text != null;
                listener.onSuccess(text);
            })
                //if it there some error this part will be executed.
            .addOnFailureListener(e -> {
                Log.e(TAG,"Textrecognition failed:",e);
                success = false;
                closeImageProxySimple(imageProxy);
            });
    }

    /**
     * Closes the ImageProxy and may do some other cleanup.
     * It is important to close the ImageProxy, because as long as image is used and not closed, the
     * analyze function won't get called again.
     *
     * @param proxy: The ImageProxy to close.
     */
    private void closeImageProxySimple(ImageProxy proxy){
        proxy.close();
    }

    /**
     * It filters the FirebaseVisionText text for a tvd-number. Other content gets discarded.
     *
     * It doesn't support the case in which multiple tvd number are contained in the text. In that
     * case it returns the first found tvd-number.
     *
     * @param text: The text which might contain the tvd-number.
     * @return a proper formatted tvd-number if found or else null when found nothing.
     */
    private String filterText(FirebaseVisionText text){
        String result = "";
        Matcher match = numberPattern.matcher(text.getText());
        if(match.find()){
            result += match.group();

            result = result.replaceAll("\\n","")
                    .replaceAll("\\s","")
                    .toUpperCase();
            if(!langPattern.matcher(result).find()){
                result = "CH"+result;
            }
            return result.substring(0,2)+" "+result.substring(2,6)+" "+result.substring(6,10);
        }else
            return null;
    }

    public enum Degrees {
        _0(FirebaseVisionImageMetadata.ROTATION_0),
        _90(FirebaseVisionImageMetadata.ROTATION_90),
        _180(FirebaseVisionImageMetadata.ROTATION_180),
        _270(FirebaseVisionImageMetadata.ROTATION_270);

        public  int rotation;

        Degrees(int rotation){
            this.rotation = rotation;
        }
    }
}
