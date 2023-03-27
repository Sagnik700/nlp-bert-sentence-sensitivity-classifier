package com.example.nlpbertandroid;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.tensorflow.lite.Interpreter;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static Module module = null;
    private static LinkedHashMap<String, Integer> vocabMapBert = null;
    private static LinkedHashMap<String, Integer> vocabMapGptNeo = null;
    private static Interpreter tfliteModule = null;
    private static boolean isModelBert;
    private static String MODELNAME;
    private static String VOCABDICTIONARY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            ArrayAdapter<CharSequence>adapter = ArrayAdapter.createFromResource(this, R.array.model_types, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            Spinner modelType = (Spinner) findViewById(R.id.modelTypeDropdown);
            modelType.setAdapter(adapter);
            modelType.setOnItemSelectedListener(this);
            initViews();
        } catch (Exception e) {
            Log.e("Error during startup", "Thrown from onCreate()", e);
            finish();
        }
    }

    //initialize views of the images and the buttons
    private void initViews() {
        findViewById(R.id.processButton).setOnClickListener(v -> {
            try {
                if (isModelBert)
                    processTextBert();
                else
                    processTextGptNeo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Object item = parent.getItemAtPosition(pos);

        Toast.makeText(this, "Model changed to " + item.toString(), Toast.LENGTH_SHORT).show();

        // Default model selected in Bert - case 0
        switch(pos) {
            case 0:
                isModelBert = true;
                MODELNAME = "bertsequenceclassif.ptl";
                VOCABDICTIONARY = "bertvocab.txt";
                try {
                    if (module == null)
                        module = LiteModuleLoader.load(assetFilePath(this, MODELNAME));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                isModelBert = false;
                MODELNAME = "gptneotflite.tflite";
                VOCABDICTIONARY = "gptneovocab.txt";
                try {
                    if (tfliteModule == null)
                        tfliteModule = new Interpreter(loadModelFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Default model is Bert is nothing is selected
        isModelBert = true;
        MODELNAME = "bertsequenceclassif.ptl";
        VOCABDICTIONARY = "bertvocab.txt";
        try {
            if (module == null)
                module = LiteModuleLoader.load(assetFilePath(this, MODELNAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Utility function to load and read tflite file
     */
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor=this.getAssets().openFd(MODELNAME);
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=fileDescriptor.getStartOffset();
        long declareLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
    }

    /**
     * Copies specified asset to the file in /files app directory and returns this file absolute path.
     *
     * @return absolute file path
     */
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    /*
    Process text from EditText space in the app with Bert model
     */
    public void processTextBert() throws IOException {
        // Populate vocabulary if empty
        if (vocabMapBert == null) {
            vocabMapBert = new LinkedHashMap<>();
            try (Stream<String> stream = Files.lines(Paths.get(assetFilePath(this, VOCABDICTIONARY)))) {
                stream.forEach(s -> vocabMapBert.put(s, 0));
            }
            int i=0;
            for (String key : vocabMapBert.keySet()) {
                vocabMapBert.put(key, i++);
            }
        }

        BertTokenizer bt = new BertTokenizer(vocabMapBert);

        // Input string from EditText
        String inputString   = ((EditText)findViewById(R.id.inputTextView)).getText().toString();
        System.out.println("Input String: " + inputString);
        // Tokenized form of the input string
        int[] tokenizedText = bt.finalTokenization(bt.wordTokenization(bt.preTokenization(inputString)));

        System.out.println("Tokenized text: " + Arrays.toString(tokenizedText));

        /* Uncomment for debuggging */
        // int[] tokenizedText = finalTokenization(wd.wordTokenization(preTokenization("Hello World")));
        // System.out.println("tokenizedText: " + Arrays.toString(tokenizedText));

        // Tensor form of the tokenized input string
        final Tensor inputTensor = Tensor.fromBlob(tokenizedText, new long[]{1, tokenizedText.length});

        // running the model
        final Map<String, IValue> outputTensor = module.forward(IValue.from(inputTensor)).toDictStringKey();

        // Viewing the output to retrieve the classification scores
        for (Map.Entry<String, IValue> entry : outputTensor.entrySet()) {

            // getting tensor content as java array of floats
            final float[] scores = entry.getValue().toTensor().getDataAsFloatArray();

            // searching for the index with maximum score
            float maxScore = -Float.MAX_VALUE;
            int maxScoreIdx = -1;
            for (int im = 0; im < scores.length; im++) {
                if (scores[im] > maxScore) {
                    maxScore = scores[im];
                    maxScoreIdx = im;
                }
            }

            // Set resultant classification label in TextView
            TextView resultTextView = (TextView)findViewById(R.id.resultTextView);
            if(maxScoreIdx==0){
                resultTextView.setText(R.string.Sensitive);
                System.out.println("Result: Sensitive");
            }else{
                resultTextView.setText(R.string.NonSensitive);
                System.out.println("Result: Non-sensitive");
            }
        }

    }

    /*
    Process text from EditText space in the app with GPT Neo-125M model
     */
    private void processTextGptNeo() throws IOException {
        // Populate vocabulary if empty
        if (vocabMapGptNeo == null) {
            vocabMapGptNeo = new LinkedHashMap<>();
            AssetManager am = this.getAssets();
            InputStream is = am.open(VOCABDICTIONARY);
            if (is != null) {
                InputStreamReader inputReader = new InputStreamReader(is);
                BufferedReader bufferedReader = new BufferedReader(inputReader);
                String line;
                int i=0;
                try {
                    while ((line = bufferedReader.readLine()) != null)
                        vocabMapGptNeo.put(line, i++);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        GptNeoTokenizer gptTok = new GptNeoTokenizer(vocabMapGptNeo);

        // Input string from EditText
        String inputString   = ((EditText)findViewById(R.id.inputTextView)).getText().toString();
        // Tokenized form of the input string
        long[] tokenizedText = gptTok.finalTokenization(gptTok.wordTokenization(inputString));

        // Initializing the output variable
        float[][] output=new float[1][2];
        // running the model
        tfliteModule.run(tokenizedText,output);

        // Set resultant classification label in TextView
        TextView resultTextView = (TextView)findViewById(R.id.resultTextView);
        if(output[0][0]>output[0][1]){
            resultTextView.setText(R.string.Sensitive);
            System.out.println("Result: Sensitive");
        }else{
            resultTextView.setText(R.string.NonSensitive);
            System.out.println("Result: Non-sensitive");
        }
    }
}