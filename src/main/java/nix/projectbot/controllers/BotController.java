package nix.projectbot.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nix.projectbot.dto.UserDTO;
import nix.projectbot.dto.UserMapper;
import nix.projectbot.model.User;
import nix.projectbot.repository.UserDataRepository;
import nix.projectbot.repository.UserRepository;
import nix.projectbot.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@Controller
@RequestMapping("/botinfo")
public class BotController {

    private final UserRepository userRepository;

    private final DataService dataService;

    Logger logger = LoggerFactory.getLogger(BotController.class);

    @Autowired
    public BotController(UserRepository userRepository, UserDataRepository userDataRepository, DataService dataService) {
        this.userRepository = userRepository;
        this.dataService = dataService;
    }

    @GetMapping()
    public String index(Model model) {
        logger.info("index");
        Stream<User> stream = StreamSupport.stream(userRepository.findAll().spliterator(), false);
        model.addAttribute("users", stream.map(UserMapper.INSTANCE::userToUserDTO).collect(Collectors.toList()));
        return "bot/index";
    }

    @GetMapping("/user/{id}")
    public String showUser(Model model,  @PathVariable("id") long id) {
        logger.info("showUser");
        User user = userRepository.findById(id).get();
        UserDTO userDTO = UserMapper.INSTANCE.userToUserDTO(user);
        model.addAttribute("user", userDTO);
        model.addAttribute("data", user.getUserData());
        return "bot/show";
    }

    @RequestMapping(value = "/audio/{audioId}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_OCTET_STREAM_VALUE })
    public ResponseEntity playAudio(HttpServletRequest request, HttpServletResponse response, @PathVariable("audioId") long audioId) throws FileNotFoundException {
        logger.info("playAudio");
        File targetFile = dataService.getTemporaryFileOfAudioData(audioId);
        InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream(targetFile));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentLength(targetFile.length());
        httpHeaders.setCacheControl(CacheControl.noCache().getHeaderValue());
        return new ResponseEntity(inputStreamResource, httpHeaders, HttpStatus.OK);
    }


}
