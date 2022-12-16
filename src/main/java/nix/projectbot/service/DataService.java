package nix.projectbot.service;

import nix.projectbot.model.UserData;
import nix.projectbot.repository.UserDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class DataService {

    private UserDataRepository userDataRepository;

    private static int counter;

    @Autowired
    public DataService(UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
    }

    public File getTemporaryFileOfAudioData(long audioId) {
        var userData = userDataRepository.findById(audioId);
        String fileName = counter++ + ".ogg";
        File targetFile = new File("audio/" + fileName);
        try (OutputStream outStream = new FileOutputStream(targetFile)) {
            outStream.write(userData.get().getData());
        } catch (Exception ex) {
            throw new RuntimeException(ex.getCause());
        }
        return targetFile;
    }
}
