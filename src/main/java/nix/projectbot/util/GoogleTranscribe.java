package nix.projectbot.util;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
@Slf4j
@Component
public class GoogleTranscribe {

    private static String pathToServiceAccount;
    static {
        setPrivateName();
    }


    public static void setPrivateName() {
        File file = null;
        try {
            file = ResourceUtils.getFile("classpath:serviceaccount.json");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        GoogleTranscribe.pathToServiceAccount = file.getAbsolutePath();
    }

    public static String syncRecognizeFile(String fileName) throws Exception {

        String sResult = "";
        CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream(pathToServiceAccount)));

        SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();

        try (SpeechClient speech = SpeechClient.create(settings)) {
            Path path = Paths.get(fileName);
            byte[] data = Files.readAllBytes(path);
            ByteString audioBytes = ByteString.copyFrom(data);

            // Configure request with local raw PCM audio
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.OGG_OPUS)
                            .setLanguageCode("ru-RU")
                            .setSampleRateHertz(48000)
                            .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

            // Use blocking call to get audio transcript
            RecognizeResponse response = speech.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                sResult =  alternative.getTranscript();
                break;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return sResult;
    }
}
