package nix.projectbot.service;

import lombok.extern.slf4j.Slf4j;
import nix.projectbot.config.BotConfig;
import nix.projectbot.model.User;
import nix.projectbot.model.UserData;
import nix.projectbot.repository.UserDataRepository;
import nix.projectbot.repository.UserRepository;
import nix.projectbot.util.GoogleTranscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;



@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig botConfig;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDataRepository userDataRepository;

    public TelegramBot(@Autowired BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {

            long chatId = update.getMessage().getChatId();

            String message = update.getMessage().getText();
            log.info(message);
            switch (message) {
                case "/start": {
                    registerUser(update.getMessage());
                    sendTextMessage(chatId, "Hi, " + update.getMessage().getChat().getFirstName() + "! Please record audio for transcribtion");
                    break;
                }
                default: {
                    sendTextMessage(chatId, "Don't understand you! Just record audio!");
                    break;
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasVoice()) {
            var fileId = update.getMessage().getVoice().getFileId();
            GetFile uploadedFile = new GetFile();
            uploadedFile.setFileId(fileId);
            long chatId = update.getMessage().getChatId();

            String filePath = null;
            try {
                File outputFile = new File("tmp.oga");
                filePath = execute(uploadedFile).getFilePath();
                File file = downloadFile(filePath, outputFile);
                String transcript = GoogleTranscribe.syncRecognizeFile(outputFile.getAbsolutePath());
                storeAudio(update.getMessage(), Files.readAllBytes(file.toPath()), transcript);
                sendTextMessage(chatId, transcript);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            } catch (IOException e) {
                log.error(e.getMessage());
            } catch (Exception e) {
                log.error(e.getMessage());
            }


        }

    }

    private void registerUser(Message message) {
        if(userRepository.findById(message.getChatId()).isEmpty()) {

            var chat = message.getChat();
            var user = new User(message.getChatId(),
                    chat.getFirstName(),
                    chat.getLastName(),
                    chat.getUserName(),
                    new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
            log.info("User saved :" + user);
        }
    }

    private void storeAudio(Message message, byte[] data, String transcript) {

            var chat = message.getChat();

            var user = userRepository.findByChatId(message.getChatId());
            if(user == null) {
                user = new User(message.getChatId(),
                        chat.getFirstName(),
                        chat.getLastName(),
                        chat.getUserName(),
                        new Timestamp(System.currentTimeMillis()));
                userRepository.save(user);
                log.info("User saved :" + user);
            }

            var userData = new UserData();
            userData.setData(data);
            userData.setTranscript(transcript);
            userData.setOwner(user);
            userDataRepository.save(userData);
            log.info("User data & transcript saved :" + userData);

    }

    public void sendTextMessage(long chatId, String message){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException ex) {
            log.error(ex.getMessage());
        }

    }

}
