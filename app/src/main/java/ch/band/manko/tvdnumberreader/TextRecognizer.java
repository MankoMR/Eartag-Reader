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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextRecognizer implements ImageAnalysis.Analyzer {
    private static final String TAG = TextRecognizer.class.getSimpleName();
    private static final Pattern pattern = Pattern.compile("(([A-Z]{2}\\s{0,3})?[0-9]{4}\\s{0,3}[0-9]{4})");

    private AtomicInteger finishCounter = new AtomicInteger(0);
    private int parallelWorker = -1;
    private Rotation currentRotation = Rotation._0;
    private Rotation successfullRotation = Rotation._0;
    private boolean success = false;
    private ResultListener listener;

    public TextRecognizer(ResultListener resultListener){
        this.listener = resultListener;
    }


    @SuppressLint("UnsafeExperimentalUsageError")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {

        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null || listener == null) {
            imageProxy.close();
            return;
        }
        //int rotation = degreesToFirebaseRotation(imageProxy.getImageInfo().getRotationDegrees());
        parallelWorker = 1;
        if(success){
            successfullRotation = currentRotation;
            processImage(imageProxy,successfullRotation);
        } else {
            switch (currentRotation){
                case _0:
                    currentRotation = Rotation._180;
                    break;
                case _180:
                    currentRotation = Rotation._90;
                    break;
                case _90:
                    currentRotation = Rotation._270;
                    break;
                case _270:
                    currentRotation = Rotation._0;
                    break;
            }
            processImage(imageProxy,currentRotation);
        }

    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private void processImage(ImageProxy imageProxy,Rotation rotation){
        FirebaseVisionImage image =
                FirebaseVisionImage.fromMediaImage(Objects.requireNonNull(imageProxy.getImage()), rotation.rotation);

        // Pass image to an ML Kit Vision API
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        detector.processImage(image)
            .addOnSuccessListener(firebaseVisionText -> {
                closeImageProxySimple(imageProxy);
                String text = filterText(firebaseVisionText);
                success = text != null;
                listener.onSuccess(text);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG,"Textrecognition failed:",e);
                success = false;
                closeImageProxySimple(imageProxy);
            });
    }
    private void closeImageProxySimple(ImageProxy proxy){
        proxy.close();
    }
    private void closeImageProxy(ImageProxy proxy){
        if(parallelWorker < 0){
            proxy.close();
            throw new Error("Set parralel worker to parallel working Imageprocessors on Imageproxy");
        }
        int finished = finishCounter.incrementAndGet();
        Log.d(TAG+" Finished","Finished:"+finished);
        if (finishCounter.compareAndSet(4,0)){
            Log.d(TAG+" Finished","Close Proxy");
            proxy.close();
        }
    }

    private int degreesToFirebaseRotation(int degrees) {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }

    public interface ResultListener extends OnSuccessListener<String>{
    }
    private String filterText(FirebaseVisionText text){
        String result = "";
    Matcher match = pattern.matcher(text.getText());
    if(match.find()){
        result += match.group();
        //return result;
        return result.replaceAll("\\n","");
    }else
        return null;
    }
    public enum Rotation {
        _0(FirebaseVisionImageMetadata.ROTATION_0),
        _90(FirebaseVisionImageMetadata.ROTATION_90),
        _180(FirebaseVisionImageMetadata.ROTATION_180),
        _270(FirebaseVisionImageMetadata.ROTATION_270);

        public  int rotation;

        Rotation(int rotation){
            this.rotation = rotation;
        }
    }
}
