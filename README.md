# CSP Lab NLP Sentence Sensitivity Classifier

This project is developed under the Computer Security and Privacy Lab of University of Goettingen. Typing any sentence in the text field within the android application, the mobile version of BertForSequenceClassification or GPT-Neo-125M-ForSequence Classification model will classify the sentence to either Sensitive or Non-sensitive.

*****
## Project Structure:-

Assests folder(/app/src/main/assets/):-
1. bertsequenceclassif.ptl - Pytorch mobile version of BertForSequenceClassification model
2. bertvocab.txt - Uncased vocabulary dictionary used by bert for string tokenization
3. gptneotflite.tflite - Tflite mobile version of GPT Neo-125M For Sequence Classification
4. gptneovocab.txt - Uncased vocabulary dictionary used by GPT Neo-125M for string tokenization
*****

*****
## How to run the Android project:-

1. Clone the repository into local.
2. Import the project in Android Studio.
3. Perform a clean Gradle build in Android Studio(if any complications arise, delete the '.idea' and '.gradle' folder from the project structure and perform a clean build).
4. Run the application in an emulator or Android mobile and type any sentence in the provided EditText area.
5. Click on Process button.
6. The sentence classification of either Sensitive or Non-sensitive will be displayed in the app as well as in the Log space of the Android Studio.
*****

*****
## Tokenization process within Android application

A properly published package from a reliable open source could not be found from either HuggingFace or from any other Python library for the tokenization process that is accepted by BERT and GPT Neo-125M for NLP tasks. Each different model and version of Bert and GPT Neo uses different tokenization process and it was very important to use the same tokenization process which was used for fine-tuning the model, but unfortunately because of unavailability of exact similar tokenizer library for Android, the tokenization logic had to written from scratch in Java. Several rounds of analysis and cross verification were needed to be performed with respect to the already present tokenization method in the Jupyter notebook and the algorithm used behind the process could be replicated without using any external jar file, reducing overload from the Android application and building the whole tokenization process within the project workflow.

The BertTokenizer class tokenizes a piece of text into its word pieces. This uses a greedy longest-match-first
algorithm to perform tokenization using the given vocabulary within a map. For example: input = "unaffable",
output = \["un", "##aff", "##able"].

The GptNeoTokenizer class tokenizes a piece of text into its word pieces. This uses a greedy longest-match-first
algorithm to perform tokenization using the given vocabulary within a map. For example: input = "unaffable",
output = \["un", "aff", "able"]. Words with a preceding blank space have different tokenization output than the one without it. For example, tokenization of "Hello" is different from " Hello".
*****

*****
## Jupiter notebook list (inside 'jyputer notebook' directory):-

1. BERT_Fine_Tuning_Sentence_Classification_v4 final.ipynb - Fine-tunes the pretrained BertForSequenceClassification from Pytorch Hugging Face library with provided small dataset. The small dataset that is provided is being divided into training, validation and test sets to first train the last classification layer upon our new dataset and then to validate and test its predictions. The notebook is very well documented inside and thus every information required can be found while running it. At the end of the notebook a mobile version of the model is generated stored with the .ptl extension.

2. Twitter_NLP_GPTNeoAndClassification.ipynb - Fine-tunes the pretrained GPT Neo-125M For Sequence Classification from Pytorch Hugging Face library with provided small dataset. The small dataset that is provided is being divided into training, validation and test sets to first train the last classification layer upon our new dataset and then to validate and test its predictions. The notebook is very well documented inside and thus every information required can be found while running it. At the end of the notebook a tflite mobile version of the model is generated stored with the .tflite extension.

*Note: Android application threw an error during the import of the models which were converted to their mobile versions from the jupyter notebook keeping GPU as the backend. This is because the android application required the models and its parameters to be in CPU backend, even though the model is trained with GPU backend. Thus after training and testing the model in GPU backend, it needs to be saved in some storage area, and then the runtime backend needs to be changed to CPU after which the saved model needs to be imported in the jupyter notebook (currently running with CPU runtime) and transformed to .ptl  and .tflite versions respectively.
*****

*****
## Limitations, improvements and important notes:-

1. The size of the mobile version of the Bert model even after optimization is more than 400Mbs in size and for GPT Neo-125M is almost 580Mbs. This can be bit too big for a mobile application so finding a smaller alternative version of the model having the same capability is of prime importance.
2. To maximize the score, we should remove the "validation set" (which we used to help determine how many epochs to train for) and train on the entire training set since dataset for fine-tuning was very small.
3. Hyperparameter tuning (adjusting the learning rate, epochs, batch size, ADAM properties, etc.) can be a good idea during the fine-tuning process of the models.
4. Multiple times Pytorch failed to read the bert mobile model and put it into a Module object in the android application. After some research, the solution came out to be the re-computation of the .ptl model file from jupyter notebook and use it in the Android application. It will be the exact same model as before which was not getting imported properly, but re-generating it at the jupyter notebook side after running the notebook from scratch solved the issue. The error that was thrown was like: 'PytorchStreamReader failed reading file constants/102.storage: CRC-32 check failed ()'
More details can be found on - https://discuss.pytorch.org/t/error-on-torch-load-pytorchstreamreader-failed/95103/12
5. The reading from the gptneovocab.txt file from the Assets folder in the android application sometimes created a problem. The special characters were not getting read properly which got discovered while debugging, even though the same process which failed was successful in the initial stages. Thus for any future occurrence of the same kind of issue, different way of accessing the gptneovocab.txt file from the Assets folder should be tried out.
